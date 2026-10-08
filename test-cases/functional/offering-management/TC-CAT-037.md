# TC-CAT-037: No price on an offering

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-037 |
| Requirement ID (required) | [REQ-CAT-005](../../../docs/02-requirements/FRD/offering-management/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/offering-management/acceptance-criteria.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Any offering.

## Steps
1. Open the admin Offerings page and the public offering page.

## Expected Result
Neither shows a price of its own; the note says prices stay on the products (OF-2).

## Automated coverage
- AdminOfferingsPage.test.tsx — states that an offering has no price of its own

## Actual Result
The automated tests above passed on 2026-10-08.

## Status
Passed (automated run 2026-10-08)

## Linked Defect (if failed)
