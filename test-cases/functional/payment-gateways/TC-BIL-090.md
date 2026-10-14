# TC-BIL-090: A deactivated but configured provider still has its webhooks accepted

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-090 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.10, .13) |
| Acceptance Criterion | [AC-20](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-13](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 16 (Provider failures do not create wrong statuses) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Partly |

## Preconditions
A payment created through FakePay; FakePay deactivated afterwards (still configured).

## Steps
1. Deactivate the gateway.
2. Confirm the payment (automated).
3. Deliver the signed `payment.captured` webhook for the payment (manual or a new test).
4. Refund (automated).

## Expected Result
Confirm and refund succeed through the deactivated provider; the webhook returns 200 and the payment is CAPTURED.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `aPaymentKeepsTheProviderThatCreatedItWhateverIsDefaultLater`

Not covered by an automated test: a webhook delivered while the provider is deactivated (the behaviour follows from `configured()` ignoring activation and was verified by code reading only). Manual step 3 is Not Run.

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 14 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures). The parts listed as not covered have **not** been run.

## Status
Passed for the automated part (2026-10-14); the not-covered part is Not Run

## Linked Defect (if failed)
None.
