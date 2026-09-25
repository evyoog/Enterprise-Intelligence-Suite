# 07 Subscription & Entitlement Management

| Field | Value |
|---|---|
| Application ID | 07 ([WB] numbering) |
| Application code | `APP-SUB` ([DN-5](../open-decisions.md#dn-5-application-codes)); IDs use `SUB`, for example `REQ-SUB-001` |
| Application | Subscription & Entitlement Management |
| Description | Subscriptions, licenses, quotas and entitlements ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2026.4 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2026.4.3](../sprints/SPRINT-2026.4.3.md) (1–31 Dec 2026, [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Capabilities / features / functions | 4 / 8 / 30 ([WB]) |
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
| [07.01](#0701-subscription-management) | Subscription Management | 07.01.01 Subscription Lifecycle, 07.01.02 Subscription Changes | Yes | Phase 1 / MVP | P0 | C4 |
| [07.02](#0702-entitlement-management) | Entitlement Management | 07.02.01 Entitlements, 07.02.02 Quota | Yes | Phase 1 / MVP | P0 | C4 |
| [07.03](#0703-license--quota-management) | License & Quota Management | 07.03.01 Licensing, 07.03.02 Usage Limits | Yes | Phase 1 / MVP | P0 | C4 |
| [07.04](#0704-renewal--lifecycle) | Renewal & Lifecycle | 07.04.01 Renewals, 07.04.02 Lifecycle | Yes | Phase 1 / MVP | P0 | C4 |

## 07.01 Subscription Management

### Feature 07.01.01 Subscription Lifecycle

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 07.01.01.01 | Create subscription | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/subscription-management/create-subscription` | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 07.01.01.02 | Activate subscription | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/subscription-management/activate-subscription` | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 07.01.01.03 | Suspend subscription | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/subscription-management/suspend-subscription` | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 07.01.01.04 | Upgrade | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/subscription-management/upgrade` | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 07.01.01.05 | Downgrade | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/subscription-management/downgrade` | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 07.01.01.06 | Renew | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/subscription-management/renew` | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 07.01.01.07 | Cancel | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/subscription-management/cancel` | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |

### Feature 07.01.02 Subscription Changes

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 07.01.02.01 | Change quantity | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/subscription-management/change-quantity` | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 07.01.02.02 | Change plan | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/subscription-management/change-plan` | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 07.01.02.03 | Schedule change | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/subscription-management/schedule-change` | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |

## 07.02 Entitlement Management

### Feature 07.02.01 Entitlements

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 07.02.01.01 | Grant entitlement | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/entitlement-management/grant-entitlement` | API-012 POST /v1/entitlements/check | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 07.02.01.02 | Revoke entitlement | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/entitlement-management/revoke-entitlement` | API-012 POST /v1/entitlements/check | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 07.02.01.03 | Validate entitlement | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/entitlement-management/validate-entitlement` | API-012 POST /v1/entitlements/check | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 07.02.01.04 | Check feature access | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/entitlement-management/check-feature-access` | API-012 POST /v1/entitlements/check | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |

### Feature 07.02.02 Quota

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 07.02.02.01 | Check quota | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/entitlement-management/check-quota` | API-012 POST /v1/entitlements/check | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 07.02.02.02 | Allocate quota | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/entitlement-management/allocate-quota` | API-012 POST /v1/entitlements/check | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 07.02.02.03 | Adjust quota | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/entitlement-management/adjust-quota` | API-012 POST /v1/entitlements/check | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |

## 07.03 License & Quota Management

### Feature 07.03.01 Licensing

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 07.03.01.01 | Issue license | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/license-quota-management/issue-license` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 07.03.01.02 | Validate license | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/license-quota-management/validate-license` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 07.03.01.03 | Expire license | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/license-quota-management/expire-license` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 07.03.01.04 | Revoke license | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/license-quota-management/revoke-license` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |

### Feature 07.03.02 Usage Limits

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 07.03.02.01 | Define quota | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/license-quota-management/define-quota` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 07.03.02.02 | Monitor quota | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/license-quota-management/monitor-quota` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 07.03.02.03 | Enforce quota | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/license-quota-management/enforce-quota` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |

## 07.04 Renewal & Lifecycle

### Feature 07.04.01 Renewals

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 07.04.01.01 | Schedule renewal | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/renewal-lifecycle/schedule-renewal` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 07.04.01.02 | Notify renewal | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/renewal-lifecycle/notify-renewal` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 07.04.01.03 | Auto-renew | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/renewal-lifecycle/auto-renew` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 07.04.01.04 | Process renewal | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/renewal-lifecycle/process-renewal` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |

### Feature 07.04.02 Lifecycle

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 07.04.02.01 | Expire subscription | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/renewal-lifecycle/expire-subscription` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |
| 07.04.02.02 | Reactivate subscription | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/renewal-lifecycle/reactivate-subscription` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service |

> **Columns from [WB:Traceability]** (Primary API, Microservice, Entity, Event, Journey) are kept for reference only. Under [C13](../open-decisions.md#c13) microservices are logical domains built as modules in the single backend. Under [C14](../open-decisions.md#c14) the implemented endpoints and each FRD's `api-requirements.md` are the source of truth for APIs.

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 09 | EVT-009 SubscriptionCreated is consumed by Entitlement and Provisioning | [WB:Events] |
| 08 | EVT-010 SubscriptionChanged is consumed by Billing and Entitlement | [WB:Events] |
| 08 | UJ-006 Manage Subscription (Subscription, Pricing, Billing) | [WB:User Journeys] |

## Deliverables

Not specified in any source. Under [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates) the sprint commits this application's P0 capabilities; P1 capabilities are stretch scope.

## Requirements, design and tests

| Artifact | Location | Status |
|---|---|---|
| Feature FRD | `docs/02-requirements/FRD/<feature>/` (copy `_template/`) | See the sprint page for FRDs in progress |
| Requirement | `REQ-SUB-<NNN>` inside the FRD | Approved FRD required before build ([DN-4](../open-decisions.md#dn-4-business-rules-and-acceptance-criteria)) |
| Business rules | `docs/03-business-rules/` and `FRD/<feature>/business-rules.md` | Per FRD |
| Test cases | `test-cases/functional/<feature>/TC-SUB-<NNN>.md` | Not created |
| NFRs | [WB:Non-Functional Requirements] NFR-001 to NFR-014 (platform-wide) | See [EIS-document-analysis.md](../EIS-document-analysis.md) section 2.17 |

## Related code already in this repository

Observed on branch `dev`. This is a module-level mapping, not a verified function-by-function implementation status.

- Backend: `backend/src/main/java/com/vyoog/eisplatform/modules/registration (SubscriptionController, product access)`
- Frontend: `frontend/src/pages/MyProductsPage.tsx`
