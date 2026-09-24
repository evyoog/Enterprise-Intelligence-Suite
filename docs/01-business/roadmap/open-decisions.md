# Open planning decisions

These conflicts and gaps in the planning sources must be resolved before sprint scope is final. The text is taken from [`EIS-document-analysis.md`](EIS-document-analysis.md), sections 3.5 and 6.6. Every sprint document links here.

| Field | Value |
|---|---|
| Status | Open. No decision has been recorded yet |
| Owner | Not specified |

When a decision is made, record it below the relevant item, with the date and who approved it, then update the affected sprint and application pages.

## Conflicts between and within the sources
| # | Conflict | Sources | Effect |
|---|----------|---------|--------|
| C1 | **Sprint ID not inside its PI.** Macro Planner #9 Workflow & Automation and #10 Analytics & Reporting, and Agile Planner AP-C14 ALM Integration & Traceability and AP-C15 MACRO PLANNER Integration, all have **PI 2026.4** but **Sprint 2027.4.1**. | [PO] Tables 4 and 6 | It could be a typo for **2026.4.1**, or it could mean **PI 2027.4**. It must be confirmed. |
| C2 | **MVP scope differs by source.** [CG] Phase 1 has 8 apps (Portal, Catalog, Marketplace, Tenant, IAM, Subscription, Billing, AI Advisor). [WB:Roadmap] Phase 1 **adds** Order/Provisioning, Knowledge, Support, and API/Events/Audit/Observability. [CG] puts those in Phase 2 or 3. | [CG] Suggested MVP; [WB:Roadmap] | The MVP boundary is undefined |
| C3 | **The phase order conflicts with the sprint order.** [CG] and [WB] treat Marketplace (03) and AI Advisor (04) as Phase 1/MVP, but [PO] schedules them **last** (2027.1.3 and 2027.1.2), **after** Phase 2 apps Order & Provisioning (09) and Service & Resource (10) in 2027.1.1. Integration (13) and Analytics (16), which are Phase 3 in [CG], are also in 2027.1.1, **before** Marketplace. | [PO] Table 2 vs [CG] / [WB:Roadmap] | Priorities must be reconciled. **[PO] is the dated plan.** |
| C4 | **MVP flags are inconsistent.** All 69 capabilities and 114 features are MVP=Yes, but only 15 of 444 functions are MVP=Yes, and none of them are in Portal, IAM, Tenant or Marketplace, which [WB:Roadmap] calls the Phase 1 Foundation. | [WB:Capabilities], [WB:Features], [WB:Functions], [WB:Roadmap] | The MVP flags in [WB] cannot be used as they are |
| C5 | **Priority is inconsistent.** Capabilities are all P0, features are all P1, and functions are all P1. [WB:Roadmap] has P0, P1 and P2. | [WB] | Same as C4 |
| C6 | **Roadmap phase is inconsistent.** [WB:Traceability] puts IAM, Tenant and Portal functions in Phase 2, but [WB:Roadmap] makes them Phase 1/MVP and [PO] schedules IAM and Portal in the **first** sprint (2026.3.3). | [WB:Traceability] vs [WB:Roadmap] and [PO] | The Traceability phase column is unreliable |
| C7 | **Order of EIS vs SaaS dependencies.** SW Life Cycle "integrates with" Macro Planner and Agile Planner, and all three start in 2026.3.3. Agile Planner's "MACRO PLANNER Integration" (AP-C15) is scheduled for 2027.4.1 (see C1). | [PO] | The integration is stated but scheduled much later than its consumers |
| C8 | **SaaS products depend on platform capabilities that come later.** Macro Planner, Agile Planner and SW Life Cycle need Organization/Tenant (EIS 05, sprint **2026.4.2**), Subscription (07, **2026.4.3**) and Integration (13, **2027.1.1**), but they start in **2026.3.3**. Their own Organization & Identity / Tenant capabilities are also in 2026.3.3. | [PO] | The SaaS apps must either build their own tenancy early or wait for EIS. This is **Not specified.** |
| C9 | **Who is the Agile/ALM system of record.** [CG] names **Azure DevOps** (Agile and ALM), **Project Online/PWA** and **Macro Planner**. [PO] plans in-house **Agile Planner** and **SW Life Cycle** to provide the same functions. | [CG] Phase 0, Systems of record; [PO] | The engineering toolchain must be decided |
| C10 | **Application 1 has two names.** "Experience & Portal" ([CG] Table 1) and "Experience & Customer Portal" (everywhere else). | [CG] | Minor |
| C11 | **Capability counts differ.** [CG] has 50 and [PO]/[WB] have 69. [CG] also said "~100 capabilities". | [CG] vs [WB] | [WB] is the most complete |
| C12 | **AI flags are inconsistent.** 04.02.01 Sales Assistance and 04.03.01 Technical Guidance are AI=No as features but AI=Yes as functions. 02.02.02 Availability is AI=Yes as a feature but AI=No as functions. 12.01 Ticket Management functions are AI=Yes. | [WB:Features] vs [WB:Functions] | Minor |
| C13 | **Traceability microservice mappings contradict [WB:Microservices]** (see 2.18). | [WB] | Correct them before generating design docs |
| C14 | **API verbs do not match functions.** Create and update functions map to GET endpoints (API-004, API-015). | [WB:Traceability] | Correct them before writing OpenAPI |
| C15 | **"Macro Planner" means two different things.** It is a Thittam SaaS application in [PO], and the portfolio system of record in the [CG] golden path. | [PO] vs [CG] | Consistent in meaning (the same tool), but its role as EIS engineering tooling is **not** in [PO] |


## Decisions needed
1. **C1:** confirm sprint 2027.4.1 against PI 2026.4 (Macro Planner #9 and #10, Agile Planner AP-C14 and AP-C15).
2. **C2 and C3:** confirm the MVP and the order of applications. [PO] sprints and [CG]/[WB] phases disagree.
3. **Sprint scope:** decide which capabilities and features of an application go into its [PO] sprint, and the sprint length and dates.
4. **C4 to C6, C12 to C14:** clean up the [WB] MVP, Priority, AI, Journey, Phase, microservice and API mappings.
5. **C8:** decide whether Thittam and Thiran (starting 2026.3.3) wait for EIS tenancy and subscription (2026.4.2 and 2026.4.3) or use their own.
6. **C9:** decide the ALM system of record: Azure DevOps and Project Online ([CG]), or in-house Agile Planner and SW Life Cycle ([PO]).
7. **Business rules and acceptance criteria:** none exist in any document, and the repository's `CLAUDE.md` requires them before implementation.
