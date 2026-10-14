# TC-BIL-110: AC-40 Adding a gateway validates the definition and saves nothing when it is wrong

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-110 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.16) |
| Acceptance Criterion | [AC-40](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-20](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | None (added by C99) |
| Decision | [C99](../../../docs/01-business/roadmap/open-decisions.md#c99) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
An administrator.

## Steps
1. Send `POST /admin/billing/gateways` with, one at a time: a blank name; currencies ["RUPEE"]; methods ["crypto"]; an empty field list; two fields named "a" and "A"; environment "SANDBOX"; a value for a field the definition does not have; a body with no currencies, methods or fields.
2. Read the response and look for the gateway.
3. In the dialog, leave the name empty, then a credential label empty, then switch **Required** off on the only credential.

## Expected Result
Each request is 400 with a reason naming the problem ("The name is required (up to 100 characters).", "Enter at least one currency as a three-letter code, for example INR or USD.", "Choose at least one payment method from card, upi, netbanking, wallet, bank_transfer, paypal.", "Define 1 to 12 credentials, at least one of them required.", "The credential name … is used twice.", "Environment must be TEST or LIVE.", "Unknown field: …") and **nothing is saved**. In the dialog **Add gateway** stays disabled and the first problem is shown ("Enter a gateway name.", "Every credential needs a label and a name (letters and digits, starting with a letter).", "Mark at least one credential as required.").

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `addingAGatewayValidatesTheDefinitionAndRefusesDuplicates` (name, currency, method, no fields, duplicate field name, environment, unknown field; nothing saved)
- `backend/src/test/java/com/vyoog/eisplatform/config/PaymentGatewayAuthorizationTest.java` — `addingAGatewayNeedsTheBillingPermissionStoresCredentialsAndNeverReturnsThem` (a definition with no currencies, methods or fields is 400)
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "adds a gateway from scratch: derives the code and credential names and keeps the Add button off until the form is valid"

Not covered by an automated test: a name over 100 characters, a description over 300, more than 20 currencies or 50 countries, a country that is not two letters, more than 12 credentials, no credential marked required, a credential name that is not letters and digits or over 40 characters, a label over 60 characters, and a value over 500 characters or with a control character. Run these through the API manually.

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed). The parts listed as not covered have **not** been run.

## Status
Passed for the automated part (2026-10-14); the not-covered part is Not Run

## Linked Defect (if failed)
None.
