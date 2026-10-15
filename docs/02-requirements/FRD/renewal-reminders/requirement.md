# REQ-SUB-004 — Auto-renewal and renewal reminders

**Status:** Draft
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Not yet approved. Every open question that blocked approval is answered ([C64](../../../01-business/roadmap/open-decisions.md#c64), [C100](../../../01-business/roadmap/open-decisions.md#c100)); what remains is confirmation of the engineering defaults.
**Decision:** [C64](../../../01-business/roadmap/open-decisions.md#c64) (answer to D15, option A with the product owner's reminder rules), [C100](../../../01-business/roadmap/open-decisions.md#c100) (RR-1 to RR-7, 2026-10-14)
**Built:** 2026-10-03 at the product owner's request, with the engineering defaults below; **changed 2026-10-14 for [C100](../../../01-business/roadmap/open-decisions.md#c100)** (auto-renew off, the 7-day read-only grace period, reminders during the grace period, reminder recipients; migration [V040](../../../../database/migrations/V040__seats_grace_purchase_requests_coupons.sql)). Test cases: [TESTPLAN-SUB-004](../../../../test-cases/functional/renewal-reminders/TESTPLAN-SUB-004.md).

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
| 07.04.01.02 | Notify | Yes: renewal reminders (.4–.11) and the reminders during the grace period and their recipients (.16–.20) |
| 07.04.01.03 | Auto-renew | Yes (.1, .2); turning it off or on and the grace period (.3, .12–.15) |
| 07.04.01.04 | Process renewal | Already in [REQ-SUB-001](../subscription-lifecycle/requirement.md); called by auto-renew |

## Summary
Paid subscriptions **auto-renew by default**. On the renewal date the term is extended and the renewal is billed: charged automatically once that is possible, otherwise through a **renewal invoice** the customer pays online or by invoice. Customers get **renewal reminder emails daily** from N days before renewal at a set time in their own time zone until renewed; the platform sets the defaults (7 days), each user can turn them off or choose their own days and time.

[C100](../../../01-business/roadmap/open-decisions.md#c100) adds three things. A customer **can turn auto-renew off**; when the term then ends, the subscription is **read-only for 7 days (a grace period)** and then expires. **Reminders continue every day of the grace period** and stop at the end of the grace day in each recipient's own time zone. For an organization subscription the reminders go to the **organization administrators and the people with Manage billing**, and an administrator can **add any other members**, each on email, in-app or both.

## Actors
- **Customer / organization admin**: receives reminders; sets their own reminder preferences; turns auto-renew on or off.
- **Organization user with Manage billing** (`MANAGE_ORGANIZATION_BILLING`; organization administrators have it): chooses the extra reminder recipients and the channels; turns auto-renew on or off for the organization's subscriptions (a user with Manage subscriptions can also turn it on or off).
- **Organization member added as a recipient**: receives the reminders on the chosen channels, with their own time, time zone and days settings.
- **Platform administrator** (`MANAGE_BILLING`): sets the platform defaults.
- **Scheduler**: runs auto-renewal and reminders.

## Functional requirements
### Auto-renewal
| ID | Requirement | Priority |
|---|---|---|
| REQ-SUB-004.1 | Every new paid subscription has **auto-renew ON** by default and a renewal date (the end of its first billing period). | Must |
| REQ-SUB-004.2 | On the renewal date an auto-renewing subscription is renewed and billed through Billing ([REQ-BIL-001](../billing-payments/requirement.md)): charged automatically **once Razorpay is configured** and a reusable saved method can be charged; until then, or without a usable saved method, a **renewal invoice** is generated for the customer to pay (online or Pay by invoice, [C55](../../../01-business/roadmap/open-decisions.md#c55)). | Must |
| REQ-SUB-004.3 | A customer **can turn auto-renew off and on** for a subscription that has a renewal date ([C100](../../../01-business/roadmap/open-decisions.md#c100) RR-1 B; answers Open question 1). Turning it off asks for a confirmation that explains the grace period (REQ-SUB-004.13). An individual uses `PATCH /me/subscriptions/{id}/auto-renew`; an organization subscription is changed by a user with Manage billing or Manage subscriptions. Each change is audited. | Must |

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

### After the term ends: grace period, read-only and reminders ([C100](../../../01-business/roadmap/open-decisions.md#c100) RR-1 B, RR-4 A)
| ID | Requirement | Priority |
|---|---|---|
| REQ-SUB-004.12 | **Turn auto-renew off or on.** The switch is on each subscription card on My subscriptions (individual and organization). Allowed only on an ACTIVE or SUSPENDED subscription that has a renewal date. Turning it off or on is audited (`SUBSCRIPTION_AUTO_RENEW_CHANGED`) and publishes `SubscriptionChanged`. Reminders before the renewal date still go out while auto-renew is off, because the term will end. | Must |
| REQ-SUB-004.13 | **Seven-day read-only grace period.** When the term of an ACTIVE subscription ends with auto-renew **off**, it is **not expired at once**: it stays ACTIVE with a **grace end date** (term end + 7 days) and is **read-only**. When the grace end date has passed it becomes EXPIRED and grants nothing. The owner of an individual subscription is notified when the grace period starts and when the subscription expires. | Must |
| REQ-SUB-004.14 | **Renewing ends the grace period.** Renewing the subscription, or turning auto-renew back **on** during the grace period (which renews at once), clears the grace end date and restores full access. | Must |
| REQ-SUB-004.15 | **Read-only means the products enforce it.** While in grace the entitlement check answers `allowed` with `accessMode = READ_ONLY` and the grace end date ([REQ-SUB-002.10](../entitlements/requirement.md)); EIS shows "Read-only until {date}" and a notice. EIS does not itself block anything inside a hosted product. | Must |
| REQ-SUB-004.16 | **Daily reminders during the grace period, then stop at the end of the local grace day.** From the day the term ended to the grace period's last day, a reminder goes out **once a day** at the recipient's send time in the recipient's time zone. The last reminder is on the **last grace day in that time zone** — even after the grace instant itself and after the subscription has expired — and then they stop. They do not stop at the instant the grace period ends. A renewed subscription gets no more. Never more than one per subscription, recipient and local day (REQ-SUB-004.7). | Must |
| REQ-SUB-004.17 | **Grace reminder wording.** The grace reminder uses its own template, `NOTIF_RENEWAL_GRACE_REMINDER` ("Your {product} subscription has ended" … "It is read-only until {graceEnd}; renew before then to keep full access."), with the product, plan, the day the term ended, the last day of the grace period in the recipient's zone and links to manage the subscription, pay, and change the reminders. After the subscription has expired the same template says "It has expired; renew to get access back." | Must |

### Who receives the reminders ([C100](../../../01-business/roadmap/open-decisions.md#c100) RR-2 B, RR-5, RR-6 A, RR-7 A)
| ID | Requirement | Priority |
|---|---|---|
| REQ-SUB-004.18 | **Recipients of an organization subscription:** every ACTIVE **organization administrator**, every ACTIVE member with **Manage billing**, and the people an administrator **added** (REQ-SUB-004.19); each person once. An individual subscription's recipient is its owner. | Must |
| REQ-SUB-004.19 | **An administrator chooses many people.** Anyone with Manage billing (organization administrators have it) can add any ACTIVE member of the organization as a recipient, with **email**, **in-app** or **both** (at least one) for each, and remove them again, on My subscriptions. Administrators and Manage billing holders always receive on both channels and are shown read-only. A change is audited (`RENEWAL_REMINDER_RECIPIENTS_CHANGED`). A person who stops being an ACTIVE member receives nothing. | Must |
| REQ-SUB-004.20 | **Settings of an added person.** An added recipient follows their **own** reminder settings: on or off, days before, send time and time zone (REQ-SUB-004.7, .8). **Reminders also appear in the app** (in-app notification, [C100](../../../01-business/roadmap/open-decisions.md#c100) RR-6 A) unless an added person has in-app switched off; the wording of the renewal and grace reminders is edited by platform administrators as **message templates** ([C92](../../../01-business/roadmap/open-decisions.md#c92), [C100](../../../01-business/roadmap/open-decisions.md#c100) RR-7 A; codes `NOTIF_RENEWAL_REMINDER` and `NOTIF_RENEWAL_GRACE_REMINDER`). | Must |
| REQ-SUB-004.21 | **Free trials are skipped** ([C102](../../../01-business/roadmap/open-decisions.md#c102), [REQ-MKT-005](../product-evaluation/requirement.md)). A subscription with `trial` = true gets **no renewal reminder** and no grace reminder: it does not renew and has no grace period. Its own notice is sent **3 days before it ends** (once) and when it ends, by the subscription job. | Must |

## Engineering defaults (built 2026-10-03, until the open questions are answered)
| Topic | Default | Where |
|---|---|---|
| Renewal date | The subscription's term end (`expiresAt`): start + 30 days (Monthly plan) or + 365 days (Yearly), the same periods as Renew. Set for new subscriptions on a Monthly/Yearly plan; subscriptions without one have no renewal date and no reminders | `SubscriptionService` |
| Automatic charging | **Not possible yet** even with Razorpay keys: charging a saved method without the customer needs the Razorpay Customer/Token integration ([C47](../../../01-business/roadmap/open-decisions.md#c47) follow-up). Every renewal therefore issues a renewal invoice now; the switch point is one method (`RenewalService.canChargeAutomatically`) | `RenewalService` |
| Auto-renew job | Every 15 minutes: ACTIVE subscriptions with auto-renew ON whose renewal date has passed are renewed (term extended, renewal invoice, `SubscriptionRenewed`). The hourly expiry job now only expires subscriptions with auto-renew OFF | `RenewalJob`, `SubscriptionService.expireOverdueSubscriptions` |
| Existing subscriptions | Auto-renew ON (migration default) | `V018` |
| Turn auto-renew off (OQ 1) | **Offered** ([C100](../../../01-business/roadmap/open-decisions.md#c100) RR-1 B): REQ-SUB-004.12. Individual `PATCH /me/subscriptions/{id}/auto-renew`; organization `PATCH /organization/me/subscriptions/{id}/auto-renew` | `SubscriptionService.setAutoRenew`, `setOrganizationAutoRenew` |
| Grace period | 7 days (`SubscriptionService.GRACE_DAYS`), started by the hourly expiry job when it first sees an ended subscription with auto-renew off: it sets the grace end date and the subscription stays ACTIVE. Until that run (up to an hour after the term end) the entitlement check says `NO_SUBSCRIPTION`. If the job was down for more than 7 days the subscription expires directly | `SubscriptionExpiryJob`, `expireOverdueSubscriptions` |
| Turning auto-renew back on in grace | Renews at once, from the **old renewal date** (the usual renewal rule, so the days already passed count in the new term), with a renewal invoice. An individual's **Renew** instead starts the new term from now | `SubscriptionService.autoRenew`, `renewSubscription` |
| Grace notices | The owner of an **individual** subscription gets an in-app notice when the grace period starts and when the subscription expires. **Organization** subscriptions get no such notice (organization-owned subscriptions are not notified, as before); their recipients get the grace reminders | `SubscriptionService.notify` |
| Grace reminders | The same scheduler (every 15 minutes) and the same log table: one per subscription, recipient and local day; from the local day the term ended to the local day of the grace end date, at the recipient's send time, ignoring their "days before" value; the recipient's own on/off applies | `RenewalReminderService.sendGraceDue` |
| Default send time (OQ 2) | 09:00 (confirmed, [C100](../../../01-business/roadmap/open-decisions.md#c100) RR-3 A) | `billing_settings.reminder_send_time` |
| Platform default time zone | `Asia/Kolkata` (proposed default — confirm), editable with the other defaults | `billing_settings.reminder_time_zone` |
| Days-before range (OQ 3) | 1 to 30, for users and the platform default (confirmed, [C100](../../../01-business/roadmap/open-decisions.md#c100) RR-3 A) | validation |
| After the renewal date (OQ 4) | **Superseded by [C100](../../../01-business/roadmap/open-decisions.md#c100) RR-4 A:** reminders continue daily through the grace period and stop at the end of the local grace day. With auto-renew on there is no grace period, so none go out after the renewal date | `RenewalReminderService` |
| Organization recipients (OQ 5) | **Superseded by [C100](../../../01-business/roadmap/open-decisions.md#c100) RR-2 B / RR-5:** ACTIVE organization admins, ACTIVE members with Manage billing, and the people an administrator added (table `renewal_reminder_recipient`); each uses their own user settings. Up to 200 added people | `RenewalReminderService.recipients`, `RenewalReminderRecipientService` |
| Per user or per subscription (OQ 6) | One setting per user for all their subscriptions | `renewal_reminder_preference` |
| In-app (OQ 7) | Each reminder is also an in-app notification, on the channels chosen for an added person ([C100](../../../01-business/roadmap/open-decisions.md#c100) RR-6 A) | `NotificationService.notifyTemplated` |
| Template (OQ 8) | **Platform message templates** ([C92](../../../01-business/roadmap/open-decisions.md#c92), [C100](../../../01-business/roadmap/open-decisions.md#c100) RR-7 A): `NOTIF_RENEWAL_REMINDER` and `NOTIF_RENEWAL_GRACE_REMINDER`, English and Spanish, edited on the platform defaults and templates screen | `MessageCatalog` |
| Amount | The plan price; tax lines are added when the REQ-BIL-002 tax engine is built | — |
| Scheduler | Every 15 minutes; a reminder is due when the recipient's local time has reached the send time on a day 1…N days before the local renewal date, and none was logged for that local date | `RenewalJob` |

## Out of scope
Reminders for a free trial (it has its own notice, REQ-SUB-004.21); dunning after a failed renewal; a grace period of any other length or one the customer chooses; SMS or push; an organization "Renew now" action (see [Open points](#open-points)).

## Dependencies
Subscription lifecycle (REQ-SUB-001), billing and invoices (REQ-BIL-001), entitlements (REQ-SUB-002.10, the read-only answer), seats (REQ-SUB-003, a pending decrease is applied at renewal), user preferences (time zone), notifications and message templates (C92), organization permissions (REQ-TEN-005), platform events (REQ-INT-002), audit.

## Where each part of this FRD lives
| Part | Location |
|---|---|
| Requirement, business rules, workflow, acceptance criteria | this folder |
| Screens | [ui-requirements.md](ui-requirements.md) → [renewal-reminders.md](../../../05-ui/screen-requirements/renewal-reminders.md) |
| API | [api-requirements.md](api-requirements.md) → [renewal-reminders.md](../../../06-api/api-requirements/renewal-reminders.md) |
| Data model | [renewal-reminders.md](../../../07-database/data-model/renewal-reminders.md) |

## Open points
Raised for the product owner ([C100](../../../01-business/roadmap/open-decisions.md#c100) "Defaults applied"); none blocks use of the feature.
1. **There is no organization "Renew now" endpoint.** An individual renews from the grace banner (`POST /me/subscriptions/{id}/renew`). An organization subscription can only be brought back by turning **auto-renew on**, which renews at once; the organization card has no Renew button, although the grace notice text still says "Renew now". Decide whether organizations get a manual renew.
2. **Per-seat pricing is an engineering default to confirm** ([REQ-SUB-003.7](../subscription-seats/requirement.md)): it changes the amount of every organization renewal invoice.
3. **Turning auto-renew back on in grace renews from the old renewal date**, while an individual's Renew starts the new term from today. Confirm which the product owner wants.
4. **Grace starts when the hourly expiry job runs**, so for up to an hour after a term ends the entitlement check still says `NO_SUBSCRIPTION` rather than `READ_ONLY`.

## Open questions
All answered. None remains open.

| # | Question | Answer |
|---|---|---|
| 1 | ~~Can customers **turn auto-renew off**, and what happens at the renewal date then (expire, or a grace period)?~~ | **Answered — [C100](../../../01-business/roadmap/open-decisions.md#c100) RR-1 B.** Yes; a 7-day read-only grace period follows, then the subscription expires ([REQ-SUB-004.3, .12 to .15](#functional-requirements)). |
| 2 | ~~**Default send time** for the platform setting.~~ | **Answered — [C100](../../../01-business/roadmap/open-decisions.md#c100) RR-3 A.** 09:00 stays. |
| 3 | ~~**Allowed range** for "days before".~~ | **Answered — [C100](../../../01-business/roadmap/open-decisions.md#c100) RR-3 A.** 1 to 30 stays. |
| 4 | ~~**After the renewal date passes without renewal:** do daily reminders continue, and for how many days?~~ | **Answered — [C100](../../../01-business/roadmap/open-decisions.md#c100) RR-4 A.** Yes, daily through the grace period, stopping at the end of the grace day in the recipient's own time zone ([REQ-SUB-004.16](#functional-requirements)). |
| 5 | ~~**Organization subscriptions:** who receives reminders? Are settings per user or per organization?~~ | **Answered — [C100](../../../01-business/roadmap/open-decisions.md#c100) RR-2 B and RR-5.** Organization admins and people with Manage billing, plus anyone an administrator adds, on email, in-app or both; each person uses their own settings ([REQ-SUB-004.18 to .20](#functional-requirements)). |
| 6 | ~~Are user settings **one setting for all subscriptions**, or **per subscription**?~~ | **Not asked; stays as built:** one setting per user for all their subscriptions. |
| 7 | ~~Should reminders also appear as **in-app notifications**?~~ | **Answered — [C100](../../../01-business/roadmap/open-decisions.md#c100) RR-6 A.** Yes. |
| 8 | ~~**Email template:** who edits the wording?~~ | **Answered — [C100](../../../01-business/roadmap/open-decisions.md#c100) RR-7 A.** Administrators, through the message templates ([REQ-SUB-004.20](#functional-requirements)). |
