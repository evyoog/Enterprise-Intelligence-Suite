# Screen sections: Auto-renewal and renewal reminders

| Field | Value |
|---|---|
| Requirement | [REQ-SUB-004](../../02-requirements/FRD/renewal-reminders/requirement.md) |
| Decision | [C64](../../01-business/roadmap/open-decisions.md#c64); [C100](../../01-business/roadmap/open-decisions.md#c100) RR-1 to RR-7; placement per [C44](../../01-business/roadmap/open-decisions.md#c44) — additions to existing screens, no new screen |
| Built | 2026-10-03 — `frontend/src/components/preferences/RenewalRemindersCard.tsx`, `frontend/src/pages/MySubscriptionsPage.tsx`, `frontend/src/components/settings/RenewalReminderDefaultsPanel.tsx`. 2026-10-14 (C100) — `frontend/src/components/subscriptions/AutoRenewControl.tsx`, `GraceNotice.tsx`, `RenewalRecipientsSection.tsx`, `frontend/src/utils/grace.ts` |

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
For each subscription with a renewal date: an **Auto-renew on** chip (when on), "Renews on 2 Nov 2026", and "Next reminder: 26 Oct 2026, 08:30" — or "Reminders are off" with a link to Preferences, or "No reminder before this renewal." The same chips and the switch below are on the organization cards ([subscription-seats.md](subscription-seats.md)).

### Auto-renew switch ([C100](../../01-business/roadmap/open-decisions.md#c100) RR-1 B, REQ-SUB-004.12)
| Element | Behaviour | i18n key |
|---|---|---|
| Switch **Auto-renew** | Shown on an ACTIVE subscription with a renewal date (individual card, organization card). On: hint "Renews automatically at the end of the term." Off: "Not renewing. When the term ends it is read-only for 7 days, then it expires." Disabled while its own request runs. Turning it **on** is immediate; during the grace period it renews at once. A refusal shows the backend message | `subscriptions.autoRenew.*` |
| **Confirmation when turning it off** | Dialog "Turn off auto-renew for {product}?" — "When the term ends, {product} will not renew. It then becomes read-only for 7 days (a grace period) and expires at the end of that period unless you renew. If you turn auto-renew back on during the grace period, it renews at once." Buttons **Turn off auto-renew** (warning colour) / **Cancel** | `subscriptions.autoRenew.confirmOff*` |

### Grace period ([C100](../../01-business/roadmap/open-decisions.md#c100) RR-1 B, REQ-SUB-004.13 to .15)
While the term has ended and `graceEndsAt` is in the future (`isInGrace`):
- the card shows a warning chip with a lock icon, **"Read-only until {date}"**, in place of the "Renews on" chip (`subscriptions.grace.chip`);
- a warning **notice**: "The term of {product} has ended. It is read-only until {date} and then expires. Renew now, or turn auto-renew on to renew at once." (`subscriptions.grace.notice`). An **individual** card adds a **Renew now** button (`POST /me/subscriptions/{id}/renew`). An **organization** card has **no Renew now button**: the organization turns the auto-renew switch on instead (the notice text still says "Renew now"; see the FRD [open points](../../02-requirements/FRD/renewal-reminders/requirement.md#open-points));
- the entitlement card for the product shows "Read-only until {date}" ([entitlements.md](entitlements.md#entitlement-card));
- once the grace date has passed the chip and notice are gone (the subscription is expired and the card shows its status).

## 3. Billing settings → Renewal reminders tab (`/admin/billing/settings?tab=reminders`, `MANAGE_BILLING`)
A fifth tab on [Billing settings](admin-billing-settings.md), same layout (accent section, live preview, save bar).

| Field | Required | Validation |
|---|---|---|
| Default days before renewal | Yes (default 7) | 1–30 |
| Default send time | Yes (default 09:00 — proposed default, confirm) | `HH:mm` |
| Default time zone | Yes (default Asia/Kolkata — proposed default, confirm) | IANA time zone (select) |

Preview: the reminder schedule for an example renewal date (7 … 1 days before, at the time). Note: users can override days and time and can turn reminders off.

## 4. Renewal reminder recipients (organization)
[C100](../../01-business/roadmap/open-decisions.md#c100) RR-2 B, RR-5 (REQ-SUB-004.18 to .20). A section on **My subscriptions**, below the Organization subscriptions section, with a bell icon and title **"Renewal reminder recipients"**: "Organization admins and people with Manage billing always get renewal reminders. Add other members who should get them too." **Shown only to members with Manage billing**; hidden if the API answers 403 or 404.

| Part | Content |
|---|---|
| **Always receive reminders** | Read-only list (an `h3`): name, email and a chip with the reason — **Organization admin** or **Manage billing** — for each default recipient. Not editable |
| **Also receive reminders** | One row per added person: name, email, checkboxes **Email** and **In-app** (accessible names "Email for {name}", "In-app for {name}"), and a remove button ("Remove {name}"). "No one else has been added." when empty |
| **Add person** | An autocomplete of the ACTIVE members who are not already listed (from `GET …/candidates`, so it works for a Manage billing holder without the Manage users permission). A new person starts with both channels on. "No more members to add" when none are left. |
| **Save recipients** | Enabled when the list changed and every person has at least one channel; otherwise a warning says "Choose email, in-app or both for {name}." Success "Reminder recipients saved."; a refusal shows the backend message |

An added person follows their own reminder settings (days, time, time zone). A person who is added but is also an administrator or Manage billing holder is shown only under the always-on list.

## 5. Message templates
The wording of `NOTIF_RENEWAL_REMINDER` and `NOTIF_RENEWAL_GRACE_REMINDER` is edited by platform administrators on the platform defaults and templates screen ([platform-defaults-templates.md](platform-defaults-templates.md)); the grace template's placeholders include `graceEnd`.

## Accessibility and i18n
Visible labels, helper text for errors, switch with accessible name and a described-by hint, the confirmation dialog labelled by its title, preview as a labelled region, axe tests for the card, the page additions, the grace notice, the recipients section and the tab. Strings under `preferences.renewalReminders.*`, `subscriptions.renewal.*`, `subscriptions.autoRenew.*`, `subscriptions.grace.*`, `subscriptions.recipients.*`, `admin.billingSettings.reminders.*` (en and es).

## API used
[renewal-reminders.md](../../06-api/api-requirements/renewal-reminders.md): `PATCH /me/subscriptions/{id}/auto-renew`, `PATCH /organization/me/subscriptions/{id}/auto-renew`, `GET`/`PUT /organization/me/renewal-reminder-recipients`, `GET …/candidates`, `GET /me/renewals`.
