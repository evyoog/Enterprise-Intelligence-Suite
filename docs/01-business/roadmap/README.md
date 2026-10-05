# Roadmap

This folder holds the delivery roadmap for the EIS (PaaS) platform. It is built only from the planning [source documents](../source-documents/).

| File / folder | Purpose |
|---------------|---------|
| [`sprints/`](sprints/) | One `SPRINT-<PI.Sprint>.md` per EIS sprint: scope, capabilities, features, requirement candidates, dependencies, deliverables, open issues and traceability |
| [`applications/`](applications/) | One page per EIS application (16): capability → feature → function, with API, microservice, entity, event, journey and phase |
| [`roadmap.csv`](roadmap.csv) | The 16 EIS roadmap rows, in the column layout of the **Roadmap** sheet in [`../planning-roadmap-template.xlsx`](../planning-roadmap-template.xlsx) |
| [`open-decisions.md`](open-decisions.md) | Conflicts and gaps in the sources that affect the EIS sprints, and the decisions needed before sprint scope is final |
| [`EIS-document-analysis.md`](EIS-document-analysis.md) | The full analysis of the three source documents and all 18 workbook sheets |
| [`project-status-report.md`](project-status-report.md) | Verified project status (2026-10-05): completed / partial / not started, gaps, FRD status, decisions, questions, Gantt chart, roadmap and completion % |

## Conventions
- **PI:** `YYYY.Quarter`, for example `2026.3`.
- **Sprint:** `PI.SprintNumber`, for example `2026.3.3`. Both formats come from `planning-roadmap-template.xlsx` and match the "Sprint (PI.Sprint)" field in the story template and the GitHub issue template.
- **IDs:** EIS application, capability, feature and function IDs are the workbook's numeric IDs (`06`, `06.01`, `06.01.02`, `06.01.02.01`). Each application also has a code from [DN-5](open-decisions.md#dn-5-application-codes) (for example `APP-IAM`), and requirement, feature, story and test IDs use it without the `APP-` prefix (for example `REQ-IAM-001`, `TC-IAM-001`).
- **Dates, MVP and priority:** sprints are calendar months; each sprint commits its P0 (MVP) capabilities and P1 is stretch scope ([DN-2](open-decisions.md#dn-2-sprint-scope-length-and-dates), [C4](open-decisions.md#c4), [C5](open-decisions.md#c5)). Application 01 is named "Enterprise Intelligence Suite" ([C10](open-decisions.md#c10)).
- **Missing values:** anything the sources do not give is written **Not specified**. Inferences are labelled as such.

## Where sprints fit in the SDLC
```
source-documents ([PO] [CG] [WB])
      │
      ▼
roadmap/ ── sprints/SPRINT-<PI.N>.md ──► applications/<NN>-<app>.md ──► capability ─► feature ─► function
                                                                                        │
      ┌─────────────────────────────────────────────────────────────────────────────────┘
      ▼
docs/02-requirements/FRD/<feature>/  →  REQ-<CODE>-<NNN>  →  STORY (Sprint = PI.N)
      →  backend/ · frontend/ · ai-service/  →  test-cases/…/TC-<CODE>-<NNN>  →  UAT  →  docs/11-release-notes/
```

## EIS applications by PI and sprint ([PO])
| PI | Sprint | Dates | Applications |
|----|--------|-------|--------------|
| 2026.3 | [2026.3.3](sprints/SPRINT-2026.3.3.md) | 1–30 Sep 2026 | [01 Enterprise Intelligence Suite](applications/01-enterprise-intelligence-suite.md), [06 Identity & Access Management](applications/06-identity-access-management.md) |
| 2026.4 | [2026.4.1](sprints/SPRINT-2026.4.1.md) | 1–31 Oct 2026 | [02 Product & Catalog Management](applications/02-product-catalog-management.md) |
| 2026.4 | [2026.4.2](sprints/SPRINT-2026.4.2.md) | 1–30 Nov 2026 | [05 Customer / Tenant Management](applications/05-customer-tenant-management.md) |
| 2026.4 | [2026.4.3](sprints/SPRINT-2026.4.3.md) | 1–31 Dec 2026 | [07 Subscription & Entitlement Management](applications/07-subscription-entitlement-management.md), [08 Billing & Payments](applications/08-billing-payments.md) |
| 2027.1 | [2027.1.1](sprints/SPRINT-2027.1.1.md) | 1–31 Jan 2027 | [09 Order & Provisioning Management](applications/09-order-provisioning-management.md), [10 Service & Resource Management](applications/10-service-resource-management.md), [13 Integration & API Platform](applications/13-integration-api-platform.md), [16 Analytics & Data Platform](applications/16-analytics-data-platform.md) |
| 2027.1 | [2027.1.2](sprints/SPRINT-2027.1.2.md) | 1–28 Feb 2027 | [04 AI Advisor & Agent Platform](applications/04-ai-advisor-agent-platform.md) |
| 2027.1 | [2027.1.3](sprints/SPRINT-2027.1.3.md) | 1–31 Mar 2027 | [03 Marketplace](applications/03-marketplace.md), [11 Training & Knowledge Management](applications/11-training-knowledge-management.md), [12 Support & Service Management](applications/12-support-service-management.md) |
| 2027.2 | [2027.2.1](sprints/SPRINT-2027.2.1.md) | 1–30 Apr 2027 | [14 Partner & Provider Management](applications/14-partner-provider-management.md) |
| 2027.2 | [2027.2.2](sprints/SPRINT-2027.2.2.md) | 1–31 May 2027 | [15 Administration & Governance](applications/15-administration-governance.md) |

The same schedule, grouped by PI, is in [`sprints/README.md`](sprints/README.md).

## Keeping this up to date
1. When the plan changes, add the new source document to [`../source-documents/`](../source-documents/).
2. Update the affected `SPRINT-*.md`, application pages and `roadmap.csv`.
3. Record resolved conflicts in [`open-decisions.md`](open-decisions.md).
4. When a feature is approved, create its FRD in `docs/02-requirements/FRD/<feature>/` and link it from the application page.
