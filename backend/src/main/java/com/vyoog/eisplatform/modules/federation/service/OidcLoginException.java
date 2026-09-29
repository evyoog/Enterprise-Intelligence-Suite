package com.vyoog.eisplatform.modules.federation.service;

/** An OIDC sign-in that cannot complete; its message is shown to the user via
 * {@code /?ssoError=} (same convention as SamlLoginException). */
public class OidcLoginException extends RuntimeException {
    public OidcLoginException(String message) {
        super(message);
    }
}
