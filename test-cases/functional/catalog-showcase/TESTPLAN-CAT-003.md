# TESTPLAN-CAT-003: Catalog Showcase

| Field | Value |
|---|---|
| Requirement | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) |
| Sprint | [2026.4.1](../../../docs/01-business/roadmap/sprints/SPRINT-2026.4.1.md) |
| Decision | [C66](../../../docs/01-business/roadmap/open-decisions.md#c66) |
| Scope | Public catalog API and pages, platform and app forms, All Apps, Platforms list, accessibility |
| Out of scope | Screens that only inherit the theme (C66 *Not redesigned*) — covered by their own existing tests |
| Run | `cd backend && mvn -B verify`; `cd frontend && npm test` |

| Test case | Criterion | Title | Status |
|---|---|---|---|
| [TC-CAT-011](TC-CAT-011.md) | AC-1 | The public catalog lists only visible platforms | Passed |
| [TC-CAT-012](TC-CAT-012.md) | AC-2 | Showcase fields are validated and stored | Passed |
| [TC-CAT-013](TC-CAT-013.md) | AC-3 | The Product Catalog shows real data, filters and states | Passed |
| [TC-CAT-014](TC-CAT-014.md) | AC-4 | Platform details page | Passed |
| [TC-CAT-015](TC-CAT-015.md) | AC-5 | Platform form live preview and validation | Passed |
| [TC-CAT-016](TC-CAT-016.md) | AC-6 | App form sections, existing rules and new fields | Passed |
| [TC-CAT-017](TC-CAT-017.md) | AC-7 | All Apps filters, create and delete confirmation | Passed |
| [TC-CAT-018](TC-CAT-018.md) | AC-8 | Accessibility and colour contrast | Passed |
