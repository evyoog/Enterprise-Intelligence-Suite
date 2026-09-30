# TC-BIL-008: Billing details can be saved and read back; the gateway status masks the key id and never returns a secret

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-008 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md), [AC-13](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A customer with no billing details yet; Razorpay test credentials configured.

## Steps
1. Act: read billing details before any are saved.
2. Act: save billing details.
3. Act: read them back.
4. Act: read the gateway status.

## Expected Result
Step 1: null. Step 3: the saved billing name and email. Step 4: `configured=true`, the masked key id keeps only the `rzp_test_`/`rzp_live_` prefix and last 4 characters, `keySecretSet`/`webhookSecretSet` are true, and no field carries the actual secret values.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/BillingServiceTest.java` — `billingDetailsCanBeSavedAndReadBack`, `gatewayStatusMasksTheKeyIdAndNeverReturnsSecrets`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/razorpay/RazorpayPropertiesTest.java` — full class

## Actual Result
The automated tests passed on 2026-09-30.

## Status
Passed (automated run 2026-09-30)

## Linked Defect (if failed)
None.
