# SPRINT-2027.1.1

| Field | Value |
|---|---|
| Sprint ID (PI.Sprint) | 2027.1.1 |
| PI – CY Quarter | 2027.1 |
| Start / end dates | 1–31 Jan 2027 ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Sprint goal | Not specified |
| Team / capacity | Not specified |
| Status | Not specified |
| Source | [PO] roadmap table ([`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)); decisions in [open-decisions.md](../open-decisions.md) |
| Previous / next sprint | [2026.4.3](SPRINT-2026.4.3.md) · [2027.1.2](SPRINT-2027.1.2.md) |

## Scope

| Application ID | Code | Application | Roadmap item | Source |
|---|---|---|---|---|
| 09 | `APP-ORD` | [Order & Provisioning Management](../applications/09-order-provisioning-management.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |
| 11a | `APP-KNW` (part) | [Training & Knowledge Management](../applications/11-training-knowledge-management.md) | Knowledge Base only (11.01) | [C31](../open-decisions.md#c31): pulled forward from 2027.1.3 |
| 13b | `APP-INT` (part) | [Integration & API Platform](../applications/13-integration-api-platform.md) | Connectors and Webhooks only (13.02, 13.04) | [C31](../open-decisions.md#c31): 13a (API Management, Event Platform) moved to 2026.4.2 |
| 16 | `APP-ANL` | [Analytics & Data Platform](../applications/16-analytics-data-platform.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |

> **Commitment ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)):** this sprint commits the P0 (MVP) capabilities of its applications and treats P1 capabilities as stretch scope. Anything not finished is recorded as carry-over on the next sprint page.

## Scope changes from decisions

| Change | Decision and scope | FRD | Requirement |
|---|---|---|---|
| Retirement task | [C20](../open-decisions.md#c20) Switch per-product status on the interim status page to Health Monitoring (10.04). Note: 10.04 is P1 (stretch) under C4/C5; if it is not delivered, this task carries over (DN-2). 10 itself is no longer in this sprint (see below) — this task now carries into [2027.1.2](SPRINT-2027.1.2.md) instead | [service-status-page](../../../02-requirements/FRD/service-status-page/requirement.md) | REQ-PRT-001 |
| Moved out | [C31](../open-decisions.md#c31) 10 Service & Resource Management moves to [2027.1.2](SPRINT-2027.1.2.md) (service instances are created by provisioning, which is built in this sprint, so they can't be managed in the same sprint that builds provisioning) | - | - |
| Moved in | [C31](../open-decisions.md#c31) 11a Knowledge Base moves here from 2027.1.3 (customers need product knowledge before they buy, and both human support and AI agents answer from it) | - | - |
| Split | [C31](../open-decisions.md#c31) 13 Integration & API Platform splits: 13a (API Management, Event Platform) moved to [2026.4.2](SPRINT-2026.4.2.md); 13b (Connectors, Webhooks) stays here | - | - |
| Decided | [C39](../open-decisions.md#c39) Order Lifecycle (09.01), Provisioning folded into Approve (09.02), single-hop Approval (09.04): organization purchasing only — an individual customer's self-serve subscribe (07.01) is untouched. 09.03 Workflow Orchestration deliberately not built as a generic engine (one hard-coded process, no second consumer) | [order-lifecycle](../../../02-requirements/FRD/order-lifecycle/requirement.md) | REQ-ORD-001 |
| Decided | [C39](../open-decisions.md#c39) Knowledge Articles (11.01.01) built; 11.01.02 AI Knowledge carried — no vector-store/embeddings decision exists | [knowledge-base](../../../02-requirements/FRD/knowledge-base/requirement.md) | REQ-KNW-001 |
| Not built | [C39](../open-decisions.md#c39) 13b Connectors & Webhooks and 16 Analytics & Data Platform: both P1/stretch under C4/C5, and neither has a real consumer yet — carried further |
| Added | [C56](../open-decisions.md#c56) Provisioning contract (answer to D6, option B): EIS notifies each hosted product of subscription start, suspension, resumption and cancellation; the product creates or changes the tenant and reports back. FRD is a documents-only stub; the delivery mechanism (D13 events or D19 webhooks) is not decided | [provisioning-contract](../../../02-requirements/FRD/provisioning-contract/requirement.md) | REQ-ORD-002 |
| Decided | [C58](../open-decisions.md#c58) Vector store for 11.01.02 AI Knowledge: **pgvector** in the existing PostgreSQL database. Embedding model Not specified (depends on D8, LLM provider). 11.01.02 stays carried until D8 is decided; no FRD change yet | - | - |
| Decided | [C65](../open-decisions.md#c65) (D16) Who manages an organization's subscriptions: organization admins by default, delegable through the new *Manage subscriptions* feature permission. Covers 09.02.01.02/.04/.05 (configure, suspend, deprovision an organization subscription) — the gap C38/C39 carry. Draft; build waits for approval | [access-management](../../../02-requirements/FRD/access-management/requirement.md) | REQ-TEN-005.4 |
| Specified (Draft) | [C71](../open-decisions.md#c71)–[C77](../open-decisions.md#c77) (2026-10-05) Knowledge Center and Knowledge Management CMS: knowledge-base update (REQ-KNW-001.7–.11), content types and publishing workflow, private S3 media (D23 → [C72](../open-decisions.md#c72)), videos (YouTube, S3, external URL), Knowledge Center reader pages, analytics, AI assistant prepared (not built until D8), knowledge permissions. Documents only; nothing built until the product owner replies "Approved" per FRD. This sprint's dates are unchanged | [knowledge-base](../../../02-requirements/FRD/knowledge-base/requirement.md), [knowledge-content](../../../02-requirements/FRD/knowledge-content/requirement.md), [knowledge-media](../../../02-requirements/FRD/knowledge-media/requirement.md), [knowledge-videos](../../../02-requirements/FRD/knowledge-videos/requirement.md), [knowledge-center](../../../02-requirements/FRD/knowledge-center/requirement.md), [knowledge-analytics](../../../02-requirements/FRD/knowledge-analytics/requirement.md), [knowledge-permissions](../../../02-requirements/FRD/knowledge-permissions/requirement.md) | REQ-KNW-001 (update), REQ-KNW-002–006, REQ-KNW-008 |

### FRDs in this sprint

| FRD | Requirement | Functions | Status |
|---|---|---|---|
| [order-lifecycle](../../../02-requirements/FRD/order-lifecycle/requirement.md) | REQ-ORD-001 | 09.01.01, 09.02.01 (folded into approval), 09.04.01 (single-hop) | Approved |
| [knowledge-base](../../../02-requirements/FRD/knowledge-base/requirement.md) | REQ-KNW-001 | 11.01.01 (11.01.02 AI Knowledge carried) | Approved (update REQ-KNW-001.7–.11: Specified (Draft) 2026-10-05) |
| [provisioning-contract](../../../02-requirements/FRD/provisioning-contract/requirement.md) | REQ-ORD-002 | 09.02.01 (provision, activate, suspend, deprovision — contract only) | Draft (stub) |
| [access-management](../../../02-requirements/FRD/access-management/requirement.md) | REQ-TEN-005 (.4 only here) | 09.02.01.02/.04/.05 — who may configure, suspend, cancel an organization subscription | Draft — waits for approval |
| [knowledge-content](../../../02-requirements/FRD/knowledge-content/requirement.md) | REQ-KNW-002 | 11.01.01 (content types, workflow, versions) | Specified (Draft) 2026-10-05 — waits for approval |
| [knowledge-media](../../../02-requirements/FRD/knowledge-media/requirement.md) | REQ-KNW-003 | 11.01.01 (documents, images, templates in private S3) | Specified (Draft) 2026-10-05 — waits for approval |
| [knowledge-videos](../../../02-requirements/FRD/knowledge-videos/requirement.md) | REQ-KNW-004 | 11.01.01 / 11.03.02 (video library; watch progress with 11b) | Specified (Draft) 2026-10-05 — waits for approval |
| [knowledge-center](../../../02-requirements/FRD/knowledge-center/requirement.md) | REQ-KNW-005 | 11.01.01 (reader pages) | Specified (Draft) 2026-10-05 — waits for approval |
| [knowledge-analytics](../../../02-requirements/FRD/knowledge-analytics/requirement.md) | REQ-KNW-006 | 11.01.01 (views, feedback, search gaps) | Specified (Draft) 2026-10-05 — waits for approval |
| [knowledge-permissions](../../../02-requirements/FRD/knowledge-permissions/requirement.md) | REQ-KNW-008 | 11.01.01 (contributor / publisher) | Specified (Draft) 2026-10-05 — waits for approval |

### Progress (as of 2026-09-28)

| Feature | Status | Note |
|---|---|---|
| 09.01.01 Order Lifecycle | Done (this FRD's scope) | Organization purchasing only; Create/Validate/Price/Submit folded into one action |
| 09.02.01 Service Provisioning | Partly done | Provision/Activate folded into order approval; Configure/Suspend/Deprovision an existing org subscription not built — permission decided by [C65](../open-decisions.md#c65) (D16), specified in REQ-TEN-005 (Draft, 2026-10-03) |
| 09.03 Workflow Orchestration | Not started, not planned as a generic engine | See [C39](../open-decisions.md#c39) — no second orchestrated process exists to justify one |
| 09.04.01 Approvals | Partly done | Create/Approve/Reject built, single-hop; Route approval/Escalate not built — no multi-level chain defined |
| 11.01.01 Knowledge Articles | Done (this FRD's scope) | Create/Edit/Publish/Search/Version, plain text search |
| 11.01.02 AI Knowledge | Not started | Carried — needs a vector-store/embeddings-model decision first |
| 13b Connectors & Webhooks | Not started | Carried — P1/stretch, no real consumer yet |
| 16 Analytics & Data Platform | Not started | Carried — P1/stretch (16.03 has no priority given at all) |

09.02.01's remaining items, 09.03, 09.04.01's remaining items, 11.01.02, 13b, and 16 remain open for this sprint, carried to a later one once the decisions [C39](../open-decisions.md#c39) names are made.

## EIS 09 Order & Provisioning Management

**Planned work ([PO] / [WB] description):** Orders, provisioning and workflow orchestration

Full breakdown with APIs, services, entities and events: [applications/09-order-provisioning-management.md](../applications/09-order-provisioning-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [09.01 Order Management](../applications/09-order-provisioning-management.md#0901-order-management) | 09.01.01 Order Lifecycle | Create order; Validate order; Price order; Submit order; Approve order; Cancel order; Track order | P0 | Commit |
| [09.02 Provisioning](../applications/09-order-provisioning-management.md#0902-provisioning) | 09.02.01 Service Provisioning | Provision service; Configure service; Activate service; Suspend service; Deprovision service | P0 | Commit |
| [09.03 Workflow Orchestration](../applications/09-order-provisioning-management.md#0903-workflow-orchestration) | 09.03.01 Workflow Runtime | Trigger workflow; Execute workflow; Retry step; Rollback; Compensate; Escalate | P0 | Commit |
| [09.03 Workflow Orchestration](../applications/09-order-provisioning-management.md#0903-workflow-orchestration) | 09.03.02 Workflow Design | Define workflow; Configure step; Set dependency | P0 | Commit |
| [09.04 Approval Management](../applications/09-order-provisioning-management.md#0904-approval-management) | 09.04.01 Approvals | Create approval; Route approval; Approve; Reject; Escalate | P0 | Commit |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - EVT-005 CheckoutCompleted is consumed by Order (applications 03; [WB:Events])
  - UJ-005 Provision Service: Order accepted → Entitlement → Provision → Activate → Notify (applications 07; [WB:User Journeys])
  - EVT-012 ProvisioningStarted is consumed by Resource Service (applications 10; [WB:Events])

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). Deliverables are otherwise Not specified in any source.

## EIS 11a Training & Knowledge Management: Knowledge Base

**Planned work:** Knowledge Base only — 11b Learning & Training Delivery & Certification stays in [2027.1.3](SPRINT-2027.1.3.md) ([C31](../open-decisions.md#c31)).

Full breakdown: [applications/11-training-knowledge-management.md](../applications/11-training-knowledge-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [11.01 Knowledge Base](../applications/11-training-knowledge-management.md#1101-knowledge-base) | 11.01.01 Knowledge Articles | Create article; Edit article; Publish article; Search article; Version article | P0 | Commit |
| [11.01 Knowledge Base](../applications/11-training-knowledge-management.md#1101-knowledge-base) | 11.01.02 AI Knowledge | Index content; Retrieve relevant content; Validate source | P0 | Commit |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]:** consumed by human support (12a, 2027.1.2), the AI support agent (04b, 2027.1.3), and search (01b, 2027.1.3).

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)).

## EIS 13b Integration & API Platform: connectors and webhooks

**Planned work:** Integration Hub and Webhooks only — 13a (API Management, Event Platform) moved to [2026.4.2](SPRINT-2026.4.2.md) ([C31](../open-decisions.md#c31)).

Full breakdown: [applications/13-integration-api-platform.md](../applications/13-integration-api-platform.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [13.02 Integration Hub](../applications/13-integration-api-platform.md#1302-integration-hub) | 13.02.01 Connectors | Create connector; Authenticate connector; Test connector; Enable connector | P1 | Stretch |
| [13.02 Integration Hub](../applications/13-integration-api-platform.md#1302-integration-hub) | 13.02.02 Data Integration | Synchronize data; Transform data; Handle integration error | P1 | Stretch |
| [13.04 Webhooks](../applications/13-integration-api-platform.md#1304-webhooks) | 13.04.01 Webhook Management | Register webhook; Authenticate webhook; Trigger webhook; Retry webhook; Monitor webhook | P1 | Stretch |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]:** depends on 13a (API Management, Event Platform), built in 2026.4.2. First needed by Partner & Provider (14, 2027.2.1).

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). 13.01 API Management and 13.03 Event Platform (P0, Commit) are no longer in this sprint — see [2026.4.2](SPRINT-2026.4.2.md).

## EIS 16 Analytics & Data Platform

**Planned work ([PO] / [WB] description):** Customer, product, operational and business analytics

Full breakdown with APIs, services, entities and events: [applications/16-analytics-data-platform.md](../applications/16-analytics-data-platform.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [16.01 Customer Analytics](../applications/16-analytics-data-platform.md#1601-customer-analytics) | 16.01.01 Customer Usage | Analyze usage; Analyze adoption; Analyze engagement; Calculate customer health | P1 | Stretch |
| [16.01 Customer Analytics](../applications/16-analytics-data-platform.md#1601-customer-analytics) | 16.01.02 Customer Value | Analyze spending; Calculate customer lifetime value; Analyze churn | P1 | Stretch |
| [16.02 Product Analytics](../applications/16-analytics-data-platform.md#1602-product-analytics) | 16.02.01 Product Performance | Analyze views; Analyze trials; Analyze conversions; Analyze subscriptions | P1 | Stretch |
| [16.02 Product Analytics](../applications/16-analytics-data-platform.md#1602-product-analytics) | 16.02.02 Product Usage | Analyze usage; Analyze feature adoption; Analyze churn | P1 | Stretch |
| [16.03 Operational Analytics](../applications/16-analytics-data-platform.md#1603-operational-analytics) | 16.03.01 Operations | Analyze incidents; Analyze SLA; Analyze provisioning time; Analyze service health | Not specified | Not specified |
| [16.04 Business Analytics](../applications/16-analytics-data-platform.md#1604-business-analytics) | 16.04.01 Financial KPIs | Calculate revenue; Calculate ARR; Calculate MRR; Calculate marketplace GMV | P1 | Stretch |
| [16.04 Business Analytics](../applications/16-analytics-data-platform.md#1604-business-analytics) | 16.04.02 Growth KPIs | Calculate acquisition; Calculate churn; Calculate conversion | P1 | Stretch |
| [16.05 Data Platform](../applications/16-analytics-data-platform.md#1605-data-platform) | 16.05.01 Data Ingestion | Ingest operational data; Ingest event data; Validate data | P1 | Stretch |
| [16.05 Data Platform](../applications/16-analytics-data-platform.md#1605-data-platform) | 16.05.02 Data Management | Transform data; Catalog data; Manage lineage; Manage data quality | P1 | Stretch |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - Analytics consumes EVT-001, EVT-015 and EVT-018 (applications all; [WB:Events])
  - UJ-012 Renewal and Expansion (Analytics, AI, Subscription, Billing) (applications 01, 07, 04; [WB:User Journeys])

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). Deliverables are otherwise Not specified in any source.

## Decisions affecting this sprint

- [C3](../open-decisions.md#c3) The [PO] sprint order is authoritative; the MVP is complete at the end of sprint 2027.1.3.
- [C4](../open-decisions.md#c4), [C5](../open-decisions.md#c5), [C6](../open-decisions.md#c6) MVP, priority and phase as shown above.
- [C31](../open-decisions.md#c31) Corrected sprint sequence: 10 moved out, 11a and 13b moved/kept in as shown above.
- [C56](../open-decisions.md#c56) Provisioning contract; [C58](../open-decisions.md#c58) pgvector for AI knowledge.
- Sprint goal, team, capacity and status are Not specified.

Details: [open-decisions.md](../open-decisions.md).

## Traceability

```
[PO] roadmap row → SPRINT-2027.1.1 → application page → capability → feature → function (requirement candidate)
   → FRD docs/02-requirements/FRD/<feature>/ → REQ-<CODE>-<NNN> (Approved before build)
   → STORY-<CODE>-<NNN> with "Sprint (PI.Sprint)" = 2027.1.1
   → code (backend/ · frontend/ · ai-service/) → TC-<CODE>-<NNN> in test-cases/ → UAT
```

Stories for this sprint use `docs/02-requirements/functional-requirements/user-story-template.md` or the GitHub **User Story** issue template, with **Sprint (PI.Sprint) = 2027.1.1**. The Definition of Done is the one in the story template.

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)
- **Decisions:** [`open-decisions.md`](../open-decisions.md) (2026-09-25)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source or decision gives are written **Not specified**.
