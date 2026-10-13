package com.vyoog.eisplatform.modules.billing.service;

/** Published when a customer or an organization saves its billing details. A Draft invoice that was waiting for them (tax could
 * not be calculated without a country or state) is tried again (REQ-BIL-002.42). Exactly one id is set. */
public record BillingDetailsSavedEvent(Long customerId, Long organizationId) {
}
