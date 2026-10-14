package com.vyoog.eisplatform.modules.support.controller;

import com.vyoog.eisplatform.modules.support.dto.SlaDtos;
import com.vyoog.eisplatform.modules.support.service.SlaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/** C101 / REQ-SUP-002: SLA targets, business hours and alert list. {@code MANAGE_SUPPORT_TICKETS}-gated by the {@code /admin/support/**} rule in SecurityConfig. */
@RestController
@RequestMapping("/admin/support/sla")
@RequiredArgsConstructor
public class AdminSlaController {

    private final SlaService slaService;

    @GetMapping
    public SlaDtos.Config get() {
        return slaService.config();
    }

    @PutMapping("/policies")
    public SlaDtos.Config updatePolicies(@RequestBody SlaDtos.PoliciesRequest request, @AuthenticationPrincipal Jwt jwt) {
        return slaService.updatePolicies(jwt.getSubject(), request);
    }

    @PutMapping("/settings")
    public SlaDtos.Config updateSettings(@RequestBody SlaDtos.Settings request, @AuthenticationPrincipal Jwt jwt) {
        return slaService.updateSettings(jwt.getSubject(), request);
    }
}
