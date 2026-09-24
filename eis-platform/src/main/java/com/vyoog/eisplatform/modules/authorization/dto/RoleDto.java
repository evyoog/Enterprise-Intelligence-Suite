package com.vyoog.eisplatform.modules.authorization.dto;

import com.vyoog.eisplatform.modules.authorization.model.RoleScope;

import java.util.List;

public record RoleDto(Long id, String name, RoleScope scope, String description,
                       List<String> permissionNames, boolean systemManaged) {
}
