-- C29 (sprint 2026.3.3, REQ-IAM-001): the organization MFA policy on
-- federated sign-in, and setting up an authenticator during sign-in.
-- Mirrors backend/src/main/resources/db/schema.sql.

SET search_path TO eis_platform;

ALTER TABLE mfa_login_challenge
    ADD COLUMN IF NOT EXISTS kind VARCHAR(10) NOT NULL DEFAULT 'VERIFY',
    ADD COLUMN IF NOT EXISTS impersonated BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE mfa_login_challenge
    ADD CONSTRAINT mfa_login_challenge_kind_check CHECK (kind IN ('VERIFY', 'ENROLL'));
