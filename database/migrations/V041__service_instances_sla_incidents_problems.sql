-- C101 / sprint 2027.1.2 answers: service instances and reporter keys (SR), SLA policy and ticket deadlines (SL), unified incidents and problems (IC).
-- Additive. Mirrors backend/src/main/resources/db/schema.sql (Flyway is disabled; apply by hand).
SET search_path TO eis_platform, public;

-- SR-1..SR-5: one instance per subscription; health is reported by the product (or set by a platform administrator).
CREATE TABLE IF NOT EXISTS service_instance (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL UNIQUE REFERENCES product_subscription(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    owner_customer_id BIGINT REFERENCES customer(id),
    owner_organization_id BIGINT REFERENCES organization(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PROVISIONING'
        CHECK (status IN ('PROVISIONING', 'ACTIVE', 'SUSPENDED', 'FAILED', 'DEPROVISIONED')),
    health VARCHAR(10) NOT NULL DEFAULT 'UNKNOWN' CHECK (health IN ('HEALTHY', 'DEGRADED', 'DOWN', 'UNKNOWN')),
    health_message VARCHAR(300),
    last_reported_at TIMESTAMP,
    tenant_reference VARCHAR(100),
    access_url VARCHAR(500),
    last_changed_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM' CHECK (last_changed_by IN ('SYSTEM', 'PRODUCT', 'ADMIN')),
    last_change_note VARCHAR(300),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_service_instance_product ON service_instance (product_id, status);
CREATE INDEX IF NOT EXISTS idx_service_instance_org ON service_instance (owner_organization_id);

-- A product reports through a key a platform administrator creates for it (shown once, stored as a hash).
CREATE TABLE IF NOT EXISTS service_reporter_key (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    label VARCHAR(100),
    key_prefix VARCHAR(12) NOT NULL,
    key_hash VARCHAR(64) NOT NULL UNIQUE,
    created_by_keycloak_sub VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    last_used_at TIMESTAMP,
    revoked_at TIMESTAMP
);

-- SL-1, SL-2: the support targets per priority (the same for every plan) and the business hours they run in.
CREATE TABLE IF NOT EXISTS sla_policy (
    priority VARCHAR(20) PRIMARY KEY CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    first_response_minutes INTEGER NOT NULL CHECK (first_response_minutes > 0),
    resolution_minutes INTEGER NOT NULL CHECK (resolution_minutes > 0),
    around_the_clock BOOLEAN NOT NULL DEFAULT false,
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
INSERT INTO sla_policy (priority, first_response_minutes, resolution_minutes, around_the_clock) VALUES
    ('URGENT', 60, 480, true), ('HIGH', 240, 480, false), ('MEDIUM', 480, 1440, false), ('LOW', 480, 2400, false)
    ON CONFLICT (priority) DO NOTHING;
-- Business hours: "1 day" = 8 business hours, so HIGH is 4 h / 1 day (480 min), MEDIUM 8 h / 3 days (1440 min), LOW 1 day (480) / 5 days (2400).

CREATE TABLE IF NOT EXISTS sla_settings (
    id SMALLINT PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    business_days VARCHAR(30) NOT NULL DEFAULT 'MON,TUE,WED,THU,FRI',
    business_start TIME NOT NULL DEFAULT '09:00',
    business_end TIME NOT NULL DEFAULT '18:00',
    time_zone VARCHAR(60) NOT NULL DEFAULT 'Asia/Kolkata',
    warn_at_percent INTEGER NOT NULL DEFAULT 80 CHECK (warn_at_percent BETWEEN 1 AND 99),
    alert_emails VARCHAR(1000),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
INSERT INTO sla_settings (id) VALUES (1) ON CONFLICT (id) DO NOTHING;

ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS first_responded_at TIMESTAMP;
ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS first_response_due_at TIMESTAMP;
ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS resolution_due_at TIMESTAMP;
ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS first_response_warned_at TIMESTAMP;
ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS resolution_warned_at TIMESTAMP;
ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS first_response_breached_at TIMESTAMP;
ALTER TABLE support_ticket ADD COLUMN IF NOT EXISTS resolution_breached_at TIMESTAMP;

-- IC-1 A: a support incident is the status-page incident. The public fields stay as they were; these are internal.
ALTER TABLE service_incident ADD COLUMN IF NOT EXISTS phase VARCHAR(15) NOT NULL DEFAULT 'INVESTIGATING'
    CHECK (phase IN ('LOGGED', 'INVESTIGATING', 'RESOLVED', 'CLOSED'));
ALTER TABLE service_incident ADD COLUMN IF NOT EXISTS severity VARCHAR(10) NOT NULL DEFAULT 'MINOR' CHECK (severity IN ('MINOR', 'MAJOR', 'CRITICAL'));
ALTER TABLE service_incident ADD COLUMN IF NOT EXISTS internal_notes VARCHAR(4000);
ALTER TABLE service_incident ADD COLUMN IF NOT EXISTS assigned_to_customer_id BIGINT;
ALTER TABLE service_incident ADD COLUMN IF NOT EXISTS resolution_summary VARCHAR(2000);
ALTER TABLE service_incident ADD COLUMN IF NOT EXISTS closed_at TIMESTAMP;
ALTER TABLE service_incident ADD COLUMN IF NOT EXISTS problem_id BIGINT;
-- Incidents already ended before this change are Resolved.
UPDATE service_incident SET phase = 'RESOLVED' WHERE ended_at IS NOT NULL AND phase = 'INVESTIGATING';

CREATE TABLE IF NOT EXISTS service_incident_ticket (
    incident_id BIGINT NOT NULL REFERENCES service_incident(id) ON DELETE CASCADE,
    ticket_id BIGINT NOT NULL REFERENCES support_ticket(id) ON DELETE CASCADE,
    linked_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (incident_id, ticket_id)
);

-- IC-2 A: a problem is the root cause behind several incidents, with corrective actions.
CREATE TABLE IF NOT EXISTS problem (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(4000),
    status VARCHAR(25) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'ROOT_CAUSE_IDENTIFIED', 'RESOLVED', 'CLOSED')),
    root_cause VARCHAR(4000),
    created_by_keycloak_sub VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    resolved_at TIMESTAMP,
    closed_at TIMESTAMP
);
ALTER TABLE service_incident DROP CONSTRAINT IF EXISTS fk_service_incident_problem;
ALTER TABLE service_incident ADD CONSTRAINT fk_service_incident_problem FOREIGN KEY (problem_id) REFERENCES problem(id) ON DELETE SET NULL;

CREATE TABLE IF NOT EXISTS problem_action (
    id BIGSERIAL PRIMARY KEY,
    problem_id BIGINT NOT NULL REFERENCES problem(id) ON DELETE CASCADE,
    description VARCHAR(500) NOT NULL,
    owner VARCHAR(100),
    due_on DATE,
    done BOOLEAN NOT NULL DEFAULT false,
    done_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_problem_action_problem ON problem_action (problem_id);
