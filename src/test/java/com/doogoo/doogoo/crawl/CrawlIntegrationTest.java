package com.doogoo.doogoo.crawl;

import com.doogoo.doogoo.config.CrawlConfig;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("두드림 크롤링 통합 테스트")
@Disabled("실제 사이트 크롤링 - 필요시에만 수동 실행")
class CrawlIntegrationTest {

    private DodreamCrawler crawler;
    private CrawlConfig config;

    @BeforeEach
    void setUp() {
        config = new CrawlConfig();
        config.setBaseUrl("https://do.sejong.ac.kr");
        config.setListPath("/ko/program/all/list/all/{page}");
        config.setDetailPath("/ko/program/all/view/{id}");
        config.setDelayMs(1500);
        config.setActiveStatuses(List.of("scheduled", "open", "operation"));

        crawler = new DodreamCrawler(config);
    }

    @Test
    @DisplayName("접수예정 상태로 목록 크롤링")
    void fetchListPage_WithScheduledStatus() {
        Document doc = crawler.fetchListPage(1, "scheduled");

        assertNotNull(doc, "크롤링된 문서가 null이 아님");
        assertFalse(doc.select("ul[data-role=list]").isEmpty(), "목록이 존재");
    }

    @Test
    @DisplayName("접수중 상태로 목록 크롤링")
    void fetchListPage_WithOpenStatus() {
        Document doc = crawler.fetchListPage(1, "open");

        assertNotNull(doc, "크롤링된 문서가 null이 아님");
        assertFalse(doc.select("ul[data-role=list]").isEmpty(), "목록이 존재");
    }

    @Test
    @DisplayName("운영중 상태로 목록 크롤링")
    void fetchListPage_WithOperationStatus() {
        Document doc = crawler.fetchListPage(1, "operation");

        assertNotNull(doc, "크롤링된 문서가 null이 아님");
        assertFalse(doc.select("ul[data-role=list]").isEmpty(), "목록이 존재");
    }

    @Test
    @DisplayName("각 상태별 크롤링 결과가 다름")
    void fetchListPage_DifferentResultsPerStatus() {
        Document scheduledDoc = crawler.fetchListPage(1, "scheduled");
        Document openDoc = crawler.fetchListPage(1, "open");
        Document operationDoc = crawler.fetchListPage(1, "operation");

        int scheduledCount = scheduledDoc.select("li[data-role=item]").size();
        int openCount = openDoc.select("li[data-role=item]").size();
        int operationCount = operationDoc.select("li[data-role=item]").size();

        System.out.println("scheduled: " + scheduledCount + "개");
        System.out.println("open: " + openCount + "개");
        System.out.println("operation: " + operationCount + "개");
    }
}
