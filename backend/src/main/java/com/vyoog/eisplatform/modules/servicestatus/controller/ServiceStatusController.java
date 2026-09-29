package com.vyoog.eisplatform.modules.servicestatus.controller;

import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import com.vyoog.eisplatform.modules.servicestatus.dto.ServiceStatusPageDto;
import com.vyoog.eisplatform.modules.servicestatus.service.ServiceStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** REQ-PRT-001.3: the customer status page, for any signed-in user (covered by
 * SecurityConfig's {@code /me/**} authenticated rule). */
@RestController
@RequiredArgsConstructor
public class ServiceStatusController {

    private final ServiceStatusService serviceStatusService;
    private final CurrentCustomerResolver currentCustomerResolver;

    @GetMapping("/me/service-status")
    public ServiceStatusPageDto status(@AuthenticationPrincipal Jwt jwt) {
        Long customerId = currentCustomerResolver.resolveOptional(jwt).map(c -> c.getId()).orElse(null);
        return serviceStatusService.customerView(customerId);
    }
}
