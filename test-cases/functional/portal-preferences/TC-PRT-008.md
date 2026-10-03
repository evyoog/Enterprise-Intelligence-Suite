# TC-PRT-008: Preferences — Page structure and labels

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-008 |
| Requirement ID (required) | [C67](../../../docs/01-business/roadmap/open-decisions.md#c67) — [preferences screen](../../../docs/05-ui/screen-requirements/preferences.md) |
| Test Plan | [TESTPLAN-PRT-002](TESTPLAN-PRT-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Signed-in user.

## Steps
1. Open Preferences.

## Expected Result
Four sections in order: Appearance, Language & Formats, Notifications, Renewal Reminders. Theme, language, region, time zone, date format, time format and first day of week are labelled selects; reduce motion is a labelled switch; accent color is a labelled radio group; no axe violations.

## Automated coverage
- `frontend/src/pages/PreferencesPage.test.tsx` — `shows the four sections in order`, `every grouped control has an accessible, associated label`, `has no detectable a11y violations`

## Actual Result
The automated tests above passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
