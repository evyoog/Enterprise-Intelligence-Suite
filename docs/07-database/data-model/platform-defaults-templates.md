# Data model — platform defaults and message templates

[REQ-GOV-001.7 to .12](../../02-requirements/FRD/platform-administration/requirement.md). Migration `database/migrations/V033__message_templates_platform_defaults.sql` (also in `backend/src/main/resources/db/schema.sql`).

## message_template
One row per message type and language that an administrator has edited. The built-in text is in code (`MessageCatalog`), not in the table.
| Attribute | Required | Description |
|---|---|---|
| id | Yes | Primary key |
| code | Yes | Message type, for example `EMAIL_INVITATION` |
| language | Yes | `en` or `es` |
| subject | Yes | Up to 200 characters, with `{placeholders}` |
| body | Yes | Up to 5000 characters, with `{placeholders}` |
| updated_by | No | Keycloak subject of the administrator |
| updated_at | Yes | Last change |

Unique on (`code`, `language`). *Reset* deletes the row.

## platform_default
A single row (`id = 1`), seeded empty.
| Attribute | Required | Description |
|---|---|---|
| id | Yes | Always 1 |
| language, region, currency, time_zone, date_format | No | Null = no platform default |
| updated_by, updated_at | No | Last change |

The renewal reminder default is not here: it stays in the billing settings (renewal reminder defaults, 1 to 30) and is read and written there.
