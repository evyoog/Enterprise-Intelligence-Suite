package com.vyoog.eisplatform.modules.federation.controller;

import com.vyoog.eisplatform.modules.federation.service.OidcAuthenticationService;
import com.vyoog.eisplatform.modules.federation.service.OidcLoginException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * REQ-IAM-006 (C27): browser-navigation endpoints of OIDC sign-in, the OIDC
 * counterpart of SamlLoginController. Public (no token yet); any failure goes
 * back to the web app as {@code /?ssoError=}.
 */
@Slf4j
@RestController
@RequestMapping("/oidc")
@RequiredArgsConstructor
public class OidcLoginController {

    private final OidcAuthenticationService oidcAuthenticationService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @GetMapping("/{organizationId}/login-init")
    public void loginInit(@PathVariable Long organizationId, HttpServletResponse response) throws IOException {
        try {
            response.sendRedirect(oidcAuthenticationService.buildRedirectUrl(organizationId));
        } catch (OidcLoginException e) {
            log.warn("OIDC login-init failed for organization {}: {}", organizationId, e.getMessage());
            response.sendRedirect(errorUrl(e.getMessage()));
        }
    }

    @GetMapping("/{organizationId}/callback")
    public void callback(@PathVariable Long organizationId,
                         @RequestParam(required = false) String code,
                         @RequestParam(required = false) String state,
                         @RequestParam(required = false) String error,
                         HttpServletResponse response) throws IOException {
        try {
            response.sendRedirect(oidcAuthenticationService.handleCallback(organizationId, code, state, error, response));
        } catch (OidcLoginException e) {
            log.warn("OIDC callback rejected for organization {}: {}", organizationId, e.getMessage());
            response.sendRedirect(errorUrl(e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Unexpected error handling OIDC callback for organization {}", organizationId, e);
            response.sendRedirect(errorUrl("Sign-in could not be completed. Please try again."));
        }
    }

    private String errorUrl(String message) {
        return frontendUrl + "/?ssoError=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
    }
}
