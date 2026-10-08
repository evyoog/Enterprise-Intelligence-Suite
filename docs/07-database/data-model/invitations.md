# Data model — Invite user (REQ-TEN-008)

Migration `database/migrations/V026__organization_invitation.sql`, mirrored in `schema.sql`.

## organization_invitation
id, organization_id → organization (cascade), email, normalized_email, org_role (`ORG_ADMIN`/`MEMBER`), org_node_id → org_node (set null), token_hash (SHA-256 hex, unique), status (PENDING, ACCEPTED, DECLINED, EXPIRED, REVOKED), invited_by_customer_id, accepted_by_customer_id, expires_at, created_at, updated_at, accepted_at, declined_at, revoked_at, last_sent_at, send_count. Partial unique index: one PENDING per (organization_id, normalized_email).

## member_access_override (slice of REQ-TEN-005)
id, organization_member_id → organization_member (cascade), item_type (`PERMISSION`), permission_code, granted, set_by_customer_id, set_at; unique (organization_member_id, item_type, permission_code). Only `INVITE_USERS` is written in this sprint; REQ-TEN-005 adds product items and more permissions.

## Permission
`INVITE_USERS` is seeded on the `ORG_ADMIN` role and protected from deletion.
