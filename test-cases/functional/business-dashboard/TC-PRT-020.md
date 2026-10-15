# TC-PRT-020: Product launch history is recorded and purged after 13 months

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-020 |
| Requirement ID (required) | [C104](../../../docs/01-business/roadmap/open-decisions.md#c104) AN-1 — [16 Analytics & data platform](../../../docs/01-business/roadmap/applications/16-analytics-data-platform.md) |
| Priority | P2 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A customer with an active subscription to a product.

## Steps
1. Launch the product twice; read the `product_launch_history` rows for it.
2. Backdate one history row to 400 days ago; run the cleanup job's service method.

## Expected Result
Each launch creates its own history row (customer, product, timestamp) alongside the existing `product_usage` cumulative counter (unchanged — still "last launched" and "launch count"). The cleanup run removes only the row older than 13 months; the recent row stays.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/dashboard/service/DashboardServiceTest.java` — `everyLaunchIsRecordedAsItsOwnHistoryRow`
- `backend/src/test/java/com/vyoog/eisplatform/modules/dashboard/service/DashboardServiceTest.java` — `launchHistoryOlderThan13MonthsIsPurged`

## Actual Result
The automated tests above passed on 2026-10-15.

## Status
Passed (automated).

## Linked Defect (if failed)
None.
