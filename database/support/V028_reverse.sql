-- Reverses V028__subscription_end_of_day.sql for the rows it touched: puts back the old value, but ONLY where the subscription still
-- has the value V028 wrote (a subscription renewed or changed since is left alone). Exact for untouched-since rows; take a backup first.
SET search_path TO eis_platform, public;

UPDATE product_subscription s
SET expires_at = b.old_expires_at
FROM product_subscription_expiry_backup b
WHERE b.subscription_id = s.id AND s.expires_at = b.new_expires_at;

-- Keep the backup table until the reverse is verified; then: DROP TABLE product_subscription_expiry_backup;
