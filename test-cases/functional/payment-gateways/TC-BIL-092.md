# TC-BIL-092: Razorpay's environment must match its key prefix

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-092 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.14) |
| Acceptance Criterion | [AC-22](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-14](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 7 (Activation is blocked while configuration is incomplete) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Razorpay with a `rzp_test_` key in the environment.

## Steps
1. Save environment Live with the test key.
2. Save Key ID "abc_123".
3. Validate Live with a live key, Test with a live key, and no environment.

## Expected Result
400 "This is a Test key but the environment is Live."; 400 "The Key ID must start with rzp_test_ or rzp_live_."; matching pairs accepted, mismatch refused, no environment accepted. A test of a mismatched configuration reports UNSUPPORTED_CONFIGURATION.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/GatewayManagementServiceTest.java` — `razorpayKeysMustMatchTheChosenEnvironment`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/gateway/RazorpayGatewayTest.java` — `theKeyAndTheEnvironmentMustAgree`

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 14 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
