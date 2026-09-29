package com.vyoog.eisplatform.modules.registration.dto;

import com.vyoog.eisplatform.modules.registration.model.OrganizationLifecycleStatus;
import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;

import java.time.Instant;

/** Platform-admin view of a registered organization — full company details,
 * not the leaner OrganizationDto an org's own member sees of itself. */
public record OrganizationAdminDto(
    Long id,
    String name,
    String code,
    String type,
    String industry,
    String website,
    String businessEmail,
    String phone,
    String country,
    String state,
    String city,
    String address,
    String gstin,
    String pan,
    String companyRegistrationNumber,
    String taxVatNumber,
    boolean billingSameAsAddress,
    String billingAddress,
    String billingCountry,
    String billingState,
    String billingCity,
    Long parentOrganizationId,
    int licensedSeats,
    long activeMemberCount,
    RegistrationStatus status,
    OrganizationLifecycleStatus lifecycleStatus,
    String adminFirstName,
    String adminLastName,
    String adminEmail,
    boolean adminKeycloakLinked,
    Instant createdAt,
    Long regionId,
    String regionName,
    boolean allowSeatOverage
) {
}
