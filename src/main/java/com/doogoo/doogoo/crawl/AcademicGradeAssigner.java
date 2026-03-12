package com.doogoo.doogoo.crawl;

import com.doogoo.doogoo.academic.domain.AcademicScheduleDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class AcademicGradeAssigner {

    private static final String[] GRADE_FULL  = {"4", "3", "2", "1"};
    private static final String[] GRADE_SHORT = {"4", "3", "2"};

    private final KoreanBusinessDayCalculator businessDayCalculator;

    public AcademicGradeAssigner(KoreanBusinessDayCalculator businessDayCalculator) {
        this.businessDayCalculator = businessDayCalculator;
    }

    public List<AcademicScheduleDto> assign(int year, LocalDate startDate, LocalDate endDate, String content) {
        if (content.contains("학기 수강신청")) {
            if (content.contains("계절")) {
                return List.of(new AcademicScheduleDto(year, startDate, endDate, content, null));
            }
            return splitCourseRegistration(year, startDate, endDate, content);
        }
        if (content.contains("졸업식") || content.contains("학위수여식")) {
            return List.of(new AcademicScheduleDto(year, startDate, endDate, content, "4"));
        }
        if (content.contains("입학식") || content.contains("전공배정")) {
            return List.of(new AcademicScheduleDto(year, startDate, endDate, content, "1"));
        }
        return List.of(new AcademicScheduleDto(year, startDate, endDate, content, null));
    }

    private List<AcademicScheduleDto> splitCourseRegistration(int year, LocalDate startDate, LocalDate endDate, String content) {
        long businessDays = businessDayCalculator.countBusinessDays(startDate, endDate);
        String[] gradeOrder = businessDays >= 5 ? GRADE_FULL : GRADE_SHORT;

        List<AcademicScheduleDto> result = new ArrayList<>();
        LocalDate current = startDate;
        int gradeIndex = 0;

        while (!current.isAfter(endDate)) {
            if (businessDayCalculator.isBusinessDay(current)) {
                String gradeId = gradeIndex < gradeOrder.length ? gradeOrder[gradeIndex] : null;
                result.add(new AcademicScheduleDto(year, current, current, content, gradeId));
                gradeIndex++;
            }
            current = current.plusDays(1);
        }

        return result;
    }
}
