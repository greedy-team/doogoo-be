package com.doogoo.doogoo.crawl;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Set;

@Component
public class KoreanBusinessDayCalculator {

    private static final Set<MonthDay> FIXED_HOLIDAYS = Set.of(
            MonthDay.of(8, 15),
            MonthDay.of(8, 17)
    );

    public boolean isBusinessDay(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) return false;
        return !FIXED_HOLIDAYS.contains(MonthDay.from(date));
    }

    public long countBusinessDays(LocalDate start, LocalDate end) {
        long count = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            if (isBusinessDay(current)) count++;
            current = current.plusDays(1);
        }
        return count;
    }
}
