-- BR-SUB-010 / REQ-INT-003 (platform ↔ tool synchronization, phase 7, 2026-10-09).
-- A subscription's end is the END OF ITS LAST DAY in India: 23:59:00.000 Asia/Kolkata (+05:30), stored as the same instant in UTC
-- (18:29:00.000 on that calendar date). This moves every existing expires_at to that time on the SAME Indian calendar date.
--
-- RUN database/support/V028_dry_run.sql FIRST and review the rows it lists. A backup of every changed value is kept in
-- product_subscription_expiry_backup; database/support/V028_reverse.sql restores exactly the rows this script touched.
-- Mirrors backend/src/main/resources/db/schema.sql (Flyway is disabled; apply by hand). Safe to run twice.

SET search_path TO eis_platform, public;

CREATE TABLE IF NOT EXISTS product_subscription_expiry_backup (
    subscription_id BIGINT PRIMARY KEY,
    old_expires_at  TIMESTAMP NOT NULL,
    new_expires_at  TIMESTAMP NOT NULL,
    normalized_at   TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO product_subscription_expiry_backup (subscription_id, old_expires_at, new_expires_at)
SELECT id, expires_at,
       ((((expires_at AT TIME ZONE 'UTC') AT TIME ZONE 'Asia/Kolkata')::date + time '23:59:00') AT TIME ZONE 'Asia/Kolkata') AT TIME ZONE 'UTC'
FROM product_subscription
WHERE expires_at IS NOT NULL
  AND expires_at <> ((((expires_at AT TIME ZONE 'UTC') AT TIME ZONE 'Asia/Kolkata')::date + time '23:59:00') AT TIME ZONE 'Asia/Kolkata') AT TIME ZONE 'UTC'
ON CONFLICT (subscription_id) DO NOTHING;

UPDATE product_subscription s
SET expires_at = b.new_expires_at
FROM product_subscription_expiry_backup b
WHERE b.subscription_id = s.id AND s.expires_at = b.old_expires_at;
