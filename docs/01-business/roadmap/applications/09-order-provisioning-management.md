# 09 Order & Provisioning Management

| Field | Value |
|---|---|
| Application ID | 09 ([WB] numbering; an `APP-<CODE>` code is not assigned in any source) |
| Application | Order & Provisioning Management |
| Description | Orders, provisioning and workflow orchestration ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2027.1 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2027.1.1](../sprints/SPRINT-2027.1.1.md) |
| Capabilities / features / functions | 4 / 5 / 26 ([WB]) |
| Application status | Not specified |

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.

## Capabilities

| Capability ID | Capability | Features | Priority | MVP | AI relevant |
|---|---|---|---|---|---|
| [09.01](#0901-order-management) | Order Management | 09.01.01 Order Lifecycle | P0 | Yes | No |
| [09.02](#0902-provisioning) | Provisioning | 09.02.01 Service Provisioning | P0 | Yes | No |
| [09.03](#0903-workflow-orchestration) | Workflow Orchestration | 09.03.01 Workflow Runtime, 09.03.02 Workflow Design | P0 | Yes | No |
| [09.04](#0904-approval-management) | Approval Management | 09.04.01 Approvals | P0 | Yes | No |

> The Priority and MVP values are copied from [WB:Capabilities]. Every capability in [WB] is P0 / MVP=Yes, which conflicts with the function-level MVP flags (C4 in [open-decisions.md](../open-decisions.md)).

## 09.01 Order Management

### Feature 09.01.01 Order Lifecycle

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 09.01.01.01 | Create order | Yes | No | Platform Service | `/order-management/create-order` | API-009 POST /v1/orders | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 1 / MVP |
| 09.01.01.02 | Validate order | Yes | No | Platform Service | `/order-management/validate-order` | API-009 POST /v1/orders | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 1 / MVP |
| 09.01.01.03 | Price order | Yes | No | Platform Service | `/order-management/price-order` | API-009 POST /v1/orders | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 1 / MVP |
| 09.01.01.04 | Submit order | No | No | Platform Service | `/order-management/submit-order` | API-009 POST /v1/orders | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.01.01.05 | Approve order | No | No | Platform Service | `/order-management/approve-order` | API-009 POST /v1/orders | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.01.01.06 | Cancel order | No | No | Platform Service | `/order-management/cancel-order` | API-009 POST /v1/orders | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.01.01.07 | Track order | No | No | Platform Service | `/order-management/track-order` | API-009 POST /v1/orders | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |

## 09.02 Provisioning

### Feature 09.02.01 Service Provisioning

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 09.02.01.01 | Provision service | No | No | Platform Service | `/provisioning/provision-service` | API-010 POST /v1/provisioning | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.02.01.02 | Configure service | No | No | Platform Service | `/provisioning/configure-service` | API-010 POST /v1/provisioning | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.02.01.03 | Activate service | No | No | Platform Service | `/provisioning/activate-service` | API-010 POST /v1/provisioning | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.02.01.04 | Suspend service | No | No | Platform Service | `/provisioning/suspend-service` | API-010 POST /v1/provisioning | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.02.01.05 | Deprovision service | No | No | Platform Service | `/provisioning/deprovision-service` | API-010 POST /v1/provisioning | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |

## 09.03 Workflow Orchestration

### Feature 09.03.01 Workflow Runtime

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 09.03.01.01 | Trigger workflow | No | No | Platform Service | `/workflow-orchestration/trigger-workflow` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.03.01.02 | Execute workflow | No | No | Platform Service | `/workflow-orchestration/execute-workflow` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.03.01.03 | Retry step | No | No | Platform Service | `/workflow-orchestration/retry-step` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.03.01.04 | Rollback | No | No | Platform Service | `/workflow-orchestration/rollback` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.03.01.05 | Compensate | No | No | Platform Service | `/workflow-orchestration/compensate` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.03.01.06 | Escalate | No | No | Platform Service | `/workflow-orchestration/escalate` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |

### Feature 09.03.02 Workflow Design

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 09.03.02.01 | Define workflow | No | No | Platform Service | `/workflow-orchestration/define-workflow` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.03.02.02 | Configure step | No | No | Platform Service | `/workflow-orchestration/configure-step` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.03.02.03 | Set dependency | No | No | Platform Service | `/workflow-orchestration/set-dependency` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |

## 09.04 Approval Management

### Feature 09.04.01 Approvals

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 09.04.01.01 | Create approval | No | No | Platform Service | `/approval-management/create-approval` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.04.01.02 | Route approval | No | No | Platform Service | `/approval-management/route-approval` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.04.01.03 | Approve | No | No | Platform Service | `/approval-management/approve` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.04.01.04 | Reject | No | No | Platform Service | `/approval-management/reject` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.04.01.05 | Escalate | No | No | Platform Service | `/approval-management/escalate` | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 03 | EVT-005 CheckoutCompleted is consumed by Order | [WB:Events] |
| 07 | UJ-005 Provision Service: Order accepted → Entitlement → Provision → Activate → Notify | [WB:User Journeys] |
| 10 | EVT-012 ProvisioningStarted is consumed by Resource Service | [WB:Events] |

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
