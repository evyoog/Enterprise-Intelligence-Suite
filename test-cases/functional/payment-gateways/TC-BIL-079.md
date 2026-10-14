# TC-BIL-079: A connection test calls the real provider and shows what it answered

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-079 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.6) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-7](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 6 (Connection tests are real) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
Razorpay with a mocked client that answers 401, a network error, 400 and success in turn (automated); a real Razorpay test key pair (manual, TC-BIL-103).

## Steps
1. Test with the client answering HTTP 401.
2. Then with the provider unreachable.
3. Then with HTTP 400.
4. Then with success.
5. Test FakePay while incomplete.

## Expected Result
AUTHENTICATION_FAILED, PROVIDER_UNAVAILABLE, TEST_FAILED, CONNECTION_SUCCESSFUL; for an incomplete gateway CONFIGURATION_INCOMPLETE and no provider call. The row shows the real status and message.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `theRazorpayConnectionTestReportsWhatRazorpayActuallyAnswered`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `configuredConnectedActiveAndDefaultAreSeparateStatuses`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/gateway/RazorpayGatewayTest.java` — `statusesFromRazorpayBecomeEisStatuses`
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "shows the real outcome of a connection test and reloads the list"

The automated Razorpay cases use a mocked `RazorpayClient`; a call to the real Razorpay is manual (TC-BIL-103, Not Run).

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 14 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 44 passed). The parts listed as not covered have **not** been run.

## Status
Passed for the automated part (2026-10-14); the not-covered part is Not Run

## Linked Defect (if failed)
None.
