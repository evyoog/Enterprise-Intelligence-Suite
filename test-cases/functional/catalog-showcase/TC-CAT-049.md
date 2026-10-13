# TC-CAT-049: The From price is the cheapest paid plan and New means added in the last 30 days

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-049 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) (C94) |
| Acceptance Criterion | [AC-11](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P1 |
| Type | Functional / UI |
| Automated | Yes |

## Preconditions
Products with several plans and different added dates.

## Steps
1. Work out the From plan. 2. Check the New badge for a product added 12 days ago, 70 days ago and with no date.

## Expected Result
The cheapest paid plan (none for a free-only product); New only for the 12-day-old product.

## Automated coverage
- `frontend/src/utils/catalogFilters.test.ts` — `shows the cheapest paid plan…`, `marks a product New…`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
