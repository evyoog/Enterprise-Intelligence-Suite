# TC-BIL-019: The Payment step shows the billing summary with Change, the Amount due badge, the tiles and the cart summary

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-019 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.18, .22, .23; C59) |
| Acceptance Criterion | [AC-31](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A customer arriving from the cart with saved billing details and an OPEN invoice for two products.

## Steps
1. Open the Payment step.
2. Click Change next to Contact.

## Expected Result
The breadcrumb shows Cart (link to `/cart`) › Billing details › Payment (current, `aria-current="step"`) › Complete; Contact and Billing address rows with Change; "Amount due" badge with the total; tiles Card, UPI, Netbanking, Wallets, Pay by invoice in one row; the cart summary lists both products with "Tax: none for this region", the total in the primary colour and Edit cart. Change reopens Billing details with the saved values.

## Automated coverage
- `frontend/src/pages/CheckoutPage.test.tsx` — "shows the breadcrumb from the cart, the billing summary with Change, the amount due and the cart summary", "shows five method tiles…"

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
