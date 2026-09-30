package com.vyoog.eisplatform.modules.dashboard.dto;

import com.vyoog.eisplatform.modules.registration.dto.SubscriptionDto;

import java.util.List;
import java.util.Map;

/**
 * {@code subscriptions} reflects real, configured subscription rows (status/
 * dates). {@code spentThisPeriodByCurrency}/{@code spentLastPeriodByCurrency}
 * (01.02.01 View spending, REQ-BIL-001.16, C46) are real totals from paid
 * invoices — "this period"/"last period" are the trailing and preceding
 * 30-day windows (no fiscal-calendar concept exists elsewhere in this
 * backend to align to instead). Both maps are empty, never fabricated, when
 * the organization has no paid invoices yet — {@code note} still explains
 * that case rather than the frontend silently showing nothing.
 */
public record BillingOverviewDto(
    List<SubscriptionDto> subscriptions,
    Map<String, Long> spentThisPeriodByCurrency,
    Map<String, Long> spentLastPeriodByCurrency,
    String note
) {
}
