# TC-BIL-115: AC-45 Only MANAGE_BILLING can add, edit, remove or read the templates of gateways

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-115 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.16, .14) |
| Acceptance Criterion | [AC-45](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-18](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | None (added by C99) |
| Decision | [C99](../../../docs/01-business/roadmap/open-decisions.md#c99) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Security |
| Automated | Partly |

## Preconditions
A token with the platform permission `MANAGE_BILLING`; a token without it; no token.

## Steps
1. Call `POST /admin/billing/gateways` with a valid body with no token, with the user token and with the administrator token.
2. Call `GET …/templates` with the user token and the administrator token.
3. Call `DELETE …/HTTPPAY` with the user token and the administrator token.
4. Call `PUT …/HTTPPAY/definition` with no token and with the user token.

## Expected Result
No token: 401. Signed in without the permission: 403 and nothing is stored. Administrator: 201 for the add, 200 for the templates, 204 for the delete, 200 for the definition edit. Only the administrator's add stores credentials.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/PaymentGatewayAuthorizationTest.java` — `addingAGatewayNeedsTheBillingPermissionStoresCredentialsAndNeverReturnsThem` (POST 401, 403, 201; templates 403 then 200; DELETE 403 then 204)
- `backend/src/test/java/com/vyoog/eisplatform/config/PaymentGatewayAuthorizationTest.java` — `everyGatewayEndpointNeedsSignInAndTheBillingPermission` (the existing gateway endpoints)

Not covered by an automated test: `PUT …/definition` without a token or without the permission, and `GET …/templates` without a token (all paths under `/admin/billing` share one rule in `SecurityConfig`, but these two calls are not exercised).

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed). The parts listed as not covered have **not** been run.

## Status
Passed for the automated part (2026-10-14); the not-covered part is Not Run

## Linked Defect (if failed)
None.
