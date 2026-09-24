# SPRINT-2026.4.1

| Field | Value |
|---|---|
| Sprint ID (PI.Sprint) | 2026.4.1 |
| PI – CY Quarter | 2026.4 |
| Start / end dates | Not specified |
| Sprint goal | Not specified |
| Team / capacity | Not specified |
| Status | Not specified |
| Source | [PO] roadmap tables ([`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)) |
| Previous / next sprint | [2026.3.3](SPRINT-2026.3.3.md) · [2026.4.2](SPRINT-2026.4.2.md) |

## Scope

| Product | Application ID | Application | Roadmap item(s) | Source |
|---|---|---|---|---|
| EIS (PaaS) | 02 | [Product & Catalog Management](../applications/02-product-catalog-management.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |
| Thiran (SaaS) | Not specified | [SW Life Cycle](../applications/thiran-sw-life-cycle.md) | SWLC-CAP-11 Workflow & Approval Management | [PO] "SW Life Cycle - Roadmap Initiatives" |

> [PO] assigns **one sprint per EIS application**. It does not say which capabilities or features fall inside this sprint, or whether the application must be finished in it. The EIS scope below is the application's full [WB] breakdown until sprint scope is decided (see [open-decisions.md](../open-decisions.md)).

## EIS 02 Product & Catalog Management

**Planned work ([PO] / [WB] description):** Products, solutions, services, plans and content

Full breakdown with APIs, services, entities and events: [applications/02-product-catalog-management.md](../applications/02-product-catalog-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | MVP functions |
|---|---|---|---|
| [02.01 Product Management](../applications/02-product-catalog-management.md#0201-product-management) | 02.01.01 Product Lifecycle | Create product; Update product; Version product; Publish product; Retire product | 02.01.01.01, 02.01.01.02, 02.01.01.03 |
| [02.01 Product Management](../applications/02-product-catalog-management.md#0201-product-management) | 02.01.02 Product Structure | Define product hierarchy; Define variants; Define dependencies | 02.01.02.01, 02.01.02.02, 02.01.02.03 |
| [02.02 Offering Management](../applications/02-product-catalog-management.md#0202-offering-management) | 02.02.01 Offering Definition | Create offering; Bundle products; Define prerequisites; Define compatibility | - |
| [02.02 Offering Management](../applications/02-product-catalog-management.md#0202-offering-management) | 02.02.02 Availability | Define regions; Define channels; Define eligibility | - |
| [02.03 Plan Management](../applications/02-product-catalog-management.md#0203-plan-management) | 02.03.01 Plan Definition | Create plan; Define billing frequency; Define usage limits; Define included features | - |
| [02.03 Plan Management](../applications/02-product-catalog-management.md#0203-plan-management) | 02.03.02 Pricing Models | Define subscription price; Define usage price; Define tier price; Define overage charge | - |
| [02.04 Product Content](../applications/02-product-catalog-management.md#0204-product-content) | 02.04.01 Product Documentation | Upload datasheet; Publish documentation; Version content | - |
| [02.04 Product Content](../applications/02-product-catalog-management.md#0204-product-content) | 02.04.02 Rich Media | Upload images; Upload videos; Manage case studies | - |
| [02.05 Localization](../applications/02-product-catalog-management.md#0205-localization) | 02.05.01 Content Localization | Translate product content; Translate documentation; Publish localized content | - |
| [02.05 Localization](../applications/02-product-catalog-management.md#0205-localization) | 02.05.02 Regionalization | Configure currency; Configure date/time format; Configure regional terminology | - |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - EVT-003 ProductPublished and EVT-004 OfferingUpdated are consumed by Marketplace, Search and AI (applications 03, AI; [WB:Events])

### Expected deliverables

- Not specified in any source.
- Implied by [WB:Traceability]: APIs `API-004 GET /v1/products`, `API-005 GET /v1/offerings/{id}`; services Catalog Service.

### Related code already in this repository

Observed on branch `dev`, module level only: `backend/…/modules/product`, `backend/…/modules/platform`; `frontend/src/pages/ProductsPage.tsx`, `frontend/src/pages/admin/AdminProductsPage.tsx`, `frontend/src/pages/admin/EditProductPage.tsx`, `frontend/src/pages/admin/PlatformsListPage.tsx`, `frontend/src/pages/admin/EditPlatformPage.tsx`.

## Hosted SaaS products in this sprint

These are tracked here so their dependencies on EIS are visible. [PO] lists no functions, requirements or deliverables for them.

| Product | Application | ID | Capability | Features ([PO]) | PI stated |
|---|---|---|---|---|---|
| Thiran (SaaS) | [SW Life Cycle](../applications/thiran-sw-life-cycle.md) | SWLC-CAP-11 | Workflow & Approval Management | Not specified | 2026.4 |

**Stated dependencies ([PO]):**
- SW Life Cycle integrates with Macro Planner and Agile Planner.

## Open issues affecting this sprint

- None specific to this sprint. The general decisions in open-decisions.md still apply
- The sprint scope, deliverables, acceptance criteria and dates are not specified in any source.

Details: [open-decisions.md](../open-decisions.md).

## Traceability

```
[PO] roadmap row → SPRINT-2026.4.1 → application page → capability → feature → function (requirement candidate)
   → FRD docs/02-requirements/FRD/<feature>/ (not created) → REQ-<APP-CODE>-<NNN> (not created)
   → STORY-<APP-CODE>-<NNN> with "Sprint (PI.Sprint)" = 2026.4.1
   → code (backend/ · frontend/ · ai-service/) → TC-<APP-CODE>-<NNN> in test-cases/ → UAT
```

Stories for this sprint use `docs/02-requirements/functional-requirements/user-story-template.md` or the GitHub **User Story** issue template, with **Sprint (PI.Sprint) = 2026.4.1**. The Definition of Done is the one in the story template.

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.
