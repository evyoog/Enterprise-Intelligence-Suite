# SPRINT-2027.1.3

| Field | Value |
|---|---|
| Sprint ID (PI.Sprint) | 2027.1.3 |
| PI – CY Quarter | 2027.1 |
| Start / end dates | 1–31 Mar 2027 ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Sprint goal | Not specified |
| Team / capacity | Not specified |
| Status | Not specified |
| Source | [PO] roadmap table ([`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)); decisions in [open-decisions.md](../open-decisions.md) |
| Previous / next sprint | [2027.1.2](SPRINT-2027.1.2.md) · [2027.2.1](SPRINT-2027.2.1.md) |

## Scope

| Application ID | Code | Application | Roadmap item | Source |
|---|---|---|---|---|
| 01b | `APP-PRT` (part) | [Enterprise Intelligence Suite](../applications/01-enterprise-intelligence-suite.md) | Dashboard and Global Search on live data | [PO], scope split by [C31](../open-decisions.md#c31) |
| 04b | `APP-AIP` (part) | [AI Advisor & Agent Platform](../applications/04-ai-advisor-agent-platform.md) | Customer-facing agents (04.01–04.04) | [C31](../open-decisions.md#c31): pulled back from 2027.1.2 |
| 03b | `APP-MKT` (part) | [Marketplace](../applications/03-marketplace.md) | Product Evaluation, Reviews & Ratings (03.02, 03.04) | [PO], scope split by [C31](../open-decisions.md#c31) |
| 11b | `APP-KNW` (part) | [Training & Knowledge Management](../applications/11-training-knowledge-management.md) | Learning, Training Delivery, Certification (11.02–11.04) | [PO], scope split by [C31](../open-decisions.md#c31) |
| 15b | `APP-GOV` (part) | [Administration & Governance](../applications/15-administration-governance.md) | Policy Management, Compliance (15.02, 15.04) | [C31](../open-decisions.md#c31): pulled forward from 2027.2.2 |

> **Commitment ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)):** this sprint commits the P0 (MVP) capabilities of its applications and treats P1 capabilities as stretch scope. Anything not finished is recorded as carry-over on the next sprint page.
>
> **This is the busiest sprint in the corrected sequence** (over 80 open functions across five application slices). 03b and 11b have no dependents and can move to [2027.2.1](SPRINT-2027.2.1.md) if the load here is too high ([C31](../open-decisions.md#c31)).

## Scope changes from decisions

| Change | Decision and scope | FRD | Requirement |
|---|---|---|---|
| Added | [C16](../open-decisions.md#c16) Semantic search (01.03.01), delivered with Knowledge (11a, already built in 2027.1.1). Moved from sprint [2026.3.3](SPRINT-2026.3.3.md) | - | - |
| Added | [C17](../open-decisions.md#c17) Global search (01.03.01) adds knowledge articles (11a) and support tickets (12a, already built in 2027.1.2) | - | - |
| Split | [C31](../open-decisions.md#c31) 01 splits: 01a (shell) was built in 2026.3.3; 01b (dashboard and search on live data) is here, since its data sources (02, 05, 07, 08, 10, 11a, 12a) are all built by the end of 2027.1.2 | - | - |
| Split | [C31](../open-decisions.md#c31) 04 splits: 04a (orchestration foundation) was built in [2027.1.2](SPRINT-2027.1.2.md); 04b (the customer-facing agents) is here | - | - |
| Split | [C31](../open-decisions.md#c31) 03 splits: 03a (Discovery, Checkout) was built in [2027.1.2](SPRINT-2027.1.2.md); 03b (Evaluation, Reviews) is here | - | - |
| Split | [C31](../open-decisions.md#c31) 11 splits: 11a (Knowledge Base) was built in [2027.1.1](SPRINT-2027.1.1.md); 11b (Learning, Training Delivery, Certification) is here | - | - |
| Split | [C31](../open-decisions.md#c31) 12 splits: 12a (human support) was built in [2027.1.2](SPRINT-2027.1.2.md); 12.02 AI Support ships here, with 04b, not as a separate slice | - | - |
| Moved in | [C31](../open-decisions.md#c31) 15b Policy & Compliance moves here from 2027.2.2, so partner onboarding and listing review (14, 2027.2.1) have a policy engine to check against | - | - |
| Retirement task, superseded | [C20](../open-decisions.md#c20)'s task to switch the interim status page's incidents to Incident & Problem Management (12.04) no longer applies here — 12.04 was built with 12a in [2027.1.2](SPRINT-2027.1.2.md) instead. Confirm the interim page can now be retired when 12a's incident feature actually ships | [service-status-page](../../../02-requirements/FRD/service-status-page/requirement.md) | REQ-PRT-001 |
| Decided | [C41](../open-decisions.md#c41) Unified Search (01.03.01, minus semantic search): a new cross-entity keyword search over products, published knowledge articles and the caller's own tickets — the first place these three are combined | [global-search](../../../02-requirements/FRD/global-search/requirement.md) | REQ-PRT-002 |
| Already satisfied | [C41](../open-decisions.md#c41) 01.02 Customer Dashboard's own "on live data" functions: `BusinessDashboardService` (built in earlier phases) already returns real organization/subscription/usage/service-health data — nothing new needed | - | - |
| Decided | [C41](../open-decisions.md#c41) Reviews & Ratings (03.04.01) built; a new customer-facing product detail page hosts it | [product-reviews](../../../02-requirements/FRD/product-reviews/requirement.md) | REQ-MKT-002 |
| Not built | [C41](../open-decisions.md#c41) 03.02 Product Evaluation (trials/demos/sandboxes) — needs 10 Service & Resource Management, still not built, to provision anything to trial against |
| Not built | [C41](../open-decisions.md#c41) 04b Customer-facing AI agents and 12.02 AI Support — same agent/LLM framework gap 04a already carries |
| Not built | [C41](../open-decisions.md#c41) 11b Learning & Certification — all P1/stretch, explicitly deferrable per this sprint's own note, no dependents |
| Not built | [C41](../open-decisions.md#c41) 15b Policy Management & Compliance — 15.02's priority is Not specified in any source; needs its own general policy-engine scoping decision |
| Decided | [C58](../open-decisions.md#c58) Vector store for semantic search (01.03.01.02): **pgvector** in the existing PostgreSQL database. Embedding model Not specified (depends on D8, LLM provider). Semantic search stays carried until D8 is decided; no FRD change yet | - | - |
| Decided | [C70](../open-decisions.md#c70) (2026-10-05) Keyword search improved (exact phrase, English/Spanish word forms, partial words, typos, exact IDs, ranking, highlighting, "Did you mean", suggestions, Ctrl/Cmd+K, re-indexing, rebuild, insights, synonyms) and semantic search (01.03.01.02, hybrid with pgvector, open-source multilingual model in ai-service, public content only). Built early on 2026-10-05; this sprint's dates are unchanged | [global-search](../../../02-requirements/FRD/global-search/requirement.md), [semantic-search](../../../02-requirements/FRD/semantic-search/requirement.md) | REQ-PRT-002, REQ-PRT-003 |
| Specified (Draft) | [C75](../open-decisions.md#c75)–[C77](../open-decisions.md#c77) (2026-10-05) Knowledge Center parts that belong here: video learning (11.03.02 watch progress, REQ-KNW-004), Academy (11b, built only if the product owner confirms — otherwise data model and navigation prepared), knowledge search extension (new types, transcripts, chapters, audience filter before ranking — [C76](../open-decisions.md#c76), reuses REQ-PRT-002/003), AI Knowledge Assistant contract (REQ-KNW-007, not built until D8, with 04b/12.02). Documents only; this sprint's dates are unchanged | [knowledge-videos](../../../02-requirements/FRD/knowledge-videos/requirement.md), [knowledge-center](../../../02-requirements/FRD/knowledge-center/requirement.md), [knowledge-assistant](../../../02-requirements/FRD/knowledge-assistant/requirement.md) | REQ-KNW-004, REQ-KNW-005, REQ-KNW-007 |

### FRDs in this sprint

| FRD | Requirement | Functions | Status |
|---|---|---|---|
| [global-search](../../../02-requirements/FRD/global-search/requirement.md) | REQ-PRT-002 | 01.03.01.01, .03–.05 (C70 improvements REQ-PRT-002.5–.17) | Approved (C70 additions approved 2026-10-05) |
| [semantic-search](../../../02-requirements/FRD/semantic-search/requirement.md) | REQ-PRT-003 | 01.03.01.02 | Approved (2026-10-05, C70) |
| [knowledge-assistant](../../../02-requirements/FRD/knowledge-assistant/requirement.md) | REQ-KNW-007 | 11.01.02 / 12.02.01 (prepared; answers need D8) | Specified (Draft) 2026-10-05 — waits for approval |
| [product-reviews](../../../02-requirements/FRD/product-reviews/requirement.md) | REQ-MKT-002 | 03.04.01 | Approved |

### Progress (as of 2026-09-28)

| Feature | Status | Note |
|---|---|---|
| 01.02 Customer Dashboard | Done (pre-existing) | Already satisfied by `BusinessDashboardService`, built in earlier phases; "View spending" stays unavailable pending 08 Billing |
| 01.03.01 Global Search | Done (built early, 2026-10-05) | Keyword search improved and semantic search built ([C70](../open-decisions.md#c70)); see the progress note below. The semantic part runs on the stub test model until a real model is configured |
| 03.02 Product Evaluation | Not started | Carried — needs 10 Service & Resource Management to provision anything to trial against |
| 03.04.01 Reviews & Ratings | Done (this FRD's scope) | Submit/rate/moderate/view all built; new product detail page |
| 04b Customer-facing AI agents (whole application) | Not started | Carried — same agent/LLM framework gap as 04a |
| 12.02 AI Support | Not started | Carried — ships with 04b once that framework decision is made |
| 11b Learning & Certification (whole application) | Not started | Carried — all P1/stretch, explicitly deferrable, no dependents |
| 15b Policy Management & Compliance (whole application) | Not started | Carried — needs its own general policy-engine scoping decision |

03.02, 04b, 12.02, 11b and 15b remain open for this sprint, each carried to a later one once the decision it needs is made. (01.03.01.02 was built early on 2026-10-05, C70.)

### Keyword and semantic search (C70) — built early on 2026-10-05

Built early on 2026-10-05, before this sprint starts (1–31 Mar 2027); the sprint's dates and scope are unchanged.

| Item | Result |
|---|---|
| Commits | `ce41979` (build, tests and documents) and the commit that records this hash |
| What | Search index (`search_document`, V020) with automatic re-indexing and admin rebuild; keyword search: exact phrase, English/Spanish word forms, accents, partial words, typos, exact IDs, ranking, highlighting, "Did you mean", type filters, history with Clear, zero-result help; top-bar suggestions and Ctrl/Cmd+K; results page with match labels; semantic search: ai-service `/embed`, passages, pgvector HNSW, hybrid merge, keyword fallback; insights and synonyms (`/admin/search`, `MANAGE_SEARCH`) |
| Quality (40 queries, EN + ES) | hit@5: before 20%, keyword 95%, hybrid 95% (stub model) — [quality report](../../../02-requirements/FRD/semantic-search/quality-report.md) |
| Speed (13,036 records) | keyword p95 273.7 ms (target < 500), hybrid p95 291.9 ms (target < 800; stub model, a real model's own time not measured) |
| Final values | typo threshold 0.4, "Did you mean" threshold 0.45, similarity threshold 0.45, chunk 600 characters with 100 overlap, k = 60 |
| Not done | A real embedding model (not chosen, and huggingface.co is blocked in the build environment): semantic quality with a real model, re-tuning of similarity and chunk size, and the model's own latency |

## EIS 01b Enterprise Intelligence Suite: dashboard and search on live data

**Planned work:** Wire the Customer Dashboard and Global Search (built as an empty shell with the Portal in 2026.3.3, [C31](../open-decisions.md#c31)) to the real data that now exists: organizations and subscriptions (05, 07), spending (08), usage and service health (10), and knowledge/ticket search (11a, 12a).

Full breakdown: [applications/01-enterprise-intelligence-suite.md](../applications/01-enterprise-intelligence-suite.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [01.02 Customer Dashboard](../applications/01-enterprise-intelligence-suite.md#0102-customer-dashboard) | 01.02.01 Business Overview | View organization summary; View subscriptions; View spending; View usage | P0 | Commit |
| [01.02 Customer Dashboard](../applications/01-enterprise-intelligence-suite.md#0102-customer-dashboard) | 01.02.02 Service Health | View service status; View alerts; View incidents | P0 | Commit |
| [01.03 Global Search](../applications/01-enterprise-intelligence-suite.md#0103-global-search) | 01.03.01 Unified Search | Keyword search; Semantic search ([C16](../open-decisions.md#c16)); Filter results; Sort results; View search history | P0 | Commit |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]:** 02 Catalog, 05 Tenant (2026.4.1); 07 Subscriptions, 08 Billing (2026.4.3); 10 Services, 12a Support (2027.1.2); 11a Knowledge base (2027.1.1). All land by the end of 2027.1.2, one sprint before this one.

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). The interim, manually-posted service status page (C20, built in 2026.3.3) is retired once 01.02.02/10.04 Health Monitoring covers the same ground.

## EIS 04b AI Advisor & Agent Platform: customer agents

**Planned work:** AI Product Advisor, AI Sales Agent, AI Technical Advisor, AI Support Agent — built on the orchestration foundation (04a) from [2027.1.2](SPRINT-2027.1.2.md). AI Support (12.02) ships here too, not as a separate 12 slice.

Full breakdown: [applications/04-ai-advisor-agent-platform.md](../applications/04-ai-advisor-agent-platform.md), [applications/12-support-service-management.md](../applications/12-support-service-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [04.01 AI Product Advisor](../applications/04-ai-advisor-agent-platform.md#0401-ai-product-advisor) | 04.01.01 Requirement Discovery | Ask customer questions; Capture requirements; Identify constraints | P0 | Commit |
| [04.01 AI Product Advisor](../applications/04-ai-advisor-agent-platform.md#0401-ai-product-advisor) | 04.01.02 Recommendation | Search catalog; Evaluate compatibility; Rank products; Explain recommendation; Recommend configuration; Estimate cost | P0 | Commit |
| [04.02 AI Sales Agent](../applications/04-ai-advisor-agent-platform.md#0402-ai-sales-agent) | 04.02.01 Sales Assistance | Qualify lead; Explain pricing; Generate proposal; Generate quote; Recommend upsell; Recommend cross-sell | P1 | Stretch |
| [04.03 AI Technical Advisor](../applications/04-ai-advisor-agent-platform.md#0403-ai-technical-advisor) | 04.03.01 Technical Guidance | Recommend architecture; Explain configuration; Troubleshoot issue; Recommend best practice | P1 | Stretch |
| [04.04 AI Support Agent](../applications/04-ai-advisor-agent-platform.md#0404-ai-support-agent) | 04.04.01 Support Automation | Understand request; Search knowledge base; Diagnose issue; Recommend resolution; Create ticket; Escalate to human | P0 | Commit |
| [12.02 AI Support](../applications/12-support-service-management.md#1202-ai-support) | 12.02.01 Conversational Support | Start conversation; Search knowledge; Diagnose issue; Recommend resolution; Execute permitted remediation | P0 | Commit |
| [12.02 AI Support](../applications/12-support-service-management.md#1202-ai-support) | 12.02.02 Human Handoff | Create ticket; Transfer conversation; Provide AI summary | P0 | Commit |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]:** 04a (orchestration, 2027.1.2), 11a Knowledge base (2027.1.1), 12a human support (2027.1.2, for escalation), 03a Checkout (2027.1.2, for the advisor's recommendations to be sellable).

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)), plus the C21 per-agent scoping (Advisory agent: order drafts only; Support agent: tickets only).

## EIS 03b Marketplace: trials and reviews

**Planned work:** Product Evaluation (trial, demo, sandbox) and Reviews & Ratings. 03a (Discovery, Checkout) was built in [2027.1.2](SPRINT-2027.1.2.md) ([C31](../open-decisions.md#c31)).

Full breakdown: [applications/03-marketplace.md](../applications/03-marketplace.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [03.02 Product Evaluation](../applications/03-marketplace.md#0302-product-evaluation) | 03.02.01 Evaluation | Start trial; Request demo; Launch sandbox; View prerequisites | P0 | Commit |
| [03.02 Product Evaluation](../applications/03-marketplace.md#0302-product-evaluation) | 03.02.02 Comparison | Compare products; Compare plans; Estimate cost | P0 | Commit |
| [03.04 Reviews & Ratings](../applications/03-marketplace.md#0304-reviews--ratings) | 03.04.01 Customer Feedback | Submit review; Rate product; Moderate review; View ratings | P0 | Commit |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]:** 03a Discovery & checkout (2027.1.2); trials/sandboxes need 10 Services (2027.1.2).

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). **Can move to [2027.2.1](SPRINT-2027.2.1.md)** if this sprint is overloaded ([C31](../open-decisions.md#c31)) — nothing later depends on it.

## EIS 11b Training & Knowledge Management: learning and certification

**Planned work:** Learning Management, Training Delivery, Certification. 11a (Knowledge Base) was built in [2027.1.1](SPRINT-2027.1.1.md) ([C31](../open-decisions.md#c31)).

Full breakdown: [applications/11-training-knowledge-management.md](../applications/11-training-knowledge-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [11.02 Learning Management](../applications/11-training-knowledge-management.md#1102-learning-management) | 11.02.01 Courses | Create course; Publish course; Enroll user; Track progress; Complete course | P1 | Stretch |
| [11.02 Learning Management](../applications/11-training-knowledge-management.md#1102-learning-management) | 11.02.02 Learning Paths | Create learning path; Assign learning path; Track path progress | P1 | Stretch |
| [11.03 Training Delivery](../applications/11-training-knowledge-management.md#1103-training-delivery) | 11.03.01 Labs & Assessments | Launch lab; Submit assessment; Score assessment; Track completion | P1 | Stretch |
| [11.03 Training Delivery](../applications/11-training-knowledge-management.md#1103-training-delivery) | 11.03.02 Video Learning | Stream video; Track watch progress | P1 | Stretch |
| [11.04 Certification](../applications/11-training-knowledge-management.md#1104-certification) | 11.04.01 Certificates | Define certification; Issue certificate; Verify certificate; Expire certificate | P1 | Stretch |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]:** 11a Knowledge base (2027.1.1).

### Expected deliverables

- All-P1 (stretch) scope ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). **Can move to [2027.2.1](SPRINT-2027.2.1.md)** if this sprint is overloaded ([C31](../open-decisions.md#c31)) — nothing depends on it.

## EIS 15b Administration & Governance: policy and compliance

**Planned work:** Policy Management, Compliance. 15a (Audit, Platform Administration) was built in [2026.4.2](SPRINT-2026.4.2.md); 15c (Regional Operations) stays in [2027.2.2](SPRINT-2027.2.2.md) ([C31](../open-decisions.md#c31)).

Full breakdown: [applications/15-administration-governance.md](../applications/15-administration-governance.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [15.02 Policy Management](../applications/15-administration-governance.md#1502-policy-management) | 15.02.01 Policy Lifecycle | Create policy; Assign policy; Evaluate policy; Enforce policy; Manage exception | Not specified | Not specified |
| [15.04 Compliance](../applications/15-administration-governance.md#1504-compliance) | 15.04.01 Compliance Controls | Define control; Map requirement; Collect evidence; Track remediation | P1 | Stretch |
| [15.04 Compliance](../applications/15-administration-governance.md#1504-compliance) | 15.04.02 Data Governance | Classify data; Define retention; Apply retention | P1 | Stretch |

Note: 06.02.02 "Policy" for this sprint's purposes is the **organization MFA policy**, already built in 2026.3.3 ([C21](../open-decisions.md#c21)); 15.02 here is the **general** policy engine C21 refers to.

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]:** 15a Audit & Platform Administration (2026.4.2); needed by Partner & Provider (14, 2027.2.1) for listing review.

### Expected deliverables

- The capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)); 15.02 Policy Lifecycle's priority is Not specified in any source.

## Decisions affecting this sprint

- [C3](../open-decisions.md#c3) The [PO] sprint order is authoritative; the MVP is complete at the end of sprint 2027.1.3.
- [C4](../open-decisions.md#c4), [C5](../open-decisions.md#c5), [C6](../open-decisions.md#c6) MVP, priority and phase as shown above.
- [C10](../open-decisions.md#c10) Application 01 is named "Enterprise Intelligence Suite".
- [C21](../open-decisions.md#c21) General policy engine (15.02) is distinct from the organization MFA policy (06.02.02), already built.
- [C31](../open-decisions.md#c31) Corrected sprint sequence: 01b, 04b, 03b, 11b and 15b as shown above. This is the busiest sprint; 03b and 11b can move to 2027.2.1.
- [C58](../open-decisions.md#c58) pgvector for semantic search.
- Sprint goal, team, capacity and status are Not specified.

Details: [open-decisions.md](../open-decisions.md).

## Traceability

```
[PO] roadmap row → SPRINT-2027.1.3 → application page → capability → feature → function (requirement candidate)
   → FRD docs/02-requirements/FRD/<feature>/ → REQ-<CODE>-<NNN> (Approved before build)
   → STORY-<CODE>-<NNN> with "Sprint (PI.Sprint)" = 2027.1.3
   → code (backend/ · frontend/ · ai-service/) → TC-<CODE>-<NNN> in test-cases/ → UAT
```

Stories for this sprint use `docs/02-requirements/functional-requirements/user-story-template.md` or the GitHub **User Story** issue template, with **Sprint (PI.Sprint) = 2027.1.3**. The Definition of Done is the one in the story template.

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)
- **Decisions:** [`open-decisions.md`](../open-decisions.md) (2026-09-25)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source or decision gives are written **Not specified**.
