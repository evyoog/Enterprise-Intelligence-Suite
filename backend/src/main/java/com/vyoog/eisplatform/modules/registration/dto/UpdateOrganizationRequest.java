package com.vyoog.eisplatform.modules.registration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * REQ-TEN-001 (05.01.01.02 Update organization): the company details a
 * platform admin may correct. Deliberately absent: {@code code} (the
 * organization's permanent identifier), licensed seats (their own endpoint),
 * the MFA policy (the organization admin's own setting), the parent link and
 * both statuses. Every field is replaced as sent; a blank optional field
 * clears it.
 */
public record UpdateOrganizationRequest(
    @NotBlank @Size(max = 255) String name,
    @Size(max = 100) String type,
    @Size(max = 150) String industry,
    @Size(max = 500) String website,
    @NotBlank @Email @Size(max = 255) String businessEmail,
    @Size(max = 30) String phone,
    @NotBlank @Size(max = 100) String country,
    @Size(max = 100) String state,
    @Size(max = 100) String city,
    @Size(max = 500) String address,
    @Size(max = 50) String gstin,
    @Size(max = 50) String pan,
    @Size(max = 100) String companyRegistrationNumber,
    @Size(max = 100) String taxVatNumber,
    boolean billingSameAsAddress,
    @Size(max = 500) String billingAddress,
    @Size(max = 100) String billingCountry,
    @Size(max = 100) String billingState,
    @Size(max = 100) String billingCity,
    // 05.02.01.03 Assign region, 05.02.01.05 Configure tenant policies
    // (sprint 2026.4.2, carried from 2026.4.1). Null regionId clears the
    // assignment (top-level/unassigned).
    Long regionId,
    boolean allowSeatOverage
) {
}
