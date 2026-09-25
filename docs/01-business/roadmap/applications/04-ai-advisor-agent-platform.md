# 04 AI Advisor & Agent Platform

| Field | Value |
|---|---|
| Application ID | 04 ([WB] numbering) |
| Application code | `APP-AIP` ([DN-5](../open-decisions.md#dn-5-application-codes)); IDs use `AIP`, for example `REQ-AIP-001` |
| Application | AI Advisor & Agent Platform |
| Description | AI-guided selling, technical assistance and support ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2027.1 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2027.1.2](../sprints/SPRINT-2027.1.2.md) (1–28 Feb 2027, [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Capabilities / features / functions | 5 / 6 / 31 ([WB]) |
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
| [04.01](#0401-ai-product-advisor) | AI Product Advisor | 04.01.01 Requirement Discovery, 04.01.02 Recommendation | Yes | Phase 1 / MVP | P0 | C4 |
| [04.02](#0402-ai-sales-agent) | AI Sales Agent | 04.02.01 Sales Assistance | No | Phase 2 | P1 | [WB:Roadmap] "AI Expansion" |
| [04.03](#0403-ai-technical-advisor) | AI Technical Advisor | 04.03.01 Technical Guidance | No | Phase 2 | P1 | [WB:Roadmap] "AI Expansion" |
| [04.04](#0404-ai-support-agent) | AI Support Agent | 04.04.01 Support Automation | Yes | Phase 1 / MVP | P0 | C4 |
| [04.05](#0405-ai-agent-orchestration) | AI Agent Orchestration | 04.05.01 Agent Runtime | Partly | Phase 1 / MVP (2 functions) · Phase 2 | P0 (2 functions) · P1 | C4, [WB:Roadmap] "AI Expansion" |

## 04.01 AI Product Advisor

### Feature 04.01.01 Requirement Discovery

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 04.01.01.01 | Ask customer questions | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-product-advisor/ask-customer-questions` | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.01.01.02 | Capture requirements | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-product-advisor/capture-requirements` | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.01.01.03 | Identify constraints | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-product-advisor/identify-constraints` | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |

### Feature 04.01.02 Recommendation

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: Yes.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 04.01.02.01 | Search catalog | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-product-advisor/search-catalog` | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.01.02.02 | Evaluate compatibility | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-product-advisor/evaluate-compatibility` | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.01.02.03 | Rank products | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-product-advisor/rank-products` | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.01.02.04 | Explain recommendation | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-product-advisor/explain-recommendation` | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.01.02.05 | Recommend configuration | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-product-advisor/recommend-configuration` | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.01.02.06 | Estimate cost | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-product-advisor/estimate-cost` | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |

## 04.02 AI Sales Agent

### Feature 04.02.01 Sales Assistance

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 04.02.01.01 | Qualify lead | No | Phase 2 | P1 | Yes | AI Agent | `/ai-sales-agent/qualify-lead` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.02.01.02 | Explain pricing | No | Phase 2 | P1 | Yes | AI Agent | `/ai-sales-agent/explain-pricing` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.02.01.03 | Generate proposal | No | Phase 2 | P1 | Yes | AI Agent | `/ai-sales-agent/generate-proposal` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.02.01.04 | Generate quote | No | Phase 2 | P1 | Yes | AI Agent | `/ai-sales-agent/generate-quote` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.02.01.05 | Recommend upsell | No | Phase 2 | P1 | Yes | AI Agent | `/ai-sales-agent/recommend-upsell` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.02.01.06 | Recommend cross-sell | No | Phase 2 | P1 | Yes | AI Agent | `/ai-sales-agent/recommend-cross-sell` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |

## 04.03 AI Technical Advisor

### Feature 04.03.01 Technical Guidance

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 04.03.01.01 | Recommend architecture | No | Phase 2 | P1 | Yes | AI Agent | `/ai-technical-advisor/recommend-architecture` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.03.01.02 | Explain configuration | No | Phase 2 | P1 | Yes | AI Agent | `/ai-technical-advisor/explain-configuration` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.03.01.03 | Troubleshoot issue | No | Phase 2 | P1 | Yes | AI Agent | `/ai-technical-advisor/troubleshoot-issue` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.03.01.04 | Recommend best practice | No | Phase 2 | P1 | Yes | AI Agent | `/ai-technical-advisor/recommend-best-practice` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |

## 04.04 AI Support Agent

### Feature 04.04.01 Support Automation

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: Yes.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 04.04.01.01 | Understand request | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-support-agent/understand-request` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.04.01.02 | Search knowledge base | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-support-agent/search-knowledge-base` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.04.01.03 | Diagnose issue | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-support-agent/diagnose-issue` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.04.01.04 | Recommend resolution | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-support-agent/recommend-resolution` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.04.01.05 | Create ticket | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-support-agent/create-ticket` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.04.01.06 | Escalate to human | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-support-agent/escalate-to-human` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |

## 04.05 AI Agent Orchestration

### Feature 04.05.01 Agent Runtime

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: Yes.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 04.05.01.01 | Register agent | No | Phase 2 | P1 | Yes | AI Agent | `/ai-agent-orchestration/register-agent` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.05.01.02 | Route request | No | Phase 2 | P1 | Yes | AI Agent | `/ai-agent-orchestration/route-request` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.05.01.03 | Select tools | No | Phase 2 | P1 | Yes | AI Agent | `/ai-agent-orchestration/select-tools` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.05.01.04 | Manage context | No | Phase 2 | P1 | Yes | AI Agent | `/ai-agent-orchestration/manage-context` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.05.01.05 | Apply guardrails | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-agent-orchestration/apply-guardrails` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |
| 04.05.01.06 | Audit agent action | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-agent-orchestration/audit-agent-action` | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection |

> **Columns from [WB:Traceability]** (Primary API, Microservice, Entity, Event, Journey) are kept for reference only. Under [C13](../open-decisions.md#c13) microservices are logical domains built as modules in the single backend. Under [C14](../open-decisions.md#c14) the implemented endpoints and each FRD's `api-requirements.md` are the source of truth for APIs.

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 02, 08 | UJ-002 AI Guided Selection: Portal → AI Advisor → Catalog (AI, Catalog, Pricing) | [WB:User Journeys] |
| 11, 12, 10 | UJ-008 AI Technical Support (AI, Knowledge, Support, Service) | [WB:User Journeys] |

## Deliverables

Not specified in any source. Under [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates) the sprint commits this application's P0 capabilities; P1 capabilities are stretch scope.

## Requirements, design and tests

| Artifact | Location | Status |
|---|---|---|
| Feature FRD | `docs/02-requirements/FRD/<feature>/` (copy `_template/`) | See the sprint page for FRDs in progress |
| Requirement | `REQ-AIP-<NNN>` inside the FRD | Approved FRD required before build ([DN-4](../open-decisions.md#dn-4-business-rules-and-acceptance-criteria)) |
| Business rules | `docs/03-business-rules/` and `FRD/<feature>/business-rules.md` | Per FRD |
| Test cases | `test-cases/functional/<feature>/TC-AIP-<NNN>.md` | Not created |
| NFRs | [WB:Non-Functional Requirements] NFR-001 to NFR-014 (platform-wide) | See [EIS-document-analysis.md](../EIS-document-analysis.md) section 2.17 |
