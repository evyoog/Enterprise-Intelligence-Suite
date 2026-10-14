package com.vyoog.eisplatform.modules.registration.controller;

import com.vyoog.eisplatform.modules.registration.dto.EntitlementCheckRequest;
import com.vyoog.eisplatform.modules.registration.dto.EntitlementCheckResponse;
import com.vyoog.eisplatform.modules.registration.service.EntitlementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** REQ-SUB-002.5 (C97 EN-2): the check hosted products call, backend to backend, with the same {@code X-Internal-Sso-Secret} header as the SSO bridge. */
@RestController
@RequestMapping("/internal/entitlements")
public class InternalEntitlementController {

    private final EntitlementService entitlementService;
    private final String sharedSecret;

    public InternalEntitlementController(EntitlementService entitlementService,
                                         @Value("${vyoog.internal.sso-shared-secret}") String sharedSecret) {
        this.entitlementService = entitlementService;
        this.sharedSecret = sharedSecret;
    }

    @PostMapping("/check")
    public ResponseEntity<EntitlementCheckResponse> check(@Valid @RequestBody EntitlementCheckRequest request,
                                                          @RequestHeader(value = "X-Internal-Sso-Secret", required = false) String providedSecret) {
        if (!secretMatches(providedSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(entitlementService.check(request));
    }

    private boolean secretMatches(String provided) {
        if (provided == null || sharedSecret == null || sharedSecret.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(provided.getBytes(StandardCharsets.UTF_8), sharedSecret.getBytes(StandardCharsets.UTF_8));
    }
}
