# TC-TEN-089: Order reminder fires once after 3 days and never auto-decides

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-089 |
| Requirement ID (required) | [REQ-TEN-010](../../../docs/02-requirements/FRD/application-access/requirement.md) (.10, BR-AAC-012); also [REQ-ORD-001](../../../docs/02-requirements/FRD/order-lifecycle/requirement.md) BR-ORD-012 |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/application-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-010](TESTPLAN-TEN-010.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`OrderServiceTest.aRequestPendingThreeDaysIsRemindedOnceAndNeverAutoDecided`) |

## Preconditions
An organization with an admin and a member; `app.orders.job-enabled=false` (the test drives `OrderService.remindPendingOrders()` directly rather than waiting for the scheduled job).

## Steps
1. The member submits an order (`SubmitOrderRequest`); call `remindPendingOrders()` immediately.
2. Backdate the order's `created_at` by 4 days; call `remindPendingOrders()`.
3. Call `remindPendingOrders()` again without any further change.
4. Submit and approve a second order, then backdate it by 4 days and call `remindPendingOrders()`.

## Expected Result
Step 1: `remindPendingOrders()` returns 0 — not due yet. Step 2: returns 1; the order's `remindedAt` is set and its status is still SUBMITTED (unchanged). Step 3: returns 0 — the same order is never reminded twice. Step 4: returns 0 — a decided (APPROVED) order is never reminded even once it is 4 days old. At no point does the reminder change an order's status; only `approveOrder`/`rejectOrder` do that.

## Status
Passed (automated, 2026-10-15).

## Linked Defect (if failed)
None.
