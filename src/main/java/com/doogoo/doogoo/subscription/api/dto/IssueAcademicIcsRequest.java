package com.doogoo.doogoo.subscription.api.dto;

public record IssueAcademicIcsRequest(
        String selectedDepartmentId,
        Integer selectedGradeId,
        boolean alarmEnabled,
        Integer alarmMinutesBefore
) {}
