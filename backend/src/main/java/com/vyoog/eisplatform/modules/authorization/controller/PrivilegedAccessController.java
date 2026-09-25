package com.vyoog.eisplatform.modules.authorization.controller;

import com.vyoog.eisplatform.modules.authorization.dto.PrivilegedAccessDecisionRequest;
import com.vyoog.eisplatform.modules.authorization.dto.PrivilegedAccessRequestCreateRequest;
import com.vyoog.eisplatform.modules.authorization.dto.PrivilegedAccessRequestDto;
import com.vyoog.eisplatform.modules.authorization.dto.RequestablePermissionDto;
import com.vyoog.eisplatform.modules.authorization.service.PrivilegedAccessService;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Phase 6 (PAM): "Normal user requests temporary admin access" — any
 * authenticated caller, deliberately more permissive than most of this app's
 * endpoints, since requesting elevated access is exactly the thing a
 * caller WITHOUT that access needs to be able to do. Works for both scopes:
 * {@code permissionName} alone determines which one server-side (see
 * {@code PrivilegedAccessService#resolveScope}) — the client never states a
 * scope or organization id.
 */
@RestController
@RequestMapping("/me/privileged-access")
@RequiredArgsConstructor
public class PrivilegedAccessController {

    private final PrivilegedAccessService privilegedAccessService;
    private final CurrentCustomerResolver currentCustomerResolver;

    @PostMapping("/requests")
    public PrivilegedAccessRequestDto request(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PrivilegedAccessRequestCreateRequest request) {
        Long customerId = currentCustomerResolver.resolveOptional(jwt).map(c -> c.getId()).orElse(null);
        return privilegedAccessService.request(jwt.getSubject(), customerId, request.permissionName(), request.justification(), request.durationMinutes());
    }

    /** Decision C24 (REQ-IAM-004): what the request form may offer this caller. */
    @GetMapping("/requestable-permissions")
    public List<RequestablePermissionDto> requestablePermissions(@AuthenticationPrincipal Jwt jwt) {
        Long customerId = currentCustomerResolver.resolveOptional(jwt).map(c -> c.getId()).orElse(null);
        return privilegedAccessService.listRequestablePermissions(customerId);
    }

    @GetMapping("/requests")
    public List<PrivilegedAccessRequestDto> myRequests(@AuthenticationPrincipal Jwt jwt) {
        return privilegedAccessService.listMyRequests(jwt.getSubject());
    }

    /** Withdrawing your own request/grant early — PENDING or an active
     * grant. Ownership (the request's own requester matches the caller) is
     * the only authorization check {@code revokeOwn} needs — see its own
     * javadoc for why that's safe regardless of scope. */
    @PostMapping("/requests/{id}/revoke")
    public PrivilegedAccessRequestDto revokeMine(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                  @RequestBody(required = false) PrivilegedAccessDecisionRequest body) {
        String note = body == null ? null : body.note();
        return privilegedAccessService.revokeOwn(jwt.getSubject(), id, note);
    }
}
