# TC-KNW-023: Granting or removing a knowledge permission is audited

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-023 |
| Requirement ID (required) | [REQ-KNW-008](../../../docs/02-requirements/FRD/knowledge-permissions/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/knowledge-permissions/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | No (manual / UAT) |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
On Roles & permissions add KNOWLEDGE_CONTRIBUTE to a role, then remove it.

## Expected Result
Both changes appear in the audit log (existing role administration audit).

## Manual / UAT
- UAT-KNW-001 step 1

## Actual Result
Not run yet.

## Status
Pending UAT
