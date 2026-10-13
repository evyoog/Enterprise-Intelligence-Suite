# API requirements — platform defaults and message templates (modules `administration`, `template`)

Requirement: [REQ-GOV-001.7 to .12](../../02-requirements/FRD/platform-administration/requirement.md), decision [C92](../../01-business/roadmap/open-decisions.md#c92). All endpoints need `MANAGE_PLATFORM_SETTINGS` (the existing `/admin/platform-settings/**` rule).

## Defaults for new users
| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/admin/platform-settings/defaults` | The defaults | 200 | 403 |
| PUT | `/admin/platform-settings/defaults` | Save; blank text clears a default; missing `reminderDays` leaves it unchanged | 200 | 400 (not a supported language, real locale, enabled currency, real time zone, known date format; reminder days 1 to 30), 403 |

```json
// PUT /admin/platform-settings/defaults
{ "language": "es", "region": "es-ES", "currency": "EUR", "timeZone": "Europe/Madrid", "dateFormat": "DD/MM/YYYY", "reminderDays": 10 }
// 200 — the same fields plus "updatedAt"
```
The defaults are also returned to every signed-in user as the read-only `defaults` object of `GET /me/preferences`.

## Message templates
| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/admin/platform-settings/message-templates` | Every message type: code, name, description, channel (`EMAIL` or `NOTIFICATION`), placeholders (`name, description, example, required`) and one variant per language | 200 | 403 |
| GET | `/admin/platform-settings/message-templates/{code}` | One message type | 200 | 403, 404 |
| PUT | `/admin/platform-settings/message-templates/{code}/{language}` | Save `{ subject, body }` for the language | 200 (the type) | 400 (unknown or missing placeholder, length, language), 403, 404 |
| DELETE | `/admin/platform-settings/message-templates/{code}/{language}` | Reset the language to the built-in text | 200 (the type) | 403, 404 |
| POST | `/admin/platform-settings/message-templates/{code}/{language}/preview` | `{ subject, body }` filled with example values; nothing is saved | 200 `{ subject, body }` | 400, 403, 404 |

A variant has `language, customized, subject, body` (the text in use), `defaultSubject, defaultBody` (built-in) and `updatedAt`.

**Message types (codes):** `EMAIL_VERIFY`, `EMAIL_DUPLICATE_REGISTRATION`, `EMAIL_PASSWORD_RESET`, `EMAIL_PASSWORD_CHANGED`, `EMAIL_INVITATION`, `NOTIF_ACCESS_CHANGED`, `NOTIF_ORG_ROLE_CHANGED`, `NOTIF_MEMBERSHIP_CHANGED`, `NOTIF_PRODUCT_ACCESS_GRANTED`, `NOTIF_PROJECT_ACCESS_GRANTED`, `NOTIF_INVITATION_ACCEPTED`, `NOTIF_INVITATION_DECLINED`, `NOTIF_EVENT_FAILED`, `NOTIF_RENEWAL_REMINDER`. The placeholders of each type are listed by the API.
