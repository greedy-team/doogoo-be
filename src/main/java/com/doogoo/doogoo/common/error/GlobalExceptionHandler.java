package com.doogoo.doogoo.common.error;

import com.doogoo.doogoo.common.log.JsonLog;
import com.doogoo.doogoo.common.log.LogDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DoogooException.class)
    public ResponseEntity<ErrorResponse> handleDoogooException(DoogooException e, HttpServletRequest request) {
        logError(e.getErrorCode(), e.getMessage(), request, e);
        ErrorResponse body = ErrorResponse.from(e.getErrorCode());
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        logError(ErrorCode.INVALID_TOKEN_FORMAT, e.getMessage(), request, e);
        ErrorResponse body = ErrorResponse.from(ErrorCode.INVALID_TOKEN_FORMAT);
        return ResponseEntity.status(ErrorCode.INVALID_TOKEN_FORMAT.getStatus()).body(body);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResource(NoResourceFoundException e) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        logError(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage(), request, e);
        ErrorResponse body = ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus()).body(body);
    }

    private void logError(ErrorCode ec, String message, HttpServletRequest request, Throwable t) {
        int status = ec.getStatus().value();
        String refKey = extractRefKey(request);

        String event = resolveEvent(ec, request);

        LogDto.ErrorLog errorLog = new LogDto.ErrorLog(
                event,
                refKey,
                status,
                ec.getCode(),
                message
        );

        if (status >= 500) {
            JsonLog.error(GlobalExceptionHandler.class, errorLog);
        } else {
            JsonLog.warn(GlobalExceptionHandler.class, errorLog);
        }
    }

    private String extractRefKey(HttpServletRequest request) {
        String uri = request.getRequestURI();
        try {
            if (uri != null && uri.startsWith("/cal/")) {
                String token = uri.substring(uri.lastIndexOf('/') + 1);
                return token.replace(".ics", "");
            }
            return request.getMethod() + " " + uri;
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String resolveEvent(ErrorCode ec, HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (uri != null && uri.startsWith("/cal/")) {
            return ec.getStatus().is4xxClientError() ? "ics.request.warn" : "ics.request.error";
        }

        if (uri != null && uri.startsWith("/api/")) {
            return ec.getStatus().is4xxClientError() ? "api.request.warn" : "api.request.error";
        }

        return ec.getStatus().is4xxClientError() ? "http.request.warn" : "http.request.error";
    }
}

