-- Global and per-application seat pricing (seat add-on, additive to a product's/plan's base price).
-- Null seat_price on a product means "inherit the platform default" (billing_settings.global_seat_price).
-- seat_pricing_enabled (default false) preserves every existing product's current pricing behaviour unchanged.
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS global_seat_price NUMERIC(12, 2);
ALTER TABLE billing_settings ADD COLUMN IF NOT EXISTS global_seat_price_currency VARCHAR(10);

ALTER TABLE products ADD COLUMN IF NOT EXISTS seat_pricing_enabled BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE products ADD COLUMN IF NOT EXISTS seat_price NUMERIC(12, 2);
ALTER TABLE products ADD COLUMN IF NOT EXISTS seat_price_currency VARCHAR(10);
