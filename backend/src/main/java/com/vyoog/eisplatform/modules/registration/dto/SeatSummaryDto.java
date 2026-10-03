package com.vyoog.eisplatform.modules.registration.dto;

/** REQ-SUB-003: an organization subscription's seats. {@code inUse} = the
 * organization's ACTIVE members (pool default); {@code minimum} = the lowest
 * quantity allowed now. */
public record SeatSummaryDto(
    Long subscriptionId,
    String productName,
    int quantity,
    long inUse,
    long minimum,
    int organizationSeatLimit,
    int maximum
) {
}
