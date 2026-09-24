# 03 Marketplace

| Field | Value |
|---|---|
| Application ID | 03 ([WB] numbering; an `APP-<CODE>` code is not assigned in any source) |
| Application | Marketplace |
| Description | Discovery, evaluation, comparison and checkout ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2027.1 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2027.1.3](../sprints/SPRINT-2027.1.3.md) |
| Capabilities / features / functions | 4 / 7 / 27 ([WB]) |
| Application status | Not specified |

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.

## Capabilities

| Capability ID | Capability | Features | Priority | MVP | AI relevant |
|---|---|---|---|---|---|
| [03.01](#0301-product-discovery) | Product Discovery | 03.01.01 Catalog Browsing, 03.01.02 Recommendations | P0 | Yes | No |
| [03.02](#0302-product-evaluation) | Product Evaluation | 03.02.01 Evaluation, 03.02.02 Comparison | P0 | Yes | No |
| [03.03](#0303-marketplace-checkout) | Marketplace Checkout | 03.03.01 Checkout, 03.03.02 Purchase Validation | P0 | Yes | No |
| [03.04](#0304-reviews--ratings) | Reviews & Ratings | 03.04.01 Customer Feedback | P0 | Yes | No |

> The Priority and MVP values are copied from [WB:Capabilities]. Every capability in [WB] is P0 / MVP=Yes, which conflicts with the function-level MVP flags (C4 in [open-decisions.md](../open-decisions.md)).

## 03.01 Product Discovery

### Feature 03.01.01 Catalog Browsing

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 03.01.01.01 | Browse categories | No | No | Customer | `/product-discovery/browse-categories` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.01.01.02 | Search products | No | No | Customer | `/product-discovery/search-products` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.01.01.03 | Filter products | No | No | Customer | `/product-discovery/filter-products` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.01.01.04 | Sort products | No | No | Customer | `/product-discovery/sort-products` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |

### Feature 03.01.02 Recommendations

Priority P1 · MVP Yes · AI required Yes ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 03.01.02.01 | Recommend products | No | No | Customer | `/product-discovery/recommend-products` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.01.02.02 | Show featured products | No | No | Customer | `/product-discovery/show-featured-products` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.01.02.03 | Show popular products | No | No | Customer | `/product-discovery/show-popular-products` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |

## 03.02 Product Evaluation

### Feature 03.02.01 Evaluation

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 03.02.01.01 | Start trial | No | No | Customer | `/product-evaluation/start-trial` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.02.01.02 | Request demo | No | No | Customer | `/product-evaluation/request-demo` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.02.01.03 | Launch sandbox | No | No | Customer | `/product-evaluation/launch-sandbox` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.02.01.04 | View prerequisites | No | No | Customer | `/product-evaluation/view-prerequisites` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |

### Feature 03.02.02 Comparison

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 03.02.02.01 | Compare products | No | No | Customer | `/product-evaluation/compare-products` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.02.02.02 | Compare plans | No | No | Customer | `/product-evaluation/compare-plans` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.02.02.03 | Estimate cost | No | No | Customer | `/product-evaluation/estimate-cost` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |

## 03.03 Marketplace Checkout

### Feature 03.03.01 Checkout

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 03.03.01.01 | Select product | No | No | Customer | `/marketplace-checkout/select-product` | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.03.01.02 | Select plan | No | No | Customer | `/marketplace-checkout/select-plan` | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.03.01.03 | Configure options | No | No | Customer | `/marketplace-checkout/configure-options` | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.03.01.04 | Apply discount | No | No | Customer | `/marketplace-checkout/apply-discount` | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.03.01.05 | Accept terms | No | No | Customer | `/marketplace-checkout/accept-terms` | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.03.01.06 | Submit order | No | No | Customer | `/marketplace-checkout/submit-order` | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |

### Feature 03.03.02 Purchase Validation

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 03.03.02.01 | Validate eligibility | No | No | Customer | `/marketplace-checkout/validate-eligibility` | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.03.02.02 | Validate payment | No | No | Customer | `/marketplace-checkout/validate-payment` | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-003 Self-Service Purchase | Phase 2 |
| 03.03.02.03 | Validate dependencies | No | No | Customer | `/marketplace-checkout/validate-dependencies` | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |

## 03.04 Reviews & Ratings

### Feature 03.04.01 Customer Feedback

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 03.04.01.01 | Submit review | No | No | Customer | `/reviews-ratings/submit-review` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.04.01.02 | Rate product | No | No | Customer | `/reviews-ratings/rate-product` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.04.01.03 | Moderate review | No | No | Customer | `/reviews-ratings/moderate-review` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.04.01.04 | View ratings | No | No | Customer | `/reviews-ratings/view-ratings` | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 02, 08, 09 | UJ-003 Self-Service Purchase: Marketplace → Checkout → Payment | [WB:User Journeys] |
| 09, 08 | EVT-005 CheckoutCompleted is consumed by Order and Billing | [WB:Events] |

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
