# TC-PRT-004: Service Status Page — AC-4

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-004 |
| Requirement ID (required) | [REQ-PRT-001](../../../docs/02-requirements/FRD/service-status-page/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/service-status-page/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-001](TESTPLAN-PRT-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An open incident.

## Steps
1. Arrange: an open incident.
2. Act: the administrator resolves it.
3. Observe the response and the UI.

## Expected Result
It shows as resolved; an end time before the start is refused with 400 and the message is shown.

## Automated coverage
- `backend/…/ServiceStatusServiceTest.java` — "incidentsAreOpenUntilEndedAndTheEndMustNotBeBeforeTheStart"
- `frontend/src/pages/admin/AdminIncidentsPage.test.tsx` — "only offers the allowed next phases and requires a resolution summary before RESOLVED", "shows what the server refuses and keeps the dialog open" (since 2026-10-14 ([C101](../../../docs/01-business/roadmap/open-decisions.md#c101)) incidents are resolved on the Incidents tab; the earlier case "resolves an open incident and shows backend refusals" of `ServiceStatusPage.test.tsx` was removed with the Manage-tab incident form)

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
