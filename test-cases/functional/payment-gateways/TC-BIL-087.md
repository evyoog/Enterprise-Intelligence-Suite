# TC-BIL-087: Webhooks are accepted only with a valid provider signature

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-087 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.10) |
| Acceptance Criterion | [AC-17](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-12](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 11 (Webhooks are signature-verified) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
FakePay and Razorpay configured; a CREATED payment.

## Steps
1. Post a webhook with a wrong signature, with none, and with an unparsable body.
2. Read the payment.
3. Post it correctly signed.
4. Post to `/webhooks/stripe` and `/webhooks/razorpay` with and without a valid signature over HTTP.
5. Confirm a payment with a forged checkout signature.

## Expected Result
400 for each bad delivery and the payment stays CREATED; 200 for the signed one and the payment is CAPTURED, the invoice PAID; 404 for Stripe (a code that is neither a built-in provider nor an added gateway; a gateway an administrator added has no webhook either, so it is 404 too); the forged confirmation is refused and the invoice stays OPEN.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `webhooksAreVerifiedPerProviderProcessedOnceAndTrackedWithoutTrustingAnUnsignedCaller`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `aBadSignatureOrAnInactiveRouteNeverCapturesAPayment`
- `backend/src/test/java/com/vyoog/eisplatform/config/PaymentGatewayAuthorizationTest.java` — `webhooksArePublicButNeedAValidSignatureAndAKnownProvider`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/gateway/RazorpayGatewayTest.java` — `webhookSignaturesAreReadFromRazorpaysHeaderWhateverItsCase`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/BillingServiceTest.java` — `aWebhookWithAnInvalidSignatureIsRejected`

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
