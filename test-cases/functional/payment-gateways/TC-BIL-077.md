# TC-BIL-077: Credentials are encrypted at rest with a master key from the environment, and refused without it

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-077 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.5) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-3](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 5 (Credentials are masked) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Security |
| Automated | Partly |

## Preconditions
The test profile sets a valid key; for the refusal, a cipher built with no, malformed or short keys, and the details with `encryptionAvailable` false.

## Steps
1. Save credentials and read `credentials_encrypted` from the database.
2. Encrypt the same text twice.
3. Build the cipher with an empty, non-base64, 16-byte and 31-byte key and try to encrypt.
4. Open Configure when the server reports no key.

## Expected Result
The stored text does not contain the secret or the key ID; two ciphertexts of the same text differ; without a usable key the cipher reports unavailable and refuses to encrypt; the dialog shows the warning and Save is disabled.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `credentialsAreEncryptedAtRestAndNeverReturned`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/gateway/CredentialCipherTest.java` — `roundTripsAndNeverRepeatsACiphertext`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/gateway/CredentialCipherTest.java` — `withoutAUsableKeyNothingCanBeStored`
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "refuses to save when the server has no encryption key"

Not covered by an automated test: the server-side refusal (409 "Credentials cannot be stored because the encryption key is not configured…") of `saveConfiguration` when the key is missing (the test profile always has a key); and that no credential is ever written to a log (checked by code review: no log statement receives a value).

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed). The parts listed as not covered have **not** been run.

## Status
Passed for the automated part (2026-10-14); the not-covered part is Not Run

## Linked Defect (if failed)
None.
