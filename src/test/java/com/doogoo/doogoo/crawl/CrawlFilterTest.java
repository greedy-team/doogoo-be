package com.doogoo.doogoo.crawl;

import com.doogoo.doogoo.config.CrawlConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("두드림 크롤링 - 마감 필터링 테스트")
class CrawlFilterTest {

    private CrawlConfig config;

    @BeforeEach
    void setUp() {
        config = new CrawlConfig();
        config.setBaseUrl("https://do.sejong.ac.kr");
        config.setListPath("/ko/program/all/list/all/{page}");
        config.setDetailPath("/ko/program/all/view/{id}");
        config.setActiveStatuses(List.of("scheduled", "open", "operation"));
    }

    @Test
    @DisplayName("활성 상태 목록에 마감(completed)이 포함되지 않음")
    void activeStatuses_ShouldNotIncludeCompleted() {
        List<String> statuses = config.getActiveStatuses();

        assertEquals(3, statuses.size(), "활성 상태는 3개여야 함");
        assertTrue(statuses.contains("scheduled"), "scheduled 포함");
        assertTrue(statuses.contains("open"), "open 포함");
        assertTrue(statuses.contains("operation"), "operation 포함");
        assertFalse(statuses.contains("completed"), "completed는 제외되어야 함");
    }

    @Test
    @DisplayName("상태 필터가 적용된 목록 URL 생성")
    void buildListUrl_WithStatus() {
        String url1 = config.buildListUrl(1, "scheduled");
        assertEquals(
            "https://do.sejong.ac.kr/ko/program/all/list/all/1?status=scheduled",
            url1,
            "접수예정 URL 생성"
        );

        String url2 = config.buildListUrl(2, "open");
        assertEquals(
            "https://do.sejong.ac.kr/ko/program/all/list/all/2?status=open",
            url2,
            "접수중 URL 생성"
        );

        String url3 = config.buildListUrl(1, "operation");
        assertEquals(
            "https://do.sejong.ac.kr/ko/program/all/list/all/1?status=operation",
            url3,
            "운영중 URL 생성"
        );
    }

    @Test
    @DisplayName("상세 페이지 URL이 올바르게 생성됨")
    void buildDetailUrl_ShouldGenerateCorrectUrl() {
        String detailUrl = config.buildDetailUrl(12345L);

        assertEquals(
            "https://do.sejong.ac.kr/ko/program/all/view/12345",
            detailUrl,
            "상세 URL 생성"
        );
    }

    @Test
    @DisplayName("기존 목록 URL 생성 메서드도 정상 작동")
    void buildListUrl_WithoutStatus() {
        String url = config.buildListUrl(1);

        assertEquals(
            "https://do.sejong.ac.kr/ko/program/all/list/all/1",
            url,
            "필터 없는 URL 생성"
        );
    }

    @Test
    @DisplayName("모든 활성 상태에 대해 URL 생성 가능")
    void buildListUrl_ForAllActiveStatuses() {
        List<String> statuses = config.getActiveStatuses();

        for (String status : statuses) {
            String url = config.buildListUrl(1, status);

            assertNotNull(url, status + " URL이 null이 아님");
            assertTrue(url.contains("status=" + status), status + " 파라미터 포함");
            assertTrue(url.startsWith("https://do.sejong.ac.kr"), "기본 URL로 시작");
        }
    }
}
