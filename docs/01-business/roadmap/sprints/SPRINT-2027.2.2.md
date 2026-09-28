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
| 15c | `APP-GOV` (part) | [Administration & Governance](../applications/15-administration-governance.md) | Regional Operations only (15.05) | [PO], scope split by [C31](../open-decisions.md#c31) |

> **Commitment ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)):** this sprint commits the P0 (MVP) capabilities of its applications and treats P1 capabilities as stretch scope. Anything not finished is recorded as carry-over on the next sprint page.

## Scope changes from decisions

| Change | Decision and scope | FRD | Requirement |
|---|---|---|---|
| Superseded | [C21](../open-decisions.md#c21)'s general policy engine (06.02.02/15.02) is no longer here — [C31](../open-decisions.md#c31) moved 15b (Policy Management, Compliance) to [2027.1.3](SPRINT-2027.1.3.md) | - | - |
| Split | [C31](../open-decisions.md#c31) 15 splits: 15a (Audit, Platform Administration) was built in [2026.4.2](SPRINT-2026.4.2.md); 15b (Policy, Compliance) was nominally here too but [C41](../open-decisions.md#c41) carried it, still unresolved; 15c (Regional Operations) is here | - | - |
| Already satisfied | [C43](../open-decisions.md#c43) 15.05.01 Region Management — `PlatformAdministrationService`'s existing region CRUD (`REQ-GOV-001.2`, built [2026.4.2](SPRINT-2026.4.2.md)) already covers Create/Configure/Activate/Suspend region; nothing new to build | [platform-administration](../../../02-requirements/FRD/platform-administration/requirement.md) | REQ-GOV-001 |
| Not built | [C43](../open-decisions.md#c43) 15.05.02 Data Residency — needs the same unresolved general policy-engine decision ([C41](../open-decisions.md#c41)) and 10 Service & Resource Management ([C40](../open-decisions.md#c40)), neither of which this sprint has a new basis to resolve |

## EIS 15c Administration & Governance: regional operations

**Planned work:** Regional Operations — expanding to more regions, after the single-region platform is proven.

Full breakdown: [applications/15-administration-governance.md](../applications/15-administration-governance.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [15.05 Regional Operations](../applications/15-administration-governance.md#1505-regional-operations) | 15.05.01 Region Management | Create region; Configure region; Activate region; Suspend region | P1 | Stretch |
| [15.05 Regional Operations](../applications/15-administration-governance.md#1505-regional-operations) | 15.05.02 Data Residency | Define residency policy; Validate residency; Report residency | P1 | Stretch |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]:** 15b Policy & compliance (2027.1.3) for the policy engine a region's residency rules plug into; 10 Service & Resource Management (2027.1.2) for what actually runs per region.
- UJ-011 Regional Expansion (Regional Ops, Governance) (applications 02, 10; [WB:User Journeys])

### Expected deliverables

- All-P1 (stretch) scope ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). 15.01 Platform Administration and 15.03 Audit (15a) were already built in 2026.4.2. 15.02 Policy Management and 15.04 Compliance (15b) were nominally scheduled in 2027.1.3 but [C41](../open-decisions.md#c41) carried them, still unresolved.

### Progress (as of 2026-09-28)

| Feature | Status | Note |
|---|---|---|
| 15.05.01 Region Management | Done (pre-existing) | Already satisfied by `PlatformAdministrationService`'s region CRUD, built 2026.4.2 (REQ-GOV-001.2) — see [C43](../open-decisions.md#c43) |
| 15.05.02 Data Residency | Not started | Carried — needs the general policy engine (15b, C41) and 10 Service & Resource Management (C40), neither resolved |

This is the last sprint in the corrected sequence ([C31](../open-decisions.md#c31)). Every item this roadmap still carries — 15.05.02 here, plus 01.03.01.02, 03.02, 04b/12.02, 11b, 15b itself (C41), and 14.02/14.03/14.04 (C42) — waits on one of a small number of named, still-undecided prerequisites (an embeddings/vector-store decision, an agent/LLM framework choice, a general policy-engine decision, real service/resource infrastructure, a product-ownership model, a billing engine, or a partner-identity/role decision), not on any remaining sprint slot.

### Related code already in this repository

Observed on branch `dev`, module level only: `backend/…/modules/audit`, `backend/…/modules/dashboard (OrganizationAuditLogController)`; `frontend/src/pages/admin/AdminAuditLogPage.tsx` — this is 15a's audit work, built early in 2026.4.2, not 15c itself. 15.05.01 Region Management (15c's own scope) is satisfied by `backend/…/modules/administration` (`PlatformAdministrationService`, `PlatformRegionRepository`) and `frontend/src/pages/admin/settings/CommonSettingsPage.tsx` — see [C43](../open-decisions.md#c43).

## Decisions affecting this sprint

- [C3](../open-decisions.md#c3) The [PO] sprint order is authoritative; the MVP is complete at the end of sprint 2027.1.3.
- [C4](../open-decisions.md#c4), [C5](../open-decisions.md#c5), [C6](../open-decisions.md#c6) MVP, priority and phase as shown above.
- [C31](../open-decisions.md#c31) Corrected sprint sequence: only 15c (Regional Operations) remains in this sprint; 15a and 15b moved earlier.
- [C43](../open-decisions.md#c43) 15.05.01 already satisfied; 15.05.02 not built, pending the general policy-engine and Service & Resource Management gaps.
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
