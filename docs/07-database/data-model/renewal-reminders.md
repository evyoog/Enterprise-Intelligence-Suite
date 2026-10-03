# Data model — Auto-renewal and renewal reminders

[REQ-SUB-004](../../02-requirements/FRD/renewal-reminders/requirement.md). Migration `database/migrations/V018__seats_auto_renew_reminders.sql` (with the seats column of [subscription-seats.md](subscription-seats.md)); the same changes are in `backend/src/main/resources/db/schema.sql`.

## Changed tables
| Table | Column | Description |
|---|---|---|
| product_subscription | auto_renew | BOOLEAN NOT NULL DEFAULT TRUE. Existing subscriptions get TRUE |
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
| user_id | VARCHAR(255) NOT NULL UNIQUE | Keycloak subject |
| enabled | BOOLEAN NOT NULL DEFAULT TRUE | |
| days_before | INT NULL | 1–30; NULL = platform default |
| send_time | VARCHAR(5) NULL | `HH:mm`; NULL = platform default |
| updated_at | TIMESTAMPTZ NOT NULL | |

### renewal_reminder_log
One row per reminder sent ([BR-6](../../02-requirements/FRD/renewal-reminders/business-rules.md)).

| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL PK | |
| subscription_id | BIGINT NOT NULL | FK product_subscription |
| recipient_user_id | VARCHAR(255) NOT NULL | |
| local_date | DATE NOT NULL | The recipient's local date of sending |
| renewal_date | TIMESTAMPTZ NOT NULL | The renewal date the reminder was about |
| days_before | INT NOT NULL | |
| sent_at | TIMESTAMPTZ NOT NULL | |

Unique `(subscription_id, recipient_user_id, local_date)`; index on `subscription_id`.
