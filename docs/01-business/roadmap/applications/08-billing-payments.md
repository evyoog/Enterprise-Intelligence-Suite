# 08 Billing & Payments

| Field | Value |
|---|---|
| Application ID | 08 ([WB] numbering) |
| Application code | `APP-BIL` ([DN-5](../open-decisions.md#dn-5-application-codes)); IDs use `BIL`, for example `REQ-BIL-001` |
| Application | Billing & Payments |
| Description | Pricing, billing, invoices, taxes and payments ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2026.4 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2026.4.3](../sprints/SPRINT-2026.4.3.md) (1–31 Dec 2026, [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Capabilities / features / functions | 5 / 9 / 33 ([WB]) |
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
| [08.01](#0801-pricing) | Pricing | 08.01.01 Price Books, 08.01.02 Promotions | Yes | Phase 1 / MVP | P0 | C4 |
| [08.02](#0802-billing) | Billing | 08.02.01 Usage Billing, 08.02.02 Invoice Generation | Yes | Phase 1 / MVP | P0 | C4 |
| [08.03](#0803-payment) | Payment | 08.03.01 Payment Methods, 08.03.02 Transactions | Yes | Phase 1 / MVP | P0 | C4 |
| [08.04](#0804-financial-documents) | Financial Documents | 08.04.01 Documents | Yes | Phase 1 / MVP | P0 | C4 |
| [08.05](#0805-tax--currency) | Tax & Currency | 08.05.01 Tax, 08.05.02 Currency | Yes | Phase 1 / MVP | P0 | C4 |

## 08.01 Pricing

### Feature 08.01.01 Price Books

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 08.01.01.01 | Create price | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/pricing/create-price` | API-006 POST /v1/pricing/quote | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |
| 08.01.01.02 | Define tiers | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/pricing/define-tiers` | API-006 POST /v1/pricing/quote | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |
| 08.01.01.03 | Define volume pricing | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/pricing/define-volume-pricing` | API-006 POST /v1/pricing/quote | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |
| 08.01.01.04 | Define customer pricing | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/pricing/define-customer-pricing` | API-006 POST /v1/pricing/quote | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |

### Feature 08.01.02 Promotions

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 08.01.02.01 | Create discount | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/pricing/create-discount` | API-006 POST /v1/pricing/quote | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |
| 08.01.02.02 | Create coupon | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/pricing/create-coupon` | API-006 POST /v1/pricing/quote | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |
| 08.01.02.03 | Apply promotion | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/pricing/apply-promotion` | API-006 POST /v1/pricing/quote | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |

## 08.02 Billing

### Feature 08.02.01 Usage Billing

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 08.02.01.01 | Collect usage | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/billing/collect-usage` | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |
| 08.02.01.02 | Calculate charges | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/billing/calculate-charges` | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |
| 08.02.01.03 | Apply discounts | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/billing/apply-discounts` | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |
| 08.02.01.04 | Calculate taxes | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/billing/calculate-taxes` | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |

### Feature 08.02.02 Invoice Generation

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 08.02.02.01 | Generate invoice | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/billing/generate-invoice` | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |
| 08.02.02.02 | Adjust invoice | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/billing/adjust-invoice` | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |
| 08.02.02.03 | Credit invoice | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/billing/credit-invoice` | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |
| 08.02.02.04 | Finalize invoice | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/billing/finalize-invoice` | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |

## 08.03 Payment

### Feature 08.03.01 Payment Methods

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 08.03.01.01 | Add payment method | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/payment/add-payment-method` | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase |
| 08.03.01.02 | Remove payment method | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/payment/remove-payment-method` | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase |
| 08.03.01.03 | Set default payment method | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/payment/set-default-payment-method` | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase |

### Feature 08.03.02 Transactions

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 08.03.02.01 | Authorize payment | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/payment/authorize-payment` | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase |
| 08.03.02.02 | Capture payment | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/payment/capture-payment` | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase |
| 08.03.02.03 | Refund payment | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/payment/refund-payment` | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase |
| 08.03.02.04 | Retry payment | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/payment/retry-payment` | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase |
| 08.03.02.05 | Reconcile payment | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/payment/reconcile-payment` | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase |

## 08.04 Financial Documents

### Feature 08.04.01 Documents

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 08.04.01.01 | Generate invoice | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/financial-documents/generate-invoice` | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |
| 08.04.01.02 | Generate receipt | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/financial-documents/generate-receipt` | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |
| 08.04.01.03 | Generate credit note | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/financial-documents/generate-credit-note` | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |
| 08.04.01.04 | Download document | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/financial-documents/download-document` | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |

## 08.05 Tax & Currency

### Feature 08.05.01 Tax

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 08.05.01.01 | Configure tax rules | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/tax-currency/configure-tax-rules` | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |
| 08.05.01.02 | Calculate tax | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/tax-currency/calculate-tax` | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |
| 08.05.01.03 | Validate tax | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/tax-currency/validate-tax` | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |

### Feature 08.05.02 Currency

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 08.05.02.01 | Configure currency | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/tax-currency/configure-currency` | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |
| 08.05.02.02 | Convert currency | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/tax-currency/convert-currency` | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |
| 08.05.02.03 | Format currency | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/tax-currency/format-currency` | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service |

> **Columns from [WB:Traceability]** (Primary API, Microservice, Entity, Event, Journey) are kept for reference only. Under [C13](../open-decisions.md#c13) microservices are logical domains built as modules in the single backend. Under [C14](../open-decisions.md#c14) the implemented endpoints and each FRD's `api-requirements.md` are the source of truth for APIs.

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 07, 09 | EVT-007 PaymentAuthorized is consumed by Order and Billing | [WB:Events] |
| 10 | EVT-015 UsageRecorded is produced by Resource Service and consumed by Billing | [WB:Events] |
| 01, 12 | EVT-008 PaymentFailed is consumed by Notification and Support | [WB:Events] |

## Deliverables

Not specified in any source. Under [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates) the sprint commits this application's P0 capabilities; P1 capabilities are stretch scope.

## Requirements, design and tests

| Artifact | Location | Status |
|---|---|---|
| Feature FRD | `docs/02-requirements/FRD/<feature>/` (copy `_template/`) | See the sprint page for FRDs in progress |
| Requirement | `REQ-BIL-<NNN>` inside the FRD | Approved FRD required before build ([DN-4](../open-decisions.md#dn-4-business-rules-and-acceptance-criteria)) |
| Business rules | `docs/03-business-rules/` and `FRD/<feature>/business-rules.md` | Per FRD |
| Test cases | `test-cases/functional/<feature>/TC-BIL-<NNN>.md` | Not created |
| NFRs | [WB:Non-Functional Requirements] NFR-001 to NFR-014 (platform-wide) | See [EIS-document-analysis.md](../EIS-document-analysis.md) section 2.17 |
