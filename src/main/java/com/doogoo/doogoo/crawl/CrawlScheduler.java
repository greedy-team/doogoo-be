package com.doogoo.doogoo.crawl;

import com.doogoo.doogoo.academic.application.AcademicScheduleSyncService;
import com.doogoo.doogoo.academic.domain.AcademicScheduleDto;
import com.doogoo.doogoo.classify.AiClassifier;
import com.doogoo.doogoo.classify.AiClassifyResult;
import com.doogoo.doogoo.common.error.ErrorCode;
import com.doogoo.doogoo.dodream.application.EventSyncService;
import com.doogoo.doogoo.dodream.domain.Event;
import com.doogoo.doogoo.dodream.domain.EventDto;
import com.doogoo.doogoo.global.config.CrawlConfig;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class CrawlScheduler {

    private static final Logger log = LoggerFactory.getLogger(CrawlScheduler.class);

    private final DodreamCrawler crawler;
    private final DodreamParser parser;
    private final AiClassifier aiClassifier;
    private final EventSyncService syncService;
    private final CrawlConfig config;
    private final AcademicCrawler academicCrawler;
    private final AcademicParser academicParser;
    private final AcademicScheduleSyncService academicSyncService;

    public CrawlScheduler(
            DodreamCrawler crawler,
            DodreamParser parser,
            AiClassifier aiClassifier,
            EventSyncService syncService,
            CrawlConfig config,
            AcademicCrawler academicCrawler,
            AcademicParser academicParser,
            AcademicScheduleSyncService academicSyncService
    ) {
        this.crawler = crawler;
        this.parser = parser;
        this.aiClassifier = aiClassifier;
        this.syncService = syncService;
        this.config = config;
        this.academicCrawler = academicCrawler;
        this.academicParser = academicParser;
        this.academicSyncService = academicSyncService;
    }

    @Scheduled(cron = "0 0 0 */2 * *", zone = "Asia/Seoul")
    public void regularCrawl() {
        log.info("=== 정기 크롤링 시작 ===");
        int newCount = 0;
        int updateCount = 0;
        int closedCount = 0;

        try {
            Map<Long, EventDto> allEvents = new LinkedHashMap<>();

            for (String status : config.getActiveStatuses()) {
                log.info("상태 '{}' 크롤링 시작", status);

                for (int page = 1; page <= config.getRegularPages(); page++) {
                    Document doc = crawler.fetchListPage(page, status);

                    List<Long> closedIds = parser.parseClosedIds(doc);
                    if (!closedIds.isEmpty()) {
                        syncService.markClosedBatch(closedIds);
                        closedCount += closedIds.size();
                    }

                    List<EventDto> events = parser.parseList(doc);
                    for (EventDto dto : events) {
                        allEvents.putIfAbsent(dto.dodreamId(), dto);
                    }
                }
            }

            log.info("총 {}개의 고유 공지 발견", allEvents.size());

            for (EventDto dto : allEvents.values()) {
                try {
                    if (syncService.tryUpdateFromList(dto)) {
                        updateCount++;
                    } else {
                        Document detailDoc = crawler.fetchDetailPage(dto.dodreamId());
                        EventDto detailed = parser.parseDetail(detailDoc, dto);
                        AiClassifyResult aiResult = aiClassifier.classify(detailed.title(), detailed.description(), detailed.department());
                        syncService.saveNew(detailed, aiResult);
                        newCount++;
                    }
                } catch (Exception e) {
                    log.warn("[{}] {}: dodreamId={}", ErrorCode.CRAWL_FAILED.getCode(), ErrorCode.CRAWL_FAILED.getMessage(), dto.dodreamId(), e);
                }
            }
        } catch (Exception e) {
            log.error("[{}] {}: {}", ErrorCode.CRAWL_FAILED.getCode(), ErrorCode.CRAWL_FAILED.getMessage(), e.getMessage(), e);
        }

        log.info("=== 정기 크롤링 완료: 신규={}, 업데이트={}, 마감={} ===", newCount, updateCount, closedCount);
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void fullSync() {
        log.info("=== 전체 동기화 시작 ===");
        int syncCount = 0;
        int skipCount = 0;

        try {
            List<Event> openEvents = syncService.findOpenEvents();
            for (Event event : openEvents) {
                try {
                    Document detailDoc = crawler.fetchDetailPage(event.getDodreamId());
                    EventDto detailed = parser.parseDetail(detailDoc, event.toDto());

                    boolean descriptionChanged = !Objects.equals(event.getDescription(), detailed.description());
                    AiClassifyResult aiResult = descriptionChanged
                            ? aiClassifier.classify(detailed.title(), detailed.description(), detailed.department())
                            : null;

                    syncService.enrichWithDetail(detailed, aiResult);
                    syncCount++;
                    if (!descriptionChanged) skipCount++;
                } catch (Exception e) {
                    log.warn("[{}] {}: dodreamId={}", ErrorCode.CRAWL_FAILED.getCode(), ErrorCode.CRAWL_FAILED.getMessage(), event.getDodreamId(), e);
                }
            }
        } catch (Exception e) {
            log.error("[{}] {}: {}", ErrorCode.CRAWL_FAILED.getCode(), ErrorCode.CRAWL_FAILED.getMessage(), e.getMessage(), e);
        }

        log.info("=== 전체 동기화 완료: {}건 처리, {}건 AI 재분류 생략 ===", syncCount, skipCount);
    }

    @Scheduled(cron = "0 0 2 1 1 *", zone = "Asia/Seoul")
    public void crawlAcademicSchedule() {
        int year = LocalDate.now().getYear();
        crawlAcademicScheduleForYear(year);
        crawlAcademicScheduleForYear(year - 1);
    }

    public void crawlAcademicScheduleForYear(int year) {
        log.info("=== 학사일정 크롤링 시작: year={} ===", year);
        try {
            Document doc = academicCrawler.fetchCalendar(year);
            List<AcademicScheduleDto> dtos = academicParser.parse(doc, year);
            academicSyncService.replaceByYear(year, dtos);
            log.info("=== 학사일정 크롤링 완료: year={}, {}건 ===", year, dtos.size());
        } catch (Exception e) {
            log.error("[{}] {}: year={}", ErrorCode.ACADEMIC_CRAWL_FAILED.getCode(), ErrorCode.ACADEMIC_CRAWL_FAILED.getMessage(), year, e);
        }
    }

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void cleanExpiredEvents() {
        log.info("=== 만료 공지 정리 시작 ===");
        try {
            int closedCount = syncService.closeExpiredEvents();
            log.info("=== 만료 공지 정리 완료: {}건 마감 처리 ===", closedCount);
        } catch (Exception e) {
            log.error("[{}] {}: {}", ErrorCode.CRAWL_FAILED.getCode(), ErrorCode.CRAWL_FAILED.getMessage(), e.getMessage(), e);
        }
    }
}
