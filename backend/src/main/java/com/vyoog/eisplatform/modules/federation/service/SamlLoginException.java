package com.vyoog.eisplatform.modules.federation.service;

/**
 * Any rejection along the SAML login/ACS path (bad signature, expired/replayed
 * request, unrecognized identity, disabled provider, etc.) — deliberately one
 * exception type covering all of them, since every call site (SamlLoginController)
 * handles them identically: log the real reason server-side, redirect the
 * browser back to the frontend with a generic error indicator rather than a
 * raw JSON error page (this endpoint is reached by a real browser
 * navigation/form-POST from the IdP, not a fetch/XHR the SPA could otherwise
 * read a JSON body from).
 */
public class SamlLoginException extends RuntimeException {

    public SamlLoginException(String message) {
        super(message);
    }
}
