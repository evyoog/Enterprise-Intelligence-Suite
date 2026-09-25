# 15 Administration & Governance

| Field | Value |
|---|---|
| Application ID | 15 ([WB] numbering; an `APP-<CODE>` code is not assigned in any source) |
| Application | Administration & Governance |
| Description | Platform configuration, policies, audit and compliance ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2027.2 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2027.2.2](../sprints/SPRINT-2027.2.2.md) |
| Capabilities / features / functions | 5 / 9 / 33 ([WB]) |
| Application status | Not specified |

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.

## Capabilities

| Capability ID | Capability | Features | Priority | MVP | AI relevant |
|---|---|---|---|---|---|
| [15.01](#1501-platform-administration) | Platform Administration | 15.01.01 Platform Configuration, 15.01.02 Global Settings | P0 | Yes | No |
| [15.02](#1502-policy-management) | Policy Management | 15.02.01 Policy Lifecycle | P0 | Yes | No |
| [15.03](#1503-audit) | Audit | 15.03.01 Audit Logging, 15.03.02 Audit Search | P0 | Yes | No |
| [15.04](#1504-compliance) | Compliance | 15.04.01 Compliance Controls, 15.04.02 Data Governance | P0 | Yes | No |
| [15.05](#1505-regional-operations) | Regional Operations | 15.05.01 Region Management, 15.05.02 Data Residency | P0 | Yes | No |

> The Priority and MVP values are copied from [WB:Capabilities]. Every capability in [WB] is P0 / MVP=Yes, which conflicts with the function-level MVP flags (C4 in [open-decisions.md](../open-decisions.md)).

## 15.01 Platform Administration

### Feature 15.01.01 Platform Configuration

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 15.01.01.01 | Configure platform | No | No | Platform Service | `/platform-administration/configure-platform` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.01.01.02 | Configure languages | No | No | Platform Service | `/platform-administration/configure-languages` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.01.01.03 | Configure currencies | No | No | Platform Service | `/platform-administration/configure-currencies` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.01.01.04 | Configure feature flags | No | No | Platform Service | `/platform-administration/configure-feature-flags` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |

### Feature 15.01.02 Global Settings

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 15.01.02.01 | Configure regions | No | No | Platform Service | `/platform-administration/configure-regions` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.01.02.02 | Configure defaults | No | No | Platform Service | `/platform-administration/configure-defaults` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.01.02.03 | Manage templates | No | No | Platform Service | `/platform-administration/manage-templates` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |

## 15.02 Policy Management

### Feature 15.02.01 Policy Lifecycle

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 15.02.01.01 | Create policy | No | No | Platform Service | `/policy-management/create-policy` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.02.01.02 | Assign policy | No | No | Platform Service | `/policy-management/assign-policy` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.02.01.03 | Evaluate policy | No | No | Platform Service | `/policy-management/evaluate-policy` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.02.01.04 | Enforce policy | No | No | Platform Service | `/policy-management/enforce-policy` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.02.01.05 | Manage exception | No | No | Platform Service | `/policy-management/manage-exception` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |

## 15.03 Audit

### Feature 15.03.01 Audit Logging

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 15.03.01.01 | Record activity | No | No | Platform Service | `/audit/record-activity` | API-022 GET /v1/audit/events | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.03.01.02 | Record login | No | No | Platform Service | `/audit/record-login` | API-022 GET /v1/audit/events | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.03.01.03 | Record configuration change | No | No | Platform Service | `/audit/record-configuration-change` | API-022 GET /v1/audit/events | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.03.01.04 | Record financial transaction | No | No | Platform Service | `/audit/record-financial-transaction` | API-022 GET /v1/audit/events | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |

### Feature 15.03.02 Audit Search

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 15.03.02.01 | Search audit logs | No | No | Platform Service | `/audit/search-audit-logs` | API-022 GET /v1/audit/events | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.03.02.02 | Filter audit logs | No | No | Platform Service | `/audit/filter-audit-logs` | API-022 GET /v1/audit/events | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.03.02.03 | Export audit logs | No | No | Platform Service | `/audit/export-audit-logs` | API-022 GET /v1/audit/events | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |

## 15.04 Compliance

### Feature 15.04.01 Compliance Controls

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 15.04.01.01 | Define control | No | No | Platform Service | `/compliance/define-control` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.04.01.02 | Map requirement | No | No | Platform Service | `/compliance/map-requirement` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.04.01.03 | Collect evidence | No | No | Platform Service | `/compliance/collect-evidence` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.04.01.04 | Track remediation | No | No | Platform Service | `/compliance/track-remediation` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |

### Feature 15.04.02 Data Governance

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 15.04.02.01 | Classify data | No | No | Platform Service | `/compliance/classify-data` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.04.02.02 | Define retention | No | No | Platform Service | `/compliance/define-retention` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.04.02.03 | Apply retention | No | No | Platform Service | `/compliance/apply-retention` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |

## 15.05 Regional Operations

### Feature 15.05.01 Region Management

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 15.05.01.01 | Create region | No | No | Platform Service | `/regional-operations/create-region` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.05.01.02 | Configure region | No | No | Platform Service | `/regional-operations/configure-region` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.05.01.03 | Activate region | No | No | Platform Service | `/regional-operations/activate-region` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.05.01.04 | Suspend region | No | No | Platform Service | `/regional-operations/suspend-region` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |

### Feature 15.05.02 Data Residency

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 15.05.02.01 | Define residency policy | No | No | Platform Service | `/regional-operations/define-residency-policy` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.05.02.02 | Validate residency | No | No | Platform Service | `/regional-operations/validate-residency` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.05.02.03 | Report residency | No | No | Platform Service | `/regional-operations/report-residency` | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| all | EVT-020 AuditRecorded is consumed by Compliance; MS-021 Audit Service stores immutable audit records | [WB:Events], [WB:Microservices] |
| 02, 10 | UJ-011 Regional Expansion (Regional Ops, Governance) | [WB:User Journeys] |

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

- Backend: `backend/src/main/java/com/vyoog/eisplatform/modules/audit`, `backend/src/main/java/com/vyoog/eisplatform/modules/dashboard (OrganizationAuditLogController)`
- Frontend: `frontend/src/pages/admin/AdminAuditLogPage.tsx`
