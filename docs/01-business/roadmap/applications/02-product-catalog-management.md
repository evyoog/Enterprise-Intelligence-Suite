# 02 Product & Catalog Management

| Field | Value |
|---|---|
| Application ID | 02 ([WB] numbering; an `APP-<CODE>` code is not assigned in any source) |
| Application | Product & Catalog Management |
| Description | Products, solutions, services, plans and content ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2026.4 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2026.4.1](../sprints/SPRINT-2026.4.1.md) |
| Capabilities / features / functions | 5 / 10 / 35 ([WB]) |
| Application status | Not specified |

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.

## Capabilities

| Capability ID | Capability | Features | Priority | MVP | AI relevant |
|---|---|---|---|---|---|
| [02.01](#0201-product-management) | Product Management | 02.01.01 Product Lifecycle, 02.01.02 Product Structure | P0 | Yes | No |
| [02.02](#0202-offering-management) | Offering Management | 02.02.01 Offering Definition, 02.02.02 Availability | P0 | Yes | No |
| [02.03](#0203-plan-management) | Plan Management | 02.03.01 Plan Definition, 02.03.02 Pricing Models | P0 | Yes | No |
| [02.04](#0204-product-content) | Product Content | 02.04.01 Product Documentation, 02.04.02 Rich Media | P0 | Yes | No |
| [02.05](#0205-localization) | Localization | 02.05.01 Content Localization, 02.05.02 Regionalization | P0 | Yes | No |

> The Priority and MVP values are copied from [WB:Capabilities]. Every capability in [WB] is P0 / MVP=Yes, which conflicts with the function-level MVP flags (C4 in [open-decisions.md](../open-decisions.md)).

## 02.01 Product Management

### Feature 02.01.01 Product Lifecycle

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 02.01.01.01 | Create product | Yes | No | Platform Service | `/product-management/create-product` | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 1 / MVP |
| 02.01.01.02 | Update product | Yes | No | Platform Service | `/product-management/update-product` | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 1 / MVP |
| 02.01.01.03 | Version product | Yes | No | Platform Service | `/product-management/version-product` | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 1 / MVP |
| 02.01.01.04 | Publish product | No | No | Platform Service | `/product-management/publish-product` | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.01.01.05 | Retire product | No | No | Platform Service | `/product-management/retire-product` | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |

### Feature 02.01.02 Product Structure

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 02.01.02.01 | Define product hierarchy | Yes | No | Platform Service | `/product-management/define-product-hierarchy` | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 1 / MVP |
| 02.01.02.02 | Define variants | Yes | No | Platform Service | `/product-management/define-variants` | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 1 / MVP |
| 02.01.02.03 | Define dependencies | Yes | No | Platform Service | `/product-management/define-dependencies` | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 1 / MVP |

## 02.02 Offering Management

### Feature 02.02.01 Offering Definition

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 02.02.01.01 | Create offering | No | No | Platform Service | `/offering-management/create-offering` | API-005 GET /v1/offerings/{id} | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.02.01.02 | Bundle products | No | No | Platform Service | `/offering-management/bundle-products` | API-005 GET /v1/offerings/{id} | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.02.01.03 | Define prerequisites | No | No | Platform Service | `/offering-management/define-prerequisites` | API-005 GET /v1/offerings/{id} | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.02.01.04 | Define compatibility | No | No | Platform Service | `/offering-management/define-compatibility` | API-005 GET /v1/offerings/{id} | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |

### Feature 02.02.02 Availability

Priority P1 · MVP Yes · AI required Yes ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 02.02.02.01 | Define regions | No | No | Platform Service | `/offering-management/define-regions` | API-005 GET /v1/offerings/{id} | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.02.02.02 | Define channels | No | No | Platform Service | `/offering-management/define-channels` | API-005 GET /v1/offerings/{id} | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.02.02.03 | Define eligibility | No | No | Platform Service | `/offering-management/define-eligibility` | API-005 GET /v1/offerings/{id} | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |

## 02.03 Plan Management

### Feature 02.03.01 Plan Definition

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 02.03.01.01 | Create plan | No | No | Platform Service | `/plan-management/create-plan` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.03.01.02 | Define billing frequency | No | No | Platform Service | `/plan-management/define-billing-frequency` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.03.01.03 | Define usage limits | No | No | Platform Service | `/plan-management/define-usage-limits` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.03.01.04 | Define included features | No | No | Platform Service | `/plan-management/define-included-features` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |

### Feature 02.03.02 Pricing Models

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 02.03.02.01 | Define subscription price | No | No | Platform Service | `/plan-management/define-subscription-price` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.03.02.02 | Define usage price | No | No | Platform Service | `/plan-management/define-usage-price` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.03.02.03 | Define tier price | No | No | Platform Service | `/plan-management/define-tier-price` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.03.02.04 | Define overage charge | No | No | Platform Service | `/plan-management/define-overage-charge` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |

## 02.04 Product Content

### Feature 02.04.01 Product Documentation

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 02.04.01.01 | Upload datasheet | No | No | Platform Service | `/product-content/upload-datasheet` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.04.01.02 | Publish documentation | No | No | Platform Service | `/product-content/publish-documentation` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.04.01.03 | Version content | No | No | Platform Service | `/product-content/version-content` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |

### Feature 02.04.02 Rich Media

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 02.04.02.01 | Upload images | No | No | Platform Service | `/product-content/upload-images` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.04.02.02 | Upload videos | No | No | Platform Service | `/product-content/upload-videos` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.04.02.03 | Manage case studies | No | No | Platform Service | `/product-content/manage-case-studies` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |

## 02.05 Localization

### Feature 02.05.01 Content Localization

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 02.05.01.01 | Translate product content | No | No | Platform Service | `/localization/translate-product-content` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.05.01.02 | Translate documentation | No | No | Platform Service | `/localization/translate-documentation` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.05.01.03 | Publish localized content | No | No | Platform Service | `/localization/publish-localized-content` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |

### Feature 02.05.02 Regionalization

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 02.05.02.01 | Configure currency | No | No | Platform Service | `/localization/configure-currency` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.05.02.02 | Configure date/time format | No | No | Platform Service | `/localization/configure-date/time-format` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.05.02.03 | Configure regional terminology | No | No | Platform Service | `/localization/configure-regional-terminology` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 03, AI | EVT-003 ProductPublished and EVT-004 OfferingUpdated are consumed by Marketplace, Search and AI | [WB:Events] |

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

## Related code already in this repository

Observed on branch `dev`. This is a module-level mapping, not a verified function-by-function implementation status.

- Backend: `backend/src/main/java/com/vyoog/eisplatform/modules/product`, `backend/src/main/java/com/vyoog/eisplatform/modules/platform`
- Frontend: `frontend/src/pages/ProductsPage.tsx`, `frontend/src/pages/admin/AdminProductsPage.tsx`, `frontend/src/pages/admin/EditProductPage.tsx`, `frontend/src/pages/admin/PlatformsListPage.tsx`, `frontend/src/pages/admin/EditPlatformPage.tsx`
