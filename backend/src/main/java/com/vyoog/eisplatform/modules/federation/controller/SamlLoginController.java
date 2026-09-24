package com.vyoog.eisplatform.modules.federation.controller;

import com.vyoog.eisplatform.modules.federation.dto.SsoCheckResponseDto;
import com.vyoog.eisplatform.modules.federation.service.SamlAuthenticationService;
import com.vyoog.eisplatform.modules.federation.service.SamlLoginException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Phase 5 (2026.3.3): the actual SAML login endpoints — all public (no JWT),
 * same reasoning as {@link SamlMetadataController} for {@code login-init}/
 * {@code sso-check} (nobody has a Vyoog token before they've logged in) and,
 * for {@code acs}, because the party POSTing here is the customer's own
 * external IdP via the end user's browser, not this app's SPA.
 *
 * <p>{@code login-init} and {@code acs} are real browser navigations (a top-level
 * redirect, and the IdP's own HTML form auto-POST), never {@code fetch}/XHR —
 * both respond with an HTTP redirect, on success AND on failure, rather than a
 * JSON error body no running script would ever read at that point in the flow.
 * {@code sso-check} is the one exception: the SPA calls it directly via
 * {@code fetch} before navigating anywhere, so it returns plain JSON.
 */
@Slf4j
@RestController
@RequestMapping("/saml")
@RequiredArgsConstructor
public class SamlLoginController {

    private final SamlAuthenticationService samlAuthenticationService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @GetMapping("/sso-check")
    public SsoCheckResponseDto ssoCheck(@RequestParam(required = false) String organizationCode) {
        return samlAuthenticationService.checkSso(organizationCode);
    }

    @GetMapping("/{organizationId}/login-init")
    public ResponseEntity<Void> loginInit(@PathVariable Long organizationId) {
        try {
            String redirectUrl = samlAuthenticationService.buildRedirectUrl(organizationId);
            return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
        } catch (SamlLoginException e) {
            log.warn("SAML login-init failed for organization {}: {}", organizationId, e.getMessage());
            return errorRedirect(e.getMessage());
        }
    }

    @PostMapping(value = "/{organizationId}/acs", consumes = "application/x-www-form-urlencoded")
    public void acs(@PathVariable Long organizationId,
                     @RequestParam("SAMLResponse") String samlResponse,
                     HttpServletResponse response) throws IOException {
        try {
            String redirectTarget = samlAuthenticationService.handleAcs(organizationId, samlResponse, response);
            response.sendRedirect(redirectTarget);
        } catch (SamlLoginException e) {
            log.warn("SAML ACS rejected for organization {}: {}", organizationId, e.getMessage());
            response.sendRedirect(frontendUrl + "/?ssoError=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        } catch (RuntimeException e) {
            log.error("Unexpected error handling SAML ACS for organization {}", organizationId, e);
            response.sendRedirect(frontendUrl + "/?ssoError="
                + URLEncoder.encode("Sign-in could not be completed. Please try again.", StandardCharsets.UTF_8));
        }
    }

    private ResponseEntity<Void> errorRedirect(String message) {
        String url = frontendUrl + "/?ssoError=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, url).build();
    }
}
