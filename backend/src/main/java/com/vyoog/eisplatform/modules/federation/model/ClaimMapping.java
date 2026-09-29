package com.vyoog.eisplatform.modules.federation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * REQ-IAM-007 (C23/C28): per-provider attribute / claim names for the four
 * details the sign-in reads. Each is optional; a configured name is tried
 * first, then the protocol's default names. Fallbacks after that stay fixed.
 * Role mapping is out of scope (federated users join as MEMBER).
 */
@Embeddable
@Getter
@Setter
public class ClaimMapping {

    @Column(name = "email_claim", length = 255)
    private String email;

    @Column(name = "first_name_claim", length = 255)
    private String firstName;

    @Column(name = "last_name_claim", length = 255)
    private String lastName;

    @Column(name = "display_name_claim", length = 255)
    private String displayName;

    /** Hibernate loads an embedded value with all-null columns as null. */
    public static ClaimMapping orEmpty(ClaimMapping mapping) {
        return mapping == null ? new ClaimMapping() : mapping;
    }

    public com.vyoog.eisplatform.modules.federation.dto.ClaimMappingDto toDto() {
        return new com.vyoog.eisplatform.modules.federation.dto.ClaimMappingDto(email, firstName, lastName, displayName);
    }

    /** Replaces all four names; blank becomes null (= default). */
    public void apply(com.vyoog.eisplatform.modules.federation.dto.ClaimMappingDto dto) {
        email = blankToNull(dto.email());
        firstName = blankToNull(dto.firstName());
        lastName = blankToNull(dto.lastName());
        displayName = blankToNull(dto.displayName());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /** The configured name (if any) followed by the defaults. */
    public static List<String> tryFirst(String configured, List<String> defaults) {
        List<String> names = new ArrayList<>();
        if (configured != null && !configured.isBlank()) {
            names.add(configured.trim());
        }
        names.addAll(defaults);
        return names;
    }
}
