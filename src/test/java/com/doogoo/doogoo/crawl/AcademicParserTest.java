package com.doogoo.doogoo.crawl;

import com.doogoo.doogoo.academic.domain.AcademicScheduleDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AcademicParserTest {

    private final KoreanBusinessDayCalculator businessDayCalculator = new KoreanBusinessDayCalculator();
    private final AcademicPeriodParser periodParser = new AcademicPeriodParser();
    private final AcademicGradeAssigner gradeAssigner = new AcademicGradeAssigner(businessDayCalculator);
    private final AcademicParser academicParser = new AcademicParser(periodParser, gradeAssigner);

    @Test
    @DisplayName("학사일정 페이지 HTML(academic.html)에서 일정이 정상적으로 파싱되어야 한다")
    void parseAcademicSchedule_success() throws Exception {
        File htmlFile = new ClassPathResource("academic.html").getFile();
        Document doc = Jsoup.parse(htmlFile, StandardCharsets.UTF_8.name());
        int targetYear = 2026;

        List<AcademicScheduleDto> schedules = academicParser.parse(doc, targetYear);

        assertThat(schedules).isNotNull();
        System.out.println("✅ 파싱된 학사일정 개수: " + schedules.size() + "개");

        if (!schedules.isEmpty()) {
            for (int i = 0; i < Math.min(5, schedules.size()); i++) {
                AcademicScheduleDto schedule = schedules.get(i);
                System.out.println("--- [ 일정 " + (i + 1) + " ] ---");
                System.out.println("부서/구분: " + schedule.department());
                System.out.println("기간: " + schedule.startDate() + " ~ " + schedule.endDate());
                System.out.println("내용: " + schedule.content());
            }
        }

        assertThat(schedules).isNotEmpty();
    }
}
