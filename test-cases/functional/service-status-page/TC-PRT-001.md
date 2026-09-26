# TC-PRT-001: Service Status Page — AC-1

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-001 |
| Requirement ID (required) | [REQ-PRT-001](../../../docs/02-requirements/FRD/service-status-page/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/service-status-page/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-001](TESTPLAN-PRT-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
The setting is on.

## Steps
1. Arrange: the setting is on.
2. Act: a platform administrator posts a product status with a note.
3. Observe the response and the UI.

## Expected Result
Every signed-in customer sees it on `/status`, and a product with no posted status shows Operational.

## Automated coverage
- `backend/…/servicestatus/service/ServiceStatusServiceTest.java` — "everySignedInCustomerSeesEveryProductsStatusButIncidentDetailsOnlyForPurchasedOnes", "aProductWithNoPostedStatusIsOperationalAndInactiveProductsAreNotListed"
- `frontend/src/pages/ServiceStatusPage.test.tsx` — "posts a new status with a note for one product", "shows every product status, and incident details only for purchased products"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
