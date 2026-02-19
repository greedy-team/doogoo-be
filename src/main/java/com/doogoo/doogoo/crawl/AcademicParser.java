package com.doogoo.doogoo.crawl;

import com.doogoo.doogoo.academic.AcademicScheduleDto;
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

    /**
     * 학사일정 테이블 구조 (5컬럼):
     * [년도, 월, 구분(단과대), 기간(1.2 ~ 1.3), 일정내용]
     *
     * 년도/월 컬럼은 rowspan으로 묶이므로 td 수가 적을 수 있음.
     * td가 5개 → 모든 컬럼 존재
     * td가 4개 → 년도 없음 (월 rowspan)
     * td가 3개 → 년도/월 없음 (둘 다 rowspan)
     */
    public List<AcademicScheduleDto> parse(Document doc, int year) {
        List<AcademicScheduleDto> result = new ArrayList<>();
        Elements rows = doc.select("table tbody tr");

        if (rows.isEmpty()) {
            log.warn("학사일정 테이블 행을 찾지 못했습니다 (year={})", year);
            return result;
        }

        for (Element row : rows) {
            Elements cells = row.select("td");
            int size = cells.size();

            if (size < 3) continue;

            try {
                // 컬럼 수에 따라 offset 결정
                int offset = switch (size) {
                    case 5 -> 2; // 년도 + 월 + 구분 + 기간 + 일정
                    case 4 -> 1; // 월 + 구분 + 기간 + 일정
                    default -> 0; // 구분 + 기간 + 일정
                };

                String department = cells.get(offset).text().trim();
                String period     = cells.get(offset + 1).text().trim();
                String content    = cells.get(offset + 2).text().trim();

                if (content.isBlank()) continue;

                LocalDate[] dates = parsePeriod(period, year);
                if (dates == null) continue;

                result.add(new AcademicScheduleDto(year, department, dates[0], dates[1], content));

            } catch (Exception e) {
                log.warn("행 파싱 실패 (year={}): {}", year, e.getMessage());
            }
        }

        log.info("{}년도 학사일정 {}건 파싱 완료", year, result.size());
        return result;
    }

    /**
     * "1.2 ~ 1.3" 또는 "1.2" 형식의 기간 문자열을 파싱합니다.
     */
    private LocalDate[] parsePeriod(String period, int year) {
        if (period == null || period.isBlank()) return null;
        try {
            String[] parts = period.split("~");
            LocalDate start = parseDate(parts[0].trim(), year);
            LocalDate end   = (parts.length > 1) ? parseDate(parts[1].trim(), year) : start;
            return new LocalDate[]{start, end};
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            log.warn("기간 파싱 실패: '{}' - {}", period, e.getMessage());
            return null;
        }
    }

    /** "1.2" 형식의 날짜를 LocalDate로 변환합니다. */
    private LocalDate parseDate(String dateStr, int year) {
        String[] parts = dateStr.split("\\.");
        int month = Integer.parseInt(parts[0].trim());
        int day   = Integer.parseInt(parts[1].trim());
        return LocalDate.of(year, month, day);
    }
}
