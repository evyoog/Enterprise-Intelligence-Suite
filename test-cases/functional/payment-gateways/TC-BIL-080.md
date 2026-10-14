# TC-BIL-080: Activation is blocked while the configuration is incomplete or the test fails

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-080 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.7) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-8](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 7 (Activation is blocked while configuration is incomplete); 16 (Provider failures do not create wrong statuses) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
FakePay unconfigured, then configured with its test result set to each failing status in turn.

## Steps
1. Activate FakePay before it is configured.
2. Configure it and activate with the provider answering authentication failed, unavailable, unsupported configuration and test failed.
3. Let the test pass and activate.
4. Deactivate it.

## Expected Result
409 "… is not fully configured yet."; for each failing status 409 and the gateway stays inactive with that status stored; with a passing test it becomes active; deactivate is always allowed.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `activationNeedsCompleteConfigurationAndAPassingConnectionTest`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `configuredConnectedActiveAndDefaultAreSeparateStatuses`
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "shows the server message when activation is refused"
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "shows the overview from the backend and each provider with only the actions valid for its state, with no axe violations"

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
