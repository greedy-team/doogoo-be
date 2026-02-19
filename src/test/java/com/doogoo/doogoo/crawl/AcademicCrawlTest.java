package com.doogoo.doogoo.crawl;

import com.doogoo.doogoo.academic.AcademicScheduleDto;
import com.doogoo.doogoo.config.SejongPortalConfig;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("세종대 학사일정 크롤링 통합 테스트")
@Disabled("실제 포털 로그인 필요 - 필요시에만 수동 실행")
class AcademicCrawlTest {

    private static final int THIS_YEAR = LocalDate.now().getYear();

    private AcademicCrawler crawler;
    private AcademicParser parser;

    @BeforeEach
    void setUp() {
        SejongPortalConfig config = new SejongPortalConfig();
        config.setLoginUrl("https://portal.sejong.ac.kr/jsp/login/loginSSL.jsp");
        config.setCalendarUrl("https://portal.sejong.ac.kr/user/comm/calendar/ptfol/index.do");
        config.setId(System.getenv("SEJONG_PORTAL_ID"));
        config.setPassword(System.getenv("SEJONG_PORTAL_PASSWORD"));

        crawler = new AcademicCrawler(config);
        parser = new AcademicParser();
    }

    @Test
    @DisplayName("로그인 후 학사일정 페이지 HTML을 정상적으로 가져온다")
    void fetchCalendar_returnDocument() {
        Document doc = crawler.fetchCalendar(THIS_YEAR);

        assertNotNull(doc, "문서가 null이 아님");
        assertFalse(doc.select("table tbody tr").isEmpty(), "학사일정 행이 존재");
    }

    @Test
    @DisplayName("가져온 HTML에서 학사일정 목록을 파싱한다")
    void parseCalendar_returnSchedules() {
        Document doc = crawler.fetchCalendar(THIS_YEAR);
        List<AcademicScheduleDto> schedules = parser.parse(doc, THIS_YEAR);

        assertFalse(schedules.isEmpty(), "파싱된 학사일정이 1건 이상");
        schedules.forEach(s ->
                System.out.printf("[%s] %s ~ %s | %s%n",
                        s.department(), s.startDate(), s.endDate(), s.content())
        );
    }

    @Test
    @DisplayName("파싱된 학사일정 각 항목이 유효한 값을 가진다")
    void parseCalendar_eachScheduleIsValid() {
        Document doc = crawler.fetchCalendar(THIS_YEAR);
        List<AcademicScheduleDto> schedules = parser.parse(doc, THIS_YEAR);

        for (AcademicScheduleDto s : schedules) {
            assertNotNull(s.content(), "일정 내용이 null이 아님");
            assertFalse(s.content().isBlank(), "일정 내용이 비어있지 않음");
            assertNotNull(s.startDate(), "시작일이 null이 아님");
            assertNotNull(s.endDate(), "종료일이 null이 아님");
            assertTrue(s.year() == THIS_YEAR, "연도가 THIS_YEAR");
        }
    }
}
