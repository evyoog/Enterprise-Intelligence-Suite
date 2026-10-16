package com.vyoog.eisplatform.modules.profile.controller;

import com.vyoog.eisplatform.modules.profile.service.ProfileException;
import com.vyoog.eisplatform.modules.profile.service.ProfileValidationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Error bodies for the two profile controllers ONLY (assignableTypes), so the platform's existing error
 * behaviour elsewhere does not change. Same shape as GlobalExceptionHandler, plus {@code fieldErrors} on a
 * validation failure. Every other exception raised in these controllers still falls through to
 * GlobalExceptionHandler.
 */
@RestControllerAdvice(assignableTypes = {ProfileController.class, AdminProfileController.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ProfileExceptionHandler {

    @ExceptionHandler(ProfileException.class)
    public ResponseEntity<Map<String, Object>> handleProfile(ProfileException ex) {
        Map<String, Object> body = body(ex.getStatus(), ex.getMessage());
        if (ex instanceof ProfileValidationException validation) {
            body.put("fieldErrors", validation.getFieldErrors());
        }
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    /** A multipart request over the servlet limit (25 MB) is still "photo too large" to the caller. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleTooLarge(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(body(HttpStatus.PAYLOAD_TOO_LARGE, "The photo must be 1 MB or smaller"));
    }

    private static Map<String, Object> body(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
}
