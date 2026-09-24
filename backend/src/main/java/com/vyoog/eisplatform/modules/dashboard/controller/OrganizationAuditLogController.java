package com.vyoog.eisplatform.modules.dashboard.controller;

import com.vyoog.eisplatform.modules.audit.dto.AuditLogPageDto;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import com.vyoog.eisplatform.modules.registration.service.OrganizationSelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 25: an org admin's own view of their organization's audit trail —
 * same MANAGE_ORGANIZATION access boundary as BusinessDashboardController
 * (enforced inside the service, not the URL), and lives here rather than in
 * modules.registration for the same reason BusinessDashboardController does
 * (see that class's own javadoc): avoids a two-way module dependency.
 */
@RestController
@RequestMapping("/organization/me/audit-logs")
@RequiredArgsConstructor
public class OrganizationAuditLogController {

    private final AuditService auditService;
    private final OrganizationSelfService organizationSelfService;
    private final CurrentCustomerResolver currentCustomerResolver;

    @GetMapping
    public AuditLogPageDto search(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Long customerId = currentCustomerResolver.resolve(jwt).getId();
        Long organizationId = organizationSelfService.requireOrganizationManagement(customerId);
        return auditService.search(organizationId, null, action, null, null, page, size);
    }
}
