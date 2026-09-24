package com.vyoog.eisplatform.modules.auth.controller;

import com.vyoog.eisplatform.modules.auth.dto.*;
import com.vyoog.eisplatform.modules.auth.service.PlatformMfaService;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * Self-service Platform MFA management — always scoped to the CALLER's own
 * customer id, resolved from their JWT (see CurrentCustomerResolver), never
 * from a client-supplied id. There is deliberately no admin-facing "reset
 * another user's MFA" endpoint here — that capability isn't supported by
 * any of the three source documents' 22 named IAM functions, so it isn't
 * built (see this phase's own report).
 */
@RestController
@RequestMapping("/me/mfa")
@RequiredArgsConstructor
public class MfaController {

    private final PlatformMfaService platformMfaService;
    private final CurrentCustomerResolver currentCustomerResolver;

    @GetMapping
    public MfaStatusDto status(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return platformMfaService.getStatus(customer.getId());
    }

    @PostMapping("/enroll")
    public MfaEnrollResponse enroll(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody MfaEnrollRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return platformMfaService.enroll(customer.getId(), customer.getEmail(), request.currentPassword());
    }

    @PostMapping("/verify-enrollment")
    public MfaRecoveryCodesResponse verifyEnrollment(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody MfaVerifyEnrollmentRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return platformMfaService.verifyEnrollment(customer.getId(), customer.getEmail(), request.code());
    }

    @PostMapping("/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody MfaManagementActionRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        platformMfaService.disable(customer.getId(), customer.getEmail(), request.currentPassword(), request.code());
    }

    @PostMapping("/recovery-codes/regenerate")
    public MfaRecoveryCodesResponse regenerateRecoveryCodes(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody MfaManagementActionRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return platformMfaService.regenerateRecoveryCodes(customer.getId(), customer.getEmail(), request.currentPassword(), request.code());
    }
}
