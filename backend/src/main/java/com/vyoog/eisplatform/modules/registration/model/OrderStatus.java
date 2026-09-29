package com.vyoog.eisplatform.modules.registration.model;

/**
 * 09.01.01 Order Lifecycle (sprint 2027.1.1). No separate PROVISIONED state:
 * this platform has no async provisioning queue or workflow engine (see
 * OrderService's own javadoc), so approving an order provisions it
 * synchronously in the same call — APPROVED already means "provisioned".
 * Every non-SUBMITTED status is terminal; there is no path back to SUBMITTED.
 */
public enum OrderStatus {
    SUBMITTED,
    APPROVED,
    REJECTED,
    CANCELLED
}
