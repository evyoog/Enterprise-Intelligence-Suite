package com.vyoog.eisplatform.modules.registration.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateGroupRequest(@NotBlank String name) {
}
