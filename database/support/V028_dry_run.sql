-- Review BEFORE applying V028__subscription_end_of_day.sql: every subscription whose end would change, with the Indian date it keeps.
-- A row whose Indian date differs from the UTC date of the old value moves by almost a day: check those with the billing owner.
SET search_path TO eis_platform, public;

SELECT s.id AS subscription_id,
       s.owner_organization_id,
       s.owner_customer_id,
       s.status,
       s.expires_at AS old_expires_at_utc,
       ((((s.expires_at AT TIME ZONE 'UTC') AT TIME ZONE 'Asia/Kolkata')::date + time '23:59:00') AT TIME ZONE 'Asia/Kolkata') AT TIME ZONE 'UTC' AS new_expires_at_utc,
       ((s.expires_at AT TIME ZONE 'UTC') AT TIME ZONE 'Asia/Kolkata')::date AS indian_date_kept,
       (s.expires_at::date <> ((s.expires_at AT TIME ZONE 'UTC') AT TIME ZONE 'Asia/Kolkata')::date) AS utc_date_differs
FROM product_subscription s
WHERE s.expires_at IS NOT NULL
  AND s.expires_at <> ((((s.expires_at AT TIME ZONE 'UTC') AT TIME ZONE 'Asia/Kolkata')::date + time '23:59:00') AT TIME ZONE 'Asia/Kolkata') AT TIME ZONE 'UTC'
ORDER BY s.id;
