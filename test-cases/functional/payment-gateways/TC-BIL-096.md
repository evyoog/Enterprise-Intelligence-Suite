# TC-BIL-096: Every administrator change is audited without values

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-096 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.15) |
| Acceptance Criterion | [AC-26](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-19](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 5 (Credentials are masked) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P1 |
| Type | Security |
| Automated | Partly |

## Preconditions
FakePay; an administrator with a Keycloak subject.

## Steps
1. Configure with a secret, activate.
2. Read the change history in the details.
3. Open the Activity tab.
4. Inspect the audit rows.

## Expected Result
History contains PAYMENT_GATEWAY_CONFIGURED and PAYMENT_GATEWAY_ACTIVATED with the actor; the detail text lists field names only; the Activity tab shows "Configuration saved · {actor}"; no secret appears anywhere in the details.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `rotatingTheCredentialsOfAnActiveGatewayKeepsTheOldOnesWhenTheNewOnesFail`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `credentialsAreEncryptedAtRestAndNeverReturned`
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "shows capabilities, separate evidence, the webhook set-up and recent failures in the details"

Not covered by an automated test: the audit entries for test, deactivate, default, methods and a rejected rotation, the C99 entries `PAYMENT_GATEWAY_ADDED` (names the credential fields stored), `PAYMENT_GATEWAY_DEFINITION_CHANGED` and `PAYMENT_GATEWAY_REMOVED`, and a direct check of the audit rows for values. Verify those manually in the audit log.

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed). The parts listed as not covered have **not** been run.

## Status
Passed for the automated part (2026-10-14); the not-covered part is Not Run

## Linked Defect (if failed)
None.
