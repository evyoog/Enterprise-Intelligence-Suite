package com.vyoog.eisplatform.modules.serviceinstance.controller;

import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import com.vyoog.eisplatform.modules.serviceinstance.dto.ServiceInstanceDtos;
import com.vyoog.eisplatform.modules.serviceinstance.service.ServiceInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** C101 / SR-4 A: an organization admin sees the organization's own service instances. */
@RestController
@RequiredArgsConstructor
public class OrganizationServiceInstanceController {

    private final CurrentCustomerResolver currentCustomerResolver;
    private final ServiceInstanceService service;

    @GetMapping("/organization/me/service-instances")
    public List<ServiceInstanceDtos.Instance> mine(@AuthenticationPrincipal Jwt jwt) {
        return service.listForOrganization(currentCustomerResolver.resolve(jwt).getId());
    }
}
