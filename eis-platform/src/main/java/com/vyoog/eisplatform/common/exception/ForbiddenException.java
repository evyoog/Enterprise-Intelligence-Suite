package com.vyoog.eisplatform.common.exception;

/** For an authenticated caller who simply isn't allowed to do this specific
 * thing — e.g. a plain org MEMBER calling an ORG_ADMIN-only endpoint, or an
 * ORG_ADMIN reaching for another organization's data. Distinct from
 * InvalidCredentialsException (401 — "who even are you"). */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
