# TC-BIL-114: AC-44 A configuration-only gateway cannot be tested, activated or routed to, and is counted honestly

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-114 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.17, .3) |
| Acceptance Criterion | [AC-44](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-10, BR-12, BR-23](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 15 (Unsupported providers are never operational) |
| Decision | [C99](../../../docs/01-business/roadmap/open-decisions.md#c99) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
Razorpay active and default; an added gateway ACME that is Configured; the invoice currency INR.

## Steps
1. Call `POST /admin/billing/gateways/ACME/test` and `/activate`.
2. Call `…/ACME/deactivate`, `PUT …/ACME/methods` and `PUT /admin/billing/gateways/default` with `{"code":"ACME"}`.
3. Start a payment for an INR invoice with card.
4. Send `POST /webhooks/acme`.
5. Read the overview and ACME's card and details.

## Expected Result
Test, activate, deactivate, methods and default are each 409 "Acme has no payment integration in EIS, so it can store credentials but cannot be tested, activated or used for payments.". The payment is routed to Razorpay, never to ACME, and no payment row names ACME. The webhook is 404. ACME is never Active or Default; its connection status is Not tested with "No payment integration exists for this gateway, so it cannot be tested or activated."; its details have no Webhook tab. Registered counts ACME, Implemented and Active do not, and Need configuration counts it only while it is not Configured.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `anAdministratorCanAddAGatewayAndItsCredentialsAreStoredEncryptedAndNeverReturned` (test and activate refused with "no payment integration"; routing picks RAZORPAY; registered 3, implemented 2; actions and message)
- `backend/src/test/java/com/vyoog/eisplatform/config/PaymentGatewayAuthorizationTest.java` — `addingAGatewayNeedsTheBillingPermissionStoresCredentialsAndNeverReturnsThem` (test and activate are 409 over HTTP)
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "labels a gateway added by an administrator as having no payment integration and offers only what works" (no Test, Activate or default button, no Webhook tab, no evidence)

Not covered by an automated test: the refusal of deactivate, default and methods for an added gateway (`requireImplemented` is the same code path as test and activate), the 404 of a webhook sent to an added gateway (`PaymentService.processWebhook` looks up an adapter only), and the Need configuration count.

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed). The parts listed as not covered have **not** been run.

## Status
Passed for the automated part (2026-10-14); the not-covered part is Not Run

## Linked Defect (if failed)
None.
