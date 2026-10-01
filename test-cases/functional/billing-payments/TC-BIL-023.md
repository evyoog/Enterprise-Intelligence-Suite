# TC-BIL-023: Payment brand logos come from the app bundle and are shown only for offered methods

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-023 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.18, .22, .23; C59) |
| Acceptance Criterion | [AC-35](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P2 |
| Type | Functional |
| Automated | Partly |

## Preconditions
None.

## Steps
1. Inspect the tiles, the saved cards, the cart trust line and the "Powered by" badge.

## Expected Result
Every brand appears only for a method EIS offers; wallets use a generic icon; no request goes to an external site for a payment logo. Until official files with a recorded licence are added to `frontend/src/assets/payment-logos/` (see its ATTRIBUTION.md), each brand is shown as its name in a text chip.

## Automated coverage
- `frontend/src/pages/CheckoutPage.test.tsx`, `frontend/src/pages/CartPage.test.tsx` — brand names in the accepted-methods lists
- `frontend/src/components/payments/PaymentLogos.tsx` — loads only bundled files via `import.meta.glob`

## Actual Result
Text labels verified in the automated run on 2026-10-01. No official logo files have been added yet.

## Status
Partly passed (logo files pending)

## Linked Defect (if failed)
None.
