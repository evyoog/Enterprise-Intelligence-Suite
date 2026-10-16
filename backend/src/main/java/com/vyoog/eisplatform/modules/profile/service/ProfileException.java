package com.vyoog.eisplatform.modules.profile.service;

import org.springframework.http.HttpStatus;

/** A profile request that failed with a specific HTTP status (409 conflict, 413 photo too large, 502 Keycloak, ...). */
public class ProfileException extends RuntimeException {

    private final HttpStatus status;

    public ProfileException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
