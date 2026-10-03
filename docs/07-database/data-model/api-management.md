# Data model — API management

[REQ-INT-001](../../02-requirements/FRD/api-management/requirement.md). Migration `database/migrations/V017__platform_events_api_keys.sql`.

## api_key
| Attribute | Required | Description |
|---|---|---|
| id | Yes | Primary key |
| owner_customer_id | Yes | FK → customer (the owner) |
| owner_keycloak_sub | Yes | The owner's login identity, used to act as the owner |
| owner_authorities | No | Comma-separated Keycloak roles copied at creation, platform administration excluded |
| name | Yes | 1–100 characters |
| key_prefix | Yes | `eis_<8 characters>`, unique — shown in lists, used to find the key |
| key_hash | Yes | SHA-256 (hex) of the full key; the full key is never stored (BR-1) |
| expires_at | No | Optional expiry |
| revoked_at | No | Set when revoked |
| last_used_at | No | Last successful authentication |
| request_count | Yes | Successful authentications (default 0) |
| created_at | Yes | Creation time |

Status is derived: REVOKED if `revoked_at`, else EXPIRED if `expires_at` has passed, else ACTIVE.
Rate-limit counters are kept in memory per backend instance, not in the database.
