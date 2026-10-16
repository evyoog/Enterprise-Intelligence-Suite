package com.vyoog.eisplatform.modules.platformadmin.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** The platform admin registry file as parsed from JSON (C108). Unknown properties are ignored. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PlatformAdminFile(Integer version, String description, List<PlatformAdminEntry> platformAdmins) {
}
