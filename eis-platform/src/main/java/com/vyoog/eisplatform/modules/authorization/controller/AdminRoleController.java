package com.vyoog.eisplatform.modules.authorization.controller;

import com.vyoog.eisplatform.modules.authorization.dto.CreateRoleRequest;
import com.vyoog.eisplatform.modules.authorization.dto.RoleDto;
import com.vyoog.eisplatform.modules.authorization.dto.UpdateRoleRequest;
import com.vyoog.eisplatform.modules.authorization.service.RoleAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Platform-admin RBAC administration — gated by the MANAGE_ROLES permission
 * at the URL level (see SecurityConfig), same pattern as every other
 * {@code /admin/**} controller in this app. */
@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class AdminRoleController {

    private final RoleAdminService roleAdminService;

    @GetMapping
    public List<RoleDto> list() {
        return roleAdminService.listRoles();
    }

    @GetMapping("/{id}")
    public RoleDto get(@PathVariable Long id) {
        return roleAdminService.getRole(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleDto create(@Valid @RequestBody CreateRoleRequest request) {
        return roleAdminService.createRole(request);
    }

    @PutMapping("/{id}")
    public RoleDto update(@PathVariable Long id, @RequestBody UpdateRoleRequest request) {
        return roleAdminService.updateRole(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        roleAdminService.deleteRole(id);
    }
}
