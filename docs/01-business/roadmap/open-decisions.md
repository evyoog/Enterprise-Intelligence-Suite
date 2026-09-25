# Open planning decisions

These conflicts and gaps in the planning sources affect the EIS (PaaS) sprints and must be resolved before sprint scope is final. The text is taken from [`EIS-document-analysis.md`](EIS-document-analysis.md), sections 3.5 and 6.6. Every sprint document links here.

| Field | Value |
|---|---|
| Status | Open. No decision has been recorded yet |
| Owner | Not specified |

When a decision is made, record it below the relevant item, with the date and who approved it, then update the affected sprint and application pages.

## Conflicts between and within the sources
IDs match section 3.5 of the analysis. C1, C7, C8, C9 and C15 are left out because they concern only the hosted SaaS products (Thittam and Thiran), not the EIS (PaaS) sprints.

| # | Conflict | Sources | Effect |
|---|----------|---------|--------|
| C2 | **MVP scope differs by source.** [CG] Phase 1 has 8 apps (Portal, Catalog, Marketplace, Tenant, IAM, Subscription, Billing, AI Advisor). [WB:Roadmap] Phase 1 **adds** Order/Provisioning, Knowledge, Support, and API/Events/Audit/Observability. [CG] puts those in Phase 2 or 3. | [CG] Suggested MVP; [WB:Roadmap] | The MVP boundary is undefined |
| C3 | **The phase order conflicts with the sprint order.** [CG] and [WB] treat Marketplace (03) and AI Advisor (04) as Phase 1/MVP, but [PO] schedules them **last** (2027.1.3 and 2027.1.2), **after** Phase 2 apps Order & Provisioning (09) and Service & Resource (10) in 2027.1.1. Integration (13) and Analytics (16), which are Phase 3 in [CG], are also in 2027.1.1, **before** Marketplace. | [PO] Table 2 vs [CG] / [WB:Roadmap] | Priorities must be reconciled. **[PO] is the dated plan.** |
| C4 | **MVP flags are inconsistent.** All 69 capabilities and 114 features are MVP=Yes, but only 15 of 444 functions are MVP=Yes, and none of them are in Portal, IAM, Tenant or Marketplace, which [WB:Roadmap] calls the Phase 1 Foundation. | [WB:Capabilities], [WB:Features], [WB:Functions], [WB:Roadmap] | The MVP flags in [WB] cannot be used as they are |
| C5 | **Priority is inconsistent.** Capabilities are all P0, features are all P1, and functions are all P1. [WB:Roadmap] has P0, P1 and P2. | [WB] | Same as C4 |
| C6 | **Roadmap phase is inconsistent.** [WB:Traceability] puts IAM, Tenant and Portal functions in Phase 2, but [WB:Roadmap] makes them Phase 1/MVP and [PO] schedules IAM and Portal in the **first** sprint (2026.3.3). | [WB:Traceability] vs [WB:Roadmap] and [PO] | The Traceability phase column is unreliable |
| C10 | **Application 1 has two names.** "Experience & Portal" ([CG] Table 1) and "Experience & Customer Portal" (everywhere else). | [CG] | Minor |
| C11 | **Capability counts differ.** [CG] has 50 and [PO]/[WB] have 69. [CG] also said "~100 capabilities". | [CG] vs [WB] | [WB] is the most complete |
| C12 | **AI flags are inconsistent.** 04.02.01 Sales Assistance and 04.03.01 Technical Guidance are AI=No as features but AI=Yes as functions. 02.02.02 Availability is AI=Yes as a feature but AI=No as functions. 12.01 Ticket Management functions are AI=Yes. | [WB:Features] vs [WB:Functions] | Minor |
| C13 | **Traceability microservice mappings contradict [WB:Microservices]** (see 2.18). | [WB] | Correct them before generating design docs |
| C14 | **API verbs do not match functions.** Create and update functions map to GET endpoints (API-004, API-015). | [WB:Traceability] | Correct them before writing OpenAPI |


## Sprint 2026.3.3: functions not specified or dependent on later sprints
Raised from the sprint 2026.3.3 development plan. These functions are in the sprint scope but are not being built now. Each needs a decision before any work starts.

| # | Function | Why it is not built now | Status |
|---|----------|-------------------------|--------|
| C16 | 01.03.01 Semantic search | The approach, engine and data sources are Not specified | Open |
| C17 | 01.03.01 Unified search beyond products | Documentation and support search need Apps 11 and 12 (sprint 2027.1.3) | Open |
| C18 | 01.04.02 Send SMS | No SMS provider or rules are specified | Open |
| C19 | 01.02.01 View spending | Needs Billing (App 08, sprint 2026.4.3). The dashboard already shows a "not available" note | Open |
| C20 | 01.02.02 View incidents; per-product service status | Needs Incident management (App 12, sprint 2027.1.3) and product health monitoring, which is Not specified | Open |
| C21 | 06.02.02 General policy engine (beyond the MFA policy) | Policy Management is App 15 (sprint 2027.2.2). Requirements are Not specified | Open |
| C22 | 06.04.01 Configure OIDC (per-organization identity provider) | Only SAML is specified and built. OIDC identity-provider federation requirements are Not specified | Open |
| C23 | 06.04.01 Map claims (configurable per provider) | The current fixed attribute mapping works. Configurable mapping is Not specified | Open |
| C24 | 06.03.01 Request elevated access: how the requester chooses the permission | The backend accepts any permission name that some role grants, but there is no endpoint listing requestable permissions for a regular user (`GET /me/permissions` returns only the caller's own). The UI control is Not specified. Blocks the request form in FRD `privileged-access` | Open |

## Decisions needed
1. **C2 and C3:** confirm the MVP and the order of applications. [PO] sprints and [CG]/[WB] phases disagree.
2. **Sprint scope:** decide which capabilities and features of an application go into its [PO] sprint, and the sprint length and dates.
3. **C4 to C6, C12 to C14:** clean up the [WB] MVP, Priority, AI, Journey, Phase, microservice and API mappings.
4. **Business rules and acceptance criteria:** none exist in any document, and the repository's `CLAUDE.md` requires them before implementation.
5. **Application codes:** assign `APP-<CODE>` codes to the 16 EIS applications so requirements and stories can use `REQ-`, `FTR-` and `STORY-` IDs. The only example in the templates is `APP-CAT`.
