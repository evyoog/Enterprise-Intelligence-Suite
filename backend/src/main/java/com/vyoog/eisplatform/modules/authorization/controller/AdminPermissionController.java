package com.vyoog.eisplatform.modules.authorization.controller;

import com.vyoog.eisplatform.modules.authorization.dto.CreatePermissionRequest;
import com.vyoog.eisplatform.modules.authorization.dto.PermissionDto;
import com.vyoog.eisplatform.modules.authorization.dto.UpdatePermissionRequest;
import com.vyoog.eisplatform.modules.authorization.service.PermissionAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Platform-admin permission administration — gated by the MANAGE_PERMISSIONS
 * permission at the URL level (see SecurityConfig), deliberately its own
 * permission from MANAGE_ROLES: being able to define what a permission NAME
 * even means is a more foundational capability than assigning existing
 * permissions to a role. */
@RestController
@RequestMapping("/admin/permissions")
@RequiredArgsConstructor
public class AdminPermissionController {

    private final PermissionAdminService permissionAdminService;

    @GetMapping
    public List<PermissionDto> list() {
        return permissionAdminService.listPermissions();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PermissionDto create(@Valid @RequestBody CreatePermissionRequest request) {
        return permissionAdminService.createPermission(request);
    }

    @PutMapping("/{id}")
    public PermissionDto update(@PathVariable Long id, @RequestBody UpdatePermissionRequest request) {
        return permissionAdminService.updatePermission(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        permissionAdminService.deletePermission(id);
    }
}
