# TC-BIL-081: Several gateways can be active at once

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-081 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.7) |
| Acceptance Criterion | [AC-11](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-8](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 8 (Several gateways can be active at once) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Razorpay active; FakePay configured and passing its test.

## Steps
1. Activate FakePay.
2. Read both rows.
3. Deactivate Razorpay.
4. Create a payment.

## Expected Result
Both rows show Active after step 2; after step 3 FakePay still takes payments (routing finds it); the summary's Active count follows.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `configuredConnectedActiveAndDefaultAreSeparateStatuses`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `theBackendRoutesByActivationCurrencyMethodAndDefault`
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "makes another active gateway the default and deactivates one"

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
