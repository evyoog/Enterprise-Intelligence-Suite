# TC-BIL-112: AC-42 Editing a definition keeps the other values, drops the values of removed fields and cannot touch a built-in provider

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-112 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.16) |
| Acceptance Criterion | [AC-42](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-21, BR-24](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | None (added by C99) |
| Decision | [C99](../../../docs/01-business/roadmap/open-decisions.md#c99) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Partly |

## Preconditions
An added gateway ACME with fields publicKey and secretKey, both with stored values.

## Steps
1. Open **Edit** on ACME; check the code, the credential names and that there are no value inputs.
2. Rename it "Acme Payments", change the currency to EUR, remove the secretKey row and add a required "apiToken" row; Save (or call `PUT …/ACME/definition`).
3. Read the details.
4. Call `PUT /admin/billing/gateways/RAZORPAY/definition`.

## Expected Result
The dialog has the code and existing credential names locked and no Value inputs. After saving: the name is "Acme Payments", the currency EUR, the publicKey is still set, secretKey is gone (its stored value was deleted), apiToken is Not set so the configuration status is Configuration incomplete; the toast says "Acme Payments was updated."; the configuration version increases. Razorpay answers 409 "Razorpay is a built-in provider; its definition cannot be changed or removed."

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `anAddedGatewayCanBeConfiguredLaterRotatedEditedAndRemoved` (name, currency, new required field makes it incomplete, kept and dropped values)
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `theRegistryListsOnlyRealAndAdministratorAddedGatewaysNothingIsListedAsPlanned` (Razorpay has no edit or remove action)
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "edits the definition of an added gateway without asking for credential values, and removes it after confirmation"

Not covered by an automated test: the 409 of `PUT …/RAZORPAY/definition`, that `configVersion` increases and that the payment-method switches are reset, and the audit entry `PAYMENT_GATEWAY_DEFINITION_CHANGED`. Check them manually.

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed). The parts listed as not covered have **not** been run.

## Status
Passed for the automated part (2026-10-14); the not-covered part is Not Run

## Linked Defect (if failed)
None.
