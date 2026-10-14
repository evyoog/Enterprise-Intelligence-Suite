# TC-BIL-091: Payment methods are switched per gateway and unsupported ones are never offered

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-091 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.11) |
| Acceptance Criterion | [AC-21](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-15](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 10 (The backend routes each payment) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Razorpay active.

## Steps
1. Switch Razorpay to card and UPI only.
2. Read the methods offered for INR.
3. Try a method Razorpay does not support and an empty list.
4. Restore all methods.
5. In the details dialog switch Wallets off.

## Expected Result
Card and UPI offered, wallet and netbanking not; the unsupported and the empty requests are 400; restored; the screen sends `setMethods` with the remaining methods and the last method cannot be switched off.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `paymentMethodsCanBeSwitchedOffButOnlyOnesTheProviderSupports`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `theBackendRoutesByActivationCurrencyMethodAndDefault`
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "shows capabilities, separate evidence, the webhook set-up and recent failures in the details"

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
