# 05 Customer / Tenant Management

| Field | Value |
|---|---|
| Application ID | 05 ([WB] numbering; an `APP-<CODE>` code is not assigned in any source) |
| Application | Customer / Tenant Management |
| Description | Organizations, tenants, users and projects ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2026.4 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2026.4.2](../sprints/SPRINT-2026.4.2.md) |
| Capabilities / features / functions | 4 / 6 / 24 ([WB]) |
| Application status | Not specified |

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.

## Capabilities

| Capability ID | Capability | Features | Priority | MVP | AI relevant |
|---|---|---|---|---|---|
| [05.01](#0501-organization-management) | Organization Management | 05.01.01 Organization Lifecycle | P0 | Yes | No |
| [05.02](#0502-tenant-management) | Tenant Management | 05.02.01 Tenant Lifecycle | P0 | Yes | No |
| [05.03](#0503-user-management) | User Management | 05.03.01 User Lifecycle, 05.03.02 Role Assignment | P0 | Yes | No |
| [05.04](#0504-group--project-management) | Group & Project Management | 05.04.01 Groups, 05.04.02 Projects | P0 | Yes | No |

> The Priority and MVP values are copied from [WB:Capabilities]. Every capability in [WB] is P0 / MVP=Yes, which conflicts with the function-level MVP flags (C4 in [open-decisions.md](../open-decisions.md)).

## 05.01 Organization Management

### Feature 05.01.01 Organization Lifecycle

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 05.01.01.01 | Create organization | No | No | Platform Service | `/organization-management/create-organization` | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.01.01.02 | Update organization | No | No | Platform Service | `/organization-management/update-organization` | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.01.01.03 | Suspend organization | No | No | Platform Service | `/organization-management/suspend-organization` | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.01.01.04 | Activate organization | No | No | Platform Service | `/organization-management/activate-organization` | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.01.01.05 | Close organization | No | No | Platform Service | `/organization-management/close-organization` | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |

## 05.02 Tenant Management

### Feature 05.02.01 Tenant Lifecycle

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 05.02.01.01 | Create tenant | No | No | Platform Service | `/tenant-management/create-tenant` | API-003 POST /v1/tenants | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.02.01.02 | Configure tenant | No | No | Platform Service | `/tenant-management/configure-tenant` | API-003 POST /v1/tenants | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.02.01.03 | Assign region | No | No | Platform Service | `/tenant-management/assign-region` | API-003 POST /v1/tenants | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.02.01.04 | Configure isolation | No | No | Platform Service | `/tenant-management/configure-isolation` | API-003 POST /v1/tenants | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.02.01.05 | Configure tenant policies | No | No | Platform Service | `/tenant-management/configure-tenant-policies` | API-003 POST /v1/tenants | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |

## 05.03 User Management

### Feature 05.03.01 User Lifecycle

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 05.03.01.01 | Invite user | No | No | Platform Service | `/user-management/invite-user` | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.03.01.02 | Create user | No | No | Platform Service | `/user-management/create-user` | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.03.01.03 | Activate user | No | No | Platform Service | `/user-management/activate-user` | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.03.01.04 | Suspend user | No | No | Platform Service | `/user-management/suspend-user` | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.03.01.05 | Remove user | No | No | Platform Service | `/user-management/remove-user` | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |

### Feature 05.03.02 Role Assignment

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 05.03.02.01 | Assign role | No | No | Platform Service | `/user-management/assign-role` | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.03.02.02 | Assign group | No | No | Platform Service | `/user-management/assign-group` | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.03.02.03 | Review access | No | No | Platform Service | `/user-management/review-access` | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |

## 05.04 Group & Project Management

### Feature 05.04.01 Groups

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 05.04.01.01 | Create group | No | No | Platform Service | `/group-project-management/create-group` | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.04.01.02 | Add member | No | No | Platform Service | `/group-project-management/add-member` | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.04.01.03 | Remove member | No | No | Platform Service | `/group-project-management/remove-member` | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |

### Feature 05.04.02 Projects

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 05.04.02.01 | Create project | No | No | Platform Service | `/group-project-management/create-project` | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.04.02.02 | Assign users | No | No | Platform Service | `/group-project-management/assign-users` | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.04.02.03 | Assign resources | No | No | Platform Service | `/group-project-management/assign-resources` | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 06 | UJ-004 Tenant Onboarding (Identity, Tenant, IAM) | [WB:User Journeys] |
| 09, 15 | EVT-002 TenantCreated is consumed by Provisioning and Audit | [WB:Events] |

## Deliverables

Not specified in any source. [WB:Traceability] links these functions to the API and microservice columns in the tables above; those are the nearest implied deliverables.

## Requirements, design and tests

| Artifact | Location | Status |
|---|---|---|
| Feature FRD | `docs/02-requirements/FRD/<feature>/` (copy `_template/`) | Not created |
| Requirement | `docs/02-requirements/functional-requirements/REQ-<APP-CODE>-<NNN>.md` | Not created. No REQ-IDs exist in the sources |
| Business rules | `docs/03-business-rules/` | Not specified in the sources |
| Test cases | `test-cases/functional/<feature>/TC-<APP-CODE>-<NNN>.md` | Not created |
| NFRs | [WB:Non-Functional Requirements] NFR-001 to NFR-014 (platform-wide) | See [EIS-document-analysis.md](../EIS-document-analysis.md) section 2.17 |

## Related code already in this repository

Observed on branch `dev`. This is a module-level mapping, not a verified function-by-function implementation status.

- Backend: `backend/src/main/java/com/vyoog/eisplatform/modules/registration (organization, members, seats)`
- Frontend: `frontend/src/pages/register/OrganizationRegisterPage.tsx`, `frontend/src/pages/register/VerifyEmailPage.tsx`, `frontend/src/pages/admin/RegistrationsAdminPage.tsx`
