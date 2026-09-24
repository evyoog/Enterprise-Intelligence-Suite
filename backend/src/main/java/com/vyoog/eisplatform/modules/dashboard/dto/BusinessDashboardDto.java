package com.vyoog.eisplatform.modules.dashboard.dto;

import com.vyoog.eisplatform.modules.registration.dto.OrganizationDto;

import java.util.List;

public record BusinessDashboardDto(
    OrganizationDto organization,
    List<BusinessApplicationDto> applications,
    SeatUsageDto seatUsage,
    BillingOverviewDto billing,
    ServiceHealthDto serviceHealth,
    SupportOverviewDto support,
    List<DashboardAlertDto> alerts
) {
}
