package com.vyoog.eisplatform.modules.servicestatus.controller;

import com.vyoog.eisplatform.modules.servicestatus.dto.AdminIncidentDto;
import com.vyoog.eisplatform.modules.servicestatus.dto.IncidentRequest;
import com.vyoog.eisplatform.modules.servicestatus.dto.IncidentRequest;
import com.vyoog.eisplatform.modules.servicestatus.dto.ProductStatusDto;
import com.vyoog.eisplatform.modules.servicestatus.dto.ServiceStatusPageDto;
import com.vyoog.eisplatform.modules.servicestatus.dto.UpdateProductStatusRequest;
import com.vyoog.eisplatform.modules.servicestatus.service.IncidentManagementService;
import com.vyoog.eisplatform.modules.servicestatus.service.ServiceStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REQ-PRT-001.1–.2: platform admins post product status and incidents.
 * Gated by {@code MANAGE_SERVICE_STATUS} in SecurityConfig (C26). */
@RestController
@RequestMapping("/admin/service-status")
@RequiredArgsConstructor
public class AdminServiceStatusController {

    private final ServiceStatusService serviceStatusService;
    private final IncidentManagementService incidentService;

    @GetMapping
    public ServiceStatusPageDto overview() {
        return serviceStatusService.adminView();
    }

    @PutMapping("/products/{productId}")
    public ProductStatusDto updateStatus(@PathVariable Long productId, @Valid @RequestBody UpdateProductStatusRequest request,
                                         @AuthenticationPrincipal Jwt jwt) {
        return serviceStatusService.updateProductStatus(productId, request, jwt.getSubject());
    }

    @GetMapping("/incidents")
    public List<AdminIncidentDto> incidents() {
        return incidentService.list();
    }

    @GetMapping("/incidents/{incidentId}")
    public AdminIncidentDto incident(@PathVariable Long incidentId) {
        return incidentService.get(incidentId);
    }

    @PostMapping("/incidents")
    public AdminIncidentDto createIncident(@Valid @RequestBody IncidentRequest request, @AuthenticationPrincipal Jwt jwt) {
        return incidentService.createIncident(request, jwt.getSubject());
    }

    @PutMapping("/incidents/{incidentId}")
    public AdminIncidentDto updateIncident(@PathVariable Long incidentId, @Valid @RequestBody IncidentRequest request,
                                           @AuthenticationPrincipal Jwt jwt) {
        return incidentService.updateIncident(incidentId, request, jwt.getSubject());
    }

    @PostMapping("/incidents/{incidentId}/tickets/{ticketId}")
    public AdminIncidentDto linkTicket(@PathVariable Long incidentId, @PathVariable Long ticketId, @AuthenticationPrincipal Jwt jwt) {
        return incidentService.linkTicket(incidentId, ticketId, jwt.getSubject());
    }

    @DeleteMapping("/incidents/{incidentId}/tickets/{ticketId}")
    public AdminIncidentDto unlinkTicket(@PathVariable Long incidentId, @PathVariable Long ticketId, @AuthenticationPrincipal Jwt jwt) {
        return incidentService.unlinkTicket(incidentId, ticketId, jwt.getSubject());
    }
}
