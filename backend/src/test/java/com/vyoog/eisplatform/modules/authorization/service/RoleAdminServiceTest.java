package com.vyoog.eisplatform.modules.authorization.service;

import com.vyoog.eisplatform.common.exception.DuplicateResourceException;
import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.modules.authorization.dto.CreatePermissionRequest;
import com.vyoog.eisplatform.modules.authorization.dto.CreateRoleRequest;
import com.vyoog.eisplatform.modules.authorization.dto.PermissionDto;
import com.vyoog.eisplatform.modules.authorization.dto.RoleDto;
import com.vyoog.eisplatform.modules.authorization.dto.UpdateRoleRequest;
import com.vyoog.eisplatform.modules.authorization.model.RoleScope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Phase 3 (2026.3.3): makes Role/Permission genuinely admin-manageable (see
 * RoleAdminService's own javadoc) — this test proves both the happy path and
 * the two safety rails that stop it from creating dead data or breaking
 * something every existing permission check relies on.
 */
@SpringBootTest
@ActiveProfiles("test")
class RoleAdminServiceTest {

    @Autowired
    private RoleAdminService roleAdminService;
    @Autowired
    private PermissionAdminService permissionAdminService;

    @Test
    void canCreateAPlatformScopeRoleWithAnyName() {
        RoleDto role = roleAdminService.createRole(
            new CreateRoleRequest("SECURITY_ADMIN_" + System.nanoTime(), RoleScope.PLATFORM, "Security administrator", List.of()));

        assertThat(role.id()).isNotNull();
        assertThat(role.scope()).isEqualTo(RoleScope.PLATFORM);
        assertThat(role.systemManaged()).isFalse();
    }

    @Test
    void cannotCreateAnOrganizationScopeRoleWithANameOutsideTheOrgRoleEnum() {
        assertThatThrownBy(() -> roleAdminService.createRole(
            new CreateRoleRequest("BILLING_ADMIN_" + System.nanoTime(), RoleScope.ORGANIZATION, "desc", List.of())))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cannotCreateADuplicateRoleName() {
        assertThatThrownBy(() -> roleAdminService.createRole(
            new CreateRoleRequest("ADMIN", RoleScope.PLATFORM, "duplicate", List.of())))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void seededRolesAreProtectedFromDeletion() {
        RoleDto admin = roleAdminService.listRoles().stream().filter(r -> r.name().equals("ADMIN")).findFirst().orElseThrow();
        assertThat(admin.systemManaged()).isTrue();
        assertThatThrownBy(() -> roleAdminService.deleteRole(admin.id())).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void aNewlyCreatedRoleCanBeUpdatedAndThenDeleted() {
        PermissionDto permission = permissionAdminService.createPermission(
            new CreatePermissionRequest("TEST_PERM_" + System.nanoTime(), "test"));
        RoleDto role = roleAdminService.createRole(
            new CreateRoleRequest("TEMP_ROLE_" + System.nanoTime(), RoleScope.PLATFORM, "temp", List.of()));

        RoleDto updated = roleAdminService.updateRole(role.id(), new UpdateRoleRequest("updated desc", List.of(permission.id())));
        assertThat(updated.description()).isEqualTo("updated desc");
        assertThat(updated.permissionNames()).containsExactly(permission.name());

        roleAdminService.deleteRole(role.id());
        assertThatThrownBy(() -> roleAdminService.getRole(role.id()))
            .isInstanceOf(com.vyoog.eisplatform.common.exception.ResourceNotFoundException.class);
    }

    @Test
    void protectedPermissionsCannotBeDeleted() {
        PermissionDto manageCatalog = permissionAdminService.listPermissions().stream()
            .filter(p -> p.name().equals("MANAGE_CATALOG")).findFirst().orElseThrow();
        assertThat(manageCatalog.systemManaged()).isTrue();
        assertThatThrownBy(() -> permissionAdminService.deletePermission(manageCatalog.id())).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void aPermissionStillGrantedByARoleCannotBeDeleted() {
        PermissionDto permission = permissionAdminService.createPermission(
            new CreatePermissionRequest("IN_USE_PERM_" + System.nanoTime(), "test"));
        roleAdminService.createRole(new CreateRoleRequest("USES_IT_" + System.nanoTime(), RoleScope.PLATFORM, "d", List.of(permission.id())));

        assertThatThrownBy(() -> permissionAdminService.deletePermission(permission.id())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aPermissionNoLongerGrantedByAnyRoleCanBeDeleted() {
        PermissionDto permission = permissionAdminService.createPermission(
            new CreatePermissionRequest("UNUSED_PERM_" + System.nanoTime(), "test"));
        permissionAdminService.deletePermission(permission.id());
        assertThat(permissionAdminService.listPermissions().stream().noneMatch(p -> p.id().equals(permission.id()))).isTrue();
    }
}
