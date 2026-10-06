# 02 Product & Catalog Management

| Field | Value |
|---|---|
| Application ID | 02 ([WB] numbering) |
| Application code | `APP-CAT` ([DN-5](../open-decisions.md#dn-5-application-codes)); IDs use `CAT`, for example `REQ-CAT-001` |
| Application | Product & Catalog Management |
| Description | Products, solutions, services, plans and content ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2026.4 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2026.4.1](../sprints/SPRINT-2026.4.1.md) (1–31 Oct 2026, [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Capabilities / features / functions | 5 / 10 / 35 ([WB]) |
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
| [02.01](#0201-product-management) | Product Management | 02.01.01 Product Lifecycle, 02.01.02 Product Structure | Yes | Phase 1 / MVP | P0 | C4 |
| [02.02](#0202-offering-management) | Offering Management | 02.02.01 Offering Definition, 02.02.02 Availability | Yes | Phase 1 / MVP | P0 | C4 |
| [02.03](#0203-plan-management) | Plan Management | 02.03.01 Plan Definition, 02.03.02 Pricing Models | Yes | Phase 1 / MVP | P0 | C4 |
| [02.04](#0204-product-content) | Product Content | 02.04.01 Product Documentation, 02.04.02 Rich Media | Yes | Phase 1 / MVP | P0 | C4 |
| [02.05](#0205-localization) | Localization | 02.05.01 Content Localization, 02.05.02 Regionalization | Yes | Phase 1 / MVP | P0 | C4 |

## 02.01 Product Management

### Feature 02.01.01 Product Lifecycle

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 02.01.01.01 | Create product | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/product-management/create-product` | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.01.01.02 | Update product | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/product-management/update-product` | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.01.01.03 | Version product | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/product-management/version-product` | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.01.01.04 | Publish product | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/product-management/publish-product` | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.01.01.05 | Retire product | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/product-management/retire-product` | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |

### Feature 02.01.02 Product Structure

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 02.01.02.01 | Define product hierarchy | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/product-management/define-product-hierarchy` | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.01.02.02 | Define variants | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/product-management/define-variants` | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.01.02.03 | Define dependencies | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/product-management/define-dependencies` | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |

## 02.02 Offering Management

### Feature 02.02.01 Offering Definition

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 02.02.01.01 | Create offering | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/offering-management/create-offering` | API-005 GET /v1/offerings/{id} | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.02.01.02 | Bundle products | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/offering-management/bundle-products` | API-005 GET /v1/offerings/{id} | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.02.01.03 | Define prerequisites | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/offering-management/define-prerequisites` | API-005 GET /v1/offerings/{id} | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.02.01.04 | Define compatibility | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/offering-management/define-compatibility` | API-005 GET /v1/offerings/{id} | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |

### Feature 02.02.02 Availability

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: Yes.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 02.02.02.01 | Define regions | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/offering-management/define-regions` | API-005 GET /v1/offerings/{id} | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.02.02.02 | Define channels | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/offering-management/define-channels` | API-005 GET /v1/offerings/{id} | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.02.02.03 | Define eligibility | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/offering-management/define-eligibility` | API-005 GET /v1/offerings/{id} | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |

## 02.03 Plan Management

### Feature 02.03.01 Plan Definition

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 02.03.01.01 | Create plan | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/plan-management/create-plan` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.03.01.02 | Define billing frequency | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/plan-management/define-billing-frequency` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.03.01.03 | Define usage limits | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/plan-management/define-usage-limits` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.03.01.04 | Define included features | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/plan-management/define-included-features` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |

### Feature 02.03.02 Pricing Models

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 02.03.02.01 | Define subscription price | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/plan-management/define-subscription-price` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.03.02.02 | Define usage price | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/plan-management/define-usage-price` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.03.02.03 | Define tier price | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/plan-management/define-tier-price` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.03.02.04 | Define overage charge | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/plan-management/define-overage-charge` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |

## 02.04 Product Content

[C72](../open-decisions.md#c72) (2026-10-05): D23 answered — private AWS S3 with presigned URLs for knowledge media (built 2026-10-05). Whether 02.04 Product Content also uses it is Not specified.

### Feature 02.04.01 Product Documentation

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 02.04.01.01 | Upload datasheet | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/product-content/upload-datasheet` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.04.01.02 | Publish documentation | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/product-content/publish-documentation` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.04.01.03 | Version content | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/product-content/version-content` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |

### Feature 02.04.02 Rich Media

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 02.04.02.01 | Upload images | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/product-content/upload-images` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.04.02.02 | Upload videos | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/product-content/upload-videos` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.04.02.03 | Manage case studies | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/product-content/manage-case-studies` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |

## 02.05 Localization

### Feature 02.05.01 Content Localization

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 02.05.01.01 | Translate product content | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/localization/translate-product-content` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.05.01.02 | Translate documentation | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/localization/translate-documentation` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.05.01.03 | Publish localized content | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/localization/publish-localized-content` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |

### Feature 02.05.02 Regionalization

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 02.05.02.01 | Configure currency | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/localization/configure-currency` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.05.02.02 | Configure date/time format | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/localization/configure-date/time-format` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |
| 02.05.02.03 | Configure regional terminology | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/localization/configure-regional-terminology` | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service |

> **Columns from [WB:Traceability]** (Primary API, Microservice, Entity, Event, Journey) are kept for reference only. Under [C13](../open-decisions.md#c13) microservices are logical domains built as modules in the single backend. Under [C14](../open-decisions.md#c14) the implemented endpoints and each FRD's `api-requirements.md` are the source of truth for APIs.

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 03, AI | EVT-003 ProductPublished and EVT-004 OfferingUpdated are consumed by Marketplace, Search and AI | [WB:Events] |

## Deliverables

Not specified in any source. Under [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates) the sprint commits this application's P0 capabilities; P1 capabilities are stretch scope.

## Requirements, design and tests

| Artifact | Location | Status |
|---|---|---|
| Feature FRD | `docs/02-requirements/FRD/<feature>/` (copy `_template/`) | See the sprint page for FRDs in progress |
| Requirement | `REQ-CAT-<NNN>` inside the FRD | Approved FRD required before build ([DN-4](../open-decisions.md#dn-4-business-rules-and-acceptance-criteria)) |
| Business rules | `docs/03-business-rules/` and `FRD/<feature>/business-rules.md` | Per FRD |
| Test cases | `test-cases/functional/<feature>/TC-CAT-<NNN>.md` | Not created |
| NFRs | [WB:Non-Functional Requirements] NFR-001 to NFR-014 (platform-wide) | See [EIS-document-analysis.md](../EIS-document-analysis.md) section 2.17 |

## Related code already in this repository

Observed on branch `dev`. This is a module-level mapping, not a verified function-by-function implementation status.

- Backend: `backend/src/main/java/com/vyoog/eisplatform/modules/product` (version/hierarchy/dependencies, plan pricing fields — [product-lifecycle](../../../02-requirements/FRD/product-lifecycle/requirement.md), [plan-management](../../../02-requirements/FRD/plan-management/requirement.md)), `backend/src/main/java/com/vyoog/eisplatform/modules/platform`
- Frontend: `frontend/src/pages/ProductsPage.tsx`, `frontend/src/pages/admin/AdminProductsPage.tsx`, `frontend/src/pages/admin/EditProductPage.tsx`, `frontend/src/components/admin/ProductForm.tsx`, `frontend/src/pages/admin/PlatformsListPage.tsx`, `frontend/src/pages/admin/EditPlatformPage.tsx`
