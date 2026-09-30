# TC-BIL-003: A verified, captured payment marks the invoice paid

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-003 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An OPEN invoice; the payment gateway is configured (test credentials).

## Steps
1. Act: create a Razorpay order for the invoice (Razorpay mocked to return an order id).
2. Act: confirm the payment with a signature the mock accepts, and Razorpay reporting the payment as "captured".
3. Observe the payment and the invoice.

## Expected Result
The payment is CAPTURED (method/network/last 4 recorded); the invoice is PAID.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/BillingServiceTest.java` — `payingAnInvoiceWithAVerifiedCapturedPaymentMarksItPaid`

## Actual Result
The automated test passed on 2026-09-30.

## Status
Passed (automated run 2026-09-30)

## Linked Defect (if failed)
None.
