# TC-BIL-006: A webhook event is processed at most once, and an invalid webhook signature is rejected

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-006 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md), [AC-8](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
None (webhook signature verification mocked).

## Steps
1. Act: deliver the same `payment.captured` webhook body (same event id) twice, with a valid signature.
2. Observe how many `payment_webhook_event` rows exist for that event id.
3. Act: deliver a webhook with an invalid signature.

## Expected Result
Step 2: exactly one row (the second delivery is acknowledged and ignored — BR-6). Step 3: `InvalidPaymentSignatureException` is thrown; nothing is recorded.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/BillingServiceTest.java` — `aWebhookEventIsProcessedAtMostOnce`, `aWebhookWithAnInvalidSignatureIsRejected`

## Actual Result
The automated tests passed on 2026-09-30.

## Status
Passed (automated run 2026-09-30)

## Linked Defect (if failed)
None.
