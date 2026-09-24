# Thiran: SW Life Cycle

| Field | Value |
|---|---|
| Product | Thiran (SaaS), hosted on the EIS PaaS ([PO]) |
| Application | SW Life Cycle |
| Application ID | Not specified |

**Product description ([PO] "Thiran (SaaS Product)"):** "Thiran is a SaaS product that will be hosted on a Platform (PaaS). The product offers multiple applications to efficiently develop and execute Projects, Products, Production orders, etc. for an organization, division, unit, team(s), individuals by configurable workflow and enables tracking the progress and on time completion."

**Application description ([PO] "SW Life Cycle"):**
- "SW Life Cycle Application" will be hosted on an in-house Platform (PaaS).
- It integrates with **Macro Planner**, "to derive the Product - Application - Capabilities - Features breakdown".
- It integrates with **Agile Planner (SaaS application)**, "to derive the Features, Functions, Backlog, Sprints, Team Assignment, planned release, etc."
- It must be scalable and configurable (multilingual, multi-regional, marketplace options, integration with other platforms, SaaS applications, etc.).
- Teams use it to "create, manage and release Requirements, Design, API specs, Test Cases, AI agents, Approvals, Traceability, workflow, etc."
- AI integration: automatically generate code, design diagrams, test cases, reports and dashboards; perform automated testing; deployment.

## Capabilities and roadmap

Source: [PO] "SW Life Cycle Capabilities & Features" and "SW Life Cycle - Roadmap Initiatives".

| ID | Capability | Features | PI – CY Quarter | Sprint |
|---|---|---|---|---|
| SWLC-CAP-01 | Platform & Tenant Management | Not specified | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| SWLC-CAP-02 | Organization & Project Management | Not specified | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| SWLC-CAP-03 | Lifecycle / Work Item Management | Not specified | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| SWLC-CAP-04 | Requirements Management | Not specified | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| SWLC-CAP-05 | Architecture & Design Management | Not specified | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| SWLC-CAP-06 | API & Interface Management | Not specified | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| SWLC-CAP-07 | Software Configuration / Code Management | Not specified | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| SWLC-CAP-08 | Test Management | Not specified | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| SWLC-CAP-09 | Defect & Issue Management | Not specified | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| SWLC-CAP-10 | Traceability & Impact Analysis | Not specified | 2026.3 | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| SWLC-CAP-11 | Workflow & Approval Management | Not specified | 2026.4 | [2026.4.1](../sprints/SPRINT-2026.4.1.md) |
| SWLC-CAP-12 | Change & Configuration Management | Not specified | 2026.4 | [2026.4.2](../sprints/SPRINT-2026.4.2.md) |
| SWLC-CAP-13 | Release & Deployment Management | Not specified | 2026.4 | [2026.4.2](../sprints/SPRINT-2026.4.2.md) |
| SWLC-CAP-14 | Documentation Management | Not specified | 2026.4 | [2026.4.3](../sprints/SPRINT-2026.4.3.md) |
| SWLC-CAP-15 | Risk & Compliance Management | Not specified | 2026.4 | [2026.4.3](../sprints/SPRINT-2026.4.3.md) |
| SWLC-CAP-16 | Reporting & Analytics | Not specified | 2027.1 | [2027.1.1](../sprints/SPRINT-2027.1.1.md) |
| SWLC-CAP-17 | Collaboration & Review | Not specified | 2027.1 | [2027.1.1](../sprints/SPRINT-2027.1.1.md) |
| SWLC-CAP-18 | AI Engineering Platform | Not specified | 2027.1 | [2027.1.2](../sprints/SPRINT-2027.1.2.md) |
| SWLC-CAP-19 | Automation Platform | Not specified | 2027.1 | [2027.1.2](../sprints/SPRINT-2027.1.2.md) |
| SWLC-CAP-20 | Integration & Marketplace | Not specified | 2027.1 | [2027.1.3](../sprints/SPRINT-2027.1.3.md) |
| SWLC-CAP-21 | Templates & Methodologies | Not specified | 2027.1 | [2027.1.3](../sprints/SPRINT-2027.1.3.md) |
| SWLC-CAP-22 | Administration & Governance | Not specified | 2027.1 | [2027.1.3](../sprints/SPRINT-2027.1.3.md) |
| SWLC-CAP-23 | Localization & Regionalization | Not specified | 2027.1 | [2027.1.3](../sprints/SPRINT-2027.1.3.md) |

⚠ C1: the sprint ID is not inside the stated PI. See [open-decisions.md](../open-decisions.md).

## Functions, requirements, deliverables

Not specified in any source.

## Dependencies

- **Stated:** integrates with Macro Planner and Agile Planner ([PO]).
- **Overlap with EIS platform capabilities (not stated as a dependency):** organization, identity, tenant, security, audit, billing and subscription capabilities duplicate EIS applications 05, 06, 07, 08 and 15. See C8 in [open-decisions.md](../open-decisions.md).

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.
