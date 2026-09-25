# SPRINT-2026.4.3

| Field | Value |
|---|---|
| Sprint ID (PI.Sprint) | 2026.4.3 |
| PI – CY Quarter | 2026.4 |
| Start / end dates | Not specified |
| Sprint goal | Not specified |
| Team / capacity | Not specified |
| Status | Not specified |
| Source | [PO] roadmap tables ([`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)) |
| Previous / next sprint | [2026.4.2](SPRINT-2026.4.2.md) · [2027.1.1](SPRINT-2027.1.1.md) |

## Scope

| Product | Application ID | Application | Roadmap item(s) | Source |
|---|---|---|---|---|
| EIS (PaaS) | 07 | [Subscription & Entitlement Management](../applications/07-subscription-entitlement-management.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |
| EIS (PaaS) | 08 | [Billing & Payments](../applications/08-billing-payments.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |

> [PO] assigns **one sprint per EIS application**. It does not say which capabilities or features fall inside this sprint, or whether the application must be finished in it. The EIS scope below is the application's full [WB] breakdown until sprint scope is decided (see [open-decisions.md](../open-decisions.md)).

## EIS 07 Subscription & Entitlement Management

**Planned work ([PO] / [WB] description):** Subscriptions, licenses, quotas and entitlements

Full breakdown with APIs, services, entities and events: [applications/07-subscription-entitlement-management.md](../applications/07-subscription-entitlement-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | MVP functions |
|---|---|---|---|
| [07.01 Subscription Management](../applications/07-subscription-entitlement-management.md#0701-subscription-management) | 07.01.01 Subscription Lifecycle | Create subscription; Activate subscription; Suspend subscription; Upgrade; Downgrade; Renew; Cancel | 07.01.01.01, 07.01.01.02, 07.01.01.03 |
| [07.01 Subscription Management](../applications/07-subscription-entitlement-management.md#0701-subscription-management) | 07.01.02 Subscription Changes | Change quantity; Change plan; Schedule change | 07.01.02.01, 07.01.02.02, 07.01.02.03 |
| [07.02 Entitlement Management](../applications/07-subscription-entitlement-management.md#0702-entitlement-management) | 07.02.01 Entitlements | Grant entitlement; Revoke entitlement; Validate entitlement; Check feature access | - |
| [07.02 Entitlement Management](../applications/07-subscription-entitlement-management.md#0702-entitlement-management) | 07.02.02 Quota | Check quota; Allocate quota; Adjust quota | - |
| [07.03 License & Quota Management](../applications/07-subscription-entitlement-management.md#0703-license--quota-management) | 07.03.01 Licensing | Issue license; Validate license; Expire license; Revoke license | - |
| [07.03 License & Quota Management](../applications/07-subscription-entitlement-management.md#0703-license--quota-management) | 07.03.02 Usage Limits | Define quota; Monitor quota; Enforce quota | - |
| [07.04 Renewal & Lifecycle](../applications/07-subscription-entitlement-management.md#0704-renewal--lifecycle) | 07.04.01 Renewals | Schedule renewal; Notify renewal; Auto-renew; Process renewal | - |
| [07.04 Renewal & Lifecycle](../applications/07-subscription-entitlement-management.md#0704-renewal--lifecycle) | 07.04.02 Lifecycle | Expire subscription; Reactivate subscription | - |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - EVT-009 SubscriptionCreated is consumed by Entitlement and Provisioning (applications 09; [WB:Events])
  - EVT-010 SubscriptionChanged is consumed by Billing and Entitlement (applications 08; [WB:Events])
  - UJ-006 Manage Subscription (Subscription, Pricing, Billing) (applications 08; [WB:User Journeys])

### Expected deliverables

- Not specified in any source.
- Implied by [WB:Traceability]: APIs `API-011 POST /v1/subscriptions`, `API-012 POST /v1/entitlements/check`; services Subscription Service.

### Related code already in this repository

Observed on branch `dev`, module level only: `backend/…/modules/registration (SubscriptionController, product access)`; `frontend/src/pages/MyProductsPage.tsx`.

## EIS 08 Billing & Payments

**Planned work ([PO] / [WB] description):** Pricing, billing, invoices, taxes and payments

Full breakdown with APIs, services, entities and events: [applications/08-billing-payments.md](../applications/08-billing-payments.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | MVP functions |
|---|---|---|---|
| [08.01 Pricing](../applications/08-billing-payments.md#0801-pricing) | 08.01.01 Price Books | Create price; Define tiers; Define volume pricing; Define customer pricing | - |
| [08.01 Pricing](../applications/08-billing-payments.md#0801-pricing) | 08.01.02 Promotions | Create discount; Create coupon; Apply promotion | - |
| [08.02 Billing](../applications/08-billing-payments.md#0802-billing) | 08.02.01 Usage Billing | Collect usage; Calculate charges; Apply discounts; Calculate taxes | - |
| [08.02 Billing](../applications/08-billing-payments.md#0802-billing) | 08.02.02 Invoice Generation | Generate invoice; Adjust invoice; Credit invoice; Finalize invoice | - |
| [08.03 Payment](../applications/08-billing-payments.md#0803-payment) | 08.03.01 Payment Methods | Add payment method; Remove payment method; Set default payment method | - |
| [08.03 Payment](../applications/08-billing-payments.md#0803-payment) | 08.03.02 Transactions | Authorize payment; Capture payment; Refund payment; Retry payment; Reconcile payment | - |
| [08.04 Financial Documents](../applications/08-billing-payments.md#0804-financial-documents) | 08.04.01 Documents | Generate invoice; Generate receipt; Generate credit note; Download document | - |
| [08.05 Tax & Currency](../applications/08-billing-payments.md#0805-tax--currency) | 08.05.01 Tax | Configure tax rules; Calculate tax; Validate tax | - |
| [08.05 Tax & Currency](../applications/08-billing-payments.md#0805-tax--currency) | 08.05.02 Currency | Configure currency; Convert currency; Format currency | - |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - EVT-007 PaymentAuthorized is consumed by Order and Billing (applications 07, 09; [WB:Events])
  - EVT-015 UsageRecorded is produced by Resource Service and consumed by Billing (applications 10; [WB:Events])
  - EVT-008 PaymentFailed is consumed by Notification and Support (applications 01, 12; [WB:Events])

### Expected deliverables

- Not specified in any source.
- Implied by [WB:Traceability]: APIs `API-006 POST /v1/pricing/quote`, `API-013 POST /v1/billing/invoices`, `API-014 POST /v1/payments`; services Pricing Service.

## Open issues affecting this sprint

- None specific to this sprint. The general decisions in open-decisions.md still apply
- The sprint scope, deliverables, acceptance criteria and dates are not specified in any source.

Details: [open-decisions.md](../open-decisions.md).

## Traceability

```
[PO] roadmap row → SPRINT-2026.4.3 → application page → capability → feature → function (requirement candidate)
   → FRD docs/02-requirements/FRD/<feature>/ (not created) → REQ-<APP-CODE>-<NNN> (not created)
   → STORY-<APP-CODE>-<NNN> with "Sprint (PI.Sprint)" = 2026.4.3
   → code (backend/ · frontend/ · ai-service/) → TC-<APP-CODE>-<NNN> in test-cases/ → UAT
```

Stories for this sprint use `docs/02-requirements/functional-requirements/user-story-template.md` or the GitHub **User Story** issue template, with **Sprint (PI.Sprint) = 2026.4.3**. The Definition of Done is the one in the story template.

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.
