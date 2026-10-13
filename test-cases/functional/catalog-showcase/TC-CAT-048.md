# TC-CAT-048: The pricing model is worked out from the plans

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-048 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) (C94) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P1 |
| Type | Functional / UI |
| Automated | Yes |

## Preconditions
Products with repeating, one-time, free and no plans.

## Steps
1. Work out the pricing model of each product.

## Expected Result
Subscription when some paid plan repeats; One-time when only one-time paid plans exist or when there are no plans and a flat price; Free otherwise.

## Automated coverage
- `frontend/src/utils/catalogFilters.test.ts` — `works out the pricing model from the plans`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
