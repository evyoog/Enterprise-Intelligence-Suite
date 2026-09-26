# SPRINT-2027.2.1

| Field | Value |
|---|---|
| Sprint ID (PI.Sprint) | 2027.2.1 |
| PI – CY Quarter | 2027.2 |
| Start / end dates | 1–30 Apr 2027 ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Sprint goal | Not specified |
| Team / capacity | Not specified |
| Status | Not specified |
| Source | [PO] roadmap table ([`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)); decisions in [open-decisions.md](../open-decisions.md) |
| Previous / next sprint | [2027.1.3](SPRINT-2027.1.3.md) · [2027.2.2](SPRINT-2027.2.2.md) |

## Scope

| Application ID | Code | Application | Roadmap item | Source |
|---|---|---|---|---|
| 14 | `APP-PTR` | [Partner & Provider Management](../applications/14-partner-provider-management.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |

> **Commitment ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)):** this sprint commits the P0 (MVP) capabilities of its applications and treats P1 capabilities as stretch scope. Anything not finished is recorded as carry-over on the next sprint page.
>
> **Possible overflow ([C31](../open-decisions.md#c31)):** 03b Trials & reviews and 11b Learning & certification are provisionally in [2027.1.3](SPRINT-2027.1.3.md), which is the busiest sprint in the corrected sequence. Neither has anything depending on it, so either can move here if 2027.1.3 is overloaded — check that sprint's page for their current status before this sprint starts.

## Scope changes from decisions

| Change | Decision and scope | FRD | Requirement |
|---|---|---|---|
| Unchanged | [C31](../open-decisions.md#c31) 14 Partners stays in this sprint. Its dependencies (03a Checkout, 08 Billing, 13b Connectors, 15b Policy & compliance) all land earlier under the corrected sequence | - | - |

## EIS 14 Partner & Provider Management

**Planned work ([PO] / [WB] description):** Providers, publishers, onboarding and revenue sharing

Full breakdown with APIs, services, entities and events: [applications/14-partner-provider-management.md](../applications/14-partner-provider-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [14.01 Provider Onboarding](../applications/14-partner-provider-management.md#1401-provider-onboarding) | 14.01.01 Provider Lifecycle | Register provider; Verify provider; Approve provider; Activate provider | P1 | Stretch |
| [14.01 Provider Onboarding](../applications/14-partner-provider-management.md#1401-provider-onboarding) | 14.01.02 Contracts | Create contract; Manage terms; Track expiration | P1 | Stretch |
| [14.02 Publisher Management](../applications/14-partner-provider-management.md#1402-publisher-management) | 14.02.01 Publisher Catalog | Create publisher product; Manage pricing; Manage content; Publish product | P1 | Stretch |
| [14.02 Publisher Management](../applications/14-partner-provider-management.md#1402-publisher-management) | 14.02.02 Publisher Analytics | View product performance; View sales; View usage | P1 | Stretch |
| [14.03 Revenue Sharing](../applications/14-partner-provider-management.md#1403-revenue-sharing) | 14.03.01 Commission | Define commission; Calculate revenue share; Generate statement | P1 | Stretch |
| [14.03 Revenue Sharing](../applications/14-partner-provider-management.md#1403-revenue-sharing) | 14.03.02 Payouts | Calculate payout; Approve payout; Reconcile payout | P1 | Stretch |
| [14.04 Partner Operations](../applications/14-partner-provider-management.md#1404-partner-operations) | 14.04.01 Partner Support | Create partner ticket; Assign partner manager; Track partner SLA | Not specified | Not specified |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - UJ-010 Provider Product Publishing (Partner, Catalog, Marketplace) (applications 02, 03; [WB:User Journeys])
  - DE-021 Partner relates to Contract and Payout (applications 08; [WB:Data Entities])

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). Deliverables are otherwise Not specified in any source.

## Decisions affecting this sprint

- [C3](../open-decisions.md#c3) The [PO] sprint order is authoritative; the MVP is complete at the end of sprint 2027.1.3.
- [C4](../open-decisions.md#c4), [C5](../open-decisions.md#c5), [C6](../open-decisions.md#c6) MVP, priority and phase as shown above.
- [C31](../open-decisions.md#c31) Corrected sprint sequence: 14 unchanged; possible overflow from 2027.1.3 as shown above.
- Sprint goal, team, capacity and status are Not specified.

Details: [open-decisions.md](../open-decisions.md).

## Traceability

```
[PO] roadmap row → SPRINT-2027.2.1 → application page → capability → feature → function (requirement candidate)
   → FRD docs/02-requirements/FRD/<feature>/ → REQ-<CODE>-<NNN> (Approved before build)
   → STORY-<CODE>-<NNN> with "Sprint (PI.Sprint)" = 2027.2.1
   → code (backend/ · frontend/ · ai-service/) → TC-<CODE>-<NNN> in test-cases/ → UAT
```

Stories for this sprint use `docs/02-requirements/functional-requirements/user-story-template.md` or the GitHub **User Story** issue template, with **Sprint (PI.Sprint) = 2027.2.1**. The Definition of Done is the one in the story template.

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)
- **Decisions:** [`open-decisions.md`](../open-decisions.md) (2026-09-25)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source or decision gives are written **Not specified**.
