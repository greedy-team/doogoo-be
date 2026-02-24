package com.doogoo.doogoo.dodream.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "두드림 ICS 발급 요청")
public record IssueDoDreamIcsRequest(
        @Schema(description = "선택 학과 ID") String selectedDepartmentId,
        @Schema(description = "선택 소속 학과 ID") String selectedMinorDepartmentId,
        @Schema(description = "선택 키워드 ID 목록", example = "[\"competition\"]") List<String> selectedKeywordId
) {}
