package com.doogoo.doogoo.academic.api.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "학사 공지 목록 응답")
public record AcademicNoticesResponse(
        @Schema(description = "공지 목록") List<NoticeItem> notices) {

    @Schema(description = "학사 공지 개별 항목")
    public record NoticeItem(
            String noticeId,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            @ArraySchema(
                    arraySchema = @Schema(description = "대상 학년 ID 목록"),
                    schema = @Schema(
                            description = "all=전체 학년, 1~4=해당 학년",
                            allowableValues = {"all", "1", "2", "3", "4"},
                            example = "all",
                            nullable = false
                    )
            ) List<String> gradeIds
    ) {}
}
