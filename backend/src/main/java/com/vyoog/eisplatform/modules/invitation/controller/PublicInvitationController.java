package com.vyoog.eisplatform.modules.invitation.controller;

import com.vyoog.eisplatform.modules.invitation.dto.InvitationDtos.*;
import com.vyoog.eisplatform.modules.invitation.service.InvitationService;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * REQ-TEN-008 invited side. The token in the path is the only credential for the preview, the account
 * creation and the decline (SecurityConfig permits them); accepting needs a signed-in account.
 */
@RestController
@RequestMapping("/invitations")
@RequiredArgsConstructor
public class PublicInvitationController {

    private final InvitationService service;
    private final CurrentCustomerResolver currentCustomerResolver;

    @GetMapping("/{token}")
    public Preview preview(@PathVariable String token) {
        return service.preview(token);
    }

    @PostMapping("/{token}/account")
    public AcceptResult createAccount(@PathVariable String token, @RequestBody AccountRequest request) {
        return service.createAccount(token, request);
    }

    @PostMapping("/{token}/accept")
    public AcceptResult accept(@AuthenticationPrincipal Jwt jwt, @PathVariable String token) {
        return service.accept(token, currentCustomerResolver.resolve(jwt).getId());
    }

    @PostMapping("/{token}/decline")
    public ResponseEntity<Void> decline(@PathVariable String token) {
        service.decline(token);
        return ResponseEntity.noContent().build();
    }
}
