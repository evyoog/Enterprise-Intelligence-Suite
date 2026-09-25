# 01 Experience & Customer Portal

| Field | Value |
|---|---|
| Application ID | 01 ([WB] numbering; an `APP-<CODE>` code is not assigned in any source) |
| Application | Experience & Customer Portal |
| Description | Customer-facing web/mobile experience ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2026.3 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| Capabilities / features / functions | 4 / 7 / 26 ([WB]) |
| Application status | Not specified |

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.

## Capabilities

| Capability ID | Capability | Features | Priority | MVP | AI relevant |
|---|---|---|---|---|---|
| [01.01](#0101-portal-experience) | Portal Experience | 01.01.01 Responsive Web Portal, 01.01.02 Accessibility | P0 | Yes | No |
| [01.02](#0102-customer-dashboard) | Customer Dashboard | 01.02.01 Business Overview, 01.02.02 Service Health | P0 | Yes | No |
| [01.03](#0103-global-search) | Global Search | 01.03.01 Unified Search | P0 | Yes | No |
| [01.04](#0104-notifications--communications) | Notifications & Communications | 01.04.01 Notification Center, 01.04.02 Outbound Communications | P0 | Yes | No |

> The Priority and MVP values are copied from [WB:Capabilities]. Every capability in [WB] is P0 / MVP=Yes, which conflicts with the function-level MVP flags (C4 in [open-decisions.md](../open-decisions.md)).

## 01.01 Portal Experience

### Feature 01.01.01 Responsive Web Portal

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 01.01.01.01 | Login | No | No | Customer | `/portal-experience/login` | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.01.01.02 | Navigate portal | No | No | Customer | `/portal-experience/navigate-portal` | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.01.01.03 | Select language | No | No | Customer | `/portal-experience/select-language` | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.01.01.04 | Select region | No | No | Customer | `/portal-experience/select-region` | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.01.01.05 | Customize preferences | No | No | Customer | `/portal-experience/customize-preferences` | - | - | - | - | UJ-005 Provision Service | Phase 2 |

### Feature 01.01.02 Accessibility

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 01.01.02.01 | Configure accessibility preferences | No | No | Customer | `/portal-experience/configure-accessibility-preferences` | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.01.02.02 | Use keyboard navigation | No | No | Customer | `/portal-experience/use-keyboard-navigation` | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.01.02.03 | Support screen readers | No | No | Customer | `/portal-experience/support-screen-readers` | - | - | - | - | UJ-005 Provision Service | Phase 2 |

## 01.02 Customer Dashboard

### Feature 01.02.01 Business Overview

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 01.02.01.01 | View organization summary | No | No | Customer | `/customer-dashboard/view-organization-summary` | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.02.01.02 | View subscriptions | No | No | Customer | `/customer-dashboard/view-subscriptions` | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.02.01.03 | View spending | No | No | Customer | `/customer-dashboard/view-spending` | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.02.01.04 | View usage | No | No | Customer | `/customer-dashboard/view-usage` | - | - | - | - | UJ-005 Provision Service | Phase 2 |

### Feature 01.02.02 Service Health

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 01.02.02.01 | View service status | No | No | Customer | `/customer-dashboard/view-service-status` | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.02.02.02 | View alerts | No | No | Customer | `/customer-dashboard/view-alerts` | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.02.02.03 | View incidents | No | No | Customer | `/customer-dashboard/view-incidents` | - | - | - | - | UJ-005 Provision Service | Phase 2 |

## 01.03 Global Search

### Feature 01.03.01 Unified Search

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 01.03.01.01 | Keyword search | No | No | Customer | `/global-search/keyword-search` | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.03.01.02 | Semantic search | No | No | Customer | `/global-search/semantic-search` | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.03.01.03 | Filter results | No | No | Customer | `/global-search/filter-results` | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.03.01.04 | Sort results | No | No | Customer | `/global-search/sort-results` | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.03.01.05 | View search history | No | No | Customer | `/global-search/view-search-history` | - | - | - | - | UJ-005 Provision Service | Phase 2 |

## 01.04 Notifications & Communications

### Feature 01.04.01 Notification Center

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 01.04.01.01 | View notifications | No | No | Customer | `/notifications-communications/view-notifications` | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.04.01.02 | Mark notification read | No | No | Customer | `/notifications-communications/mark-notification-read` | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.04.01.03 | Configure notification preferences | No | No | Customer | `/notifications-communications/configure-notification-preferences` | - | - | - | - | UJ-005 Provision Service | Phase 2 |

### Feature 01.04.02 Outbound Communications

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 01.04.02.01 | Send email | No | No | Customer | `/notifications-communications/send-email` | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.04.02.02 | Send SMS | No | No | Customer | `/notifications-communications/send-sms` | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.04.02.03 | Send in-app notification | No | No | Customer | `/notifications-communications/send-in-app-notification` | - | - | - | - | UJ-005 Provision Service | Phase 2 |

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| all | Customer Dashboard functions display subscriptions, spending, usage, service status, alerts and incidents | [WB:Functions] 01.02.* |

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

- Backend: `backend/src/main/java/com/vyoog/eisplatform/modules/dashboard`, `backend/src/main/java/com/vyoog/eisplatform/modules/notification`, `backend/src/main/java/com/vyoog/eisplatform/modules/preference`
- Frontend: `frontend/src/pages/HomePage.tsx`, `frontend/src/pages/BusinessDashboardPage.tsx`, `frontend/src/pages/PreferencesPage.tsx`, `frontend/src/components/layout/NotificationBell.tsx`, `frontend/src/components/layout/SiteNavbar.tsx`
