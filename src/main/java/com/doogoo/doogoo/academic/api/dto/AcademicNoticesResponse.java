package com.doogoo.doogoo.academic.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "학사 공지 목록 응답")
public record AcademicNoticesResponse(
        @Schema(description = "공지 목록") List<NoticeItem> notices) {

    @Schema(description = "학사 공지 한 건")
    public record NoticeItem(
            String noticeId,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            @Schema(
            description = "대상 학년 ID. null=전체 학년, 1~4=해당 학년",
            example = "2"
    ) String gradeId
    ) {}
}
