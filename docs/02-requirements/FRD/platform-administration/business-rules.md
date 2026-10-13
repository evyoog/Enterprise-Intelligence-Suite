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
| BR-GOV-008 | The built-in text of every message type (English and Spanish) lives in code and is what EIS sent before templates existed. An edit is stored per type and language and **replaces** the built-in text for that language only; *Reset* deletes the stored row. | backend | REQ-GOV-001.7 |
| BR-GOV-009 | Template text is saved only if every `{placeholder}` is one the type offers and every required placeholder appears; subject 1–200 and body 1–5000 characters; the language must be a supported one. A preview fills the placeholders with example values and saves nothing. | backend | REQ-GOV-001.8 |
| BR-GOV-010 | Rendering order for a message: stored text in the recipient's language, built-in text in that language, stored English text, built-in English text. Values inserted into placeholders are never interpreted as placeholders. | backend | REQ-GOV-001.9 |
| BR-GOV-011 | The language of a message is the recipient's saved language, else the platform default language, else English. A recipient with no account (for example an invitee) gets the platform default language, else English. | backend | REQ-GOV-001.9 |
| BR-GOV-012 | Platform defaults start empty (no behaviour change until set). Blank text clears a default. Language must be supported; region a real locale tag; currency an existing, enabled currency; time zone a real IANA zone; date format one of auto, DD/MM/YYYY, MM/DD/YYYY, YYYY-MM-DD; reminder days 1 to 30 (stored as the renewal-reminder default, left unchanged when omitted). | backend | REQ-GOV-001.10 |
| BR-GOV-013 | A user's own saved choice always wins over a default. The currency default is stored and exposed but not applied to plans. | backend, frontend (`PreferenceSync`) | REQ-GOV-001.11 |

Rules shared with other features belong in `docs/03-business-rules/` and are referenced here by ID.
