# TC-PRT-026: Search administration and its permission

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-026 |
| Requirement ID (required) | [REQ-PRT-002](../../../docs/02-requirements/FRD/global-search/requirement.md) |
| Acceptance Criterion | [AC-11](../../../docs/02-requirements/FRD/global-search/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-002](TESTPLAN-PRT-002.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
A platform admin (ADMIN role) and a user without MANAGE_SEARCH.

## Steps
1. Call /admin/search/index, /index/rebuild and /insights without a token, with another role, and as admin.
2. Open /admin/search: Index, Synonyms and Insights tabs.

## Expected Result
401 without a token, 403 without MANAGE_SEARCH, 200 for the admin. The page shows the index state, rebuilds after confirmation, adds and deletes synonyms, and shows insights; no axe violations.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/SearchAuthorizationTest.java`
- `backend/src/test/java/com/vyoog/eisplatform/modules/authorization/service/AuthorizationServiceTest.java` — MANAGE_SEARCH seeded for ADMIN
- `frontend/src/pages/admin/AdminSearchPage.test.tsx` — 6 tests
- `frontend/src/components/layout/appNavigation.test.ts`

## Actual Result
The automated tests above passed on 2026-10-05 (PostgreSQL 16 with pg_trgm and pgvector 0.6.0; embeddings from the stub test model).

## Status
Passed (automated run 2026-10-05)

## Linked Defect (if failed)
None.
