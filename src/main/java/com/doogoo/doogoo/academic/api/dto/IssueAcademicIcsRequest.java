package com.doogoo.doogoo.academic.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "학사 ICS 발급 요청")
public record IssueAcademicIcsRequest(
        @Schema(description = "선택 학년 ID (1~4). null 또는 생략 시 전체 학년으로 해석", example = "1") Integer selectedGradeId
) {}
