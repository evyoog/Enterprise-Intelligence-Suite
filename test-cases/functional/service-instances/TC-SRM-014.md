# TC-SRM-014: Regression: the status page behaves as before when no instance reports

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SRM-014 |
| Requirement ID (required) | [REQ-SRM-001](../../../docs/02-requirements/FRD/service-instances/requirement.md) (C101) |
| Acceptance Criterion | [AC-14](../../../docs/02-requirements/FRD/service-instances/acceptance-criteria.md) |
| Business rule(s) | [BR-SRM-010, BR-PRT-001.2](../../../docs/02-requirements/FRD/service-instances/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SRM-001](TESTPLAN-SRM-001.md) |
| Priority | P0 |
| Type | Regression |
| Automated | Yes |

## Preconditions
The existing status-page tests of [TESTPLAN-PRT-001](../service-status-page/TESTPLAN-PRT-001.md) and a product with no instances.

## Steps
1. Run `ServiceStatusServiceTest`, `ServiceStatusAuthorizationTest` and `ServiceStatusPage.test.tsx` unchanged.
2. Read the status of a product with no instance.

## Expected Result
Every existing case still passes: a product with no posted status is Operational, a posted status is shown, incidents are shown only for purchased products, the setting turns the customer view off, changes are audited. The new `source` is MANUAL for a product without instances.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/servicestatus/service/ServiceStatusServiceTest.java` — `everySignedInCustomerSeesEveryProductsStatusButIncidentDetailsOnlyForPurchasedOnes`, `aProductWithNoPostedStatusIsOperationalAndInactiveProductsAreNotListed`, `incidentsAreOpenUntilEndedAndTheEndMustNotBeBeforeTheStart`, `statusChangesAreAuditedAndShowOnThePurchasersBusinessDashboard`, `theSettingTurnsTheCustomerViewOffButNotTheAdminView`
- `backend/src/test/java/com/vyoog/eisplatform/config/ServiceStatusAuthorizationTest.java` — `theCustomerViewNeedsASignedInUser`, "onlyAPlatformAdminCanPostStatus"
- `frontend/src/pages/ServiceStatusPage.test.tsx` — "shows every product status, and incident details only for purchased products", "says so when the page is turned off", "posts a new status with a note for one product"

Regression note: the frontend tests "posts an incident" and "resolves an open incident and shows backend refusals" of the earlier `ServiceStatusPage.test.tsx` were removed because the Manage tab no longer posts incidents; their cases now live in the Incidents tab tests (see TC-SUP-032, TC-PRT-002, TC-PRT-004).

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
