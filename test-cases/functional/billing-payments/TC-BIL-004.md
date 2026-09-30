# TC-BIL-004: An invalid checkout signature changes nothing

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-004 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An OPEN invoice and a created payment order.

## Steps
1. Act: confirm the payment with a signature Razorpay's verification (mocked) rejects.

## Expected Result
`InvalidPaymentSignatureException` is thrown (HTTP 400, code `SIGNATURE_INVALID`); the invoice remains OPEN.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/BillingServiceTest.java` — `anInvalidSignatureChangesNothing`

## Actual Result
The automated test passed on 2026-09-30.

## Status
Passed (automated run 2026-09-30)

## Linked Defect (if failed)
None.
