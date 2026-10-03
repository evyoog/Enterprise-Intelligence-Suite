# Data model — Catalog showcase (C66)

Migration: [V019__catalog_showcase.sql](../../../database/migrations/V019__catalog_showcase.sql), mirrored in `backend/src/main/resources/db/schema.sql` (Flyway disabled). All columns are additive; existing rows keep their behaviour.

## `platforms`
| Column | Type | Null | Default | Rule |
|---|---|---|---|---|
| `primary_color` | VARCHAR(7) | Yes | — | CHECK `^#[0-9A-Fa-f]{6}$`; null = default #6366F1 |
| `status` | VARCHAR(10) | No | `ACTIVE` | CHECK `ACTIVE` / `INACTIVE` |
| `show_in_catalog` | BOOLEAN | No | true | BR-CAT-301 |
| `display_order` | INT | No | 0 | CHECK 0–9999 |

## `products`
| Column | Type | Null | Default | Rule |
|---|---|---|---|---|
| `accent_color` | VARCHAR(7) | Yes | — | CHECK `^#[0-9A-Fa-f]{6}$`; null = inherit (BR-CAT-305) |
| `feature_tags` | VARCHAR(1000) | Yes | — | Comma-separated; ≤ 12 tags × ≤ 40 chars (BR-CAT-306) |
| `documentation_url` | VARCHAR(500) | Yes | — | http/https |
| `support_url` | VARCHAR(500) | Yes | — | http/https |

Not stored: catalog counts, categories and tag unions (derived, BR-CAT-302); release versions (no data — `products.version` stays an edit counter).
