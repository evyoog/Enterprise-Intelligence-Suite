# TC-BIL-085: With no eligible gateway the payment is refused clearly and nothing is created

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-085 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.12) |
| Acceptance Criterion | [AC-15](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-10](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 10 (The backend routes each payment) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
All gateways deactivated, or a currency no active gateway takes (EUR with only FakePay active).

## Steps
1. Start a payment for EUR with only FakePay active.
2. Deactivate everything and start a payment.
3. Open the checkout in that state.

## Expected Result
`PaymentGatewayNotConfiguredException` with the message naming the currency; the checkout's online tiles are disabled with the banner and Pay by invoice stays available.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `theBackendRoutesByActivationCurrencyMethodAndDefault`
- `frontend/src/pages/CheckoutPage.test.tsx` — "disables the online tiles when the gateway is not configured but keeps Pay by invoice"

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
