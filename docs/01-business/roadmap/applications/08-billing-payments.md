# 08 Billing & Payments

| Field | Value |
|---|---|
| Application ID | 08 ([WB] numbering; an `APP-<CODE>` code is not assigned in any source) |
| Application | Billing & Payments |
| Description | Pricing, billing, invoices, taxes and payments ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2026.4 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2026.4.3](../sprints/SPRINT-2026.4.3.md) |
| Capabilities / features / functions | 5 / 9 / 33 ([WB]) |
| Application status | Not specified |

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.

## Capabilities

| Capability ID | Capability | Features | Priority | MVP | AI relevant |
|---|---|---|---|---|---|
| [08.01](#0801-pricing) | Pricing | 08.01.01 Price Books, 08.01.02 Promotions | P0 | Yes | No |
| [08.02](#0802-billing) | Billing | 08.02.01 Usage Billing, 08.02.02 Invoice Generation | P0 | Yes | No |
| [08.03](#0803-payment) | Payment | 08.03.01 Payment Methods, 08.03.02 Transactions | P0 | Yes | No |
| [08.04](#0804-financial-documents) | Financial Documents | 08.04.01 Documents | P0 | Yes | No |
| [08.05](#0805-tax--currency) | Tax & Currency | 08.05.01 Tax, 08.05.02 Currency | P0 | Yes | No |

> The Priority and MVP values are copied from [WB:Capabilities]. Every capability in [WB] is P0 / MVP=Yes, which conflicts with the function-level MVP flags (C4 in [open-decisions.md](../open-decisions.md)).

## 08.01 Pricing

### Feature 08.01.01 Price Books

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 08.01.01.01 | Create price | No | No | Platform Service | `/pricing/create-price` | API-006 POST /v1/pricing/quote | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.01.01.02 | Define tiers | No | No | Platform Service | `/pricing/define-tiers` | API-006 POST /v1/pricing/quote | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.01.01.03 | Define volume pricing | No | No | Platform Service | `/pricing/define-volume-pricing` | API-006 POST /v1/pricing/quote | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.01.01.04 | Define customer pricing | No | No | Platform Service | `/pricing/define-customer-pricing` | API-006 POST /v1/pricing/quote | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |

### Feature 08.01.02 Promotions

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 08.01.02.01 | Create discount | No | No | Platform Service | `/pricing/create-discount` | API-006 POST /v1/pricing/quote | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.01.02.02 | Create coupon | No | No | Platform Service | `/pricing/create-coupon` | API-006 POST /v1/pricing/quote | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.01.02.03 | Apply promotion | No | No | Platform Service | `/pricing/apply-promotion` | API-006 POST /v1/pricing/quote | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |

## 08.02 Billing

### Feature 08.02.01 Usage Billing

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 08.02.01.01 | Collect usage | No | No | Platform Service | `/billing/collect-usage` | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.02.01.02 | Calculate charges | No | No | Platform Service | `/billing/calculate-charges` | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.02.01.03 | Apply discounts | No | No | Platform Service | `/billing/apply-discounts` | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.02.01.04 | Calculate taxes | No | No | Platform Service | `/billing/calculate-taxes` | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |

### Feature 08.02.02 Invoice Generation

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 08.02.02.01 | Generate invoice | No | No | Platform Service | `/billing/generate-invoice` | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.02.02.02 | Adjust invoice | No | No | Platform Service | `/billing/adjust-invoice` | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.02.02.03 | Credit invoice | No | No | Platform Service | `/billing/credit-invoice` | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.02.02.04 | Finalize invoice | No | No | Platform Service | `/billing/finalize-invoice` | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |

## 08.03 Payment

### Feature 08.03.01 Payment Methods

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 08.03.01.01 | Add payment method | No | No | Platform Service | `/payment/add-payment-method` | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase | Phase 2 |
| 08.03.01.02 | Remove payment method | No | No | Platform Service | `/payment/remove-payment-method` | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase | Phase 2 |
| 08.03.01.03 | Set default payment method | No | No | Platform Service | `/payment/set-default-payment-method` | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase | Phase 2 |

### Feature 08.03.02 Transactions

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 08.03.02.01 | Authorize payment | No | No | Platform Service | `/payment/authorize-payment` | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase | Phase 2 |
| 08.03.02.02 | Capture payment | No | No | Platform Service | `/payment/capture-payment` | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase | Phase 2 |
| 08.03.02.03 | Refund payment | No | No | Platform Service | `/payment/refund-payment` | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase | Phase 2 |
| 08.03.02.04 | Retry payment | No | No | Platform Service | `/payment/retry-payment` | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase | Phase 2 |
| 08.03.02.05 | Reconcile payment | No | No | Platform Service | `/payment/reconcile-payment` | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase | Phase 2 |

## 08.04 Financial Documents

### Feature 08.04.01 Documents

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 08.04.01.01 | Generate invoice | No | No | Platform Service | `/financial-documents/generate-invoice` | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.04.01.02 | Generate receipt | No | No | Platform Service | `/financial-documents/generate-receipt` | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.04.01.03 | Generate credit note | No | No | Platform Service | `/financial-documents/generate-credit-note` | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.04.01.04 | Download document | No | No | Platform Service | `/financial-documents/download-document` | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |

## 08.05 Tax & Currency

### Feature 08.05.01 Tax

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 08.05.01.01 | Configure tax rules | No | No | Platform Service | `/tax-currency/configure-tax-rules` | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.05.01.02 | Calculate tax | No | No | Platform Service | `/tax-currency/calculate-tax` | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.05.01.03 | Validate tax | No | No | Platform Service | `/tax-currency/validate-tax` | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |

### Feature 08.05.02 Currency

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 08.05.02.01 | Configure currency | No | No | Platform Service | `/tax-currency/configure-currency` | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.05.02.02 | Convert currency | No | No | Platform Service | `/tax-currency/convert-currency` | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.05.02.03 | Format currency | No | No | Platform Service | `/tax-currency/format-currency` | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 07, 09 | EVT-007 PaymentAuthorized is consumed by Order and Billing | [WB:Events] |
| 10 | EVT-015 UsageRecorded is produced by Resource Service and consumed by Billing | [WB:Events] |
| 01, 12 | EVT-008 PaymentFailed is consumed by Notification and Support | [WB:Events] |

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
