# TC-TEN-037: Organization hierarchy — audit (AC-11)

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-037 |
| Requirement ID (required) | [REQ-TEN-006](../../../docs/02-requirements/FRD/org-hierarchy/requirement.md) |
| Acceptance Criterion | [AC-11](../../../docs/02-requirements/FRD/org-hierarchy/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-006](TESTPLAN-TEN-006.md) |
| Priority | P1 |
| Type | Functional |
| Automated | No (manual) |

## Steps
1. Create, edit, move, deactivate, delete a node, import a CSV, change levels and place a member.
2. As a platform administrator open the audit log.

## Expected Result
One entry per change with actions `ORG_NODE_CREATED`, `ORG_NODE_UPDATED`, `ORG_NODE_MOVED`, `ORG_NODE_DEACTIVATED`, `ORG_NODE_DELETED`, `ORG_NODE_IMPORTED`, `ORG_LEVELS_CHANGED`, `ORG_NODE_MEMBER_PLACED`.

## Status
Not run (needs a running environment; the service calls `AuditService.recordSuccess` for each, covered indirectly by the tests above).
