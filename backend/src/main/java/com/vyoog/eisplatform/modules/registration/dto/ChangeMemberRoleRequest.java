package com.vyoog.eisplatform.modules.registration.dto;

import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import jakarta.validation.constraints.NotNull;

public record ChangeMemberRoleRequest(@NotNull OrgRole orgRole) {
}
