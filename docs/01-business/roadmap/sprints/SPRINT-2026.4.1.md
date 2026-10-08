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
| 05 | `APP-TEN` | [Customer / Tenant Management](../applications/05-customer-tenant-management.md) | (whole application, except 05.01 Organization Management — built early in 2026.3.3, [C25](../open-decisions.md#c25)) | [C31](../open-decisions.md#c31): pulled forward from 2026.4.2 |

> **Commitment ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)):** this sprint commits the P0 (MVP) capabilities of its applications and treats P1 capabilities as stretch scope. Anything not finished is recorded as carry-over on the next sprint page.

## Changes and decisions in this sprint

| Change | Decision and scope | FRD | Requirement |
|---|---|---|---|
| Sequence correction | [C31](../open-decisions.md#c31) Adopts the corrected sprint sequence: 05 Tenant pulled into this sprint (from 2026.4.2) | - | - |
| Decided | [C32](../open-decisions.md#c32) Product Lifecycle & Structure: version counter, Publish/Retire actions and the new RETIRED status, hierarchy/variant/dependency fields | [product-lifecycle](../../../02-requirements/FRD/product-lifecycle/requirement.md) | REQ-CAT-001 |
| Decided | [C33](../open-decisions.md#c33) Plan Management: currency, usage limit, included features, usage price, overage charge, tier-pricing text — data fields only, no billing engine | [plan-management](../../../02-requirements/FRD/plan-management/requirement.md) | REQ-CAT-002 |
| Decided | [C84](../open-decisions.md#c84) Invite user (05.03.01.01) by email, with `INVITE_USERS` delegation; Create user stays carried | [invite-user](../../../02-requirements/FRD/invite-user/requirement.md) | REQ-TEN-008 |
| Decided | [C34](../open-decisions.md#c34) Member Lifecycle: Suspend/Reactivate/Remove and Review access; Invite/Create user carried to 2026.4.2 | [member-lifecycle](../../../02-requirements/FRD/member-lifecycle/requirement.md) | REQ-TEN-002 |
| Decided | [C66](../open-decisions.md#c66) UI/UX redesign with the Product Catalog as reference; catalog showcase fields (platform colour, catalog visibility and order; app accent colour, feature tags, documentation/support links); public catalog API | [catalog-showcase](../../../02-requirements/FRD/catalog-showcase/requirement.md) | REQ-CAT-003 |
| [product-content](../../../02-requirements/FRD/product-content/requirement.md) | REQ-CAT-004 | 02.04.01.01–.03, 02.04.02.01–.03 | Approved (2026-10-07, C81) |
| Decided | [C67](../open-decisions.md#c67) Preferences page redesign: four sections, personal accent colour, date/time/week formats, notification email categories on the existing API; renewal reminders unchanged | - (screen spec [preferences](../../../05-ui/screen-requirements/preferences.md)) | - |
| Decided | [C35](../open-decisions.md#c35) Groups: create/add/remove member; Projects (05.04.02) carried to 2026.4.2 | [group-management](../../../02-requirements/FRD/group-management/requirement.md) | REQ-TEN-003 |
| Decided | [C72](../open-decisions.md#c72) (2026-10-05) D23 answered: file storage is a private AWS S3 bucket with presigned URLs, for knowledge media (built 2026-10-05, C78). 02.04 Product Content (still not started) is **not** moved by this decision — whether product images and datasheets also use S3 is Not specified. This sprint's dates are unchanged | - ([aws-s3](../../../09-integrations/aws-s3.md)) | - |
| Decided | [C81](../open-decisions.md#c81) (2026-10-07) 02.04 Product Content: datasheets, documentation (knowledge articles), images, videos (links) and case studies configured on the application's Content tab; PC-1 to PC-6 answered with the recommended defaults; S3 shared with knowledge media | [product-content](../../../02-requirements/FRD/product-content/requirement.md) | REQ-CAT-004 |

### FRDs in this sprint

| FRD | Requirement | Functions | Status |
|---|---|---|---|
| [product-lifecycle](../../../02-requirements/FRD/product-lifecycle/requirement.md) | REQ-CAT-001 | 02.01.01.03–.05, 02.01.02 | Approved |
| [plan-management](../../../02-requirements/FRD/plan-management/requirement.md) | REQ-CAT-002 | 02.03.01.03–.04, 02.03.02.02–.04 | Approved |
| [member-lifecycle](../../../02-requirements/FRD/member-lifecycle/requirement.md) | REQ-TEN-002 | 05.03.01.03–.05, 05.03.02.03 | Approved |
| [invite-user](../../../02-requirements/FRD/invite-user/requirement.md) | REQ-TEN-008 | 05.03.01.01 | Approved |
| [group-management](../../../02-requirements/FRD/group-management/requirement.md) | REQ-TEN-003 | 05.04.01 | Approved |
| [catalog-showcase](../../../02-requirements/FRD/catalog-showcase/requirement.md) | REQ-CAT-003 | No function ID (restyles 02.01/02.03 screens; links touch 02.04.01.02) | Approved |

### Progress (as of 2026-09-26, first week of the sprint)

| Feature | Status | Note |
|---|---|---|
| 02.01.01 Product Lifecycle | Done (this FRD's scope) | Version, Publish, Retire built; Create/Update product were already built |
| 02.01.02 Product Structure | Done | Hierarchy, variants, dependencies built |
| 02.02 Offering Management | Partly done (2026-10-08) | FRD [REQ-CAT-005](../../../02-requirements/FRD/offering-management/requirement.md) Approved ([C85](../open-decisions.md#c85)). Built: Create offering, Bundle products (grouping and public browsing, **no offering price** — OF-2 open), Define prerequisites (the product dependencies, OF-6), Define compatibility ("works with", OF-7), Define eligibility (individuals / organizations, enforced in cart, subscribe and order), Define channels (platform only, OF-4). **Not built:** Define regions (OF-3 and OF-5 conflict); buying an offering as a whole (needs bundle pricing) |
| 02.03 Plan Management | Done (this FRD's scope) | Currency, usage limit, included features, usage price, overage charge, tier-pricing text built; Create plan/billing frequency/subscription price were already built |
| 02.04 Product Content | Done (2026-10-07) | FRD [REQ-CAT-004](../../../02-requirements/FRD/product-content/requirement.md) Approved with default answers ([C81](../open-decisions.md#c81)); Upload datasheet, Publish documentation, Version content, Upload images, Upload videos (links) and Manage case studies built as the admin Content tab and the public Resources tab. Needs a test bucket for the real-S3 check (TC-CAT-032) |
| 02.05 Localization | Not started | Needs its own FRD |
| 05.02 Tenant Lifecycle | Not started | Needs its own FRD (what "tenant" means beyond Organization is undecided) |
| 05.03.01 User Lifecycle | Partly done | Suspend/Reactivate/Remove built; **Invite user built 2026-10-08** ([C84](../open-decisions.md#c84), [REQ-TEN-008](../../../02-requirements/FRD/invite-user/requirement.md)); Create user (05.03.01.02) stays carried |
| 05.03.02 Role Assignment | Done | Assign role and Assign group were already built/built this sprint; Review access built this sprint |
| 05.04.01 Groups | Done | Create/add/remove member built |
| 05.04.02 Projects | Not started | Carried to 2026.4.2 (C35) |

### Catalog showcase and UI redesign (C66, 2026-10-03)

| Item | Status | Note |
|---|---|---|
| Design system: theme, shell, page header, shared components | Done | [design-system.md](../../../05-ui/screen-requirements/design-system.md) |
| Product Catalog, platform details, app details, app card | Done | Real counts only; no versions or ratings (no data) |
| Create/Edit Platform with live preview; Platforms list | Done | Colour, status, show in catalog, display order |
| Create/Edit App with live preview; All Apps table and filters | Done | Every existing field kept; feature tags, links, colour added; delete/retire confirm |
| Public catalog API, schema, migration V019 | Done | [API](../../../06-api/api-requirements/catalog-showcase.md), [data model](../../../07-database/data-model/catalog-showcase.md) |
| Tests | Done | [TESTPLAN-CAT-003](../../../../test-cases/functional/catalog-showcase/TESTPLAN-CAT-003.md), TC-CAT-011–018 |
| Other screens (dashboard, registrations, privileged access, roles, permissions, audit, status, KB, support, reviews, partners, billing, settings) | Theme only | Carried: restyle one area at a time ([C66](../open-decisions.md#c66) follow-up) |

### Preferences redesign (C67, C68, 2026-10-03)

| Item | Status | Note |
|---|---|---|
| Single-column Preferences page (Appearance, Language & Formats, Notifications, Renewal Reminders) | Done | [preferences.md](../../../05-ui/screen-requirements/preferences.md) |
| Personal accent colour (10 presets) applied to the theme primary | Done | Stored in the browser; AA contrast checked |
| Date format, time format | Done | Region default unless chosen. *First day of week* was added and then removed (product owner, 2026-10-03) |
| Notification email categories on the existing opt-out API | Done | In-app shown as always on (backend always records) |
| Settings navigation layout (C68): left navigation, focused panel, section selector on phones | Done | Supersedes the single-column layout of C67 |
| Tests | Done | [TESTPLAN-PRT-002](../../../../test-cases/functional/portal-preferences/TESTPLAN-PRT-002.md), TC-PRT-008–013 |

### Business dashboard redesign (C69, 2026-10-03)

| Item | Status | Note |
|---|---|---|
| Workspace layout: header, welcome and status, quick actions, KPIs, analytics, health, workspace, account, applications, activity, attention, billing | Done | [business-dashboard.md](../../../05-ui/screen-requirements/business-dashboard.md) |
| Organization settings page (members, groups, MFA policy, privileged access moved off the dashboard) | Done | `/organization/settings` |
| Motion with Reduce motion / OS preference respected | Done | |
| Launches over time and period filters | Not built | No launch history in the data — C69 follow-up |
| Tests | Done | [TESTPLAN-PRT-003](../../../../test-cases/functional/business-dashboard/TESTPLAN-PRT-003.md), TC-PRT-014–019 |

The rest of 02 (Offering, Content, Localization) and 05.02 Tenant Lifecycle remain open for this sprint; continuing them needs the same FRD-first process as above.

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

Observed on branch `dev`, module level only: `backend/…/modules/product` (now with version/hierarchy/dependency fields and Publish/Retire, [product-lifecycle](../../../02-requirements/FRD/product-lifecycle/requirement.md); plan pricing fields, [plan-management](../../../02-requirements/FRD/plan-management/requirement.md)), `backend/…/modules/platform`; `frontend/src/pages/ProductsPage.tsx`, `frontend/src/pages/admin/AdminProductsPage.tsx`, `frontend/src/pages/admin/EditProductPage.tsx` (now with version/status/publish/retire), `frontend/src/pages/admin/PlatformsListPage.tsx`, `frontend/src/pages/admin/EditPlatformPage.tsx`, `frontend/src/components/admin/ProductForm.tsx` (now with structure and pricing fields).

## EIS 05 Customer / Tenant Management

**Planned work ([PO] / [WB] description):** Organizations, tenants, users and projects — this sprint covers everything except 05.01 Organization Management (built early in 2026.3.3, [C25](../open-decisions.md#c25)).

Full breakdown: [applications/05-customer-tenant-management.md](../applications/05-customer-tenant-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [05.02 Tenant Management](../applications/05-customer-tenant-management.md#0502-tenant-management) | 05.02.01 Tenant Lifecycle | Create tenant; Configure tenant; Assign region; Configure isolation; Configure tenant policies | P0 | Commit (not started — needs its own FRD) |
| [05.03 User Management](../applications/05-customer-tenant-management.md#0503-user-management) | 05.03.01 User Lifecycle | Invite user; Create user; Activate user; Suspend user; Remove user | P0 | Partly (Activate/Suspend/Remove and Invite user built; Create user carried, [C34](../open-decisions.md#c34), [C84](../open-decisions.md#c84)) |
| [05.03 User Management](../applications/05-customer-tenant-management.md#0503-user-management) | 05.03.02 Role Assignment | Assign role; Assign group; Review access | P0 | Commit (built) |
| [05.04 Group & Project Management](../applications/05-customer-tenant-management.md#0504-group--project-management) | 05.04.01 Groups | Create group; Add member; Remove member | P0 | Commit (built) |
| [05.04 Group & Project Management](../applications/05-customer-tenant-management.md#0504-group--project-management) | 05.04.02 Projects | Create project; Assign users; Assign resources | P0 | Carried to 2026.4.2 ([C35](../open-decisions.md#c35)) |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - UJ-004 Tenant Onboarding (Identity, Tenant, IAM) — application 06
  - EVT-002 TenantCreated consumed by Provisioning and Audit — applications 09, 15

### Expected deliverables

- The P0 capabilities above, except where noted as carried ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)).

### Related code already in this repository

`backend/…/modules/registration` (organization, members — now with `SUSPENDED` status, review-access columns, and the new group/group-member entities and endpoints); `frontend/src/components/organization/OrganizationMembersCard.tsx` (now with suspend/reactivate/remove/review actions), `frontend/src/components/organization/OrganizationGroupsCard.tsx` (new).

## Decisions affecting this sprint

- [C3](../open-decisions.md#c3) The [PO] sprint order is authoritative; the MVP is complete at the end of sprint 2027.1.3.
- [C4](../open-decisions.md#c4), [C5](../open-decisions.md#c5), [C6](../open-decisions.md#c6) MVP, priority and phase as shown above.
- [C31](../open-decisions.md#c31) Corrected sprint sequence: 05 Tenant pulled into this sprint.
- [C32](../open-decisions.md#c32), [C33](../open-decisions.md#c33), [C34](../open-decisions.md#c34), [C35](../open-decisions.md#c35), [C66](../open-decisions.md#c66), [C67](../open-decisions.md#c67): see the FRDs and sections above.
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
