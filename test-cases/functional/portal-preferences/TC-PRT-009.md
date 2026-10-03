# TC-PRT-009: Preferences — Theme, reduced motion and accent color

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-009 |
| Requirement ID (required) | [C67](../../../docs/01-business/roadmap/open-decisions.md#c67) — [preferences screen](../../../docs/05-ui/screen-requirements/preferences.md) |
| Test Plan | [TESTPLAN-PRT-002](TESTPLAN-PRT-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Signed-in user.

## Steps
1. Choose Dark theme.
2. Turn on Reduce motion.
3. Choose the Green accent, then press the right arrow key.

## Expected Result
The theme changes and is stored; `html[data-reduced-motion]` becomes true; Green becomes the primary colour (#15803D on contained buttons) and is stored in `vyoog-accent`; the arrow key selects Teal. Every accent keeps backgrounds and status colours unchanged.

## Automated coverage
- `frontend/src/pages/PreferencesPage.test.tsx` — `choosing dark theme updates the real ThemeModeProvider and persists it`, `toggling reduced motion flips the html[data-reduced-motion] attribute`, `selecting an accent colour applies it to the theme and persists it`
- `frontend/src/theming/preferenceFormats.test.tsx` — `replaces only the primary colour`

## Actual Result
The automated tests above passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
