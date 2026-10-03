# Screen sections: Auto-renewal and renewal reminders

| Field | Value |
|---|---|
| Requirement | [REQ-SUB-004](../../02-requirements/FRD/renewal-reminders/requirement.md) |
| Decision | [C64](../../01-business/roadmap/open-decisions.md#c64); placement per [C44](../../01-business/roadmap/open-decisions.md#c44) — additions to existing screens, no new screen |
| Built | 2026-10-03 — `frontend/src/components/preferences/RenewalRemindersCard.tsx`, `frontend/src/pages/MySubscriptionsPage.tsx`, `frontend/src/components/settings/RenewalReminderDefaultsPanel.tsx` |

Shared presentation rules: [billing-ui-standards.md](billing-ui-standards.md).

## 1. Preferences → Renewal reminders card (`/account/preferences`, signed-in users)
| Field | Control | Rule |
|---|---|---|
| Send renewal reminders | Switch | Default on. Off: the other fields are disabled and a note says "Your subscriptions still renew; only the emails stop." |
| Start reminding | Number field, "days before renewal" | 1–30. Empty = platform default, shown as helper text "Platform default: 7 days" |
| Send at | Time field (`HH:mm`) | Empty = platform default ("Platform default: 09:00") |
| Time zone | Read-only text | The effective time zone (from the Time zone preference on the same page, else the platform default) |

Summary line: "You'll get a reminder every day from 7 days before each renewal, at 09:00 (Asia/Kolkata)." **Save**; toast "Reminder settings saved."; validation errors under the fields.

## 2. My subscriptions (`/my/subscriptions`)
For each subscription with a renewal date: an **Auto-renew on** chip, "Renews on 2 Nov 2026", and "Next reminder: 26 Oct 2026, 08:30" — or "Reminders are off" with a link to Preferences. Auto-renew is read-only (turning it off is [Open question 1](../../02-requirements/FRD/renewal-reminders/requirement.md#open-questions)).

## 3. Billing settings → Renewal reminders tab (`/admin/billing/settings?tab=reminders`, `MANAGE_BILLING`)
A fifth tab on [Billing settings](admin-billing-settings.md), same layout (accent section, live preview, save bar).

| Field | Required | Validation |
|---|---|---|
| Default days before renewal | Yes (default 7) | 1–30 |
| Default send time | Yes (default 09:00 — proposed default, confirm) | `HH:mm` |
| Default time zone | Yes (default Asia/Kolkata — proposed default, confirm) | IANA time zone (select) |

Preview: the reminder schedule for an example renewal date (7 … 1 days before, at the time). Note: users can override days and time and can turn reminders off.

## Accessibility and i18n
Visible labels, helper text for errors, switch with accessible name, preview as a labelled region, axe tests for the card, the page additions and the tab. Strings under `preferences.renewalReminders.*`, `subscriptions.renewal.*`, `admin.billingSettings.reminders.*` (en and es).

## API used
[renewal-reminders.md](../../06-api/api-requirements/renewal-reminders.md).
