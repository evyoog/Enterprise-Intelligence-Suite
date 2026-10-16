package com.vyoog.eisplatform.modules.platformadmin.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * One person listed in the platform admin registry file (C108). It only identifies WHO the person is; it
 * never holds a password or any other secret. A missing {@code enabled} means enabled.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PlatformAdminEntry(String email, String keycloakSub, String displayName, Boolean enabled, String notes) {

    /** Whether the entry counts: {@code enabled} true or missing. */
    public boolean active() {
        return enabled == null || enabled;
    }
}
