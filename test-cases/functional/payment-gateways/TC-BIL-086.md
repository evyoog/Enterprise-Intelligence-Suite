# TC-BIL-086: A payment keeps the provider that created it

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-086 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.13) |
| Acceptance Criterion | [AC-16](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-11](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 10 (The backend routes each payment); 13 (The existing Razorpay flow still works) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
FakePay active and default; a payment created through it for an OPEN invoice.

## Steps
1. Create the payment (note `provider`).
2. Make Razorpay default and deactivate FakePay.
3. Confirm the payment with the provider's signature.
4. Refund part of it.
5. Create a second payment.

## Expected Result
The payment row has provider FAKEPAY; it is confirmed and the invoice becomes PAID; the refund is issued by FakePay and Razorpay's refund is never called; the second payment is routed to Razorpay.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `aPaymentKeepsTheProviderThatCreatedItWhateverIsDefaultLater`

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 14 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
