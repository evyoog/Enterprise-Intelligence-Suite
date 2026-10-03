package com.vyoog.eisplatform.modules.renewal.controller;

import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import com.vyoog.eisplatform.modules.renewal.dto.RenewalDto;
import com.vyoog.eisplatform.modules.renewal.dto.RenewalReminderPreferenceDto;
import com.vyoog.eisplatform.modules.renewal.dto.SaveRenewalReminderPreferenceRequest;
import com.vyoog.eisplatform.modules.renewal.service.RenewalReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REQ-SUB-004.8 (C64): the signed-in user's renewal reminder settings and
 * renewals (covered by the {@code /me/**} authenticated rule). */
@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class RenewalController {

    private final RenewalReminderService renewalReminderService;
    private final CurrentCustomerResolver currentCustomerResolver;

    @GetMapping("/notification-preferences/renewal-reminders")
    public RenewalReminderPreferenceDto preferences(@AuthenticationPrincipal Jwt jwt) {
        return renewalReminderService.preferences(currentCustomerResolver.resolve(jwt).getId());
    }

    @PutMapping("/notification-preferences/renewal-reminders")
    public RenewalReminderPreferenceDto savePreferences(@AuthenticationPrincipal Jwt jwt,
                                                        @Valid @RequestBody SaveRenewalReminderPreferenceRequest request) {
        return renewalReminderService.savePreferences(currentCustomerResolver.resolve(jwt).getId(), request);
    }

    @GetMapping("/renewals")
    public List<RenewalDto> renewals(@AuthenticationPrincipal Jwt jwt) {
        return renewalReminderService.myRenewals(currentCustomerResolver.resolve(jwt).getId());
    }
}
