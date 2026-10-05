package com.vyoog.eisplatform.modules.search.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** A synonym group: 2 to 10 terms, each 1 to 50 characters. */
public record SynonymRequest(
    @NotNull @Size(min = 2, max = 10) List<@NotNull @Size(min = 1, max = 50) String> terms
) {
}
