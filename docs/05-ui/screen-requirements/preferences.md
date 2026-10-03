# Preferences — Account → Preferences (C67)

| Field | Value |
|---|---|
| Route | `/preferences` (unchanged; public page when signed out, inside the app shell when signed in) |
| Decision | [C67](../../01-business/roadmap/open-decisions.md#c67) |
| Code | `frontend/src/pages/PreferencesPage.tsx`, `components/preferences/*`, `theming/accentPalette.ts`, `theming/ThemeModeProvider.tsx`, `theming/LocalePreferenceProvider.tsx` |
| Design | [design-system.md](design-system.md) |

**Title:** Preferences. **Description:** Control how the EIS Platform looks, formats information, and communicates with you.

## Layout
One centred column (max 760 px) in a single bordered container. Each section has a heading (h2), a one-line description and rows separated by thin dividers. A row is the label and an optional hint on the left and the control on the right (260 px); on phones the control drops below the label. No icons, cards inside the container, tabs, gradients or animations.

## 1. Appearance — "Customize how EIS looks for you."
| Row | Control | Values | Saved to |
|---|---|---|---|
| Theme | Select | Light, Dark, System | Account (`/me/preferences.themeMode`) and browser |
| Reduce motion | Switch | On / Off — turns off the app's entrance and hover animations (unchanged) | Account (`reducedMotion`) and browser |
| Accent color | Radio group of swatches (name + dot, check and ring when selected; arrow keys move) | Indigo (default), Blue, Violet, Purple, Green, Teal, Orange, Rose, Red, Slate | Browser (`vyoog-accent`) |

The accent replaces the MUI primary palette for the whole interface immediately. Light-mode shades (white text on buttons) — Indigo #4F46E5, Blue #2563EB, Violet #7C3AED, Purple #9333EA, Green #15803D, Teal #0F766E, Orange #C2410C, Rose #BE123C, Red #DC2626, Slate #475569 — all ≥ 4.5:1 on white and on the page background; dark-mode shades are the lighter 400 tones.

## 2. Language & Formats — "Choose your language, region, and formatting preferences."
| Row | Values | Saved to |
|---|---|---|
| Language | Existing languages | Account and browser (unchanged) |
| Region | Browser default + existing regions — date/number formatting | Account and browser (unchanged) |
| Time zone | Existing time zones | Account and browser (unchanged) |
| Date format | Region default, DD/MM/YYYY, MM/DD/YYYY, YYYY-MM-DD | Browser (`vyoog-date-format`) |
| Time format | Region default, 12-hour, 24-hour | Browser (`vyoog-time-format`) |
| First day of week | Region default, Sunday, Monday | Browser (`vyoog-week-start`); no calendar uses it yet |

## 3. Notifications — "Choose how you receive notifications from EIS."
| Row | Control | Backend categories (email opt-out) |
|---|---|---|
| In-app notifications | Text "Always on" | — the backend always records in-app notifications |
| Email notifications | Switch — off disables every category below | all |
| Products & applications | Switch | ORDER |
| Billing | Switch | BILLING, SUBSCRIPTION |
| Platform & support | Switch | SYSTEM, ORGANIZATION |
| Security | Switch; warning text when off | SECURITY, PRIVILEGED_ACCESS |

Saves immediately to `PUT /me/notifications/preferences` (the same data the bell menu edits); on failure the switch reverts and an error is shown. Signed-in users only.

## 4. Renewal Reminders — "Configure reminders for upcoming subscription renewals."
Unchanged behaviour (REQ-SUB-004.8): Send renewal reminders (switch), Start reminding (N days before; empty = platform default 7), Reminder time (empty = 09:00), Time zone (read-only, the account time zone), summary "Daily reminder from N days before renewal at HH:mm Zone." updating live, and **Save reminder settings**. Signed-in users only.

## States
Loading: skeleton rows for Notifications and Renewal Reminders. Error: inline alert with Retry. Validation: renewal days range and time format shown under the label.

## Accessibility and localization
Every select and switch is labelled by its row label; the accent picker is a `radiogroup` with `radio` items. Text in `en.json` and `es.json` under `preferences.*`.
