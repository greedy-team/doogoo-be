package com.doogoo.doogoo.common.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DoogooException.class)
    public ResponseEntity<ErrorResponse> handleDoogooException(DoogooException e) {
        ErrorResponse body = ErrorResponse.from(e.getErrorCode());
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        ErrorResponse body = ErrorResponse.from(ErrorCode.INVALID_TOKEN_FORMAT);
        return ResponseEntity.status(ErrorCode.INVALID_TOKEN_FORMAT.getStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception", e);
        ErrorResponse body = ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus()).body(body);
    }
}
