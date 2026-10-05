# TC-KNW-008: Old Knowledge Base links open the Knowledge Center

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-008 |
| Requirement ID (required) | [REQ-KNW-001](../../../docs/02-requirements/FRD/knowledge-base/requirement.md) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/knowledge-base/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P1 |
| Type | UI |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Open /knowledge-base and /knowledge-base?article=12.

## Expected Result
The Knowledge Center home opens; the article link opens /knowledge/content/12.

## Automated coverage
- `frontend/src/pages/knowledge/KnowledgeCenter.test.tsx` — `sends old Knowledge Base links to the Knowledge Center`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
