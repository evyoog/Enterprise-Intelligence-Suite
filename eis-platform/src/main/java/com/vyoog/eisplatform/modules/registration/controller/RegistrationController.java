package com.vyoog.eisplatform.modules.registration.controller;

import com.vyoog.eisplatform.modules.product.dto.ProductDto;
import com.vyoog.eisplatform.modules.product.service.ProductService;
import com.vyoog.eisplatform.modules.registration.dto.*;
import com.vyoog.eisplatform.modules.registration.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Entirely public — permitAll in SecurityConfig, same tier as /auth/**. */
@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    private final ProductService productService;

    @PostMapping("/organization")
    public ResponseEntity<RegistrationAcceptedResponse> registerOrganization(@Valid @RequestBody OrganizationRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(registrationService.registerOrganization(request));
    }

    @PostMapping("/verify-email")
    public RegistrationStatusResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return registrationService.verifyEmail(request.token());
    }

    @PostMapping("/resend-verification")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        registrationService.resendVerification(request.registrationId());
    }

    /** Reuses the existing public product catalog as-is — the registration
     * wizard's "which products do you want" step is never allowed to drift
     * from the real, backend-driven catalog. */
    @GetMapping("/products")
    public List<ProductDto> listProducts() {
        return productService.listProducts();
    }

    @GetMapping("/status/{registrationId}")
    public RegistrationStatusResponse getStatus(@PathVariable String registrationId) {
        return registrationService.getStatus(registrationId);
    }
}
