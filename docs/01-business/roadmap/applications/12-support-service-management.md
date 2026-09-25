# 12 Support & Service Management

| Field | Value |
|---|---|
| Application ID | 12 ([WB] numbering) |
| Application code | `APP-SUP` ([DN-5](../open-decisions.md#dn-5-application-codes)); IDs use `SUP`, for example `REQ-SUP-001` |
| Application | Support & Service Management |
| Description | AI/human support, incidents, requests and SLAs ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2027.1 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2027.1.3](../sprints/SPRINT-2027.1.3.md) (1–31 Mar 2027, [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Capabilities / features / functions | 4 / 7 / 28 ([WB]) |
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
| [12.01](#1201-support) | Support | 12.01.01 Ticket Management | Yes | Phase 1 / MVP | P0 | C4 |
| [12.02](#1202-ai-support) | AI Support | 12.02.01 Conversational Support, 12.02.02 Human Handoff | Yes | Phase 1 / MVP | P0 | C4 |
| [12.03](#1203-sla-management) | SLA Management | 12.03.01 SLA Policy, 12.03.02 SLA Monitoring | No | Not specified | Not specified | Not in [WB:Roadmap] |
| [12.04](#1204-incident--problem-management) | Incident & Problem Management | 12.04.01 Incident, 12.04.02 Problem | No | Not specified | P0 | C5, C20 addendum (2026-09-25) |

## 12.01 Support

### Feature 12.01.01 Ticket Management

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 12.01.01.01 | Create ticket | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/support/create-ticket` | API-016 POST /v1/support/tickets | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection |
| 12.01.01.02 | Categorize ticket | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/support/categorize-ticket` | API-016 POST /v1/support/tickets | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection |
| 12.01.01.03 | Prioritize ticket | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/support/prioritize-ticket` | API-016 POST /v1/support/tickets | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection |
| 12.01.01.04 | Assign ticket | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/support/assign-ticket` | API-016 POST /v1/support/tickets | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection |
| 12.01.01.05 | Escalate ticket | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/support/escalate-ticket` | API-016 POST /v1/support/tickets | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection |
| 12.01.01.06 | Resolve ticket | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/support/resolve-ticket` | API-016 POST /v1/support/tickets | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection |
| 12.01.01.07 | Close ticket | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/support/close-ticket` | API-016 POST /v1/support/tickets | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection |

## 12.02 AI Support

### Feature 12.02.01 Conversational Support

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: Yes.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 12.02.01.01 | Start conversation | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-support/start-conversation` | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection |
| 12.02.01.02 | Search knowledge | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-support/search-knowledge` | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection |
| 12.02.01.03 | Diagnose issue | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-support/diagnose-issue` | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection |
| 12.02.01.04 | Recommend resolution | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-support/recommend-resolution` | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection |
| 12.02.01.05 | Execute permitted remediation | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-support/execute-permitted-remediation` | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection |

### Feature 12.02.02 Human Handoff

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 12.02.02.01 | Create ticket | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-support/create-ticket` | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection |
| 12.02.02.02 | Transfer conversation | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-support/transfer-conversation` | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection |
| 12.02.02.03 | Provide AI summary | Yes | Phase 1 / MVP | P0 | Yes | AI Agent | `/ai-support/provide-ai-summary` | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection |

## 12.03 SLA Management

### Feature 12.03.01 SLA Policy

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 12.03.01.01 | Define SLA | No | Not specified | Not specified | No | Platform Service | `/sla-management/define-sla` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service |
| 12.03.01.02 | Assign SLA | No | Not specified | Not specified | No | Platform Service | `/sla-management/assign-sla` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service |
| 12.03.01.03 | Calculate SLA | No | Not specified | Not specified | No | Platform Service | `/sla-management/calculate-sla` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service |

### Feature 12.03.02 SLA Monitoring

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 12.03.02.01 | Monitor SLA | No | Not specified | Not specified | No | Platform Service | `/sla-management/monitor-sla` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service |
| 12.03.02.02 | Warn before breach | No | Not specified | Not specified | No | Platform Service | `/sla-management/warn-before-breach` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service |
| 12.03.02.03 | Escalate breach | No | Not specified | Not specified | No | Platform Service | `/sla-management/escalate-breach` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service |

## 12.04 Incident & Problem Management

### Feature 12.04.01 Incident

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 12.04.01.01 | Log incident | No | Not specified | P0 | No | Platform Service | `/incident-problem-management/log-incident` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service |
| 12.04.01.02 | Investigate incident | No | Not specified | P0 | No | Platform Service | `/incident-problem-management/investigate-incident` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service |
| 12.04.01.03 | Resolve incident | No | Not specified | P0 | No | Platform Service | `/incident-problem-management/resolve-incident` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service |
| 12.04.01.04 | Close incident | No | Not specified | P0 | No | Platform Service | `/incident-problem-management/close-incident` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service |

### Feature 12.04.02 Problem

AI required: decided in the feature FRD ([C12](../open-decisions.md#c12)). [WB:Features] flag, informational only: No.

| Function ID | Function (requirement candidate) | MVP | Priority | Phase | AI ([WB], info) | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 12.04.02.01 | Create problem | No | Not specified | P0 | No | Platform Service | `/incident-problem-management/create-problem` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service |
| 12.04.02.02 | Perform root cause analysis | No | Not specified | P0 | No | Platform Service | `/incident-problem-management/perform-root-cause-analysis` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service |
| 12.04.02.03 | Track corrective action | No | Not specified | P0 | No | Platform Service | `/incident-problem-management/track-corrective-action` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service |

> **Columns from [WB:Traceability]** (Primary API, Microservice, Entity, Event, Journey) are kept for reference only. Under [C13](../open-decisions.md#c13) microservices are logical domains built as modules in the single backend. Under [C14](../open-decisions.md#c14) the implemented endpoints and each FRD's `api-requirements.md` are the source of truth for APIs.

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 04, 11 | UJ-008 AI Technical Support and UJ-009 Human Support Escalation | [WB:User Journeys] |
| 08, 09 | EVT-008 PaymentFailed and EVT-014 ProvisioningFailed are consumed by Support | [WB:Events] |

## Deliverables

Not specified in any source. Under [DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates) the sprint commits this application's P0 capabilities; P1 capabilities are stretch scope.

## Requirements, design and tests

| Artifact | Location | Status |
|---|---|---|
| Feature FRD | `docs/02-requirements/FRD/<feature>/` (copy `_template/`) | See the sprint page for FRDs in progress |
| Requirement | `REQ-SUP-<NNN>` inside the FRD | Approved FRD required before build ([DN-4](../open-decisions.md#dn-4-business-rules-and-acceptance-criteria)) |
| Business rules | `docs/03-business-rules/` and `FRD/<feature>/business-rules.md` | Per FRD |
| Test cases | `test-cases/functional/<feature>/TC-SUP-<NNN>.md` | Not created |
| NFRs | [WB:Non-Functional Requirements] NFR-001 to NFR-014 (platform-wide) | See [EIS-document-analysis.md](../EIS-document-analysis.md) section 2.17 |
