# 01 Enterprise Intelligence Suite

| Field | Value |
|---|---|
| Application ID | 01 ([WB] numbering) |
| Application code | `APP-PRT` ([DN-5](../open-decisions.md#dn-5-application-codes)); IDs use `PRT`, for example `REQ-PRT-001` |
| Application | Enterprise Intelligence Suite ([C10](../open-decisions.md#c10); the workbook name is "Experience & Customer Portal") |
| Description | Customer-facing web/mobile experience ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2026.3 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2026.3.3](../sprints/SPRINT-2026.3.3.md) (1–30 Sep 2026, [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Capabilities / features / functions | 4 / 7 / 26 ([WB]) |
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
| [01.01](#0101-portal-experience) | Portal Experience | 01.01.01 Responsive Web Portal, 01.01.02 Accessibility | Yes | Phase 1 / MVP | P0 | C4 |
| [01.02](#0102-customer-dashboard) | Customer Dashboard | 01.02.01 Business Overview, 01.02.02 Service Health | Yes | Phase 1 / MVP | P0 | C4 |
| [01.03](#0103-global-search) | Global Search | 01.03.01 Unified Search | Yes | Phase 1 / MVP | P0 | C4 |
| [01.04](#0104-notifications--communications) | Notifications & Communications | 01.04.01 Notification Center, 01.04.02 Outbound Communications | Yes | Phase 1 / MVP | P0 | C4 |

## 01.01 Portal Experience

### Feature 01.01.01 Responsive Web Portal

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 01.01.01.01 | Login | Yes | Phase 1 / MVP | P0 | No | Customer | `/portal-experience/login` | - | - | - | - | UJ-005 Provision Service |
| 01.01.01.02 | Navigate portal | Yes | Phase 1 / MVP | P0 | No | Customer | `/portal-experience/navigate-portal` | - | - | - | - | UJ-005 Provision Service |
| 01.01.01.03 | Select language | Yes | Phase 1 / MVP | P0 | No | Customer | `/portal-experience/select-language` | - | - | - | - | UJ-005 Provision Service |
| 01.01.01.04 | Select region | Yes | Phase 1 / MVP | P0 | No | Customer | `/portal-experience/select-region` | - | - | - | - | UJ-005 Provision Service |
| 01.01.01.05 | Customize preferences | Yes | Phase 1 / MVP | P0 | No | Customer | `/portal-experience/customize-preferences` | - | - | - | - | UJ-005 Provision Service |

### Feature 01.01.02 Accessibility

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 01.01.02.01 | Configure accessibility preferences | Yes | Phase 1 / MVP | P0 | No | Customer | `/portal-experience/configure-accessibility-preferences` | - | - | - | - | UJ-005 Provision Service |
| 01.01.02.02 | Use keyboard navigation | Yes | Phase 1 / MVP | P0 | No | Customer | `/portal-experience/use-keyboard-navigation` | - | - | - | - | UJ-005 Provision Service |
| 01.01.02.03 | Support screen readers | Yes | Phase 1 / MVP | P0 | No | Customer | `/portal-experience/support-screen-readers` | - | - | - | - | UJ-005 Provision Service |

## 01.02 Customer Dashboard

### Feature 01.02.01 Business Overview

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 01.02.01.01 | View organization summary | Yes | Phase 1 / MVP | P0 | No | Customer | `/customer-dashboard/view-organization-summary` | - | - | - | - | UJ-005 Provision Service |
| 01.02.01.02 | View subscriptions | Yes | Phase 1 / MVP | P0 | No | Customer | `/customer-dashboard/view-subscriptions` | - | - | - | - | UJ-005 Provision Service |
| 01.02.01.03 | View spending | Yes | Phase 1 / MVP | P0 | No | Customer | `/customer-dashboard/view-spending` | - | - | - | - | UJ-005 Provision Service |
| 01.02.01.04 | View usage | Yes | Phase 1 / MVP | P0 | No | Customer | `/customer-dashboard/view-usage` | - | - | - | - | UJ-005 Provision Service |

### Feature 01.02.02 Service Health

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 01.02.02.01 | View service status | Yes | Phase 1 / MVP | P0 | No | Customer | `/customer-dashboard/view-service-status` | - | - | - | - | UJ-005 Provision Service |
| 01.02.02.02 | View alerts | Yes | Phase 1 / MVP | P0 | No | Customer | `/customer-dashboard/view-alerts` | - | - | - | - | UJ-005 Provision Service |
| 01.02.02.03 | View incidents | Yes | Phase 1 / MVP | P0 | No | Customer | `/customer-dashboard/view-incidents` | - | - | - | - | UJ-005 Provision Service |

## 01.03 Global Search

### Feature 01.03.01 Unified Search

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No. [C70](../open-decisions.md#c70) (2026-10-05): keyword search improved ([REQ-PRT-002](../../../02-requirements/FRD/global-search/requirement.md)) and 01.03.01.02 Semantic search specified and built ([REQ-PRT-003](../../../02-requirements/FRD/semantic-search/requirement.md)), uses an open-source embedding model, no generated text. Both built early on 2026-10-05; planned sprint 2027.1.3 unchanged.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 01.03.01.01 | Keyword search | Yes | Phase 1 / MVP | P0 | No | Customer | `/global-search/keyword-search` | - | - | - | - | UJ-005 Provision Service |
| 01.03.01.02 | Semantic search | Yes | Phase 1 / MVP | P0 | No | Customer | `/global-search/semantic-search` | - | - | - | - | UJ-005 Provision Service |
| 01.03.01.03 | Filter results | Yes | Phase 1 / MVP | P0 | No | Customer | `/global-search/filter-results` | - | - | - | - | UJ-005 Provision Service |
| 01.03.01.04 | Sort results | Yes | Phase 1 / MVP | P0 | No | Customer | `/global-search/sort-results` | - | - | - | - | UJ-005 Provision Service |
| 01.03.01.05 | View search history | Yes | Phase 1 / MVP | P0 | No | Customer | `/global-search/view-search-history` | - | - | - | - | UJ-005 Provision Service |

## 01.04 Notifications & Communications

### Feature 01.04.01 Notification Center

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 01.04.01.01 | View notifications | Yes | Phase 1 / MVP | P0 | No | Customer | `/notifications-communications/view-notifications` | - | - | - | - | UJ-005 Provision Service |
| 01.04.01.02 | Mark notification read | Yes | Phase 1 / MVP | P0 | No | Customer | `/notifications-communications/mark-notification-read` | - | - | - | - | UJ-005 Provision Service |
| 01.04.01.03 | Configure notification preferences | Yes | Phase 1 / MVP | P0 | No | Customer | `/notifications-communications/configure-notification-preferences` | - | - | - | - | UJ-005 Provision Service |

### Feature 01.04.02 Outbound Communications

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 01.04.02.01 | Send email | Yes | Phase 1 / MVP | P0 | No | Customer | `/notifications-communications/send-email` | - | - | - | - | UJ-005 Provision Service |
| 01.04.02.02 | Send SMS | Yes | Phase 1 / MVP | P0 | No | Customer | `/notifications-communications/send-sms` | - | - | - | - | UJ-005 Provision Service |
| 01.04.02.03 | Send in-app notification | Yes | Phase 1 / MVP | P0 | No | Customer | `/notifications-communications/send-in-app-notification` | - | - | - | - | UJ-005 Provision Service |

> **Columns from [WB:Traceability]** (Primary API, Microservice, Entity, Event, Journey) are kept for reference only. Under [C13](../open-decisions.md#c13) microservices are logical domains built as modules in the single backend. Under [C14](../open-decisions.md#c14) the implemented endpoints and each FRD's `api-requirements.md` are the source of truth for APIs.

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| all | Customer Dashboard functions display subscriptions, spending, usage, service status, alerts and incidents | [WB:Functions] 01.02.* |

## Deliverables

Not specified in any source. Under [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates) the sprint commits this application's P0 capabilities; P1 capabilities are stretch scope.

## Requirements, design and tests

| Artifact | Location | Status |
|---|---|---|
| Feature FRD | `docs/02-requirements/FRD/<feature>/` (copy `_template/`) | See the sprint page for FRDs in progress |
| Requirement | `REQ-PRT-<NNN>` inside the FRD | Approved FRD required before build ([DN-4](../open-decisions.md#dn-4-business-rules-and-acceptance-criteria)) |
| Business rules | `docs/03-business-rules/` and `FRD/<feature>/business-rules.md` | Per FRD |
| Test cases | `test-cases/functional/global-search/TC-PRT-001..004.md`, `TC-PRT-020..027.md`; `test-cases/functional/semantic-search/TC-PRT-028..031.md` | Created ([REQ-PRT-002](../../../02-requirements/FRD/global-search/requirement.md), [REQ-PRT-003](../../../02-requirements/FRD/semantic-search/requirement.md)); all automated tests passed 2026-10-05 |
| NFRs | [WB:Non-Functional Requirements] NFR-001 to NFR-014 (platform-wide) | See [EIS-document-analysis.md](../EIS-document-analysis.md) section 2.17 |

## Related code already in this repository

Observed on branch `dev`. This is a module-level mapping, not a verified function-by-function implementation status.

- Backend: `backend/src/main/java/com/vyoog/eisplatform/modules/dashboard`, `backend/src/main/java/com/vyoog/eisplatform/modules/notification`, `backend/src/main/java/com/vyoog/eisplatform/modules/preference`, `backend/src/main/java/com/vyoog/eisplatform/modules/servicestatus`, `backend/src/main/java/com/vyoog/eisplatform/modules/search` (01.03.01 including semantic search, C70: search index, keyword and hybrid engines, admin), `ai-service/app/embeddings.py` (embedding endpoint)
- Frontend: `frontend/src/pages/HomePage.tsx`, `frontend/src/pages/BusinessDashboardPage.tsx`, `frontend/src/pages/PreferencesPage.tsx`, `frontend/src/components/layout/NotificationBell.tsx`, `frontend/src/components/layout/SiteNavbar.tsx`, `frontend/src/components/layout/AppShell.tsx`, `frontend/src/components/layout/appNavigation.ts`, `frontend/src/pages/ServiceStatusPage.tsx`, `frontend/src/pages/admin/ServiceStatusAdminPage.tsx`, `frontend/src/api/serviceStatusApi.ts`, `frontend/src/pages/GlobalSearchPage.tsx`, `frontend/src/components/layout/TopBarSearch.tsx`, `frontend/src/components/search/`, `frontend/src/pages/admin/AdminSearchPage.tsx`
