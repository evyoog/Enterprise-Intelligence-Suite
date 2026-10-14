package com.vyoog.eisplatform.modules.renewal.controller;

import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import com.vyoog.eisplatform.modules.renewal.dto.ReminderRecipientsDto;
import com.vyoog.eisplatform.modules.renewal.dto.SaveReminderRecipientsRequest;
import com.vyoog.eisplatform.modules.renewal.service.RenewalReminderRecipientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** C100 RR-5: who gets the organization's renewal reminders. Needs the organization's Manage billing permission (checked in the service);
 * covered by the {@code /organization/me/**} authenticated rule. */
@RestController
@RequestMapping("/organization/me/renewal-reminder-recipients")
@RequiredArgsConstructor
public class RenewalRecipientsController {

    private final RenewalReminderRecipientService recipientService;
    private final CurrentCustomerResolver currentCustomerResolver;

    @GetMapping
    public ReminderRecipientsDto get(@AuthenticationPrincipal Jwt jwt) {
        return recipientService.get(currentCustomerResolver.resolve(jwt).getId());
    }

    @PutMapping
    public ReminderRecipientsDto save(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SaveReminderRecipientsRequest request) {
        return recipientService.save(currentCustomerResolver.resolve(jwt).getId(), request);
    }
}
