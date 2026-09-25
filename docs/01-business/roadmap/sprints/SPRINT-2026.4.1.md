# SPRINT-2026.4.1

| Field | Value |
|---|---|
| Sprint ID (PI.Sprint) | 2026.4.1 |
| PI – CY Quarter | 2026.4 |
| Start / end dates | 1–31 Oct 2026 ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Sprint goal | Not specified |
| Team / capacity | Not specified |
| Status | Not specified |
| Source | [PO] roadmap table ([`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)); decisions in [open-decisions.md](../open-decisions.md) |
| Previous / next sprint | [2026.3.3](SPRINT-2026.3.3.md) · [2026.4.2](SPRINT-2026.4.2.md) |

## Scope

| Application ID | Code | Application | Roadmap item | Source |
|---|---|---|---|---|
| 02 | `APP-CAT` | [Product & Catalog Management](../applications/02-product-catalog-management.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |

> **Commitment ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)):** this sprint commits the P0 (MVP) capabilities of its applications and treats P1 capabilities as stretch scope. Anything not finished is recorded as carry-over on the next sprint page.

## EIS 02 Product & Catalog Management

**Planned work ([PO] / [WB] description):** Products, solutions, services, plans and content

Full breakdown with APIs, services, entities and events: [applications/02-product-catalog-management.md](../applications/02-product-catalog-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [02.01 Product Management](../applications/02-product-catalog-management.md#0201-product-management) | 02.01.01 Product Lifecycle | Create product; Update product; Version product; Publish product; Retire product | P0 | Commit |
| [02.01 Product Management](../applications/02-product-catalog-management.md#0201-product-management) | 02.01.02 Product Structure | Define product hierarchy; Define variants; Define dependencies | P0 | Commit |
| [02.02 Offering Management](../applications/02-product-catalog-management.md#0202-offering-management) | 02.02.01 Offering Definition | Create offering; Bundle products; Define prerequisites; Define compatibility | P0 | Commit |
| [02.02 Offering Management](../applications/02-product-catalog-management.md#0202-offering-management) | 02.02.02 Availability | Define regions; Define channels; Define eligibility | P0 | Commit |
| [02.03 Plan Management](../applications/02-product-catalog-management.md#0203-plan-management) | 02.03.01 Plan Definition | Create plan; Define billing frequency; Define usage limits; Define included features | P0 | Commit |
| [02.03 Plan Management](../applications/02-product-catalog-management.md#0203-plan-management) | 02.03.02 Pricing Models | Define subscription price; Define usage price; Define tier price; Define overage charge | P0 | Commit |
| [02.04 Product Content](../applications/02-product-catalog-management.md#0204-product-content) | 02.04.01 Product Documentation | Upload datasheet; Publish documentation; Version content | P0 | Commit |
| [02.04 Product Content](../applications/02-product-catalog-management.md#0204-product-content) | 02.04.02 Rich Media | Upload images; Upload videos; Manage case studies | P0 | Commit |
| [02.05 Localization](../applications/02-product-catalog-management.md#0205-localization) | 02.05.01 Content Localization | Translate product content; Translate documentation; Publish localized content | P0 | Commit |
| [02.05 Localization](../applications/02-product-catalog-management.md#0205-localization) | 02.05.02 Regionalization | Configure currency; Configure date/time format; Configure regional terminology | P0 | Commit |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - EVT-003 ProductPublished and EVT-004 OfferingUpdated are consumed by Marketplace, Search and AI (applications 03, AI; [WB:Events])

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). Deliverables are otherwise Not specified in any source.

### Related code already in this repository

Observed on branch `dev`, module level only: `backend/…/modules/product`, `backend/…/modules/platform`; `frontend/src/pages/ProductsPage.tsx`, `frontend/src/pages/admin/AdminProductsPage.tsx`, `frontend/src/pages/admin/EditProductPage.tsx`, `frontend/src/pages/admin/PlatformsListPage.tsx`, `frontend/src/pages/admin/EditPlatformPage.tsx`.

## Decisions affecting this sprint

- [C3](../open-decisions.md#c3) The [PO] sprint order is authoritative; the MVP is complete at the end of sprint 2027.1.3.
- [C4](../open-decisions.md#c4), [C5](../open-decisions.md#c5), [C6](../open-decisions.md#c6) MVP, priority and phase as shown above.
- Sprint goal, team, capacity and status are Not specified.

Details: [open-decisions.md](../open-decisions.md).

## Traceability

```
[PO] roadmap row → SPRINT-2026.4.1 → application page → capability → feature → function (requirement candidate)
   → FRD docs/02-requirements/FRD/<feature>/ → REQ-<CODE>-<NNN> (Approved before build)
   → STORY-<CODE>-<NNN> with "Sprint (PI.Sprint)" = 2026.4.1
   → code (backend/ · frontend/ · ai-service/) → TC-<CODE>-<NNN> in test-cases/ → UAT
```

Stories for this sprint use `docs/02-requirements/functional-requirements/user-story-template.md` or the GitHub **User Story** issue template, with **Sprint (PI.Sprint) = 2026.4.1**. The Definition of Done is the one in the story template.

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)
- **Decisions:** [`open-decisions.md`](../open-decisions.md) (2026-09-25)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source or decision gives are written **Not specified**.
