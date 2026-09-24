package com.vyoog.eisplatform.modules.dashboard.dto;

public record SeatUsageDto(
    int licensedSeats,
    long activeMemberCount,
    double utilizationPercent
) {
}
