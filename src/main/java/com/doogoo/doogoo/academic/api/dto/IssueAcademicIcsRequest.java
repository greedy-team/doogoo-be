package com.doogoo.doogoo.academic.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "학사 ICS 발급 요청")
public record IssueAcademicIcsRequest(
        @Schema(description = "선택 학과 ID", example = "dept_1") String selectedDepartmentId,
        @Schema(description = "선택 학년 ID (1~4)", example = "1") Integer selectedGradeId,
        @Schema(description = "알람 사용 여부") boolean alarmEnabled,
        @Schema(description = "알람 분 전 (alarmEnabled true일 때 0~10080)") Integer alarmMinutesBefore
) {}
