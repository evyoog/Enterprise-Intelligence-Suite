# TC-TEN-126: My profile — header card completion and missing-items list

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-126 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-017, BR-PRF-023) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A person with some items missing.

## Steps
1. Read the header card: the bar, "n of 11 complete" and the percentage.
2. Expand the missing items and select one (for example postal code, then photo).
3. Fill everything and save.

## Expected Result
The figures match the API; selecting an item opens its tab and focuses the field (photo points to the avatar actions); at 11 of 11 the list is empty or hidden and the bar shows 100%.

## Automated coverage
- `frontend/src/pages/ProfilePage.test.tsx` — `shows who the person is, the completion status and the five tabs`, `opens the missing list and jumps to the tab and field that fills an item`, `says so when the profile is complete`

## Actual Result
Not run yet.

## Status
Not Run
