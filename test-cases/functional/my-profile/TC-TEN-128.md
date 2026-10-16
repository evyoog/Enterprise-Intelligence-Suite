# TC-TEN-128: Global bar — dismissal is per user and per session

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-128 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-019) |
| Acceptance Criterion | [AC-14](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A person at 40%.

## Steps
1. Press the dismiss control, then navigate to other pages and reload.
2. Open a new tab of the same browser session.
3. Open a new browser window and sign in again; then, in the first tab, sign in as a different person.

## Expected Result
The bar stays hidden for that person during the browser session (including reload and other pages) and returns in the next browser session (a new tab or window). The dismissal of one person is not applied to another person signing in on the same tab; signing out and in again as the same person in the same tab keeps it dismissed.

## Automated coverage
- `frontend/src/components/profile/ProfileCompletionBar.test.tsx` — `can be dismissed for the session, per user, and comes back once the session storage is cleared`, `returns at the next sign-in (a new browser session has no dismissal)`, `still shows (undismissed) when session storage is unavailable`
- `frontend/src/components/layout/AppShell.test.tsx` — `dismisses the bar for the session`

## Actual Result
Not run yet.

## Status
Not Run
