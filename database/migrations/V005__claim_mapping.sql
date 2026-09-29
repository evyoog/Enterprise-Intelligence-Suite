-- REQ-IAM-007 claim mapping (sprint 2026.3.3, C23/C28).
-- Mirrors backend/src/main/resources/db/schema.sql. Null = use the defaults,
-- so existing providers behave exactly as before.

SET search_path TO eis_platform;

ALTER TABLE saml_identity_provider
    ADD COLUMN IF NOT EXISTS email_claim VARCHAR(255),
    ADD COLUMN IF NOT EXISTS first_name_claim VARCHAR(255),
    ADD COLUMN IF NOT EXISTS last_name_claim VARCHAR(255),
    ADD COLUMN IF NOT EXISTS display_name_claim VARCHAR(255);

ALTER TABLE oidc_identity_provider
    ADD COLUMN IF NOT EXISTS email_claim VARCHAR(255),
    ADD COLUMN IF NOT EXISTS first_name_claim VARCHAR(255),
    ADD COLUMN IF NOT EXISTS last_name_claim VARCHAR(255),
    ADD COLUMN IF NOT EXISTS display_name_claim VARCHAR(255);
