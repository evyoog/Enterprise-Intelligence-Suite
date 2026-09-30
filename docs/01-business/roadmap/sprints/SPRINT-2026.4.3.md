# SPRINT-2026.4.3

| Field | Value |
|---|---|
| Sprint ID (PI.Sprint) | 2026.4.3 |
| PI – CY Quarter | 2026.4 |
| Start / end dates | 1–31 Dec 2026 ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Sprint goal | Not specified |
| Team / capacity | Not specified |
| Status | Not specified |
| Source | [PO] roadmap table ([`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)); decisions in [open-decisions.md](../open-decisions.md) |
| Previous / next sprint | [2026.4.2](SPRINT-2026.4.2.md) · [2027.1.1](SPRINT-2027.1.1.md) |

## Scope

| Application ID | Code | Application | Roadmap item | Source |
|---|---|---|---|---|
| 07 | `APP-SUB` | [Subscription & Entitlement Management](../applications/07-subscription-entitlement-management.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |
| 08 | `APP-BIL` | [Billing & Payments](../applications/08-billing-payments.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |

> **Commitment ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)):** this sprint commits the P0 (MVP) capabilities of its applications and treats P1 capabilities as stretch scope. Anything not finished is recorded as carry-over on the next sprint page.

## Scope changes from decisions

| Change | Decision and scope | FRD | Requirement |
|---|---|---|---|
| Added | [C19](../open-decisions.md#c19) View spending (01.02.01, application 01 Enterprise Intelligence Suite), delivered with Billing & Payments (08). Moved from sprint [2026.3.3](SPRINT-2026.3.3.md) | - | - |
| Decided | [C38](../open-decisions.md#c38) Subscription Lifecycle: suspend/reactivate/cancel/renew/change-plan built for individual-customer subscriptions, plus a scheduled auto-expiry job. Change quantity/Schedule change, 07.02 Entitlement Management, 07.03 License & Quota Management, Schedule/Notify/Auto-renew, organization-owned-subscription actions, and all of 08 Billing & Payments carried further | [subscription-lifecycle](../../../02-requirements/FRD/subscription-lifecycle/requirement.md) | REQ-SUB-001 |
| Decided | [C46](../open-decisions.md#c46) The MVP must include online payment; provider Razorpay (credentials later). Billing screens and backend built now in "Payment gateway not configured" mode; card entry only in Razorpay's secure window; one common secrets file | [billing-payments](../../../02-requirements/FRD/billing-payments/requirement.md) | REQ-BIL-001 |

### FRDs in this sprint

| FRD | Requirement | Functions | Status |
|---|---|---|---|
| [subscription-lifecycle](../../../02-requirements/FRD/subscription-lifecycle/requirement.md) | REQ-SUB-001 | 07.01.01 (suspend, reactivate, cancel, renew — upgrade/downgrade folded into change plan), 07.01.02 (change plan only), 07.04.01 (process renewal only), 07.04.02 | Approved |
| [billing-payments](../../../02-requirements/FRD/billing-payments/requirement.md) | REQ-BIL-001 | 08.02.02 (generate, finalize invoice), 08.03.01, 08.03.02, 08.04.01 (invoice, receipt, download), 08.05.02 (format currency), 01.02.01 (view spending) | Draft |

### Progress (as of 2026-09-30)

| Feature | Status | Note |
|---|---|---|
| 07.01.01 Subscription Lifecycle (suspend, reactivate, cancel, renew) | Done (this FRD's scope) | Create/Activate already existed; upgrade/downgrade folded into Change plan below |
| 07.01.02 Subscription Changes (change plan) | Partly done | Change plan built; Change quantity, Schedule change not built — no quantity/scheduling model exists yet |
| 07.02 Entitlement Management | Not started | Needs a scoping decision against the existing product-access model first ([C38](../open-decisions.md#c38)) |
| 07.03 License & Quota Management | Not started | Same as above |
| 07.04.01 Renewal & Lifecycle (process renewal) | Partly done | Process renewal built (extends term); now also generates an invoice (REQ-BIL-001.2) when the plan is paid. Schedule/Notify/Auto-renew still not built |
| 07.04.02 Lifecycle (expire, reactivate subscription) | Done (this FRD's scope) | Hourly scheduled job; reactivate folded into renew |
| 08 Billing & Payments (08.02.02, 08.03.01, 08.03.02, 08.04.01, 08.05.02, 01.02.01) | Built (REQ-BIL-001, C46/C47) | Razorpay integration (gateway-not-configured mode by default), billing details, invoices, pay-invoice via Checkout, saved payment methods, refunds, reconcile, admin gateway-status screen, real spend on the business dashboard. Pricing (price books/promotions), usage billing, tax and credit notes remain Not covered — see the FRD's own Out of scope section |

07.02, 07.03, and the remaining 07.01.02/07.04.01 items remain open, carried to a later sprint once the decisions above are made. 08's own remaining open questions (tax, invoice terms, organization-billing permission shape, legal invoice fields) are recorded in [C47](../open-decisions.md#c47) and still block the FRD's own approval.

## EIS 07 Subscription & Entitlement Management

**Planned work ([PO] / [WB] description):** Subscriptions, licenses, quotas and entitlements

Full breakdown with APIs, services, entities and events: [applications/07-subscription-entitlement-management.md](../applications/07-subscription-entitlement-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [07.01 Subscription Management](../applications/07-subscription-entitlement-management.md#0701-subscription-management) | 07.01.01 Subscription Lifecycle | Create subscription; Activate subscription; Suspend subscription; Upgrade; Downgrade; Renew; Cancel | P0 | Commit |
| [07.01 Subscription Management](../applications/07-subscription-entitlement-management.md#0701-subscription-management) | 07.01.02 Subscription Changes | Change quantity; Change plan; Schedule change | P0 | Commit |
| [07.02 Entitlement Management](../applications/07-subscription-entitlement-management.md#0702-entitlement-management) | 07.02.01 Entitlements | Grant entitlement; Revoke entitlement; Validate entitlement; Check feature access | P0 | Commit |
| [07.02 Entitlement Management](../applications/07-subscription-entitlement-management.md#0702-entitlement-management) | 07.02.02 Quota | Check quota; Allocate quota; Adjust quota | P0 | Commit |
| [07.03 License & Quota Management](../applications/07-subscription-entitlement-management.md#0703-license--quota-management) | 07.03.01 Licensing | Issue license; Validate license; Expire license; Revoke license | P0 | Commit |
| [07.03 License & Quota Management](../applications/07-subscription-entitlement-management.md#0703-license--quota-management) | 07.03.02 Usage Limits | Define quota; Monitor quota; Enforce quota | P0 | Commit |
| [07.04 Renewal & Lifecycle](../applications/07-subscription-entitlement-management.md#0704-renewal--lifecycle) | 07.04.01 Renewals | Schedule renewal; Notify renewal; Auto-renew; Process renewal | P0 | Commit |
| [07.04 Renewal & Lifecycle](../applications/07-subscription-entitlement-management.md#0704-renewal--lifecycle) | 07.04.02 Lifecycle | Expire subscription; Reactivate subscription | P0 | Commit |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - EVT-009 SubscriptionCreated is consumed by Entitlement and Provisioning (applications 09; [WB:Events])
  - EVT-010 SubscriptionChanged is consumed by Billing and Entitlement (applications 08; [WB:Events])
  - UJ-006 Manage Subscription (Subscription, Pricing, Billing) (applications 08; [WB:User Journeys])

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). Deliverables are otherwise Not specified in any source.

### Related code already in this repository

Observed on branch `dev`, module level only: `backend/…/modules/registration (SubscriptionController, SubscriptionService, SubscriptionExpiryJob, product access)`; `frontend/src/pages/MyProductsPage.tsx`, `frontend/src/pages/MySubscriptionsPage.tsx`.

## EIS 08 Billing & Payments

**Planned work ([PO] / [WB] description):** Pricing, billing, invoices, taxes and payments

Full breakdown with APIs, services, entities and events: [applications/08-billing-payments.md](../applications/08-billing-payments.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [08.01 Pricing](../applications/08-billing-payments.md#0801-pricing) | 08.01.01 Price Books | Create price; Define tiers; Define volume pricing; Define customer pricing | P0 | Commit |
| [08.01 Pricing](../applications/08-billing-payments.md#0801-pricing) | 08.01.02 Promotions | Create discount; Create coupon; Apply promotion | P0 | Commit |
| [08.02 Billing](../applications/08-billing-payments.md#0802-billing) | 08.02.01 Usage Billing | Collect usage; Calculate charges; Apply discounts; Calculate taxes | P0 | Commit |
| [08.02 Billing](../applications/08-billing-payments.md#0802-billing) | 08.02.02 Invoice Generation | Generate invoice; Adjust invoice; Credit invoice; Finalize invoice | P0 | Commit |
| [08.03 Payment](../applications/08-billing-payments.md#0803-payment) | 08.03.01 Payment Methods | Add payment method; Remove payment method; Set default payment method | P0 | Commit |
| [08.03 Payment](../applications/08-billing-payments.md#0803-payment) | 08.03.02 Transactions | Authorize payment; Capture payment; Refund payment; Retry payment; Reconcile payment | P0 | Commit |
| [08.04 Financial Documents](../applications/08-billing-payments.md#0804-financial-documents) | 08.04.01 Documents | Generate invoice; Generate receipt; Generate credit note; Download document | P0 | Commit |
| [08.05 Tax & Currency](../applications/08-billing-payments.md#0805-tax--currency) | 08.05.01 Tax | Configure tax rules; Calculate tax; Validate tax | P0 | Commit |
| [08.05 Tax & Currency](../applications/08-billing-payments.md#0805-tax--currency) | 08.05.02 Currency | Configure currency; Convert currency; Format currency | P0 | Commit |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - EVT-007 PaymentAuthorized is consumed by Order and Billing (applications 07, 09; [WB:Events])
  - EVT-015 UsageRecorded is produced by Resource Service and consumed by Billing (applications 10; [WB:Events])
  - EVT-008 PaymentFailed is consumed by Notification and Support (applications 01, 12; [WB:Events])

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). Deliverables are otherwise Not specified in any source.

## Decisions affecting this sprint

- [C3](../open-decisions.md#c3) The [PO] sprint order is authoritative; the MVP is complete at the end of sprint 2027.1.3.
- [C4](../open-decisions.md#c4), [C5](../open-decisions.md#c5), [C6](../open-decisions.md#c6) MVP, priority and phase as shown above.
- [C10](../open-decisions.md#c10) Application 01 is named "Enterprise Intelligence Suite".
- Sprint goal, team, capacity and status are Not specified.

Details: [open-decisions.md](../open-decisions.md).

## Traceability

```
[PO] roadmap row → SPRINT-2026.4.3 → application page → capability → feature → function (requirement candidate)
   → FRD docs/02-requirements/FRD/<feature>/ → REQ-<CODE>-<NNN> (Approved before build)
   → STORY-<CODE>-<NNN> with "Sprint (PI.Sprint)" = 2026.4.3
   → code (backend/ · frontend/ · ai-service/) → TC-<CODE>-<NNN> in test-cases/ → UAT
```

Stories for this sprint use `docs/02-requirements/functional-requirements/user-story-template.md` or the GitHub **User Story** issue template, with **Sprint (PI.Sprint) = 2026.4.3**. The Definition of Done is the one in the story template.

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)
- **Decisions:** [`open-decisions.md`](../open-decisions.md) (2026-09-25)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source or decision gives are written **Not specified**.
