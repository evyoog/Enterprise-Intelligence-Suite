# TC-IAM-069: Claim Mapping — AC-6

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-069 |
| Requirement ID (required) | [REQ-IAM-007](../../../docs/02-requirements/FRD/claim-mapping/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/claim-mapping/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-007](TESTPLAN-IAM-007.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
The claim mapping dialog.

## Steps
1. Arrange: the claim mapping dialog.
2. Act: it is open.
3. Observe the response and the UI.

## Expected Result
Its text exists in `en.json` and `es.json`, it is keyboard operable, refusals are shown, and jest-axe reports no violations.

## Automated coverage
- `frontend/src/components/federation/ClaimMappingDialog.test.tsx` — "keeps the dialog open and shows the backend message on failure", "has no detectable a11y violations"

**Manual check:** Check the Spanish text and keyboard use in the dev environment.

## Actual Result
The automated tests below passed on 2026-09-26; the manual check is not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
