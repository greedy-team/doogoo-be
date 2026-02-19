package com.doogoo.doogoo.academic;

import java.time.LocalDate;

public record AcademicScheduleDto(
        int year,
        String department,
        LocalDate startDate,
        LocalDate endDate,
        String content
) {}
