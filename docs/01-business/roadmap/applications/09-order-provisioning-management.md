# 09 Order & Provisioning Management

| Field | Value |
|---|---|
| Application ID | 09 ([WB] numbering) |
| Application code | `APP-ORD` ([DN-5](../open-decisions.md#dn-5-application-codes)); IDs use `ORD`, for example `REQ-ORD-001` |
| Application | Order & Provisioning Management |
| Description | Orders, provisioning and workflow orchestration ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2027.1 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2027.1.1](../sprints/SPRINT-2027.1.1.md) (1–31 Jan 2027, [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Capabilities / features / functions | 4 / 5 / 26 ([WB]) |
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
| [09.01](#0901-order-management) | Order Management | 09.01.01 Order Lifecycle | Yes | Phase 1 / MVP | P0 | C4 |
| [09.02](#0902-provisioning) | Provisioning | 09.02.01 Service Provisioning | Yes | Phase 1 / MVP | P0 | C4 |
| [09.03](#0903-workflow-orchestration) | Workflow Orchestration | 09.03.01 Workflow Runtime, 09.03.02 Workflow Design | Yes | Phase 1 / MVP | P0 | C4 |
| [09.04](#0904-approval-management) | Approval Management | 09.04.01 Approvals | Yes | Phase 1 / MVP | P0 | C4 |

## 09.01 Order Management

### Feature 09.01.01 Order Lifecycle

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 09.01.01.01 | Create order | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/order-management/create-order` | API-009 POST /v1/orders | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 09.01.01.02 | Validate order | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/order-management/validate-order` | API-009 POST /v1/orders | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 09.01.01.03 | Price order | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/order-management/price-order` | API-009 POST /v1/orders | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 09.01.01.04 | Submit order | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/order-management/submit-order` | API-009 POST /v1/orders | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 09.01.01.05 | Approve order | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/order-management/approve-order` | API-009 POST /v1/orders | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 09.01.01.06 | Cancel order | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/order-management/cancel-order` | API-009 POST /v1/orders | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 09.01.01.07 | Track order | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/order-management/track-order` | API-009 POST /v1/orders | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |

## 09.02 Provisioning

### Feature 09.02.01 Service Provisioning

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 09.02.01.01 | Provision service | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/provisioning/provision-service` | API-010 POST /v1/provisioning | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 09.02.01.02 | Configure service | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/provisioning/configure-service` | API-010 POST /v1/provisioning | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 09.02.01.03 | Activate service | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/provisioning/activate-service` | API-010 POST /v1/provisioning | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 09.02.01.04 | Suspend service | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/provisioning/suspend-service` | API-010 POST /v1/provisioning | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 09.02.01.05 | Deprovision service | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/provisioning/deprovision-service` | API-010 POST /v1/provisioning | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |

## 09.03 Workflow Orchestration

### Feature 09.03.01 Workflow Runtime

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 09.03.01.01 | Trigger workflow | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/workflow-orchestration/trigger-workflow` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 09.03.01.02 | Execute workflow | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/workflow-orchestration/execute-workflow` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 09.03.01.03 | Retry step | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/workflow-orchestration/retry-step` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 09.03.01.04 | Rollback | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/workflow-orchestration/rollback` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 09.03.01.05 | Compensate | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/workflow-orchestration/compensate` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 09.03.01.06 | Escalate | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/workflow-orchestration/escalate` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |

### Feature 09.03.02 Workflow Design

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 09.03.02.01 | Define workflow | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/workflow-orchestration/define-workflow` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 09.03.02.02 | Configure step | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/workflow-orchestration/configure-step` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 09.03.02.03 | Set dependency | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/workflow-orchestration/set-dependency` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |

## 09.04 Approval Management

### Feature 09.04.01 Approvals

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 09.04.01.01 | Create approval | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/approval-management/create-approval` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 09.04.01.02 | Route approval | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/approval-management/route-approval` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 09.04.01.03 | Approve | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/approval-management/approve` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 09.04.01.04 | Reject | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/approval-management/reject` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 09.04.01.05 | Escalate | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/approval-management/escalate` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service |

> **Columns from [WB:Traceability]** (Primary API, Microservice, Entity, Event, Journey) are kept for reference only. Under [C13](../open-decisions.md#c13) microservices are logical domains built as modules in the single backend. Under [C14](../open-decisions.md#c14) the implemented endpoints and each FRD's `api-requirements.md` are the source of truth for APIs.

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 03 | EVT-005 CheckoutCompleted is consumed by Order | [WB:Events] |
| 07 | UJ-005 Provision Service: Order accepted → Entitlement → Provision → Activate → Notify | [WB:User Journeys] |
| 10 | EVT-012 ProvisioningStarted is consumed by Resource Service | [WB:Events] |

## Deliverables

Not specified in any source. Under [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates) the sprint commits this application's P0 capabilities; P1 capabilities are stretch scope.

## Requirements, design and tests

| Artifact | Location | Status |
|---|---|---|
| Feature FRD | `docs/02-requirements/FRD/<feature>/` (copy `_template/`) | See the sprint page for FRDs in progress |
| Requirement | `REQ-ORD-<NNN>` inside the FRD | Approved FRD required before build ([DN-4](../open-decisions.md#dn-4-business-rules-and-acceptance-criteria)) |
| Business rules | `docs/03-business-rules/` and `FRD/<feature>/business-rules.md` | Per FRD |
| Test cases | `test-cases/functional/<feature>/TC-ORD-<NNN>.md` | Not created |
| NFRs | [WB:Non-Functional Requirements] NFR-001 to NFR-014 (platform-wide) | See [EIS-document-analysis.md](../EIS-document-analysis.md) section 2.17 |
