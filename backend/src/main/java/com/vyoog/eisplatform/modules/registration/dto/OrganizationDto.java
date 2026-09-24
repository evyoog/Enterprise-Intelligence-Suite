package com.vyoog.eisplatform.modules.registration.dto;

import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;

public record OrganizationDto(
    Long id,
    String name,
    String code,
    String type,
    String industry,
    String website,
    String businessEmail,
    String country,
    int licensedSeats,
    long activeMemberCount,
    RegistrationStatus status,
    boolean mfaRequired
) {
}
