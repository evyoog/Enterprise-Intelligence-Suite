package com.vyoog.eisplatform.modules.dashboard.dto;

import com.vyoog.eisplatform.modules.registration.dto.SubscriptionDto;

import java.util.List;

/**
 * {@code subscriptions} reflects real, configured subscription rows (status/
 * dates) — there is deliberately no amount/invoice/spend field anywhere on
 * this record: no payment gateway or invoice model exists in this backend
 * (see this phase's own report), and fabricating a dollar figure would
 * violate the "use only real backend data" rule. {@code note} says so
 * explicitly rather than the frontend silently showing nothing.
 */
public record BillingOverviewDto(
    List<SubscriptionDto> subscriptions,
    String note
) {
}
