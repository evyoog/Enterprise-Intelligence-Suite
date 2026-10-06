# TC-KNW-014: Expired and not-yet-effective content is hidden

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-014 |
| Requirement ID (required) | [REQ-KNW-002](../../../docs/02-requirements/FRD/knowledge-content/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/knowledge-content/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Publish an item whose expiry date has passed, and one whose effective date is in the future.

## Expected Result
Neither is shown to readers; the admin list marks the first Expired.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeContentServiceTest.java` — `expiredAndNotYetEffectiveContentIsHidden`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
