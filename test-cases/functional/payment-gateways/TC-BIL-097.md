# TC-BIL-097: Evidence flags come from real records, not from the connection test

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-097 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.9) |
| Acceptance Criterion | [AC-27](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-16](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 6 (Connection tests are real) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Razorpay with a passing test and no payments; FakePay with a captured and refunded payment.

## Steps
1. Test Razorpay successfully; read its evidence.
2. Complete and refund a FakePay payment; read its evidence.
3. Open the details dialog.

## Expected Result
Razorpay: authenticated true, payment captured false; FakePay: payment captured true, refund made true; the dialog shows four lines each "Yes" or "Not yet" with the explanatory note.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `theRazorpayConnectionTestReportsWhatRazorpayActuallyAnswered`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `aPaymentKeepsTheProviderThatCreatedItWhateverIsDefaultLater`
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "shows capabilities, separate evidence, the webhook set-up and recent failures in the details"

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
