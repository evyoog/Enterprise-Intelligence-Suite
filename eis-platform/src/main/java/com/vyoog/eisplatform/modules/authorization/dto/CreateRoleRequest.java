package com.vyoog.eisplatform.modules.authorization.dto;

import com.vyoog.eisplatform.modules.authorization.model.RoleScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateRoleRequest(@NotBlank String name, @NotNull RoleScope scope, String description,
                                 List<Long> permissionIds) {
}
