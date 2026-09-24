package com.vyoog.eisplatform.modules.authorization.dto;

public record PermissionDto(Long id, String name, String description, boolean systemManaged, long roleCount) {
}
