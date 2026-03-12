package com.doogoo.doogoo.crawl;

import com.doogoo.doogoo.academic.application.AcademicScheduleSyncService;
import com.doogoo.doogoo.academic.domain.AcademicScheduleDto;
import com.doogoo.doogoo.classify.AiClassifier;
import com.doogoo.doogoo.classify.AiClassifyResult;
import com.doogoo.doogoo.common.error.ErrorCode;
import com.doogoo.doogoo.common.log.JsonLog;
import com.doogoo.doogoo.common.log.LogDto;
import com.doogoo.doogoo.dodream.application.EventSyncService;
import com.doogoo.doogoo.dodream.domain.Event;
import com.doogoo.doogoo.dodream.domain.EventDto;
import com.doogoo.doogoo.global.config.CrawlConfig;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class CrawlScheduler {

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
        long start = System.currentTimeMillis();
        JsonLog.info(
                CrawlScheduler.class, new LogDto.CrawlStartLog(
                        "dodream.crawl.start",
                        "dodream",
                        config.getRegularPages(),
                        config.getActiveStatuses().size()
                ));

        int newCount = 0;
        int updateCount = 0;
        int closedCount = 0;
        int uniqueCount = 0;

        try {
            Map<Long, EventDto> allEvents = new LinkedHashMap<>();

            for (String status : config.getActiveStatuses()) {
                JsonLog.debug(
                        CrawlScheduler.class, new LogDto.CrawlStatusLog(
                                "dodream.crawl.status.start",
                                "dodream",
                                status,
                                null
                        ));

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

            uniqueCount = allEvents.size();


            JsonLog.info(
                    CrawlScheduler.class, new LogDto.CrawlDiscoveredLog(
                            "dodream.crawl.discovered",
                            "dodream",
                            uniqueCount
                    ));

            for (EventDto dto : allEvents.values()) {
                try {
                    if (syncService.tryUpdateFromList(dto)) {
                        updateCount++;
                    } else {
                        Document detailDoc = crawler.fetchDetailPage(dto.dodreamId());
                        EventDto detailed = parser.parseDetail(detailDoc, dto);
                        AiClassifyResult aiResult = aiClassifier.classify(
                                detailed.title(),
                                detailed.description(),
                                detailed.department());
                        String summary = aiClassifier.summarize(detailed.title(), detailed.description());
                        syncService.saveNew(detailed, aiResult, summary);
                        newCount++;
                    }
                } catch (Exception e) {
                    JsonLog.warn(
                            CrawlScheduler.class, new LogDto.ErrorLog(
                                    "dodream.crawl.item.fail",
                                    "dodream:" + dto.dodreamId(),
                                    ErrorCode.CRAWL_FAILED.getStatus().value(),
                                    ErrorCode.CRAWL_FAILED.getCode(),
                                    e.getMessage()
                            ), e);
                }
            }
            long latency = System.currentTimeMillis() - start;

            JsonLog.info(
                    CrawlScheduler.class, new LogDto.RegularCrawlSummaryLog(
                            "dodream.crawl.complete",
                            "dodream",
                            newCount,
                            updateCount,
                            closedCount,
                            uniqueCount,
                            latency
                    ));
        } catch (Exception e) {
            JsonLog.error(
                    CrawlScheduler.class, new LogDto.ErrorLog(
                            "dodream.crawl.fail",
                            "task:dodream-regular",
                            ErrorCode.CRAWL_FAILED.getStatus().value(),
                            ErrorCode.CRAWL_FAILED.getCode(),
                            e.getMessage()
                    ), e);
        }


    }

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void fullSync() {
        long start = System.currentTimeMillis();

        JsonLog.info(
                CrawlScheduler.class, new LogDto.CrawlStartLog(
                        "dodream.crawl.start",
                        "dodream-full-sync",
                        null,
                        null
                ));
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
                    String summary = descriptionChanged
                            ? aiClassifier.summarize(detailed.title(), detailed.description())
                            : null;

                    syncService.enrichWithDetail(detailed, aiResult, summary);
                    syncCount++;
                    if (!descriptionChanged) skipCount++;
                } catch (Exception e) {
                    JsonLog.warn(
                            CrawlScheduler.class, new LogDto.ErrorLog(
                                    "dodream.crawl.item.fail",
                                    "dodream:" + event.getDodreamId(),
                                    ErrorCode.CRAWL_FAILED.getStatus().value(),
                                    ErrorCode.CRAWL_FAILED.getCode(),
                                    e.getMessage()
                            ), e);
                }
            }
            long latency = System.currentTimeMillis() - start;

            JsonLog.info(
                    CrawlScheduler.class, new LogDto.FullSyncSummaryLog(
                            "dodream.crawl.complete",
                            "dodream-full-sync",
                            syncCount,
                            skipCount,
                            openEvents.size(),
                            latency
                    ));
        } catch (Exception e) {
            JsonLog.error(
                    CrawlScheduler.class, new LogDto.ErrorLog(
                            "dodream.crawl.fail",
                            "task:dodream-full-sync",
                            ErrorCode.CRAWL_FAILED.getStatus().value(),
                            ErrorCode.CRAWL_FAILED.getCode(),
                            e.getMessage()
                    ), e);
        }


    }

    @Scheduled(cron = "0 0 2 1 1 *", zone = "Asia/Seoul")
    public void crawlAcademicSchedule() {
        crawlAcademicScheduleForYear(LocalDate.now().getYear());
    }

    public void crawlAcademicScheduleForYear(int year) {
        try {
            Document doc = academicCrawler.fetchCalendar(year);
            List<AcademicScheduleDto> dtos = academicParser.parse(doc, year);
            academicSyncService.replaceByYear(year, dtos);
        } catch (Exception e) {
            JsonLog.error(
                    CrawlScheduler.class, new LogDto.ErrorLog(
                            "academic.crawl.fail",
                            "academic:" + year,
                            ErrorCode.ACADEMIC_CRAWL_FAILED.getStatus().value(),
                            ErrorCode.ACADEMIC_CRAWL_FAILED.getCode(),
                            e.getMessage()
                    ), e);
        }
    }

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void cleanExpiredEvents() {
        long start = System.currentTimeMillis();

        JsonLog.info(
                CrawlScheduler.class, new LogDto.CrawlStartLog(
                        "event.cleanup.start",
                        "expired-events",
                        null,
                        null
                ));
        try {
            int closedCount = syncService.deleteExpiredEvents();
            long latency = System.currentTimeMillis() - start;

            JsonLog.info(
                    CrawlScheduler.class, new LogDto.CleanupSummaryLog(
                            "event.cleanup.complete",
                            "expired-events",
                            closedCount,
                            latency
                    ));
        } catch (Exception e) {
            JsonLog.error(
                    CrawlScheduler.class, new LogDto.ErrorLog(
                            "event.cleanup.fail",
                            "task:cleanup-expired",
                            ErrorCode.CRAWL_FAILED.getStatus().value(),
                            ErrorCode.CRAWL_FAILED.getCode(),
                            e.getMessage()
                    ), e);
        }
    }
}
