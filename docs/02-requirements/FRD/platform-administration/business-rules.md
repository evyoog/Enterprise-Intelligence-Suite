# Business rules — Platform Administration

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-GOV-001 | `platform_currency` is seeded once, idempotently, from the fixed `Currency` enum (USD/EUR/GBP/INR) on every backend startup — never created or deleted by an admin, only enabled/disabled. | backend | REQ-GOV-001.1 |
| BR-GOV-002 | A region code is unique; creating one with a code that already exists is refused (409-style `DuplicateResourceException`). | backend | REQ-GOV-001.2 |
| BR-GOV-003 | A region referenced by any organization's `regionId` cannot be deleted — refused with the same "reasons" pattern as `CatalogProductUsageGuard`. | backend | REQ-GOV-001.2 |
| BR-GOV-004 | A feature-flag key is unique; creating one that already exists is refused. Deleting a flag removes the row entirely — a later `isEnabled` check on that key then fails open (BR-GOV-005). | backend | REQ-GOV-001.3 |
| BR-GOV-005 | `PlatformFeatureFlagService#isEnabled` returns `true` for a key with no row — an unconfigured or deleted flag never silently disables a feature nobody set up a flag for. | backend | REQ-GOV-001.4 |
| BR-GOV-006 | Every Groups action (`listMyOrgGroups`, `createGroup`, `deleteGroup`, `addGroupMember`, `removeGroupMember`) checks "groups_enabled" before the `MANAGE_USERS` permission check, and throws the same generic `ForbiddenException` either way — a disabled flag is indistinguishable, from the outside, from lacking the permission. | backend | REQ-GOV-001.5 |
| BR-GOV-007 | The supported-languages list is a fixed, hand-written pair (en, es) mirroring the frontend's own `SUPPORTED_LANGUAGES` — there is no database table backing it. | backend | REQ-GOV-001.6 |

Rules shared with other features belong in `docs/03-business-rules/` and are referenced here by ID.
