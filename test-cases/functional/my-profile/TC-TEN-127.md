# TC-TEN-127: Global bar — when it is shown and hidden

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-127 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-019) |
| Acceptance Criterion | [AC-14](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
People with a profile at 40%, with a profile at 100%, and a platform administrator with no record.

## Steps
1. Sign in as the 40% person and visit three different pages of the tool, then `/account/profile`.
2. Sign in as the 100% person and visit the same pages.
3. Sign in as the platform administrator with no record and visit the same pages.

## Expected Result
The 40% person sees the bar under the app bar on every tool page with "Your profile is 40% complete", a progress bar and a Complete profile button that opens `/account/profile`; the bar is not shown on the profile page itself. The 100% person and the person with no record never see it.

## Automated coverage
- `frontend/src/components/profile/ProfileCompletionBar.test.tsx` — `shows the percentage, a progress bar and a link to the profile page`, `is hidden on the profile page itself`, `hides the bar for a person with no record and does not poll`
- `frontend/src/components/layout/AppShell.test.tsx` — `shows the profile bar under the app bar on every page while the profile is incomplete`, `leaves the bar out on the profile page, when complete, and for a person with no customer record`, `still works when the completion cannot be read`

## Actual Result
Not run yet.

## Status
Not Run
