package com.vyoog.eisplatform.common.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Knowledge Center errors (REQ-KNW-003.11) with a stable {@code code} and a
 * friendly message; technical details are logged on the server only.
 * Codes: INVALID_FILE_TYPE, FILE_TOO_LARGE, UPLOAD_NOT_FOUND, UPLOAD_MISMATCH,
 * STORAGE_UNAVAILABLE, STORAGE_NOT_CONFIGURED, DUPLICATE_UPLOAD,
 * UPLOAD_CANCELLED, PERMISSION_DENIED, INVALID_STATE, IN_USE,
 * YOUTUBE_NOT_FOUND, YOUTUBE_UNAVAILABLE, INVALID_CONTENT,
 * ASSISTANT_NOT_CONFIGURED.
 */
public class KnowledgeException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final List<?> usedBy;

    public KnowledgeException(HttpStatus status, String code, String message) {
        this(status, code, message, null);
    }

    public KnowledgeException(HttpStatus status, String code, String message, List<?> usedBy) {
        super(message);
        this.status = status;
        this.code = code;
        this.usedBy = usedBy;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public List<?> getUsedBy() {
        return usedBy;
    }

    public static KnowledgeException badRequest(String code, String message) {
        return new KnowledgeException(HttpStatus.BAD_REQUEST, code, message);
    }

    public static KnowledgeException conflict(String message) {
        return new KnowledgeException(HttpStatus.CONFLICT, "INVALID_STATE", message);
    }

    public static KnowledgeException forbidden(String message) {
        return new KnowledgeException(HttpStatus.FORBIDDEN, "PERMISSION_DENIED", message);
    }
}
