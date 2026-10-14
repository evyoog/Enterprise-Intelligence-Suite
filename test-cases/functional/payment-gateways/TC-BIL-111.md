# TC-BIL-111: AC-41 A code that is reserved, malformed, a built-in provider or already used is refused

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-111 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.16) |
| Acceptance Criterion | [AC-41](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-20](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | None (added by C99) |
| Decision | [C99](../../../docs/01-business/roadmap/open-decisions.md#c99) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Partly |

## Preconditions
Razorpay (built-in) and one added gateway with the code OKPAY.

## Steps
1. Add a gateway with the code "1bad" and another with "default".
2. Add a gateway with the code "razorpay".
3. Add OKPAY again with the same body.
4. Read the list.

## Expected Result
The malformed and the reserved codes are 400 with "The code must be 2 to 30 letters, digits or underscores, starting with a letter, and not a reserved word." The built-in code and the repeated code are 409 "A payment gateway with the code … already exists." The existing gateways are unchanged and no extra gateway is listed.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `addingAGatewayValidatesTheDefinitionAndRefusesDuplicates` ("1bad", reserved "default", a repeated code and the adapter code "razorpay")
- `backend/src/test/java/com/vyoog/eisplatform/config/PaymentGatewayAuthorizationTest.java` — `addingAGatewayNeedsTheBillingPermissionStoresCredentialsAndNeverReturnsThem` (the same body twice: 201 then 409)

Not covered by an automated test: the other reserved codes (ACTIVE, SUMMARY, TEMPLATES), a code of 1 or 31 characters, a repeated code in a different letter case, and two simultaneous adds of one code (the unique constraint answers 409). Run these manually.

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed). The parts listed as not covered have **not** been run.

## Status
Passed for the automated part (2026-10-14); the not-covered part is Not Run

## Linked Defect (if failed)
None.
