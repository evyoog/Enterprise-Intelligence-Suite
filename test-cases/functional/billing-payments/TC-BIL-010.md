# TC-BIL-010: Paying by card opens Razorpay with the card method and shows "Payment successful"

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-010 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.18–.21, decision C55) |
| Acceptance Criterion | [AC-19](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md), [AC-20](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
As TC-BIL-009; Razorpay test credentials.

## Steps
1. Select Card, tick the terms checkbox, click **Pay {total}**.
2. Complete the payment in Razorpay Checkout.

## Expected Result
Razorpay Checkout opens with `prefill.method = card` (UPI: `upi`; Netbanking: `netbanking`; Wallets: `wallet` — C59) for the invoice total; after signature verification step 3 shows "Payment successful" with the invoice number, amount and method.

## Automated coverage
- `frontend/src/pages/CheckoutPage.test.tsx` — "pays with the default saved card; an expired card cannot be chosen"

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
