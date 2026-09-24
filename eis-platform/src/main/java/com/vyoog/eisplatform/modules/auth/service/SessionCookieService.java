package com.vyoog.eisplatform.modules.auth.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

/**
 * Phase 5 (2026.3.3): the cookie/session-finalization mechanics that used to
 * live only as {@code AuthController}'s own private methods, extracted here
 * so the new SAML ACS endpoint (which must finish a login exactly the same
 * way — same refresh cookie, same cross-app {@code vyoog_sso} bridge cookie
 * — but lives in a different controller/package and returns an HTTP redirect
 * instead of a JSON body) can reuse it verbatim instead of duplicating it.
 * {@code AuthController} itself now delegates here too, rather than keeping
 * its own copy.
 */
@Service
public class SessionCookieService {

    public static final String REFRESH_COOKIE = "eis_rt";
    public static final String REFRESH_COOKIE_PATH = "/api/auth";
    public static final String SSO_COOKIE = "vyoog_sso";

    private final SsoBridgeSessionService bridgeSessionService;
    private final boolean cookieSecure;
    private final String ssoCookieDomain;

    public SessionCookieService(
            SsoBridgeSessionService bridgeSessionService,
            @Value("${vyoog.auth.cookie-secure}") boolean cookieSecure,
            @Value("${vyoog.internal.sso-cookie-domain:}") String ssoCookieDomain) {
        this.bridgeSessionService = bridgeSessionService;
        this.cookieSecure = cookieSecure;
        this.ssoCookieDomain = ssoCookieDomain;
    }

    public void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        response.addHeader("Set-Cookie", ResponseCookie.from(REFRESH_COOKIE, refreshToken)
            .httpOnly(true).secure(cookieSecure).sameSite("Lax").path(REFRESH_COOKIE_PATH).build().toString());
    }

    public void setSsoCookie(HttpServletResponse response, String ssoSessionId) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(SSO_COOKIE, ssoSessionId)
            .httpOnly(true).secure(cookieSecure).sameSite("Lax").path("/");
        if (ssoCookieDomain != null && !ssoCookieDomain.isBlank()) {
            builder.domain(ssoCookieDomain);
        }
        response.addHeader("Set-Cookie", builder.build().toString());
    }

    public void clearCookie(HttpServletResponse response, String name, String path, String domain) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, "")
            .httpOnly(true).secure(cookieSecure).sameSite("Lax").path(path).maxAge(0);
        if (domain != null && !domain.isBlank()) {
            builder.domain(domain);
        }
        response.addHeader("Set-Cookie", builder.build().toString());
    }

    /** Sets the real refresh cookie AND creates this app's own cross-app SSO
     * bridge row for {@code sub}/{@code refreshToken}, setting the resulting
     * {@code vyoog_sso} cookie too — the two cookies a browser needs to be
     * recognized as logged in on any subsequent request, regardless of which
     * controller/flow produced this particular login.
     *
     * @param impersonated whether {@code refreshToken} came from an
     *                     {@link ImpersonationExchangeService} exchange
     *                     (e.g. a SAML login — see SamlAuthenticationService)
     *                     rather than this app's own ROPC client (a normal
     *                     password login) — see
     *                     {@link SsoBridgeSessionService#createImpersonated}
     *                     for why this must be recorded accurately: a later
     *                     refresh/session call picks its Keycloak credentials
     *                     based on exactly this flag.
     */
    public void finalizeBrowserSession(HttpServletResponse response, String sub, String refreshToken, boolean impersonated) {
        setRefreshCookie(response, refreshToken);
        String ssoSessionId = impersonated
            ? bridgeSessionService.createImpersonated(sub, refreshToken)
            : bridgeSessionService.create(sub, refreshToken);
        setSsoCookie(response, ssoSessionId);
    }
}
