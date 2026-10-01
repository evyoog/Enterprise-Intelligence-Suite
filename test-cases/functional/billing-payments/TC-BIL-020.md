# TC-BIL-020: Netbanking and Wallets open Razorpay with that method preselected

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-020 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.18, .22, .23; C59) |
| Acceptance Criterion | [AC-32](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Gateway configured.

## Steps
1. Select Netbanking, tick the terms checkbox, click Pay.

## Expected Result
`POST …/invoices/{id}/payments` is called with `method: netbanking` and Razorpay Checkout opens with `prefill.method = netbanking` (wallets: `wallet`).

## Automated coverage
- `frontend/src/pages/CheckoutPage.test.tsx` — "pays by netbanking with that method preselected in Razorpay"

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
