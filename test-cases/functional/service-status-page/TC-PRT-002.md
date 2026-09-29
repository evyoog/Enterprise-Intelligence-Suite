# TC-PRT-002: Service Status Page — AC-2

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-002 |
| Requirement ID (required) | [REQ-PRT-001](../../../docs/02-requirements/FRD/service-status-page/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/service-status-page/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-001](TESTPLAN-PRT-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
The setting is on.

## Steps
1. Arrange: the setting is on.
2. Act: a platform administrator posts an incident with title, message, start and optional end time.
3. Observe the response and the UI.

## Expected Result
Customers who purchased the product see its details, other customers do not.

## Automated coverage
- `backend/…/ServiceStatusServiceTest.java` — "everySignedInCustomerSeesEveryProductsStatusButIncidentDetailsOnlyForPurchasedOnes"
- `frontend/src/pages/ServiceStatusPage.test.tsx` — "posts an incident", "shows every product status, and incident details only for purchased products"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
