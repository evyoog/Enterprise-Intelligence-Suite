package com.vyoog.eisplatform.modules.authorization.controller;

import com.vyoog.eisplatform.modules.authorization.dto.PrivilegedAccessDecisionRequest;
import com.vyoog.eisplatform.modules.authorization.dto.PrivilegedAccessRequestDto;
import com.vyoog.eisplatform.modules.authorization.model.RoleScope;
import com.vyoog.eisplatform.modules.authorization.service.PrivilegedAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Platform-admin-only (gated by the MANAGE_PRIVILEGED_ACCESS permission —
 * see SecurityConfig; a distinct permission from MANAGE_CATALOG/
 * MANAGE_REGISTRATIONS, so approving elevated-access requests is its own
 * revocable responsibility, not folded into either). Handles only PLATFORM-
 * scope requests — see {@code OrganizationController} for the ORGANIZATION-
 * scope equivalent, kept alongside the rest of org self-service instead of
 * here.
 */
@RestController
@RequestMapping("/admin/privileged-access")
@RequiredArgsConstructor
public class PlatformPrivilegedAccessController {

    private final PrivilegedAccessService privilegedAccessService;

    @GetMapping("/pending")
    public List<PrivilegedAccessRequestDto> pending() {
        return privilegedAccessService.listPendingForPlatform();
    }

    /** REQ-IAM-004.7: active PLATFORM-scope grants, for early revocation. */
    @GetMapping("/active")
    public List<PrivilegedAccessRequestDto> active() {
        return privilegedAccessService.listActiveForPlatform();
    }

    @PostMapping("/{id}/approve")
    public PrivilegedAccessRequestDto approve(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                               @RequestBody(required = false) PrivilegedAccessDecisionRequest body) {
        String note = body == null ? null : body.note();
        return privilegedAccessService.approve(jwt.getSubject(), id, RoleScope.PLATFORM, null, note);
    }

    @PostMapping("/{id}/reject")
    public PrivilegedAccessRequestDto reject(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                              @RequestBody(required = false) PrivilegedAccessDecisionRequest body) {
        String note = body == null ? null : body.note();
        return privilegedAccessService.reject(jwt.getSubject(), id, RoleScope.PLATFORM, null, note);
    }

    @PostMapping("/{id}/revoke")
    public PrivilegedAccessRequestDto revoke(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                              @RequestBody(required = false) PrivilegedAccessDecisionRequest body) {
        String note = body == null ? null : body.note();
        return privilegedAccessService.revoke(jwt.getSubject(), id, RoleScope.PLATFORM, null, note);
    }
}
