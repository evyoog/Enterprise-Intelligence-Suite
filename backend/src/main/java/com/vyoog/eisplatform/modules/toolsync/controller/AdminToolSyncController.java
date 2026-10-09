package com.vyoog.eisplatform.modules.toolsync.controller;

import com.vyoog.eisplatform.modules.toolsync.dto.ToolSyncDtos.ActionResultDto;
import com.vyoog.eisplatform.modules.toolsync.dto.ToolSyncDtos.ConnectorDto;
import com.vyoog.eisplatform.modules.toolsync.dto.ToolSyncDtos.DeliveryPageDto;
import com.vyoog.eisplatform.modules.toolsync.dto.ToolSyncDtos.ReconcileDto;
import com.vyoog.eisplatform.modules.toolsync.service.AdminToolSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REQ-INT-003: the "Tool sync" monitor. Under {@code /admin/events/**}, so gated by {@code MANAGE_INTEGRATIONS} in SecurityConfig; every action is audited. */
@RestController
@RequestMapping("/admin/events/tool-sync")
@RequiredArgsConstructor
public class AdminToolSyncController {

    private final AdminToolSyncService service;

    @GetMapping("/overview")
    public List<ConnectorDto> overview() {
        return service.overview();
    }

    @GetMapping("/deliveries")
    public DeliveryPageDto deliveries(@RequestParam(required = false) String status,
                                      @RequestParam(required = false) Long organizationId,
                                      @RequestParam(required = false) Long connectorId,
                                      @RequestParam(defaultValue = "0") int page) {
        return service.deliveries(status, organizationId, connectorId, page);
    }

    @PostMapping("/deliveries/{id}/retry")
    public ActionResultDto retry(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.retry(id, jwt.getSubject());
    }

    @PostMapping("/deliveries/{id}/replay")
    public ActionResultDto replay(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.replay(id, jwt.getSubject());
    }

    @PostMapping("/connectors/{id}/pause")
    public ActionResultDto pause(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.setPaused(id, true, jwt.getSubject());
    }

    @PostMapping("/connectors/{id}/resume")
    public ActionResultDto resume(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.setPaused(id, false, jwt.getSubject());
    }

    @PostMapping("/tenants/{organizationId}/{connectorId}/start")
    public ActionResultDto start(@PathVariable Long organizationId, @PathVariable Long connectorId, @AuthenticationPrincipal Jwt jwt) {
        return service.start(organizationId, connectorId, jwt.getSubject());
    }

    @PostMapping("/tenants/{organizationId}/{connectorId}/reconcile")
    public ReconcileDto reconcile(@PathVariable Long organizationId, @PathVariable Long connectorId,
                                  @RequestParam(defaultValue = "true") boolean repair, @AuthenticationPrincipal Jwt jwt) {
        return service.reconcile(organizationId, connectorId, repair, jwt.getSubject());
    }
}
