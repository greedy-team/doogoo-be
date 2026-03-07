package com.doogoo.doogoo.academic.domain;

import java.time.LocalDate;

public record AcademicScheduleDto(
        int year,
        LocalDate startDate,
        LocalDate endDate,
        String content,
        String gradeId   // null = 전체, "1"~"4" = 해당 학년만
) {}
