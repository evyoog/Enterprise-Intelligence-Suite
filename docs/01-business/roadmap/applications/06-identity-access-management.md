# 06 Identity & Access Management

| Field | Value |
|---|---|
| Application ID | 06 ([WB] numbering; an `APP-<CODE>` code is not assigned in any source) |
| Application | Identity & Access Management |
| Description | Authentication, authorization, roles and policies ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2026.3 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2026.3.3](../sprints/SPRINT-2026.3.3.md) |
| Capabilities / features / functions | 4 / 6 / 22 ([WB]) |
| Application status | Not specified |

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.

## Capabilities

| Capability ID | Capability | Features | Priority | MVP | AI relevant |
|---|---|---|---|---|---|
| [06.01](#0601-authentication) | Authentication | 06.01.01 Credentials, 06.01.02 MFA | P0 | Yes | No |
| [06.02](#0602-authorization) | Authorization | 06.02.01 RBAC, 06.02.02 Policy | P0 | Yes | No |
| [06.03](#0603-privileged-access) | Privileged Access | 06.03.01 Administrative Access | P0 | Yes | No |
| [06.04](#0604-identity-federation) | Identity Federation | 06.04.01 SSO | P0 | Yes | No |

> The Priority and MVP values are copied from [WB:Capabilities]. Every capability in [WB] is P0 / MVP=Yes, which conflicts with the function-level MVP flags (C4 in [open-decisions.md](../open-decisions.md)).

## 06.01 Authentication

### Feature 06.01.01 Credentials

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 06.01.01.01 | Sign in | No | No | Platform Service | `/authentication/sign-in` | API-001 POST /v1/auth/login | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.01.01.02 | Sign out | No | No | Platform Service | `/authentication/sign-out` | API-001 POST /v1/auth/login | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.01.01.03 | Reset password | No | No | Platform Service | `/authentication/reset-password` | API-001 POST /v1/auth/login | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.01.01.04 | Manage sessions | No | No | Platform Service | `/authentication/manage-sessions` | API-001 POST /v1/auth/login | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |

### Feature 06.01.02 MFA

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 06.01.02.01 | Enroll MFA | No | No | Platform Service | `/authentication/enroll-mfa` | API-001 POST /v1/auth/login | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.01.02.02 | Verify MFA | No | No | Platform Service | `/authentication/verify-mfa` | API-001 POST /v1/auth/login | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.01.02.03 | Recover MFA | No | No | Platform Service | `/authentication/recover-mfa` | API-001 POST /v1/auth/login | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |

## 06.02 Authorization

### Feature 06.02.01 RBAC

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 06.02.01.01 | Create role | No | No | Platform Service | `/authorization/create-role` | API-002 POST /v1/authz/evaluate | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.02.01.02 | Define permission | No | No | Platform Service | `/authorization/define-permission` | API-002 POST /v1/authz/evaluate | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.02.01.03 | Assign role | No | No | Platform Service | `/authorization/assign-role` | API-002 POST /v1/authz/evaluate | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.02.01.04 | Evaluate permission | No | No | Platform Service | `/authorization/evaluate-permission` | API-002 POST /v1/authz/evaluate | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |

### Feature 06.02.02 Policy

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 06.02.02.01 | Create policy | No | No | Platform Service | `/authorization/create-policy` | API-002 POST /v1/authz/evaluate | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.02.02.02 | Assign policy | No | No | Platform Service | `/authorization/assign-policy` | API-002 POST /v1/authz/evaluate | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.02.02.03 | Evaluate policy | No | No | Platform Service | `/authorization/evaluate-policy` | API-002 POST /v1/authz/evaluate | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |

## 06.03 Privileged Access

### Feature 06.03.01 Administrative Access

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 06.03.01.01 | Request elevated access | No | No | Platform Service | `/privileged-access/request-elevated-access` | - | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.03.01.02 | Approve access | No | No | Platform Service | `/privileged-access/approve-access` | - | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.03.01.03 | Grant temporary access | No | No | Platform Service | `/privileged-access/grant-temporary-access` | - | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.03.01.04 | Revoke access | No | No | Platform Service | `/privileged-access/revoke-access` | - | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |

## 06.04 Identity Federation

### Feature 06.04.01 SSO

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 06.04.01.01 | Configure SAML | No | No | Platform Service | `/identity-federation/configure-saml` | - | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.04.01.02 | Configure OIDC | No | No | Platform Service | `/identity-federation/configure-oidc` | - | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.04.01.03 | Map claims | No | No | Platform Service | `/identity-federation/map-claims` | - | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.04.01.04 | Test federation | No | No | Platform Service | `/identity-federation/test-federation` | - | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 05 | UJ-004 Tenant Onboarding (Identity, Tenant, IAM) | [WB:User Journeys] |
| 05, 16 | EVT-001 UserCreated is consumed by Tenant and Analytics | [WB:Events] |

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

- Backend: `backend/src/main/java/com/vyoog/eisplatform/modules/auth`, `backend/src/main/java/com/vyoog/eisplatform/modules/authorization`, `backend/src/main/java/com/vyoog/eisplatform/modules/federation`
- Frontend: `frontend/src/pages/SecuritySettingsPage.tsx`, `frontend/src/pages/OrganizationSamlProvidersPage.tsx`, `frontend/src/pages/ForgotPasswordPage.tsx`, `frontend/src/pages/ResetPasswordPage.tsx`, `frontend/src/pages/admin/AdminPrivilegedAccessPage.tsx`, `frontend/src/auth/`
