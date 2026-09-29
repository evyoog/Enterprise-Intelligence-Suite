# 15 Administration & Governance

| Field | Value |
|---|---|
| Application ID | 15 ([WB] numbering) |
| Application code | `APP-GOV` ([DN-5](../open-decisions.md#dn-5-application-codes)); IDs use `GOV`, for example `REQ-GOV-001` |
| Application | Administration & Governance |
| Description | Platform configuration, policies, audit and compliance ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2027.2 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | Split ([C31](../open-decisions.md#c31)): 15a Audit & Platform Administration in [2026.4.2](../sprints/SPRINT-2026.4.2.md) (1–30 Nov 2026); 15b Policy & compliance in [2027.1.3](../sprints/SPRINT-2027.1.3.md) (1–31 Mar 2027); 15c Regional operations in [2027.2.2](../sprints/SPRINT-2027.2.2.md) (1–31 May 2027) |
| Capabilities / features / functions | 5 / 9 / 33 ([WB]) |
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
| [15.01](#1501-platform-administration) | Platform Administration | 15.01.01 Platform Configuration, 15.01.02 Global Settings | No | Not specified | Not specified | Not in [WB:Roadmap] |
| [15.02](#1502-policy-management) | Policy Management | 15.02.01 Policy Lifecycle | No | Phase 3 | P1 | [WB:Roadmap] "Advanced Governance" |
| [15.03](#1503-audit) | Audit | 15.03.01 Audit Logging, 15.03.02 Audit Search | Yes | Phase 1 / MVP | P0 | C4 |
| [15.04](#1504-compliance) | Compliance | 15.04.01 Compliance Controls, 15.04.02 Data Governance | No | Phase 3 | P1 | [WB:Roadmap] "Advanced Governance" |
| [15.05](#1505-regional-operations) | Regional Operations | 15.05.01 Region Management, 15.05.02 Data Residency | No | Phase 3 | P1 | [WB:Roadmap] "Global Scale" |

## 15.01 Platform Administration

### Feature 15.01.01 Platform Configuration

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 15.01.01.01 | Configure platform | No | Not specified | Not specified | No | Platform Service | `/platform-administration/configure-platform` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.01.01.02 | Configure languages | No | Not specified | Not specified | No | Platform Service | `/platform-administration/configure-languages` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.01.01.03 | Configure currencies | No | Not specified | Not specified | No | Platform Service | `/platform-administration/configure-currencies` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.01.01.04 | Configure feature flags | No | Not specified | Not specified | No | Platform Service | `/platform-administration/configure-feature-flags` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |

### Feature 15.01.02 Global Settings

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 15.01.02.01 | Configure regions | No | Not specified | Not specified | No | Platform Service | `/platform-administration/configure-regions` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.01.02.02 | Configure defaults | No | Not specified | Not specified | No | Platform Service | `/platform-administration/configure-defaults` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.01.02.03 | Manage templates | No | Not specified | Not specified | No | Platform Service | `/platform-administration/manage-templates` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |

## 15.02 Policy Management

### Feature 15.02.01 Policy Lifecycle

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 15.02.01.01 | Create policy | No | Phase 3 | P1 | No | Platform Service | `/policy-management/create-policy` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.02.01.02 | Assign policy | No | Phase 3 | P1 | No | Platform Service | `/policy-management/assign-policy` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.02.01.03 | Evaluate policy | No | Phase 3 | P1 | No | Platform Service | `/policy-management/evaluate-policy` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.02.01.04 | Enforce policy | No | Phase 3 | P1 | No | Platform Service | `/policy-management/enforce-policy` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.02.01.05 | Manage exception | No | Phase 3 | P1 | No | Platform Service | `/policy-management/manage-exception` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |

## 15.03 Audit

### Feature 15.03.01 Audit Logging

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 15.03.01.01 | Record activity | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/audit/record-activity` | API-022 GET /v1/audit/events | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.03.01.02 | Record login | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/audit/record-login` | API-022 GET /v1/audit/events | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.03.01.03 | Record configuration change | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/audit/record-configuration-change` | API-022 GET /v1/audit/events | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.03.01.04 | Record financial transaction | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/audit/record-financial-transaction` | API-022 GET /v1/audit/events | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |

### Feature 15.03.02 Audit Search

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 15.03.02.01 | Search audit logs | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/audit/search-audit-logs` | API-022 GET /v1/audit/events | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.03.02.02 | Filter audit logs | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/audit/filter-audit-logs` | API-022 GET /v1/audit/events | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.03.02.03 | Export audit logs | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/audit/export-audit-logs` | API-022 GET /v1/audit/events | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |

## 15.04 Compliance

### Feature 15.04.01 Compliance Controls

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 15.04.01.01 | Define control | No | Phase 3 | P1 | No | Platform Service | `/compliance/define-control` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.04.01.02 | Map requirement | No | Phase 3 | P1 | No | Platform Service | `/compliance/map-requirement` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.04.01.03 | Collect evidence | No | Phase 3 | P1 | No | Platform Service | `/compliance/collect-evidence` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.04.01.04 | Track remediation | No | Phase 3 | P1 | No | Platform Service | `/compliance/track-remediation` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |

### Feature 15.04.02 Data Governance

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 15.04.02.01 | Classify data | No | Phase 3 | P1 | No | Platform Service | `/compliance/classify-data` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.04.02.02 | Define retention | No | Phase 3 | P1 | No | Platform Service | `/compliance/define-retention` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.04.02.03 | Apply retention | No | Phase 3 | P1 | No | Platform Service | `/compliance/apply-retention` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |

## 15.05 Regional Operations

### Feature 15.05.01 Region Management

**Already satisfied ([C43](../open-decisions.md#c43), 2027.2.2):** `PlatformAdministrationService`'s existing region CRUD (`REQ-GOV-001.2`, built sprint 2026.4.2) already covers all four functions below — Create/Configure region via `createRegion`/`updateRegion`'s rename, Activate/Suspend region via that same `updateRegion` call's `enabled` toggle. No new module was built for this sprint's own slot.

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 15.05.01.01 | Create region | No | Phase 3 | P1 | No | Platform Service | `/regional-operations/create-region` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.05.01.02 | Configure region | No | Phase 3 | P1 | No | Platform Service | `/regional-operations/configure-region` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.05.01.03 | Activate region | No | Phase 3 | P1 | No | Platform Service | `/regional-operations/activate-region` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.05.01.04 | Suspend region | No | Phase 3 | P1 | No | Platform Service | `/regional-operations/suspend-region` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |

### Feature 15.05.02 Data Residency

**Not built ([C43](../open-decisions.md#c43), 2027.2.2):** needs the general policy engine (15.02, [C41](../open-decisions.md#c41), still unresolved) to define a "residency policy" against, and 10 Service & Resource Management ([C40](../open-decisions.md#c40), still not built) to actually have a per-region resource to validate or report residency for. Carried, with no further sprint scheduled.

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 15.05.02.01 | Define residency policy | No | Phase 3 | P1 | No | Platform Service | `/regional-operations/define-residency-policy` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.05.02.02 | Validate residency | No | Phase 3 | P1 | No | Platform Service | `/regional-operations/validate-residency` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |
| 15.05.02.03 | Report residency | No | Phase 3 | P1 | No | Platform Service | `/regional-operations/report-residency` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service |

> **Columns from [WB:Traceability]** (Primary API, Microservice, Entity, Event, Journey) are kept for reference only. Under [C13](../open-decisions.md#c13) microservices are logical domains built as modules in the single backend. Under [C14](../open-decisions.md#c14) the implemented endpoints and each FRD's `api-requirements.md` are the source of truth for APIs.

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| all | EVT-020 AuditRecorded is consumed by Compliance; MS-021 Audit Service stores immutable audit records | [WB:Events], [WB:Microservices] |
| 02, 10 | UJ-011 Regional Expansion (Regional Ops, Governance) | [WB:User Journeys] |

## Deliverables

Not specified in any source. Under [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates) the sprint commits this application's P0 capabilities; P1 capabilities are stretch scope.

## Requirements, design and tests

| Artifact | Location | Status |
|---|---|---|
| Feature FRD | `docs/02-requirements/FRD/<feature>/` (copy `_template/`) | See the sprint page for FRDs in progress |
| Requirement | `REQ-GOV-<NNN>` inside the FRD | Approved FRD required before build ([DN-4](../open-decisions.md#dn-4-business-rules-and-acceptance-criteria)) |
| Business rules | `docs/03-business-rules/` and `FRD/<feature>/business-rules.md` | Per FRD |
| Test cases | `test-cases/functional/<feature>/TC-GOV-<NNN>.md` | Not created |
| NFRs | [WB:Non-Functional Requirements] NFR-001 to NFR-014 (platform-wide) | See [EIS-document-analysis.md](../EIS-document-analysis.md) section 2.17 |

## Related code already in this repository

Observed on branch `dev`. This is a module-level mapping, not a verified function-by-function implementation status.

- Backend: `backend/src/main/java/com/vyoog/eisplatform/modules/audit`, `backend/src/main/java/com/vyoog/eisplatform/modules/dashboard (OrganizationAuditLogController)`, `backend/src/main/java/com/vyoog/eisplatform/modules/administration` (currencies, regions, feature flags — [platform-administration](../../../02-requirements/FRD/platform-administration/requirement.md); its region CRUD also satisfies 15.05.01 Region Management, [C43](../open-decisions.md#c43))
- Frontend: `frontend/src/pages/admin/AdminAuditLogPage.tsx`, `frontend/src/pages/admin/settings/CommonSettingsPage.tsx`
