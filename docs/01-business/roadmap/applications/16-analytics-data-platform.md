# 16 Analytics & Data Platform

| Field | Value |
|---|---|
| Application ID | 16 ([WB] numbering; an `APP-<CODE>` code is not assigned in any source) |
| Application | Analytics & Data Platform |
| Description | Customer, product, operational and business analytics ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2027.1 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2027.1.1](../sprints/SPRINT-2027.1.1.md) |
| Capabilities / features / functions | 5 / 9 / 32 ([WB]) |
| Application status | Not specified |

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.

## Capabilities

| Capability ID | Capability | Features | Priority | MVP | AI relevant |
|---|---|---|---|---|---|
| [16.01](#1601-customer-analytics) | Customer Analytics | 16.01.01 Customer Usage, 16.01.02 Customer Value | P0 | Yes | No |
| [16.02](#1602-product-analytics) | Product Analytics | 16.02.01 Product Performance, 16.02.02 Product Usage | P0 | Yes | No |
| [16.03](#1603-operational-analytics) | Operational Analytics | 16.03.01 Operations | P0 | Yes | No |
| [16.04](#1604-business-analytics) | Business Analytics | 16.04.01 Financial KPIs, 16.04.02 Growth KPIs | P0 | Yes | No |
| [16.05](#1605-data-platform) | Data Platform | 16.05.01 Data Ingestion, 16.05.02 Data Management | P0 | Yes | No |

> The Priority and MVP values are copied from [WB:Capabilities]. Every capability in [WB] is P0 / MVP=Yes, which conflicts with the function-level MVP flags (C4 in [open-decisions.md](../open-decisions.md)).

## 16.01 Customer Analytics

### Feature 16.01.01 Customer Usage

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 16.01.01.01 | Analyze usage | No | No | Platform Service | `/customer-analytics/analyze-usage` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.01.01.02 | Analyze adoption | No | No | Platform Service | `/customer-analytics/analyze-adoption` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.01.01.03 | Analyze engagement | No | No | Platform Service | `/customer-analytics/analyze-engagement` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.01.01.04 | Calculate customer health | No | No | Platform Service | `/customer-analytics/calculate-customer-health` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |

### Feature 16.01.02 Customer Value

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 16.01.02.01 | Analyze spending | No | No | Platform Service | `/customer-analytics/analyze-spending` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.01.02.02 | Calculate customer lifetime value | No | No | Platform Service | `/customer-analytics/calculate-customer-lifetime-value` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.01.02.03 | Analyze churn | No | No | Platform Service | `/customer-analytics/analyze-churn` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |

## 16.02 Product Analytics

### Feature 16.02.01 Product Performance

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 16.02.01.01 | Analyze views | No | No | Platform Service | `/product-analytics/analyze-views` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.02.01.02 | Analyze trials | No | No | Platform Service | `/product-analytics/analyze-trials` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.02.01.03 | Analyze conversions | No | No | Platform Service | `/product-analytics/analyze-conversions` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.02.01.04 | Analyze subscriptions | No | No | Platform Service | `/product-analytics/analyze-subscriptions` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |

### Feature 16.02.02 Product Usage

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 16.02.02.01 | Analyze usage | No | No | Platform Service | `/product-analytics/analyze-usage` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.02.02.02 | Analyze feature adoption | No | No | Platform Service | `/product-analytics/analyze-feature-adoption` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.02.02.03 | Analyze churn | No | No | Platform Service | `/product-analytics/analyze-churn` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |

## 16.03 Operational Analytics

### Feature 16.03.01 Operations

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 16.03.01.01 | Analyze incidents | No | No | Platform Service | `/operational-analytics/analyze-incidents` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.03.01.02 | Analyze SLA | No | No | Platform Service | `/operational-analytics/analyze-sla` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.03.01.03 | Analyze provisioning time | No | No | Platform Service | `/operational-analytics/analyze-provisioning-time` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.03.01.04 | Analyze service health | No | No | Platform Service | `/operational-analytics/analyze-service-health` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |

## 16.04 Business Analytics

### Feature 16.04.01 Financial KPIs

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 16.04.01.01 | Calculate revenue | No | No | Platform Service | `/business-analytics/calculate-revenue` | API-021 GET /v1/analytics | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.04.01.02 | Calculate ARR | No | No | Platform Service | `/business-analytics/calculate-arr` | API-021 GET /v1/analytics | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.04.01.03 | Calculate MRR | No | No | Platform Service | `/business-analytics/calculate-mrr` | API-021 GET /v1/analytics | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.04.01.04 | Calculate marketplace GMV | No | No | Platform Service | `/business-analytics/calculate-marketplace-gmv` | API-021 GET /v1/analytics | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |

### Feature 16.04.02 Growth KPIs

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 16.04.02.01 | Calculate acquisition | No | No | Platform Service | `/business-analytics/calculate-acquisition` | API-021 GET /v1/analytics | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.04.02.02 | Calculate churn | No | No | Platform Service | `/business-analytics/calculate-churn` | API-021 GET /v1/analytics | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.04.02.03 | Calculate conversion | No | No | Platform Service | `/business-analytics/calculate-conversion` | API-021 GET /v1/analytics | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |

## 16.05 Data Platform

### Feature 16.05.01 Data Ingestion

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 16.05.01.01 | Ingest operational data | No | No | Platform Service | `/data-platform/ingest-operational-data` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.05.01.02 | Ingest event data | No | No | Platform Service | `/data-platform/ingest-event-data` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.05.01.03 | Validate data | No | No | Platform Service | `/data-platform/validate-data` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |

### Feature 16.05.02 Data Management

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 16.05.02.01 | Transform data | No | No | Platform Service | `/data-platform/transform-data` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.05.02.02 | Catalog data | No | No | Platform Service | `/data-platform/catalog-data` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.05.02.03 | Manage lineage | No | No | Platform Service | `/data-platform/manage-lineage` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.05.02.04 | Manage data quality | No | No | Platform Service | `/data-platform/manage-data-quality` | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| all | Analytics consumes EVT-001, EVT-015 and EVT-018 | [WB:Events] |
| 01, 07, 04 | UJ-012 Renewal and Expansion (Analytics, AI, Subscription, Billing) | [WB:User Journeys] |

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
