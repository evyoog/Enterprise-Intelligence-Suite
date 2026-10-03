# TESTPLAN-PRT-003: Business dashboard (C69)

| Field | Value |
|---|---|
| Decision | [C69](../../../docs/01-business/roadmap/open-decisions.md#c69) |
| Screen | [business-dashboard.md](../../../docs/05-ui/screen-requirements/business-dashboard.md) |
| Sprint | [2026.4.1](../../../docs/01-business/roadmap/sprints/SPRINT-2026.4.1.md) |
| Scope | Routing, data from existing APIs, interactions, attention, account, resilience, motion, accessibility |
| Out of scope | Launches over time (no data, C69); the moved organization cards keep their own existing tests |
| Run | `cd frontend && npm test` |

| Test case | Title | Status |
|---|---|---|
| [TC-PRT-014](TC-PRT-014.md) | Audience routing and errors | Passed |
| [TC-PRT-015](TC-PRT-015.md) | Header, welcome, status and real KPIs | Passed |
| [TC-PRT-016](TC-PRT-016.md) | Analytics and table interactions | Passed |
| [TC-PRT-017](TC-PRT-017.md) | Your account, workspace and activity | Passed |
| [TC-PRT-018](TC-PRT-018.md) | Attention, status and billing from real data | Passed |
| [TC-PRT-019](TC-PRT-019.md) | Resilience, motion and accessibility | Passed |
