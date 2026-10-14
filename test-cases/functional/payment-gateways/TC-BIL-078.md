# TC-BIL-078: Configured, Connected, Active and Default are four separate statuses

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-078 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.7) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-6](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 3 (Each gateway is configured independently); 9 (A default gateway can be selected) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
FakePay not configured; Razorpay active and default.

## Steps
1. Read FakePay's row.
2. Save one of three required fields.
3. Save all three.
4. Test it.
5. Activate it, then make it default.
6. Change one credential.

## Expected Result
NOT_CONFIGURED/NOT_CONFIGURED; CONFIGURATION_INCOMPLETE (a test is not run); CONFIGURED + NOT_TESTED; CONNECTION_SUCCESSFUL but not active; active but not default; default moves; a changed configuration is re-tested (active) or shows Not tested (inactive). The screen shows separate chips.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `configuredConnectedActiveAndDefaultAreSeparateStatuses`
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "shows the overview from the backend and each provider with only the actions valid for its state, with no axe violations"
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "shows the real outcome of a connection test and reloads the list"

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
