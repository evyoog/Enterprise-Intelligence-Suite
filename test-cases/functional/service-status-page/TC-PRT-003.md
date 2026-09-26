# TC-PRT-003: Service Status Page — AC-3

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-003 |
| Requirement ID (required) | [REQ-PRT-001](../../../docs/02-requirements/FRD/service-status-page/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/service-status-page/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-001](TESTPLAN-PRT-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
`app.status-page.enabled` is off.

## Steps
1. Arrange: `app.status-page.enabled` is off.
2. Act: a customer opens `/status`.
3. Observe the response and the UI.

## Expected Result
No status or incidents are returned and the page says it is turned off; the admin view still works.

## Automated coverage
- `backend/…/ServiceStatusServiceTest.java` — "theSettingTurnsTheCustomerViewOffButNotTheAdminView"
- `frontend/src/pages/ServiceStatusPage.test.tsx` — "says so when the page is turned off"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
