# TC-BIL-026: Payment methods switched off disappear from the checkout and are refused

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-026 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.21, .24; C60) |
| Decision | [C60](../../../docs/01-business/roadmap/open-decisions.md#c60) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
As TC-BIL-024; a customer with an OPEN invoice.

## Steps
1. Switch UPI, Wallets and Pay by invoice off; set the Checkout name and colour; save.
2. Open the customer checkout.
3. Call start-payment with `method: upi`; call Pay by invoice.
4. Switch every method off and save.

## Expected Result
Step 2: only Card and Netbanking tiles; Razorpay Checkout opens with the configured name, description and colour. Step 3: 400 and 403. Step 4: refused ("Keep at least one payment method on…").

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/BillingSettingsServiceTest.java` — `switchingMethodsOffChangesTheCheckoutAndRefusesPayByInvoice`
- `frontend/src/pages/admin/AdminBillingSettingsPage.test.tsx` — "switches payment methods and checkout appearance…"

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
