# TC-KNW-049: Accessibility of Knowledge Center and Knowledge Management

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-049 |
| Requirement ID (required) | [REQ-KNW-005](../../../docs/02-requirements/FRD/knowledge-center/requirement.md) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/knowledge-center/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | UI |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Run axe on home, article, search, FAQs, editor, video editor, upload progress; use the keyboard only; set reduced motion.

## Expected Result
No axe violations; all actions reachable by keyboard; animations stop.

## Automated coverage
- `frontend/src/pages/knowledge/KnowledgeCenter.test.tsx` — `shows the hero, quick access, recommended, products and videos`
- `frontend/src/pages/knowledge-admin/KnowledgeManagement.test.tsx` — `lets a contributor draft and submit, but shows no publisher actions`
- `frontend/src/pages/knowledge-admin/KnowledgeManagement.test.tsx` — `adds and reorders blocks without a mouse`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
