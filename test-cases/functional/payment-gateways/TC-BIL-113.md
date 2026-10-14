# TC-BIL-113: AC-43 An administrator removes an added gateway and its stored credentials; a built-in provider cannot be removed

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-113 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.16) |
| Acceptance Criterion | [AC-43](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-24](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | None (added by C99) |
| Decision | [C99](../../../docs/01-business/roadmap/open-decisions.md#c99) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Partly |

## Preconditions
An added gateway ACME with stored credentials; Razorpay.

## Steps
1. Choose **Remove** on ACME and then **Cancel**.
2. Choose **Remove** again, read the dialog and confirm (or call `DELETE /admin/billing/gateways/ACME`).
3. Read the list, `GET …/ACME` and the table `payment_gateway_config`.
4. Call `DELETE /admin/billing/gateways/RAZORPAY`.
5. Add ACME again.

## Expected Result
Cancel changes nothing. The dialog is titled "Remove Acme?" and says "The gateway and its stored credentials are deleted. This cannot be undone.". After confirming: 204, the toast "Acme was removed.", ACME is gone from the list, `GET …/ACME` is 404 and its row (and ciphertext) no longer exists. Razorpay has no Remove button and the API answers 409 "Razorpay is a built-in provider; its definition cannot be changed or removed." ACME can be added again.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `anAddedGatewayCanBeConfiguredLaterRotatedEditedAndRemoved` (row deleted, detail 404, list without it)
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `theRegistryListsOnlyRealAndAdministratorAddedGatewaysNothingIsListedAsPlanned` (deleting RAZORPAY is a conflict)
- `backend/src/test/java/com/vyoog/eisplatform/config/PaymentGatewayAuthorizationTest.java` — `addingAGatewayNeedsTheBillingPermissionStoresCredentialsAndNeverReturnsThem` (DELETE: 403 without the permission, 204, then 404; RAZORPAY 409)
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "edits the definition of an added gateway without asking for credential values, and removes it after confirmation"

Not covered by an automated test: Cancel on the confirmation, adding the same code again after a removal, and the audit entry `PAYMENT_GATEWAY_REMOVED`.

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed). The parts listed as not covered have **not** been run.

## Status
Passed for the automated part (2026-10-14); the not-covered part is Not Run

## Linked Defect (if failed)
None.
