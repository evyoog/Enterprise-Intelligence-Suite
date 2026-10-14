# TC-BIL-100: The checkout shows the provider the backend chose, and an unsupported provider charges nothing

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-100 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.12, .13) |
| Acceptance Criterion | [AC-30](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-10](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 2 (The page is not Razorpay-only); 10 (The backend routes each payment) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A checkout summary naming STRIPE, and a start-payment answer with provider STRIPE. (Mocked: the test stands for a provider that has a working adapter but no launcher in the checkout screen. Since [C99](../../../docs/01-business/roadmap/open-decisions.md#c99) a gateway an administrator added is never routed to, so today this can only happen once a developer has added an adapter before its launcher.)

## Steps
1. Open the Payment step.
2. Read the "Powered by" footer.
3. Choose a method, agree to the terms and press Pay.

## Expected Result
The footer says "Stripe"; after Pay the failed state shows the message above; Razorpay Checkout is not opened and confirm is not called. For a Razorpay payment the response `provider` is RAZORPAY and the existing Razorpay flow runs.

## Automated coverage
- `frontend/src/pages/CheckoutPage.test.tsx` — "does not open a payment window for a gateway this screen has no launcher for (C98)"
- `frontend/src/pages/CheckoutPage.test.tsx` — "names the active gateway in the footer when it is not Razorpay (C97 BI-8)"
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `aPaymentKeepsTheProviderThatCreatedItWhateverIsDefaultLater`

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
