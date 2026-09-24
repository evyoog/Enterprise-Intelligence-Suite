package com.vyoog.eisplatform.common.exception;

/** For conflicts that are safe to reveal directly (e.g. an organization code
 * already in use) — never for anything that would let a caller enumerate
 * registered individual emails; see RegistrationService's own comments on
 * why email duplicates are handled differently (a generic response, not
 * this exception). */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
