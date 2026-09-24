package com.vyoog.eisplatform.modules.authorization.service;

import com.vyoog.eisplatform.common.exception.DuplicateResourceException;
import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.authorization.dto.CreatePermissionRequest;
import com.vyoog.eisplatform.modules.authorization.dto.PermissionDto;
import com.vyoog.eisplatform.modules.authorization.dto.UpdatePermissionRequest;
import com.vyoog.eisplatform.modules.authorization.model.Permission;
import com.vyoog.eisplatform.modules.authorization.model.Role;
import com.vyoog.eisplatform.modules.authorization.repository.PermissionRepository;
import com.vyoog.eisplatform.modules.authorization.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Phase 3 (2026.3.3): makes {@link Permission} genuinely admin-manageable —
 * see {@link RoleAdminService}'s own javadoc for the parallel reasoning on
 * roles. Every real permission check in this app tests a permission by
 * name (see {@code AuthorizationService#grants}), which is why name is
 * immutable once created and deletion is blocked while any role still
 * references it (see {@link #deletePermission}).
 */
@Service
@RequiredArgsConstructor
public class PermissionAdminService {

    /** Every permission RbacSeeder seeds on startup, plus the two this phase
     * adds for role/permission administration itself — none of these can be
     * deleted, since every enforcement point in SecurityConfig/
     * OrganizationSelfService/etc. checks for these exact names; removing
     * one wouldn't error, it would just silently make that check always deny. */
    private static final Set<String> PROTECTED_PERMISSION_NAMES = Set.of(
        "MANAGE_CATALOG", "MANAGE_REGISTRATIONS", "MANAGE_PRIVILEGED_ACCESS", "VIEW_AUDIT_LOG",
        "MANAGE_ORGANIZATION", "MANAGE_USERS", "MANAGE_PRODUCT_ACCESS",
        "MANAGE_ROLES", "MANAGE_PERMISSIONS"
    );

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<PermissionDto> listPermissions() {
        return permissionRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public PermissionDto createPermission(CreatePermissionRequest request) {
        String name = request.name().trim();
        if (permissionRepository.findByName(name).isPresent()) {
            throw new DuplicateResourceException("A permission named '" + name + "' already exists");
        }
        Permission permission = new Permission();
        permission.setName(name);
        permission.setDescription(request.description());
        permission = permissionRepository.save(permission);

        auditService.recordSuccess("PERMISSION_CREATED", null, null, null, "Permission", String.valueOf(permission.getId()), null,
            "Created permission " + name);
        return toDto(permission);
    }

    @Transactional
    public PermissionDto updatePermission(Long id, UpdatePermissionRequest request) {
        Permission permission = findOrThrow(id);
        permission.setDescription(request.description());
        permission = permissionRepository.save(permission);

        auditService.recordSuccess("PERMISSION_UPDATED", null, null, null, "Permission", String.valueOf(permission.getId()), null,
            "Updated permission " + permission.getName());
        return toDto(permission);
    }

    /** "Deactivate/delete where safe": a protected/seeded permission can
     * never be deleted. Any other permission can only be deleted once it's
     * detached from every role — silently cascading the delete would strip
     * that capability from whichever roles still granted it, with no way
     * for whoever holds that role to know why they lost access. */
    @Transactional
    public void deletePermission(Long id) {
        Permission permission = findOrThrow(id);
        if (PROTECTED_PERMISSION_NAMES.contains(permission.getName())) {
            throw new ForbiddenException("The '" + permission.getName() + "' permission is required by the platform and cannot be deleted");
        }
        List<Role> rolesGrantingIt = roleRepository.findByPermissions_Name(permission.getName());
        if (!rolesGrantingIt.isEmpty()) {
            throw new IllegalArgumentException(
                "This permission is still granted by " + rolesGrantingIt.size()
                    + " role(s) (" + rolesGrantingIt.stream().map(Role::getName).sorted().toList()
                    + ") — remove it from those roles first.");
        }
        permissionRepository.delete(permission);

        auditService.recordSuccess("PERMISSION_DELETED", null, null, null, "Permission", String.valueOf(id), null,
            "Deleted permission " + permission.getName());
    }

    private Permission findOrThrow(Long id) {
        return permissionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Permission not found"));
    }

    private PermissionDto toDto(Permission permission) {
        long roleCount = roleRepository.findByPermissions_Name(permission.getName()).size();
        return new PermissionDto(
            permission.getId(),
            permission.getName(),
            permission.getDescription(),
            PROTECTED_PERMISSION_NAMES.contains(permission.getName()),
            roleCount
        );
    }
}
