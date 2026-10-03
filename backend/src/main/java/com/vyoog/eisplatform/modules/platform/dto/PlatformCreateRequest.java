package com.vyoog.eisplatform.modules.platform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PlatformCreateRequest(
    @NotBlank @Size(max = 255) String name,
    @Size(max = 2000) String description,
    String imageUrl,
    // C66 showcase settings — all optional so older clients keep working:
    // null colour = default EIS colour, null status = ACTIVE, null
    // showInCatalog = true, null displayOrder = 0.
    @Pattern(regexp = "#[0-9A-Fa-f]{6}", message = "must be a colour like #6366F1") String primaryColor,
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "must be ACTIVE or INACTIVE") String status,
    Boolean showInCatalog,
    @Min(0) @Max(9999) Integer displayOrder
) {

    /** The pre-C66 shape (name, description, logo) — showcase settings default. */
    public PlatformCreateRequest(String name, String description, String imageUrl) {
        this(name, description, imageUrl, null, null, null, null);
    }
}
