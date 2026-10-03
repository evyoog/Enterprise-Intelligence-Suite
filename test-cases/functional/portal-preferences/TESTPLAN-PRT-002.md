# TESTPLAN-PRT-002: Preferences page (C67, C68)

| Field | Value |
|---|---|
| Decision | [C67](../../../docs/01-business/roadmap/open-decisions.md#c67) |
| Screen | [preferences.md](../../../docs/05-ui/screen-requirements/preferences.md) |
| Sprint | [2026.4.1](../../../docs/01-business/roadmap/sprints/SPRINT-2026.4.1.md) |
| Scope | Layout, theme, reduced motion, accent colour, formats, notification categories, renewal reminders |
| Out of scope | Backend (unchanged); cross-device persistence of accent and formats (not supported, C67) |
| Run | `cd frontend && npm test` |

| Test case | Title | Status |
|---|---|---|
| [TC-PRT-008](TC-PRT-008.md) | Page structure and labels | Passed |
| [TC-PRT-009](TC-PRT-009.md) | Theme, reduced motion and accent color | Passed |
| [TC-PRT-010](TC-PRT-010.md) | Date format and time format | Passed |
| [TC-PRT-011](TC-PRT-011.md) | Notification categories on the existing opt-out | Passed |
| [TC-PRT-012](TC-PRT-012.md) | Renewal reminders unchanged, live summary | Passed |
| [TC-PRT-013](TC-PRT-013.md) | Settings navigation layout | Passed |
