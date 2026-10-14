# Screens: SLA management

| Field | Value |
|---|---|
| Requirement | [REQ-SUP-002](../../02-requirements/FRD/sla-management/requirement.md) (.9 to .14); business rules [BR-SLA-009 to BR-SLA-017](../../02-requirements/FRD/sla-management/business-rules.md) |
| Decision | [C101](../../01-business/roadmap/open-decisions.md#c101) (SL-1 B, SL-2 B) |
| Built | Sprint 2027.1.2: `frontend/src/pages/admin/AdminSlaSettingsPage.tsx`, `frontend/src/pages/admin/AdminSupportTicketsPage.tsx` (chips, filter), `frontend/src/pages/MyTicketsPage.tsx` (deadlines), `frontend/src/components/support/` (SLA chips), `frontend/src/api/slaApi.ts`, `frontend/src/api/supportApi.ts` |

Shared presentation rules: unified MUI theme, light and dark mode; states are text chips, never colour alone. Strings are under `sla.*` in `frontend/src/i18n/locales/{en,es}.json`.

## 1. SLA settings tab

| Field | Value |
|---|---|
| Route | **Support** (`/admin/support/tickets`), the **SLA settings** tab: `/admin/support/tickets?tab=sla`. The Support tabs are `Tickets \| SLA settings` (query parameter `?tab=`) |
| Permission | Platform permission `MANAGE_SUPPORT_TICKETS`. Others: the screen is hidden and the API returns 403 |

Header **"Support SLA"** — "Response and resolution targets by ticket priority, the same for every customer and plan." and the note **"New targets apply to tickets created or re-prioritised from now on."** Two cards, each with its own Save.

### Card 1: Targets by priority
"The clock starts when the ticket is created and does not pause. Priorities that are not around the clock count business hours only." One row per priority, in the order Low, Medium, High, Urgent:

| Field | Type | Required | Validation | Message |
|---|---|---|---|---|
| First response (minutes) | Whole number, with a readable duration beside it ("4 h", "1 day (8 business h)") | Yes | 1 or more | "Enter a whole number of minutes, 1 or more." |
| Resolution (minutes) | Whole number, with a readable duration | Yes | 1 or more; not shorter than the first response | "Enter a whole number of minutes, 1 or more." / "First response cannot be later than resolution." |
| Around the clock | Switch | — | — | Urgent is on by default |

Hint: "Enter minutes. One business day is 8 business hours (480 minutes)." **Save targets** (toast "SLA targets saved."); the server's refusal is shown in an alert ("Could not save. Please try again." as a fallback).

### Card 2: Business hours and alerts
"When the clock runs for priorities that are not around the clock, and who is told as a deadline gets close."

| Field | Type | Required | Validation | Message |
|---|---|---|---|---|
| Business days | Toggle buttons Mon to Sun | At least one | — | "Pick at least one business day." |
| Start time, End time | Time (`HH:mm`) | Yes | End after start | "The end time must be after the start time." |
| Time zone | Select/text of time-zone ids | Yes | A known zone | "Choose a time zone." |
| Warn at | Whole number (%) | Yes | 1 to 99 | "Enter a whole number from 1 to 99." |
| Alert email addresses | Chips; type an address and press Enter | No | Valid addresses, at most 20 | "\"{email}\" is not a valid email address." / "At most 20 email addresses." |

Hints: "Business hours are read in this time zone." "Send a warning when this share of a target has passed." "The assignee is always warned." **Save business hours** (toast "Business hours and alerts saved.").

### States
Loading "Loading SLA settings"; load error "Could not load the SLA settings" with a retry. Defaults shown when first opened: Monday to Friday, 09:00 to 18:00, Asia/Kolkata, 80%, no alert addresses.

## 2. Support tickets: SLA chips and filter

| Field | Value |
|---|---|
| Route | **Support** (`/admin/support/tickets`), the **Tickets** tab (the default): `/admin/support/tickets` |
| Permission | `MANAGE_SUPPORT_TICKETS` |

On each ticket row ([ticket-management ui-requirements](../../02-requirements/FRD/ticket-management/ui-requirements.md)):
- Two chips under "Service level targets": **First response** and **Resolution**, each with its state (**On track**, **At risk**, **Breached**, **Met**, **Met late**) and "Due {date and time}". A ticket without deadlines shows no chips.
- A flag **"SLA breached"** when either target is breached.
- **SLA filter** (label "SLA"): **All**, **At risk (warning)**, **Breached**. An empty result shows "No tickets match this SLA filter" with "No ticket is close to its deadline right now." or "No ticket has breached its SLA."

The existing ticket controls (category, priority, assign, escalate, resolve, close) are unchanged. Changing the priority recalculates the chips after Save.

## 3. My tickets: deadlines

| Field | Value |
|---|---|
| Route | **My tickets**, `/support/tickets` |
| Permission | Any signed-in customer, their own tickets only |

Under an open ticket: **"We aim to respond by {date} and resolve by {date}."** Once someone has responded: **"We aim to resolve by {date}."** A resolved or closed ticket, or a ticket created before SLA tracking, shows **no** target. **The customer never sees the SLA state** (no chip, no "at risk", no "breached").

## Accessibility and i18n
The chips have text labels and accessible names; the filter is a labelled select; every field has a label and its error as helper text; the page and chips pass the axe test; all strings are in `en.json` and `es.json`.

## API used
[sla-management.md](../../06-api/api-requirements/sla-management.md): `GET /admin/support/sla`, `PUT /admin/support/sla/policies`, `PUT /admin/support/sla/settings`, and the SLA fields of the ticket endpoints.
