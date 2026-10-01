# TC-BIL-021: Saved cards: pay with the default card, expired cards cannot be chosen, remove from the menu

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-021 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.18, .22, .23; C59) |
| Acceptance Criterion | [AC-33](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A saved Visa ending 2860 (default) and an expired Mastercard ending 5014.

## Steps
1. Select the Card tile.
2. Try to select the Mastercard.
3. Pay with the Visa.
4. Remove the Visa from its menu.

## Expected Result
Both cards are listed with network, last 4 digits and expiry; the Mastercard shows Expired and cannot be selected; paying sends `paymentMethodId` of the Visa and `method: card` (the backend refuses an expired or foreign method); Remove asks for confirmation, then deletes the method.

## Automated coverage
- `frontend/src/pages/CheckoutPage.test.tsx` — "pays with the default saved card; an expired card cannot be chosen", "removes a saved card from its menu after confirmation"

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
