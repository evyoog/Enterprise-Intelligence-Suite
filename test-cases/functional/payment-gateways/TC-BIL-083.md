# TC-BIL-083: Changing the credentials of an active gateway tests first and keeps the old ones on failure

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-083 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.8) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-9](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 7 (Activation is blocked while configuration is incomplete); 16 (Provider failures do not create wrong statuses) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
FakePay active with credentials "original".

## Steps
1. Make the provider answer authentication failed and save a new secret.
2. Read the details.
3. Make it answer success and save the new secret again.

## Expected Result
Step 1: 409 "Not saved: …"; step 2: same `configVersion`, still active, old credentials in force; step 3: saved, version increased, connection successful, history shows the change. On screen the dialog stays open with the message.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `rotatingTheCredentialsOfAnActiveGatewayKeepsTheOldOnesWhenTheNewOnesFail`
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "shows a refused save and says when the server cannot store credentials"
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "builds the configuration form from the provider fields, keeps secrets write-only and sends only what was typed"

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 14 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 44 passed).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
