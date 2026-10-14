# TC-BIL-118: AC-48 Migration V039 adds the columns for administrator-added gateways without changing existing data (manual)

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-118 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.16) |
| Acceptance Criterion | [AC-48](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-21](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | None (added by C99) |
| Decision | [C99](../../../docs/01-business/roadmap/open-decisions.md#c99) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P0 |
| Type | Regression |
| Automated | No |

## Preconditions
A PostgreSQL 16 copy of the schema at V038 with the Razorpay row.

## Steps
1. Apply `database/migrations/V039__payment_gateway_custom_definitions.sql`.
2. Read the columns of `eis_platform.payment_gateway_config` and the Razorpay row.
3. Apply V039 again.
4. Start the backend, open Payment Gateways and add a gateway with a credential value.
5. Read the new row.

## Expected Result
`custom` (boolean, not null, default false), `display_name` (varchar 100) and `definition` (text) exist; the Razorpay row has `custom` false and its other values unchanged; the second run has no effect and no error. The new gateway row has `custom` true, `display_name`, a `definition` JSON with no credential value and a non-empty `credentials_encrypted`.

## Automated coverage
None. This case is manual only.

Manual only: the migration SQL is not executed by the automated tests (they run on in-memory H2, built from the entities, not PostgreSQL). **Not Run.**

## Actual Result
Not run. Needs the environment described under Preconditions.

## Status
Not Run

## Linked Defect (if failed)
None.
