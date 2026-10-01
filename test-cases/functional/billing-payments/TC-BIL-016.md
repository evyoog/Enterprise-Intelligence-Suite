# TC-BIL-016: A wrong amount, a future date, a missing reference or an ONLINE-route invoice is refused

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-016 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.18–.21, decision C55) |
| Acceptance Criterion | [AC-28](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
As TC-BIL-015.

## Steps
1. Record an amount different from the total.
2. Record a future date.
3. Leave the reference empty.
4. Call the API for an ONLINE-route invoice.

## Expected Result
Each is refused with a field error (UI) or HTTP 400/409 (API); the invoice stays OPEN and no payment is created.

## Automated coverage
- `frontend/src/pages/admin/AdminBillingPage.test.tsx` — "records an offline payment after validation and a confirmation step" (validation part)
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/CheckoutOfflinePaymentTest.java` — `aWrongAmountAFutureDateOrAnOnlineRouteInvoiceIsRefused`

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
