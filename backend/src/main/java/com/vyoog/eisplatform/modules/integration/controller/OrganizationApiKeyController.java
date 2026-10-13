package com.vyoog.eisplatform.modules.integration.controller;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.modules.integration.dto.ApiKeyDto;
import com.vyoog.eisplatform.modules.integration.dto.CreateApiKeyRequest;
import com.vyoog.eisplatform.modules.integration.service.OrganizationApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REQ-INT-001.1 (C92, API-1 B): the organization's API keys, for organization administrators (permission
 * <em>Manage organization</em>, checked in the service). Covered by the {@code /organization/me/**} authenticated rule.
 */
@RestController
@RequestMapping("/organization/me/api-keys")
@RequiredArgsConstructor
public class OrganizationApiKeyController {

    private final OrganizationApiKeyService service;

    @GetMapping
    public List<ApiKeyDto> list(Authentication authentication) {
        return service.list(jwt(authentication));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiKeyDto create(@Valid @RequestBody CreateApiKeyRequest request, Authentication authentication) {
        Jwt jwt = jwt(authentication);
        if (jwt.hasClaim(ApiKeyController.API_KEY_CLAIM)) {
            // A key cannot mint further keys; sign in to create one.
            throw new ForbiddenException("API keys can only be created when signed in, not with another API key.");
        }
        List<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return service.create(jwt, authorities, request);
    }

    @PostMapping("/{id}/revoke")
    public ApiKeyDto revoke(@PathVariable Long id, Authentication authentication) {
        Jwt jwt = jwt(authentication);
        if (jwt.hasClaim(ApiKeyController.API_KEY_CLAIM)) {
            throw new ForbiddenException("API keys can only be revoked when signed in, not with another API key.");
        }
        return service.revoke(jwt, id);
    }

    private static Jwt jwt(Authentication authentication) {
        return (Jwt) authentication.getPrincipal();
    }
}
