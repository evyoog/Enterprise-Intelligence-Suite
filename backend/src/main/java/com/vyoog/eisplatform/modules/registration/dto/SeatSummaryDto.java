package com.vyoog.eisplatform.modules.registration.dto;

/** REQ-SUB-003: an organization subscription's seats. {@code inUse} = the members who have the product ON (named seats, C90);
 * {@code minimum} = the lowest quantity allowed now. C100 SE-1 B: {@code pendingQuantity} is a decrease that takes effect at the next renewal;
 * {@code unitAmount} (minor units) is the price of one seat for one term; {@code remainingDays} of {@code termDays} is what an increase now is charged for.
 * {@code effect} and {@code invoiceId} are filled only in the answer to a change: NOW (charged on {@code invoiceId}, if any), AT_RENEWAL, WITHDRAWN or UNCHANGED. */
public record SeatSummaryDto(
    Long subscriptionId,
    String productName,
    int quantity,
    long inUse,
    long minimum,
    int organizationSeatLimit,
    int maximum,
    Integer pendingQuantity,
    long unitAmount,
    String currency,
    int remainingDays,
    int termDays,
    java.time.Instant renewsAt,
    String effect,
    Long invoiceId
) {
}
