# TC-PRT-016: Business dashboard — Analytics and table interactions

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-016 |
| Requirement ID (required) | [C69](../../../docs/01-business/roadmap/open-decisions.md#c69) — [business dashboard screen](../../../docs/05-ui/screen-requirements/business-dashboard.md) |
| Test Plan | [TESTPLAN-PRT-003](TESTPLAN-PRT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Two applications, one with an active subscription.

## Steps
1. Click a bar; click "Not subscribed / inactive" twice; choose an application in the filter; Clear filters; sort by Application.

## Expected Result
The bar opens /products/1; the donut filters and unfilters the table; the application filter narrows it and Clear filters restores it; sorting reverses the order.

## Automated coverage
- `frontend/src/pages/BusinessDashboardPage.test.tsx` — `a bar opens the application details; the donut and the filters narrow the table`
- `frontend/src/pages/BusinessDashboardPage.test.tsx` — `sorts the table by a clicked column`

## Actual Result
The automated tests above passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
