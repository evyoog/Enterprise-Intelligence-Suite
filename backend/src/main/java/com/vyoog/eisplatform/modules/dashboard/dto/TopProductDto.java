package com.vyoog.eisplatform.modules.dashboard.dto;

/** Platform admin dashboard (C53): one row of the most-launched-products bar
 * chart — real {@code ProductUsage} totals summed across every customer,
 * same honest usage metric {@code ProductUsage}'s own javadoc describes. */
public record TopProductDto(
    Long productId,
    String productName,
    long totalLaunches
) {
}
