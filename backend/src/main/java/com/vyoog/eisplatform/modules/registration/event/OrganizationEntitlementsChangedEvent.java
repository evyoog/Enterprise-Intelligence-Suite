package com.vyoog.eisplatform.modules.registration.event;

/**
 * Published (inside the changing transaction) when the products an organization is entitled to change — a subscription is
 * created, suspended, reactivated, cancelled or expires. REQ-TEN-005 recalculates member product access on it.
 */
public record OrganizationEntitlementsChangedEvent(Long organizationId) {
}
