package com.vyoog.eisplatform.modules.authorization.service;

import com.vyoog.eisplatform.modules.authorization.model.OrganizationOwnedResource;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;

/**
 * Phase 5's "policy-based authorization" — a single named, composable rule
 * from (caller, resource) to allow/deny. {@link AuthorizationService}
 * exposes the two standard policies (permission, ownership) as plain
 * methods; {@link #and} composes them into one decision, replacing what used
 * to be ad-hoc sequential if-statements interleaved inside a service method
 * (compare {@code OrganizationSelfService}'s Phase 2/3 history) with an
 * explicit, reusable rule pipeline instead.
 */
@FunctionalInterface
public interface AccessPolicy {

    boolean check(OrganizationMember caller, OrganizationOwnedResource resource);

    default AccessPolicy and(AccessPolicy other) {
        return (caller, resource) -> this.check(caller, resource) && other.check(caller, resource);
    }
}
