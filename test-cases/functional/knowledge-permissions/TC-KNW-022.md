# TC-KNW-022: No Knowledge Management entry points for readers

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-022 |
| Requirement ID (required) | [REQ-KNW-008](../../../docs/02-requirements/FRD/knowledge-permissions/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/knowledge-permissions/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P1 |
| Type | UI |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Sign in as a customer without knowledge permissions; open the app and /knowledge-management.

## Expected Result
No Knowledge Management menu item; the page says no access.

## Automated coverage
- `frontend/src/components/layout/appNavigation.test.ts` — `(customer and member navigation)`
- `frontend/src/pages/knowledge-admin/KnowledgeManagement.test.tsx` — `tells people without a knowledge permission that they have no access`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
