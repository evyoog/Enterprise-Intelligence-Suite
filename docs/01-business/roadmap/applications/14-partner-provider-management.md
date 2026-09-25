# 14 Partner & Provider Management

| Field | Value |
|---|---|
| Application ID | 14 ([WB] numbering) |
| Application code | `APP-PTR` ([DN-5](../open-decisions.md#dn-5-application-codes)); IDs use `PTR`, for example `REQ-PTR-001` |
| Application | Partner & Provider Management |
| Description | Providers, publishers, onboarding and revenue sharing ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2027.2 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2027.2.1](../sprints/SPRINT-2027.2.1.md) (1–30 Apr 2027, [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Capabilities / features / functions | 4 / 7 / 23 ([WB]) |
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
| [14.01](#1401-provider-onboarding) | Provider Onboarding | 14.01.01 Provider Lifecycle, 14.01.02 Contracts | No | Phase 2 | P1 | [WB:Roadmap] "Partner Ecosystem" |
| [14.02](#1402-publisher-management) | Publisher Management | 14.02.01 Publisher Catalog, 14.02.02 Publisher Analytics | No | Phase 2 | P1 | [WB:Roadmap] "Partner Ecosystem" |
| [14.03](#1403-revenue-sharing) | Revenue Sharing | 14.03.01 Commission, 14.03.02 Payouts | No | Phase 3 | P1 | [WB:Roadmap] "Marketplace Ecosystem" |
| [14.04](#1404-partner-operations) | Partner Operations | 14.04.01 Partner Support | No | Not specified | Not specified | Not in [WB:Roadmap] |

## 14.01 Provider Onboarding

### Feature 14.01.01 Provider Lifecycle

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 14.01.01.01 | Register provider | No | Phase 2 | P1 | No | Platform Service | `/provider-onboarding/register-provider` | API-020 POST /v1/providers | Partner Service | Partner | - | UJ-005 Provision Service |
| 14.01.01.02 | Verify provider | No | Phase 2 | P1 | No | Platform Service | `/provider-onboarding/verify-provider` | API-020 POST /v1/providers | Partner Service | Partner | - | UJ-005 Provision Service |
| 14.01.01.03 | Approve provider | No | Phase 2 | P1 | No | Platform Service | `/provider-onboarding/approve-provider` | API-020 POST /v1/providers | Partner Service | Partner | - | UJ-005 Provision Service |
| 14.01.01.04 | Activate provider | No | Phase 2 | P1 | No | Platform Service | `/provider-onboarding/activate-provider` | API-020 POST /v1/providers | Partner Service | Partner | - | UJ-005 Provision Service |

### Feature 14.01.02 Contracts

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 14.01.02.01 | Create contract | No | Phase 2 | P1 | No | Platform Service | `/provider-onboarding/create-contract` | API-020 POST /v1/providers | Partner Service | Partner | - | UJ-005 Provision Service |
| 14.01.02.02 | Manage terms | No | Phase 2 | P1 | No | Platform Service | `/provider-onboarding/manage-terms` | API-020 POST /v1/providers | Partner Service | Partner | - | UJ-005 Provision Service |
| 14.01.02.03 | Track expiration | No | Phase 2 | P1 | No | Platform Service | `/provider-onboarding/track-expiration` | API-020 POST /v1/providers | Partner Service | Partner | - | UJ-005 Provision Service |

## 14.02 Publisher Management

### Feature 14.02.01 Publisher Catalog

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 14.02.01.01 | Create publisher product | No | Phase 2 | P1 | No | Platform Service | `/publisher-management/create-publisher-product` | - | Partner Service | Partner | - | UJ-005 Provision Service |
| 14.02.01.02 | Manage pricing | No | Phase 2 | P1 | No | Platform Service | `/publisher-management/manage-pricing` | - | Partner Service | Partner | - | UJ-005 Provision Service |
| 14.02.01.03 | Manage content | No | Phase 2 | P1 | No | Platform Service | `/publisher-management/manage-content` | - | Partner Service | Partner | - | UJ-005 Provision Service |
| 14.02.01.04 | Publish product | No | Phase 2 | P1 | No | Platform Service | `/publisher-management/publish-product` | - | Partner Service | Partner | - | UJ-005 Provision Service |

### Feature 14.02.02 Publisher Analytics

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 14.02.02.01 | View product performance | No | Phase 2 | P1 | No | Platform Service | `/publisher-management/view-product-performance` | - | Partner Service | Partner | - | UJ-005 Provision Service |
| 14.02.02.02 | View sales | No | Phase 2 | P1 | No | Platform Service | `/publisher-management/view-sales` | - | Partner Service | Partner | - | UJ-005 Provision Service |
| 14.02.02.03 | View usage | No | Phase 2 | P1 | No | Platform Service | `/publisher-management/view-usage` | - | Partner Service | Partner | - | UJ-005 Provision Service |

## 14.03 Revenue Sharing

### Feature 14.03.01 Commission

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 14.03.01.01 | Define commission | No | Phase 3 | P1 | No | Platform Service | `/revenue-sharing/define-commission` | - | Partner Service | Partner | - | UJ-005 Provision Service |
| 14.03.01.02 | Calculate revenue share | No | Phase 3 | P1 | No | Platform Service | `/revenue-sharing/calculate-revenue-share` | - | Partner Service | Partner | - | UJ-005 Provision Service |
| 14.03.01.03 | Generate statement | No | Phase 3 | P1 | No | Platform Service | `/revenue-sharing/generate-statement` | - | Partner Service | Partner | - | UJ-005 Provision Service |

### Feature 14.03.02 Payouts

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 14.03.02.01 | Calculate payout | No | Phase 3 | P1 | No | Platform Service | `/revenue-sharing/calculate-payout` | - | Partner Service | Partner | - | UJ-005 Provision Service |
| 14.03.02.02 | Approve payout | No | Phase 3 | P1 | No | Platform Service | `/revenue-sharing/approve-payout` | - | Partner Service | Partner | - | UJ-005 Provision Service |
| 14.03.02.03 | Reconcile payout | No | Phase 3 | P1 | No | Platform Service | `/revenue-sharing/reconcile-payout` | - | Partner Service | Partner | - | UJ-005 Provision Service |

## 14.04 Partner Operations

### Feature 14.04.01 Partner Support

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: Yes.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 14.04.01.01 | Create partner ticket | No | Not specified | Not specified | No | Platform Service | `/partner-operations/create-partner-ticket` | - | Partner Service | Partner | - | UJ-005 Provision Service |
| 14.04.01.02 | Assign partner manager | No | Not specified | Not specified | No | Platform Service | `/partner-operations/assign-partner-manager` | - | Partner Service | Partner | - | UJ-005 Provision Service |
| 14.04.01.03 | Track partner SLA | No | Not specified | Not specified | No | Platform Service | `/partner-operations/track-partner-sla` | - | Partner Service | Partner | - | UJ-005 Provision Service |

> **Columns from [WB:Traceability]** (Primary API, Microservice, Entity, Event, Journey) are kept for reference only. Under [C13](../open-decisions.md#c13) microservices are logical domains built as modules in the single backend. Under [C14](../open-decisions.md#c14) the implemented endpoints and each FRD's `api-requirements.md` are the source of truth for APIs.

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 02, 03 | UJ-010 Provider Product Publishing (Partner, Catalog, Marketplace) | [WB:User Journeys] |
| 08 | DE-021 Partner relates to Contract and Payout | [WB:Data Entities] |

## Deliverables

Not specified in any source. Under [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates) the sprint commits this application's P0 capabilities; P1 capabilities are stretch scope.

## Requirements, design and tests

| Artifact | Location | Status |
|---|---|---|
| Feature FRD | `docs/02-requirements/FRD/<feature>/` (copy `_template/`) | See the sprint page for FRDs in progress |
| Requirement | `REQ-PTR-<NNN>` inside the FRD | Approved FRD required before build ([DN-4](../open-decisions.md#dn-4-business-rules-and-acceptance-criteria)) |
| Business rules | `docs/03-business-rules/` and `FRD/<feature>/business-rules.md` | Per FRD |
| Test cases | `test-cases/functional/<feature>/TC-PTR-<NNN>.md` | Not created |
| NFRs | [WB:Non-Functional Requirements] NFR-001 to NFR-014 (platform-wide) | See [EIS-document-analysis.md](../EIS-document-analysis.md) section 2.17 |
