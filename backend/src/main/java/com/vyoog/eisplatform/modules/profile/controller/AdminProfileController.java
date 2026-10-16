package com.vyoog.eisplatform.modules.profile.controller;

import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.ProfileDto;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.ProfileUpdateRequest;
import com.vyoog.eisplatform.modules.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A platform admin views or edits any person's profile details (C107; no photo). Protected by
 * SecurityConfig's existing {@code /admin/registrations/**} rule (MANAGE_REGISTRATIONS), the same as every
 * neighbouring admin registration endpoint. Creating records for OTHER people stays with registration and
 * invitation.
 */
@RestController
@RequestMapping("/admin/registrations/individuals/{customerId}/profile")
@RequiredArgsConstructor
public class AdminProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ProfileDto get(@PathVariable Long customerId) {
        return profileService.getForAdmin(customerId);
    }

    @PutMapping
    public ProfileDto update(@PathVariable Long customerId, @AuthenticationPrincipal Jwt jwt,
                             @RequestBody ProfileUpdateRequest request) {
        return profileService.updateForAdmin(customerId, jwt, request);
    }
}
