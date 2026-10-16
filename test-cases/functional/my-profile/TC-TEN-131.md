# TC-TEN-131: Admin — the Edit profile dialog from the members list and the individuals list

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-131 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-020, BR-PRF-023) |
| Acceptance Criterion | [AC-15](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A platform administrator on the organization detail page (members) and on the individuals list.

## Steps
1. Open Edit profile for a member and for an individual; change the company name and an invalid website.
2. Fix and save.

## Expected Result
The dialog shows the four editable tabs without the photo and without the Account tab, the person's email read-only, the server field errors on the fields, and after saving the list shows the updated data. A person without `MANAGE_REGISTRATIONS` does not get the action.

## Automated coverage
- `frontend/src/components/profile/ProfileEditDialog.test.tsx` — `loads the person's record from the admin endpoint and shows the same form without photo or Account tab`, `saves through the admin endpoint, then reports the result and closes`, `applies the same validation and does not call the server when a rule is broken`, `shows the server's field errors on the right tab and stays open`, `shows a refusal such as a missing permission as returned`
- `frontend/src/api/profileApi.test.ts` — `uses the admin endpoints for another person`

## Actual Result
Not run yet.

## Status
Not Run
