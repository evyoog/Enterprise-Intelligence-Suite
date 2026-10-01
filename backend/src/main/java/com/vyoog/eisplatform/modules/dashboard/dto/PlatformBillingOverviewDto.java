package com.vyoog.eisplatform.modules.dashboard.dto;

import java.util.Map;

/** Platform admin dashboard (C53) — the platform-wide equivalent of
 * {@link BillingOverviewDto}'s spend figures: real totals from paid
 * invoices across every customer and organization, trailing/preceding
 * 30-day windows (same windows {@code BusinessDashboardService} already
 * uses — no fiscal-calendar concept exists to align to instead). Both maps
 * are empty, never fabricated, when nothing has been paid yet. */
public record PlatformBillingOverviewDto(
    Map<String, Long> revenueThisPeriodByCurrency,
    Map<String, Long> revenueLastPeriodByCurrency,
    String note
) {
}
