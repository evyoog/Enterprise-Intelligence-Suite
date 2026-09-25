# SPRINT-2026.3.3

| Field | Value |
|---|---|
| Sprint ID (PI.Sprint) | 2026.3.3 |
| PI – CY Quarter | 2026.3 |
| Start / end dates | Not specified |
| Sprint goal | Not specified |
| Team / capacity | Not specified |
| Status | Not specified |
| Source | [PO] roadmap tables ([`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)) |
| Previous / next sprint | (first) · [2026.4.1](SPRINT-2026.4.1.md) |

## Scope

| Product | Application ID | Application | Roadmap item(s) | Source |
|---|---|---|---|---|
| EIS (PaaS) | 01 | [Experience & Customer Portal](../applications/01-experience-customer-portal.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |
| EIS (PaaS) | 06 | [Identity & Access Management](../applications/06-identity-access-management.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |

> [PO] assigns **one sprint per EIS application**. It does not say which capabilities or features fall inside this sprint, or whether the application must be finished in it. The EIS scope below is the application's full [WB] breakdown until sprint scope is decided (see [open-decisions.md](../open-decisions.md)).

## EIS 01 Experience & Customer Portal

**Planned work ([PO] / [WB] description):** Customer-facing web/mobile experience

Full breakdown with APIs, services, entities and events: [applications/01-experience-customer-portal.md](../applications/01-experience-customer-portal.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | MVP functions |
|---|---|---|---|
| [01.01 Portal Experience](../applications/01-experience-customer-portal.md#0101-portal-experience) | 01.01.01 Responsive Web Portal | Login; Navigate portal; Select language; Select region; Customize preferences | - |
| [01.01 Portal Experience](../applications/01-experience-customer-portal.md#0101-portal-experience) | 01.01.02 Accessibility | Configure accessibility preferences; Use keyboard navigation; Support screen readers | - |
| [01.02 Customer Dashboard](../applications/01-experience-customer-portal.md#0102-customer-dashboard) | 01.02.01 Business Overview | View organization summary; View subscriptions; View spending; View usage | - |
| [01.02 Customer Dashboard](../applications/01-experience-customer-portal.md#0102-customer-dashboard) | 01.02.02 Service Health | View service status; View alerts; View incidents | - |
| [01.03 Global Search](../applications/01-experience-customer-portal.md#0103-global-search) | 01.03.01 Unified Search | Keyword search; Semantic search; Filter results; Sort results; View search history | - |
| [01.04 Notifications & Communications](../applications/01-experience-customer-portal.md#0104-notifications--communications) | 01.04.01 Notification Center | View notifications; Mark notification read; Configure notification preferences | - |
| [01.04 Notifications & Communications](../applications/01-experience-customer-portal.md#0104-notifications--communications) | 01.04.02 Outbound Communications | Send email; Send SMS; Send in-app notification | - |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - Customer Dashboard functions display subscriptions, spending, usage, service status, alerts and incidents (applications all; [WB:Functions] 01.02.*)

### Expected deliverables

- Not specified in any source.

### Related code already in this repository

Observed on branch `dev`, module level only: `backend/…/modules/dashboard`, `backend/…/modules/notification`, `backend/…/modules/preference`; `frontend/src/pages/HomePage.tsx`, `frontend/src/pages/BusinessDashboardPage.tsx`, `frontend/src/pages/PreferencesPage.tsx`, `frontend/src/components/layout/NotificationBell.tsx`, `frontend/src/components/layout/SiteNavbar.tsx`.

## EIS 06 Identity & Access Management

**Planned work ([PO] / [WB] description):** Authentication, authorization, roles and policies

Full breakdown with APIs, services, entities and events: [applications/06-identity-access-management.md](../applications/06-identity-access-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | MVP functions |
|---|---|---|---|
| [06.01 Authentication](../applications/06-identity-access-management.md#0601-authentication) | 06.01.01 Credentials | Sign in; Sign out; Reset password; Manage sessions | - |
| [06.01 Authentication](../applications/06-identity-access-management.md#0601-authentication) | 06.01.02 MFA | Enroll MFA; Verify MFA; Recover MFA | - |
| [06.02 Authorization](../applications/06-identity-access-management.md#0602-authorization) | 06.02.01 RBAC | Create role; Define permission; Assign role; Evaluate permission | - |
| [06.02 Authorization](../applications/06-identity-access-management.md#0602-authorization) | 06.02.02 Policy | Create policy; Assign policy; Evaluate policy | - |
| [06.03 Privileged Access](../applications/06-identity-access-management.md#0603-privileged-access) | 06.03.01 Administrative Access | Request elevated access; Approve access; Grant temporary access; Revoke access | - |
| [06.04 Identity Federation](../applications/06-identity-access-management.md#0604-identity-federation) | 06.04.01 SSO | Configure SAML; Configure OIDC; Map claims; Test federation | - |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - UJ-004 Tenant Onboarding (Identity, Tenant, IAM) (applications 05; [WB:User Journeys])
  - EVT-001 UserCreated is consumed by Tenant and Analytics (applications 05, 16; [WB:Events])

### Expected deliverables

- Not specified in any source.
- Implied by [WB:Traceability]: APIs `API-001 POST /v1/auth/login`, `API-002 POST /v1/authz/evaluate`; services Identity Service.

### Related code already in this repository

Observed on branch `dev`, module level only: `backend/…/modules/auth`, `backend/…/modules/authorization`, `backend/…/modules/federation`; `frontend/src/pages/SecuritySettingsPage.tsx`, `frontend/src/pages/OrganizationSamlProvidersPage.tsx`, `frontend/src/pages/ForgotPasswordPage.tsx`, `frontend/src/pages/ResetPasswordPage.tsx`, `frontend/src/pages/admin/AdminPrivilegedAccessPage.tsx`, `frontend/src/auth/`.

## Open issues affecting this sprint

- C6: [WB:Traceability] marks IAM and Portal functions "Phase 2" although [PO] schedules them in this first sprint
- The sprint scope, deliverables, acceptance criteria and dates are not specified in any source.

Details: [open-decisions.md](../open-decisions.md).

## Traceability

```
[PO] roadmap row → SPRINT-2026.3.3 → application page → capability → feature → function (requirement candidate)
   → FRD docs/02-requirements/FRD/<feature>/ (not created) → REQ-<APP-CODE>-<NNN> (not created)
   → STORY-<APP-CODE>-<NNN> with "Sprint (PI.Sprint)" = 2026.3.3
   → code (backend/ · frontend/ · ai-service/) → TC-<APP-CODE>-<NNN> in test-cases/ → UAT
```

Stories for this sprint use `docs/02-requirements/functional-requirements/user-story-template.md` or the GitHub **User Story** issue template, with **Sprint (PI.Sprint) = 2026.3.3**. The Definition of Done is the one in the story template.

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.
