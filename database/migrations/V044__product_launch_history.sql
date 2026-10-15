-- C104 / AN-1: per-launch history, kept for 13 months, alongside the existing
-- product_usage cumulative counter (unchanged).
CREATE TABLE IF NOT EXISTS product_launch_history (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    launched_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_product_launch_history_product ON product_launch_history (product_id);
CREATE INDEX IF NOT EXISTS idx_product_launch_history_launched_at ON product_launch_history (launched_at);
