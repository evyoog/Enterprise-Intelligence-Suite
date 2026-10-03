# Design system (C66)

Decision: [C66](../../01-business/roadmap/open-decisions.md#c66). Supersedes the multi-colour page accents of C60 for page chrome; C60 accent keys are still accepted by `PageHeader` for existing callers and shown only as a small icon.

## Tokens (`frontend/src/theme.ts`)
| Token | Light | Dark | Note |
|---|---|---|---|
| Brand indigo | #6366F1 | — | `BRAND_INDIGO`; default showcase colour |
| User accent (C67) | per user | per user | Replaces Primary only; presets in `theming/accentPalette.ts`, chosen on [Preferences](preferences.md) |
| Primary (text, buttons) | #4F46E5 | #818CF8 | #6366F1 with white text is 4.47:1, under AA, so solid buttons use #4F46E5 |
| Background | #F7F8FC | #0B1020 | page |
| Paper | #FFFFFF | #121829 | cards |
| Divider / border | #E6E8F0 | #232B3F | 1px card borders |
| Text primary / secondary | #0F172A / #64748B | — | |
| Radius | 8 (controls), 12 (cards) | | |
| Font | Inter | | |

No gradients, no coloured banners, no shadows on cards (border only); buttons are solid without elevation.

## Shell
- Light sidebar (240 px), plain icons; the active item has a light-indigo background, indigo text and a 3 px left indicator.
- Minimal header: menu, search, notifications, cart, user name and avatar.

## Shared components (`frontend/src/components/ui/`)
| Component | Purpose |
|---|---|
| `PageHeader` (layout) | Uppercase label, title, description, one action on the right |
| `SummaryCard` | Icon, label, value, hint |
| `ShowcaseCard` | Platform and app card: logo, name, type badge, meta, description, features, status, action. White card, 3 px top border in the showcase colour; the whole card is one link |
| `StatusBadge` | Dot plus label; tones success, warning, error, info, neutral |
| `FeatureTag` | Neutral feature label |
| `FormSection` | Titled, described, labelled section of a form |
| `ColorPicker` | Default/inherit vs custom; 7 presets, native picker, HEX field |
| `EmptyState`, `ErrorState` | Empty and error states with an action / Retry |
| `ConfirmDialog` | Confirmation before delete or retire |
| `SupportCTA` | Link to the real support flow (`/support/tickets`) |

## Colour utilities (`frontend/src/utils/showcaseColor.ts`)
`showcasePalette(hex)` gives `base`, `soft` (tint), `border`, `hover` and `text` (darkened until 4.5:1 on white). `appColor(app)` implements BR-CAT-305.

## Screens using the design
Redesigned: see [catalog-showcase.md](catalog-showcase.md). All other screens inherit the theme, shell and `PageHeader` only (C66 *Not redesigned*).
