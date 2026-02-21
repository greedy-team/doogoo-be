package com.doogoo.doogoo.dodream.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ICS 발급 응답 (학사/두드림 공통)")
public record IssueIcsResponse(
        @Schema(description = "구독 토큰 (GET /cal/{token}.ics 에 사용)") String token,
        @Schema(description = "ICS 구독 URL") String icsUrl,
        @Schema(description = "ICS 파일 다운로드 URL") String downloadUrl
) {}
