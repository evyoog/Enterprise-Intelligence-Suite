package com.vyoog.eisplatform.modules.integration.controller;

import com.vyoog.eisplatform.modules.integration.dto.ApiKeyDto;
import com.vyoog.eisplatform.modules.integration.dto.CreateApiKeyRequest;
import com.vyoog.eisplatform.modules.integration.service.ApiKeyService;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import com.vyoog.eisplatform.common.exception.ForbiddenException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REQ-INT-001.1 (C61, C92): a signed-in user's legacy personal API keys, listed and revoked here; new keys are
 * created by organization administrators ({@link OrganizationApiKeyController}). */
@RestController
@RequestMapping("/me/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    /** Set on the request principal when the caller used an API key (see ApiKeyAuthenticationFilter). */
    public static final String API_KEY_CLAIM = "api_key_id";

    private final ApiKeyService apiKeyService;
    private final CurrentCustomerResolver currentCustomerResolver;

    @GetMapping
    public List<ApiKeyDto> list(Authentication authentication) {
        return apiKeyService.listOwn(currentCustomerResolver.resolve(jwt(authentication)).getId());
    }

    /** C92 (API-1 B): keys belong to the organization, so a person no longer creates their own. */
    @PostMapping
    public ApiKeyDto create(@Valid @RequestBody CreateApiKeyRequest request, Authentication authentication) {
        throw new ForbiddenException("API keys belong to your organization. An organization administrator creates them under "
            + "Organization → API keys.");
    }

    @PostMapping("/{id}/revoke")
    public ApiKeyDto revoke(@PathVariable Long id, Authentication authentication) {
        Jwt jwt = jwt(authentication);
        return apiKeyService.revoke(currentCustomerResolver.resolve(jwt).getId(), jwt.getSubject(), id);
    }

    private static Jwt jwt(Authentication authentication) {
        return (Jwt) authentication.getPrincipal();
    }
}
