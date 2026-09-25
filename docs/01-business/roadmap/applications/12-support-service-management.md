# 12 Support & Service Management

| Field | Value |
|---|---|
| Application ID | 12 ([WB] numbering; an `APP-<CODE>` code is not assigned in any source) |
| Application | Support & Service Management |
| Description | AI/human support, incidents, requests and SLAs ([PO] Table 1, [WB:Application Summary]) |
| Product | EIS (PaaS) |
| PI – CY Quarter | 2027.1 ([PO] "eVyoog EIS - Roadmap Initiatives") |
| Sprint | [2027.1.3](../sprints/SPRINT-2027.1.3.md) |
| Capabilities / features / functions | 4 / 7 / 28 ([WB]) |
| Application status | Not specified |

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.

## Capabilities

| Capability ID | Capability | Features | Priority | MVP | AI relevant |
|---|---|---|---|---|---|
| [12.01](#1201-support) | Support | 12.01.01 Ticket Management | P0 | Yes | Yes |
| [12.02](#1202-ai-support) | AI Support | 12.02.01 Conversational Support, 12.02.02 Human Handoff | P0 | Yes | Yes |
| [12.03](#1203-sla-management) | SLA Management | 12.03.01 SLA Policy, 12.03.02 SLA Monitoring | P0 | Yes | No |
| [12.04](#1204-incident--problem-management) | Incident & Problem Management | 12.04.01 Incident, 12.04.02 Problem | P0 | Yes | No |

> The Priority and MVP values are copied from [WB:Capabilities]. Every capability in [WB] is P0 / MVP=Yes, which conflicts with the function-level MVP flags (C4 in [open-decisions.md](../open-decisions.md)).

## 12.01 Support

### Feature 12.01.01 Ticket Management

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 12.01.01.01 | Create ticket | No | Yes | AI Agent | `/support/create-ticket` | API-016 POST /v1/support/tickets | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.01.01.02 | Categorize ticket | No | Yes | AI Agent | `/support/categorize-ticket` | API-016 POST /v1/support/tickets | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.01.01.03 | Prioritize ticket | No | Yes | AI Agent | `/support/prioritize-ticket` | API-016 POST /v1/support/tickets | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.01.01.04 | Assign ticket | No | Yes | AI Agent | `/support/assign-ticket` | API-016 POST /v1/support/tickets | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.01.01.05 | Escalate ticket | No | Yes | AI Agent | `/support/escalate-ticket` | API-016 POST /v1/support/tickets | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.01.01.06 | Resolve ticket | No | Yes | AI Agent | `/support/resolve-ticket` | API-016 POST /v1/support/tickets | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.01.01.07 | Close ticket | No | Yes | AI Agent | `/support/close-ticket` | API-016 POST /v1/support/tickets | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |

## 12.02 AI Support

### Feature 12.02.01 Conversational Support

Priority P1 · MVP Yes · AI required Yes ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 12.02.01.01 | Start conversation | No | Yes | AI Agent | `/ai-support/start-conversation` | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.02.01.02 | Search knowledge | No | Yes | AI Agent | `/ai-support/search-knowledge` | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.02.01.03 | Diagnose issue | No | Yes | AI Agent | `/ai-support/diagnose-issue` | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.02.01.04 | Recommend resolution | No | Yes | AI Agent | `/ai-support/recommend-resolution` | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.02.01.05 | Execute permitted remediation | No | Yes | AI Agent | `/ai-support/execute-permitted-remediation` | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |

### Feature 12.02.02 Human Handoff

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 12.02.02.01 | Create ticket | No | Yes | AI Agent | `/ai-support/create-ticket` | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.02.02.02 | Transfer conversation | No | Yes | AI Agent | `/ai-support/transfer-conversation` | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.02.02.03 | Provide AI summary | No | Yes | AI Agent | `/ai-support/provide-ai-summary` | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |

## 12.03 SLA Management

### Feature 12.03.01 SLA Policy

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 12.03.01.01 | Define SLA | No | No | Platform Service | `/sla-management/define-sla` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |
| 12.03.01.02 | Assign SLA | No | No | Platform Service | `/sla-management/assign-sla` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |
| 12.03.01.03 | Calculate SLA | No | No | Platform Service | `/sla-management/calculate-sla` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |

### Feature 12.03.02 SLA Monitoring

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 12.03.02.01 | Monitor SLA | No | No | Platform Service | `/sla-management/monitor-sla` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |
| 12.03.02.02 | Warn before breach | No | No | Platform Service | `/sla-management/warn-before-breach` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |
| 12.03.02.03 | Escalate breach | No | No | Platform Service | `/sla-management/escalate-breach` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |

## 12.04 Incident & Problem Management

### Feature 12.04.01 Incident

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 12.04.01.01 | Log incident | No | No | Platform Service | `/incident-problem-management/log-incident` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |
| 12.04.01.02 | Investigate incident | No | No | Platform Service | `/incident-problem-management/investigate-incident` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |
| 12.04.01.03 | Resolve incident | No | No | Platform Service | `/incident-problem-management/resolve-incident` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |
| 12.04.01.04 | Close incident | No | No | Platform Service | `/incident-problem-management/close-incident` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |

### Feature 12.04.02 Problem

Priority P1 · MVP Yes · AI required No ([WB:Features])

| Function ID | Function (requirement candidate) | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 12.04.02.01 | Create problem | No | No | Platform Service | `/incident-problem-management/create-problem` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |
| 12.04.02.02 | Perform root cause analysis | No | No | Platform Service | `/incident-problem-management/perform-root-cause-analysis` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |
| 12.04.02.03 | Track corrective action | No | No | Platform Service | `/incident-problem-management/track-corrective-action` | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |

## Dependencies

**Stated in the source documents:** Not specified.

**Implied by [WB] relationships.** These are not stated as dependencies anywhere, so confirm them before planning:

| Related application(s) | Relationship | Source |
|---|---|---|
| 04, 11 | UJ-008 AI Technical Support and UJ-009 Human Support Escalation | [WB:User Journeys] |
| 08, 09 | EVT-008 PaymentFailed and EVT-014 ProvisioningFailed are consumed by Support | [WB:Events] |

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
