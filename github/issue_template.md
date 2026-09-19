<!--
GITHUB ISSUE TEMPLATE (interim Agile Planner substitute)
Place at .github/ISSUE_TEMPLATE/story.md in each repository.
Mirrors the Story template so backlog items created directly in GitHub Issues stay ID-consistent
with items authored in user-story-template.md, for a clean future migration into Agile Planner.
-->
---
name: User Story
about: A unit of work traceable to a Feature and Requirement
title: "[STORY-<APP-CODE>-<NNN>] <Short Title>"
labels: story
---

**Feature:** FTR-<APP-CODE>-<NNN>
**Requirement:** REQ-<APP-CODE>-<NNN>
**Sprint (PI.Sprint):** <e.g. 2026.4.1>
**Priority:** P0 / P1 / P2

### Story
As a **<persona>**, I want **<capability>**, so that **<benefit>**.

### Acceptance Criteria
- [ ] AC1:
- [ ] AC2:

### Tasks
- [ ] Database schema
- [ ] API
- [ ] Authorization
- [ ] UI / integration
- [ ] Unit tests
- [ ] Integration tests

### Definition of Done
- [ ] PR merged referencing this Story's IDs
- [ ] Tests passing, quality gates green
- [ ] Traceability record updated
