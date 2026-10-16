# Data model — My profile (REQ-TEN-011)

Migration `database/migrations/V046__customer_profile.sql`, mirrored in `backend/src/main/resources/db/schema.sql` (Flyway is disabled: apply V046 by hand to each database, after V045). Decision [C107](../../01-business/roadmap/open-decisions.md#c107).

## customer: columns reused
`first_name` (100, required), `last_name` (100, required), `mobile` (30), `country` (100), `company_name` (255), `job_title` (150), `industry` (150). The validation limits of BR-PRF-007 to BR-PRF-010 (100, 100, 150, 100, 80) are all inside these existing lengths, so no column is widened. A normalised phone is at most 16 characters (`+` and 15 digits).

## customer: new columns (all nullable)
| Column | Type | Limit enforced by the server |
|---|---|---|
| `preferred_name` | varchar(60) | 60 |
| `alternate_phone` | varchar(20) | E.164, at most 16 characters |
| `bio` | varchar(500) | 500 |
| `department` | varchar(100) | 100 |
| `address_line1` | varchar(150) | 150 |
| `address_line2` | varchar(150) | 150 |
| `city` | varchar(80) | 80 |
| `state` | varchar(80) | 80 |
| `postal_code` | varchar(12) | pattern (2 to 11 characters) and India rule (BR-PRF-010) |
| `linkedin_url` | varchar(200) | 200, https, linkedin.com (BR-PRF-011) |
| `website_url` | varchar(200) | 200, http or https |
| `profile_updated_at` | timestamp | set on every save that changes a value (BR-PRF-012) |

Existing rows keep null in every new column. `email`, `status` and `keycloak_sub` are not changed by the profile (BR-PRF-004); the first save of a platform administrator with no record inserts a row with the email from the token and the sign-in linked (BR-PRF-003).

## customer_photo (new table)
| Column | Type | Notes |
|---|---|---|
| `customer_id` | bigint, primary key | references `customer(id)` on delete cascade: one photo per customer |
| `content_type` | varchar(40), not null | `image/jpeg`, `image/png` or `image/webp` |
| `size_bytes` | integer, not null | at most 1,048,576 |
| `data` | bytea, not null | the image (already downsized to 512 x 512 by the browser) |
| `updated_at` | timestamp, not null | used for `photo.updatedAt` and the `ETag` |

The photo is stored in the database on purpose: the S3 bucket decision (KC-3) is open. It is a separate table so the bytes are never loaded with the `customer` entity or with any list of customers; the profile JSON carries only `photo.present` and `photo.updatedAt`. Replacing a photo updates the row; removing it deletes the row; deleting the customer deletes it (cascade).

## Completion
Computed on read from these columns and the existence of a `customer_photo` row (BR-PRF-017); nothing is stored. It is a different set from the directory's completion (BR-DIR-004/005, [org-directory.md](org-directory.md)), which is unchanged.

## Audit
Uses the existing `audit_log` table (BR-PRF-021): actions `PROFILE_CREATED`, `PROFILE_UPDATED`, `PROFILE_PHOTO_UPDATED`, `PROFILE_PHOTO_REMOVED`, `PROFILE_UPDATED_BY_ADMIN`; entries carry the sections changed, never values.

**Platform administrator registry ([C108](../../01-business/roadmap/open-decisions.md#c108)): no database change.** The registry is a JSON file (project today, S3 later, [platform-admin-registry.md](../../08-architecture/security/platform-admin-registry.md)), not a table; there is no migration and no change to `schema.sql`. A listed login that creates their own profile inserts the same `customer` row as BR-PRF-003 describes.
