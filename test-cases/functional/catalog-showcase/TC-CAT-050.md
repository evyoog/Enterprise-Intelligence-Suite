# TC-CAT-050: Filters by platform, pricing model, feature and saved, with facet counts

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-050 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) (C94) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P1 |
| Type | Functional / UI |
| Automated | Yes |

## Preconditions
Two products with different platforms, plans and tags; one saved.

## Steps
1. Apply each filter alone, then features Hrm and Payroll together. 2. Read the facet counts.

## Expected Result
Each filter keeps only matching products; several features need all tags; the platform, pricing model and feature counts match the products.

## Automated coverage
- `frontend/src/utils/catalogFilters.test.ts` — `filters by platform, pricing model, feature…`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
