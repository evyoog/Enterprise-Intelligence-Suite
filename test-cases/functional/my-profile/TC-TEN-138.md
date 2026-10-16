# TC-TEN-138: My profile — schema and migration V046

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-138 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-014) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P1 |
| Type | Functional |
| Automated | No (manual check) |

## Preconditions
A PostgreSQL 16 database at V045.

## Steps
1. Apply `database/migrations/V046__customer_profile.sql` and compare with `backend/src/main/resources/db/schema.sql`.
2. Check that the 12 new `customer` columns exist, are nullable and have the stated lengths, and that existing rows hold null.
3. Insert a photo for a customer, then delete the customer.

## Expected Result
The new columns and `customer_photo` exist and match `schema.sql`; no existing column length was reduced; existing customers keep working with null in the new columns; deleting the customer deletes its photo (cascade). The automated tests load the same schema on H2; the PostgreSQL run is a manual check.

## Automated coverage
- None: manual check.

## Actual Result
Not run yet.

## Status
Not Run
