# TC-BIL-082: A default gateway can be selected, and it must be active

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-082 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.7) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-8](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 9 (A default gateway can be selected) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Razorpay active and default; FakePay active.

## Steps
1. Make FakePay default.
2. Read both rows.
3. Create a payment for a card in INR.
4. Deactivate FakePay and try to make it default.

## Expected Result
FakePay is the only default and takes the payment; making an inactive gateway default is 409 "Only an active gateway can be the default." The screen offers Make default only on an active, non-default row.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `configuredConnectedActiveAndDefaultAreSeparateStatuses`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `activationNeedsCompleteConfigurationAndAPassingConnectionTest`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `aPaymentKeepsTheProviderThatCreatedItWhateverIsDefaultLater`
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "makes another active gateway the default and deactivates one"

Not asserted by a test: that deactivating the default leaves no default until another is chosen or activated.

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
