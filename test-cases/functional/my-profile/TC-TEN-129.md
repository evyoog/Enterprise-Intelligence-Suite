# TC-TEN-129: Global bar — refreshes after a save or photo change, without polling

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-129 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-019) |
| Acceptance Criterion | [AC-14](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A person at 40% on a page other than the profile page, then on the profile page.

## Steps
1. On the profile page fill more fields and save; go to another page.
2. Upload and then remove a photo; go to another page.
3. Wait 2 minutes on a page and watch the network calls.

## Expected Result
After each save or photo change the bar shows the new percentage (and disappears at 100%) with exactly one completion request; there are no periodic requests.

## Automated coverage
- `frontend/src/components/profile/ProfileCompletionBar.test.tsx` — `reads GET /me/profile/completion once on sign-in and feeds the bar`, `updates when the profile page reports a save, and when asked to refresh`, `hides the bar for a person with no record and does not poll`
- `frontend/src/pages/ProfilePage.test.tsx` — `saves every tab in one request, normalised, then reports the new completion and shows a toast`, `uploads the scaled photo and reports the new completion`

## Actual Result
Not run yet.

## Status
Not Run
