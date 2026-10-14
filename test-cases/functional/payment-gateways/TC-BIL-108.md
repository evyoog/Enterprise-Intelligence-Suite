# TC-BIL-108: AC-38 An administrator adds a gateway from a template (Stripe) with credential values

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-108 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.16) |
| Acceptance Criterion | [AC-38](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-20, BR-22, BR-25](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 14 (New providers need no UI rewrite) |
| Decision | [C99](../../../docs/01-business/roadmap/open-decisions.md#c99) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An administrator with `MANAGE_BILLING`; no gateway STRIPE yet; `PAYMENT_GATEWAY_ENCRYPTION_KEY` set.

## Steps
1. Call `GET /admin/billing/gateways/templates` and read the codes.
2. Open **Add payment gateway**, choose **Stripe** in **Start from**.
3. Read the pre-filled fields, then type values for the publishable key, the secret key and the webhook signing secret.
4. Choose **Add gateway**.
5. Call the templates again and read the list.

## Expected Result
The templates list has STRIPE, PAYPAL, PAYU and CASHFREE and no RAZORPAY. After choosing Stripe the name is Stripe, the code STRIPE, the currencies USD, EUR, GBP, INR, and three credential rows are filled in (Publishable key, Secret key and Webhook signing secret) with the secret fields shown as password inputs and no value filled in; anything can be changed. After Add, STRIPE is a card labelled "No payment integration" with configuration status Configured, and Stripe no longer appears in the templates. Before the add, Stripe was never in the list.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `templatesPrefillTheFormButAreNotGatewaysAndDisappearOnceAdded`
- `backend/src/test/java/com/vyoog/eisplatform/config/PaymentGatewayAuthorizationTest.java` — `addingAGatewayNeedsTheBillingPermissionStoresCredentialsAndNeverReturnsThem` (the templates endpoint answers 200 with `STRIPE` named "Stripe")
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "adds a gateway from the form: pre-fills from a template, hides secrets, sends the definition and values once"

Not covered by an automated test: adding every required Stripe field and reading Configured (the service test supplies only the secret key); the PayPal, PayU and Cashfree templates (only their codes are asserted); and that the Spanish labels of the dialog read correctly.

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
