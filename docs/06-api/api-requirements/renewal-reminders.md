# API requirements — Auto-renewal and renewal reminders

Requirement: [REQ-SUB-004](../../02-requirements/FRD/renewal-reminders/requirement.md). Business rules: [business-rules.md](../../02-requirements/FRD/renewal-reminders/business-rules.md).

## Signed-in user
| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/me/notification-preferences/renewal-reminders` | The caller's reminder settings and the effective values (own value, else platform default) | 200 | 401 |
| PUT | `/me/notification-preferences/renewal-reminders` | Save the caller's settings. `daysBefore` and `sendTime` may be `null` (= use the platform default) | 200 | 400 (`VALIDATION_ERROR`: days outside 1–30, time not `HH:mm`), 401 |
| GET | `/me/renewals` | The caller's subscriptions with a renewal date: auto-renew, renewal date, next reminder (in the caller's time zone, or `null` when off or none is due) | 200 | 401 |

```json
// GET /me/notification-preferences/renewal-reminders
{ "enabled": true, "daysBefore": null, "sendTime": "08:30",
  "effectiveDaysBefore": 7, "effectiveSendTime": "08:30", "effectiveTimeZone": "Asia/Kolkata",
  "platformDaysBefore": 7, "platformSendTime": "09:00", "minDays": 1, "maxDays": 30 }

// GET /me/renewals
[ { "subscriptionId": 42, "productName": "Valam.ai", "planName": "Standard", "autoRenew": true,
    "renewalDate": "2026-11-02T10:00:00Z", "remindersEnabled": true,
    "nextReminderAt": "2026-10-26T08:30:00+05:30" } ]
```

## Platform administrator (`MANAGE_BILLING`; others 403)
| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/admin/billing/settings/renewal-reminders` | Platform defaults | 200 | 403 |
| PUT | `/admin/billing/settings/renewal-reminders` | Save the defaults | 200 | 400 (days outside 1–30, time not `HH:mm`, unknown time zone), 403 |

```json
{ "daysBefore": 7, "sendTime": "09:00", "timeZone": "Asia/Kolkata", "updatedAt": "2026-10-03T06:00:00Z" }
```

## Not built
Turning auto-renew off (for example `PATCH /me/subscriptions/{id}/auto-renew`) — [Open question 1](../../02-requirements/FRD/renewal-reminders/requirement.md#open-questions).

## Events
`SubscriptionRenewed` (auto-renewal) and `RenewalReminderSent` are published through the outbox ([event-platform.md](event-platform.md)). Payloads carry IDs, dates and amounts only — no card data ([BR-BIL-001](../../03-business-rules/BR-BIL-001-no-raw-card-data.md)).
