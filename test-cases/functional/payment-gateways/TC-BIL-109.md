# TC-BIL-109: AC-39 Credentials of an added gateway are stored encrypted and never returned

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-109 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.16, .5) |
| Acceptance Criterion | [AC-39](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-3, BR-22](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 5 (Credentials are masked) |
| Decision | [C99](../../../docs/01-business/roadmap/open-decisions.md#c99) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
The test profile (valid `PAYMENT_GATEWAY_ENCRYPTION_KEY`); an administrator.

## Steps
1. Add a gateway with a non-secret field "publicKey" = "pk_acme_visible_123" and a secret field "secretKey" = "sk-super-secret-value".
2. Search the add response, the list and the details (as JSON) for both values.
3. Read the stored row: `credentials_encrypted` and `definition`.
4. Read the details: the `fields` entries for both fields.
5. Rotate the secret through `PUT …/configuration` and read the response and the stored ciphertext.

## Expected Result
Neither value appears in any response. `credentials_encrypted` is a non-empty ciphertext that contains neither value; `definition` (JSON) contains neither value. The secret field shows `configured` true, `source` STORED and `maskedValue` null; the non-secret field shows a masked form (`pk_a••••••_123`). A rotation changes the ciphertext and its response carries no value.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `anAdministratorCanAddAGatewayAndItsCredentialsAreStoredEncryptedAndNeverReturned`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `anAddedGatewayCanBeConfiguredLaterRotatedEditedAndRemoved` (the ciphertext changes on rotation; a blank keeps a stored value)
- `backend/src/test/java/com/vyoog/eisplatform/config/PaymentGatewayAuthorizationTest.java` — `addingAGatewayNeedsTheBillingPermissionStoresCredentialsAndNeverReturnsThem` (create, list and configure responses contain no value, over HTTP)
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `credentialsAreEncryptedAtRestAndNeverReturned` (the same guarantee for a configured provider)
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "adds a gateway from the form: pre-fills from a template, hides secrets, sends the definition and values once" (the secret input is a password field)

Not covered by an automated test: that no log line or audit entry receives a value (checked by code review: the audit detail names the field keys only), and a look at the stored column on a real PostgreSQL database (the tests use H2).

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
