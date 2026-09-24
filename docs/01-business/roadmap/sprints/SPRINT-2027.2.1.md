# SPRINT-2027.2.1

| Field | Value |
|---|---|
| Sprint ID (PI.Sprint) | 2027.2.1 |
| PI – CY Quarter | 2027.2 |
| Start / end dates | Not specified |
| Sprint goal | Not specified |
| Team / capacity | Not specified |
| Status | Not specified |
| Source | [PO] roadmap tables ([`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)) |
| Previous / next sprint | [2027.1.3](SPRINT-2027.1.3.md) · [2027.2.2](SPRINT-2027.2.2.md) |

## Scope

| Product | Application ID | Application | Roadmap item(s) | Source |
|---|---|---|---|---|
| EIS (PaaS) | 14 | [Partner & Provider Management](../applications/14-partner-provider-management.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |

> [PO] assigns **one sprint per EIS application**. It does not say which capabilities or features fall inside this sprint, or whether the application must be finished in it. The EIS scope below is the application's full [WB] breakdown until sprint scope is decided (see [open-decisions.md](../open-decisions.md)).

## EIS 14 Partner & Provider Management

**Planned work ([PO] / [WB] description):** Providers, publishers, onboarding and revenue sharing

Full breakdown with APIs, services, entities and events: [applications/14-partner-provider-management.md](../applications/14-partner-provider-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | MVP functions |
|---|---|---|---|
| [14.01 Provider Onboarding](../applications/14-partner-provider-management.md#1401-provider-onboarding) | 14.01.01 Provider Lifecycle | Register provider; Verify provider; Approve provider; Activate provider | - |
| [14.01 Provider Onboarding](../applications/14-partner-provider-management.md#1401-provider-onboarding) | 14.01.02 Contracts | Create contract; Manage terms; Track expiration | - |
| [14.02 Publisher Management](../applications/14-partner-provider-management.md#1402-publisher-management) | 14.02.01 Publisher Catalog | Create publisher product; Manage pricing; Manage content; Publish product | - |
| [14.02 Publisher Management](../applications/14-partner-provider-management.md#1402-publisher-management) | 14.02.02 Publisher Analytics | View product performance; View sales; View usage | - |
| [14.03 Revenue Sharing](../applications/14-partner-provider-management.md#1403-revenue-sharing) | 14.03.01 Commission | Define commission; Calculate revenue share; Generate statement | - |
| [14.03 Revenue Sharing](../applications/14-partner-provider-management.md#1403-revenue-sharing) | 14.03.02 Payouts | Calculate payout; Approve payout; Reconcile payout | - |
| [14.04 Partner Operations](../applications/14-partner-provider-management.md#1404-partner-operations) | 14.04.01 Partner Support | Create partner ticket; Assign partner manager; Track partner SLA | - |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - UJ-010 Provider Product Publishing (Partner, Catalog, Marketplace) (applications 02, 03; [WB:User Journeys])
  - DE-021 Partner relates to Contract and Payout (applications 08; [WB:Data Entities])

### Expected deliverables

- Not specified in any source.
- Implied by [WB:Traceability]: APIs `API-020 POST /v1/providers`; services Partner Service.

## Open issues affecting this sprint

- None specific to this sprint. The general decisions in open-decisions.md still apply
- The sprint scope, deliverables, acceptance criteria and dates are not specified in any source.

Details: [open-decisions.md](../open-decisions.md).

## Traceability

```
[PO] roadmap row → SPRINT-2027.2.1 → application page → capability → feature → function (requirement candidate)
   → FRD docs/02-requirements/FRD/<feature>/ (not created) → REQ-<APP-CODE>-<NNN> (not created)
   → STORY-<APP-CODE>-<NNN> with "Sprint (PI.Sprint)" = 2027.2.1
   → code (backend/ · frontend/ · ai-service/) → TC-<APP-CODE>-<NNN> in test-cases/ → UAT
```

Stories for this sprint use `docs/02-requirements/functional-requirements/user-story-template.md` or the GitHub **User Story** issue template, with **Sprint (PI.Sprint) = 2027.2.1**. The Definition of Done is the one in the story template.

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.
