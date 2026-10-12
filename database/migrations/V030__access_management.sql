-- REQ-TEN-005 Access management (C90, 2026-10-12). Additive except that the old unique constraint of member_access_override
-- is replaced by two partial unique indexes. Mirrors backend/src/main/resources/db/schema.sql (Flyway is disabled; apply by hand).
-- Data model: docs/07-database/data-model/access-management.md.

SET search_path TO eis_platform, public;

-- Individual overrides now also cover product access.
ALTER TABLE member_access_override ADD COLUMN IF NOT EXISTS product_id BIGINT REFERENCES products(id) ON DELETE CASCADE;
ALTER TABLE member_access_override ALTER COLUMN permission_code DROP NOT NULL;
DO $$
DECLARE c text;
BEGIN
    FOR c IN SELECT conname FROM pg_constraint WHERE conrelid = 'member_access_override'::regclass AND contype = 'u' LOOP
        EXECUTE 'ALTER TABLE member_access_override DROP CONSTRAINT ' || quote_ident(c);
    END LOOP;
END $$;
CREATE UNIQUE INDEX IF NOT EXISTS ux_override_permission ON member_access_override (organization_member_id, permission_code) WHERE item_type = 'PERMISSION';
CREATE UNIQUE INDEX IF NOT EXISTS ux_override_product ON member_access_override (organization_member_id, product_id) WHERE item_type = 'PRODUCT';

-- Role defaults of one organization. No row = the platform role definition (permissions) or ON (products).
CREATE TABLE IF NOT EXISTS organization_role_default (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    org_role VARCHAR(20) NOT NULL,
    item_type VARCHAR(20) NOT NULL,
    permission_code VARCHAR(60),
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    enabled BOOLEAN NOT NULL,
    updated_by_customer_id BIGINT REFERENCES customer(id),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX IF NOT EXISTS ux_role_default_permission ON organization_role_default (organization_id, org_role, permission_code) WHERE item_type = 'PERMISSION';
CREATE UNIQUE INDEX IF NOT EXISTS ux_role_default_product ON organization_role_default (organization_id, org_role, product_id) WHERE item_type = 'PRODUCT';

-- Product access that was granted or revoked directly becomes an explicit override, so it keeps its state when
-- role defaults apply. Access that came from a project (source_group_id) is not touched.
INSERT INTO member_access_override (organization_member_id, item_type, product_id, granted, set_at)
SELECT a.organization_member_id, 'PRODUCT', a.product_id, (a.status = 'ACTIVE'), now()
FROM organization_product_access a
WHERE a.source_group_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM member_access_override o
                  WHERE o.organization_member_id = a.organization_member_id AND o.item_type = 'PRODUCT' AND o.product_id = a.product_id);
