# Preferences — Account → Preferences (C67, C68)

| Field | Value |
|---|---|
| Route | `/account/preferences` (unchanged; optional `?section=`; public page when signed out, inside the app shell when signed in) |
| Decisions | [C67](../../01-business/roadmap/open-decisions.md#c67) (settings), [C68](../../01-business/roadmap/open-decisions.md#c68) (layout) |
| Code | `frontend/src/pages/PreferencesPage.tsx`, `components/preferences/*`, `theming/accentPalette.ts`, `theming/ThemeModeProvider.tsx`, `theming/LocalePreferenceProvider.tsx` |
| Design | [design-system.md](design-system.md) |

**Title:** Preferences. **Description:** Control how the EIS Platform looks, formats information, and communicates with you.

## Layout (C68)
- **Full content width** (no narrow centred container). Page title and description, then two areas.
- **Left — Preferences navigation** (220 px at tablet, 264 px at desktop; outlined, sticky): overline "PREFERENCES", then one item per section with a small icon, its name and a short hint (hints hidden at tablet width). Selected item: light accent background, accent icon and text, 8 px radius. Implemented as a vertical tab list (`role="tablist"`, arrow keys, Home/End); the selection is kept in `?section=appearance|formats|notifications|renewals`.
- **Right — settings panel** (remaining width, white, outlined, 12 px radius): section title, description, divider, rows. Only the selected section is rendered.
- **Rows:** label and hint on the left; control in a fixed 220 px column on the right, every control starting on its left edge (switches offset by their built-in padding). Selects 220 px, days 96 px, time 160 px. Rows stack when the panel is narrower than 560 px (CSS container query on the panel).
- **Phones (< 900 px):** the navigation is replaced by a **Section** selector above the panel. No horizontal overflow (checked at 390 px).
- Signed out, only Appearance and Language & Formats are offered.

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
| Language | Existing languages, shown by their own names (English, Español) | Account and browser (unchanged) |
| Region | Browser default + existing regions — date/number formatting | Account and browser (unchanged) |
| Time zone | Existing time zones; a browser zone outside the list is shown as its IANA name | Account and browser (unchanged) |
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
Unchanged behaviour (REQ-SUB-004.8): Send renewal reminders (switch), Start reminding (N days before; empty = platform default 7), Reminder time (empty = 09:00), Time zone (read-only, the account time zone), summary "Daily reminder from N days before renewal at HH:mm Zone." in a light box, updating live, and **Save reminder settings** at the bottom-right. Signed-in users only.

## States
Loading: skeleton rows for Notifications and Renewal Reminders. Error: inline alert with Retry. Validation: renewal days range and time format shown under the label.

## Accessibility and localization
Every select and switch is labelled by its row label; the accent picker is a `radiogroup` with `radio` items. Text in `en.json` and `es.json` under `preferences.*`.
