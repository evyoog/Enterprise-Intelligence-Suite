-- Sprint 2027.1.3 (C102, REQ-MKT-005): free trials. Additive and re-runnable. Apply by hand (Flyway is disabled), after V041.

-- A product offers a free trial when a platform administrator sets its trial days (1 to 90). Empty = no trial.
ALTER TABLE products ADD COLUMN IF NOT EXISTS trial_days INTEGER;
ALTER TABLE products DROP CONSTRAINT IF EXISTS chk_products_trial_days;
ALTER TABLE products ADD CONSTRAINT chk_products_trial_days CHECK (trial_days IS NULL OR trial_days BETWEEN 1 AND 90);

-- A trial is a subscription with trial = true, on the product's highest plan, with expires_at = the end of the trial and auto_renew = false.
-- Buying turns trial back to false. trial_reminded_at: the "3 days left" notice was sent.
ALTER TABLE product_subscription ADD COLUMN IF NOT EXISTS trial BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE product_subscription ADD COLUMN IF NOT EXISTS trial_reminded_at TIMESTAMP;
