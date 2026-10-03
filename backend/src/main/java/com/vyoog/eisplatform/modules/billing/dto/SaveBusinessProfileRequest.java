package com.vyoog.eisplatform.modules.billing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** C60. Indian registration numbers are checked for their published format
 * only (GSTIN 15, PAN 10, CIN 21 characters); no checksum or lookup. */
public record SaveBusinessProfileRequest(
    @NotBlank @Size(max = 200) String legalName,
    @Size(max = 200) String tradeName,
    @Pattern(regexp = "(?i)^$|^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][0-9A-Z]Z[0-9A-Z]$", message = "GSTIN must be 15 characters, for example 29ABCDE1234F1Z5") String gstin,
    @Pattern(regexp = "(?i)^$|^[A-Z]{5}[0-9]{4}[A-Z]$", message = "PAN must be 10 characters, for example ABCDE1234F") String pan,
    @Pattern(regexp = "(?i)^$|^[LU][0-9]{5}[A-Z]{2}[0-9]{4}[A-Z]{3}[0-9]{6}$", message = "CIN must be 21 characters, for example U72900KA2020PTC123456") String cin,
    @NotBlank @Size(max = 200) String addressLine1,
    @Size(max = 200) String addressLine2,
    @NotBlank @Size(max = 100) String city,
    @NotBlank @Size(max = 100) String state,
    @NotBlank @Size(max = 20) String postalCode,
    @NotBlank @Size(max = 100) String country,
    @NotBlank @Email @Size(max = 255) String email,
    @Pattern(regexp = "^$|^[+0-9 ()-]{6,30}$", message = "must be a phone number") String phone,
    @Pattern(regexp = "^$|^https?://\\S{3,250}$", message = "must start with http:// or https://") String website,
    @NotBlank @Pattern(regexp = "(?i)^[A-Z0-9]{2,10}$", message = "must be 2 to 10 letters or digits") String invoicePrefix,
    @Min(0) @Max(365) int paymentTermsDays,
    @Size(max = 500) String invoiceFooterNote
) {
}
