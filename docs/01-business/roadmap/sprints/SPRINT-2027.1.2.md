# SPRINT-2027.1.2

| Field | Value |
|---|---|
| Sprint ID (PI.Sprint) | 2027.1.2 |
| PI – CY Quarter | 2027.1 |
| Start / end dates | 1–28 Feb 2027 ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Sprint goal | Not specified |
| Team / capacity | Not specified |
| Status | Not specified |
| Source | [PO] roadmap table ([`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)); decisions in [open-decisions.md](../open-decisions.md) |
| Previous / next sprint | [2027.1.1](SPRINT-2027.1.1.md) · [2027.1.3](SPRINT-2027.1.3.md) |

## Scope

| Application ID | Code | Application | Roadmap item | Source |
|---|---|---|---|---|
| 04a | `APP-AIP` (part) | [AI Advisor & Agent Platform](../applications/04-ai-advisor-agent-platform.md) | Agent orchestration foundation only (04.05) | [PO], scope split by [C31](../open-decisions.md#c31) |
| 10 | `APP-SRM` | [Service & Resource Management](../applications/10-service-resource-management.md) | (whole application) | [C31](../open-decisions.md#c31): pulled back from 2027.1.1 |
| 03a | `APP-MKT` (part) | [Marketplace](../applications/03-marketplace.md) | Product Discovery and Marketplace Checkout only (03.01, 03.03) | [C31](../open-decisions.md#c31): pulled forward from 2027.1.3 |
| 12a | `APP-SUP` (part) | [Support & Service Management](../applications/12-support-service-management.md) | Human support: Support, SLA, Incident & Problem only (12.01, 12.03, 12.04) | [C31](../open-decisions.md#c31): pulled forward from 2027.1.3 |

> **Commitment ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)):** this sprint commits the P0 (MVP) capabilities of its applications and treats P1 capabilities as stretch scope. Anything not finished is recorded as carry-over on the next sprint page.

## Scope changes from decisions

| Change | Decision and scope | FRD | Requirement |
|---|---|---|---|
| P0 added | [C21](../open-decisions.md#c21) Agent controls from [SUM] "Security & Governance Model": identity forwarding (scoped, short-lived token of the acting user); tool allowlists per agent, fixed at registration; human approval for high-risk writes above a defined threshold; audit of every agent tool call (agent identity, acting user, tool, arguments) | - | - |
| P0 added | [C21](../open-decisions.md#c21) Per-agent scoping for the MVP agents: the Advisory agent writes order drafts only (no order submission, no payment tools); the Support agent writes tickets only (no billing or payment tools, no account deletion) | - | - |
| P0 added | [C4](../open-decisions.md#c4) From 04.05 AI Agent Orchestration: 04.05.01.05 "Apply guardrails" and 04.05.01.06 "Audit agent action" | - | - |
| Split | [C31](../open-decisions.md#c31) 04 AI Advisor & Agent Platform splits: 04a (04.05 Agent Orchestration, the foundation) stays here; 04b (04.01–04.04, the customer-facing agents) moves to [2027.1.3](SPRINT-2027.1.3.md) | - | - |
| Moved in | [C31](../open-decisions.md#c31) 10 Service & Resource Management moves here from 2027.1.1 (service instances are created by provisioning, which is built there, so they can't be managed in that same sprint) | - | - |
| Moved in | [C31](../open-decisions.md#c31) 03a Discovery & checkout moves here from 2027.1.3 (checkout should close the buy/pay/provision loop before AI agents start recommending products) | - | - |
| Moved in | [C31](../open-decisions.md#c31) 12a Human support moves here from 2027.1.3 (the AI support agent, 04b, escalates to people, so human ticketing must exist first) | - | - |
| Decided | [C40](../open-decisions.md#c40) Recommendations (03.01.02): rule-based featured flag + existing launch-count usage data, no AI/ML model | [product-recommendations](../../../02-requirements/FRD/product-recommendations/requirement.md) | REQ-MKT-001 |
| Decided | [C40](../open-decisions.md#c40) Ticket Management (12.01.01) built, human-operated (not AI-driven); 12.03 SLA Management and 12.04 Incident & Problem Management not built | [ticket-management](../../../02-requirements/FRD/ticket-management/requirement.md) | REQ-SUP-001 |
| Not built | [C40](../open-decisions.md#c40) 03.03 Marketplace Checkout: needs a product-options/discount data model no source specifies — carried further |
| Not built | [C40](../open-decisions.md#c40) 04a AI Agent Orchestration (whole application, including the C21/C4 additions above): needs an agent/LLM framework decision — carried further |
| Not built | [C40](../open-decisions.md#c40) 10 Service & Resource Management (whole application, including the C20 retirement task): P1/stretch, no real infrastructure to manage yet — carried further |
| Moved out | [C59](../open-decisions.md#c59) 03.03 Marketplace Checkout moves to [2026.4.3](SPRINT-2026.4.3.md), built with Billing as the cart and checkout ([REQ-MKT-003](../../../02-requirements/FRD/cart-checkout/requirement.md)). 03a here keeps 03.01 Product Discovery only | [cart-checkout](../../../02-requirements/FRD/cart-checkout/requirement.md) | REQ-MKT-003 |
| Added | [C57](../open-decisions.md#c57) Service instances (answer to D7, option A): one subscription's provisioned tenant in a hosted product, with status and product-reported health; the source the interim status page ([C20](../open-decisions.md#c20)) switches to for per-product status. FRD is a documents-only stub | [service-instances](../../../02-requirements/FRD/service-instances/requirement.md) | REQ-SRM-001 |

### FRDs in this sprint

| FRD | Requirement | Functions | Status |
|---|---|---|---|
| [product-recommendations](../../../02-requirements/FRD/product-recommendations/requirement.md) | REQ-MKT-001 | 03.01.02 (03.01.01 already satisfied by existing catalog search) | Approved |
| [ticket-management](../../../02-requirements/FRD/ticket-management/requirement.md) | REQ-SUP-001 | 12.01.01 | Approved |
| [service-instances](../../../02-requirements/FRD/service-instances/requirement.md) | REQ-SRM-001 | 10 Service & Resource Management (service instance status and health) | Draft (stub) |

### Progress (as of 2026-09-28)

| Feature | Status | Note |
|---|---|---|
| 03.01.01 Catalog Browsing | Done (pre-existing) | Browse/search/filter/sort already built (Phase 17), before this sprint |
| 03.01.02 Recommendations | Done (this FRD's scope) | Featured flag + popularity from existing usage data, no AI/ML model |
| 03.03 Marketplace Checkout | Moved | Moved to [2026.4.3](SPRINT-2026.4.3.md) by [C59](../open-decisions.md#c59) (REQ-MKT-003, Draft) |
| 04a AI Agent Orchestration (whole application) | Not started | Carried — needs an agent/LLM framework decision first ([C40](../open-decisions.md#c40)) |
| 10 Service & Resource Management (whole application) | Not started | Carried — P1/stretch, no infrastructure to manage; the [C20](../open-decisions.md#c20) retirement task still can't land |
| 12.01.01 Ticket Management | Done (this FRD's scope) | Human-operated, not AI-driven |
| 12.03 SLA Management | Not started | Carried — needs its own SLA-policy data model first ([C40](../open-decisions.md#c40)) |
| 12.04 Incident & Problem Management | Not started | Carried — needs a decided relationship to the existing `service_incident` table first ([C40](../open-decisions.md#c40)) |

03.03, 04a, 10, 12.03 and 12.04 remain open for this sprint, each carried to a later sprint once the decision it needs is made.

## EIS 04a AI Advisor & Agent Platform: agent orchestration foundation

**Planned work:** Agent orchestration, registry, MCP gateway, model gateway, guardrails, human approval, with agents read-only. 04b (the customer-facing agents) moves to [2027.1.3](SPRINT-2027.1.3.md) ([C31](../open-decisions.md#c31)).

Full breakdown: [applications/04-ai-advisor-agent-platform.md](../applications/04-ai-advisor-agent-platform.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [04.05 AI Agent Orchestration](../applications/04-ai-advisor-agent-platform.md#0405-ai-agent-orchestration) | 04.05.01 Agent Runtime | Register agent; Route request; Select tools; Manage context; Apply guardrails (P0); Audit agent action (P0) | P0 (2 functions) · P1 | Commit · Stretch |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]:** IAM (06) for identity forwarding; 13a (2026.4.2) for the MCP/tool gateway; 15a (2026.4.2) for audit.

### Expected deliverables

- The P0 capabilities above, plus the C21 agent controls ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). 04.01–04.04 (P0/P1, Commit/Stretch) are no longer in this sprint — see [2027.1.3](SPRINT-2027.1.3.md).

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
  - EVT-012 ProvisioningStarted is produced by the Provisioning Orchestrator (application 09, 2027.1.1; [WB:Events])
  - EVT-011 EntitlementGranted is consumed by Resource and Portal (application 07; [WB:Events])

### Expected deliverables

- The retirement task from [C20](../open-decisions.md#c20) (switch the interim status page's per-product status to 10.04 Health Monitoring), plus the P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)); 10.04 is P1 (stretch) under C4/C5, so this carries over if not delivered.

## EIS 03a Marketplace: discovery and checkout

**Planned work:** Product Discovery and Marketplace Checkout. 03b (Product Evaluation, Reviews & Ratings) stays in [2027.1.3](SPRINT-2027.1.3.md) ([C31](../open-decisions.md#c31)).

Full breakdown: [applications/03-marketplace.md](../applications/03-marketplace.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [03.01 Product Discovery](../applications/03-marketplace.md#0301-product-discovery) | 03.01.01 Catalog Browsing | Browse categories; Search products; Filter products; Sort products | P0 | Commit |
| [03.01 Product Discovery](../applications/03-marketplace.md#0301-product-discovery) | 03.01.02 Recommendations | Recommend products; Show featured products; Show popular products | P0 | Commit |
| [03.03 Marketplace Checkout](../applications/03-marketplace.md#0303-marketplace-checkout) | 03.03.01 Checkout | Select product; Select plan; Configure options; Apply discount; Accept terms; Submit order | P0 | Commit |
| [03.03 Marketplace Checkout](../applications/03-marketplace.md#0303-marketplace-checkout) | 03.03.02 Purchase Validation | Validate eligibility; Validate payment; Validate dependencies | P0 | Commit |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]:** needs the catalog (02, 2026.4.1), billing/payment (08, 2026.4.3) and orders (09, 2027.1.1).

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). 03.02 Product Evaluation and 03.04 Reviews & Ratings (03b) are not in this sprint — see [2027.1.3](SPRINT-2027.1.3.md).

## EIS 12a Support & Service Management: human support

**Planned work:** Support, SLA Management, Incident & Problem Management. AI Support (12.02) ships with the customer AI agents (04b) in [2027.1.3](SPRINT-2027.1.3.md) ([C31](../open-decisions.md#c31)).

Full breakdown: [applications/12-support-service-management.md](../applications/12-support-service-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [12.01 Support](../applications/12-support-service-management.md#1201-support) | 12.01.01 Ticket Management | Create ticket; Categorize ticket; Prioritize ticket; Assign ticket; Escalate ticket; Resolve ticket; Close ticket | P0 | Commit |
| [12.03 SLA Management](../applications/12-support-service-management.md#1203-sla-management) | 12.03.01 SLA Policy | Define SLA; Assign SLA; Calculate SLA | P0 | Commit |
| [12.03 SLA Management](../applications/12-support-service-management.md#1203-sla-management) | 12.03.02 SLA Monitoring | Monitor SLA; Warn before breach; Escalate breach | P0 | Commit |
| [12.04 Incident & Problem Management](../applications/12-support-service-management.md#1204-incident--problem-management) | 12.04.01 Incident | Log incident; Investigate incident; Resolve incident; Close incident | P0 | Commit |
| [12.04 Incident & Problem Management](../applications/12-support-service-management.md#1204-incident--problem-management) | 12.04.02 Problem | Create problem; Perform root cause analysis; Track corrective action | P0 | Commit |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]:** needs Tenant Management (05, 2026.4.1) and Knowledge Base (11a, 2027.1.1). Superseded, in part, by the interim service-status incidents (C20) already built in 2026.3.3.

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). 12.02 AI Support is not in this sprint — see [2027.1.3](SPRINT-2027.1.3.md).

## Decisions affecting this sprint

- [C3](../open-decisions.md#c3) The [PO] sprint order is authoritative; the MVP is complete at the end of sprint 2027.1.3.
- [C4](../open-decisions.md#c4), [C5](../open-decisions.md#c5), [C6](../open-decisions.md#c6) MVP, priority and phase as shown above.
- [C20](../open-decisions.md#c20) The retirement task for the interim service-status page, now here with 10 Service & Resource Management.
- [C31](../open-decisions.md#c31) Corrected sprint sequence: 04 split, 10, 03a and 12a moved in as shown above.
- [C57](../open-decisions.md#c57) Service instances; [C59](../open-decisions.md#c59) 03.03 moved to 2026.4.3.
- Sprint goal, team, capacity and status are Not specified.

Details: [open-decisions.md](../open-decisions.md).

## Traceability

```
[PO] roadmap row → SPRINT-2027.1.2 → application page → capability → feature → function (requirement candidate)
   → FRD docs/02-requirements/FRD/<feature>/ → REQ-<CODE>-<NNN> (Approved before build)
   → STORY-<CODE>-<NNN> with "Sprint (PI.Sprint)" = 2027.1.2
   → code (backend/ · frontend/ · ai-service/) → TC-<CODE>-<NNN> in test-cases/ → UAT
```

Stories for this sprint use `docs/02-requirements/functional-requirements/user-story-template.md` or the GitHub **User Story** issue template, with **Sprint (PI.Sprint) = 2027.1.2**. The Definition of Done is the one in the story template.

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)
- **Decisions:** [`open-decisions.md`](../open-decisions.md) (2026-09-25)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source or decision gives are written **Not specified**.
