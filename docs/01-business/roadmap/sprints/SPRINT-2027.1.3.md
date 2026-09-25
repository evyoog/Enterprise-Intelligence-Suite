# SPRINT-2027.1.3

| Field | Value |
|---|---|
| Sprint ID (PI.Sprint) | 2027.1.3 |
| PI – CY Quarter | 2027.1 |
| Start / end dates | Not specified |
| Sprint goal | Not specified |
| Team / capacity | Not specified |
| Status | Not specified |
| Source | [PO] roadmap tables ([`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)) |
| Previous / next sprint | [2027.1.2](SPRINT-2027.1.2.md) · [2027.2.1](SPRINT-2027.2.1.md) |

## Scope

| Product | Application ID | Application | Roadmap item(s) | Source |
|---|---|---|---|---|
| EIS (PaaS) | 03 | [Marketplace](../applications/03-marketplace.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |
| EIS (PaaS) | 11 | [Training & Knowledge Management](../applications/11-training-knowledge-management.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |
| EIS (PaaS) | 12 | [Support & Service Management](../applications/12-support-service-management.md) | (whole application) | [PO] "eVyoog EIS - Roadmap Initiatives" |

> [PO] assigns **one sprint per EIS application**. It does not say which capabilities or features fall inside this sprint, or whether the application must be finished in it. The EIS scope below is the application's full [WB] breakdown until sprint scope is decided (see [open-decisions.md](../open-decisions.md)).

## EIS 03 Marketplace

**Planned work ([PO] / [WB] description):** Discovery, evaluation, comparison and checkout

Full breakdown with APIs, services, entities and events: [applications/03-marketplace.md](../applications/03-marketplace.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | MVP functions |
|---|---|---|---|
| [03.01 Product Discovery](../applications/03-marketplace.md#0301-product-discovery) | 03.01.01 Catalog Browsing | Browse categories; Search products; Filter products; Sort products | - |
| [03.01 Product Discovery](../applications/03-marketplace.md#0301-product-discovery) | 03.01.02 Recommendations | Recommend products; Show featured products; Show popular products | - |
| [03.02 Product Evaluation](../applications/03-marketplace.md#0302-product-evaluation) | 03.02.01 Evaluation | Start trial; Request demo; Launch sandbox; View prerequisites | - |
| [03.02 Product Evaluation](../applications/03-marketplace.md#0302-product-evaluation) | 03.02.02 Comparison | Compare products; Compare plans; Estimate cost | - |
| [03.03 Marketplace Checkout](../applications/03-marketplace.md#0303-marketplace-checkout) | 03.03.01 Checkout | Select product; Select plan; Configure options; Apply discount; Accept terms; Submit order | - |
| [03.03 Marketplace Checkout](../applications/03-marketplace.md#0303-marketplace-checkout) | 03.03.02 Purchase Validation | Validate eligibility; Validate payment; Validate dependencies | - |
| [03.04 Reviews & Ratings](../applications/03-marketplace.md#0304-reviews--ratings) | 03.04.01 Customer Feedback | Submit review; Rate product; Moderate review; View ratings | - |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - UJ-003 Self-Service Purchase: Marketplace → Checkout → Payment (applications 02, 08, 09; [WB:User Journeys])
  - EVT-005 CheckoutCompleted is consumed by Order and Billing (applications 09, 08; [WB:Events])

### Expected deliverables

- Not specified in any source.
- Implied by [WB:Traceability]: APIs `API-008 POST /v1/checkout`; services Marketplace Service.

## EIS 11 Training & Knowledge Management

**Planned work ([PO] / [WB] description):** Documentation, courses, labs and certifications

Full breakdown with APIs, services, entities and events: [applications/11-training-knowledge-management.md](../applications/11-training-knowledge-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | MVP functions |
|---|---|---|---|
| [11.01 Knowledge Base](../applications/11-training-knowledge-management.md#1101-knowledge-base) | 11.01.01 Knowledge Articles | Create article; Edit article; Publish article; Search article; Version article | - |
| [11.01 Knowledge Base](../applications/11-training-knowledge-management.md#1101-knowledge-base) | 11.01.02 AI Knowledge | Index content; Retrieve relevant content; Validate source | - |
| [11.02 Learning Management](../applications/11-training-knowledge-management.md#1102-learning-management) | 11.02.01 Courses | Create course; Publish course; Enroll user; Track progress; Complete course | - |
| [11.02 Learning Management](../applications/11-training-knowledge-management.md#1102-learning-management) | 11.02.02 Learning Paths | Create learning path; Assign learning path; Track path progress | - |
| [11.03 Training Delivery](../applications/11-training-knowledge-management.md#1103-training-delivery) | 11.03.01 Labs & Assessments | Launch lab; Submit assessment; Score assessment; Track completion | - |
| [11.03 Training Delivery](../applications/11-training-knowledge-management.md#1103-training-delivery) | 11.03.02 Video Learning | Stream video; Track watch progress | - |
| [11.04 Certification](../applications/11-training-knowledge-management.md#1104-certification) | 11.04.01 Certificates | Define certification; Issue certificate; Verify certificate; Expire certificate | - |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - DE-019 KnowledgeArticle and DE-020 Course relate to Product (applications 02; [WB:Data Entities])
  - UJ-008 and UJ-009 use Knowledge (applications 04, 12; [WB:User Journeys])

### Expected deliverables

- Not specified in any source.
- Implied by [WB:Traceability]: APIs `API-017 POST /v1/knowledge/search`; services Knowledge Service.

## EIS 12 Support & Service Management

**Planned work ([PO] / [WB] description):** AI/human support, incidents, requests and SLAs

Full breakdown with APIs, services, entities and events: [applications/12-support-service-management.md](../applications/12-support-service-management.md).

### Capabilities, features and requirement candidates

| Capability | Feature | Functions (requirement candidates from [WB:Functions]) | MVP functions |
|---|---|---|---|
| [12.01 Support](../applications/12-support-service-management.md#1201-support) | 12.01.01 Ticket Management | Create ticket; Categorize ticket; Prioritize ticket; Assign ticket; Escalate ticket; Resolve ticket; Close ticket | - |
| [12.02 AI Support](../applications/12-support-service-management.md#1202-ai-support) | 12.02.01 Conversational Support | Start conversation; Search knowledge; Diagnose issue; Recommend resolution; Execute permitted remediation | - |
| [12.02 AI Support](../applications/12-support-service-management.md#1202-ai-support) | 12.02.02 Human Handoff | Create ticket; Transfer conversation; Provide AI summary | - |
| [12.03 SLA Management](../applications/12-support-service-management.md#1203-sla-management) | 12.03.01 SLA Policy | Define SLA; Assign SLA; Calculate SLA | - |
| [12.03 SLA Management](../applications/12-support-service-management.md#1203-sla-management) | 12.03.02 SLA Monitoring | Monitor SLA; Warn before breach; Escalate breach | - |
| [12.04 Incident & Problem Management](../applications/12-support-service-management.md#1204-incident--problem-management) | 12.04.01 Incident | Log incident; Investigate incident; Resolve incident; Close incident | - |
| [12.04 Incident & Problem Management](../applications/12-support-service-management.md#1204-incident--problem-management) | 12.04.02 Problem | Create problem; Perform root cause analysis; Track corrective action | - |

### Dependencies

- **Stated:** Not specified.
- **Implied by [WB]** (confirm before planning):
  - UJ-008 AI Technical Support and UJ-009 Human Support Escalation (applications 04, 11; [WB:User Journeys])
  - EVT-008 PaymentFailed and EVT-014 ProvisioningFailed are consumed by Support (applications 08, 09; [WB:Events])

### Expected deliverables

- Not specified in any source.
- Implied by [WB:Traceability]: APIs `API-016 POST /v1/support/tickets`; services Support Service.

## Open issues affecting this sprint

- C3: Marketplace (03) is Phase 1/MVP in [CG] and [WB:Roadmap] but is one of the last applications scheduled
- The sprint scope, deliverables, acceptance criteria and dates are not specified in any source.

Details: [open-decisions.md](../open-decisions.md).

## Traceability

```
[PO] roadmap row → SPRINT-2027.1.3 → application page → capability → feature → function (requirement candidate)
   → FRD docs/02-requirements/FRD/<feature>/ (not created) → REQ-<APP-CODE>-<NNN> (not created)
   → STORY-<APP-CODE>-<NNN> with "Sprint (PI.Sprint)" = 2027.1.3
   → code (backend/ · frontend/ · ai-service/) → TC-<APP-CODE>-<NNN> in test-cases/ → UAT
```

Stories for this sprint use `docs/02-requirements/functional-requirements/user-story-template.md` or the GitHub **User Story** issue template, with **Sprint (PI.Sprint) = 2027.1.3**. The Definition of Done is the one in the story template.

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.
