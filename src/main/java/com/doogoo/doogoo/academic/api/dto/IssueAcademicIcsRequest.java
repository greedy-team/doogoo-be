package com.doogoo.doogoo.academic.api.dto;

public record IssueAcademicIcsRequest(
        String selectedDepartmentId,
        Integer selectedGradeId,
        boolean alarmEnabled,
        Integer alarmMinutesBefore
) {}
