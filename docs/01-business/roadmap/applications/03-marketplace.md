# 03 Marketplace

| Field | Value |
|---|---|
| Application ID | 03 ([WB] numbering) |
| Application code | `APP-MKT` ([DN-5](../open-decisions.md#dn-5-application-codes)); IDs use `MKT`, for example `REQ-MKT-001` |
| Application | Marketplace |
| Description | Discovery, evaluation, comparison and checkout ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2027.1 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | Split ([C31](../open-decisions.md#c31)): 03a Discovery & checkout in [2027.1.2](../sprints/SPRINT-2027.1.2.md) (1–28 Feb 2027); 03b Trials & reviews in [2027.1.3](../sprints/SPRINT-2027.1.3.md) (1–31 Mar 2027) |
| Capabilities / features / functions | 4 / 7 / 27 ([WB]) |
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
| [03.01](#0301-product-discovery) | Product Discovery | 03.01.01 Catalog Browsing, 03.01.02 Recommendations | Yes | Phase 1 / MVP | P0 | C4 |
| [03.02](#0302-product-evaluation) | Product Evaluation | 03.02.01 Evaluation, 03.02.02 Comparison | Yes | Phase 1 / MVP | P0 | C4 |
| [03.03](#0303-marketplace-checkout) | Marketplace Checkout | 03.03.01 Checkout, 03.03.02 Purchase Validation | Yes | Phase 1 / MVP | P0 | C4 |
| [03.04](#0304-reviews--ratings) | Reviews & Ratings | 03.04.01 Customer Feedback | Yes | Phase 1 / MVP | P0 | C4 |

## 03.01 Product Discovery

### Feature 03.01.01 Catalog Browsing

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 03.01.01.01 | Browse categories | Yes | Phase 1 / MVP | P0 | No | Customer | `/product-discovery/browse-categories` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 03.01.01.02 | Search products | Yes | Phase 1 / MVP | P0 | No | Customer | `/product-discovery/search-products` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 03.01.01.03 | Filter products | Yes | Phase 1 / MVP | P0 | No | Customer | `/product-discovery/filter-products` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 03.01.01.04 | Sort products | Yes | Phase 1 / MVP | P0 | No | Customer | `/product-discovery/sort-products` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |

### Feature 03.01.02 Recommendations

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: Yes.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 03.01.02.01 | Recommend products | Yes | Phase 1 / MVP | P0 | No | Customer | `/product-discovery/recommend-products` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 03.01.02.02 | Show featured products | Yes | Phase 1 / MVP | P0 | No | Customer | `/product-discovery/show-featured-products` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 03.01.02.03 | Show popular products | Yes | Phase 1 / MVP | P0 | No | Customer | `/product-discovery/show-popular-products` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |

## 03.02 Product Evaluation

### Feature 03.02.01 Evaluation

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 03.02.01.01 | Start trial | Yes | Phase 1 / MVP | P0 | No | Customer | `/product-evaluation/start-trial` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 03.02.01.02 | Request demo | Yes | Phase 1 / MVP | P0 | No | Customer | `/product-evaluation/request-demo` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 03.02.01.03 | Launch sandbox | Yes | Phase 1 / MVP | P0 | No | Customer | `/product-evaluation/launch-sandbox` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 03.02.01.04 | View prerequisites | Yes | Phase 1 / MVP | P0 | No | Customer | `/product-evaluation/view-prerequisites` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |

### Feature 03.02.02 Comparison

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 03.02.02.01 | Compare products | Yes | Phase 1 / MVP | P0 | No | Customer | `/product-evaluation/compare-products` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 03.02.02.02 | Compare plans | Yes | Phase 1 / MVP | P0 | No | Customer | `/product-evaluation/compare-plans` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 03.02.02.03 | Estimate cost | Yes | Phase 1 / MVP | P0 | No | Customer | `/product-evaluation/estimate-cost` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |

## 03.03 Marketplace Checkout

### Feature 03.03.01 Checkout

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 03.03.01.01 | Select product | Yes | Phase 1 / MVP | P0 | No | Customer | `/marketplace-checkout/select-product` | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 03.03.01.02 | Select plan | Yes | Phase 1 / MVP | P0 | No | Customer | `/marketplace-checkout/select-plan` | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 03.03.01.03 | Configure options | Yes | Phase 1 / MVP | P0 | No | Customer | `/marketplace-checkout/configure-options` | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 03.03.01.04 | Apply discount | Yes | Phase 1 / MVP | P0 | No | Customer | `/marketplace-checkout/apply-discount` | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 03.03.01.05 | Accept terms | Yes | Phase 1 / MVP | P0 | No | Customer | `/marketplace-checkout/accept-terms` | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 03.03.01.06 | Submit order | Yes | Phase 1 / MVP | P0 | No | Customer | `/marketplace-checkout/submit-order` | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |

### Feature 03.03.02 Purchase Validation

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 03.03.02.01 | Validate eligibility | Yes | Phase 1 / MVP | P0 | No | Customer | `/marketplace-checkout/validate-eligibility` | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 03.03.02.02 | Validate payment | Yes | Phase 1 / MVP | P0 | No | Customer | `/marketplace-checkout/validate-payment` | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-003 Self-Service Purchase |
| 03.03.02.03 | Validate dependencies | Yes | Phase 1 / MVP | P0 | No | Customer | `/marketplace-checkout/validate-dependencies` | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |

## 03.04 Reviews & Ratings

### Feature 03.04.01 Customer Feedback

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 03.04.01.01 | Submit review | Yes | Phase 1 / MVP | P0 | No | Customer | `/reviews-ratings/submit-review` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 03.04.01.02 | Rate product | Yes | Phase 1 / MVP | P0 | No | Customer | `/reviews-ratings/rate-product` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 03.04.01.03 | Moderate review | Yes | Phase 1 / MVP | P0 | No | Customer | `/reviews-ratings/moderate-review` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 03.04.01.04 | View ratings | Yes | Phase 1 / MVP | P0 | No | Customer | `/reviews-ratings/view-ratings` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |

> **Columns from [WB:Traceability]** (Primary API, Microservice, Entity, Event, Journey) are kept for reference only. Under [C13](../open-decisions.md#c13) microservices are logical domains built as modules in the single backend. Under [C14](../open-decisions.md#c14) the implemented endpoints and each FRD's `api-requirements.md` are the source of truth for APIs.

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 02, 08, 09 | UJ-003 Self-Service Purchase: Marketplace → Checkout → Payment | [WB:User Journeys] |
| 09, 08 | EVT-005 CheckoutCompleted is consumed by Order and Billing | [WB:Events] |

## Deliverables

Not specified in any source. Under [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates) the sprint commits this application's P0 capabilities; P1 capabilities are stretch scope.

## Requirements, design and tests

| Artifact | Location | Status |
|---|---|---|
| Feature FRD | `docs/02-requirements/FRD/<feature>/` (copy `_template/`) | See the sprint page for FRDs in progress |
| Requirement | `REQ-MKT-<NNN>` inside the FRD | Approved FRD required before build ([DN-4](../open-decisions.md#dn-4-business-rules-and-acceptance-criteria)) |
| Business rules | `docs/03-business-rules/` and `FRD/<feature>/business-rules.md` | Per FRD |
| Test cases | `test-cases/functional/<feature>/TC-MKT-<NNN>.md` | Not created |
| NFRs | [WB:Non-Functional Requirements] NFR-001 to NFR-014 (platform-wide) | See [EIS-document-analysis.md](../EIS-document-analysis.md) section 2.17 |
