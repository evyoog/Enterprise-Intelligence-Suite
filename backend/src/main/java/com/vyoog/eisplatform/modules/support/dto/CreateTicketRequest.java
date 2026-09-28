package com.vyoog.eisplatform.modules.support.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTicketRequest(@NotBlank String subject, @NotBlank String description) {
}
