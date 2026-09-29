package com.vyoog.eisplatform.modules.partner.controller;

import com.vyoog.eisplatform.modules.partner.dto.ApplyAsProviderRequest;
import com.vyoog.eisplatform.modules.partner.dto.ProviderDto;
import com.vyoog.eisplatform.modules.partner.service.PartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** 14.01.01.01 Register provider — public, no Vyoog account required.
 * Covered by SecurityConfig's permitAll rule for /partners/apply. */
@RestController
@RequiredArgsConstructor
public class ProviderApplicationController {

    private final PartnerService partnerService;

    @PostMapping("/partners/apply")
    public ProviderDto apply(@Valid @RequestBody ApplyAsProviderRequest request) {
        return partnerService.apply(request);
    }
}
