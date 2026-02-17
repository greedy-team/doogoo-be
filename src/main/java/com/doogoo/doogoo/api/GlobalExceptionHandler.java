package com.doogoo.doogoo.api;

import com.doogoo.doogoo.subscription.domain.InvalidTokenException;
import com.doogoo.doogoo.subscription.domain.SubscriptionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorBody> handleInvalidToken(InvalidTokenException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorBody("INVALID_TOKEN"));
    }

    @ExceptionHandler(SubscriptionNotFoundException.class)
    public ResponseEntity<ErrorBody> handleSubscriptionNotFound(SubscriptionNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody("SUBSCRIPTION_NOT_FOUND"));
    }

    public record ErrorBody(String code) {}
}
