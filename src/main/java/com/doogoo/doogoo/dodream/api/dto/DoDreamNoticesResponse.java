package com.doogoo.doogoo.dodream.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DoDreamNoticesResponse(List<NoticeItem> notices) {

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
