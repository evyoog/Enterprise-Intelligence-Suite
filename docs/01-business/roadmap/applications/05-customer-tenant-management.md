# 05 Customer / Tenant Management

| Field | Value |
|---|---|
| Application ID | 05 ([WB] numbering) |
| Application code | `APP-TEN` ([DN-5](../open-decisions.md#dn-5-application-codes)); IDs use `TEN`, for example `REQ-TEN-001` |
| Application | Customer / Tenant Management |
| Description | Organizations, tenants, users and projects ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2026.4 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2026.4.2](../sprints/SPRINT-2026.4.2.md) (1–30 Nov 2026, [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Capabilities / features / functions | 4 / 6 / 24 ([WB]) |
| Application status | Not specified |

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)
- **Decisions:** [`open-decisions.md`](../open-decisions.md) (2026-09-25)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source or decision gives are written **Not specified**.

## Capabilities

MVP, priority and phase follow [C4](../open-decisions.md#c4), [C5](../open-decisions.md#c5) and [C6](../open-decisions.md#c6). The [WB] MVP, priority and phase columns are ignored. Where a capability is not placed in any [WB:Roadmap] workstream, its phase and priority are Not specified.

| Capability ID | Capability | Features | MVP | Priority | Phase | Basis |
|---|---|---|---|---|---|---|
| [05.01](#0501-organization-management) | Organization Management | 05.01.01 Organization Lifecycle | Yes | Phase 1 / MVP | P0 | C4 |
| [05.02](#0502-tenant-management) | Tenant Management | 05.02.01 Tenant Lifecycle | Yes | Phase 1 / MVP | P0 | C4 |
| [05.03](#0503-user-management) | User Management | 05.03.01 User Lifecycle, 05.03.02 Role Assignment | Yes | Phase 1 / MVP | P0 | C4 |
| [05.04](#0504-group--project-management) | Group & Project Management | 05.04.01 Groups, 05.04.02 Projects | Yes | Phase 1 / MVP | P0 | C4 |

## 05.01 Organization Management

### Feature 05.01.01 Organization Lifecycle

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 05.01.01.01 | Create organization | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/organization-management/create-organization` | - | Tenant Service | Organization | - | UJ-005 Provision Service |
| 05.01.01.02 | Update organization | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/organization-management/update-organization` | - | Tenant Service | Organization | - | UJ-005 Provision Service |
| 05.01.01.03 | Suspend organization | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/organization-management/suspend-organization` | - | Tenant Service | Organization | - | UJ-005 Provision Service |
| 05.01.01.04 | Activate organization | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/organization-management/activate-organization` | - | Tenant Service | Organization | - | UJ-005 Provision Service |
| 05.01.01.05 | Close organization | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/organization-management/close-organization` | - | Tenant Service | Organization | - | UJ-005 Provision Service |

## 05.02 Tenant Management

### Feature 05.02.01 Tenant Lifecycle

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 05.02.01.01 | Create tenant | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/tenant-management/create-tenant` | API-003 POST /v1/tenants | Tenant Service | Organization | - | UJ-005 Provision Service |
| 05.02.01.02 | Configure tenant | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/tenant-management/configure-tenant` | API-003 POST /v1/tenants | Tenant Service | Organization | - | UJ-005 Provision Service |
| 05.02.01.03 | Assign region | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/tenant-management/assign-region` | API-003 POST /v1/tenants | Tenant Service | Organization | - | UJ-005 Provision Service |
| 05.02.01.04 | Configure isolation | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/tenant-management/configure-isolation` | API-003 POST /v1/tenants | Tenant Service | Organization | - | UJ-005 Provision Service |
| 05.02.01.05 | Configure tenant policies | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/tenant-management/configure-tenant-policies` | API-003 POST /v1/tenants | Tenant Service | Organization | - | UJ-005 Provision Service |

## 05.03 User Management

### Feature 05.03.01 User Lifecycle

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 05.03.01.01 | Invite user | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/user-management/invite-user` | - | Tenant Service | Organization | - | UJ-005 Provision Service |
| 05.03.01.02 | Create user | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/user-management/create-user` | - | Tenant Service | Organization | - | UJ-005 Provision Service |
| 05.03.01.03 | Activate user | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/user-management/activate-user` | - | Tenant Service | Organization | - | UJ-005 Provision Service |
| 05.03.01.04 | Suspend user | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/user-management/suspend-user` | - | Tenant Service | Organization | - | UJ-005 Provision Service |
| 05.03.01.05 | Remove user | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/user-management/remove-user` | - | Tenant Service | Organization | - | UJ-005 Provision Service |

### Feature 05.03.02 Role Assignment

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 05.03.02.01 | Assign role | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/user-management/assign-role` | - | Tenant Service | Organization | - | UJ-005 Provision Service |
| 05.03.02.02 | Assign group | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/user-management/assign-group` | - | Tenant Service | Organization | - | UJ-005 Provision Service |
| 05.03.02.03 | Review access | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/user-management/review-access` | - | Tenant Service | Organization | - | UJ-005 Provision Service |

## 05.04 Group & Project Management

### Feature 05.04.01 Groups

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 05.04.01.01 | Create group | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/group-project-management/create-group` | - | Tenant Service | Organization | - | UJ-005 Provision Service |
| 05.04.01.02 | Add member | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/group-project-management/add-member` | - | Tenant Service | Organization | - | UJ-005 Provision Service |
| 05.04.01.03 | Remove member | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/group-project-management/remove-member` | - | Tenant Service | Organization | - | UJ-005 Provision Service |

### Feature 05.04.02 Projects

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 05.04.02.01 | Create project | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/group-project-management/create-project` | - | Tenant Service | Organization | - | UJ-005 Provision Service |
| 05.04.02.02 | Assign users | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/group-project-management/assign-users` | - | Tenant Service | Organization | - | UJ-005 Provision Service |
| 05.04.02.03 | Assign resources | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/group-project-management/assign-resources` | - | Tenant Service | Organization | - | UJ-005 Provision Service |

> **Columns from [WB:Traceability]** (Primary API, Microservice, Entity, Event, Journey) are kept for reference only. Under [C13](../open-decisions.md#c13) microservices are logical domains built as modules in the single backend. Under [C14](../open-decisions.md#c14) the implemented endpoints and each FRD's `api-requirements.md` are the source of truth for APIs.

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 06 | UJ-004 Tenant Onboarding (Identity, Tenant, IAM) | [WB:User Journeys] |
| 09, 15 | EVT-002 TenantCreated is consumed by Provisioning and Audit | [WB:Events] |

## Deliverables

Not specified in any source. Under [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates) the sprint commits this application's P0 capabilities; P1 capabilities are stretch scope.

## Requirements, design and tests

| Artifact | Location | Status |
|---|---|---|
| Feature FRD | `docs/02-requirements/FRD/<feature>/` (copy `_template/`) | See the sprint page for FRDs in progress |
| Requirement | `REQ-TEN-<NNN>` inside the FRD | Approved FRD required before build ([DN-4](../open-decisions.md#dn-4-business-rules-and-acceptance-criteria)) |
| Business rules | `docs/03-business-rules/` and `FRD/<feature>/business-rules.md` | Per FRD |
| Test cases | `test-cases/functional/<feature>/TC-TEN-<NNN>.md` | Not created |
| NFRs | [WB:Non-Functional Requirements] NFR-001 to NFR-014 (platform-wide) | See [EIS-document-analysis.md](../EIS-document-analysis.md) section 2.17 |

## Related code already in this repository

Observed on branch `dev`. This is a module-level mapping, not a verified function-by-function implementation status.

- Backend: `backend/src/main/java/com/vyoog/eisplatform/modules/registration (organization, members, seats)`
- Frontend: `frontend/src/pages/register/OrganizationRegisterPage.tsx`, `frontend/src/pages/register/VerifyEmailPage.tsx`, `frontend/src/pages/admin/RegistrationsAdminPage.tsx`
