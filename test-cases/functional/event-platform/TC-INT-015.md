# TC-INT-015: Administrators list, filter, open and retry events

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-015 |
| Requirement ID (required) | [REQ-INT-002](../../../docs/02-requirements/FRD/event-platform/requirement.md) (C62) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/event-platform/acceptance-criteria.md), [AC-8](../../../docs/02-requirements/FRD/event-platform/acceptance-criteria.md), [AC-11](../../../docs/02-requirements/FRD/event-platform/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-002](TESTPLAN-INT-002.md) |
| Priority | P1 |
| Type | Functional, Security, Accessibility |
| Automated | Yes |

## Preconditions
A platform administrator (`MANAGE_INTEGRATIONS`) and a user without it.

## Steps
1. Open Admin → Integrations → Platform events.
2. Filter by type and status; click the Delivered tile.
3. Open an event.
4. Retry a FAILED event; try to retry a PENDING one through the API.
5. Call the API as a user without the permission.

## Expected Result
Only matching events are listed, newest first, 20 per page; the dialog shows payload and receipts; retry sets PENDING with attempts 0 and is audited (`EVENT_RETRIED`); a non-FAILED retry is 409; an unknown event is 404; an unknown status filter is 400; without the permission 403. axe finds no violations.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/integration/service/OutboxDispatcherTest.java` — `anAdministratorCanRetryOnlyAFailedEvent`, `theAdminListFiltersByTypeAndStatus`
- `backend/src/test/java/com/vyoog/eisplatform/config/PlatformEventsAuthorizationTest.java`
- `frontend/src/pages/admin/AdminPlatformEventsPage.test.tsx`

## Actual Result
The automated tests passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
