package com.doogoo.doogoo.academic.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AcademicNoticesResponse(List<NoticeItem> notices) {

    public record NoticeItem(
            String noticeId,
            String title,
            String gradeId,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {}
}
