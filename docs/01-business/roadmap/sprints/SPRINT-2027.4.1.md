# SPRINT-2027.4.1

| Field | Value |
|---|---|
| Sprint ID (PI.Sprint) | 2027.4.1 |
| PI – CY Quarter | 2026.4 ⚠ C1 (sprint ID is not inside the stated PI) |
| Start / end dates | Not specified |
| Sprint goal | Not specified |
| Team / capacity | Not specified |
| Status | Not specified |
| Source | [PO] roadmap tables ([`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)) |
| Previous / next sprint | [2027.2.2](SPRINT-2027.2.2.md) · (last) |

## Scope

| Product | Application ID | Application | Roadmap item(s) | Source |
|---|---|---|---|---|
| Thittam (SaaS) | Not specified | [Agile Planner](../applications/thittam-agile-planner.md) | AP-C14 ALM Integration & Traceability, AP-C15 MACRO PLANNER Integration | [PO] "Agile Planner - Roadmap Initiatives" |
| Thittam (SaaS) | Not specified | [Macro Planner](../applications/thittam-macro-planner.md) | #9 Workflow & Automation, #10 Analytics & Reporting | [PO] "Macro Planner - Roadmap Initiatives" |

## Hosted SaaS products in this sprint

These are tracked here so their dependencies on EIS are visible. [PO] lists no functions, requirements or deliverables for them.

| Product | Application | ID | Capability | Features ([PO]) | PI stated |
|---|---|---|---|---|---|
| Thittam (SaaS) | [Macro Planner](../applications/thittam-macro-planner.md) | #9 | Workflow & Automation | Event engine, Rules engine, Workflow engine, Approval engine, Notification engine, Scheduled jobs, Automation actions | 2026.4 ⚠ C1 |
| Thittam (SaaS) | [Macro Planner](../applications/thittam-macro-planner.md) | #10 | Analytics & Reporting | Gantt chart, Critical path, Baseline, Actual vs planned, Schedule variance | 2026.4 ⚠ C1 |
| Thittam (SaaS) | [Agile Planner](../applications/thittam-agile-planner.md) | AP-C14 | ALM Integration & Traceability | Not specified | 2026.4 ⚠ C1 |
| Thittam (SaaS) | [Agile Planner](../applications/thittam-agile-planner.md) | AP-C15 | MACRO PLANNER Integration | Not specified | 2026.4 ⚠ C1 |

**Stated dependencies ([PO]):**
- AP-C15 is itself the integration with Macro Planner.

## Open issues affecting this sprint

- C1: every item in this sprint is stated with PI 2026.4, so sprint ID 2027.4.1 is not inside its PI. Either the sprint is 2026.4.1 or the PI is 2027.4. Confirm before planning
- The sprint scope, deliverables, acceptance criteria and dates are not specified in any source.

Details: [open-decisions.md](../open-decisions.md).

## Traceability

```
[PO] roadmap row → SPRINT-2027.4.1 → application page → capability → feature → function (requirement candidate)
   → FRD docs/02-requirements/FRD/<feature>/ (not created) → REQ-<APP-CODE>-<NNN> (not created)
   → STORY-<APP-CODE>-<NNN> with "Sprint (PI.Sprint)" = 2027.4.1
   → code (backend/ · frontend/ · ai-service/) → TC-<APP-CODE>-<NNN> in test-cases/ → UAT
```

Stories for this sprint use `docs/02-requirements/functional-requirements/user-story-template.md` or the GitHub **User Story** issue template, with **Sprint (PI.Sprint) = 2027.4.1**. The Definition of Done is the one in the story template.

**Sources:**
- **[PO]** [`2026Q3_ProdOps_Plan.docx`](../../source-documents/2026Q3_ProdOps_Plan.docx)
- **[CG]** [`eVyoog_EIS_Platform_-_ChatGPT2.docx`](../../source-documents/eVyoog_EIS_Platform_-_ChatGPT2.docx)
- **[WB]** [`Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`](../../source-documents/Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx)

Full analysis: [`EIS-document-analysis.md`](../EIS-document-analysis.md). Values that no source gives are written **Not specified**.
