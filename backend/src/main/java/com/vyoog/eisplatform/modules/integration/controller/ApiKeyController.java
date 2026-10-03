package com.vyoog.eisplatform.modules.integration.controller;

import com.vyoog.eisplatform.modules.integration.dto.ApiKeyDto;
import com.vyoog.eisplatform.modules.integration.dto.CreateApiKeyRequest;
import com.vyoog.eisplatform.modules.integration.service.ApiKeyService;
import com.vyoog.eisplatform.modules.registration.model.Customer;
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

/** REQ-INT-001.1 (C61): a signed-in user's own API keys (covered by the
 * {@code /me/**} authenticated rule). */
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiKeyDto create(@Valid @RequestBody CreateApiKeyRequest request, Authentication authentication) {
        Jwt jwt = jwt(authentication);
        if (jwt.hasClaim(API_KEY_CLAIM)) {
            // C61 default: a key cannot mint further keys; sign in to create one.
            throw new ForbiddenException("API keys can only be created when signed in, not with another API key.");
        }
        Customer owner = currentCustomerResolver.resolve(jwt);
        List<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return apiKeyService.create(owner, jwt.getSubject(), authorities, request);
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
