package com.vyoog.eisplatform.modules.auth.model;

/** C29 (2026-09-26): what a parked sign-in is waiting for. VERIFY: the member
 * has an authenticator and must enter a code. ENROLL: the organization
 * requires MFA and the member has none yet, so they set one up during
 * sign-in; the session is issued only after a valid code. */
public enum MfaChallengeKind {
    VERIFY,
    ENROLL
}
