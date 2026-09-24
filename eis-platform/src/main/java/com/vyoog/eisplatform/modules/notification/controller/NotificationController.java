package com.vyoog.eisplatform.modules.notification.controller;

import com.vyoog.eisplatform.modules.notification.dto.NotificationPreferenceDto;
import com.vyoog.eisplatform.modules.notification.dto.NotificationSummaryDto;
import com.vyoog.eisplatform.modules.notification.dto.UpdateNotificationPreferencesRequest;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/** Covered by SecurityConfig's existing {@code /me/**} authenticated() rule —
 * same as DashboardController/SearchHistoryController, no new rule needed. */
@RestController
@RequestMapping("/me/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentCustomerResolver currentCustomerResolver;

    @GetMapping
    public NotificationSummaryDto listNotifications(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return notificationService.listNotifications(customer.getId());
    }

    @PutMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        notificationService.markRead(customer.getId(), id);
    }

    @PutMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        notificationService.markAllRead(customer.getId());
    }

    @GetMapping("/preferences")
    public NotificationPreferenceDto getPreferences(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return notificationService.getPreferences(customer.getId());
    }

    @PutMapping("/preferences")
    public NotificationPreferenceDto updatePreferences(
            @AuthenticationPrincipal Jwt jwt, @RequestBody UpdateNotificationPreferencesRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return notificationService.updatePreferences(customer.getId(), request.emailDisabledCategories());
    }
}
