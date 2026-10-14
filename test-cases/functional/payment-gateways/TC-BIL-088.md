# TC-BIL-088: Webhook events are idempotent per provider and event id

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-088 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.10) |
| Acceptance Criterion | [AC-18](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-13](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 12 (No duplicate financial operations) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A CREATED payment; FakePay and Razorpay configured.

## Steps
1. Deliver a signed `captured` event twice.
2. Count the event rows and read the payment and invoice.
3. Deliver event id "evt-1" to Razorpay and to FakePay.
4. Deliver the same Razorpay event again.

## Expected Result
One event row, one capture, the invoice PAID once; both providers keep an "evt-1" row; the repeat is acknowledged. The database enforces uniqueness on (provider, event id).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `webhooksAreVerifiedPerProviderProcessedOnceAndTrackedWithoutTrustingAnUnsignedCaller`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `aWebhookOfAnUnknownOrPlannedProviderIsRefusedAndEventIdsAreUniquePerProviderOnly`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/BillingServiceTest.java` — `aWebhookEventIsProcessedAtMostOnce`

The concurrent duplicate (two deliveries racing, acknowledged on `DataIntegrityViolationException` in `PaymentWebhookController`) is not covered by an automated test.

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 14 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
