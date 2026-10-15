package com.vyoog.eisplatform.modules.registration.controller;

import com.vyoog.eisplatform.modules.registration.dto.AccessRequestDtos.AccessRequestDto;
import com.vyoog.eisplatform.modules.registration.dto.AccessRequestDtos.CreateAccessRequest;
import com.vyoog.eisplatform.modules.registration.dto.AccessRequestDtos.DecideAccessRequest;
import com.vyoog.eisplatform.modules.registration.service.AccessRequestService;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C103 / REQ-TEN-010: a member's request for their own access to a product the organization already subscribes to, and
 * removing one's own access. {@code /organization/me/**} is authenticated-only (any member); the admin-side list and
 * decide endpoints check {@code MANAGE_PRODUCT_ACCESS} inside the service, the same permission the Access management
 * screen's one-by-one grant already uses.
 */
@RestController
@RequestMapping("/organization/me")
@RequiredArgsConstructor
public class AccessRequestController {

    private final CurrentCustomerResolver currentCustomerResolver;
    private final AccessRequestService accessRequestService;

    @PostMapping("/access-requests")
    public AccessRequestDto request(@AuthenticationPrincipal Jwt jwt, @RequestBody CreateAccessRequest request) {
        return accessRequestService.request(currentCustomerResolver.resolve(jwt).getId(), request);
    }

    @GetMapping("/access-requests")
    public List<AccessRequestDto> myRequests(@AuthenticationPrincipal Jwt jwt) {
        return accessRequestService.myRequests(currentCustomerResolver.resolve(jwt).getId());
    }

    @PostMapping("/access-requests/{id}/cancel")
    public AccessRequestDto cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long id) {
        return accessRequestService.cancel(currentCustomerResolver.resolve(jwt).getId(), id);
    }

    @DeleteMapping("/products/{productId}/access")
    public void removeMyAccess(@AuthenticationPrincipal Jwt jwt, @PathVariable Long productId) {
        accessRequestService.removeMyAccess(currentCustomerResolver.resolve(jwt).getId(), productId);
    }

    /** Admin side: MANAGE_PRODUCT_ACCESS, checked inside the service (same as the one-by-one grant on the Access management screen). */
    @GetMapping("/access-requests/all")
    public List<AccessRequestDto> listForOrganization(@AuthenticationPrincipal Jwt jwt) {
        return accessRequestService.listForOrganization(currentCustomerResolver.resolve(jwt).getId());
    }

    @PostMapping("/access-requests/{id}/approve")
    public AccessRequestDto approve(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long id) {
        return accessRequestService.approve(currentCustomerResolver.resolve(jwt).getId(), id);
    }

    @PostMapping("/access-requests/{id}/reject")
    public AccessRequestDto reject(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long id, @RequestBody(required = false) DecideAccessRequest request) {
        return accessRequestService.reject(currentCustomerResolver.resolve(jwt).getId(), id, request);
    }
}
