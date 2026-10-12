-- REQ-INT-002 (C91, EV-3 and EV-4, 2026-10-12): platform-administrator settings for the event platform:
-- how long delivered events are kept (default 30 days, configurable) and who is told when an event fails for good
-- (in-app and email). One row (id = 1), created on first use. Mirrors backend/src/main/resources/db/schema.sql
-- (Flyway is disabled; apply by hand). Data model: docs/07-database/data-model/event-platform.md.

SET search_path TO eis_platform, public;

CREATE TABLE IF NOT EXISTS event_platform_setting (
    id BIGINT PRIMARY KEY,
    retention_days INT NOT NULL DEFAULT 30 CHECK (retention_days BETWEEN 1 AND 3650),
    alert_recipients VARCHAR(2000),
    alert_in_app BOOLEAN NOT NULL DEFAULT TRUE,
    alert_email BOOLEAN NOT NULL DEFAULT TRUE,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO event_platform_setting (id) VALUES (1) ON CONFLICT (id) DO NOTHING;
