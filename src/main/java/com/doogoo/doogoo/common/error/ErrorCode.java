package com.doogoo.doogoo.common.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_TOKEN_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_TOKEN_FORMAT", "잘못된 토큰 형식입니다."),
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "TOKEN_NOT_FOUND", "토큰을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS", "요청이 많아 잠시 후 다시 시도해주세요."),

    CRAWL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "크롤링 실패"),
    PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "데이터 파싱 실패"),
    INVALID_HTML_STRUCTURE(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "HTML 구조가 유효하지 않습니다"),
    ACADEMIC_CRAWL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "학사일정 크롤링 실패"),
    ACADEMIC_LOGIN_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "C005", "세종대 포털 로그인 실패"),

    AI_CLASSIFICATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "A001", "AI 분류 실패"),

    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "이벤트를 찾을 수 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
