# TC-PRT-010: Preferences — Date format, time format and first day of week

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-010 |
| Requirement ID (required) | [C67](../../../docs/01-business/roadmap/open-decisions.md#c67) — [preferences screen](../../../docs/05-ui/screen-requirements/preferences.md) |
| Test Plan | [TESTPLAN-PRT-002](TESTPLAN-PRT-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Time zone Asia/Kolkata, region en-GB.

## Steps
1. Leave the formats at Region default.
2. Choose MM/DD/YYYY, then YYYY-MM-DD.
3. Choose 24-hour, then 12-hour.
4. Choose Monday as first day of week.

## Expected Result
Dates stay "15 Mar 2026" by default, then show 03/15/2026 and 2026-03-15; date-times show 14:05 and then 2:05 pm; all three choices are stored in the browser.

## Automated coverage
- `frontend/src/theming/preferenceFormats.test.tsx` — `keeps the region format by default and applies explicit date and time formats`
- `frontend/src/pages/PreferencesPage.test.tsx` — `saves a date format, time format and first day of week`

## Actual Result
The automated tests above passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
