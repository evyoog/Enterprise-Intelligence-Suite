package com.vyoog.eisplatform.modules.administration.dto;

public record PlatformFeatureFlagDto(String flagKey, boolean enabled, String description) {
}
