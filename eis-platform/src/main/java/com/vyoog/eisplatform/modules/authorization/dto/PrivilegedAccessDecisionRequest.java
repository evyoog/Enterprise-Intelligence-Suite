package com.vyoog.eisplatform.modules.authorization.dto;

/** Body for approve/reject/revoke — {@code note} is optional context for the
 * audit trail (why approved, why rejected, why revoked early). */
public record PrivilegedAccessDecisionRequest(String note) {
}
