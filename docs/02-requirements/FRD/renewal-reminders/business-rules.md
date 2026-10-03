# Business rules — Auto-renewal and renewal reminders (REQ-SUB-004)

| ID | Rule | Enforced in | Source |
|---|---|---|---|
| BR-1 | A new subscription on a Monthly or Yearly plan gets auto-renew ON and a renewal date one billing period after its start. | backend | .1 |
| BR-2 | At or after its renewal date, an ACTIVE subscription with auto-renew ON is renewed: the term is extended by one period from the old renewal date, a renewal invoice is generated (non-zero price), `SubscriptionRenewed` is published and the owner is notified. A subscription is renewed once per term (the renewal date moves forward). | backend | .2 |
| BR-3 | Automatic charging is used only when `canChargeAutomatically` is true — never today (no reusable Razorpay token). | backend | .2 |
| BR-4 | Effective settings per recipient: enabled (user, default on); days before = user value, else platform default (default 7); send time = user value, else platform default (default 09:00); time zone = user preference `timeZone`, else platform default time zone. | backend | .4, .5, .7, .8 |
| BR-5 | A reminder is due for (subscription, recipient) when the subscription is ACTIVE, reminders are enabled, and in the recipient's time zone: 1 ≤ (renewal date − today) ≤ days before, and the local time ≥ send time. | backend | .6, .7 |
| BR-6 | At most one reminder per subscription, recipient and local date: the log row (unique) is written before the email; a second attempt the same day finds it and sends nothing. | database + backend | .7 |
| BR-7 | Days before: 1–30 (user and platform). Send time: `HH:mm` 24-hour. Time zone: a valid IANA name. | backend | .8 |
| BR-8 | Recipients: an individual subscription's owner; an organization subscription's ACTIVE organization admins. | backend | OQ 5 default |
| BR-9 | Reminders off does not change auto-renew or renewal. | backend | .10 |
| BR-10 | Audit: `RENEWAL_REMINDER_SENT`, `RENEWAL_REMINDER_PREFERENCES_CHANGED` (user), `RENEWAL_REMINDER_DEFAULTS_CHANGED` (platform), `SUBSCRIPTION_AUTO_RENEWED`. | backend | .11 |
