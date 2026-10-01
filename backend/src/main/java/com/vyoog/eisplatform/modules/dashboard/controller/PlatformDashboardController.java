package com.vyoog.eisplatform.modules.dashboard.controller;

import com.vyoog.eisplatform.modules.dashboard.dto.PlatformDashboardDto;
import com.vyoog.eisplatform.modules.dashboard.service.PlatformDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * "/admin/platform-dashboard" (C53) — platform-wide overview for a platform
 * administrator. ADMIN-only ({@code VIEW_PLATFORM_DASHBOARD}, seeded by
 * RbacSeeder), enforced by SecurityConfig, the real security boundary, same
 * as every other admin-only endpoint in this app.
 */
@RestController
@RequestMapping("/admin/platform-dashboard")
@RequiredArgsConstructor
public class PlatformDashboardController {

    private final PlatformDashboardService platformDashboardService;

    @GetMapping
    public PlatformDashboardDto get() {
        return platformDashboardService.getPlatformDashboard();
    }
}
