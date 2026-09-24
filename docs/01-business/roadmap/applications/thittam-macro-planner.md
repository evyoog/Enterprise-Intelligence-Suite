# Thittam: Macro Planner

| Field | Value |
|---|---|
| Product | Thittam (SaaS), hosted on the EIS PaaS ([PO]) |
| Application | Macro Planner |
| Application ID | Not specified |

**Product description ([PO] "Thittam (SaaS Product)"):** "Thittam is a SaaS product that will be hosted on a Platform (PaaS). The product offers multiple planning applications supporting high level planning for an organization, division, unit, team(s), individuals and tracking of progress and completion."

**Vision ([PO]):** "The applications are scalable and configurable extending to multi lingual, multi-regional, marketplace options, integration with other platforms, SaaS applications, etc."

## Capabilities and roadmap

Source: [PO] "Macro Planner Capabilities & Features" and "Macro Planner - Roadmap Initiatives".

| ID | Capability | Primary purpose | Features | PI – CY Quarter | Sprint |
|---|---|---|---|---|---|
| #1 | Organization & Identity | Organization, divisions, units, teams, users, roles | Organization creation, Organization profile, Organization hierarchy, Division management, Business-unit management, Department management, Location management, Cost-center management, User provisioning, Authentication, Authorization, Roles, Permissions, Groups, SSO | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| #2 | Planning & Portfolio | Create/manage plans and planning hierarchy | Create plan, Plan types, Plan hierarchy, Plan versioning, Plan lifecycle, Plan ownership, Plan status, Hierarchical planning, Parent, child plans, Cross-plan relationships, Roll-up planning, Cascading targets, Cascading status, Annual planning, Quarterly planning, Monthly planning, Weekly planning, Fiscal calendars, Custom periods | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| #3 | Work Management | Tasks, activities, assignments, status, priorities | Tasks, Subtasks, Activities, Assignments, Priority, Status, Due dates, Tags, Checklists, Kanban, Planner board, List, Grid, Calendar, Timeline, Gantt | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| #4 | Schedule & Dependency | Dates, milestones, dependencies, critical path | Milestones, Gates, Deliverables, Approval points, Start, end dates, Duration, Calendar, Working days, Baselines, Forecast dates | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| #5 | Resource Management | People, teams, capacity, allocation | Team creation, Team membership, Team hierarchy, Team roles, Team responsibilities, Team capacity, Available capacity, Planned capacity, Allocated capacity, Utilization, Over-allocation | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| #6 | Goal & KPI Management | Objectives, KPIs, targets, actuals | KPI definition, KPI target, Actual, Forecast, Threshold, Variance, Trend | 2026.4 | [2026.4.2](../sprints/SPRINT-2026.4.2.md) |
| #7 | Template Management | Business/project/production/etc. plan templates | Business Plan, Sales Plan, Marketing Plan, HR Plan, Finance Plan, Production Plan, Purchase Plan, Delivery Plan, Project Plan, Product Plan, Training Plan, Strategic Plan, Operational Plan, Capacity Plan, Resource Plan, Quality Plan, Risk Plan, Compliance Plan | 2026.4 | [2026.4.3](../sprints/SPRINT-2026.4.3.md) |
| #8 | Collaboration | Comments, discussions, notifications, documents | Comments, Mentions, Discussions, Notifications, Activity feed, @user, @team | 2026.4 | [2026.4.2](../sprints/SPRINT-2026.4.2.md) |
| #9 | Workflow & Automation | Rules, approvals, triggers, automated actions | Event engine, Rules engine, Workflow engine, Approval engine, Notification engine, Scheduled jobs, Automation actions | 2026.4 | [2027.4.1](../sprints/SPRINT-2027.4.1.md) ⚠ C1 |
| #10 | Analytics & Reporting | Dashboards, reports, progress, variance | Gantt chart, Critical path, Baseline, Actual vs planned, Schedule variance | 2026.4 | [2027.4.1](../sprints/SPRINT-2027.4.1.md) ⚠ C1 |
| #11 | Integration & API | ERP, CRM, ALM, Agile, HR, finance, external SaaS | Not specified | 2027.1 | [2027.1.1](../sprints/SPRINT-2027.1.1.md) |
| #12 | Marketplace & Extensions | Apps, connectors, templates, plugins | Not specified | 2027.1 | [2027.1.3](../sprints/SPRINT-2027.1.3.md) |
| #13 | Administration & Configuration | Tenant/platform configuration | Not specified | 2027.1 | [2027.1.1](../sprints/SPRINT-2027.1.1.md) |
| #14 | Localization | Language, region, timezone, currency, calendar | Not specified | 2027.1 | [2027.1.2](../sprints/SPRINT-2027.1.2.md) |
| #15 | Platform Operations | Security, audit, monitoring, billing, subscription | Not specified | 2027.1 | [2027.1.3](../sprints/SPRINT-2027.1.3.md) |

⚠ C1: the sprint ID is not inside the stated PI. See [open-decisions.md](../open-decisions.md).

## Functions, requirements, deliverables

Not specified in any source.

## Dependencies

- **Stated:** SW Life Cycle uses Macro Planner to derive the Product → Application → Capabilities → Features breakdown ([PO]).
- **Stated:** Agile Planner AP-C15 "MACRO PLANNER Integration" ([PO]).
- **Overlap with EIS platform capabilities (not stated as a dependency):** organization, identity, tenant, security, audit, billing and subscription capabilities duplicate EIS applications 05, 06, 07, 08 and 15. See C8 in [open-decisions.md](../open-decisions.md).

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.
