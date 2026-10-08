package com.vyoog.eisplatform.modules.invitation.controller;

import com.vyoog.eisplatform.modules.invitation.dto.InvitationDtos.*;
import com.vyoog.eisplatform.modules.invitation.service.InvitationService;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REQ-TEN-008 inviter side. Covered by the existing {@code /organization/me/**} authenticated rule;
 * INVITE_USERS and the "own organization only" scoping are enforced in the service.
 */
@RestController
@RequestMapping("/organization/me")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService service;
    private final CurrentCustomerResolver currentCustomerResolver;

    private Long caller(Jwt jwt) {
        return currentCustomerResolver.resolve(jwt).getId();
    }

    @PostMapping("/invitations")
    public ResponseEntity<CreateResult> create(@AuthenticationPrincipal Jwt jwt, @RequestBody CreateInvitationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(caller(jwt), request));
    }

    @GetMapping("/invitations")
    public List<InvitationDto> list(@AuthenticationPrincipal Jwt jwt, @RequestParam(required = false) String status) {
        return service.list(caller(jwt), status);
    }

    @PostMapping("/invitations/{id}/resend")
    public CreateResult resend(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return service.resend(caller(jwt), id);
    }

    @PostMapping("/invitations/{id}/revoke")
    public InvitationDto revoke(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return service.revoke(caller(jwt), id);
    }

    @GetMapping("/invitations/structure-nodes")
    public List<NodeOption> structureNodes(@AuthenticationPrincipal Jwt jwt) {
        return service.structureNodes(caller(jwt));
    }

    @GetMapping("/invitations/inviters")
    public InvitersDto inviters(@AuthenticationPrincipal Jwt jwt) {
        return service.inviters(caller(jwt));
    }

    @PutMapping("/members/{memberId}/invite-permission")
    public InvitersDto setInvitePermission(@AuthenticationPrincipal Jwt jwt, @PathVariable Long memberId,
                                           @RequestBody InvitePermissionRequest request) {
        return service.setInvitePermission(caller(jwt), memberId, request.allowed());
    }
}
