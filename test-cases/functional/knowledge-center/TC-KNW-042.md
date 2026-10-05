# TC-KNW-042: Organization-restricted content never reaches other organizations

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-042 |
| Requirement ID (required) | [REQ-KNW-005](../../../docs/02-requirements/FRD/knowledge-center/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/knowledge-center/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Publish content for organization 501; browse, search and open its URL as 502.

## Expected Result
Never listed or found; direct URL 404.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeContentServiceTest.java` — `audienceIsAppliedBeforeAnythingIsReturned`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
