package com.doogoo.doogoo.common.error;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "에러 응답")
public record ErrorResponse(
        @Schema(description = "HTTP 상태 코드") int status,
        @Schema(description = "에러 코드") String code,
        @Schema(description = "에러 메시지") String message,
        @Schema(description = "발생 시각") LocalDateTime timestamp
) {
    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                LocalDateTime.now()
        );
    }
}
