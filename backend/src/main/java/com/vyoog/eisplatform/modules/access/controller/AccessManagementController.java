package com.vyoog.eisplatform.modules.access.controller;

import com.vyoog.eisplatform.modules.access.dto.AccessDtos.*;
import com.vyoog.eisplatform.modules.access.service.AccessManagementService;
import com.vyoog.eisplatform.modules.audit.dto.AuditLogPageDto;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * REQ-TEN-005 (C90): Access management API of the caller's own organization. The permission (Manage access) and the
 * guard rails are checked in {@link AccessManagementService}; nothing here takes an organization id.
 */
@RestController
@RequestMapping("/organization/me/access")
@RequiredArgsConstructor
public class AccessManagementController {

    private final AccessManagementService accessService;
    private final CurrentCustomerResolver currentCustomerResolver;

    @GetMapping("/permissions")
    public List<PermissionCatalogEntry> permissions(@AuthenticationPrincipal Jwt jwt) {
        return accessService.catalogue(currentCustomerResolver.resolve(jwt).getId());
    }

    @GetMapping("/members")
    public MemberPage members(@AuthenticationPrincipal Jwt jwt,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size,
                              @RequestParam(required = false) String q,
                              @RequestParam(required = false) String role,
                              @RequestParam(required = false) Long productId,
                              @RequestParam(required = false) Boolean custom,
                              @RequestParam(required = false) Instant reviewedBefore) {
        return accessService.listMembers(currentCustomerResolver.resolve(jwt).getId(), q, role, productId, custom, reviewedBefore, page, size);
    }

    @GetMapping("/members/{memberId}")
    public MemberDetail member(@AuthenticationPrincipal Jwt jwt, @PathVariable Long memberId) {
        return accessService.memberDetail(currentCustomerResolver.resolve(jwt).getId(), memberId);
    }

    @PutMapping("/members/{memberId}/overrides")
    public MemberDetail setOverrides(@AuthenticationPrincipal Jwt jwt, @PathVariable Long memberId,
                                     @RequestBody SetOverridesRequest request) {
        return accessService.setOverrides(currentCustomerResolver.resolve(jwt).getId(), memberId, request);
    }

    @DeleteMapping("/members/{memberId}/overrides")
    public MemberDetail resetAll(@AuthenticationPrincipal Jwt jwt, @PathVariable Long memberId) {
        return accessService.resetAll(currentCustomerResolver.resolve(jwt).getId(), memberId);
    }

    @GetMapping("/roles/{role}/defaults")
    public RoleDefaults roleDefaults(@AuthenticationPrincipal Jwt jwt, @PathVariable String role) {
        return accessService.roleDefaults(currentCustomerResolver.resolve(jwt).getId(), role);
    }

    @PutMapping("/roles/{role}/defaults")
    public RoleDefaultsResult saveRoleDefaults(@AuthenticationPrincipal Jwt jwt, @PathVariable String role,
                                               @RequestParam(defaultValue = "false") boolean preview,
                                               @RequestBody RoleDefaultsRequest request) {
        return accessService.saveRoleDefaults(currentCustomerResolver.resolve(jwt).getId(), role, request, preview);
    }

    @PostMapping("/bulk")
    public BulkResult bulk(@AuthenticationPrincipal Jwt jwt, @RequestBody BulkRequest request) {
        return accessService.bulk(currentCustomerResolver.resolve(jwt).getId(), request);
    }

    @GetMapping("/activity")
    public AuditLogPageDto activity(@AuthenticationPrincipal Jwt jwt,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return accessService.activity(currentCustomerResolver.resolve(jwt).getId(), page, size);
    }
}
