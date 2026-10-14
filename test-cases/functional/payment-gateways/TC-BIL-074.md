# TC-BIL-074: Gateways are configured independently

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-074 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.1, .7) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-6, BR-8](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 3 (Each gateway is configured independently) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
Razorpay active and default; FakePay not configured.

## Steps
1. Save FakePay's fields.
2. Test and activate FakePay.
3. Read Razorpay's row.
4. Make FakePay default and read both rows again.

## Expected Result
After each step Razorpay is still active; after the last step the default flag has moved and Razorpay is still active. FakePay's statuses change only as the steps say.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `configuredConnectedActiveAndDefaultAreSeparateStatuses`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `aPaymentKeepsTheProviderThatCreatedItWhateverIsDefaultLater`

Not asserted: that saving one gateway leaves another gateway's stored credentials byte-for-byte unchanged.

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures). The parts listed as not covered have **not** been run.

## Status
Passed for the automated part (2026-10-14); the not-covered part is Not Run

## Linked Defect (if failed)
None.
