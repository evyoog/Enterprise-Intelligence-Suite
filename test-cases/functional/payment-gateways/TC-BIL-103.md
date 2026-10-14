# TC-BIL-103: A real Razorpay connection test works against the Razorpay Test environment

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-103 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.6) |
| Acceptance Criterion | [AC-33](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-7](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 6 (Connection tests are real) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | No |

## Preconditions
Razorpay Test Key ID and secret from the Razorpay Dashboard; the backend can reach Razorpay; `PAYMENT_GATEWAY_ENCRYPTION_KEY` set (or the keys in the secrets file).

## Steps
1. Configure Razorpay with the Test keys and environment Test (or set them in `config/secrets.env`).
2. Choose Test connection.
3. Save a wrong key secret on an inactive copy of the setup or in a scratch environment and test again.
4. Switch the network off from the backend and test.

## Expected Result
Connection successful with the time of the test; authentication failed for the wrong secret; provider unavailable when Razorpay cannot be reached. Never a simulated success.

## Automated coverage
None. This case is manual only.

Manual only: the automated tests mock `RazorpayClient`. **Not Run** — needs real Razorpay keys, which the product owner has not supplied.

## Actual Result
Not run. Needs the environment described under Preconditions.

## Status
Not Run

## Linked Defect (if failed)
None.
