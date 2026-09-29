package com.vyoog.eisplatform.modules.partner.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** 14.01.01.01 Register provider — public self-service application, no
 * Vyoog account required (see Provider's own javadoc). */
public record ApplyAsProviderRequest(
    @NotBlank String name,
    @NotBlank String contactName,
    @NotBlank @Email String contactEmail,
    String description
) {
}
