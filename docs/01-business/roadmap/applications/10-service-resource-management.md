# 10 Service & Resource Management

| Field | Value |
|---|---|
| Application ID | 10 ([WB] numbering) |
| Application code | `APP-SRM` ([DN-5](../open-decisions.md#dn-5-application-codes)); IDs use `SRM`, for example `REQ-SRM-001` |
| Application | Service & Resource Management |
| Description | Service instances and cloud/platform resources ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2027.1 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2027.1.1](../sprints/SPRINT-2027.1.1.md) (1–31 Jan 2027, [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Capabilities / features / functions | 4 / 5 / 23 ([WB]) |
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
| [10.01](#1001-service-management) | Service Management | 10.01.01 Service Instance | No | Phase 2 | P1 | [WB:Roadmap] "Service Platform" |
| [10.02](#1002-resource-management) | Resource Management | 10.02.01 Resource Lifecycle | No | Phase 2 | P1 | [WB:Roadmap] "Service Platform" |
| [10.03](#1003-configuration-management) | Configuration Management | 10.03.01 Configuration | No | Not specified | Not specified | Not in [WB:Roadmap] |
| [10.04](#1004-monitoring--health) | Monitoring & Health | 10.04.01 Health Monitoring, 10.04.02 Usage Monitoring | No | Phase 2 | P1 | [WB:Roadmap] "Service Platform" |

## 10.01 Service Management

### Feature 10.01.01 Service Instance

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 10.01.01.01 | Create service instance | No | Phase 2 | P1 | No | Platform Service | `/service-management/create-service-instance` | API-015 GET /v1/services | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 10.01.01.02 | Configure service | No | Phase 2 | P1 | No | Platform Service | `/service-management/configure-service` | API-015 GET /v1/services | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 10.01.01.03 | Start service | No | Phase 2 | P1 | No | Platform Service | `/service-management/start-service` | API-015 GET /v1/services | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 10.01.01.04 | Stop service | No | Phase 2 | P1 | No | Platform Service | `/service-management/stop-service` | API-015 GET /v1/services | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 10.01.01.05 | Restart service | No | Phase 2 | P1 | No | Platform Service | `/service-management/restart-service` | API-015 GET /v1/services | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 10.01.01.06 | Scale service | No | Phase 2 | P1 | No | Platform Service | `/service-management/scale-service` | API-015 GET /v1/services | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 10.01.01.07 | Delete service | No | Phase 2 | P1 | No | Platform Service | `/service-management/delete-service` | API-015 GET /v1/services | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |

## 10.02 Resource Management

### Feature 10.02.01 Resource Lifecycle

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 10.02.01.01 | Create resource | No | Phase 2 | P1 | No | Platform Service | `/resource-management/create-resource` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 10.02.01.02 | Update resource | No | Phase 2 | P1 | No | Platform Service | `/resource-management/update-resource` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 10.02.01.03 | Scale resource | No | Phase 2 | P1 | No | Platform Service | `/resource-management/scale-resource` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 10.02.01.04 | Monitor resource | No | Phase 2 | P1 | No | Platform Service | `/resource-management/monitor-resource` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 10.02.01.05 | Delete resource | No | Phase 2 | P1 | No | Platform Service | `/resource-management/delete-resource` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |

## 10.03 Configuration Management

### Feature 10.03.01 Configuration

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 10.03.01.01 | Create configuration | No | Not specified | Not specified | No | Platform Service | `/configuration-management/create-configuration` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 10.03.01.02 | Validate configuration | No | Not specified | Not specified | No | Platform Service | `/configuration-management/validate-configuration` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 10.03.01.03 | Apply configuration | No | Not specified | Not specified | No | Platform Service | `/configuration-management/apply-configuration` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 10.03.01.04 | Rollback configuration | No | Not specified | Not specified | No | Platform Service | `/configuration-management/rollback-configuration` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |

## 10.04 Monitoring & Health

### Feature 10.04.01 Health Monitoring

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 10.04.01.01 | Collect health status | No | Phase 2 | P1 | No | Platform Service | `/monitoring-health/collect-health-status` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 10.04.01.02 | Detect anomaly | No | Phase 2 | P1 | No | Platform Service | `/monitoring-health/detect-anomaly` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 10.04.01.03 | Create alert | No | Phase 2 | P1 | No | Platform Service | `/monitoring-health/create-alert` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 10.04.01.04 | View health | No | Phase 2 | P1 | No | Platform Service | `/monitoring-health/view-health` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |

### Feature 10.04.02 Usage Monitoring

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 10.04.02.01 | Collect metrics | No | Phase 2 | P1 | No | Platform Service | `/monitoring-health/collect-metrics` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 10.04.02.02 | View usage | No | Phase 2 | P1 | No | Platform Service | `/monitoring-health/view-usage` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |
| 10.04.02.03 | Set threshold | No | Phase 2 | P1 | No | Platform Service | `/monitoring-health/set-threshold` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service |

> **Columns from [WB:Traceability]** (Primary API, Microservice, Entity, Event, Journey) are kept for reference only. Under [C13](../open-decisions.md#c13) microservices are logical domains built as modules in the single backend. Under [C14](../open-decisions.md#c14) the implemented endpoints and each FRD's `api-requirements.md` are the source of truth for APIs.

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 09 | EVT-012 ProvisioningStarted is produced by the Provisioning Orchestrator | [WB:Events] |
| 07 | EVT-011 EntitlementGranted is consumed by Resource and Portal | [WB:Events] |

## Deliverables

Not specified in any source. Under [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates) the sprint commits this application's P0 capabilities; P1 capabilities are stretch scope.

## Requirements, design and tests

| Artifact | Location | Status |
|---|---|---|
| Feature FRD | `docs/02-requirements/FRD/<feature>/` (copy `_template/`) | See the sprint page for FRDs in progress |
| Requirement | `REQ-SRM-<NNN>` inside the FRD | Approved FRD required before build ([DN-4](../open-decisions.md#dn-4-business-rules-and-acceptance-criteria)) |
| Business rules | `docs/03-business-rules/` and `FRD/<feature>/business-rules.md` | Per FRD |
| Test cases | `test-cases/functional/<feature>/TC-SRM-<NNN>.md` | Not created |
| NFRs | [WB:Non-Functional Requirements] NFR-001 to NFR-014 (platform-wide) | See [EIS-document-analysis.md](../EIS-document-analysis.md) section 2.17 |
