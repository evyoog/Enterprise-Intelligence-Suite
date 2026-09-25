# 16 Analytics & Data Platform

| Field | Value |
|---|---|
| Application ID | 16 ([WB] numbering) |
| Application code | `APP-ANL` ([DN-5](../open-decisions.md#dn-5-application-codes)); IDs use `ANL`, for example `REQ-ANL-001` |
| Application | Analytics & Data Platform |
| Description | Customer, product, operational and business analytics ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2027.1 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2027.1.1](../sprints/SPRINT-2027.1.1.md) (1–31 Jan 2027, [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Capabilities / features / functions | 5 / 9 / 32 ([WB]) |
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
| [16.01](#1601-customer-analytics) | Customer Analytics | 16.01.01 Customer Usage, 16.01.02 Customer Value | No | Phase 3 | P1 | [WB:Roadmap] "Analytics" |
| [16.02](#1602-product-analytics) | Product Analytics | 16.02.01 Product Performance, 16.02.02 Product Usage | No | Phase 3 | P1 | [WB:Roadmap] "Analytics" |
| [16.03](#1603-operational-analytics) | Operational Analytics | 16.03.01 Operations | No | Not specified | Not specified | Not in [WB:Roadmap] |
| [16.04](#1604-business-analytics) | Business Analytics | 16.04.01 Financial KPIs, 16.04.02 Growth KPIs | No | Phase 3 | P1 | [WB:Roadmap] "Analytics" |
| [16.05](#1605-data-platform) | Data Platform | 16.05.01 Data Ingestion, 16.05.02 Data Management | No | Phase 3 | P1 | [WB:Roadmap] "Analytics" |

## 16.01 Customer Analytics

### Feature 16.01.01 Customer Usage

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 16.01.01.01 | Analyze usage | No | Phase 3 | P1 | No | Platform Service | `/customer-analytics/analyze-usage` | - | Analytics Service | - | - | UJ-005 Provision Service |
| 16.01.01.02 | Analyze adoption | No | Phase 3 | P1 | No | Platform Service | `/customer-analytics/analyze-adoption` | - | Analytics Service | - | - | UJ-005 Provision Service |
| 16.01.01.03 | Analyze engagement | No | Phase 3 | P1 | No | Platform Service | `/customer-analytics/analyze-engagement` | - | Analytics Service | - | - | UJ-005 Provision Service |
| 16.01.01.04 | Calculate customer health | No | Phase 3 | P1 | No | Platform Service | `/customer-analytics/calculate-customer-health` | - | Analytics Service | - | - | UJ-005 Provision Service |

### Feature 16.01.02 Customer Value

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 16.01.02.01 | Analyze spending | No | Phase 3 | P1 | No | Platform Service | `/customer-analytics/analyze-spending` | - | Analytics Service | - | - | UJ-005 Provision Service |
| 16.01.02.02 | Calculate customer lifetime value | No | Phase 3 | P1 | No | Platform Service | `/customer-analytics/calculate-customer-lifetime-value` | - | Analytics Service | - | - | UJ-005 Provision Service |
| 16.01.02.03 | Analyze churn | No | Phase 3 | P1 | No | Platform Service | `/customer-analytics/analyze-churn` | - | Analytics Service | - | - | UJ-005 Provision Service |

## 16.02 Product Analytics

### Feature 16.02.01 Product Performance

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 16.02.01.01 | Analyze views | No | Phase 3 | P1 | No | Platform Service | `/product-analytics/analyze-views` | - | Analytics Service | - | - | UJ-005 Provision Service |
| 16.02.01.02 | Analyze trials | No | Phase 3 | P1 | No | Platform Service | `/product-analytics/analyze-trials` | - | Analytics Service | - | - | UJ-005 Provision Service |
| 16.02.01.03 | Analyze conversions | No | Phase 3 | P1 | No | Platform Service | `/product-analytics/analyze-conversions` | - | Analytics Service | - | - | UJ-005 Provision Service |
| 16.02.01.04 | Analyze subscriptions | No | Phase 3 | P1 | No | Platform Service | `/product-analytics/analyze-subscriptions` | - | Analytics Service | - | - | UJ-005 Provision Service |

### Feature 16.02.02 Product Usage

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 16.02.02.01 | Analyze usage | No | Phase 3 | P1 | No | Platform Service | `/product-analytics/analyze-usage` | - | Analytics Service | - | - | UJ-005 Provision Service |
| 16.02.02.02 | Analyze feature adoption | No | Phase 3 | P1 | No | Platform Service | `/product-analytics/analyze-feature-adoption` | - | Analytics Service | - | - | UJ-005 Provision Service |
| 16.02.02.03 | Analyze churn | No | Phase 3 | P1 | No | Platform Service | `/product-analytics/analyze-churn` | - | Analytics Service | - | - | UJ-005 Provision Service |

## 16.03 Operational Analytics

### Feature 16.03.01 Operations

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 16.03.01.01 | Analyze incidents | No | Not specified | Not specified | No | Platform Service | `/operational-analytics/analyze-incidents` | - | Analytics Service | - | - | UJ-005 Provision Service |
| 16.03.01.02 | Analyze SLA | No | Not specified | Not specified | No | Platform Service | `/operational-analytics/analyze-sla` | - | Analytics Service | - | - | UJ-005 Provision Service |
| 16.03.01.03 | Analyze provisioning time | No | Not specified | Not specified | No | Platform Service | `/operational-analytics/analyze-provisioning-time` | - | Analytics Service | - | - | UJ-005 Provision Service |
| 16.03.01.04 | Analyze service health | No | Not specified | Not specified | No | Platform Service | `/operational-analytics/analyze-service-health` | - | Analytics Service | - | - | UJ-005 Provision Service |

## 16.04 Business Analytics

### Feature 16.04.01 Financial KPIs

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 16.04.01.01 | Calculate revenue | No | Phase 3 | P1 | No | Platform Service | `/business-analytics/calculate-revenue` | API-021 GET /v1/analytics | Analytics Service | - | - | UJ-005 Provision Service |
| 16.04.01.02 | Calculate ARR | No | Phase 3 | P1 | No | Platform Service | `/business-analytics/calculate-arr` | API-021 GET /v1/analytics | Analytics Service | - | - | UJ-005 Provision Service |
| 16.04.01.03 | Calculate MRR | No | Phase 3 | P1 | No | Platform Service | `/business-analytics/calculate-mrr` | API-021 GET /v1/analytics | Analytics Service | - | - | UJ-005 Provision Service |
| 16.04.01.04 | Calculate marketplace GMV | No | Phase 3 | P1 | No | Platform Service | `/business-analytics/calculate-marketplace-gmv` | API-021 GET /v1/analytics | Analytics Service | - | - | UJ-005 Provision Service |

### Feature 16.04.02 Growth KPIs

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 16.04.02.01 | Calculate acquisition | No | Phase 3 | P1 | No | Platform Service | `/business-analytics/calculate-acquisition` | API-021 GET /v1/analytics | Analytics Service | - | - | UJ-005 Provision Service |
| 16.04.02.02 | Calculate churn | No | Phase 3 | P1 | No | Platform Service | `/business-analytics/calculate-churn` | API-021 GET /v1/analytics | Analytics Service | - | - | UJ-005 Provision Service |
| 16.04.02.03 | Calculate conversion | No | Phase 3 | P1 | No | Platform Service | `/business-analytics/calculate-conversion` | API-021 GET /v1/analytics | Analytics Service | - | - | UJ-005 Provision Service |

## 16.05 Data Platform

### Feature 16.05.01 Data Ingestion

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 16.05.01.01 | Ingest operational data | No | Phase 3 | P1 | No | Platform Service | `/data-platform/ingest-operational-data` | - | Analytics Service | - | - | UJ-005 Provision Service |
| 16.05.01.02 | Ingest event data | No | Phase 3 | P1 | No | Platform Service | `/data-platform/ingest-event-data` | - | Analytics Service | - | - | UJ-005 Provision Service |
| 16.05.01.03 | Validate data | No | Phase 3 | P1 | No | Platform Service | `/data-platform/validate-data` | - | Analytics Service | - | - | UJ-005 Provision Service |

### Feature 16.05.02 Data Management

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 16.05.02.01 | Transform data | No | Phase 3 | P1 | No | Platform Service | `/data-platform/transform-data` | - | Analytics Service | - | - | UJ-005 Provision Service |
| 16.05.02.02 | Catalog data | No | Phase 3 | P1 | No | Platform Service | `/data-platform/catalog-data` | - | Analytics Service | - | - | UJ-005 Provision Service |
| 16.05.02.03 | Manage lineage | No | Phase 3 | P1 | No | Platform Service | `/data-platform/manage-lineage` | - | Analytics Service | - | - | UJ-005 Provision Service |
| 16.05.02.04 | Manage data quality | No | Phase 3 | P1 | No | Platform Service | `/data-platform/manage-data-quality` | - | Analytics Service | - | - | UJ-005 Provision Service |

> **Columns from [WB:Traceability]** (Primary API, Microservice, Entity, Event, Journey) are kept for reference only. Under [C13](../open-decisions.md#c13) microservices are logical domains built as modules in the single backend. Under [C14](../open-decisions.md#c14) the implemented endpoints and each FRD's `api-requirements.md` are the source of truth for APIs.

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| all | Analytics consumes EVT-001, EVT-015 and EVT-018 | [WB:Events] |
| 01, 07, 04 | UJ-012 Renewal and Expansion (Analytics, AI, Subscription, Billing) | [WB:User Journeys] |

## Deliverables

Not specified in any source. Under [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates) the sprint commits this application's P0 capabilities; P1 capabilities are stretch scope.

## Requirements, design and tests

| Artifact | Location | Status |
|---|---|---|
| Feature FRD | `docs/02-requirements/FRD/<feature>/` (copy `_template/`) | See the sprint page for FRDs in progress |
| Requirement | `REQ-ANL-<NNN>` inside the FRD | Approved FRD required before build ([DN-4](../open-decisions.md#dn-4-business-rules-and-acceptance-criteria)) |
| Business rules | `docs/03-business-rules/` and `FRD/<feature>/business-rules.md` | Per FRD |
| Test cases | `test-cases/functional/<feature>/TC-ANL-<NNN>.md` | Not created |
| NFRs | [WB:Non-Functional Requirements] NFR-001 to NFR-014 (platform-wide) | See [EIS-document-analysis.md](../EIS-document-analysis.md) section 2.17 |
