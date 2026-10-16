package com.vyoog.eisplatform.modules.profile.service;

import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** HTTP 400 with one English message per invalid field; keys are {@code <section>.<field>}. */
public class ProfileValidationException extends ProfileException {

    private final Map<String, String> fieldErrors;

    public ProfileValidationException(Map<String, String> fieldErrors) {
        super(HttpStatus.BAD_REQUEST, "Validation failed");
        this.fieldErrors = Collections.unmodifiableMap(new LinkedHashMap<>(fieldErrors));
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
