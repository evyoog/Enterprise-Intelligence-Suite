# Roadmap

This folder holds the delivery roadmap for the EIS platform and the SaaS products it hosts. It is built only from the planning [source documents](../source-documents/).

| File / folder | Purpose |
|---------------|---------|
| [`sprints/`](sprints/) | One `SPRINT-<PI.Sprint>.md` per sprint: scope, capabilities, features, requirement candidates, dependencies, deliverables, open issues and traceability |
| [`applications/`](applications/) | One page per application: capability → feature → function, with API, microservice, entity, event, journey and phase |
| [`roadmap.csv`](roadmap.csv) | All 72 roadmap rows, in the column layout of the **Roadmap** sheet in [`../planning-roadmap-template.xlsx`](../planning-roadmap-template.xlsx), so they can be imported into Macro Planner later |
| [`open-decisions.md`](open-decisions.md) | Conflicts and gaps in the sources (C1–C15) and the decisions needed before sprint scope is final |
| [`EIS-document-analysis.md`](EIS-document-analysis.md) | The full analysis of the three source documents and all 18 workbook sheets |

## Conventions
- **PI:** `YYYY.Quarter`, for example `2026.3`.
- **Sprint:** `PI.SprintNumber`, for example `2026.3.3`. Both formats come from `planning-roadmap-template.xlsx` and match the "Sprint (PI.Sprint)" field in the story template and the GitHub issue template.
- **IDs:** EIS application, capability, feature and function IDs are the workbook's numeric IDs (`06`, `06.01`, `06.01.02`, `06.01.02.01`). The SaaS capability IDs are the ones in [PO] (`#1`, `AP-C01`, `SWLC-CAP-01`). `APP-<CODE>` / `FTR-<APP-CODE>-<NNN>` codes are **not yet assigned** in any source (see [open-decisions.md](open-decisions.md)).
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
docs/02-requirements/FRD/<feature>/  →  REQ-<APP-CODE>-<NNN>  →  STORY (Sprint = PI.N)
      →  backend/ · frontend/ · ai-service/  →  test-cases/…/TC-<APP-CODE>-<NNN>  →  UAT  →  docs/11-release-notes/
```

## EIS applications by PI and sprint ([PO])
| PI | Sprint | Applications |
|----|--------|--------------|
| 2026.3 | [2026.3.3](sprints/SPRINT-2026.3.3.md) | [01 Experience & Customer Portal](applications/01-experience-customer-portal.md), [06 Identity & Access Management](applications/06-identity-access-management.md) |
| 2026.4 | [2026.4.1](sprints/SPRINT-2026.4.1.md) | [02 Product & Catalog Management](applications/02-product-catalog-management.md) |
| 2026.4 | [2026.4.2](sprints/SPRINT-2026.4.2.md) | [05 Customer / Tenant Management](applications/05-customer-tenant-management.md) |
| 2026.4 | [2026.4.3](sprints/SPRINT-2026.4.3.md) | [07 Subscription & Entitlement Management](applications/07-subscription-entitlement-management.md), [08 Billing & Payments](applications/08-billing-payments.md) |
| 2027.1 | [2027.1.1](sprints/SPRINT-2027.1.1.md) | [09 Order & Provisioning Management](applications/09-order-provisioning-management.md), [10 Service & Resource Management](applications/10-service-resource-management.md), [13 Integration & API Platform](applications/13-integration-api-platform.md), [16 Analytics & Data Platform](applications/16-analytics-data-platform.md) |
| 2027.1 | [2027.1.2](sprints/SPRINT-2027.1.2.md) | [04 AI Advisor & Agent Platform](applications/04-ai-advisor-agent-platform.md) |
| 2027.1 | [2027.1.3](sprints/SPRINT-2027.1.3.md) | [03 Marketplace](applications/03-marketplace.md), [11 Training & Knowledge Management](applications/11-training-knowledge-management.md), [12 Support & Service Management](applications/12-support-service-management.md) |
| 2027.2 | [2027.2.1](sprints/SPRINT-2027.2.1.md) | [14 Partner & Provider Management](applications/14-partner-provider-management.md) |
| 2027.2 | [2027.2.2](sprints/SPRINT-2027.2.2.md) | [15 Administration & Governance](applications/15-administration-governance.md) |

The hosted SaaS schedule (Macro Planner, Agile Planner, SW Life Cycle) is in [`sprints/README.md`](sprints/README.md). The sprint `2027.4.1` has an unconfirmed PI (C1).

## Keeping this up to date
1. When the plan changes, add the new source document to [`../source-documents/`](../source-documents/).
2. Update the affected `SPRINT-*.md`, application pages and `roadmap.csv`.
3. Record resolved conflicts in [`open-decisions.md`](open-decisions.md).
4. When a feature is approved, create its FRD in `docs/02-requirements/FRD/<feature>/` and link it from the application page.
