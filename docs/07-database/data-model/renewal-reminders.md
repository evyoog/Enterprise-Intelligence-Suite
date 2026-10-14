# Data model — Auto-renewal and renewal reminders

[REQ-SUB-004](../../02-requirements/FRD/renewal-reminders/requirement.md). Migration `database/migrations/V018__seats_auto_renew_reminders.sql` (with the seats column of [subscription-seats.md](subscription-seats.md)); the same changes are in `backend/src/main/resources/db/schema.sql`.

## Changed tables
| Table | Column | Description |
|---|---|---|
| product_subscription | auto_renew | BOOLEAN NOT NULL DEFAULT TRUE. Existing subscriptions get TRUE |
| billing_settings | (check) | `reminder_lead_days` BETWEEN 1 AND 30 |
| billing_settings | reminder_lead_days | INT NOT NULL DEFAULT 7, CHECK 1–30. Platform default days before renewal |
| billing_settings | reminder_send_time | VARCHAR(5) NOT NULL DEFAULT '09:00'. Platform default send time (`HH:mm`) |
| billing_settings | reminder_time_zone | VARCHAR(64) NOT NULL DEFAULT 'Asia/Kolkata'. Used when a recipient has no time zone preference |

The renewal date is the existing `product_subscription.expires_at`.

## New tables
### renewal_reminder_preference
One row per user who changed their settings; no row = defaults (enabled, platform days and time).

| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL PK | |
| customer_id | BIGINT NOT NULL UNIQUE | FK customer (the user) |
| enabled | BOOLEAN NOT NULL DEFAULT TRUE | |
| days_before | INT NULL | 1–30; NULL = platform default |
| send_time | VARCHAR(5) NULL | `HH:mm`; NULL = platform default |
| updated_at | TIMESTAMP NOT NULL | |

### renewal_reminder_log
One row per reminder sent ([BR-6](../../02-requirements/FRD/renewal-reminders/business-rules.md)).

| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL PK | |
| subscription_id | BIGINT NOT NULL | FK product_subscription |
| recipient_customer_id | BIGINT NOT NULL | FK customer |
| local_date | DATE NOT NULL | The recipient's local date of sending |
| renewal_date | TIMESTAMP NOT NULL | The renewal date the reminder was about |
| days_before | INT NOT NULL | |
| sent_at | TIMESTAMP NOT NULL | |

Unique `(subscription_id, recipient_customer_id, local_date)`; index on `subscription_id`.

## Grace period and reminder recipients ([C100](../../01-business/roadmap/open-decisions.md#c100))
[REQ-SUB-004.13 to .20](../../02-requirements/FRD/renewal-reminders/requirement.md). Migration [`V040__seats_grace_purchase_requests_coupons.sql`](../../../database/migrations/V040__seats_grace_purchase_requests_coupons.sql), also in `backend/src/main/resources/db/schema.sql`. Additive.

### Changed table
| Table | Column | Description |
|---|---|---|
| product_subscription | grace_ends_at | TIMESTAMP, nullable. Set by the hourly expiry job to the term end + 7 days when a term ends with auto-renew off; the subscription stays ACTIVE and read-only until then, after which it becomes EXPIRED. Cleared by renewing. Null otherwise. `renewal_reminder_log` is reused for grace reminders (the same unique key, one per subscription, recipient and local date) |

### renewal_reminder_recipient
The people an organization administrator added to the renewal reminders of the organization's subscriptions (the always-on recipients, administrators and Manage billing holders, are not stored).

| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL PK | |
| organization_id | BIGINT NOT NULL | FK organization |
| customer_id | BIGINT NOT NULL | FK customer: the person added (an ACTIVE member when the reminder is sent) |
| email_enabled | BOOLEAN NOT NULL DEFAULT true | Receives the email |
| in_app_enabled | BOOLEAN NOT NULL DEFAULT true | Receives the in-app notification. The service refuses a row with both false |
| created_by_customer_id | BIGINT NULL | Who saved the list |
| created_at | TIMESTAMP NOT NULL DEFAULT now() | |

Unique `(organization_id, customer_id)`. A save replaces the whole list of the organization.

