# Thittam: Agile Planner

| Field | Value |
|---|---|
| Product | Thittam (SaaS), hosted on the EIS PaaS ([PO]) |
| Application | Agile Planner |
| Application ID | Not specified |

**Product description ([PO] "Thittam (SaaS Product)"):** "Thittam is a SaaS product that will be hosted on a Platform (PaaS). The product offers multiple planning applications supporting high level planning for an organization, division, unit, team(s), individuals and tracking of progress and completion."

**Vision ([PO]):** "The applications are scalable and configurable extending to multi lingual, multi-regional, marketplace options, integration with other platforms, SaaS applications, etc."

## Capabilities and roadmap

Source: [PO] "Agile Planner Capabilities & Features" and "Agile Planner - Roadmap Initiatives".

| ID | Capability | Features | PI – CY Quarter | Sprint |
|---|---|---|---|---|
| AP-C01 | Organization & Tenant Management | Multi-Tenant Organization, User Management, Role & Permission Management | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| AP-C02 | Portfolio / Program Management | Portfolio, Program, Goal Management | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| AP-C03 | Application / Product Management | Application, Product Registry, Product Planning | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| AP-C04 | Capability & Feature Management | Capability Management, Feature Management | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| AP-C05 | Function & Backlog Management | Function Management | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| AP-C06 | Agile Planning & Sprint Management | Sprint Management | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| AP-C07 | Team & Resource Management | Teams | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| AP-C08 | Board Management | BOARD | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| AP-C09 | Workflow & State Management | Configurable workflow engine | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| AP-C10 | Work Assignment & Collaboration | Assign work, Reassign work, Followers, Comments, Mentions, Attachments, Checklist, Activity history, Notifications, Work log, Time tracking, Approval, Escalation | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| AP-C11 | Progress & Status Management | Not specified | 2026.4 | [2026.4.2](../sprints/SPRINT-2026.4.2.md) |
| AP-C12 | Dependency & Risk Management | Work dependency, Feature dependency, Team dependency, Application dependency, External dependency, Blocking relationship, Risk, Issue, Assumption, Decision, Escalation | 2026.4 | [2026.4.3](../sprints/SPRINT-2026.4.3.md) |
| AP-C13 | Metrics, Dashboards & Reporting | Not specified | 2026.4 | [2026.4.2](../sprints/SPRINT-2026.4.2.md) |
| AP-C14 | ALM Integration & Traceability | Not specified | 2026.4 | [2027.4.1](../sprints/SPRINT-2027.4.1.md) ⚠ C1 |
| AP-C15 | MACRO PLANNER Integration | Not specified | 2026.4 | [2027.4.1](../sprints/SPRINT-2027.4.1.md) ⚠ C1 |
| AP-C16 | Automation & Notifications | Not specified | 2027.1 | [2027.1.1](../sprints/SPRINT-2027.1.1.md) |
| AP-C17 | Administration, Configuration & Security | Not specified | 2027.1 | [2027.1.2](../sprints/SPRINT-2027.1.2.md) |
| AP-C18 | Marketplace / Integration Platform | Not specified | 2027.1 | [2027.1.3](../sprints/SPRINT-2027.1.3.md) |

⚠ C1: the sprint ID is not inside the stated PI. See [open-decisions.md](../open-decisions.md).

## Functions, requirements, deliverables

Not specified in any source.

## Dependencies

- **Stated:** AP-C15 "MACRO PLANNER Integration" ([PO]).
- **Stated:** SW Life Cycle uses Agile Planner to derive Features, Functions, Backlog, Sprints, Team Assignment and planned release ([PO]).
- **Overlap with EIS platform capabilities (not stated as a dependency):** organization, identity, tenant, security, audit, billing and subscription capabilities duplicate EIS applications 05, 06, 07, 08 and 15. See C8 in [open-decisions.md](../open-decisions.md).

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.
