# 04 AI Advisor & Agent Platform

| Field | Value |
|---|---|
| Application ID | 04 ([WB] numbering; an `APP-<CODE>` code is not assigned in any source) |
| Application | AI Advisor & Agent Platform |
| Description | AI-guided selling, technical assistance and support ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2027.1 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2027.1.2](../sprints/SPRINT-2027.1.2.md) |
| Capabilities / features / functions | 5 / 6 / 31 ([WB]) |
| Application status | Not specified |

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.

## Capabilities

| Capability ID | Capability | Features | Priority | MVP | AI relevant |
|---|---|---|---|---|---|
| [04.01](#0401-ai-product-advisor) | AI Product Advisor | 04.01.01 Requirement Discovery, 04.01.02 Recommendation | P0 | Yes | Yes |
| [04.02](#0402-ai-sales-agent) | AI Sales Agent | 04.02.01 Sales Assistance | P0 | Yes | Yes |
| [04.03](#0403-ai-technical-advisor) | AI Technical Advisor | 04.03.01 Technical Guidance | P0 | Yes | Yes |
| [04.04](#0404-ai-support-agent) | AI Support Agent | 04.04.01 Support Automation | P0 | Yes | Yes |
| [04.05](#0405-ai-agent-orchestration) | AI Agent Orchestration | 04.05.01 Agent Runtime | P0 | Yes | Yes |

> The Priority and MVP values are copied from [WB:Capabilities]. Every capability in [WB] is P0 / MVP=Yes, which conflicts with the function-level MVP flags (C4 in [open-decisions.md](../open-decisions.md)).

## 04.01 AI Product Advisor

### Feature 04.01.01 Requirement Discovery

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 04.01.01.01 | Ask customer questions | No | Yes | AI Agent | `/ai-product-advisor/ask-customer-questions` | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.01.01.02 | Capture requirements | No | Yes | AI Agent | `/ai-product-advisor/capture-requirements` | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.01.01.03 | Identify constraints | No | Yes | AI Agent | `/ai-product-advisor/identify-constraints` | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |

### Feature 04.01.02 Recommendation

Priority P1 · MVP Yes · AI required Yes ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 04.01.02.01 | Search catalog | No | Yes | AI Agent | `/ai-product-advisor/search-catalog` | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.01.02.02 | Evaluate compatibility | No | Yes | AI Agent | `/ai-product-advisor/evaluate-compatibility` | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.01.02.03 | Rank products | No | Yes | AI Agent | `/ai-product-advisor/rank-products` | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.01.02.04 | Explain recommendation | No | Yes | AI Agent | `/ai-product-advisor/explain-recommendation` | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.01.02.05 | Recommend configuration | No | Yes | AI Agent | `/ai-product-advisor/recommend-configuration` | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.01.02.06 | Estimate cost | No | Yes | AI Agent | `/ai-product-advisor/estimate-cost` | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |

## 04.02 AI Sales Agent

### Feature 04.02.01 Sales Assistance

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 04.02.01.01 | Qualify lead | No | Yes | AI Agent | `/ai-sales-agent/qualify-lead` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.02.01.02 | Explain pricing | No | Yes | AI Agent | `/ai-sales-agent/explain-pricing` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.02.01.03 | Generate proposal | No | Yes | AI Agent | `/ai-sales-agent/generate-proposal` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.02.01.04 | Generate quote | No | Yes | AI Agent | `/ai-sales-agent/generate-quote` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.02.01.05 | Recommend upsell | No | Yes | AI Agent | `/ai-sales-agent/recommend-upsell` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.02.01.06 | Recommend cross-sell | No | Yes | AI Agent | `/ai-sales-agent/recommend-cross-sell` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |

## 04.03 AI Technical Advisor

### Feature 04.03.01 Technical Guidance

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 04.03.01.01 | Recommend architecture | No | Yes | AI Agent | `/ai-technical-advisor/recommend-architecture` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.03.01.02 | Explain configuration | No | Yes | AI Agent | `/ai-technical-advisor/explain-configuration` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.03.01.03 | Troubleshoot issue | No | Yes | AI Agent | `/ai-technical-advisor/troubleshoot-issue` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.03.01.04 | Recommend best practice | No | Yes | AI Agent | `/ai-technical-advisor/recommend-best-practice` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |

## 04.04 AI Support Agent

### Feature 04.04.01 Support Automation

Priority P1 · MVP Yes · AI required Yes ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 04.04.01.01 | Understand request | No | Yes | AI Agent | `/ai-support-agent/understand-request` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.04.01.02 | Search knowledge base | No | Yes | AI Agent | `/ai-support-agent/search-knowledge-base` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.04.01.03 | Diagnose issue | No | Yes | AI Agent | `/ai-support-agent/diagnose-issue` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.04.01.04 | Recommend resolution | No | Yes | AI Agent | `/ai-support-agent/recommend-resolution` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.04.01.05 | Create ticket | No | Yes | AI Agent | `/ai-support-agent/create-ticket` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.04.01.06 | Escalate to human | No | Yes | AI Agent | `/ai-support-agent/escalate-to-human` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |

## 04.05 AI Agent Orchestration

### Feature 04.05.01 Agent Runtime

Priority P1 · MVP Yes · AI required Yes ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 04.05.01.01 | Register agent | No | Yes | AI Agent | `/ai-agent-orchestration/register-agent` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.05.01.02 | Route request | No | Yes | AI Agent | `/ai-agent-orchestration/route-request` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.05.01.03 | Select tools | No | Yes | AI Agent | `/ai-agent-orchestration/select-tools` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.05.01.04 | Manage context | No | Yes | AI Agent | `/ai-agent-orchestration/manage-context` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.05.01.05 | Apply guardrails | No | Yes | AI Agent | `/ai-agent-orchestration/apply-guardrails` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.05.01.06 | Audit agent action | No | Yes | AI Agent | `/ai-agent-orchestration/audit-agent-action` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 02, 08 | UJ-002 AI Guided Selection: Portal → AI Advisor → Catalog (AI, Catalog, Pricing) | [WB:User Journeys] |
| 11, 12, 10 | UJ-008 AI Technical Support (AI, Knowledge, Support, Service) | [WB:User Journeys] |

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
