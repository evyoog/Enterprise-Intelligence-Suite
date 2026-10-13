-- REQ-GOV-002 (C92, TM-1 A and TM-2 A, 2026-10-13): admin-editable message templates (one per message type and
-- language) and the platform defaults new users start with. Mirrors backend/src/main/resources/db/schema.sql
-- (Flyway is disabled; apply by hand). Data model: docs/07-database/data-model/platform-administration.md.

SET search_path TO eis_platform, public;

CREATE TABLE IF NOT EXISTS message_template (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(60) NOT NULL,
    language VARCHAR(10) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_message_template UNIQUE (code, language)
);

-- One row (id = 1). Every value may be empty: empty means "no platform default" (the browser decides), so nothing
-- changes for anyone until an administrator sets a default. Reminder days stay in billing_settings (REQ-SUB-004).
CREATE TABLE IF NOT EXISTS platform_default (
    id BIGINT PRIMARY KEY,
    language VARCHAR(10),
    region VARCHAR(20),
    currency VARCHAR(10),
    time_zone VARCHAR(64),
    date_format VARCHAR(20),
    updated_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO platform_default (id) VALUES (1) ON CONFLICT (id) DO NOTHING;
