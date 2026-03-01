package com.doogoo.doogoo.crawl;

import com.doogoo.doogoo.common.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AcademicPeriodParser {

    private static final Logger log = LoggerFactory.getLogger(AcademicPeriodParser.class);

    public LocalDate[] parse(String period, int year) {
        if (period == null || period.isBlank()) return null;
        try {
            String[] parts = period.split("~");
            LocalDate start = parseDate(parts[0].trim(), year);
            if (parts.length <= 1) {
                return new LocalDate[]{start, start};
            }
            LocalDate end = parseDate(parts[1].trim(), year);
            if (end.getMonthValue() < start.getMonthValue()) {
                end = end.withYear(year + 1);
            }
            return new LocalDate[]{start, end};
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            log.warn("[{}] {}: '{}' - {}", ErrorCode.PARSE_FAILED.getCode(), ErrorCode.PARSE_FAILED.getMessage(), period, e.getMessage(), e);
            return null;
        }
    }

    private LocalDate parseDate(String dateStr, int year) {
        String[] parts = dateStr.split("\\.");
        int month = Integer.parseInt(parts[0].trim());
        int day   = Integer.parseInt(parts[1].trim());
        return LocalDate.of(year, month, day);
    }
}
