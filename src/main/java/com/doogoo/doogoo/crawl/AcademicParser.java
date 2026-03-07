package com.doogoo.doogoo.crawl;

import com.doogoo.doogoo.academic.domain.AcademicScheduleDto;
import com.doogoo.doogoo.common.error.ErrorCode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class AcademicParser {

    private static final Logger log = LoggerFactory.getLogger(AcademicParser.class);

    private final AcademicPeriodParser periodParser;
    private final AcademicGradeAssigner gradeAssigner;

    public AcademicParser(AcademicPeriodParser periodParser, AcademicGradeAssigner gradeAssigner) {
        this.periodParser = periodParser;
        this.gradeAssigner = gradeAssigner;
    }

    public List<AcademicScheduleDto> parse(Document doc, int year) {
        List<AcademicScheduleDto> result = new ArrayList<>();
        Elements rows = doc.select("#calList tbody tr");

        if (rows.isEmpty()) {
            log.warn("[{}] {}: year={}", ErrorCode.INVALID_HTML_STRUCTURE.getCode(), ErrorCode.INVALID_HTML_STRUCTURE.getMessage(), year);
            return result;
        }

        for (Element row : rows) {
            Elements cells = row.select("td");
            if (cells.size() != 5) continue;

            try {
                String period  = cells.get(3).text().trim();
                String content = cells.get(4).text().trim();

                if (content.isBlank()) continue;

                LocalDate[] dates = periodParser.parse(period, year);
                if (dates == null) continue;

                result.addAll(gradeAssigner.assign(year, dates[0], dates[1], content));

            } catch (Exception e) {
                log.warn("[{}] {}: year={}, {}", ErrorCode.PARSE_FAILED.getCode(), ErrorCode.PARSE_FAILED.getMessage(), year, e.getMessage(), e);
            }
        }

        log.info("{}년도 학사일정 {}건 파싱 완료", year, result.size());
        return result;
    }
}
