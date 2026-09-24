# SPRINT-2027.2.2

| Field | Value |
|---|---|
| Sprint ID (PI.Sprint) | 2027.2.2 |
| PI – CY Quarter | 2027.2 |
| Start / end dates | Not specified |
| Sprint goal | Not specified |
| Team / capacity | Not specified |
| Status | Not specified |
| Source | [PO] roadmap tables ([`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)) |
| Previous / next sprint | [2027.2.1](SPRINT-2027.2.1.md) · [2027.4.1](SPRINT-2027.4.1.md) |

## Scope

| Product | Application ID | Application | Roadmap item(s) | Source |
|---|---|---|---|---|
| EIS (PaaS) | 15 | [Administration & Governance](../applications/15-administration-governance.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |

> [PO] assigns **one sprint per EIS application**. It does not say which capabilities or features fall inside this sprint, or whether the application must be finished in it. The EIS scope below is the application's full [WB] breakdown until sprint scope is decided (see [open-decisions.md](../open-decisions.md)).

## EIS 15 Administration & Governance

**Planned work ([PO] / [WB] description):** Platform configuration, policies, audit and compliance

Full breakdown with APIs, services, entities and events: [applications/15-administration-governance.md](../applications/15-administration-governance.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | MVP functions |
|---|---|---|---|
| [15.01 Platform Administration](../applications/15-administration-governance.md#1501-platform-administration) | 15.01.01 Platform Configuration | Configure platform; Configure languages; Configure currencies; Configure feature flags | - |
| [15.01 Platform Administration](../applications/15-administration-governance.md#1501-platform-administration) | 15.01.02 Global Settings | Configure regions; Configure defaults; Manage templates | - |
| [15.02 Policy Management](../applications/15-administration-governance.md#1502-policy-management) | 15.02.01 Policy Lifecycle | Create policy; Assign policy; Evaluate policy; Enforce policy; Manage exception | - |
| [15.03 Audit](../applications/15-administration-governance.md#1503-audit) | 15.03.01 Audit Logging | Record activity; Record login; Record configuration change; Record financial transaction | - |
| [15.03 Audit](../applications/15-administration-governance.md#1503-audit) | 15.03.02 Audit Search | Search audit logs; Filter audit logs; Export audit logs | - |
| [15.04 Compliance](../applications/15-administration-governance.md#1504-compliance) | 15.04.01 Compliance Controls | Define control; Map requirement; Collect evidence; Track remediation | - |
| [15.04 Compliance](../applications/15-administration-governance.md#1504-compliance) | 15.04.02 Data Governance | Classify data; Define retention; Apply retention | - |
| [15.05 Regional Operations](../applications/15-administration-governance.md#1505-regional-operations) | 15.05.01 Region Management | Create region; Configure region; Activate region; Suspend region | - |
| [15.05 Regional Operations](../applications/15-administration-governance.md#1505-regional-operations) | 15.05.02 Data Residency | Define residency policy; Validate residency; Report residency | - |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - EVT-020 AuditRecorded is consumed by Compliance; MS-021 Audit Service stores immutable audit records (applications all; [WB:Events], [WB:Microservices])
  - UJ-011 Regional Expansion (Regional Ops, Governance) (applications 02, 10; [WB:User Journeys])

### Expected deliverables

- Not specified in any source.
- Implied by [WB:Traceability]: APIs `API-022 GET /v1/audit/events`; services Audit Service.

### Related code already in this repository

Observed on branch `dev`, module level only: `backend/…/modules/audit`, `backend/…/modules/dashboard (OrganizationAuditLogController)`; `frontend/src/pages/admin/AdminAuditLogPage.tsx`.

## Open issues affecting this sprint

- None specific to this sprint. The general decisions in open-decisions.md still apply
- The sprint scope, deliverables, acceptance criteria and dates are not specified in any source.

Details: [open-decisions.md](../open-decisions.md).

## Traceability

```
[PO] roadmap row → SPRINT-2027.2.2 → application page → capability → feature → function (requirement candidate)
   → FRD docs/02-requirements/FRD/<feature>/ (not created) → REQ-<APP-CODE>-<NNN> (not created)
   → STORY-<APP-CODE>-<NNN> with "Sprint (PI.Sprint)" = 2027.2.2
   → code (backend/ · frontend/ · ai-service/) → TC-<APP-CODE>-<NNN> in test-cases/ → UAT
```

Stories for this sprint use `docs/02-requirements/functional-requirements/user-story-template.md` or the GitHub **User Story** issue template, with **Sprint (PI.Sprint) = 2027.2.2**. The Definition of Done is the one in the story template.

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.
