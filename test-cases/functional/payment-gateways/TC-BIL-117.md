# TC-BIL-117: AC-47 Without the encryption key an add or save with credential values is refused clearly (manual)

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-117 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.16, .5) |
| Acceptance Criterion | [AC-47](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-3, BR-22](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 5 (Credentials are masked) |
| Decision | [C99](../../../docs/01-business/roadmap/open-decisions.md#c99) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Security |
| Automated | No |

## Preconditions
A backend started **without** `PAYMENT_GATEWAY_ENCRYPTION_KEY` (or with a value that is not base64 of 32 bytes); an administrator.

## Steps
1. Open **Add payment gateway**, fill the form and type a value for one credential; choose **Add gateway**.
2. Read the message in the dialog and the list.
3. Remove the value and choose **Add gateway** again.
4. Open **Configure** on the new gateway.
5. Call `PUT …/configuration` with a value.

## Expected Result
The add with a value is 409 "Credentials cannot be stored because the encryption key is not configured on the server (PAYMENT_GATEWAY_ENCRYPTION_KEY)." shown inside the dialog, and the gateway is **not** created. The add without any value succeeds (the definition holds no secret). Configure shows the warning that the server has no encryption key and Save is disabled; the API refuses a value with the same 409. No credential is stored anywhere.

## Automated coverage
None. This case is manual only.

Manual only: the test profile always has a key, so the server-side refusal in `create` and `saveConfiguration` is not run by an automated test. The Configure dialog warning for a server without a key is covered by `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "refuses to save when the server has no encryption key" (API mocked); the Add dialog does not know about the key beforehand and shows the server's message after Add. **Not Run.**

## Actual Result
Not run. Needs the environment described under Preconditions.

## Status
Not Run

## Linked Defect (if failed)
None.
