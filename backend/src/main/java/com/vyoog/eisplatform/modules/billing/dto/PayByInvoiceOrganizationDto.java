package com.vyoog.eisplatform.modules.billing.dto;

/** C97 BI-4: an organization and whether the platform admin has enabled Pay by invoice for it. */
public record PayByInvoiceOrganizationDto(Long id, String name, String code, boolean enabled) {
}
