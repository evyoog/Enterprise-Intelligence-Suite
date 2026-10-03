# REQ-SUB-004 — Auto-renewal and renewal reminders

**Status:** Draft
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Not yet approved
**Decision:** [C64](../../../01-business/roadmap/open-decisions.md#c64) (answer to D15, option A with the product owner's reminder rules)
**Built:** 2026-10-03 at the product owner's request, with the engineering defaults below. Test cases: [TESTPLAN-SUB-004](../../../../test-cases/functional/renewal-reminders/TESTPLAN-SUB-004.md).

| Field | Value |
|---|---|
| Sprint | [2026.4.3](../../../01-business/roadmap/sprints/SPRINT-2026.4.3.md) |
| Requirement ID | REQ-SUB-004 |
| Application | [07 Subscription & Entitlement Management](../../../01-business/roadmap/applications/07-subscription-entitlement-management.md) |
| Application code | `APP-SUB` |
| Priority | P0 |
| AI required | No |

## Source functions
| Function ID | Function | Covered here |
|---|---|---|
| 07.04.01.01 | Schedule renewal | Yes: the renewal date of new paid subscriptions (.1) |
| 07.04.01.02 | Notify | Yes: renewal reminders (.4–.11) |
| 07.04.01.03 | Auto-renew | Yes (.1, .2) |
| 07.04.01.04 | Process renewal | Already in [REQ-SUB-001](../subscription-lifecycle/requirement.md); called by auto-renew |

## Summary
Paid subscriptions **auto-renew by default**. On the renewal date the term is extended and the renewal is billed: charged automatically once that is possible, otherwise through a **renewal invoice** the customer pays online or by invoice. Customers get **renewal reminder emails daily** from N days before renewal at a set time in their own time zone until renewed; the platform sets the defaults (7 days), each user can turn them off or choose their own days and time.

## Actors
- **Customer / organization admin**: receives reminders; sets their own reminder preferences.
- **Platform administrator** (`MANAGE_BILLING`): sets the platform defaults.
- **Scheduler**: runs auto-renewal and reminders.

## Functional requirements
### Auto-renewal
| ID | Requirement | Priority |
|---|---|---|
| REQ-SUB-004.1 | Every new paid subscription has **auto-renew ON** by default and a renewal date (the end of its first billing period). | Must |
| REQ-SUB-004.2 | On the renewal date an auto-renewing subscription is renewed and billed through Billing ([REQ-BIL-001](../billing-payments/requirement.md)): charged automatically **once Razorpay is configured** and a reusable saved method can be charged; until then, or without a usable saved method, a **renewal invoice** is generated for the customer to pay (online or Pay by invoice, [C55](../../../01-business/roadmap/open-decisions.md#c55)). | Must |
| REQ-SUB-004.3 | Whether a customer can **turn auto-renew off**: Open question 1. | — |

### Renewal reminders (product owner's rules)
| ID | Requirement | Priority |
|---|---|---|
| REQ-SUB-004.4 | **Platform default days before renewal** (platform administrator setting): **default 7**, changeable. | Must |
| REQ-SUB-004.5 | **Platform default send time** (platform administrator setting): default Open question 2. | Must |
| REQ-SUB-004.6 | **Daily until renewed:** reminders start on the configured day before the renewal date and are sent **once a day** at the configured time — for the default, 7, 6, 5, 4, 3, 2 and 1 day before — and **stop as soon as the subscription is renewed**. | Must |
| REQ-SUB-004.7 | **Correct time:** each reminder is sent at the configured time **in the recipient's time zone** (user preference `timeZone`; the platform default time zone if none). Never more than one reminder per subscription and recipient per day, even if the job runs late or twice. Each sent reminder is logged. | Must |
| REQ-SUB-004.8 | **User controls:** each user can turn renewal reminders **off** (and on), set **how many days before** renewal they start, and set the **time of day** — each overriding the platform default. Allowed range: Open question 3. | Must |
| REQ-SUB-004.9 | **Email content:** product, plan, renewal date, amount (with tax per [REQ-BIL-002](../tax-rules/requirement.md)), whether a saved method will be charged or an invoice issued, and links to manage the subscription, to pay or update the payment method, and to change or turn off reminders. | Must |
| REQ-SUB-004.10 | Turning reminders off stops the emails only — not renewal or auto-renewal. | Must |
| REQ-SUB-004.11 | Sending a reminder, and every change to reminder settings (platform and user), are audited; a sent reminder publishes `RenewalReminderSent` ([REQ-INT-002](../event-platform/requirement.md)). | Must |

## Engineering defaults (built 2026-10-03, until the open questions are answered)
| Topic | Default | Where |
|---|---|---|
| Renewal date | The subscription's term end (`expiresAt`): start + 30 days (Monthly plan) or + 365 days (Yearly), the same periods as Renew. Set for new subscriptions on a Monthly/Yearly plan; subscriptions without one have no renewal date and no reminders | `SubscriptionService` |
| Automatic charging | **Not possible yet** even with Razorpay keys: charging a saved method without the customer needs the Razorpay Customer/Token integration ([C47](../../../01-business/roadmap/open-decisions.md#c47) follow-up). Every renewal therefore issues a renewal invoice now; the switch point is one method (`RenewalService.canChargeAutomatically`) | `RenewalService` |
| Auto-renew job | Every 15 minutes: ACTIVE subscriptions with auto-renew ON whose renewal date has passed are renewed (term extended, renewal invoice, `SubscriptionRenewed`). The hourly expiry job now only expires subscriptions with auto-renew OFF | `RenewalJob`, `SubscriptionService.expireOverdueSubscriptions` |
| Existing subscriptions | Auto-renew ON (migration default) | `V018` |
| Turn auto-renew off (OQ 1) | **Not offered**; the status is shown read-only | — |
| Default send time (OQ 2) | 09:00 (proposed default — confirm) | `billing_settings.reminder_send_time` |
| Platform default time zone | `Asia/Kolkata` (proposed default — confirm), editable with the other defaults | `billing_settings.reminder_time_zone` |
| Days-before range (OQ 3) | 1 to 30, for users and the platform default | validation |
| After the renewal date (OQ 4) | No more reminders | `RenewalReminderService` |
| Organization recipients (OQ 5) | Every ACTIVE member with the organization-admin role; each uses their own user settings | `RenewalReminderService` |
| Per user or per subscription (OQ 6) | One setting per user for all their subscriptions | `renewal_reminder_preference` |
| In-app (OQ 7) | Each reminder is also an in-app notification (the existing notification service sends both) | `NotificationService.notify` |
| Template (OQ 8) | Fixed English wording in code until D27 (platform templates) is decided | `RenewalReminderService` |
| Amount | The plan price; tax lines are added when the REQ-BIL-002 tax engine is built | — |
| Scheduler | Every 15 minutes; a reminder is due when the recipient's local time has reached the send time on a day 1…N days before the local renewal date, and none was logged for that local date | `RenewalJob` |

## Out of scope
Turning auto-renew off (OQ 1); grace periods; dunning after a failed renewal; editing the email template (D27); SMS or push.

## Dependencies
Subscription lifecycle (REQ-SUB-001), billing and invoices (REQ-BIL-001), user preferences (time zone), notifications, platform events (REQ-INT-002), audit.

## Where each part of this FRD lives
| Part | Location |
|---|---|
| Requirement, business rules, workflow, acceptance criteria | this folder |
| Screens | [ui-requirements.md](ui-requirements.md) → [renewal-reminders.md](../../../05-ui/screen-requirements/renewal-reminders.md) |
| API | [api-requirements.md](api-requirements.md) → [renewal-reminders.md](../../../06-api/api-requirements/renewal-reminders.md) |
| Data model | [renewal-reminders.md](../../../07-database/data-model/renewal-reminders.md) |

## Open questions
| # | Question | Blocks approval |
|---|---|---|
| 1 | Can customers **turn auto-renew off**, and what happens at the renewal date then (expire, or a grace period)? | Yes |
| 2 | **Default send time** for the platform setting (proposed default — confirm: 09:00). | No — confirm in review |
| 3 | **Allowed range** for "days before" (minimum 1; maximum?), and does the platform admin set that range? | No — confirm in review |
| 4 | **After the renewal date passes without renewal** (payment failed or auto-renew off): do daily reminders continue, and for how many days? | No — confirm in review |
| 5 | **Organization subscriptions:** who receives reminders (all organization admins, billing users, the purchaser)? Are settings per user or per organization? | Yes |
| 6 | Are user settings **one setting for all subscriptions**, or **per subscription**? | No — confirm in review |
| 7 | Should reminders also appear as **in-app notifications**? | No — confirm in review |
| 8 | **Email template:** who edits the wording? Platform templates (D27) are not decided. | No — confirm in review |
