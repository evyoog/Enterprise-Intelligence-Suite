# Screen: My subscriptions

| Field | Value |
|---|---|
| Requirement | [REQ-SUB-001](../../02-requirements/FRD/subscription-lifecycle/requirement.md) (suspend, reactivate, cancel, renew, change plan, scheduled downgrade), [REQ-SUB-003](../../02-requirements/FRD/subscription-seats/requirement.md) (organization seats, a decrease at renewal), [REQ-SUB-004](../../02-requirements/FRD/renewal-reminders/requirement.md) (renewal date, reminders, auto-renew switch, read-only grace period, reminder recipients), [REQ-SUB-002](../../02-requirements/FRD/entitlements/requirement.md) (what the plans include) |
| Route | `/my/subscriptions` (signed-in tool frame, [application-layout.md](application-layout.md)); linked from **Your dashboard** (`/my/products`) by "Manage subscriptions" |
| Permissions | Any signed-in customer, for their own subscriptions. The Organization subscriptions section needs `MANAGE_ORGANIZATION_BILLING` to change seats and `MANAGE_SUBSCRIPTIONS` to look ([subscription-seats.md](subscription-seats.md)); the reminder recipients section needs `MANAGE_ORGANIZATION_BILLING` ([renewal-reminders.md](renewal-reminders.md#4-renewal-reminder-recipients-organization)) |
| Built | `frontend/src/pages/MySubscriptionsPage.tsx`; the scheduled-downgrade notice and the plan rule were added 2026-10-13 ([C97](../../01-business/roadmap/open-decisions.md#c97) SC-1); the auto-renew switch, the read-only (grace) chip and notice and the organization sections were added 2026-10-14 ([C100](../../01-business/roadmap/open-decisions.md#c100)) |

Before this file existed the screen was described only in [subscription-lifecycle ui-requirements](../../02-requirements/FRD/subscription-lifecycle/ui-requirements.md) and the section files linked above. This file is the single screen requirement.

## Layout
1. Page header "My subscriptions" with a **Back to dashboard** button.
2. One **subscription card** per individual subscription (below).
3. **Organization subscriptions** section (seats, auto-renew, grace notice) — [subscription-seats.md](subscription-seats.md) — and, for Manage billing holders, the **Renewal reminder recipients** section — [renewal-reminders.md](renewal-reminders.md#4-renewal-reminder-recipients-organization).
4. **What your plans include** section — [entitlements.md](entitlements.md#section-on-my-subscriptions).
5. The **Change plan** dialog, opened from a card.

## Subscription card
| Element | Content | i18n key |
|---|---|---|
| Product name | Bold | — |
| Plan | "Plan: {name}", or "No plan" | `subscriptions.plan`, `subscriptions.noPlan` |
| Term end | "Expires {date}", or "No expiry set" | `subscriptions.expires`, `subscriptions.neverExpires` |
| Status chip | Pending (a purchase waiting for payment — [REQ-BIL-001.25](../../02-requirements/FRD/billing-payments/requirement.md)), Active (success), Suspended (warning), Cancelled, Expired | `subscriptions.status.*` |
| Renewal chips | "Auto-renew on", "Renews on {date}"; while the term has ended and the grace period runs, **"Read-only until {date}"** (warning, lock icon) replaces "Renews on" | `subscriptions.renewal.*`, `subscriptions.grace.chip` |
| **Grace notice** | Warning alert while in grace: "The term of {product} has ended. It is read-only until {date} and then expires. Renew now, or turn auto-renew on to renew at once.", with a **Renew now** button (`POST …/renew`) | `subscriptions.grace.*` |
| **Auto-renew switch** | On an ACTIVE subscription with a renewal date; turning it off asks first (the grace period is explained); on is immediate. Detail in [renewal-reminders.md](renewal-reminders.md#2-my-subscriptions-mysubscriptions) | `subscriptions.autoRenew.*` |
| **Scheduled change notice** | Shown only while a downgrade is scheduled — see below | `subscriptions.scheduledChange.*` |
| Reminder line | Next renewal reminder, or "Reminders are off." with a link to preferences | `subscriptions.renewal.*` |
| Actions | ACTIVE: **Change plan**, **Suspend**, **Renew**, **Cancel subscription**. SUSPENDED: **Reactivate**, **Cancel subscription**. EXPIRED: **Renew**. PENDING and CANCELLED: none | `subscriptions.actions.*` |

Cancel asks for a native confirmation first ("This permanently cancels your subscription to {product}. Continue?"). Actions are disabled, not hidden, while that card's own request is in flight.

## Scheduled downgrade ([C97](../../01-business/roadmap/open-decisions.md#c97) SC-1)
Rule: an upgrade takes effect immediately; a downgrade (a cheaper plan per month) takes effect at the next renewal ([BR-SUB-011](../../02-requirements/FRD/subscription-lifecycle/business-rules.md)).

- **Change plan dialog:** title "Change plan for {product}"; above the select the line **"An upgrade takes effect now. A downgrade takes effect at your next renewal."** (`subscriptions.changePlanDialog.rule`); a **Plan** select (the product's plans, plus "No plan"); **Cancel** and **Save**. After Save the dialog closes and the card shows the result: a new plan at once (upgrade) or the notice below (downgrade).
- **Notice** (an info alert inside the card, only while `pendingPlanId` is set): **"Your plan changes to {plan} on {date}, at your next renewal. You keep your current plan until then."** where {date} is the subscription's term end in the platform date format, or "your next renewal" when there is no term end.
- **Keep current plan:** a button in the alert (`subscriptions.scheduledChange.keep`). It calls `DELETE /me/subscriptions/{id}/plan/scheduled`; on success the notice disappears and the plan is unchanged. A refusal shows the backend message in the page-level alert.
- The current plan shown on the card does not change until the renewal; after the renewal the card shows the new plan and no notice.

## Free trial ([C102](../../01-business/roadmap/open-decisions.md#c102), REQ-MKT-005)
A subscription with `trial` true shows a **"Free trial"** chip and **"Ends {date} ({n} days left)"** (or "Ended {date}"), the note "A free trial cannot be renewed or changed. Subscribe to keep using {product}." and a **Subscribe** button to the product page, and **no** auto-renew switch, "Renews on" chip, Renew or Change plan (and no seat controls in the Organization subscriptions section). Detail and the cart limitation: [product-evaluation.md](product-evaluation.md#2-my-subscriptions-and-organization-subscriptions-free-trial).

## Not built
- Organization plan changes have the same rule and endpoints (`PATCH` and `DELETE …/plan/scheduled` under `/organization/me/subscriptions/{id}`), but the Organization subscriptions section shows **no** scheduled-change notice and no Keep current plan button yet; withdrawing is possible through the API only.

## States
Loading: a centred spinner. Empty: "You have no subscriptions yet." Load error: the backend message in an alert. Action error: the backend message in a dismissible alert, the list stays.

## Accessibility and i18n
Every action is a labelled button operable by keyboard; the dialog has a labelled select; the notice is an `Alert` (role `alert` region of the card) and its button has a visible label; axe test on the page. All strings are in `frontend/src/i18n/locales/{en,es}.json` under `subscriptions.*` and `entitlements.*`.

## API used
[subscription-lifecycle api-requirements](../../02-requirements/FRD/subscription-lifecycle/api-requirements.md): `GET /me/subscriptions`, `POST /me/subscriptions/{id}/suspend | reactivate | cancel | renew`, `PATCH /me/subscriptions/{id}/plan`, `DELETE /me/subscriptions/{id}/plan/scheduled`; [entitlements.md](../../06-api/api-requirements/entitlements.md) `GET /me/entitlements`; renewal info `GET /me/renewals`; `PATCH /me/subscriptions/{id}/auto-renew` ([renewal-reminders.md](../../06-api/api-requirements/renewal-reminders.md#auto-renew-and-the-grace-period)).
