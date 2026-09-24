# SPRINT-2026.4.2

| Field | Value |
|---|---|
| Sprint ID (PI.Sprint) | 2026.4.2 |
| PI – CY Quarter | 2026.4 |
| Start / end dates | Not specified |
| Sprint goal | Not specified |
| Team / capacity | Not specified |
| Status | Not specified |
| Source | [PO] roadmap tables ([`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)) |
| Previous / next sprint | [2026.4.1](SPRINT-2026.4.1.md) · [2026.4.3](SPRINT-2026.4.3.md) |

## Scope

| Product | Application ID | Application | Roadmap item(s) | Source |
|---|---|---|---|---|
| EIS (PaaS) | 05 | [Customer / Tenant Management](../applications/05-customer-tenant-management.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |
| Thiran (SaaS) | Not specified | [SW Life Cycle](../applications/thiran-sw-life-cycle.md) | SWLC-CAP-12 Change & Configuration Management, SWLC-CAP-13 Release & Deployment Management | [PO] "SW Life Cycle - Roadmap Initiatives" |
| Thittam (SaaS) | Not specified | [Agile Planner](../applications/thittam-agile-planner.md) | AP-C11 Progress & Status Management, AP-C13 Metrics, Dashboards & Reporting | [PO] "Agile Planner - Roadmap Initiatives" |
| Thittam (SaaS) | Not specified | [Macro Planner](../applications/thittam-macro-planner.md) | #6 Goal & KPI Management, #8 Collaboration | [PO] "Macro Planner - Roadmap Initiatives" |

> [PO] assigns **one sprint per EIS application**. It does not say which capabilities or features fall inside this sprint, or whether the application must be finished in it. The EIS scope below is the application's full [WB] breakdown until sprint scope is decided (see [open-decisions.md](../open-decisions.md)).

## EIS 05 Customer / Tenant Management

**Planned work ([PO] / [WB] description):** Organizations, tenants, users and projects

Full breakdown with APIs, services, entities and events: [applications/05-customer-tenant-management.md](../applications/05-customer-tenant-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | MVP functions |
|---|---|---|---|
| [05.01 Organization Management](../applications/05-customer-tenant-management.md#0501-organization-management) | 05.01.01 Organization Lifecycle | Create organization; Update organization; Suspend organization; Activate organization; Close organization | - |
| [05.02 Tenant Management](../applications/05-customer-tenant-management.md#0502-tenant-management) | 05.02.01 Tenant Lifecycle | Create tenant; Configure tenant; Assign region; Configure isolation; Configure tenant policies | - |
| [05.03 User Management](../applications/05-customer-tenant-management.md#0503-user-management) | 05.03.01 User Lifecycle | Invite user; Create user; Activate user; Suspend user; Remove user | - |
| [05.03 User Management](../applications/05-customer-tenant-management.md#0503-user-management) | 05.03.02 Role Assignment | Assign role; Assign group; Review access | - |
| [05.04 Group & Project Management](../applications/05-customer-tenant-management.md#0504-group--project-management) | 05.04.01 Groups | Create group; Add member; Remove member | - |
| [05.04 Group & Project Management](../applications/05-customer-tenant-management.md#0504-group--project-management) | 05.04.02 Projects | Create project; Assign users; Assign resources | - |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - UJ-004 Tenant Onboarding (Identity, Tenant, IAM) (applications 06; [WB:User Journeys])
  - EVT-002 TenantCreated is consumed by Provisioning and Audit (applications 09, 15; [WB:Events])

### Expected deliverables

- Not specified in any source.
- Implied by [WB:Traceability]: APIs `API-003 POST /v1/tenants`; services Tenant Service.

### Related code already in this repository

Observed on branch `dev`, module level only: `backend/…/modules/registration (organization, members, seats)`; `frontend/src/pages/register/OrganizationRegisterPage.tsx`, `frontend/src/pages/register/VerifyEmailPage.tsx`, `frontend/src/pages/admin/RegistrationsAdminPage.tsx`.

## Hosted SaaS products in this sprint

These are tracked here so their dependencies on EIS are visible. [PO] lists no functions, requirements or deliverables for them.

| Product | Application | ID | Capability | Features ([PO]) | PI stated |
|---|---|---|---|---|---|
| Thittam (SaaS) | [Macro Planner](../applications/thittam-macro-planner.md) | #6 | Goal & KPI Management | KPI definition, KPI target, Actual, Forecast, Threshold, Variance, Trend | 2026.4 |
| Thittam (SaaS) | [Macro Planner](../applications/thittam-macro-planner.md) | #8 | Collaboration | Comments, Mentions, Discussions, Notifications, Activity feed, @user, @team | 2026.4 |
| Thittam (SaaS) | [Agile Planner](../applications/thittam-agile-planner.md) | AP-C11 | Progress & Status Management | Not specified | 2026.4 |
| Thittam (SaaS) | [Agile Planner](../applications/thittam-agile-planner.md) | AP-C13 | Metrics, Dashboards & Reporting | Not specified | 2026.4 |
| Thiran (SaaS) | [SW Life Cycle](../applications/thiran-sw-life-cycle.md) | SWLC-CAP-12 | Change & Configuration Management | Not specified | 2026.4 |
| Thiran (SaaS) | [SW Life Cycle](../applications/thiran-sw-life-cycle.md) | SWLC-CAP-13 | Release & Deployment Management | Not specified | 2026.4 |

**Stated dependencies ([PO]):**
- SW Life Cycle integrates with Macro Planner and Agile Planner.

## Open issues affecting this sprint

- C8: EIS 05 Customer / Tenant Management arrives after the SaaS tenancy capabilities scheduled in 2026.3.3
- The sprint scope, deliverables, acceptance criteria and dates are not specified in any source.

Details: [open-decisions.md](../open-decisions.md).

## Traceability

```
[PO] roadmap row → SPRINT-2026.4.2 → application page → capability → feature → function (requirement candidate)
   → FRD docs/02-requirements/FRD/<feature>/ (not created) → REQ-<APP-CODE>-<NNN> (not created)
   → STORY-<APP-CODE>-<NNN> with "Sprint (PI.Sprint)" = 2026.4.2
   → code (backend/ · frontend/ · ai-service/) → TC-<APP-CODE>-<NNN> in test-cases/ → UAT
```

Stories for this sprint use `docs/02-requirements/functional-requirements/user-story-template.md` or the GitHub **User Story** issue template, with **Sprint (PI.Sprint) = 2026.4.2**. The Definition of Done is the one in the story template.

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.
