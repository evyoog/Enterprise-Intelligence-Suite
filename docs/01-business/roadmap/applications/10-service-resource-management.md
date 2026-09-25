# 10 Service & Resource Management

| Field | Value |
|---|---|
| Application ID | 10 ([WB] numbering; an `APP-<CODE>` code is not assigned in any source) |
| Application | Service & Resource Management |
| Description | Service instances and cloud/platform resources ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2027.1 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2027.1.1](../sprints/SPRINT-2027.1.1.md) |
| Capabilities / features / functions | 4 / 5 / 23 ([WB]) |
| Application status | Not specified |

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.

## Capabilities

| Capability ID | Capability | Features | Priority | MVP | AI relevant |
|---|---|---|---|---|---|
| [10.01](#1001-service-management) | Service Management | 10.01.01 Service Instance | P0 | Yes | No |
| [10.02](#1002-resource-management) | Resource Management | 10.02.01 Resource Lifecycle | P0 | Yes | No |
| [10.03](#1003-configuration-management) | Configuration Management | 10.03.01 Configuration | P0 | Yes | No |
| [10.04](#1004-monitoring--health) | Monitoring & Health | 10.04.01 Health Monitoring, 10.04.02 Usage Monitoring | P0 | Yes | No |

> The Priority and MVP values are copied from [WB:Capabilities]. Every capability in [WB] is P0 / MVP=Yes, which conflicts with the function-level MVP flags (C4 in [open-decisions.md](../open-decisions.md)).

## 10.01 Service Management

### Feature 10.01.01 Service Instance

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 10.01.01.01 | Create service instance | No | No | Platform Service | `/service-management/create-service-instance` | API-015 GET /v1/services | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.01.01.02 | Configure service | No | No | Platform Service | `/service-management/configure-service` | API-015 GET /v1/services | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.01.01.03 | Start service | No | No | Platform Service | `/service-management/start-service` | API-015 GET /v1/services | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.01.01.04 | Stop service | No | No | Platform Service | `/service-management/stop-service` | API-015 GET /v1/services | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.01.01.05 | Restart service | No | No | Platform Service | `/service-management/restart-service` | API-015 GET /v1/services | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.01.01.06 | Scale service | No | No | Platform Service | `/service-management/scale-service` | API-015 GET /v1/services | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.01.01.07 | Delete service | No | No | Platform Service | `/service-management/delete-service` | API-015 GET /v1/services | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |

## 10.02 Resource Management

### Feature 10.02.01 Resource Lifecycle

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 10.02.01.01 | Create resource | No | No | Platform Service | `/resource-management/create-resource` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.02.01.02 | Update resource | No | No | Platform Service | `/resource-management/update-resource` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.02.01.03 | Scale resource | No | No | Platform Service | `/resource-management/scale-resource` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.02.01.04 | Monitor resource | No | No | Platform Service | `/resource-management/monitor-resource` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.02.01.05 | Delete resource | No | No | Platform Service | `/resource-management/delete-resource` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |

## 10.03 Configuration Management

### Feature 10.03.01 Configuration

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 10.03.01.01 | Create configuration | No | No | Platform Service | `/configuration-management/create-configuration` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.03.01.02 | Validate configuration | No | No | Platform Service | `/configuration-management/validate-configuration` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.03.01.03 | Apply configuration | No | No | Platform Service | `/configuration-management/apply-configuration` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.03.01.04 | Rollback configuration | No | No | Platform Service | `/configuration-management/rollback-configuration` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |

## 10.04 Monitoring & Health

### Feature 10.04.01 Health Monitoring

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 10.04.01.01 | Collect health status | No | No | Platform Service | `/monitoring-health/collect-health-status` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.04.01.02 | Detect anomaly | No | No | Platform Service | `/monitoring-health/detect-anomaly` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.04.01.03 | Create alert | No | No | Platform Service | `/monitoring-health/create-alert` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.04.01.04 | View health | No | No | Platform Service | `/monitoring-health/view-health` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |

### Feature 10.04.02 Usage Monitoring

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 10.04.02.01 | Collect metrics | No | No | Platform Service | `/monitoring-health/collect-metrics` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.04.02.02 | View usage | No | No | Platform Service | `/monitoring-health/view-usage` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.04.02.03 | Set threshold | No | No | Platform Service | `/monitoring-health/set-threshold` | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 09 | EVT-012 ProvisioningStarted is produced by the Provisioning Orchestrator | [WB:Events] |
| 07 | EVT-011 EntitlementGranted is consumed by Resource and Portal | [WB:Events] |

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
