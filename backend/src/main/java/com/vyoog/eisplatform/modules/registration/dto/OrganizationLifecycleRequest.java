package com.vyoog.eisplatform.modules.registration.dto;

import jakarta.validation.constraints.Size;

/** REQ-TEN-001: optional reason for a suspend / activate / close, written to
 * the audit log only. The body itself may be omitted. */
public record OrganizationLifecycleRequest(@Size(max = 500) String reason) {
}
