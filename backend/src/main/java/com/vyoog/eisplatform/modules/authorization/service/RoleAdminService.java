package com.vyoog.eisplatform.modules.authorization.service;

import com.vyoog.eisplatform.common.exception.DuplicateResourceException;
import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.authorization.dto.CreateRoleRequest;
import com.vyoog.eisplatform.modules.authorization.dto.RoleDto;
import com.vyoog.eisplatform.modules.authorization.dto.UpdateRoleRequest;
import com.vyoog.eisplatform.modules.authorization.model.Permission;
import com.vyoog.eisplatform.modules.authorization.model.Role;
import com.vyoog.eisplatform.modules.authorization.model.RoleScope;
import com.vyoog.eisplatform.modules.authorization.repository.PermissionRepository;
import com.vyoog.eisplatform.modules.authorization.repository.RoleRepository;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Phase 3 (2026.3.3): makes {@link Role} genuinely admin-manageable via API
 * rather than only ever created by {@link RbacSeeder} at startup — "what a
 * role is allowed to do" (its permission set) is fully editable for every
 * role; "does a brand-new role name actually mean anything" depends on
 * scope, enforced here rather than left to silently create dead data (see
 * {@link #requireAssignableName}).
 */
@Service
@RequiredArgsConstructor
public class RoleAdminService {

    /** These three role names are load-bearing — {@link RbacSeeder} re-creates
     * them on every startup if missing, {@code SecurityConfig}/{@code
     * AuthorizationService} assume ORG_ADMIN and MEMBER always exist, and
     * ADMIN is the one Keycloak client-role name this whole app currently
     * trusts for platform administration. Deleting (or renaming) any of
     * these would not fail loudly — it would silently strip everyone's
     * access on the next permission check. */
    private static final Set<String> PROTECTED_ROLE_NAMES = Set.of("ADMIN", "ORG_ADMIN", "MEMBER");

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<RoleDto> listRoles() {
        return roleRepository.findAll().stream().map(RoleAdminService::toDto).toList();
    }

    @Transactional(readOnly = true)
    public RoleDto getRole(Long id) {
        return toDto(findRoleOrThrow(id));
    }

    /**
     * PLATFORM scope is naturally open-ended — Keycloak can issue a client
     * role of any name, and {@code AuthorizationService.hasPlatformPermission}
     * will recognize it the moment some user's token carries it, so a new
     * PLATFORM role name is real the instant it's created here.
     *
     * <p>ORGANIZATION scope is NOT open-ended today — every organization
     * member's role is the fixed Java {@code OrgRole} enum (ORG_ADMIN,
     * MEMBER only; see that enum's own javadoc), not a free string. A role
     * row named anything else could never actually be held by any member —
     * it would be permanently inert data, not a usable role. Rather than
     * silently create that dead row, this refuses the request with a
     * message that explains exactly why (see {@link #requireAssignableName}) —
     * matching the master instruction to not invent policy levels the
     * existing architecture doesn't support.
     */
    @Transactional
    public RoleDto createRole(CreateRoleRequest request) {
        String name = request.name().trim();
        if (roleRepository.findByName(name).isPresent()) {
            throw new DuplicateResourceException("A role named '" + name + "' already exists");
        }
        requireAssignableName(request.scope(), name);

        Role role = new Role();
        role.setName(name);
        role.setScope(request.scope());
        role.setDescription(request.description());
        role.setPermissions(resolvePermissions(request.permissionIds()));
        role = roleRepository.save(role);

        auditService.recordSuccess("ROLE_CREATED", null, null, null, "Role", String.valueOf(role.getId()), null,
            "Created role " + name + " (" + request.scope() + ")");
        return toDto(role);
    }

    @Transactional
    public RoleDto updateRole(Long id, UpdateRoleRequest request) {
        Role role = findRoleOrThrow(id);
        role.setDescription(request.description());
        if (request.permissionIds() != null) {
            role.setPermissions(resolvePermissions(request.permissionIds()));
        }
        role = roleRepository.save(role);

        auditService.recordSuccess("ROLE_UPDATED", null, null, null, "Role", String.valueOf(role.getId()), null,
            "Updated role " + role.getName());
        return toDto(role);
    }

    /** "Deactivate/delete where safe" — a protected (seeded, system-relied-on)
     * role can never be deleted through this API at all, regardless of
     * whether anything currently holds it; there's no safe version of
     * deleting ADMIN/ORG_ADMIN/MEMBER. Any other role can always be deleted:
     * PLATFORM roles have no DB row tracking "who holds this" to strand (that
     * lives in Keycloak, untouched by this deletion), and an ORGANIZATION
     * role beyond the two protected names could never have been assigned to
     * anyone in the first place (see {@link #createRole}), so there's never
     * a real member left holding a role that just vanished underneath them. */
    @Transactional
    public void deleteRole(Long id) {
        Role role = findRoleOrThrow(id);
        if (PROTECTED_ROLE_NAMES.contains(role.getName())) {
            throw new ForbiddenException("The '" + role.getName() + "' role is required by the platform and cannot be deleted");
        }
        role.getPermissions().clear();
        roleRepository.save(role);
        roleRepository.delete(role);

        auditService.recordSuccess("ROLE_DELETED", null, null, null, "Role", String.valueOf(id), null,
            "Deleted role " + role.getName());
    }

    private void requireAssignableName(RoleScope scope, String name) {
        if (scope != RoleScope.ORGANIZATION) {
            return;
        }
        boolean matchesOrgRoleEnum = java.util.Arrays.stream(OrgRole.values()).anyMatch(r -> r.name().equals(name));
        if (!matchesOrgRoleEnum) {
            throw new IllegalArgumentException(
                "An organization-scope role name must be one of " + java.util.Arrays.toString(OrgRole.values())
                    + " — organization membership only recognizes these today, so any other name could never actually be assigned to a member.");
        }
    }

    private Set<Permission> resolvePermissions(List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
        if (permissions.size() != Set.copyOf(permissionIds).size()) {
            throw new IllegalArgumentException("One or more permission ids do not exist");
        }
        return permissions;
    }

    private Role findRoleOrThrow(Long id) {
        return roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role not found"));
    }

    private static RoleDto toDto(Role role) {
        return new RoleDto(
            role.getId(),
            role.getName(),
            role.getScope(),
            role.getDescription(),
            role.getPermissions().stream().map(Permission::getName).sorted().collect(Collectors.toList()),
            PROTECTED_ROLE_NAMES.contains(role.getName())
        );
    }
}
