package com.doogoo.doogoo.crawl;

import com.doogoo.doogoo.classify.AiClassifier;
import com.doogoo.doogoo.classify.AiClassifyResult;
import com.doogoo.doogoo.config.CrawlConfig;
import com.doogoo.doogoo.event.Event;
import com.doogoo.doogoo.event.EventDto;
import com.doogoo.doogoo.event.EventSyncService;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CrawlScheduler {

    private static final Logger log = LoggerFactory.getLogger(CrawlScheduler.class);

    private final DodreamCrawler crawler;
    private final DodreamParser parser;
    private final AiClassifier aiClassifier;
    private final EventSyncService syncService;
    private final CrawlConfig config;

    public CrawlScheduler(
            DodreamCrawler crawler,
            DodreamParser parser,
            AiClassifier aiClassifier,
            EventSyncService syncService,
            CrawlConfig config
    ) {
        this.crawler = crawler;
        this.parser = parser;
        this.aiClassifier = aiClassifier;
        this.syncService = syncService;
        this.config = config;
    }

    @Scheduled(cron = "0 0 */6 * * *")
    public void regularCrawl() {
        log.info("=== 정기 크롤링 시작 ===");
        int newCount = 0;
        int updateCount = 0;
        int closedCount = 0;

        try {
            for (int page = 1; page <= config.getRegularPages(); page++) {
                Document doc = crawler.fetchListPage(page);

                List<Long> closedIds = parser.parseClosedIds(doc);
                if (!closedIds.isEmpty()) {
                    syncService.markClosedBatch(closedIds);
                    closedCount += closedIds.size();
                }

                List<EventDto> openEvents = parser.parseList(doc);

                for (EventDto dto : openEvents) {
                    try {
                        if (syncService.tryUpdateFromList(dto)) {
                            updateCount++;
                        } else {
                            Document detailDoc = crawler.fetchDetailPage(dto.dodreamId());
                            EventDto detailed = parser.parseDetail(detailDoc, dto);
                            AiClassifyResult aiResult = aiClassifier.classify(detailed.title(), detailed.description());
                            syncService.saveNew(detailed, aiResult);
                            newCount++;
                        }
                    } catch (Exception e) {
                        log.warn("공지 처리 실패 - dodreamId: {}, 다음 공지로 넘어감: {}", dto.dodreamId(), e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("정기 크롤링 중 오류 발생: {}", e.getMessage(), e);
        }

        log.info("=== 정기 크롤링 완료: 신규={}, 업데이트={}, 마감={} ===", newCount, updateCount, closedCount);
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void fullSync() {
        log.info("=== 전체 동기화 시작 ===");
        int syncCount = 0;

        try {
            List<Event> openEvents = syncService.findOpenEvents();
            for (Event event : openEvents) {
                try {
                    Document detailDoc = crawler.fetchDetailPage(event.getDodreamId());
                    EventDto detailed = parser.parseDetail(detailDoc, event.toDto());
                    AiClassifyResult aiResult = aiClassifier.classify(detailed.title(), detailed.description());
                    syncService.enrichWithDetail(detailed, aiResult);
                    syncCount++;
                } catch (Exception e) {
                    log.warn("전체 동기화 중 공지 처리 실패 - dodreamId: {}: {}", event.getDodreamId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("전체 동기화 중 오류 발생: {}", e.getMessage(), e);
        }

        log.info("=== 전체 동기화 완료: {}건 처리 ===", syncCount);
    }

    @Scheduled(cron = "0 0 4 * * *")
    public void cleanExpiredEvents() {
        log.info("=== 만료 공지 정리 시작 ===");

        try {
            int closedCount = syncService.closeExpiredEvents();
            log.info("=== 만료 공지 정리 완료: {}건 마감 처리 ===", closedCount);
        } catch (Exception e) {
            log.error("만료 공지 정리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
