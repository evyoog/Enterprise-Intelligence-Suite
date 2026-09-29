# TC-PRT-007: Service Status Page — AC-7

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-007 |
| Requirement ID (required) | [REQ-PRT-001](../../../docs/02-requirements/FRD/service-status-page/requirement.md) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/service-status-page/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-001](TESTPLAN-PRT-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
The status pages.

## Steps
1. Arrange: the status pages.
2. Act: they are shown.
3. Observe the response and the UI.

## Expected Result
Their text exists in `en.json` and `es.json`, they are keyboard operable, and jest-axe reports no violations.

## Automated coverage
- `frontend/src/pages/ServiceStatusPage.test.tsx` — "has no detectable a11y violations" (customer and admin)

**Manual check:** Check the Spanish text and keyboard use in the dev environment.

## Actual Result
The automated tests below passed on 2026-09-26; the manual check is not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
