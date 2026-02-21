package com.doogoo.doogoo.dodream.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "두드림 공지 목록 응답")
public record DoDreamNoticesResponse(
        @Schema(description = "공지 목록") List<NoticeItem> notices) {

    @Schema(description = "두드림 공지 한 건")
    public record NoticeItem(
            String noticeId,
            String title,
            String departmentName,
            LocalDateTime applicationStartAt,
            LocalDateTime applicationEndAt,
            LocalDateTime operatingStartAt,
            LocalDateTime operatingEndAt,
            List<String> keywordIds,
            String detailUrl
    ) {}
}
