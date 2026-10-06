# TC-KNW-040: Knowledge Center home

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-040 |
| Requirement ID (required) | [REQ-KNW-005](../../../docs/02-requirements/FRD/knowledge-center/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/knowledge-center/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | UI |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Open /knowledge-base (bookmark) signed out and signed in.

## Expected Result
Header, hero, quick access with counts, recommended, products, popular guides, featured videos; signed in also the learning panel.

## Automated coverage
- `frontend/src/pages/knowledge/KnowledgeCenter.test.tsx` — `shows the hero, quick access, recommended, products and videos`
- `frontend/src/pages/knowledge/KnowledgeCenter.test.tsx` — `sends old Knowledge Base links to the Knowledge Center`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
