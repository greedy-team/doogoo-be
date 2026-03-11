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
            @Schema(description = "학과 ID. null 또는 \"all\"이면 자유전공학부") String departmentId,
            String departmentName,
            LocalDateTime applicationStartAt,
            LocalDateTime applicationEndAt,
            LocalDateTime operatingStartAt,
            LocalDateTime operatingEndAt,
            String location,
            String description,
            String mileage,
            @Schema(description = "키워드 ID 목록. null(빈 배열)이면 기타(k_7)로 해석") List<String> keywordIds,
            String detailUrl
    ) {}
}
