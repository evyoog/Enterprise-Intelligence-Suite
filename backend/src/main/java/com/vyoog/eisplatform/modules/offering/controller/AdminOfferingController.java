package com.vyoog.eisplatform.modules.offering.controller;

import com.vyoog.eisplatform.modules.offering.dto.OfferingDtos.*;
import com.vyoog.eisplatform.modules.offering.service.EligibilityService;
import com.vyoog.eisplatform.modules.offering.service.OfferingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REQ-CAT-005 administration. SecurityConfig requires MANAGE_CATALOG for {@code /admin/offerings/**} (BR-OFR-001). */
@RestController
@RequestMapping("/admin/offerings")
@RequiredArgsConstructor
public class AdminOfferingController {

    private final OfferingService offerings;
    private final EligibilityService eligibility;

    @GetMapping
    public List<OfferingDto> list() {
        return offerings.adminList();
    }

    @GetMapping("/{id}")
    public OfferingDto get(@PathVariable Long id) {
        return offerings.adminGet(id);
    }

    @PostMapping
    public ResponseEntity<OfferingDto> create(@AuthenticationPrincipal Jwt jwt, @RequestBody OfferingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(offerings.create(jwt.getSubject(), jwt.getClaimAsString("email"), request));
    }

    @PutMapping("/{id}")
    public OfferingDto update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @RequestBody OfferingRequest request) {
        return offerings.update(jwt.getSubject(), jwt.getClaimAsString("email"), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        offerings.delete(jwt.getSubject(), jwt.getClaimAsString("email"), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product-rules")
    public List<ProductRuleDto> rules() {
        return eligibility.rules();
    }

    @PutMapping("/product-rules/{productId}")
    public ProductRuleDto setRules(@AuthenticationPrincipal Jwt jwt, @PathVariable Long productId, @RequestBody ProductRuleRequest request) {
        return eligibility.setRules(jwt.getSubject(), jwt.getClaimAsString("email"), productId, request);
    }
}
