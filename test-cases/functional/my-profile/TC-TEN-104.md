# TC-TEN-104: My profile — screen validation, tab badges and server errors on the fields

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-104 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-023) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
The profile page open for a member.

## Steps
1. Type an invalid first name and an invalid website; leave the fields.
2. Look at the tab labels and press Save.
3. Fix the client-side errors but make the server refuse a field the screen allows (stub a `fieldErrors` answer for `address.city`).
4. Fix every field and save.

## Expected Result
Each invalid field shows its message and `aria-invalid`; the tabs Personal and Online show an error badge; Save is refused locally and nothing is sent. A server `fieldErrors` entry is shown on the matching field and its tab, and the first invalid tab opens. After the fixes the save succeeds and the badges disappear.

## Automated coverage
- `frontend/src/pages/ProfilePage.test.tsx` — `shows the rule inline when a field is left, marks the tab and sets aria-invalid`, `does not call the server when a save is tried with errors, and shows the tab and field to fix`, `maps the server field errors back onto their fields, on the right tab`
- `frontend/src/utils/profileValidation.test.ts` — `names the tabs that hold errors, in tab order`, `validates one field against the whole form`
- `frontend/src/api/profileApi.test.ts` — `reads the server's per-field messages from a 400`

## Actual Result
Not run yet.

## Status
Not Run
