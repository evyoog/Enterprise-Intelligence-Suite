# TC-CAT-051: Sort by name, price, newest and rating

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-051 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) (C94) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P1 |
| Type | Functional / UI |
| Automated | Yes |

## Preconditions
Three products with different prices, added dates and ratings.

## Steps
1. Sort by each key.

## Expected Result
Name A to Z; lowest From price first; most recently added first; highest rating first (unrated last).

## Automated coverage
- `frontend/src/utils/catalogFilters.test.ts` — `sorts by name, price, newest and rating`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
