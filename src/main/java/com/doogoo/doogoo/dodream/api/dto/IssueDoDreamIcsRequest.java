package com.doogoo.doogoo.dodream.api.dto;

import java.util.List;

public record IssueDoDreamIcsRequest(
        String selectedDepartmentId,
        String selectedMinorDepartmentId,
        List<String> selectedKeywordId,
        boolean alarmEnabled,
        Integer alarmMinutesBefore
) {}
