# 06 Identity & Access Management

| Field | Value |
|---|---|
| Application ID | 06 ([WB] numbering) |
| Application code | `APP-IAM` ([DN-5](../open-decisions.md#dn-5-application-codes)); IDs use `IAM`, for example `REQ-IAM-001` |
| Application | Identity & Access Management |
| Description | Authentication, authorization, roles and policies ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2026.3 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2026.3.3](../sprints/SPRINT-2026.3.3.md) (1–30 Sep 2026, [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Capabilities / features / functions | 4 / 6 / 22 ([WB]) |
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
| [06.01](#0601-authentication) | Authentication | 06.01.01 Credentials, 06.01.02 MFA | Yes | Phase 1 / MVP | P0 | C4 |
| [06.02](#0602-authorization) | Authorization | 06.02.01 RBAC, 06.02.02 Policy | Yes | Phase 1 / MVP | P0 | C4 |
| [06.03](#0603-privileged-access) | Privileged Access | 06.03.01 Administrative Access | Yes | Phase 1 / MVP | P0 | C4 |
| [06.04](#0604-identity-federation) | Identity Federation | 06.04.01 SSO | Yes | Phase 1 / MVP | P0 | C4 |

## 06.01 Authentication

### Feature 06.01.01 Credentials

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 06.01.01.01 | Sign in | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/authentication/sign-in` | API-001 POST /v1/auth/login | Identity Service | User | UserCreated | UJ-005 Provision Service |
| 06.01.01.02 | Sign out | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/authentication/sign-out` | API-001 POST /v1/auth/login | Identity Service | User | UserCreated | UJ-005 Provision Service |
| 06.01.01.03 | Reset password | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/authentication/reset-password` | API-001 POST /v1/auth/login | Identity Service | User | UserCreated | UJ-005 Provision Service |
| 06.01.01.04 | Manage sessions | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/authentication/manage-sessions` | API-001 POST /v1/auth/login | Identity Service | User | UserCreated | UJ-005 Provision Service |

### Feature 06.01.02 MFA

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 06.01.02.01 | Enroll MFA | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/authentication/enroll-mfa` | API-001 POST /v1/auth/login | Identity Service | User | UserCreated | UJ-005 Provision Service |
| 06.01.02.02 | Verify MFA | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/authentication/verify-mfa` | API-001 POST /v1/auth/login | Identity Service | User | UserCreated | UJ-005 Provision Service |
| 06.01.02.03 | Recover MFA | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/authentication/recover-mfa` | API-001 POST /v1/auth/login | Identity Service | User | UserCreated | UJ-005 Provision Service |

## 06.02 Authorization

### Feature 06.02.01 RBAC

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 06.02.01.01 | Create role | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/authorization/create-role` | API-002 POST /v1/authz/evaluate | Identity Service | User | UserCreated | UJ-005 Provision Service |
| 06.02.01.02 | Define permission | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/authorization/define-permission` | API-002 POST /v1/authz/evaluate | Identity Service | User | UserCreated | UJ-005 Provision Service |
| 06.02.01.03 | Assign role | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/authorization/assign-role` | API-002 POST /v1/authz/evaluate | Identity Service | User | UserCreated | UJ-005 Provision Service |
| 06.02.01.04 | Evaluate permission | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/authorization/evaluate-permission` | API-002 POST /v1/authz/evaluate | Identity Service | User | UserCreated | UJ-005 Provision Service |

### Feature 06.02.02 Policy

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 06.02.02.01 | Create policy | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/authorization/create-policy` | API-002 POST /v1/authz/evaluate | Identity Service | User | UserCreated | UJ-005 Provision Service |
| 06.02.02.02 | Assign policy | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/authorization/assign-policy` | API-002 POST /v1/authz/evaluate | Identity Service | User | UserCreated | UJ-005 Provision Service |
| 06.02.02.03 | Evaluate policy | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/authorization/evaluate-policy` | API-002 POST /v1/authz/evaluate | Identity Service | User | UserCreated | UJ-005 Provision Service |

## 06.03 Privileged Access

### Feature 06.03.01 Administrative Access

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 06.03.01.01 | Request elevated access | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/privileged-access/request-elevated-access` | - | Identity Service | User | UserCreated | UJ-005 Provision Service |
| 06.03.01.02 | Approve access | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/privileged-access/approve-access` | - | Identity Service | User | UserCreated | UJ-005 Provision Service |
| 06.03.01.03 | Grant temporary access | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/privileged-access/grant-temporary-access` | - | Identity Service | User | UserCreated | UJ-005 Provision Service |
| 06.03.01.04 | Revoke access | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/privileged-access/revoke-access` | - | Identity Service | User | UserCreated | UJ-005 Provision Service |

## 06.04 Identity Federation

### Feature 06.04.01 SSO

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 06.04.01.01 | Configure SAML | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/identity-federation/configure-saml` | - | Identity Service | User | UserCreated | UJ-005 Provision Service |
| 06.04.01.02 | Configure OIDC | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/identity-federation/configure-oidc` | - | Identity Service | User | UserCreated | UJ-005 Provision Service |
| 06.04.01.03 | Map claims | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/identity-federation/map-claims` | - | Identity Service | User | UserCreated | UJ-005 Provision Service |
| 06.04.01.04 | Test federation | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/identity-federation/test-federation` | - | Identity Service | User | UserCreated | UJ-005 Provision Service |

> **Columns from [WB:Traceability]** (Primary API, Microservice, Entity, Event, Journey) are kept for reference only. Under [C13](../open-decisions.md#c13) microservices are logical domains built as modules in the single backend. Under [C14](../open-decisions.md#c14) the implemented endpoints and each FRD's `api-requirements.md` are the source of truth for APIs.

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 05 | UJ-004 Tenant Onboarding (Identity, Tenant, IAM) | [WB:User Journeys] |
| 05, 16 | EVT-001 UserCreated is consumed by Tenant and Analytics | [WB:Events] |

## Deliverables

Not specified in any source. Under [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates) the sprint commits this application's P0 capabilities; P1 capabilities are stretch scope.

## Requirements, design and tests

| Artifact | Location | Status |
|---|---|---|
| Feature FRD | `docs/02-requirements/FRD/<feature>/` (copy `_template/`) | See the sprint page for FRDs in progress |
| Requirement | `REQ-IAM-<NNN>` inside the FRD | Approved FRD required before build ([DN-4](../open-decisions.md#dn-4-business-rules-and-acceptance-criteria)) |
| Business rules | `docs/03-business-rules/` and `FRD/<feature>/business-rules.md` | Per FRD |
| Test cases | `test-cases/functional/<feature>/TC-IAM-<NNN>.md` | Not created |
| NFRs | [WB:Non-Functional Requirements] NFR-001 to NFR-014 (platform-wide) | See [EIS-document-analysis.md](../EIS-document-analysis.md) section 2.17 |

## Related code already in this repository

Observed on branch `dev`. This is a module-level mapping, not a verified function-by-function implementation status.

- Backend: `backend/src/main/java/com/vyoog/eisplatform/modules/auth`, `backend/src/main/java/com/vyoog/eisplatform/modules/authorization`, `backend/src/main/java/com/vyoog/eisplatform/modules/federation`
- Frontend: `frontend/src/pages/SecuritySettingsPage.tsx`, `frontend/src/pages/OrganizationSamlProvidersPage.tsx`, `frontend/src/pages/ForgotPasswordPage.tsx`, `frontend/src/pages/ResetPasswordPage.tsx`, `frontend/src/pages/admin/AdminPrivilegedAccessPage.tsx`, `frontend/src/auth/`, `frontend/src/components/organization/`, `frontend/src/components/security/PrivilegedAccessRequestsCard.tsx`, `frontend/src/pages/admin/RolesAdminPage.tsx`, `frontend/src/pages/admin/PermissionsAdminPage.tsx`, `frontend/src/api/rolesApi.ts`, `frontend/src/api/permissionsApi.ts`, `frontend/src/api/privilegedAccessApi.ts`
