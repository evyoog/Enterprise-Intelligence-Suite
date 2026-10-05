# TC-KNW-016: A module in use cannot be deleted, only deactivated

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-016 |
| Requirement ID (required) | [REQ-KNW-002](../../../docs/02-requirements/FRD/knowledge-content/requirement.md) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/knowledge-content/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Delete a module that has content; then deactivate it.

## Expected Result
Delete answers 409 IN_USE; deactivation works.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeContentServiceTest.java` — `moduleInUseCannotBeDeletedOnlyDeactivated`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
