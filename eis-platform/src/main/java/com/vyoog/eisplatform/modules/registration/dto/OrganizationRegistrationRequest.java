package com.vyoog.eisplatform.modules.registration.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * No {@code parentOrganizationId} field anywhere in this request, by design —
 * public organization registration can never set one (see Organization's own
 * javadoc); that linkage is Vyoog-Admin-managed only, through a separate
 * admin-only endpoint.
 *
 * <p>{@code code}, {@code country}, {@code productIds}, {@code licensedSeats},
 * and {@code firstAdmin.firstName}/{@code lastName} are all now OPTIONAL —
 * the real registration form only collects organization name, GSTIN, email,
 * phone, and password; RegistrationService fills in sensible defaults for
 * everything else (see its own javadoc on {@code registerOrganization}) so
 * this one DTO/endpoint still works exactly as before for anything that
 * chooses to send the fuller set (e.g. a future admin-driven detailed
 * registration), without forcing every caller to supply them.
 */
public record OrganizationRegistrationRequest(
    @NotBlank String name,
    String code,
    String type,
    String industry,
    String website,

    @NotBlank @Email String businessEmail,
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

    List<Long> productIds,

    Integer licensedSeats,

    @NotNull @Valid FirstAdmin firstAdmin
) {
    public record FirstAdmin(
        String firstName,
        String lastName,
        @NotBlank @Email String email,
        String mobile,
        @NotBlank String password,
        @NotBlank String confirmPassword
    ) {
    }
}
