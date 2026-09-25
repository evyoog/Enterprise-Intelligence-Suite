# SPRINT-2026.4.2

| Field | Value |
|---|---|
| Sprint ID (PI.Sprint) | 2026.4.2 |
| PI – CY Quarter | 2026.4 |
| Start / end dates | 1–30 Nov 2026 ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Sprint goal | Not specified |
| Team / capacity | Not specified |
| Status | Not specified |
| Source | [PO] roadmap table ([`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)); decisions in [open-decisions.md](../open-decisions.md) |
| Previous / next sprint | [2026.4.1](SPRINT-2026.4.1.md) · [2026.4.3](SPRINT-2026.4.3.md) |

## Scope

| Application ID | Code | Application | Roadmap item | Source |
|---|---|---|---|---|
| 05 | `APP-TEN` | [Customer / Tenant Management](../applications/05-customer-tenant-management.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |

> **Commitment ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)):** this sprint commits the P0 (MVP) capabilities of its applications and treats P1 capabilities as stretch scope. Anything not finished is recorded as carry-over on the next sprint page.

## EIS 05 Customer / Tenant Management

**Planned work ([PO] / [WB] description):** Organizations, tenants, users and projects

Full breakdown with APIs, services, entities and events: [applications/05-customer-tenant-management.md](../applications/05-customer-tenant-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [05.01 Organization Management](../applications/05-customer-tenant-management.md#0501-organization-management) | 05.01.01 Organization Lifecycle | Create organization; Update organization; Suspend organization; Activate organization; Close organization | P0 | Commit |
| [05.02 Tenant Management](../applications/05-customer-tenant-management.md#0502-tenant-management) | 05.02.01 Tenant Lifecycle | Create tenant; Configure tenant; Assign region; Configure isolation; Configure tenant policies | P0 | Commit |
| [05.03 User Management](../applications/05-customer-tenant-management.md#0503-user-management) | 05.03.01 User Lifecycle | Invite user; Create user; Activate user; Suspend user; Remove user | P0 | Commit |
| [05.03 User Management](../applications/05-customer-tenant-management.md#0503-user-management) | 05.03.02 Role Assignment | Assign role; Assign group; Review access | P0 | Commit |
| [05.04 Group & Project Management](../applications/05-customer-tenant-management.md#0504-group--project-management) | 05.04.01 Groups | Create group; Add member; Remove member | P0 | Commit |
| [05.04 Group & Project Management](../applications/05-customer-tenant-management.md#0504-group--project-management) | 05.04.02 Projects | Create project; Assign users; Assign resources | P0 | Commit |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - UJ-004 Tenant Onboarding (Identity, Tenant, IAM) (applications 06; [WB:User Journeys])
  - EVT-002 TenantCreated is consumed by Provisioning and Audit (applications 09, 15; [WB:Events])

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). Deliverables are otherwise Not specified in any source.

### Related code already in this repository

Observed on branch `dev`, module level only: `backend/…/modules/registration (organization, members, seats)`; `frontend/src/pages/register/OrganizationRegisterPage.tsx`, `frontend/src/pages/register/VerifyEmailPage.tsx`, `frontend/src/pages/admin/RegistrationsAdminPage.tsx`.

## Decisions affecting this sprint

- [C3](../open-decisions.md#c3) The [PO] sprint order is authoritative; the MVP is complete at the end of sprint 2027.1.3.
- [C4](../open-decisions.md#c4), [C5](../open-decisions.md#c5), [C6](../open-decisions.md#c6) MVP, priority and phase as shown above.
- Sprint goal, team, capacity and status are Not specified.

Details: [open-decisions.md](../open-decisions.md).

## Traceability

```
[PO] roadmap row → SPRINT-2026.4.2 → application page → capability → feature → function (requirement candidate)
   → FRD docs/02-requirements/FRD/<feature>/ → REQ-<CODE>-<NNN> (Approved before build)
   → STORY-<CODE>-<NNN> with "Sprint (PI.Sprint)" = 2026.4.2
   → code (backend/ · frontend/ · ai-service/) → TC-<CODE>-<NNN> in test-cases/ → UAT
```

Stories for this sprint use `docs/02-requirements/functional-requirements/user-story-template.md` or the GitHub **User Story** issue template, with **Sprint (PI.Sprint) = 2026.4.2**. The Definition of Done is the one in the story template.

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)
- **Decisions:** [`open-decisions.md`](../open-decisions.md) (2026-09-25)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source or decision gives are written **Not specified**.
