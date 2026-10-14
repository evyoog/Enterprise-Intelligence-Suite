# TC-BIL-104: A real signed Razorpay webhook is accepted once

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-104 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.10) |
| Acceptance Criterion | [AC-34](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-12, BR-13](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | 11 (Webhooks are signature-verified); 12 (No duplicate financial operations) |
| Decision | [C98](../../../docs/01-business/roadmap/open-decisions.md#c98) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | No |

## Preconditions
A public backend URL (`BACKEND_URL`); the webhook registered in the Razorpay Dashboard for payment.captured, payment.failed and refund.processed; an OPEN invoice paid with a Razorpay test payment.

## Steps
1. Register the webhook with a secret and enter the same secret in EIS.
2. Pay the invoice in Razorpay test mode.
3. Open View details → Webhook.
4. Use the Dashboard's redelivery for the event.
5. Change the secret in EIS to a wrong value and redeliver.

## Expected Result
Signature verified and the event shown; the invoice is PAID once; the redelivery is acknowledged with no change; the wrong secret makes the delivery fail and the rejected count rises.

## Automated coverage
None. This case is manual only.

Manual only: no automated test receives a delivery from Razorpay itself. **Not Run** — needs a public URL and Razorpay credentials.

## Actual Result
Not run. Needs the environment described under Preconditions.

## Status
Not Run

## Linked Defect (if failed)
None.
