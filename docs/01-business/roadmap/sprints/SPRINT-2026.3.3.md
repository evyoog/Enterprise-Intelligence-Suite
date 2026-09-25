# SPRINT-2026.3.3

| Field | Value |
|---|---|
| Sprint ID (PI.Sprint) | 2026.3.3 |
| PI – CY Quarter | 2026.3 |
| Start / end dates | 1–30 Sep 2026 ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Sprint goal | Not specified |
| Team / capacity | Not specified |
| Status | Not specified |
| Source | [PO] roadmap table ([`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)); decisions in [open-decisions.md](../open-decisions.md) |
| Previous / next sprint | (first) · [2026.4.1](SPRINT-2026.4.1.md) |

## Scope

| Application ID | Code | Application | Roadmap item | Source |
|---|---|---|---|---|
| 01 | `APP-PRT` | [Enterprise Intelligence Suite](../applications/01-enterprise-intelligence-suite.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |
| 06 | `APP-IAM` | [Identity & Access Management](../applications/06-identity-access-management.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |

> **Commitment ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)):** this sprint commits the P0 (MVP) capabilities of its applications and treats P1 capabilities as stretch scope. Anything not finished is recorded as carry-over on the next sprint page.

## Scope changes from decisions

| Change | Decision and scope | FRD | Requirement |
|---|---|---|---|
| Added | [C20](../open-decisions.md#c20) Interim service status page: platform admins post per-product status and incidents manually, and customers view them (01.02.02). The FRD must be Approved before build | [service-status-page](../../../02-requirements/FRD/service-status-page/requirement.md) | REQ-PRT-001 |
| Added | [C22](../open-decisions.md#c22) Per-organization OIDC identity-provider federation, mirroring the SAML design (06.04.01 Configure OIDC) | [oidc-federation](../../../02-requirements/FRD/oidc-federation/requirement.md) | REQ-IAM-006 |
| Added | [C23](../open-decisions.md#c23) Configurable claim mapping per identity provider (SAML and OIDC) for email, first name, last name and display name; role mapping out of scope (06.04.01 Map claims) | [claim-mapping](../../../02-requirements/FRD/claim-mapping/requirement.md) | REQ-IAM-007 |
| Added | [C24](../open-decisions.md#c24) Read-only endpoint listing the permissions a user may request, shown as a dropdown in the request form (06.03.01) | [privileged-access](../../../02-requirements/FRD/privileged-access/requirement.md) | REQ-IAM-004 |
| Clarified | [C21](../open-decisions.md#c21) 06.02.02 Policy means the organization MFA policy in this sprint. The general policy engine moves to sprint [2027.2.2](SPRINT-2027.2.2.md) | [organization-mfa-policy](../../../02-requirements/FRD/organization-mfa-policy/requirement.md) | REQ-IAM-001 |
| Deferred | [C16](../open-decisions.md#c16) Semantic search (01.03.01) moves to sprint [2027.1.3](SPRINT-2027.1.3.md). Keyword search satisfies 01.03.01 here | - | - |
| Deferred | [C17](../open-decisions.md#c17) Global search stays products-only here. Knowledge articles and support tickets are added in sprint [2027.1.3](SPRINT-2027.1.3.md) | - | - |
| Deferred | [C18](../open-decisions.md#c18) Send SMS (01.04.02) is deferred until an SMS provider and rules are specified in a new FRD (no sprint assigned). Email and in-app notifications satisfy 01.04.02 here | - | - |
| Deferred | [C19](../open-decisions.md#c19) View spending (01.02.01) moves to sprint [2026.4.3](SPRINT-2026.4.3.md). The dashboard keeps its "not available" note | - | - |

## FRDs for this sprint

Build starts only when the FRD is Approved ([DN-4](../open-decisions.md#dn-4-business-rules-and-acceptance-criteria)).

| FRD | Requirement | Functions | Status |
|---|---|---|---|
| [organization-mfa-policy](../../../02-requirements/FRD/organization-mfa-policy/requirement.md) | REQ-IAM-001 | 06.02.02 | Approved |
| [member-role-assignment](../../../02-requirements/FRD/member-role-assignment/requirement.md) | REQ-IAM-002 | 06.02.01 | Approved |
| [role-permission-administration](../../../02-requirements/FRD/role-permission-administration/requirement.md) | REQ-IAM-003 | 06.02.01 | Approved |
| [privileged-access](../../../02-requirements/FRD/privileged-access/requirement.md) | REQ-IAM-004 | 06.03.01 | Approved |
| [saml-federation](../../../02-requirements/FRD/saml-federation/requirement.md) | REQ-IAM-005 | 06.04.01 | Approved |
| [oidc-federation](../../../02-requirements/FRD/oidc-federation/requirement.md) | REQ-IAM-006 | 06.04.01 | Approved |
| [claim-mapping](../../../02-requirements/FRD/claim-mapping/requirement.md) | REQ-IAM-007 | 06.04.01 | Approved |
| [service-status-page](../../../02-requirements/FRD/service-status-page/requirement.md) | REQ-PRT-001 | 01.02.02 | Approved |

## EIS 01 Enterprise Intelligence Suite

**Planned work ([PO] / [WB] description):** Customer-facing web/mobile experience

Full breakdown with APIs, services, entities and events: [applications/01-enterprise-intelligence-suite.md](../applications/01-enterprise-intelligence-suite.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [01.01 Portal Experience](../applications/01-enterprise-intelligence-suite.md#0101-portal-experience) | 01.01.01 Responsive Web Portal | Login; Navigate portal; Select language; Select region; Customize preferences | P0 | Commit |
| [01.01 Portal Experience](../applications/01-enterprise-intelligence-suite.md#0101-portal-experience) | 01.01.02 Accessibility | Configure accessibility preferences; Use keyboard navigation; Support screen readers | P0 | Commit |
| [01.02 Customer Dashboard](../applications/01-enterprise-intelligence-suite.md#0102-customer-dashboard) | 01.02.01 Business Overview | View organization summary; View subscriptions; View spending; View usage | P0 | Commit |
| [01.02 Customer Dashboard](../applications/01-enterprise-intelligence-suite.md#0102-customer-dashboard) | 01.02.02 Service Health | View service status; View alerts; View incidents | P0 | Commit |
| [01.03 Global Search](../applications/01-enterprise-intelligence-suite.md#0103-global-search) | 01.03.01 Unified Search | Keyword search; Semantic search; Filter results; Sort results; View search history | P0 | Commit |
| [01.04 Notifications & Communications](../applications/01-enterprise-intelligence-suite.md#0104-notifications--communications) | 01.04.01 Notification Center | View notifications; Mark notification read; Configure notification preferences | P0 | Commit |
| [01.04 Notifications & Communications](../applications/01-enterprise-intelligence-suite.md#0104-notifications--communications) | 01.04.02 Outbound Communications | Send email; Send SMS; Send in-app notification | P0 | Commit |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - Customer Dashboard functions display subscriptions, spending, usage, service status, alerts and incidents (applications all; [WB:Functions] 01.02.*)

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). Deliverables are otherwise Not specified in any source.

### Related code already in this repository

Observed on branch `dev`, module level only: `backend/…/modules/dashboard`, `backend/…/modules/notification`, `backend/…/modules/preference`; `frontend/src/pages/HomePage.tsx`, `frontend/src/pages/BusinessDashboardPage.tsx`, `frontend/src/pages/PreferencesPage.tsx`, `frontend/src/components/layout/NotificationBell.tsx`, `frontend/src/components/layout/SiteNavbar.tsx`.

## EIS 06 Identity & Access Management

**Planned work ([PO] / [WB] description):** Authentication, authorization, roles and policies

Full breakdown with APIs, services, entities and events: [applications/06-identity-access-management.md](../applications/06-identity-access-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [06.01 Authentication](../applications/06-identity-access-management.md#0601-authentication) | 06.01.01 Credentials | Sign in; Sign out; Reset password; Manage sessions | P0 | Commit |
| [06.01 Authentication](../applications/06-identity-access-management.md#0601-authentication) | 06.01.02 MFA | Enroll MFA; Verify MFA; Recover MFA | P0 | Commit |
| [06.02 Authorization](../applications/06-identity-access-management.md#0602-authorization) | 06.02.01 RBAC | Create role; Define permission; Assign role; Evaluate permission | P0 | Commit |
| [06.02 Authorization](../applications/06-identity-access-management.md#0602-authorization) | 06.02.02 Policy | Create policy; Assign policy; Evaluate policy | P0 | Commit |
| [06.03 Privileged Access](../applications/06-identity-access-management.md#0603-privileged-access) | 06.03.01 Administrative Access | Request elevated access; Approve access; Grant temporary access; Revoke access | P0 | Commit |
| [06.04 Identity Federation](../applications/06-identity-access-management.md#0604-identity-federation) | 06.04.01 SSO | Configure SAML; Configure OIDC; Map claims; Test federation | P0 | Commit |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - UJ-004 Tenant Onboarding (Identity, Tenant, IAM) (applications 05; [WB:User Journeys])
  - EVT-001 UserCreated is consumed by Tenant and Analytics (applications 05, 16; [WB:Events])

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). Deliverables are otherwise Not specified in any source.

### Related code already in this repository

Observed on branch `dev`, module level only: `backend/…/modules/auth`, `backend/…/modules/authorization`, `backend/…/modules/federation`; `frontend/src/pages/SecuritySettingsPage.tsx`, `frontend/src/pages/OrganizationSamlProvidersPage.tsx`, `frontend/src/pages/ForgotPasswordPage.tsx`, `frontend/src/pages/ResetPasswordPage.tsx`, `frontend/src/pages/admin/AdminPrivilegedAccessPage.tsx`, `frontend/src/auth/`.

## Decisions affecting this sprint

- [C3](../open-decisions.md#c3) The [PO] sprint order is authoritative; the MVP is complete at the end of sprint 2027.1.3.
- [C4](../open-decisions.md#c4), [C5](../open-decisions.md#c5), [C6](../open-decisions.md#c6) MVP, priority and phase as shown above.
- [C10](../open-decisions.md#c10) Application 01 is named "Enterprise Intelligence Suite".
- Sprint goal, team, capacity and status are Not specified.

Details: [open-decisions.md](../open-decisions.md).

## Traceability

```
[PO] roadmap row → SPRINT-2026.3.3 → application page → capability → feature → function (requirement candidate)
   → FRD docs/02-requirements/FRD/<feature>/ → REQ-<CODE>-<NNN> (Approved before build)
   → STORY-<CODE>-<NNN> with "Sprint (PI.Sprint)" = 2026.3.3
   → code (backend/ · frontend/ · ai-service/) → TC-<CODE>-<NNN> in test-cases/ → UAT
```

Stories for this sprint use `docs/02-requirements/functional-requirements/user-story-template.md` or the GitHub **User Story** issue template, with **Sprint (PI.Sprint) = 2026.3.3**. The Definition of Done is the one in the story template.

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)
- **Decisions:** [`open-decisions.md`](../open-decisions.md) (2026-09-25)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source or decision gives are written **Not specified**.
