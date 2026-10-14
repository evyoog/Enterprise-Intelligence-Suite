# TC-BIL-095: Existing routes and permissions stay compatible, and only MANAGE_BILLING reaches the gateway API

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-095 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.14) |
| Acceptance Criterion | [AC-25](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-17, BR-18](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 17 (Existing routes and permissions stay compatible) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Security |
| Automated | Partly |

## Preconditions
MockMvc security context with no JWT, a plain user and an administrator.

## Steps
1. GET the list, summary and details with each caller.
2. POST test, activate and deactivate and PUT configuration as a plain user.
3. POST `/webhooks/razorpay` with no token.
4. Call the earlier Razorpay status route.

## Expected Result
401 / 403 / 200 as stated for reads; 403 for the plain user's writes; the webhook is reachable without a token and is rejected only for a bad signature; the legacy status route still answers.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/PaymentGatewayAuthorizationTest.java` — `everyGatewayEndpointNeedsSignInAndTheBillingPermission`
- `backend/src/test/java/com/vyoog/eisplatform/config/PaymentGatewayAuthorizationTest.java` — `webhooksArePublicButNeedAValidSignatureAndAKnownProvider`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/BillingServiceTest.java` — `gatewayStatusMasksTheKeyIdAndNeverReturnsSecrets`

Not covered by an automated test: the 401/403 of the `PUT …/methods` and `PUT …/default` routes, the `/gateways/active` alias and the legacy `/gateway/test` route.

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 14 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures). The parts listed as not covered have **not** been run.

## Status
Passed for the automated part (2026-10-14); the not-covered part is Not Run

## Linked Defect (if failed)
None.
