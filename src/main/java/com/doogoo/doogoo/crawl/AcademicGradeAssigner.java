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

    public List<AcademicScheduleDto> assign(int year, String department, LocalDate startDate, LocalDate endDate, String content) {
        if (content.contains("수강신청")) {
            // 계절학기 수강신청은 학년 구분 없이 전체
            if (content.contains("계절")) {
                return List.of(new AcademicScheduleDto(year, department, startDate, endDate, content, null));
            }
            return splitCourseRegistration(year, department, startDate, endDate, content);
        }
        if (content.contains("졸업식") || content.contains("학위수여식")) {
            return List.of(new AcademicScheduleDto(year, department, startDate, endDate, content, "4"));
        }
        if (content.contains("입학식") || content.contains("전공배정")) {
            return List.of(new AcademicScheduleDto(year, department, startDate, endDate, content, "1"));
        }
        return List.of(new AcademicScheduleDto(year, department, startDate, endDate, content, null));
    }

    /**
     * 수강신청 기간을 영업일(주말·공휴일 제외)별로 쪼개 학년을 배정.
     * 주말·공휴일은 결과에 포함하지 않음 (캘린더에 미표시).
     *
     * 예) 26.8.14~8.21 → 8.14=4학년, 8.18=3학년, 8.19=2학년, 8.20=1학년, 8.21=전체
     */
    private List<AcademicScheduleDto> splitCourseRegistration(int year, String department, LocalDate startDate, LocalDate endDate, String content) {
        long businessDays = businessDayCalculator.countBusinessDays(startDate, endDate);
        String[] gradeOrder = businessDays >= 5 ? GRADE_FULL : GRADE_SHORT;

        List<AcademicScheduleDto> result = new ArrayList<>();
        LocalDate current = startDate;
        int gradeIndex = 0;

        while (!current.isAfter(endDate)) {
            if (businessDayCalculator.isBusinessDay(current)) {
                String gradeId = gradeIndex < gradeOrder.length ? gradeOrder[gradeIndex] : null;
                result.add(new AcademicScheduleDto(year, department, current, current, content, gradeId));
                gradeIndex++;
            }
            current = current.plusDays(1);
        }

        return result;
    }
}
