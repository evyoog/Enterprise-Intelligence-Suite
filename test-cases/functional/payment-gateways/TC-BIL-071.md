# TC-BIL-071: The page lists every registered provider with an overview and only valid actions

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-071 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.1, .2) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-1, BR-2, BR-23](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 1 (The page shows the registered providers) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98), amended by [C99](../../../docs/01-business/roadmap/open-decisions.md#c99) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A backend whose registry has Razorpay (implemented) and one gateway an administrator added (configuration only); no other gateway; an administrator with `MANAGE_BILLING`.

## Steps
1. Call `GET /admin/billing/gateways` and `/summary`.
2. Open `/admin/billing/payment-gateway`.
3. Read the overview, the rows and the buttons; look for Stripe, PayPal, PayU, Cashfree and a "Planned integrations" section.

## Expected Result
The API lists Razorpay as IMPLEMENTED and the added gateway as CONFIGURATION_ONLY (`custom` true); the summary counts every gateway as registered and only Razorpay as implemented. The screen shows the four statistics, an **Add payment gateway** button and a row per gateway (the added one labelled "No payment integration"); each row shows only the buttons its `actions` allow (Razorpay has no Edit or Remove). **Stripe, PayPal, PayU and Cashfree are not listed and there is no "Planned integrations" section** ([C99](../../../docs/01-business/roadmap/open-decisions.md#c99) removed the Planned concept).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `theRegistryListsOnlyRealAndAdministratorAddedGatewaysNothingIsListedAsPlanned`
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "shows the overview from the backend and each provider with only the actions valid for its state, with no axe violations"
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "labels a gateway added by an administrator as having no payment integration and offers only what works" (also asserts that no "Planned integrations" text exists)

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
