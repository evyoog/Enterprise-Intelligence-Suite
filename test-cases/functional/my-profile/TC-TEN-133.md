# TC-TEN-133: Profile menu — My profile first, the email line and the avatar block

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-133 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-025) |
| Acceptance Criterion | [AC-17](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in person in the tool.

## Steps
1. Open the account menu at the top right.
2. Check the order of items and the identity block.
3. Select the avatar and name block, then My profile.

## Expected Result
My profile is the first menu item, followed by Security, Preferences and Sign out; the email is shown under the name; the avatar shows the photo when there is one; both the identity block and My profile open `/account/profile`.

## Automated coverage
- `frontend/src/components/layout/AppShell.test.tsx` — `opens My profile from the first account-menu item`, `opens My profile when the avatar and name block in the menu is clicked, and shows the email line`

## Actual Result
Not run yet.

## Status
Not Run
