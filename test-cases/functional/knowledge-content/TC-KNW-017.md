# TC-KNW-017: Blocks are validated and never hold script links

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-017 |
| Requirement ID (required) | [REQ-KNW-002](../../../docs/02-requirements/FRD/knowledge-content/requirement.md) |
| Acceptance Criterion | BR-KCON-008 |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Save a link block with javascript:, an unknown block type and an external direct action.

## Expected Result
Each is refused with INVALID_CONTENT; text containing <script> is shown as text.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeContentServiceTest.java` — `blocksAreValidatedAndNeverHoldHtmlLinks`
- `frontend/src/pages/knowledge/KnowledgeCenter.test.tsx` — `renders blocks as text, links glossary terms and asks signed-out readers to sign in for feedback`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
