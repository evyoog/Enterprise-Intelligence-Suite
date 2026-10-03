# SPRINT-2026.4.2

| Field | Value |
|---|---|
| Sprint ID (PI.Sprint) | 2026.4.2 |
| PI – CY Quarter | 2026.4 |
| Start / end dates | 1–30 Nov 2026 ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Sprint goal | Not specified |
| Team / capacity | Not specified |
| Status | Not specified |
| Source | [PO] roadmap table ([`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)); decisions in [open-decisions.md](../open-decisions.md) |
| Previous / next sprint | [2026.4.1](SPRINT-2026.4.1.md) · [2026.4.3](SPRINT-2026.4.3.md) |

## Scope

| Application ID | Code | Application | Roadmap item | Source |
|---|---|---|---|---|
| 05 | `APP-TEN` | [Customer / Tenant Management](../applications/05-customer-tenant-management.md) | Carry-over only: 05.02 Tenant Lifecycle, 05.03.01 Invite/Create user, 05.04.02 Projects (see [C31](../open-decisions.md#c31)) | [PO] "eVyoog EIS - Roadmap Initiatives" |
| 13a | `APP-INT` (part) | Gateway & Events (API Management 13.01, Event Platform 13.03) | [C31](../open-decisions.md#c31): pulled forward from 2027.1.1 |
| 15a | `APP-GOV` (part) | Audit & Platform Administration (15.01 Platform Administration, 15.03 Audit) | [C31](../open-decisions.md#c31): pulled forward from 2027.2.2 |

> **Commitment ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)):** this sprint commits the P0 (MVP) capabilities of its applications and treats P1 capabilities as stretch scope. Anything not finished is recorded as carry-over on the next sprint page.
>
> 13a's scope table is below, under "EIS 13a" ([C61](../open-decisions.md#c61), [C62](../open-decisions.md#c62)). 15a's own table is under "EIS 15a". 15.03 Audit Logging/Search were already built early, in sprint 2026.3.3 (see that sprint page's own audit trail work).

## Scope changes from decisions

| Change | Decision and scope | FRD | Requirement |
|---|---|---|---|
| Pulled forward | [C25](../open-decisions.md#c25) 05.01.01.02–.05 (update, suspend, activate, soft close organization) were built early in sprint [2026.3.3](SPRINT-2026.3.3.md). 05.01.01.01 Create organization stays in this sprint | [organization-lifecycle](../../../02-requirements/FRD/organization-lifecycle/requirement.md) | REQ-TEN-001 |
| Pulled back | [C31](../open-decisions.md#c31) Most of 05 (05.02, 05.03.01.03–.05, 05.03.02, 05.04.01) moved to [2026.4.1](SPRINT-2026.4.1.md). Only Invite/Create user (05.03.01.01/.02) and Projects (05.04.02) stay here, as carry-over from that sprint ([C34](../open-decisions.md#c34), [C35](../open-decisions.md#c35)) | [member-lifecycle](../../../02-requirements/FRD/member-lifecycle/requirement.md), [group-management](../../../02-requirements/FRD/group-management/requirement.md) | REQ-TEN-002, REQ-TEN-003 |
| Pulled forward | [C31](../open-decisions.md#c31) 13a Gateway & Events (from 2027.1.1) and 15a Audit & Platform Administration (from 2027.2.2) added to this sprint | - | - |
| Decided | [C36](../open-decisions.md#c36) Platform Administration: currencies (enable/disable), regions (free CRUD), feature flags (CRUD + "groups_enabled"), languages (read-only). Configure defaults and Manage templates carried further | [platform-administration](../../../02-requirements/FRD/platform-administration/requirement.md) | REQ-GOV-001 |
| Decided | [C61](../open-decisions.md#c61) (D12 → B) API management inside the platform backend: API keys, rate limits, `/v1` versioning, per-key usage. No separate gateway product | [api-management](../../../02-requirements/FRD/api-management/requirement.md) | REQ-INT-001 |
| Decided | [C62](../open-decisions.md#c62) (D13 → A) Platform events through a transactional outbox with at-least-once dispatch, retries and an admin view. External webhooks (D19) not included | [event-platform](../../../02-requirements/FRD/event-platform/requirement.md) | REQ-INT-002 |
| Decided | [C37](../open-decisions.md#c37) Tenant Lifecycle: Assign region and Configure tenant policies (allowSeatOverage) built; Create/Configure tenant and Configure isolation satisfied by existing code, no new capability | [tenant-lifecycle](../../../02-requirements/FRD/tenant-lifecycle/requirement.md) | REQ-TEN-004 |

### FRDs in this sprint

| FRD | Requirement | Functions | Status |
|---|---|---|---|
| [platform-administration](../../../02-requirements/FRD/platform-administration/requirement.md) | REQ-GOV-001 | 15.01.01 (currencies, feature flags, languages), 15.01.02 (regions) | Approved |
| [tenant-lifecycle](../../../02-requirements/FRD/tenant-lifecycle/requirement.md) | REQ-TEN-004 | 05.02.01.03, 05.02.01.05 (.01/.02/.04 satisfied by existing code) | Approved |
| [api-management](../../../02-requirements/FRD/api-management/requirement.md) | REQ-INT-001 | 13.01.01.03, 13.01.02.01–.04 (13.01.01.02 partly; .01, .04 Not specified) | Draft — built 2026-10-03 at the product owner's request |
| [event-platform](../../../02-requirements/FRD/event-platform/requirement.md) | REQ-INT-002 | 13.03.01.01–.04 (.05 Replay Not specified) | Draft — built 2026-10-03 at the product owner's request |

### Progress (as of 2026-10-03)

| Feature | Status | Note |
|---|---|---|
| 15.01.01 Platform Configuration (currencies, feature flags, languages) | Done (this FRD's scope) | Configure defaults not built |
| 15.01.02 Global Settings (regions) | Partly done | Regions built; Configure defaults, Manage templates not built |
| 15.03 Audit | Done | Already built early in 2026.3.3 (see that sprint's own audit trail work) |
| 13a 13.01 API Management | Done (this FRD's scope) | API keys, rate limits, `/v1` alias, per-key usage ([REQ-INT-001](../../../02-requirements/FRD/api-management/requirement.md)); FRD still Draft, open questions listed there |
| 13a 13.03 Event Platform | Done (this FRD's scope) | Outbox, dispatcher, retries, admin events view ([REQ-INT-002](../../../02-requirements/FRD/event-platform/requirement.md)); Replay not built; FRD still Draft |
| 05.02 Tenant Lifecycle | Done (this FRD's scope) | Assign region, tenant policies built; Create/Configure tenant, Configure isolation satisfied by existing code (C37) |
| 05.03.01 Invite/Create user | Not started | Carried again — needs a decision on the identity-creation flow ([C34](../open-decisions.md#c34)) |
| 05.04.02 Projects | Not started | Carried again — needs a decision on the Project resource model ([C35](../open-decisions.md#c35)) |

The remaining 15.01.02 items remain open for this sprint; 13a's FRDs need approval of their open questions.

## EIS 05 Customer / Tenant Management (carry-over only)

**Planned work:** 05.01, 05.03.02 and 05.04.01 were already built (2026.3.3, 2026.4.1) and are not repeated here — only this sprint's actual carry-over is listed.

Full breakdown with APIs, services, entities and events: [applications/05-customer-tenant-management.md](../applications/05-customer-tenant-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [05.02 Tenant Management](../applications/05-customer-tenant-management.md#0502-tenant-management) | 05.02.01 Tenant Lifecycle | Create tenant; Configure tenant; Assign region; Configure isolation; Configure tenant policies | P0 | Commit — **Done** ([tenant-lifecycle](../../../02-requirements/FRD/tenant-lifecycle/requirement.md)) |
| [05.03 User Management](../applications/05-customer-tenant-management.md#0503-user-management) | 05.03.01 User Lifecycle | Invite user; Create user | P0 | Commit — **Not started** |
| [05.04 Group & Project Management](../applications/05-customer-tenant-management.md#0504-group--project-management) | 05.04.02 Projects | Create project; Assign users; Assign resources | P0 | Commit — **Not started** |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - UJ-004 Tenant Onboarding (Identity, Tenant, IAM) (applications 06; [WB:User Journeys])
  - EVT-002 TenantCreated is consumed by Provisioning and Audit (applications 09, 15; [WB:Events])

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)); 05.02 is done, the other two remain open.

### Related code already in this repository

Observed on branch `dev`, module level only: `backend/…/modules/registration` (organization, members, seats, organization lifecycle; now also region assignment and seat-overage policy — [tenant-lifecycle](../../../02-requirements/FRD/tenant-lifecycle/requirement.md)); `frontend/src/pages/register/OrganizationRegisterPage.tsx`, `frontend/src/pages/register/VerifyEmailPage.tsx`, `frontend/src/pages/admin/RegistrationsAdminPage.tsx`, `frontend/src/components/admin/OrganizationEditDialog.tsx` (now with region/seat-overage fields), `frontend/src/components/admin/OrganizationLifecycleDialog.tsx`.

## EIS 13a Integration & API Platform: gateway and events

**Planned work:** 13.01 API Management and 13.03 Event Platform ([C31](../open-decisions.md#c31) pulled forward). Decided by [C61](../open-decisions.md#c61) and [C62](../open-decisions.md#c62).

Full breakdown: [applications/13-integration-api-platform.md](../applications/13-integration-api-platform.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [13.01 API Management](../applications/13-integration-api-platform.md#1301-api-management) | 13.01.01 API Lifecycle | Register API; Publish API; Version API; Deprecate API | P0 | Commit — Version **Done**, Publish partly (OpenAPI), Register and Deprecate **Not specified** ([api-management](../../../02-requirements/FRD/api-management/requirement.md)) |
| [13.01 API Management](../applications/13-integration-api-platform.md#1301-api-management) | 13.01.02 API Security | Authenticate API; Authorize API; Rate limit API; Monitor API | P0 | Commit — **Done** (Monitor partly: per-key usage) |
| [13.03 Event Platform](../applications/13-integration-api-platform.md#1303-event-platform) | 13.03.01 Event Bus | Publish event; Subscribe to event; Route event; Retry event; Replay event | P0 | Commit — **Done** except Replay (**Not specified**) ([event-platform](../../../02-requirements/FRD/event-platform/requirement.md)) |

### Dependencies

- **Stated:** [C61](../open-decisions.md#c61), [C62](../open-decisions.md#c62).
- **Implied:** the renewal job and reminders ([REQ-SUB-004](../../../02-requirements/FRD/renewal-reminders/requirement.md), sprint [2026.4.3](SPRINT-2026.4.3.md)) publish events through the outbox. External webhooks (13.04, D19) are not decided.

### Expected deliverables

- The P0 functions above that have a decided scope; the rest are open questions in the FRDs.

### Related code already in this repository

New this sprint: `backend/…/modules/events` (outbox, dispatcher, admin API), `backend/…/modules/apikeys` (API keys, rate limiting, `/v1` alias); `frontend/src/pages/admin/AdminPlatformEventsPage.tsx`, `frontend/src/pages/admin/AdminApiKeysPage.tsx`, `frontend/src/components/security/ApiKeysSection.tsx`.

## EIS 15a Administration & Governance: platform administration

**Planned work:** Platform Configuration and Global Settings — audit (15.03) was already built early, in 2026.3.3.

Full breakdown: [applications/15-administration-governance.md](../applications/15-administration-governance.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [15.01 Platform Administration](../applications/15-administration-governance.md#1501-platform-administration) | 15.01.01 Platform Configuration | Configure platform (currencies, feature flags); Configure languages | Not specified | Not specified — **Done** ([platform-administration](../../../02-requirements/FRD/platform-administration/requirement.md)) |
| [15.01 Platform Administration](../applications/15-administration-governance.md#1501-platform-administration) | 15.01.02 Global Settings | Configure regions; Configure defaults; Manage templates | Not specified | Not specified — regions **Done**, defaults/templates **Not started** |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]:** consumed by [tenant-lifecycle](../../../02-requirements/FRD/tenant-lifecycle/requirement.md) (regions) and by 05.04.01 Groups (the "groups_enabled" flag).

### Expected deliverables

- The capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)); their [WB] priority is Not specified in any source.

### Related code already in this repository

New this sprint: `backend/…/modules/administration` (currencies, regions, feature flags); `frontend/src/pages/admin/settings/CommonSettingsPage.tsx` (replaces its earlier placeholder), `frontend/src/api/platformAdministrationApi.ts`.

## Decisions affecting this sprint

- [C3](../open-decisions.md#c3) The [PO] sprint order is authoritative; the MVP is complete at the end of sprint 2027.1.3.
- [C4](../open-decisions.md#c4), [C5](../open-decisions.md#c5), [C6](../open-decisions.md#c6) MVP, priority and phase as shown above.
- [C31](../open-decisions.md#c31) Corrected sprint sequence: 05 carry-over, 13a and 15a as shown above.
- [C36](../open-decisions.md#c36), [C37](../open-decisions.md#c37), [C61](../open-decisions.md#c61), [C62](../open-decisions.md#c62): see the FRDs above.
- Sprint goal, team, capacity and status are Not specified.

Details: [open-decisions.md](../open-decisions.md).

## Traceability

```
[PO] roadmap row → SPRINT-2026.4.2 → application page → capability → feature → function (requirement candidate)
   → FRD docs/02-requirements/FRD/<feature>/ → REQ-<CODE>-<NNN> (Approved before build)
   → STORY-<CODE>-<NNN> with "Sprint (PI.Sprint)" = 2026.4.2
   → code (backend/ · frontend/ · ai-service/) → TC-<CODE>-<NNN> in test-cases/ → UAT
```

Stories for this sprint use `docs/02-requirements/functional-requirements/user-story-template.md` or the GitHub **User Story** issue template, with **Sprint (PI.Sprint) = 2026.4.2**. The Definition of Done is the one in the story template.

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)
- **Decisions:** [`open-decisions.md`](../open-decisions.md) (2026-09-25)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source or decision gives are written **Not specified**.
