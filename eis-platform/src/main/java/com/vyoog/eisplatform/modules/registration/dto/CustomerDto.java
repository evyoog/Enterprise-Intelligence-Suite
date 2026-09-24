package com.vyoog.eisplatform.modules.registration.dto;

import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;

public record CustomerDto(
    Long id,
    String email,
    String firstName,
    String lastName,
    String mobile,
    String country,
    String companyName,
    String jobTitle,
    String industry,
    RegistrationStatus status
) {
}
