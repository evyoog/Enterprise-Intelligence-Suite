package com.vyoog.eisplatform.modules.registration.dto;

/**
 * Deliberately identical in shape whether the submitted email/registration
 * was actually new or already existed — see RegistrationService's own
 * comments on why (never let this response become an account-enumeration
 * oracle). {@code registrationId} is safe to poll via
 * GET /register/status/{registrationId} either way.
 */
public record RegistrationAcceptedResponse(String registrationId, String message) {
}
