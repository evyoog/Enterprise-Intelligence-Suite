# TC-TEN-134: My profile — unsaved-changes guard, Discard and Save bar

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-134 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-023) |
| Acceptance Criterion | [AC-18](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
The profile page with an edit made and not saved.

## Steps
1. Press a sidebar link; cancel the confirmation; press it again and confirm.
2. Edit again and use browser back.
3. Edit and press Discard.
4. Open the page without edits and navigate away.

## Expected Result
Leaving with unsaved changes asks for confirmation and staying keeps the edit; confirming leaves. Discard restores the saved values on every tab. Leaving a clean form does not ask. The Save and Discard bar stays visible while the form is long.

## Automated coverage
- `frontend/src/pages/ProfilePage.test.tsx` — `asks before an in-app link is followed, and lets the person stay or leave`, `follows a link without asking when nothing changed`, `warns on reload or close only while there are unsaved edits`, `shows the Save / Discard bar only once something changed, and Discard restores the saved values`

## Actual Result
Not run yet.

## Status
Not Run
