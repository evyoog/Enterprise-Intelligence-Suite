# TC-PRT-006: Service Status Page — AC-6

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-006 |
| Requirement ID (required) | [REQ-PRT-001](../../../docs/02-requirements/FRD/service-status-page/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/service-status-page/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-001](TESTPLAN-PRT-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A purchased product in a major outage.

## Steps
1. Arrange: a purchased product in a major outage.
2. Act: its organization admin opens the business dashboard.
3. Observe the response and the UI.

## Expected Result
An error alert names the product, and it disappears when the product is Operational again; every change is audited.

## Automated coverage
- `backend/…/ServiceStatusServiceTest.java` — "statusChangesAreAuditedAndShowOnThePurchasersBusinessDashboard"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
