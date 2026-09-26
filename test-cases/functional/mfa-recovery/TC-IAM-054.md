# TC-IAM-054: Mfa Recovery — AC-5

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-054 |
| Requirement ID (required) | [REQ-IAM-008](../../../docs/02-requirements/FRD/mfa-recovery/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/mfa-recovery/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-008](TESTPLAN-IAM-008.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
The reset dialog.

## Steps
1. Arrange: the reset dialog.
2. Act: it is open.
3. Observe the response and the UI.

## Expected Result
Its text exists in `en.json` and `es.json`, it is keyboard operable, and a jest-axe check reports no violations.

## Automated coverage
- `frontend/src/components/security/ResetMfaDialog.test.tsx` — "has no detectable a11y violations with the dialog open"

**Manual check:** Check the Spanish text and keyboard use in the dev environment.

## Actual Result
The automated tests below passed on 2026-09-26; the manual check is not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
