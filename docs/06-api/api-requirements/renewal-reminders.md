# API requirements — Auto-renewal and renewal reminders

Requirement: [REQ-SUB-004](../../02-requirements/FRD/renewal-reminders/requirement.md). Business rules: [business-rules.md](../../02-requirements/FRD/renewal-reminders/business-rules.md).

## Signed-in user
| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/me/notification-preferences/renewal-reminders` | The caller's reminder settings and the effective values (own value, else platform default) | 200 | 401 |
| PUT | `/me/notification-preferences/renewal-reminders` | Save the caller's settings. `daysBefore` and `sendTime` may be `null` (= use the platform default) | 200 | 400 (`VALIDATION_ERROR`: days outside 1–30, time not `HH:mm`), 401 |
| GET | `/me/renewals` | The caller's subscriptions with a renewal date: auto-renew, renewal date, next reminder (in the caller's time zone, or `null` when off or none is due), and `graceEndsAt` while the subscription is in its read-only grace period. Includes the organization subscriptions of organizations the caller **administers** (a Manage billing holder who is not an administrator does not see them here; they use the organization subscriptions list) | 200 | 401 |

```json
// GET /me/notification-preferences/renewal-reminders
{ "enabled": true, "daysBefore": null, "sendTime": "08:30",
  "effectiveDaysBefore": 7, "effectiveSendTime": "08:30", "effectiveTimeZone": "Asia/Kolkata",
  "platformDaysBefore": 7, "platformSendTime": "09:00", "minDays": 1, "maxDays": 30 }

// GET /me/renewals
[ { "subscriptionId": 42, "productName": "Valam.ai", "planName": "Standard", "autoRenew": true,
    "renewalDate": "2026-11-02T10:00:00Z", "remindersEnabled": true,
    "nextReminderAt": "2026-10-26T08:30:00+05:30", "graceEndsAt": null } ]
```

## Platform administrator (`MANAGE_BILLING`; others 403)
| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/admin/billing/settings/renewal-reminders` | Platform defaults | 200 | 403 |
| PUT | `/admin/billing/settings/renewal-reminders` | Save the defaults | 200 | 400 (days outside 1–30, time not `HH:mm`, unknown time zone), 403 |

```json
{ "daysBefore": 7, "sendTime": "09:00", "timeZone": "Asia/Kolkata", "updatedAt": "2026-10-03T06:00:00Z" }
```

## Auto-renew and the grace period
[C100](../../01-business/roadmap/open-decisions.md#c100) RR-1 B. Body for both: `{ "autoRenew": true | false }` (required). The answer is the subscription (`SubscriptionDto`) with `autoRenew`, `expiresAt` and `graceEndsAt`.

| Method | Path | Who | Success | Errors |
|---|---|---|---|---|
| PATCH | `/me/subscriptions/{id}/auto-renew` | The owner of an individual subscription | 200 | 400 (`autoRenew` missing; the subscription is not ACTIVE or SUSPENDED; it has no renewal date), 401, 404 (not the caller's, or an organization subscription) |
| PATCH | `/organization/me/subscriptions/{id}/auto-renew` | An organization user with `MANAGE_ORGANIZATION_BILLING` or `MANAGE_SUBSCRIPTIONS` | 200 | 400 (as above), 401, 403, 404 (another organization's subscription) |

- Setting the value it already has changes nothing and is not audited. A change is audited (`SUBSCRIPTION_AUTO_RENEW_CHANGED`) and publishes `SubscriptionChanged`.
- **Turning it off:** the subscription keeps renewing nothing; when the term ends the hourly expiry job starts a **7-day read-only grace period** (`graceEndsAt` = term end + 7 days, status stays `ACTIVE`) and then sets `EXPIRED`.
- **Turning it on during the grace period** (term ended, `graceEndsAt` set) **renews at once**: a renewal invoice is created, `graceEndsAt` is cleared and the term moves one period on from the old renewal date. Turning it on earlier only sets the flag.
- While in grace the entitlement check answers `allowed` with `accessMode: READ_ONLY` and `graceEndsAt` ([entitlements.md](entitlements.md#read-only-during-the-grace-period)).
- There is **no organization "renew now" endpoint**; an individual can still `POST /me/subscriptions/{id}/renew` ([REQ-SUB-004 open points](../../02-requirements/FRD/renewal-reminders/requirement.md#open-points)).

## Reminder recipients of an organization
[C100](../../01-business/roadmap/open-decisions.md#c100) RR-2 B, RR-5. All three need `MANAGE_ORGANIZATION_BILLING` in the caller's organization (others 403; no organization 404).

| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/organization/me/renewal-reminder-recipients` | Who gets the reminders: `defaults` (ACTIVE organization admins and ACTIVE members with Manage billing; always on, both channels) and `added` (the people an administrator chose, with their channels) | 200 | 401, 403 |
| GET | `/organization/me/renewal-reminder-recipients/candidates` | The ACTIVE members who are not default recipients and could be added (so a Manage billing holder can pick people without the Manage users permission), by name | 200 | 401, 403 |
| PUT | `/organization/me/renewal-reminder-recipients` | **Replaces** the added people. Body `{ "recipients": [ { "customerId": 7, "email": true, "inApp": false } ] }` (at most 200). Audited `RENEWAL_REMINDER_RECIPIENTS_CHANGED` | 200 (the same shape as GET) | 400 (a person listed twice; neither channel chosen; not an ACTIVE member of the organization), 401, 403 |

```json
// GET /organization/me/renewal-reminder-recipients
{ "defaults": [ { "customerId": 3, "name": "Asha Rao", "email": "asha@example.com", "reason": "ORG_ADMIN", "emailEnabled": true, "inAppEnabled": true } ],
  "added":    [ { "customerId": 7, "name": "Ravi Kumar", "email": "ravi@example.com", "reason": "ADDED", "emailEnabled": true, "inAppEnabled": false } ] }
// GET …/candidates
[ { "customerId": 9, "name": "Meena Iyer", "email": "meena@example.com" } ]
```
`reason` is `ORG_ADMIN`, `MANAGE_BILLING` or `ADDED`. An added person is listed under `defaults` instead when they are also an administrator or a Manage billing holder. Each recipient follows their **own** reminder settings (days, time, time zone, on or off) from `/me/notification-preferences/renewal-reminders`.

## Reminder wording
The renewal reminder and the grace reminder are the message templates `NOTIF_RENEWAL_REMINDER` and `NOTIF_RENEWAL_GRACE_REMINDER` ([platform-defaults-templates.md](platform-defaults-templates.md)); the grace template has the placeholders `product`, `plan`, `date` (the day the term ended), `graceEnd`, `how`, `manageUrl`, `billingUrl` and `preferencesUrl`.

## Events
`SubscriptionRenewed` (auto-renewal), `SubscriptionChanged` (auto-renew switched, grace period started) and `RenewalReminderSent` (payload field `grace`: true for a grace-period reminder) are published through the outbox ([event-platform.md](event-platform.md)). Payloads carry IDs, dates and amounts only — no card data ([BR-BIL-001](../../03-business-rules/BR-BIL-001-no-raw-card-data.md)).
