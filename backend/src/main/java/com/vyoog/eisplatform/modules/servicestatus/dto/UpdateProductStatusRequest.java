package com.vyoog.eisplatform.modules.servicestatus.dto;

import com.vyoog.eisplatform.modules.servicestatus.model.ServiceStatusValue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProductStatusRequest(@NotNull ServiceStatusValue status, @Size(max = 500) String note) {
}
