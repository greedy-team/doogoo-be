package com.doogoo.doogoo.academic.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "학사 ICS 발급 요청")
public record IssueAcademicIcsRequest(
        @Schema(
                description = "선택 학년 ID. all=전체 학년, 1~4=해당 학년",
                allowableValues = {"all", "1", "2", "3", "4"},
                example = "all",
                nullable = false
        )
        @NotBlank
        String selectedGradeId
) {}
