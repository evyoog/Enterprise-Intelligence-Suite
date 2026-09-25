# 13 Integration & API Platform

| Field | Value |
|---|---|
| Application ID | 13 ([WB] numbering) |
| Application code | `APP-INT` ([DN-5](../open-decisions.md#dn-5-application-codes)); IDs use `INT`, for example `REQ-INT-001` |
| Application | Integration & API Platform |
| Description | APIs, connectors, events and webhooks ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2027.1 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2027.1.1](../sprints/SPRINT-2027.1.1.md) (1–31 Jan 2027, [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Capabilities / features / functions | 4 / 6 / 25 ([WB]) |
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
| [13.01](#1301-api-management) | API Management | 13.01.01 API Lifecycle, 13.01.02 API Security | Yes | Phase 1 / MVP | P0 | C4 |
| [13.02](#1302-integration-hub) | Integration Hub | 13.02.01 Connectors, 13.02.02 Data Integration | No | Phase 2 | P1 | [WB:Roadmap] "Integrations" |
| [13.03](#1303-event-platform) | Event Platform | 13.03.01 Event Bus | Yes | Phase 1 / MVP | P0 | C4 |
| [13.04](#1304-webhooks) | Webhooks | 13.04.01 Webhook Management | No | Phase 2 | P1 | [WB:Roadmap] "Integrations" |

## 13.01 API Management

### Feature 13.01.01 API Lifecycle

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 13.01.01.01 | Register API | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/api-management/register-api` | - | Integration Service | - | - | UJ-005 Provision Service |
| 13.01.01.02 | Publish API | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/api-management/publish-api` | - | Integration Service | - | - | UJ-005 Provision Service |
| 13.01.01.03 | Version API | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/api-management/version-api` | - | Integration Service | - | - | UJ-005 Provision Service |
| 13.01.01.04 | Deprecate API | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/api-management/deprecate-api` | - | Integration Service | - | - | UJ-005 Provision Service |

### Feature 13.01.02 API Security

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 13.01.02.01 | Authenticate API | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/api-management/authenticate-api` | - | Integration Service | - | - | UJ-005 Provision Service |
| 13.01.02.02 | Authorize API | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/api-management/authorize-api` | - | Integration Service | - | - | UJ-005 Provision Service |
| 13.01.02.03 | Rate limit API | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/api-management/rate-limit-api` | - | Integration Service | - | - | UJ-005 Provision Service |
| 13.01.02.04 | Monitor API | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/api-management/monitor-api` | - | Integration Service | - | - | UJ-005 Provision Service |

## 13.02 Integration Hub

### Feature 13.02.01 Connectors

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 13.02.01.01 | Create connector | No | Phase 2 | P1 | No | Platform Service | `/integration-hub/create-connector` | API-018 POST /v1/integrations | Integration Service | - | - | UJ-005 Provision Service |
| 13.02.01.02 | Authenticate connector | No | Phase 2 | P1 | No | Platform Service | `/integration-hub/authenticate-connector` | API-018 POST /v1/integrations | Integration Service | - | - | UJ-005 Provision Service |
| 13.02.01.03 | Test connector | No | Phase 2 | P1 | No | Platform Service | `/integration-hub/test-connector` | API-018 POST /v1/integrations | Integration Service | - | - | UJ-005 Provision Service |
| 13.02.01.04 | Enable connector | No | Phase 2 | P1 | No | Platform Service | `/integration-hub/enable-connector` | API-018 POST /v1/integrations | Integration Service | - | - | UJ-005 Provision Service |

### Feature 13.02.02 Data Integration

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 13.02.02.01 | Synchronize data | No | Phase 2 | P1 | No | Platform Service | `/integration-hub/synchronize-data` | API-018 POST /v1/integrations | Integration Service | - | - | UJ-005 Provision Service |
| 13.02.02.02 | Transform data | No | Phase 2 | P1 | No | Platform Service | `/integration-hub/transform-data` | API-018 POST /v1/integrations | Integration Service | - | - | UJ-005 Provision Service |
| 13.02.02.03 | Handle integration error | No | Phase 2 | P1 | No | Platform Service | `/integration-hub/handle-integration-error` | API-018 POST /v1/integrations | Integration Service | - | - | UJ-005 Provision Service |

## 13.03 Event Platform

### Feature 13.03.01 Event Bus

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 13.03.01.01 | Publish event | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/event-platform/publish-event` | API-019 POST /v1/events | Integration Service | - | - | UJ-005 Provision Service |
| 13.03.01.02 | Subscribe to event | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/event-platform/subscribe-to-event` | API-019 POST /v1/events | Integration Service | - | - | UJ-005 Provision Service |
| 13.03.01.03 | Route event | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/event-platform/route-event` | API-019 POST /v1/events | Integration Service | - | - | UJ-005 Provision Service |
| 13.03.01.04 | Retry event | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/event-platform/retry-event` | API-019 POST /v1/events | Integration Service | - | - | UJ-005 Provision Service |
| 13.03.01.05 | Replay event | Yes | Phase 1 / MVP | P0 | No | Platform Service | `/event-platform/replay-event` | API-019 POST /v1/events | Integration Service | - | - | UJ-005 Provision Service |

## 13.04 Webhooks

### Feature 13.04.01 Webhook Management

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 13.04.01.01 | Register webhook | No | Phase 2 | P1 | No | Platform Service | `/webhooks/register-webhook` | - | Integration Service | - | - | UJ-005 Provision Service |
| 13.04.01.02 | Authenticate webhook | No | Phase 2 | P1 | No | Platform Service | `/webhooks/authenticate-webhook` | - | Integration Service | - | - | UJ-005 Provision Service |
| 13.04.01.03 | Trigger webhook | No | Phase 2 | P1 | No | Platform Service | `/webhooks/trigger-webhook` | - | Integration Service | - | - | UJ-005 Provision Service |
| 13.04.01.04 | Retry webhook | No | Phase 2 | P1 | No | Platform Service | `/webhooks/retry-webhook` | - | Integration Service | - | - | UJ-005 Provision Service |
| 13.04.01.05 | Monitor webhook | No | Phase 2 | P1 | No | Platform Service | `/webhooks/monitor-webhook` | - | Integration Service | - | - | UJ-005 Provision Service |

> **Columns from [WB:Traceability]** (Primary API, Microservice, Entity, Event, Journey) are kept for reference only. Under [C13](../open-decisions.md#c13) microservices are logical domains built as modules in the single backend. Under [C14](../open-decisions.md#c14) the implemented endpoints and each FRD's `api-requirements.md` are the source of truth for APIs.

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| all | MS-019 Event Gateway routes events for all domains; API-019 POST /v1/events | [WB:Microservices], [WB:APIs] |

## Deliverables

Not specified in any source. Under [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates) the sprint commits this application's P0 capabilities; P1 capabilities are stretch scope.

## Requirements, design and tests

| Artifact | Location | Status |
|---|---|---|
| Feature FRD | `docs/02-requirements/FRD/<feature>/` (copy `_template/`) | See the sprint page for FRDs in progress |
| Requirement | `REQ-INT-<NNN>` inside the FRD | Approved FRD required before build ([DN-4](../open-decisions.md#dn-4-business-rules-and-acceptance-criteria)) |
| Business rules | `docs/03-business-rules/` and `FRD/<feature>/business-rules.md` | Per FRD |
| Test cases | `test-cases/functional/<feature>/TC-INT-<NNN>.md` | Not created |
| NFRs | [WB:Non-Functional Requirements] NFR-001 to NFR-014 (platform-wide) | See [EIS-document-analysis.md](../EIS-document-analysis.md) section 2.17 |
