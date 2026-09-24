package com.vyoog.eisplatform.modules.registration.dto;

import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;

public record RegistrationStatusResponse(String registrationId, RegistrationStatus status) {
}
