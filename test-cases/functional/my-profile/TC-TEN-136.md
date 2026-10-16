# TC-TEN-136: My profile — Spanish text

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-136 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-024) |
| Acceptance Criterion | [AC-18](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Functional |
| Automated | No (manual check) |

## Preconditions
The language set to Spanish on Preferences.

## Steps
1. Open the profile page, every tab, the validation messages, the photo messages, the banner, the global bar and the profile menu.
2. Check that `en.json` and `es.json` have the same `profile.*`, `profile.bar.*` and `appShell.nav.profile` keys.

## Expected Result
All visible text is Spanish with no English leaking and no raw keys, including client-side validation messages, tab labels, toasts and the bar; both language files have identical key sets. Messages that come from the server field errors are shown without a raw key.

## Automated coverage
- None: manual check.

## Actual Result
Not run yet.

## Status
Not Run
