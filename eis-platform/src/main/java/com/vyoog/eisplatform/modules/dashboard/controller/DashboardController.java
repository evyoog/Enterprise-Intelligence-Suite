package com.vyoog.eisplatform.modules.dashboard.controller;

import com.vyoog.eisplatform.modules.dashboard.dto.DashboardDto;
import com.vyoog.eisplatform.modules.dashboard.dto.DashboardPreferenceDto;
import com.vyoog.eisplatform.modules.dashboard.dto.UpdateDashboardPreferencesRequest;
import com.vyoog.eisplatform.modules.dashboard.service.DashboardService;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * Phase 16: everything here is scoped to the caller's own customer id,
 * resolved from their JWT the same way every other {@code /me/**} endpoint
 * already does — already covered by SecurityConfig's existing
 * {@code /me/**} authenticated() rule, no new rule needed.
 */
@RestController
@RequestMapping("/me/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentCustomerResolver currentCustomerResolver;

    @GetMapping
    public DashboardDto getDashboard(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        var amr = jwt.getClaimAsStringList("amr");
        boolean mfaVerified = amr != null && amr.contains("otp");
        return dashboardService.getDashboard(customer.getId(), jwt.getSubject(), mfaVerified);
    }

    @PostMapping("/favorites/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addFavorite(@AuthenticationPrincipal Jwt jwt, @PathVariable Long productId) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        dashboardService.addFavorite(customer.getId(), productId);
    }

    @DeleteMapping("/favorites/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavorite(@AuthenticationPrincipal Jwt jwt, @PathVariable Long productId) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        dashboardService.removeFavorite(customer.getId(), productId);
    }

    /** Called when the frontend's own Launch button is clicked — see
     * DashboardService's own javadoc on why this is the entire basis for
     * "recently used"/"frequently used", real rather than fabricated. */
    @PostMapping("/launches/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recordLaunch(@AuthenticationPrincipal Jwt jwt, @PathVariable Long productId) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        dashboardService.recordLaunch(customer.getId(), productId);
    }

    @PutMapping("/preferences")
    public DashboardPreferenceDto updatePreferences(@AuthenticationPrincipal Jwt jwt, @RequestBody UpdateDashboardPreferencesRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return dashboardService.updatePreferences(customer.getId(), request.widgetOrder(), request.hiddenWidgets());
    }
}
