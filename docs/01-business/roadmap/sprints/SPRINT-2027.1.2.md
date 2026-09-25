# SPRINT-2027.1.2

| Field | Value |
|---|---|
| Sprint ID (PI.Sprint) | 2027.1.2 |
| PI – CY Quarter | 2027.1 |
| Start / end dates | 1–28 Feb 2027 ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Sprint goal | Not specified |
| Team / capacity | Not specified |
| Status | Not specified |
| Source | [PO] roadmap table ([`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)); decisions in [open-decisions.md](../open-decisions.md) |
| Previous / next sprint | [2027.1.1](SPRINT-2027.1.1.md) · [2027.1.3](SPRINT-2027.1.3.md) |

## Scope

| Application ID | Code | Application | Roadmap item | Source |
|---|---|---|---|---|
| 04 | `APP-AIP` | [AI Advisor & Agent Platform](../applications/04-ai-advisor-agent-platform.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |

> **Commitment ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)):** this sprint commits the P0 (MVP) capabilities of its applications and treats P1 capabilities as stretch scope. Anything not finished is recorded as carry-over on the next sprint page.

## Scope changes from decisions

| Change | Decision and scope | FRD | Requirement |
|---|---|---|---|
| P0 added | [C21](../open-decisions.md#c21) Agent controls from [SUM] "Security & Governance Model": identity forwarding (scoped, short-lived token of the acting user); tool allowlists per agent, fixed at registration; human approval for high-risk writes above a defined threshold; audit of every agent tool call (agent identity, acting user, tool, arguments) | - | - |
| P0 added | [C21](../open-decisions.md#c21) Per-agent scoping for the MVP agents: the Advisory agent writes order drafts only (no order submission, no payment tools); the Support agent writes tickets only (no billing or payment tools, no account deletion) | - | - |
| P0 added | [C4](../open-decisions.md#c4) From 04.05 AI Agent Orchestration: 04.05.01.05 "Apply guardrails" and 04.05.01.06 "Audit agent action" | - | - |

## EIS 04 AI Advisor & Agent Platform

**Planned work ([PO] / [WB] description):** AI-guided selling, technical assistance and support

Full breakdown with APIs, services, entities and events: [applications/04-ai-advisor-agent-platform.md](../applications/04-ai-advisor-agent-platform.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [04.01 AI Product Advisor](../applications/04-ai-advisor-agent-platform.md#0401-ai-product-advisor) | 04.01.01 Requirement Discovery | Ask customer questions; Capture requirements; Identify constraints | P0 | Commit |
| [04.01 AI Product Advisor](../applications/04-ai-advisor-agent-platform.md#0401-ai-product-advisor) | 04.01.02 Recommendation | Search catalog; Evaluate compatibility; Rank products; Explain recommendation; Recommend configuration; Estimate cost | P0 | Commit |
| [04.02 AI Sales Agent](../applications/04-ai-advisor-agent-platform.md#0402-ai-sales-agent) | 04.02.01 Sales Assistance | Qualify lead; Explain pricing; Generate proposal; Generate quote; Recommend upsell; Recommend cross-sell | P1 | Stretch |
| [04.03 AI Technical Advisor](../applications/04-ai-advisor-agent-platform.md#0403-ai-technical-advisor) | 04.03.01 Technical Guidance | Recommend architecture; Explain configuration; Troubleshoot issue; Recommend best practice | P1 | Stretch |
| [04.04 AI Support Agent](../applications/04-ai-advisor-agent-platform.md#0404-ai-support-agent) | 04.04.01 Support Automation | Understand request; Search knowledge base; Diagnose issue; Recommend resolution; Create ticket; Escalate to human | P0 | Commit |
| [04.05 AI Agent Orchestration](../applications/04-ai-advisor-agent-platform.md#0405-ai-agent-orchestration) | 04.05.01 Agent Runtime | Register agent; Route request; Select tools; Manage context; Apply guardrails (P0); Audit agent action (P0) | P0 (2 functions) · P1 | Commit · Stretch |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - UJ-002 AI Guided Selection: Portal → AI Advisor → Catalog (AI, Catalog, Pricing) (applications 02, 08; [WB:User Journeys])
  - UJ-008 AI Technical Support (AI, Knowledge, Support, Service) (applications 11, 12, 10; [WB:User Journeys])

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). Deliverables are otherwise Not specified in any source.

## Decisions affecting this sprint

- [C3](../open-decisions.md#c3) The [PO] sprint order is authoritative; the MVP is complete at the end of sprint 2027.1.3.
- [C4](../open-decisions.md#c4), [C5](../open-decisions.md#c5), [C6](../open-decisions.md#c6) MVP, priority and phase as shown above.
- Sprint goal, team, capacity and status are Not specified.

Details: [open-decisions.md](../open-decisions.md).

## Traceability

```
[PO] roadmap row → SPRINT-2027.1.2 → application page → capability → feature → function (requirement candidate)
   → FRD docs/02-requirements/FRD/<feature>/ → REQ-<CODE>-<NNN> (Approved before build)
   → STORY-<CODE>-<NNN> with "Sprint (PI.Sprint)" = 2027.1.2
   → code (backend/ · frontend/ · ai-service/) → TC-<CODE>-<NNN> in test-cases/ → UAT
```

Stories for this sprint use `docs/02-requirements/functional-requirements/user-story-template.md` or the GitHub **User Story** issue template, with **Sprint (PI.Sprint) = 2027.1.2**. The Definition of Done is the one in the story template.

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)
- **Decisions:** [`open-decisions.md`](../open-decisions.md) (2026-09-25)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source or decision gives are written **Not specified**.
