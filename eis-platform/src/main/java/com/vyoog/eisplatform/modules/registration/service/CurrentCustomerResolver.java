package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resolves the authenticated caller's own {@link Customer} row from their
 * JWT's {@code sub} claim — the ONLY way any /me/** or /organization/me/**
 * endpoint identifies "who is calling", never a client-supplied id. A caller
 * whose token has no linked customer row (e.g. the platform admin's own
 * Keycloak account, which was never created through this registration flow)
 * gets a clean 404 here rather than a crash.
 */
@Component
@RequiredArgsConstructor
public class CurrentCustomerResolver {

    private final CustomerRepository customerRepository;

    public Customer resolve(Jwt jwt) {
        return resolveByKeycloakSub(jwt.getSubject())
            .orElseThrow(() -> new ResourceNotFoundException("No Vyoog account is linked to this login"));
    }

    /** Same lookup as {@link #resolve}, but for callers where "no linked
     * Customer row" is an expected, normal case rather than an error — e.g. a
     * platform admin's Keycloak account, which was never created through this
     * app's own registration flow (see this class's own javadoc). */
    public Optional<Customer> resolveOptional(Jwt jwt) {
        return resolveByKeycloakSub(jwt.getSubject());
    }

    /** Same lookup, for the handful of call sites (e.g. AuthController's
     * login/logout, which run before any JWT-authenticated Spring Security
     * context exists) that only have the raw {@code sub} string in hand —
     * keeps every CustomerRepository call inside this one service, per the
     * layered-architecture rule (controllers never touch a repository directly). */
    public Optional<Customer> resolveByKeycloakSub(String keycloakSub) {
        return customerRepository.findByKeycloakSub(keycloakSub);
    }
}
