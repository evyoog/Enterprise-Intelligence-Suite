package com.vyoog.eisplatform.modules.servicestatus.controller;

import com.vyoog.eisplatform.modules.servicestatus.dto.IncidentDto;
import com.vyoog.eisplatform.modules.servicestatus.dto.IncidentRequest;
import com.vyoog.eisplatform.modules.servicestatus.dto.ProductStatusDto;
import com.vyoog.eisplatform.modules.servicestatus.dto.ServiceStatusPageDto;
import com.vyoog.eisplatform.modules.servicestatus.dto.UpdateProductStatusRequest;
import com.vyoog.eisplatform.modules.servicestatus.service.ServiceStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/** REQ-PRT-001.1–.2: platform admins post product status and incidents.
 * Gated by {@code MANAGE_SERVICE_STATUS} in SecurityConfig (C26). */
@RestController
@RequestMapping("/admin/service-status")
@RequiredArgsConstructor
public class AdminServiceStatusController {

    private final ServiceStatusService serviceStatusService;

    @GetMapping
    public ServiceStatusPageDto overview() {
        return serviceStatusService.adminView();
    }

    @PutMapping("/products/{productId}")
    public ProductStatusDto updateStatus(@PathVariable Long productId, @Valid @RequestBody UpdateProductStatusRequest request,
                                         @AuthenticationPrincipal Jwt jwt) {
        return serviceStatusService.updateProductStatus(productId, request, jwt.getSubject());
    }

    @PostMapping("/incidents")
    public IncidentDto createIncident(@Valid @RequestBody IncidentRequest request, @AuthenticationPrincipal Jwt jwt) {
        return serviceStatusService.createIncident(request, jwt.getSubject());
    }

    @PutMapping("/incidents/{incidentId}")
    public IncidentDto updateIncident(@PathVariable Long incidentId, @Valid @RequestBody IncidentRequest request,
                                      @AuthenticationPrincipal Jwt jwt) {
        return serviceStatusService.updateIncident(incidentId, request, jwt.getSubject());
    }
}
