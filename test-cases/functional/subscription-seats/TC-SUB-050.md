# TC-SUB-050: A seat-increase invoice appears in Billing with its line and tax

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-050 |
| Requirement ID (required) | [REQ-SUB-003](../../../docs/02-requirements/FRD/subscription-seats/requirement.md) (C100) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/subscription-seats/acceptance-criteria.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-SUB-003](TESTPLAN-SUB-003.md) |
| Priority | P1 |
| Type | Functional (manual) |
| Automated | No |

## Preconditions
The dev environment; an organization on a Monthly plan with a tax rule for its region approved; a Manage billing user; billing details saved.

## Steps
1. Increase the seats of a subscription by 2 with 10 days left of a 30-day term.
2. Open Billing → Invoices (organization) from the View invoices link.
3. Open the new invoice and download the document.

## Expected Result
The invoice list shows a new invoice; its line reads "Added seats: {product} — {plan}" with quantity 2 and the amount 2 × price × 10 ÷ 30; the tax lines are calculated on that amount; the invoice number follows the usual format; the document prints the same. No coupon discount is shown.

## Automated coverage
None. This case is manual only.

Manual only: no automated test checks this end to end (the amount is covered by TC-SUB-046; the tax calculation by the tax-rules tests for any invoice). **Not Run**.

## Actual Result
Not Run. The case has not been run by hand yet.

## Status
Not Run

## Linked Defect (if failed)
None.
