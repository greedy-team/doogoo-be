package com.doogoo.doogoo.academic.api.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "학사 ICS 발급 요청")
public record IssueAcademicIcsRequest(
        @ArraySchema(
                arraySchema = @Schema(description = "선택 학년 ID 목록"),
                schema = @Schema(
                        description = "all=전체 학년, 1~4=해당 학년",
                        allowableValues = {"all", "1", "2", "3", "4"},
                        example = "all",
                        nullable = false
                )
        )
        @NotEmpty
        List<String> selectedGradeIds
) {}
