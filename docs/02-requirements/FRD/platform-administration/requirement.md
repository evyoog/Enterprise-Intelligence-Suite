# REQ-GOV-001 — Platform Administration

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-27 ([C36](../../../01-business/roadmap/open-decisions.md#c36))

| Field | Value |
|---|---|
| Sprint | [2026.4.2](../../../01-business/roadmap/sprints/SPRINT-2026.4.2.md) |
| Requirement ID | REQ-GOV-001 |
| Application | [15 Administration & Governance](../../../01-business/roadmap/applications/15-administration-governance.md) |
| Application code | `APP-GOV` ([DN-5](../../../01-business/roadmap/open-decisions.md#dn-5-application-codes)) |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Application page |
|---|---|---|
| 15.01.01 | Configure platform (currencies, feature flags) | [15 Administration & Governance](../../../01-business/roadmap/applications/15-administration-governance.md#1501-platform-administration) |
| 15.01.01 | Configure languages (read-only) | same |
| 15.01.02 | Configure regions (Global Settings) | same |

15.01.01 Configure defaults and 15.01.02 Manage templates are **not** in this FRD — carried forward ([C36](../../../01-business/roadmap/open-decisions.md#c36)).

## Summary
A platform administrator manages three platform-wide settings on the admin Common settings page: which currencies are offered on the plan editor, a free-form list of regions organizations can be assigned to, and runtime feature flags that gate whether a built feature is currently switched on. Supported languages are shown for reference only; adding one requires real translated strings, not a setting.

## Actors
- Platform administrator (holding `MANAGE_PLATFORM_SETTINGS`)

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-GOV-001.1 | An admin can view every currency and toggle it enabled/disabled. Disabling a currency only removes it from the plan editor's choices; it does not alter any existing plan. | P0 |
| REQ-GOV-001.2 | An admin can create, rename, enable/disable and delete a region. A region assigned to an organization cannot be deleted. | P0 |
| REQ-GOV-001.3 | An admin can create, update (enabled/description) and delete a feature flag. | P0 |
| REQ-GOV-001.4 | Any module can check whether a named feature flag is enabled; an unknown key is treated as enabled. | P0 |
| REQ-GOV-001.5 | The "groups_enabled" flag gates every 05.04.01 Groups action; disabling it makes Groups behave, for every organization, exactly as it does when the caller lacks the underlying permission. | P0 |
| REQ-GOV-001.6 | An admin can view the platform's supported languages (read-only). | P0 |

## Out of scope
- Currency conversion, multi-currency billing, or any actual charge being affected by a currency's enabled state (C36)
- A fixed or admin-editable list of regions with real geography — regions are freely admin-defined (C36)
- Adding or removing a language (needs real translated strings; out of scope) (C36)
- 15.01.02 Configure defaults, Manage templates (carried forward)

## Dependencies
- New module `modules/administration`: `PlatformCurrency`, `PlatformRegion`, `PlatformFeatureFlag`, `PlatformAdministrationService`, `PlatformFeatureFlagService`.
- New tables `platform_currency`, `platform_region`, `platform_feature_flag` ([V007](../../../../database/migrations/V007__platform_administration_tenant_lifecycle.sql)).
- New permission `MANAGE_PLATFORM_SETTINGS`, seeded for ADMIN.
- Consumed by [tenant-lifecycle](../tenant-lifecycle/requirement.md) (region assignment) and by 05.04.01 Groups (the "groups_enabled" flag).
