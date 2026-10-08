package com.vyoog.eisplatform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Public storefront reads (product listing/detail) and the login/refresh/logout endpoints
 * stay open; everything else requires a valid Keycloak-issued JWT (see
 * spring.security.oauth2.resourceserver.jwt in application.yml).
 *
 * Patterns here are matched against the path AFTER server.servlet.context-path (/api) is
 * stripped — "/products/**", not "/api/products/**" — since Spring Security matches on the
 * servlet path, not the full request URI.
 */
@Configuration
public class SecurityConfig {

    @Value("${app.cors-allowed-origins}")
    private List<String> corsAllowedOrigins;

    private final KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter;
    private final PermissionAuthorizationManagerFactory permissions;
    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(
            KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter,
            PermissionAuthorizationManagerFactory permissions,
            ApiKeyAuthenticationFilter apiKeyAuthenticationFilter,
            RateLimitFilter rateLimitFilter) {
        this.keycloakJwtAuthenticationConverter = keycloakJwtAuthenticationConverter;
        this.permissions = permissions;
        this.apiKeyAuthenticationFilter = apiKeyAuthenticationFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // REQ-INT-001 (C61): X-API-Key authentication before the bearer-token
            // filter, rate limiting once the caller is known.
            .addFilterBefore(apiKeyAuthenticationFilter, BearerTokenAuthenticationFilter.class)
            .addFilterAfter(rateLimitFilter, BearerTokenAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // Order matters here: rules are evaluated top-to-bottom, first match
                // wins. The ADMIN-only GET /products/admin is a specific case of the
                // general GET /products/** pattern below it — it MUST come first, or
                // the general permitAll would match it before this rule is ever
                // reached (exactly the bug already found once in this file: a
                // wildcard rule silently swallowing a more specific one placed after
                // it never gets evaluated).
                // Phase 3: every hasRole("ADMIN") below became a named-permission
                // check via PermissionAuthorizationManagerFactory instead — same JWT
                // authority ("ROLE_ADMIN") still decides who qualifies (see that
                // class's own javadoc for why that source wasn't changed), but WHAT
                // that role is allowed to do is now real, DB-editable data
                // (Role/Permission, seeded by RbacSeeder) instead of a string
                // literal repeated across this file.
                // "/products/admin/search" is listed explicitly alongside
                // "/products/admin" rather than relying on a wildcard — an exact
                // literal here can't accidentally swallow a future sibling path
                // the way an Ant-style "/products/admin/**" pattern's edge cases
                // might (see this rule block's own opening comment on why
                // ordering/pattern precision here is treated carefully).
                .requestMatchers(HttpMethod.GET, "/products/admin", "/products/admin/search")
                    .access(permissions.platformPermission("MANAGE_CATALOG"))
                .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                // REQ-CAT-005 Offerings (02.02): public browsing of ACTIVE offerings (the
                // service filters; the same visibility as GET /products/**).
                .requestMatchers(HttpMethod.GET, "/offerings", "/offerings/*").permitAll()
                // Product mutations require the MANAGE_CATALOG permission. This check is
                // the real security boundary — the frontend hiding the "Settings" menu
                // from non-admins is just UX, not enforcement; someone could still POST
                // here directly with a valid non-admin token if this rule weren't here.
                .requestMatchers(HttpMethod.POST, "/products/images").access(permissions.platformPermission("MANAGE_CATALOG"))
                .requestMatchers(HttpMethod.POST, "/products").access(permissions.platformPermission("MANAGE_CATALOG"))
                // 02.01.01 Product Lifecycle (sprint 2026.4.1) — publish/retire.
                // Neither is the exact-path POST /products above, so each needs
                // its own explicit rule, same reasoning as DELETE /products/** below.
                .requestMatchers(HttpMethod.POST, "/products/*/publish", "/products/*/retire")
                    .access(permissions.platformPermission("MANAGE_CATALOG"))
                .requestMatchers(HttpMethod.PUT, "/products/**").access(permissions.platformPermission("MANAGE_CATALOG"))
                // Without this explicit rule, DELETE /products/{id} would fall
                // through to the generic authenticated() catch-all below and be
                // reachable by ANY logged-in customer, not just an admin.
                .requestMatchers(HttpMethod.DELETE, "/products/**").access(permissions.platformPermission("MANAGE_CATALOG"))
                // Platforms have no public-facing listing (unlike products) — only
                // the uploaded-logo GET needs to stay open, for the same reason
                // product images do: a plain <img> tag can't attach a bearer token.
                // That specific rule must come first, same ordering reason as above.
                // C66: the public Product Catalog's platform cards and details
                // (ACTIVE, shown-in-catalog platforms only — see CatalogService).
                .requestMatchers(HttpMethod.GET, "/catalog/platforms", "/catalog/platforms/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/platforms/images/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/platforms/**").access(permissions.platformPermission("MANAGE_CATALOG"))
                .requestMatchers(HttpMethod.POST, "/platforms/images").access(permissions.platformPermission("MANAGE_CATALOG"))
                .requestMatchers(HttpMethod.POST, "/platforms").access(permissions.platformPermission("MANAGE_CATALOG"))
                .requestMatchers(HttpMethod.PUT, "/platforms/**").access(permissions.platformPermission("MANAGE_CATALOG"))
                // Same reasoning as DELETE /products/** above.
                .requestMatchers(HttpMethod.DELETE, "/platforms/**").access(permissions.platformPermission("MANAGE_CATALOG"))
                .requestMatchers("/auth/**").permitAll()
                // Phase 4 (2026.3.3): this app's own SP metadata — fetched by
                // a customer's own IdP administrator, who has no Vyoog
                // account or token (see SamlMetadataController's own doc).
                .requestMatchers(HttpMethod.GET, "/saml/*/metadata").permitAll()
                // Phase 5 (2026.3.3): the actual SAML login endpoints — all
                // reached before any Vyoog token exists (sso-check/login-init
                // are pre-login SPA/browser calls; acs is POSTed to directly
                // by the customer's own external IdP) — see
                // SamlLoginController's own doc. Authorization here is
                // enforced entirely inside SamlAuthenticationService (stable
                // per-organization identity mapping, request/replay tracking),
                // never by a JWT, since none of these callers have one.
                .requestMatchers(HttpMethod.GET, "/saml/sso-check").permitAll()
                .requestMatchers(HttpMethod.GET, "/saml/*/login-init").permitAll()
                .requestMatchers(HttpMethod.POST, "/saml/*/acs").permitAll()
                // REQ-IAM-006 (C27): OIDC sign-in navigations, before any session exists.
                .requestMatchers(HttpMethod.GET, "/oidc/*/login-init").permitAll()
                .requestMatchers(HttpMethod.GET, "/oidc/*/callback").permitAll()
                // Guarded by its own shared-secret header check inside the controller,
                // not JWT — this is a backend-to-backend call from PMS's own backend,
                // which has no Keycloak-issued bearer token of its own to present here.
                .requestMatchers("/internal/**").permitAll()
                // Registration + email verification + the registration wizard's own
                // product-catalog read are all pre-login by definition — nobody has a
                // token yet at this point.
                .requestMatchers("/register/**").permitAll()
                // 11.01 Knowledge Base (sprint 2027.1.1): public reads, same
                // reasoning as the product catalog above — a visitor needs
                // product knowledge before they buy, signed in or not.
                .requestMatchers(HttpMethod.GET, "/knowledge-base/**").permitAll()
                // REQ-KNW-005 Knowledge Center (C71–C77): reads are public;
                // what each caller may see is decided per item in the service
                // (BR-KVS-001) from the caller's own token, if any. Feedback
                // needs a signed-in reader (checked in the service); the
                // assistant answers "not configured" until D8.
                .requestMatchers("/knowledge/**").permitAll()
                // 01.03 Global Search (sprint 2027.1.3): public for products/
                // knowledge articles; ticket results are scoped inside the
                // service itself to whichever customer the caller's own JWT
                // resolves to (empty for a signed-out caller) — see
                // GlobalSearchController's own doc.
                .requestMatchers(HttpMethod.GET, "/search", "/search/suggest").permitAll()
                // REQ-CAT-004 Product content administration (02.04): the catalog
                // permission, same as every product mutation above (BR-PCON-001).
                .requestMatchers("/admin/products/**").access(permissions.platformPermission("MANAGE_CATALOG"))
                // REQ-CAT-005 Offering management + product rules: the catalog permission (BR-OFR-001).
                .requestMatchers("/admin/offerings/**").access(permissions.platformPermission("MANAGE_CATALOG"))
                .requestMatchers("/admin/knowledge-base/**").access(permissions.platformPermission("MANAGE_KNOWLEDGE_BASE"))
                // REQ-KNW-008 Knowledge Management: contributors
                // (KNOWLEDGE_CONTRIBUTE) or publishers (MANAGE_KNOWLEDGE_BASE,
                // reused — C71). Publisher-only actions are checked again in
                // the services (BR-KPRM-002); ADMIN holds both.
                .requestMatchers("/admin/knowledge/**").access(AuthorizationManagers.anyOf(
                    permissions.platformPermission("KNOWLEDGE_CONTRIBUTE"),
                    permissions.platformPermission("MANAGE_KNOWLEDGE_BASE")))
                // 12.01 Ticket Management (sprint 2027.1.2): admin ticket actions.
                // Creating/tracking a customer's own tickets is covered by the
                // existing "/me/**" rule below.
                .requestMatchers("/admin/support/**").access(permissions.platformPermission("MANAGE_SUPPORT_TICKETS"))
                // 03.04 Reviews & Ratings (sprint 2027.1.3): moderating reviews.
                // Public GET /products/{id}/reviews is covered by the existing
                // GET /products/** permitAll rule above; submitting/reading the
                // caller's own review is covered by the existing /me/** rule.
                .requestMatchers("/admin/reviews/**").access(permissions.platformPermission("MANAGE_REVIEWS"))
                // 14.01 Provider Onboarding (sprint 2027.2.1): a prospective
                // partner applies before it has any Vyoog identity — same
                // reasoning as /register/** above, so it's public, not
                // /me/**. Verify/approve/activate/reject and contract
                // management are platform-admin-only.
                .requestMatchers(HttpMethod.POST, "/partners/apply").permitAll()
                .requestMatchers("/admin/partners/**").access(permissions.platformPermission("MANAGE_PARTNERS"))
                // Platform-admin-only registration/provisioning actions — requires the
                // MANAGE_REGISTRATIONS permission (same "ADMIN" JWT authority qualifies
                // today, see RbacSeeder, but this is now a distinct, separately
                // revocable permission from MANAGE_CATALOG above).
                .requestMatchers("/admin/registrations/**").access(permissions.platformPermission("MANAGE_REGISTRATIONS"))
                // REQ-TEN-007 (C83): the read-only Organizations directory and detail tabs.
                .requestMatchers("/admin/organizations/**").access(permissions.platformPermission("MANAGE_REGISTRATIONS"))
                // C26 (REQ-PRT-001): posting product status and incidents.
                .requestMatchers("/admin/service-status/**").access(permissions.platformPermission("MANAGE_SERVICE_STATUS"))
                // 15.01 Platform Administration (sprint 2026.4.2): currencies, regions, feature flags.
                .requestMatchers("/admin/platform-settings/**").access(permissions.platformPermission("MANAGE_PLATFORM_SETTINGS"))
                // Phase 6 (PAM): approving/rejecting/revoking PLATFORM-scope
                // privileged-access requests — its own permission, distinct
                // from both MANAGE_CATALOG and MANAGE_REGISTRATIONS, since
                // granting elevated access is a deliberately separate,
                // independently-revocable responsibility.
                .requestMatchers("/admin/privileged-access/**").access(permissions.platformPermission("MANAGE_PRIVILEGED_ACCESS"))
                // Phase 25: platform-wide audit trail — its own permission,
                // distinct from every other admin capability above (being
                // able to see what every admin action did is more sensitive
                // than performing any single one of them).
                .requestMatchers("/admin/audit-logs/**").access(permissions.platformPermission("VIEW_AUDIT_LOG"))
                // Phase 3 (2026.3.3): RBAC administration itself — two
                // separate permissions, not one, since defining what a
                // permission NAME means (MANAGE_PERMISSIONS) is a more
                // foundational capability than assigning existing
                // permissions to a role (MANAGE_ROLES).
                .requestMatchers("/admin/roles/**").access(permissions.platformPermission("MANAGE_ROLES"))
                .requestMatchers("/admin/permissions/**").access(permissions.platformPermission("MANAGE_PERMISSIONS"))
                // 08 Billing & Payments (sprint 2026.4.3, C46): its own
                // permission — refunding a payment or seeing every
                // customer's invoices is a distinct, sensitive
                // responsibility from every other admin capability above.
                .requestMatchers("/admin/billing/**").access(permissions.platformPermission("MANAGE_BILLING"))
                // Platform admin dashboard (C53): a read-only, cross-domain
                // overview — its own permission, distinct from every single-
                // domain admin capability above (see RbacSeeder's own comment).
                // REQ-INT-001/REQ-INT-002 (C61, C62): platform events and
                // API-key administration — its own permission.
                .requestMatchers("/admin/events/**", "/admin/api-keys/**").access(permissions.platformPermission("MANAGE_INTEGRATIONS"))
                // C70: search index status and rebuild, synonyms and insights.
                .requestMatchers("/admin/search/**").access(permissions.platformPermission("MANAGE_SEARCH"))
                .requestMatchers("/admin/platform-dashboard/**").access(permissions.platformPermission("VIEW_PLATFORM_DASHBOARD"))
                // Razorpay calls this directly — no Vyoog user token exists
                // on that request. Trusted only via its own signature
                // (BR-5/BR-6), verified inside PaymentService, never by a
                // JWT here.
                .requestMatchers(HttpMethod.POST, "/webhooks/razorpay").permitAll()
                // Requesting/viewing/self-revoking privileged access (either
                // scope — see PrivilegedAccessController) is deliberately
                // covered by the existing /me/** authenticated() rule below,
                // same as ORGANIZATION-scope approval is covered by the
                // existing /organization/me/** rule — no separate entry
                // needed for either.
                // An individual customer's own products/subscriptions, and an
                // organization member's own org self-service — any authenticated
                // Vyoog customer. Further scoping (ORG_ADMIN-only actions, "your own
                // organization only") happens inside the service layer, never here,
                // since that scoping depends on data (which org a caller belongs to)
                // a URL pattern can't express.
                // REQ-TEN-008 (C84): the invitation link is the credential for preview, account creation and
                // decline; accepting needs a signed-in account (the default rule below).
                .requestMatchers(HttpMethod.GET, "/invitations/*").permitAll()
                .requestMatchers(HttpMethod.POST, "/invitations/*/account", "/invitations/*/decline").permitAll()
                .requestMatchers("/me/**").authenticated()
                .requestMatchers("/organization/me/**").authenticated()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtAuthenticationConverter)));

        return http.build();
    }

    // Credentialed requests (the login flow sets/reads an HttpOnly refresh-token cookie)
    // can't use a wildcard origin — browsers reject Access-Control-Allow-Origin: * combined
    // with credentials, so app.cors-allowed-origins must list explicit origins here.
    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(corsAllowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Phase 5 (2026.3.3): registered BEFORE "/**" — Spring's
        // UrlBasedCorsConfigurationSource matches the most specific
        // registered pattern, but this is made explicit rather than relied
        // on implicitly. SAML's login-init/acs endpoints are reached by a
        // real top-level browser navigation/form-POST from a customer's own
        // external IdP domain, never by fetch/XHR from this app's own SPA —
        // CORS (a same-origin script-request concept) simply doesn't apply
        // to that. Confirmed live: without this, Spring's own CORS filter
        // rejected a real cross-origin ACS form-POST with 403 "Invalid CORS
        // request", since no real IdP's domain could ever appear in
        // app.cors-allowed-origins (that list is for THIS app's own SPA
        // origin). No credentials here either — a session is established via
        // a plain Set-Cookie response header on a normal navigation, not a
        // credentialed XHR.
        CorsConfiguration samlConfig = new CorsConfiguration();
        samlConfig.setAllowedOriginPatterns(List.of("*"));
        samlConfig.setAllowedMethods(List.of("GET", "POST"));
        samlConfig.setAllowedHeaders(List.of("*"));
        samlConfig.setAllowCredentials(false);
        source.registerCorsConfiguration("/saml/**", samlConfig);

        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
