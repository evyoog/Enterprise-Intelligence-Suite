# TC-BIL-007: Saving a card requires consent; setting a default leaves only one default; removing deletes the token at Razorpay

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-007 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md), [AC-10](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A customer; the gateway is configured.

## Steps
1. Act: start saving a card without ticking consent.
2. Observe the result.
3. Act: save a card with consent, marked default. Act: save a second card with consent, also marked default.
4. Observe the saved methods.
5. Act: remove the first (no-longer-default) method.

## Expected Result
Step 2: refused (`IllegalArgumentException`). Step 4: exactly one method is default (the second one — the first default was cleared). Step 5: `RazorpayClient#deleteToken` is called and the method disappears from the active list.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/BillingServiceTest.java` — `savingACardWithoutConsentIsRefused`, `addingAPaymentMethodAndSettingItDefaultLeavesOnlyOneDefault`

## Actual Result
The automated tests passed on 2026-09-30.

## Status
Passed (automated run 2026-09-30)

## Linked Defect (if failed)
None.
