# TC-BIL-106: A changed or lost master key leaves stored credentials unreadable and says so

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-106 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.5) |
| Acceptance Criterion | [AC-36](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-3](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 5 (Credentials are masked) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P1 |
| Type | Security |
| Automated | Partly |

## Preconditions
Credentials stored under key A; the server restarted with key B.

## Steps
1. Decrypt the stored text with another key and with tampered bytes.
2. Read the gateway details under key B.
3. Open Configure and enter the credentials again, then Test and Activate.

## Expected Result
Decryption returns nothing; details show `credentialsUnreadable` true and the fields not set; the dialog shows the error; after re-entry the gateway is configured again.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/gateway/CredentialCipherTest.java` — `aDifferentKeyOrTamperedDataCannotBeRead`

Automated: only the cipher level (`aDifferentKeyOrTamperedDataCannotBeRead`). Not covered: the `credentialsUnreadable` flag in the details and the dialog alert; check manually by starting the backend with another key.

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures). The parts listed as not covered have **not** been run.

## Status
Passed for the automated part (2026-10-14); the not-covered part is Not Run

## Linked Defect (if failed)
None.
