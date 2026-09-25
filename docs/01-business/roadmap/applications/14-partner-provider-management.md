# 14 Partner & Provider Management

| Field | Value |
|---|---|
| Application ID | 14 ([WB] numbering; an `APP-<CODE>` code is not assigned in any source) |
| Application | Partner & Provider Management |
| Description | Providers, publishers, onboarding and revenue sharing ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2027.2 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2027.2.1](../sprints/SPRINT-2027.2.1.md) |
| Capabilities / features / functions | 4 / 7 / 23 ([WB]) |
| Application status | Not specified |

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.

## Capabilities

| Capability ID | Capability | Features | Priority | MVP | AI relevant |
|---|---|---|---|---|---|
| [14.01](#1401-provider-onboarding) | Provider Onboarding | 14.01.01 Provider Lifecycle, 14.01.02 Contracts | P0 | Yes | No |
| [14.02](#1402-publisher-management) | Publisher Management | 14.02.01 Publisher Catalog, 14.02.02 Publisher Analytics | P0 | Yes | No |
| [14.03](#1403-revenue-sharing) | Revenue Sharing | 14.03.01 Commission, 14.03.02 Payouts | P0 | Yes | No |
| [14.04](#1404-partner-operations) | Partner Operations | 14.04.01 Partner Support | P0 | Yes | No |

> The Priority and MVP values are copied from [WB:Capabilities]. Every capability in [WB] is P0 / MVP=Yes, which conflicts with the function-level MVP flags (C4 in [open-decisions.md](../open-decisions.md)).

## 14.01 Provider Onboarding

### Feature 14.01.01 Provider Lifecycle

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 14.01.01.01 | Register provider | No | No | Platform Service | `/provider-onboarding/register-provider` | API-020 POST /v1/providers | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.01.01.02 | Verify provider | No | No | Platform Service | `/provider-onboarding/verify-provider` | API-020 POST /v1/providers | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.01.01.03 | Approve provider | No | No | Platform Service | `/provider-onboarding/approve-provider` | API-020 POST /v1/providers | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.01.01.04 | Activate provider | No | No | Platform Service | `/provider-onboarding/activate-provider` | API-020 POST /v1/providers | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |

### Feature 14.01.02 Contracts

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 14.01.02.01 | Create contract | No | No | Platform Service | `/provider-onboarding/create-contract` | API-020 POST /v1/providers | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.01.02.02 | Manage terms | No | No | Platform Service | `/provider-onboarding/manage-terms` | API-020 POST /v1/providers | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.01.02.03 | Track expiration | No | No | Platform Service | `/provider-onboarding/track-expiration` | API-020 POST /v1/providers | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |

## 14.02 Publisher Management

### Feature 14.02.01 Publisher Catalog

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 14.02.01.01 | Create publisher product | No | No | Platform Service | `/publisher-management/create-publisher-product` | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.02.01.02 | Manage pricing | No | No | Platform Service | `/publisher-management/manage-pricing` | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.02.01.03 | Manage content | No | No | Platform Service | `/publisher-management/manage-content` | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.02.01.04 | Publish product | No | No | Platform Service | `/publisher-management/publish-product` | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |

### Feature 14.02.02 Publisher Analytics

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 14.02.02.01 | View product performance | No | No | Platform Service | `/publisher-management/view-product-performance` | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.02.02.02 | View sales | No | No | Platform Service | `/publisher-management/view-sales` | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.02.02.03 | View usage | No | No | Platform Service | `/publisher-management/view-usage` | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |

## 14.03 Revenue Sharing

### Feature 14.03.01 Commission

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 14.03.01.01 | Define commission | No | No | Platform Service | `/revenue-sharing/define-commission` | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.03.01.02 | Calculate revenue share | No | No | Platform Service | `/revenue-sharing/calculate-revenue-share` | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.03.01.03 | Generate statement | No | No | Platform Service | `/revenue-sharing/generate-statement` | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |

### Feature 14.03.02 Payouts

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 14.03.02.01 | Calculate payout | No | No | Platform Service | `/revenue-sharing/calculate-payout` | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.03.02.02 | Approve payout | No | No | Platform Service | `/revenue-sharing/approve-payout` | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.03.02.03 | Reconcile payout | No | No | Platform Service | `/revenue-sharing/reconcile-payout` | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |

## 14.04 Partner Operations

### Feature 14.04.01 Partner Support

Priority P1 · MVP Yes · AI required Yes ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 14.04.01.01 | Create partner ticket | No | No | Platform Service | `/partner-operations/create-partner-ticket` | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.04.01.02 | Assign partner manager | No | No | Platform Service | `/partner-operations/assign-partner-manager` | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.04.01.03 | Track partner SLA | No | No | Platform Service | `/partner-operations/track-partner-sla` | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 02, 03 | UJ-010 Provider Product Publishing (Partner, Catalog, Marketplace) | [WB:User Journeys] |
| 08 | DE-021 Partner relates to Contract and Payout | [WB:Data Entities] |

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
