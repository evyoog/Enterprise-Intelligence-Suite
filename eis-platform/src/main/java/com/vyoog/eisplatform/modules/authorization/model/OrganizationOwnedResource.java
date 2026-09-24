package com.vyoog.eisplatform.modules.authorization.model;

/**
 * Phase 5: the one contract a resource needs to satisfy to be checkable by
 * {@code AuthorizationService}'s generic resource-based rules — "which
 * organization does this belong to," nothing else. Deliberately independent
 * of any specific entity (eis-platform's own {@code OrganizationMember}
 * implements it, see that class), so the same check works for any future
 * tenant-owned resource in this app without a new bespoke method, and is the
 * shape another Vyoog application (PMS's own Project, Ticketing's own
 * Ticket, Requirements' own Requirement) could implement on its own entities
 * if this authorization approach is ever adopted there — see this phase's
 * own report for why that adoption isn't done here, in those other apps'
 * separate codebases, as part of this roadmap.
 */
public interface OrganizationOwnedResource {
    Long organizationId();
}
