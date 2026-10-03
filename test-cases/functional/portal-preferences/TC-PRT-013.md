# TC-PRT-013: Preferences — settings navigation layout

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-013 |
| Requirement ID (required) | [C68](../../../docs/01-business/roadmap/open-decisions.md#c68) — [preferences screen](../../../docs/05-ui/screen-requirements/preferences.md) |
| Test Plan | [TESTPLAN-PRT-002](TESTPLAN-PRT-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Signed-in user.

## Steps
1. Open Preferences.
2. Select Language & Formats in the navigation.
3. Open `/account/preferences?section=renewals`, then press the down arrow on the selected navigation item.
4. Use the Section selector (phone layout) to choose Notifications.
5. Run axe on each of the four panels.

## Expected Result
The navigation lists the four sections; only the selected section's panel is shown (one h2). The URL selects Renewal Reminders; the arrow key moves selection and focus to Appearance. The selector switches the panel. No axe violations on any panel. Visual check at 1440, 1000 and 390 px: controls share one right-hand column, rows stack on narrow panels, no horizontal overflow.

## Automated coverage
- `frontend/src/pages/PreferencesPage.test.tsx` — `shows a navigation of four sections and only the selected one`, `moves between sections with the arrow keys and opens a section from the URL`, `offers a section selector for small screens`, `has no detectable a11y violations on any section`

## Actual Result
The automated tests above passed on 2026-10-03; screenshots checked at 1440, 1000 and 390 px.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
