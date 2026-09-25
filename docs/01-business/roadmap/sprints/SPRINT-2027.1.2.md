# SPRINT-2027.1.2

| Field | Value |
|---|---|
| Sprint ID (PI.Sprint) | 2027.1.2 |
| PI – CY Quarter | 2027.1 |
| Start / end dates | Not specified |
| Sprint goal | Not specified |
| Team / capacity | Not specified |
| Status | Not specified |
| Source | [PO] roadmap tables ([`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)) |
| Previous / next sprint | [2027.1.1](SPRINT-2027.1.1.md) · [2027.1.3](SPRINT-2027.1.3.md) |

## Scope

| Product | Application ID | Application | Roadmap item(s) | Source |
|---|---|---|---|---|
| EIS (PaaS) | 04 | [AI Advisor & Agent Platform](../applications/04-ai-advisor-agent-platform.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |

> [PO] assigns **one sprint per EIS application**. It does not say which capabilities or features fall inside this sprint, or whether the application must be finished in it. The EIS scope below is the application's full [WB] breakdown until sprint scope is decided (see [open-decisions.md](../open-decisions.md)).

## EIS 04 AI Advisor & Agent Platform

**Planned work ([PO] / [WB] description):** AI-guided selling, technical assistance and support

Full breakdown with APIs, services, entities and events: [applications/04-ai-advisor-agent-platform.md](../applications/04-ai-advisor-agent-platform.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | MVP functions |
|---|---|---|---|
| [04.01 AI Product Advisor](../applications/04-ai-advisor-agent-platform.md#0401-ai-product-advisor) | 04.01.01 Requirement Discovery | Ask customer questions; Capture requirements; Identify constraints | - |
| [04.01 AI Product Advisor](../applications/04-ai-advisor-agent-platform.md#0401-ai-product-advisor) | 04.01.02 Recommendation | Search catalog; Evaluate compatibility; Rank products; Explain recommendation; Recommend configuration; Estimate cost | - |
| [04.02 AI Sales Agent](../applications/04-ai-advisor-agent-platform.md#0402-ai-sales-agent) | 04.02.01 Sales Assistance | Qualify lead; Explain pricing; Generate proposal; Generate quote; Recommend upsell; Recommend cross-sell | - |
| [04.03 AI Technical Advisor](../applications/04-ai-advisor-agent-platform.md#0403-ai-technical-advisor) | 04.03.01 Technical Guidance | Recommend architecture; Explain configuration; Troubleshoot issue; Recommend best practice | - |
| [04.04 AI Support Agent](../applications/04-ai-advisor-agent-platform.md#0404-ai-support-agent) | 04.04.01 Support Automation | Understand request; Search knowledge base; Diagnose issue; Recommend resolution; Create ticket; Escalate to human | - |
| [04.05 AI Agent Orchestration](../applications/04-ai-advisor-agent-platform.md#0405-ai-agent-orchestration) | 04.05.01 Agent Runtime | Register agent; Route request; Select tools; Manage context; Apply guardrails; Audit agent action | - |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - UJ-002 AI Guided Selection: Portal → AI Advisor → Catalog (AI, Catalog, Pricing) (applications 02, 08; [WB:User Journeys])
  - UJ-008 AI Technical Support (AI, Knowledge, Support, Service) (applications 11, 12, 10; [WB:User Journeys])

### Expected deliverables

- Not specified in any source.
- Implied by [WB:Traceability]: APIs `API-007 POST /v1/recommendations`; services AI Agent Gateway.

## Open issues affecting this sprint

- C3: AI Advisor (04) is Phase 1/MVP in [CG] and [WB:Roadmap] but is scheduled after Phase 2/3 applications
- The sprint scope, deliverables, acceptance criteria and dates are not specified in any source.

Details: [open-decisions.md](../open-decisions.md).

## Traceability

```
[PO] roadmap row → SPRINT-2027.1.2 → application page → capability → feature → function (requirement candidate)
   → FRD docs/02-requirements/FRD/<feature>/ (not created) → REQ-<APP-CODE>-<NNN> (not created)
   → STORY-<APP-CODE>-<NNN> with "Sprint (PI.Sprint)" = 2027.1.2
   → code (backend/ · frontend/ · ai-service/) → TC-<APP-CODE>-<NNN> in test-cases/ → UAT
```

Stories for this sprint use `docs/02-requirements/functional-requirements/user-story-template.md` or the GitHub **User Story** issue template, with **Sprint (PI.Sprint) = 2027.1.2**. The Definition of Done is the one in the story template.

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.
