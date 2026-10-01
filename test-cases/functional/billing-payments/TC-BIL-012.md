# TC-BIL-012: A failed or cancelled online payment leaves the invoice open and offers Try again / Choose another method

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-012 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.18–.21, decision C55) |
| Acceptance Criterion | [AC-23](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
As TC-BIL-009.

## Steps
1. Select UPI, tick the terms checkbox, click Pay.
2. Close the Razorpay window without paying.

## Expected Result
Step 3 shows "Payment failed", "Payment was cancelled." and "Your invoice is still open. No money has been taken."; **Try again** and **Choose another method** are shown; no payment is confirmed.

## Automated coverage
- `frontend/src/pages/CheckoutPage.test.tsx` — "shows a failed result with retry options when the customer closes Razorpay"

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
