# SPRINT-2027.1.1

| Field | Value |
|---|---|
| Sprint ID (PI.Sprint) | 2027.1.1 |
| PI – CY Quarter | 2027.1 |
| Start / end dates | 1–31 Jan 2027 ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Sprint goal | Not specified |
| Team / capacity | Not specified |
| Status | Not specified |
| Source | [PO] roadmap table ([`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)); decisions in [open-decisions.md](../open-decisions.md) |
| Previous / next sprint | [2026.4.3](SPRINT-2026.4.3.md) · [2027.1.2](SPRINT-2027.1.2.md) |

## Scope

| Application ID | Code | Application | Roadmap item | Source |
|---|---|---|---|---|
| 09 | `APP-ORD` | [Order & Provisioning Management](../applications/09-order-provisioning-management.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |
| 10 | `APP-SRM` | [Service & Resource Management](../applications/10-service-resource-management.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |
| 13 | `APP-INT` | [Integration & API Platform](../applications/13-integration-api-platform.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |
| 16 | `APP-ANL` | [Analytics & Data Platform](../applications/16-analytics-data-platform.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |

> **Commitment ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)):** this sprint commits the P0 (MVP) capabilities of its applications and treats P1 capabilities as stretch scope. Anything not finished is recorded as carry-over on the next sprint page.

## Scope changes from decisions

| Change | Decision and scope | FRD | Requirement |
|---|---|---|---|
| Retirement task | [C20](../open-decisions.md#c20) Switch per-product status on the interim status page to Health Monitoring (10.04). Note: 10.04 is P1 (stretch) under C4/C5; if it is not delivered, this task carries over (DN-2) | [service-status-page](../../../02-requirements/FRD/service-status-page/requirement.md) | REQ-PRT-001 |

## EIS 09 Order & Provisioning Management

**Planned work ([PO] / [WB] description):** Orders, provisioning and workflow orchestration

Full breakdown with APIs, services, entities and events: [applications/09-order-provisioning-management.md](../applications/09-order-provisioning-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [09.01 Order Management](../applications/09-order-provisioning-management.md#0901-order-management) | 09.01.01 Order Lifecycle | Create order; Validate order; Price order; Submit order; Approve order; Cancel order; Track order | P0 | Commit |
| [09.02 Provisioning](../applications/09-order-provisioning-management.md#0902-provisioning) | 09.02.01 Service Provisioning | Provision service; Configure service; Activate service; Suspend service; Deprovision service | P0 | Commit |
| [09.03 Workflow Orchestration](../applications/09-order-provisioning-management.md#0903-workflow-orchestration) | 09.03.01 Workflow Runtime | Trigger workflow; Execute workflow; Retry step; Rollback; Compensate; Escalate | P0 | Commit |
| [09.03 Workflow Orchestration](../applications/09-order-provisioning-management.md#0903-workflow-orchestration) | 09.03.02 Workflow Design | Define workflow; Configure step; Set dependency | P0 | Commit |
| [09.04 Approval Management](../applications/09-order-provisioning-management.md#0904-approval-management) | 09.04.01 Approvals | Create approval; Route approval; Approve; Reject; Escalate | P0 | Commit |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - EVT-005 CheckoutCompleted is consumed by Order (applications 03; [WB:Events])
  - UJ-005 Provision Service: Order accepted → Entitlement → Provision → Activate → Notify (applications 07; [WB:User Journeys])
  - EVT-012 ProvisioningStarted is consumed by Resource Service (applications 10; [WB:Events])

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). Deliverables are otherwise Not specified in any source.

## EIS 10 Service & Resource Management

**Planned work ([PO] / [WB] description):** Service instances and cloud/platform resources

Full breakdown with APIs, services, entities and events: [applications/10-service-resource-management.md](../applications/10-service-resource-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [10.01 Service Management](../applications/10-service-resource-management.md#1001-service-management) | 10.01.01 Service Instance | Create service instance; Configure service; Start service; Stop service; Restart service; Scale service; Delete service | P1 | Stretch |
| [10.02 Resource Management](../applications/10-service-resource-management.md#1002-resource-management) | 10.02.01 Resource Lifecycle | Create resource; Update resource; Scale resource; Monitor resource; Delete resource | P1 | Stretch |
| [10.03 Configuration Management](../applications/10-service-resource-management.md#1003-configuration-management) | 10.03.01 Configuration | Create configuration; Validate configuration; Apply configuration; Rollback configuration | Not specified | Not specified |
| [10.04 Monitoring & Health](../applications/10-service-resource-management.md#1004-monitoring--health) | 10.04.01 Health Monitoring | Collect health status; Detect anomaly; Create alert; View health | P1 | Stretch |
| [10.04 Monitoring & Health](../applications/10-service-resource-management.md#1004-monitoring--health) | 10.04.02 Usage Monitoring | Collect metrics; View usage; Set threshold | P1 | Stretch |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - EVT-012 ProvisioningStarted is produced by the Provisioning Orchestrator (applications 09; [WB:Events])
  - EVT-011 EntitlementGranted is consumed by Resource and Portal (applications 07; [WB:Events])

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). Deliverables are otherwise Not specified in any source.

## EIS 13 Integration & API Platform

**Planned work ([PO] / [WB] description):** APIs, connectors, events and webhooks

Full breakdown with APIs, services, entities and events: [applications/13-integration-api-platform.md](../applications/13-integration-api-platform.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [13.01 API Management](../applications/13-integration-api-platform.md#1301-api-management) | 13.01.01 API Lifecycle | Register API; Publish API; Version API; Deprecate API | P0 | Commit |
| [13.01 API Management](../applications/13-integration-api-platform.md#1301-api-management) | 13.01.02 API Security | Authenticate API; Authorize API; Rate limit API; Monitor API | P0 | Commit |
| [13.02 Integration Hub](../applications/13-integration-api-platform.md#1302-integration-hub) | 13.02.01 Connectors | Create connector; Authenticate connector; Test connector; Enable connector | P1 | Stretch |
| [13.02 Integration Hub](../applications/13-integration-api-platform.md#1302-integration-hub) | 13.02.02 Data Integration | Synchronize data; Transform data; Handle integration error | P1 | Stretch |
| [13.03 Event Platform](../applications/13-integration-api-platform.md#1303-event-platform) | 13.03.01 Event Bus | Publish event; Subscribe to event; Route event; Retry event; Replay event | P0 | Commit |
| [13.04 Webhooks](../applications/13-integration-api-platform.md#1304-webhooks) | 13.04.01 Webhook Management | Register webhook; Authenticate webhook; Trigger webhook; Retry webhook; Monitor webhook | P1 | Stretch |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - MS-019 Event Gateway routes events for all domains; API-019 POST /v1/events (applications all; [WB:Microservices], [WB:APIs])

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). Deliverables are otherwise Not specified in any source.

## EIS 16 Analytics & Data Platform

**Planned work ([PO] / [WB] description):** Customer, product, operational and business analytics

Full breakdown with APIs, services, entities and events: [applications/16-analytics-data-platform.md](../applications/16-analytics-data-platform.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [16.01 Customer Analytics](../applications/16-analytics-data-platform.md#1601-customer-analytics) | 16.01.01 Customer Usage | Analyze usage; Analyze adoption; Analyze engagement; Calculate customer health | P1 | Stretch |
| [16.01 Customer Analytics](../applications/16-analytics-data-platform.md#1601-customer-analytics) | 16.01.02 Customer Value | Analyze spending; Calculate customer lifetime value; Analyze churn | P1 | Stretch |
| [16.02 Product Analytics](../applications/16-analytics-data-platform.md#1602-product-analytics) | 16.02.01 Product Performance | Analyze views; Analyze trials; Analyze conversions; Analyze subscriptions | P1 | Stretch |
| [16.02 Product Analytics](../applications/16-analytics-data-platform.md#1602-product-analytics) | 16.02.02 Product Usage | Analyze usage; Analyze feature adoption; Analyze churn | P1 | Stretch |
| [16.03 Operational Analytics](../applications/16-analytics-data-platform.md#1603-operational-analytics) | 16.03.01 Operations | Analyze incidents; Analyze SLA; Analyze provisioning time; Analyze service health | Not specified | Not specified |
| [16.04 Business Analytics](../applications/16-analytics-data-platform.md#1604-business-analytics) | 16.04.01 Financial KPIs | Calculate revenue; Calculate ARR; Calculate MRR; Calculate marketplace GMV | P1 | Stretch |
| [16.04 Business Analytics](../applications/16-analytics-data-platform.md#1604-business-analytics) | 16.04.02 Growth KPIs | Calculate acquisition; Calculate churn; Calculate conversion | P1 | Stretch |
| [16.05 Data Platform](../applications/16-analytics-data-platform.md#1605-data-platform) | 16.05.01 Data Ingestion | Ingest operational data; Ingest event data; Validate data | P1 | Stretch |
| [16.05 Data Platform](../applications/16-analytics-data-platform.md#1605-data-platform) | 16.05.02 Data Management | Transform data; Catalog data; Manage lineage; Manage data quality | P1 | Stretch |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - Analytics consumes EVT-001, EVT-015 and EVT-018 (applications all; [WB:Events])
  - UJ-012 Renewal and Expansion (Analytics, AI, Subscription, Billing) (applications 01, 07, 04; [WB:User Journeys])

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). Deliverables are otherwise Not specified in any source.

## Decisions affecting this sprint

- [C3](../open-decisions.md#c3) The [PO] sprint order is authoritative; the MVP is complete at the end of sprint 2027.1.3.
- [C4](../open-decisions.md#c4), [C5](../open-decisions.md#c5), [C6](../open-decisions.md#c6) MVP, priority and phase as shown above.
- Sprint goal, team, capacity and status are Not specified.

Details: [open-decisions.md](../open-decisions.md).

## Traceability

```
[PO] roadmap row → SPRINT-2027.1.1 → application page → capability → feature → function (requirement candidate)
   → FRD docs/02-requirements/FRD/<feature>/ → REQ-<CODE>-<NNN> (Approved before build)
   → STORY-<CODE>-<NNN> with "Sprint (PI.Sprint)" = 2027.1.1
   → code (backend/ · frontend/ · ai-service/) → TC-<CODE>-<NNN> in test-cases/ → UAT
```

Stories for this sprint use `docs/02-requirements/functional-requirements/user-story-template.md` or the GitHub **User Story** issue template, with **Sprint (PI.Sprint) = 2027.1.1**. The Definition of Done is the one in the story template.

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)
- **Decisions:** [`open-decisions.md`](../open-decisions.md) (2026-09-25)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source or decision gives are written **Not specified**.
