-- C107 (2026-10-16): "My profile" for every signed-in person.
-- New optional person-owned columns on customer. The existing first_name, last_name, mobile, country,
-- company_name, job_title and industry columns are reused unchanged (their lengths already cover the
-- validation limits: mobile 30, country 100, company_name 255, job_title 150, industry 150).
ALTER TABLE customer ADD COLUMN IF NOT EXISTS preferred_name VARCHAR(60);
ALTER TABLE customer ADD COLUMN IF NOT EXISTS alternate_phone VARCHAR(20);
ALTER TABLE customer ADD COLUMN IF NOT EXISTS bio VARCHAR(500);
ALTER TABLE customer ADD COLUMN IF NOT EXISTS department VARCHAR(100);
ALTER TABLE customer ADD COLUMN IF NOT EXISTS address_line1 VARCHAR(150);
ALTER TABLE customer ADD COLUMN IF NOT EXISTS address_line2 VARCHAR(150);
ALTER TABLE customer ADD COLUMN IF NOT EXISTS city VARCHAR(80);
ALTER TABLE customer ADD COLUMN IF NOT EXISTS state VARCHAR(80);
ALTER TABLE customer ADD COLUMN IF NOT EXISTS postal_code VARCHAR(12);
ALTER TABLE customer ADD COLUMN IF NOT EXISTS linkedin_url VARCHAR(200);
ALTER TABLE customer ADD COLUMN IF NOT EXISTS website_url VARCHAR(200);
ALTER TABLE customer ADD COLUMN IF NOT EXISTS profile_updated_at TIMESTAMP;

-- Profile photo, one row per person. Stored in the database on purpose: the object-storage decision (KC-3)
-- is still open. Never loaded together with the customer row.
CREATE TABLE IF NOT EXISTS customer_photo (
    customer_id BIGINT PRIMARY KEY REFERENCES customer (id) ON DELETE CASCADE,
    content_type VARCHAR(40) NOT NULL,
    size_bytes INTEGER NOT NULL,
    data BYTEA NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
