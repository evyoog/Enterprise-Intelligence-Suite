# SPRINT-2027.2.2

| Field | Value |
|---|---|
| Sprint ID (PI.Sprint) | 2027.2.2 |
| PI – CY Quarter | 2027.2 |
| Start / end dates | 1–31 May 2027 ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Sprint goal | Not specified |
| Team / capacity | Not specified |
| Status | Not specified |
| Source | [PO] roadmap table ([`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)); decisions in [open-decisions.md](../open-decisions.md) |
| Previous / next sprint | [2027.2.1](SPRINT-2027.2.1.md) · (last) |

## Scope

| Application ID | Code | Application | Roadmap item | Source |
|---|---|---|---|---|
| 15 | `APP-GOV` | [Administration & Governance](../applications/15-administration-governance.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |

> **Commitment ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)):** this sprint commits the P0 (MVP) capabilities of its applications and treats P1 capabilities as stretch scope. Anything not finished is recorded as carry-over on the next sprint page.

## Scope changes from decisions

| Change | Decision and scope | FRD | Requirement |
|---|---|---|---|
| Added | [C21](../open-decisions.md#c21) General policy engine, delivered with Policy Management (15.02), where its requirements are defined. Moved from sprint [2026.3.3](SPRINT-2026.3.3.md) (06.02.02) | - | - |

## EIS 15 Administration & Governance

**Planned work ([PO] / [WB] description):** Platform configuration, policies, audit and compliance

Full breakdown with APIs, services, entities and events: [applications/15-administration-governance.md](../applications/15-administration-governance.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [15.01 Platform Administration](../applications/15-administration-governance.md#1501-platform-administration) | 15.01.01 Platform Configuration | Configure platform; Configure languages; Configure currencies; Configure feature flags | Not specified | Not specified |
| [15.01 Platform Administration](../applications/15-administration-governance.md#1501-platform-administration) | 15.01.02 Global Settings | Configure regions; Configure defaults; Manage templates | Not specified | Not specified |
| [15.02 Policy Management](../applications/15-administration-governance.md#1502-policy-management) | 15.02.01 Policy Lifecycle | Create policy; Assign policy; Evaluate policy; Enforce policy; Manage exception | P1 | Stretch |
| [15.03 Audit](../applications/15-administration-governance.md#1503-audit) | 15.03.01 Audit Logging | Record activity; Record login; Record configuration change; Record financial transaction | P0 | Commit |
| [15.03 Audit](../applications/15-administration-governance.md#1503-audit) | 15.03.02 Audit Search | Search audit logs; Filter audit logs; Export audit logs | P0 | Commit |
| [15.04 Compliance](../applications/15-administration-governance.md#1504-compliance) | 15.04.01 Compliance Controls | Define control; Map requirement; Collect evidence; Track remediation | P1 | Stretch |
| [15.04 Compliance](../applications/15-administration-governance.md#1504-compliance) | 15.04.02 Data Governance | Classify data; Define retention; Apply retention | P1 | Stretch |
| [15.05 Regional Operations](../applications/15-administration-governance.md#1505-regional-operations) | 15.05.01 Region Management | Create region; Configure region; Activate region; Suspend region | P1 | Stretch |
| [15.05 Regional Operations](../applications/15-administration-governance.md#1505-regional-operations) | 15.05.02 Data Residency | Define residency policy; Validate residency; Report residency | P1 | Stretch |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - EVT-020 AuditRecorded is consumed by Compliance; MS-021 Audit Service stores immutable audit records (applications all; [WB:Events], [WB:Microservices])
  - UJ-011 Regional Expansion (Regional Ops, Governance) (applications 02, 10; [WB:User Journeys])

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). Deliverables are otherwise Not specified in any source.

### Related code already in this repository

Observed on branch `dev`, module level only: `backend/…/modules/audit`, `backend/…/modules/dashboard (OrganizationAuditLogController)`; `frontend/src/pages/admin/AdminAuditLogPage.tsx`.

## Decisions affecting this sprint

- [C3](../open-decisions.md#c3) The [PO] sprint order is authoritative; the MVP is complete at the end of sprint 2027.1.3.
- [C4](../open-decisions.md#c4), [C5](../open-decisions.md#c5), [C6](../open-decisions.md#c6) MVP, priority and phase as shown above.
- Sprint goal, team, capacity and status are Not specified.

Details: [open-decisions.md](../open-decisions.md).

## Traceability

```
[PO] roadmap row → SPRINT-2027.2.2 → application page → capability → feature → function (requirement candidate)
   → FRD docs/02-requirements/FRD/<feature>/ → REQ-<CODE>-<NNN> (Approved before build)
   → STORY-<CODE>-<NNN> with "Sprint (PI.Sprint)" = 2027.2.2
   → code (backend/ · frontend/ · ai-service/) → TC-<CODE>-<NNN> in test-cases/ → UAT
```

Stories for this sprint use `docs/02-requirements/functional-requirements/user-story-template.md` or the GitHub **User Story** issue template, with **Sprint (PI.Sprint) = 2027.2.2**. The Definition of Done is the one in the story template.

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)
- **Decisions:** [`open-decisions.md`](../open-decisions.md) (2026-09-25)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source or decision gives are written **Not specified**.
