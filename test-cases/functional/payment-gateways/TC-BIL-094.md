# TC-BIL-094: Provider failures never create a wrong status

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-094 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.6, .7, .10) |
| Acceptance Criterion | [AC-24](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-7, BR-8, BR-9, BR-13](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 16 (Provider failures do not create wrong statuses) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
FakePay and Razorpay with a controllable test result.

## Steps
1. Activate while the provider answers each failing status.
2. Rotate an active gateway while it answers authentication failed.
3. Confirm a payment with a forged signature.
4. Deliver a forged webhook.
5. Test Razorpay with 401, unreachable and 400.

## Expected Result
Each failing status is stored as itself and the gateway stays inactive; the rotation is refused and unchanged; the payment stays CREATED and the invoice OPEN after forged input; Razorpay maps 401 to AUTHENTICATION_FAILED, unreachable to PROVIDER_UNAVAILABLE, 400 to TEST_FAILED.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `activationNeedsCompleteConfigurationAndAPassingConnectionTest`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `rotatingTheCredentialsOfAnActiveGatewayKeepsTheOldOnesWhenTheNewOnesFail`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `aBadSignatureOrAnInactiveRouteNeverCapturesAPayment`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `webhooksAreVerifiedPerProviderProcessedOnceAndTrackedWithoutTrustingAnUnsignedCaller`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `theRazorpayConnectionTestReportsWhatRazorpayActuallyAnswered`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/gateway/RazorpayGatewayTest.java` — `statusesFromRazorpayBecomeEisStatuses`

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 14 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
