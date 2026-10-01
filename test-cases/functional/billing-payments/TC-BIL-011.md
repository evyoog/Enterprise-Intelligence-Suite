# TC-BIL-011: The card panel never holds card data (BR-BIL-001)

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-011 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.18–.21, decision C55) |
| Acceptance Criterion | [AC-21](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
As TC-BIL-009.

## Steps
1. Select Card.
2. Inspect the card panel and the DOM.
3. Complete a card payment and inspect network requests and server logs.

## Expected Result
The card number, name, expiry and CVV are decorative read-only placeholders (no `<input>` other than the save-card and terms checkboxes); a note says card details are handled by the payment provider. No EIS request payload, log or database column contains a card number, expiry or CVV.

## Automated coverage
- `frontend/src/pages/CheckoutPage.test.tsx` — "never renders a card input: the card panel holds no text fields (BR-BIL-001)"

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
