package com.doogoo.doogoo.dodream.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "두드림 ICS 발급 요청")
public record IssueDoDreamIcsRequest(
        @Schema(
                description = "선택 학과 ID. null이면 학과 필터 미적용(자유전공학부·전체로 해석)",
                example = "dept-cse"
        ) String selectedDepartmentId,
        @Schema(
                description = "선택 소속(학부) 학과 ID. null이면 해당 필터 미적용",
                example = "dept-liberal-studies"
        ) String selectedMinorDepartmentId,
        @Schema(
                description = "선택 키워드 ID 목록. null 또는 빈 배열이면 키워드 필터 미적용. 예: k_1=학술/연구, k_2=취창업, k_6=캠퍼스, k_7=기타",
                example = "[\"k_1\"]"
        ) List<String> selectedKeywordId
) {}
