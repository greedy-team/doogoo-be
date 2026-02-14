package com.doogoo.doogoo.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    CRAWL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "크롤링 실패"),
    PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "데이터 파싱 실패"),
    INVALID_HTML_STRUCTURE(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "HTML 구조가 유효하지 않습니다"),

    AI_CLASSIFICATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "A001", "AI 분류 실패"),

    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "이벤트를 찾을 수 없습니다"),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "서버 내부 오류가 발생했습니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}
