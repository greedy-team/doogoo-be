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
            String gradeId,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {}
}
