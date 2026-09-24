# 07 Subscription & Entitlement Management

| Field | Value |
|---|---|
| Application ID | 07 ([WB] numbering; an `APP-<CODE>` code is not assigned in any source) |
| Application | Subscription & Entitlement Management |
| Description | Subscriptions, licenses, quotas and entitlements ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2026.4 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2026.4.3](../sprints/SPRINT-2026.4.3.md) |
| Capabilities / features / functions | 4 / 8 / 30 ([WB]) |
| Application status | Not specified |

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.

## Capabilities

| Capability ID | Capability | Features | Priority | MVP | AI relevant |
|---|---|---|---|---|---|
| [07.01](#0701-subscription-management) | Subscription Management | 07.01.01 Subscription Lifecycle, 07.01.02 Subscription Changes | P0 | Yes | No |
| [07.02](#0702-entitlement-management) | Entitlement Management | 07.02.01 Entitlements, 07.02.02 Quota | P0 | Yes | No |
| [07.03](#0703-license--quota-management) | License & Quota Management | 07.03.01 Licensing, 07.03.02 Usage Limits | P0 | Yes | No |
| [07.04](#0704-renewal--lifecycle) | Renewal & Lifecycle | 07.04.01 Renewals, 07.04.02 Lifecycle | P0 | Yes | No |

> The Priority and MVP values are copied from [WB:Capabilities]. Every capability in [WB] is P0 / MVP=Yes, which conflicts with the function-level MVP flags (C4 in [open-decisions.md](../open-decisions.md)).

## 07.01 Subscription Management

### Feature 07.01.01 Subscription Lifecycle

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 07.01.01.01 | Create subscription | Yes | No | Platform Service | `/subscription-management/create-subscription` | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 1 / MVP |
| 07.01.01.02 | Activate subscription | Yes | No | Platform Service | `/subscription-management/activate-subscription` | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 1 / MVP |
| 07.01.01.03 | Suspend subscription | Yes | No | Platform Service | `/subscription-management/suspend-subscription` | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 1 / MVP |
| 07.01.01.04 | Upgrade | No | No | Platform Service | `/subscription-management/upgrade` | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.01.01.05 | Downgrade | No | No | Platform Service | `/subscription-management/downgrade` | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.01.01.06 | Renew | No | No | Platform Service | `/subscription-management/renew` | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.01.01.07 | Cancel | No | No | Platform Service | `/subscription-management/cancel` | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |

### Feature 07.01.02 Subscription Changes

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 07.01.02.01 | Change quantity | Yes | No | Platform Service | `/subscription-management/change-quantity` | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 1 / MVP |
| 07.01.02.02 | Change plan | Yes | No | Platform Service | `/subscription-management/change-plan` | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 1 / MVP |
| 07.01.02.03 | Schedule change | Yes | No | Platform Service | `/subscription-management/schedule-change` | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 1 / MVP |

## 07.02 Entitlement Management

### Feature 07.02.01 Entitlements

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 07.02.01.01 | Grant entitlement | No | No | Platform Service | `/entitlement-management/grant-entitlement` | API-012 POST /v1/entitlements/check | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.02.01.02 | Revoke entitlement | No | No | Platform Service | `/entitlement-management/revoke-entitlement` | API-012 POST /v1/entitlements/check | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.02.01.03 | Validate entitlement | No | No | Platform Service | `/entitlement-management/validate-entitlement` | API-012 POST /v1/entitlements/check | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.02.01.04 | Check feature access | No | No | Platform Service | `/entitlement-management/check-feature-access` | API-012 POST /v1/entitlements/check | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |

### Feature 07.02.02 Quota

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 07.02.02.01 | Check quota | No | No | Platform Service | `/entitlement-management/check-quota` | API-012 POST /v1/entitlements/check | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.02.02.02 | Allocate quota | No | No | Platform Service | `/entitlement-management/allocate-quota` | API-012 POST /v1/entitlements/check | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.02.02.03 | Adjust quota | No | No | Platform Service | `/entitlement-management/adjust-quota` | API-012 POST /v1/entitlements/check | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |

## 07.03 License & Quota Management

### Feature 07.03.01 Licensing

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 07.03.01.01 | Issue license | No | No | Platform Service | `/license-quota-management/issue-license` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.03.01.02 | Validate license | No | No | Platform Service | `/license-quota-management/validate-license` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.03.01.03 | Expire license | No | No | Platform Service | `/license-quota-management/expire-license` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.03.01.04 | Revoke license | No | No | Platform Service | `/license-quota-management/revoke-license` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |

### Feature 07.03.02 Usage Limits

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 07.03.02.01 | Define quota | No | No | Platform Service | `/license-quota-management/define-quota` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.03.02.02 | Monitor quota | No | No | Platform Service | `/license-quota-management/monitor-quota` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.03.02.03 | Enforce quota | No | No | Platform Service | `/license-quota-management/enforce-quota` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |

## 07.04 Renewal & Lifecycle

### Feature 07.04.01 Renewals

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 07.04.01.01 | Schedule renewal | No | No | Platform Service | `/renewal-lifecycle/schedule-renewal` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.04.01.02 | Notify renewal | No | No | Platform Service | `/renewal-lifecycle/notify-renewal` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.04.01.03 | Auto-renew | No | No | Platform Service | `/renewal-lifecycle/auto-renew` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.04.01.04 | Process renewal | No | No | Platform Service | `/renewal-lifecycle/process-renewal` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |

### Feature 07.04.02 Lifecycle

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 07.04.02.01 | Expire subscription | No | No | Platform Service | `/renewal-lifecycle/expire-subscription` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.04.02.02 | Reactivate subscription | No | No | Platform Service | `/renewal-lifecycle/reactivate-subscription` | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 09 | EVT-009 SubscriptionCreated is consumed by Entitlement and Provisioning | [WB:Events] |
| 08 | EVT-010 SubscriptionChanged is consumed by Billing and Entitlement | [WB:Events] |
| 08 | UJ-006 Manage Subscription (Subscription, Pricing, Billing) | [WB:User Journeys] |

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

- Backend: `backend/src/main/java/com/vyoog/eisplatform/modules/registration (SubscriptionController, product access)`
- Frontend: `frontend/src/pages/MyProductsPage.tsx`
