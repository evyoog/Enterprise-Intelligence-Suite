# TC-BIL-076: Credentials are write-only and masked

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-076 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.4) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-4](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 5 (Credentials are masked) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
FakePay configured with the secret "top-secret-value"; Razorpay credentials from the test environment.

## Steps
1. Read the details as JSON and search for the secret values.
2. Read a secret field and an identifier field.
3. Open Configure on screen.
4. Save with a blank secret field.

## Expected Result
No secret value is in the JSON or the page; the secret field has `maskedValue` null and source STORED or ENVIRONMENT; the identifier is masked; Configure shows empty password inputs with "saved — leave blank to keep"; the blank field keeps its stored value.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `credentialsAreEncryptedAtRestAndNeverReturned`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `aBlankFieldKeepsTheStoredValueAndBadInputIsRefused`
- `backend/src/test/java/com/vyoog/eisplatform/config/PaymentGatewayAuthorizationTest.java` — `theDetailShowsWhetherEachCredentialIsSetAndNeverItsValue`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/gateway/RazorpayGatewayTest.java` — `theKeyAndTheEnvironmentMustAgree`
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "builds the configuration form from the provider fields, keeps secrets write-only and sends only what was typed"

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 14 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 44 passed).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
