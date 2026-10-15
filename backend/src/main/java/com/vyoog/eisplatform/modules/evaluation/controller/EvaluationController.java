package com.vyoog.eisplatform.modules.evaluation.controller;

import com.vyoog.eisplatform.modules.evaluation.dto.EvaluationDtos;
import com.vyoog.eisplatform.modules.evaluation.service.CompareService;
import com.vyoog.eisplatform.modules.evaluation.service.DemoRequestService;
import com.vyoog.eisplatform.modules.evaluation.service.TrialService;
import com.vyoog.eisplatform.modules.registration.dto.SubscriptionDto;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import com.vyoog.eisplatform.modules.support.dto.SupportTicketDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/** C102 / REQ-MKT-005: free trials, demo requests and the compare page. The compare page is public ({@code GET /products/**}); a token, when sent, adds the tax estimate. */
@RestController
@RequiredArgsConstructor
public class EvaluationController {

    private final CurrentCustomerResolver currentCustomerResolver;
    private final TrialService trialService;
    private final DemoRequestService demoRequestService;
    private final CompareService compareService;

    @PostMapping("/me/trials")
    public SubscriptionDto startTrial(@AuthenticationPrincipal Jwt jwt, @RequestBody EvaluationDtos.StartTrialRequest request) {
        return trialService.startForCustomer(currentCustomerResolver.resolve(jwt).getId(), request.productId());
    }

    @PostMapping("/organization/me/trials")
    public SubscriptionDto startOrganizationTrial(@AuthenticationPrincipal Jwt jwt, @RequestBody EvaluationDtos.StartTrialRequest request) {
        return trialService.startForOrganization(currentCustomerResolver.resolve(jwt).getId(), request.productId());
    }

    @PostMapping("/me/demo-requests")
    public SupportTicketDto requestDemo(@AuthenticationPrincipal Jwt jwt, @RequestBody EvaluationDtos.DemoRequest request) {
        return demoRequestService.request(currentCustomerResolver.resolve(jwt).getId(), request);
    }

    @GetMapping("/products/compare")
    public EvaluationDtos.Comparison compare(@RequestParam(required = false) String items, @RequestParam(required = false) Integer seats, @AuthenticationPrincipal Jwt jwt) {
        Long customerId = jwt == null ? null : currentCustomerResolver.resolve(jwt).getId();
        return compareService.compare(items, seats, customerId);
    }
}
