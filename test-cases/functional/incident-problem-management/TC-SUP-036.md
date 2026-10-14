# TC-SUP-036: Migration V041: fresh load, upgrade and re-run; existing incidents get a phase

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-036 |
| Requirement ID (required) | [REQ-SUP-003](../../../docs/02-requirements/FRD/incident-problem-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-16](../../../docs/02-requirements/FRD/incident-problem-management/acceptance-criteria.md) |
| Business rule(s) | [BR-INC-018](../../../docs/02-requirements/FRD/incident-problem-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-003](TESTPLAN-SUP-003.md) |
| Priority | P1 |
| Type | Functional (manual) |
| Automated | No |

## Preconditions
PostgreSQL 16; the previous schema (with a resolved and an unresolved row in `service_incident`) and `database/migrations/V041__service_instances_sla_incidents_problems.sql`.

## Steps
1. Load `schema.sql` into an empty database.
2. On a database with the previous schema and incident rows, apply V041.
3. Apply V041 a second time.
4. Read `phase` of the old incident rows.

## Expected Result
Steps 1 to 3 succeed with no error; step 4: the incident with an end time is RESOLVED, the other INVESTIGATING, severity MINOR.

## Automated coverage
None. This case is manual only.

Steps 1 to 3 were validated on PostgreSQL 16 when the migration was built (a fresh schema load; the previous schema plus V041; V041 twice). The backfill result of step 4 was not separately recorded. Manual only: no automated test.

## Actual Result
Steps 1 to 3: validated by the build on 2026-10-14. Step 4: Not Run.

## Status
Partly passed: steps 1 to 3 validated on PostgreSQL 16 (2026-10-14); step 4 Not Run

## Linked Defect (if failed)
None.
