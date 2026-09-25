# SPRINT-2027.1.3

| Field | Value |
|---|---|
| Sprint ID (PI.Sprint) | 2027.1.3 |
| PI – CY Quarter | 2027.1 |
| Start / end dates | 1–31 Mar 2027 ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)) |
| Sprint goal | Not specified |
| Team / capacity | Not specified |
| Status | Not specified |
| Source | [PO] roadmap table ([`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)); decisions in [open-decisions.md](../open-decisions.md) |
| Previous / next sprint | [2027.1.2](SPRINT-2027.1.2.md) · [2027.2.1](SPRINT-2027.2.1.md) |

## Scope

| Application ID | Code | Application | Roadmap item | Source |
|---|---|---|---|---|
| 03 | `APP-MKT` | [Marketplace](../applications/03-marketplace.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |
| 11 | `APP-KNW` | [Training & Knowledge Management](../applications/11-training-knowledge-management.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |
| 12 | `APP-SUP` | [Support & Service Management](../applications/12-support-service-management.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |

> **Commitment ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)):** this sprint commits the P0 (MVP) capabilities of its applications and treats P1 capabilities as stretch scope. Anything not finished is recorded as carry-over on the next sprint page.

## Scope changes from decisions

| Change | Decision and scope | FRD | Requirement |
|---|---|---|---|
| Added | [C16](../open-decisions.md#c16) Semantic search (01.03.01), delivered with Knowledge (11). Moved from sprint [2026.3.3](SPRINT-2026.3.3.md) | - | - |
| Added | [C17](../open-decisions.md#c17) Global search (01.03.01) adds knowledge articles (11) and support tickets (12) | - | - |
| Retirement task | [C20](../open-decisions.md#c20) Switch incidents on the interim status page to Incident & Problem Management (12.04), then remove the interim page. Note: 12.04 has no phase in [WB:Roadmap] (priority Not specified under C5) | [service-status-page](../../../02-requirements/FRD/service-status-page/requirement.md) | REQ-PRT-001 |

## EIS 03 Marketplace

**Planned work ([PO] / [WB] description):** Discovery, evaluation, comparison and checkout

Full breakdown with APIs, services, entities and events: [applications/03-marketplace.md](../applications/03-marketplace.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [03.01 Product Discovery](../applications/03-marketplace.md#0301-product-discovery) | 03.01.01 Catalog Browsing | Browse categories; Search products; Filter products; Sort products | P0 | Commit |
| [03.01 Product Discovery](../applications/03-marketplace.md#0301-product-discovery) | 03.01.02 Recommendations | Recommend products; Show featured products; Show popular products | P0 | Commit |
| [03.02 Product Evaluation](../applications/03-marketplace.md#0302-product-evaluation) | 03.02.01 Evaluation | Start trial; Request demo; Launch sandbox; View prerequisites | P0 | Commit |
| [03.02 Product Evaluation](../applications/03-marketplace.md#0302-product-evaluation) | 03.02.02 Comparison | Compare products; Compare plans; Estimate cost | P0 | Commit |
| [03.03 Marketplace Checkout](../applications/03-marketplace.md#0303-marketplace-checkout) | 03.03.01 Checkout | Select product; Select plan; Configure options; Apply discount; Accept terms; Submit order | P0 | Commit |
| [03.03 Marketplace Checkout](../applications/03-marketplace.md#0303-marketplace-checkout) | 03.03.02 Purchase Validation | Validate eligibility; Validate payment; Validate dependencies | P0 | Commit |
| [03.04 Reviews & Ratings](../applications/03-marketplace.md#0304-reviews--ratings) | 03.04.01 Customer Feedback | Submit review; Rate product; Moderate review; View ratings | P0 | Commit |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - UJ-003 Self-Service Purchase: Marketplace → Checkout → Payment (applications 02, 08, 09; [WB:User Journeys])
  - EVT-005 CheckoutCompleted is consumed by Order and Billing (applications 09, 08; [WB:Events])

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). Deliverables are otherwise Not specified in any source.

## EIS 11 Training & Knowledge Management

**Planned work ([PO] / [WB] description):** Documentation, courses, labs and certifications

Full breakdown with APIs, services, entities and events: [applications/11-training-knowledge-management.md](../applications/11-training-knowledge-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [11.01 Knowledge Base](../applications/11-training-knowledge-management.md#1101-knowledge-base) | 11.01.01 Knowledge Articles | Create article; Edit article; Publish article; Search article; Version article | P0 | Commit |
| [11.01 Knowledge Base](../applications/11-training-knowledge-management.md#1101-knowledge-base) | 11.01.02 AI Knowledge | Index content; Retrieve relevant content; Validate source | P0 | Commit |
| [11.02 Learning Management](../applications/11-training-knowledge-management.md#1102-learning-management) | 11.02.01 Courses | Create course; Publish course; Enroll user; Track progress; Complete course | P1 | Stretch |
| [11.02 Learning Management](../applications/11-training-knowledge-management.md#1102-learning-management) | 11.02.02 Learning Paths | Create learning path; Assign learning path; Track path progress | P1 | Stretch |
| [11.03 Training Delivery](../applications/11-training-knowledge-management.md#1103-training-delivery) | 11.03.01 Labs & Assessments | Launch lab; Submit assessment; Score assessment; Track completion | P1 | Stretch |
| [11.03 Training Delivery](../applications/11-training-knowledge-management.md#1103-training-delivery) | 11.03.02 Video Learning | Stream video; Track watch progress | P1 | Stretch |
| [11.04 Certification](../applications/11-training-knowledge-management.md#1104-certification) | 11.04.01 Certificates | Define certification; Issue certificate; Verify certificate; Expire certificate | P1 | Stretch |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - DE-019 KnowledgeArticle and DE-020 Course relate to Product (applications 02; [WB:Data Entities])
  - UJ-008 and UJ-009 use Knowledge (applications 04, 12; [WB:User Journeys])

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). Deliverables are otherwise Not specified in any source.

## EIS 12 Support & Service Management

**Planned work ([PO] / [WB] description):** AI/human support, incidents, requests and SLAs

Full breakdown with APIs, services, entities and events: [applications/12-support-service-management.md](../applications/12-support-service-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | Priority | Commitment |
|---|---|---|---|---|
| [12.01 Support](../applications/12-support-service-management.md#1201-support) | 12.01.01 Ticket Management | Create ticket; Categorize ticket; Prioritize ticket; Assign ticket; Escalate ticket; Resolve ticket; Close ticket | P0 | Commit |
| [12.02 AI Support](../applications/12-support-service-management.md#1202-ai-support) | 12.02.01 Conversational Support | Start conversation; Search knowledge; Diagnose issue; Recommend resolution; Execute permitted remediation | P0 | Commit |
| [12.02 AI Support](../applications/12-support-service-management.md#1202-ai-support) | 12.02.02 Human Handoff | Create ticket; Transfer conversation; Provide AI summary | P0 | Commit |
| [12.03 SLA Management](../applications/12-support-service-management.md#1203-sla-management) | 12.03.01 SLA Policy | Define SLA; Assign SLA; Calculate SLA | Not specified | Not specified |
| [12.03 SLA Management](../applications/12-support-service-management.md#1203-sla-management) | 12.03.02 SLA Monitoring | Monitor SLA; Warn before breach; Escalate breach | Not specified | Not specified |
| [12.04 Incident & Problem Management](../applications/12-support-service-management.md#1204-incident--problem-management) | 12.04.01 Incident | Log incident; Investigate incident; Resolve incident; Close incident | Not specified | Not specified |
| [12.04 Incident & Problem Management](../applications/12-support-service-management.md#1204-incident--problem-management) | 12.04.02 Problem | Create problem; Perform root cause analysis; Track corrective action | Not specified | Not specified |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - UJ-008 AI Technical Support and UJ-009 Human Support Escalation (applications 04, 11; [WB:User Journeys])
  - EVT-008 PaymentFailed and EVT-014 ProvisioningFailed are consumed by Support (applications 08, 09; [WB:Events])

### Expected deliverables

- The P0 capabilities above ([DN-2](../open-decisions.md#dn-2-sprint-scope-length-and-dates)). Deliverables are otherwise Not specified in any source.

## Decisions affecting this sprint

- [C3](../open-decisions.md#c3) The [PO] sprint order is authoritative; the MVP is complete at the end of sprint 2027.1.3.
- [C4](../open-decisions.md#c4), [C5](../open-decisions.md#c5), [C6](../open-decisions.md#c6) MVP, priority and phase as shown above.
- [C10](../open-decisions.md#c10) Application 01 is named "Enterprise Intelligence Suite".
- Sprint goal, team, capacity and status are Not specified.

Details: [open-decisions.md](../open-decisions.md).

## Traceability

```
[PO] roadmap row → SPRINT-2027.1.3 → application page → capability → feature → function (requirement candidate)
   → FRD docs/02-requirements/FRD/<feature>/ → REQ-<CODE>-<NNN> (Approved before build)
   → STORY-<CODE>-<NNN> with "Sprint (PI.Sprint)" = 2027.1.3
   → code (backend/ · frontend/ · ai-service/) → TC-<CODE>-<NNN> in test-cases/ → UAT
```

Stories for this sprint use `docs/02-requirements/functional-requirements/user-story-template.md` or the GitHub **User Story** issue template, with **Sprint (PI.Sprint) = 2027.1.3**. The Definition of Done is the one in the story template.

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)
- **Decisions:** [`open-decisions.md`](../open-decisions.md) (2026-09-25)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source or decision gives are written **Not specified**.
