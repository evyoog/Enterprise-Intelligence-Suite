# Open planning decisions

These conflicts and gaps in the planning sources affect the EIS (PaaS) sprints. The conflict text is taken from [`EIS-document-analysis.md`](EIS-document-analysis.md), sections 3.5 and 6.6. Every sprint document links here.

| Field | Value |
|---|---|
| Status | **Decided.** All items below were decided on 2026-09-25 |
| Owner | Product owner |
| Approved by / on | Product owner / 2026-09-25 (decision session) |

**Sources considered:** [PO] `2026Q3_ProdOps_Plan.docx`, [CG] `eVyoog_EIS_Platform_-_ChatGPT2.docx`, [WB] `Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx` (all in [`../source-documents/`](../source-documents/)), and **[SUM] `EIS_Platform_Summary.docx`** (the shared project summary, 2026-09-18; not yet in `source-documents/`). Every decision was checked against all four. Where [SUM] confirms or refines a decision, its record says so.

Each decision is recorded in [Decision record](#decision-record) below its item ID. When a decision is changed, update its record with the new date and approver, then update the affected sprint and application pages.

## Conflicts between and within the sources
IDs match section 3.5 of the analysis. C1, C7, C8, C9 and C15 are left out because they concern only the hosted SaaS products (Thittam and Thiran), not the EIS (PaaS) sprints.

| # | Conflict | Sources | Effect | Status |
|---|----------|---------|--------|--------|
| C2 | **MVP scope differs by source.** [CG] Phase 1 has 8 apps (Portal, Catalog, Marketplace, Tenant, IAM, Subscription, Billing, AI Advisor). [WB:Roadmap] Phase 1 **adds** Order/Provisioning, Knowledge, Support, and API/Events/Audit/Observability. [CG] puts those in Phase 2 or 3. | [CG] Suggested MVP; [WB:Roadmap] | The MVP boundary is undefined | Decided 2026-09-25 ([record](#c2)) |
| C3 | **The phase order conflicts with the sprint order.** [CG] and [WB] treat Marketplace (03) and AI Advisor (04) as Phase 1/MVP, but [PO] schedules them **last** (2027.1.3 and 2027.1.2), **after** Phase 2 apps Order & Provisioning (09) and Service & Resource (10) in 2027.1.1. Integration (13) and Analytics (16), which are Phase 3 in [CG], are also in 2027.1.1, **before** Marketplace. | [PO] Table 2 vs [CG] / [WB:Roadmap] | Priorities must be reconciled. **[PO] is the dated plan.** | Decided 2026-09-25 ([record](#c3)) |
| C4 | **MVP flags are inconsistent.** All 69 capabilities and 114 features are MVP=Yes, but only 15 of 444 functions are MVP=Yes, and none of them are in Portal, IAM, Tenant or Marketplace, which [WB:Roadmap] calls the Phase 1 Foundation. | [WB:Capabilities], [WB:Features], [WB:Functions], [WB:Roadmap] | The MVP flags in [WB] cannot be used as they are | Decided 2026-09-25 ([record](#c4)) |
| C5 | **Priority is inconsistent.** Capabilities are all P0, features are all P1, and functions are all P1. [WB:Roadmap] has P0, P1 and P2. | [WB] | Same as C4 | Decided 2026-09-25 ([record](#c5)) |
| C6 | **Roadmap phase is inconsistent.** [WB:Traceability] puts IAM, Tenant and Portal functions in Phase 2, but [WB:Roadmap] makes them Phase 1/MVP and [PO] schedules IAM and Portal in the **first** sprint (2026.3.3). | [WB:Traceability] vs [WB:Roadmap] and [PO] | The Traceability phase column is unreliable | Decided 2026-09-25 ([record](#c6)) |
| C10 | **Application 1 has two names.** "Experience & Portal" ([CG] Table 1) and "Experience & Customer Portal" (everywhere else). | [CG] | Minor | Decided 2026-09-25 ([record](#c10)) |
| C11 | **Capability counts differ.** [CG] has 50 and [PO]/[WB] have 69. [CG] also said "~100 capabilities". | [CG] vs [WB] | [WB] is the most complete | Decided 2026-09-25 ([record](#c11)) |
| C12 | **AI flags are inconsistent.** 04.02.01 Sales Assistance and 04.03.01 Technical Guidance are AI=No as features but AI=Yes as functions. 02.02.02 Availability is AI=Yes as a feature but AI=No as functions. 12.01 Ticket Management functions are AI=Yes. | [WB:Features] vs [WB:Functions] | Minor | Decided 2026-09-25 ([record](#c12)) |
| C13 | **Traceability microservice mappings contradict [WB:Microservices]** (see 2.18). | [WB] | Correct them before generating design docs | Decided 2026-09-25 ([record](#c13)) |
| C14 | **API verbs do not match functions.** Create and update functions map to GET endpoints (API-004, API-015). | [WB:Traceability] | Correct them before writing OpenAPI | Decided 2026-09-25 ([record](#c14)) |

## Sprint 2026.3.3: functions not specified or dependent on later sprints
Raised from the sprint 2026.3.3 development plan.

| # | Function | Why it is not built now | Status |
|---|----------|-------------------------|--------|
| C16 | 01.03.01 Semantic search | The approach, engine and data sources are Not specified | Decided 2026-09-25 ([record](#c16)) |
| C17 | 01.03.01 Unified search beyond products | Documentation and support search need Apps 11 and 12 (sprint 2027.1.3) | Decided 2026-09-25 ([record](#c17)) |
| C18 | 01.04.02 Send SMS | No SMS provider or rules are specified | Decided 2026-09-25 ([record](#c18)) |
| C19 | 01.02.01 View spending | Needs Billing (App 08, sprint 2026.4.3). The dashboard already shows a "not available" note | Decided 2026-09-25 ([record](#c19)) |
| C20 | 01.02.02 View incidents; per-product service status | Needs Incident management (App 12, sprint 2027.1.3) and product health monitoring, which is Not specified | Decided 2026-09-25 ([record](#c20)) |
| C21 | 06.02.02 General policy engine (beyond the MFA policy) | Policy Management is App 15 (sprint 2027.2.2). Requirements are Not specified | Decided 2026-09-25 ([record](#c21)) |
| C22 | 06.04.01 Configure OIDC (per-organization identity provider) | Only SAML is specified and built. OIDC identity-provider federation requirements are Not specified | Decided 2026-09-25 ([record](#c22)) |
| C23 | 06.04.01 Map claims (configurable per provider) | The current fixed attribute mapping works. Configurable mapping is Not specified | Decided 2026-09-25 ([record](#c23)) |
| C24 | 06.03.01 Request elevated access: how the requester chooses the permission | The backend accepts any permission name that some role grants, but there is no endpoint listing requestable permissions for a regular user (`GET /me/permissions` returns only the caller's own). The UI control is Not specified. Blocks the request form in FRD `privileged-access` | Decided 2026-09-25 ([record](#c24)) |
| C25 | 05.01.01 Organization Lifecycle on `/admin/registrations`: meaning of delete, scope, create, timing | Sprint 2026.4.2 scope with no FRD; "delete" vs the roadmap's "Close organization" was Not specified | Decided 2026-09-25 ([record](#c25)) |
| C26 | 01.02.02 Service status page (REQ-PRT-001): status values, visibility, posting permission, setting, placement | The five open questions in FRD `service-status-page` | Decided 2026-09-26 ([record](#c26)) |
| C27 | 06.04.01 Configure OIDC (REQ-IAM-006): provider fields, login flow, secret storage, one-provider rule, paths | The open questions in FRD `oidc-federation` | Decided 2026-09-26 ([record](#c27)) |
| C28 | 06.04.01 Map claims (REQ-IAM-007): OIDC defaults, order, fallbacks | The open questions in FRD `claim-mapping` | Decided 2026-09-26 ([record](#c28)) |
| C29 | 06.02.02 Organization MFA policy on SAML sign-in, and members with no authenticator | Found in the 2026-09-26 sprint audit: SAML sign-in skipped the policy; SAML users have no known password, so they could not enrol | Decided 2026-09-26 ([record](#c29)) |
| C30 | 06.01.02 Recover MFA: who may reset another user's MFA | Recovery codes were the only path; admin reset was Not specified | Decided 2026-09-26 ([record](#c30)) |

## Decisions needed
1. **C2 and C3:** confirm the MVP and the order of applications. → Decided 2026-09-25: see [C2](#c2) and [C3](#c3).
2. **Sprint scope:** decide which capabilities and features of an application go into its [PO] sprint, and the sprint length and dates. → Decided 2026-09-25: see [DN-2](#dn-2-sprint-scope-length-and-dates).
3. **C4 to C6, C12 to C14:** clean up the [WB] MVP, Priority, AI, Journey, Phase, microservice and API mappings. → Decided 2026-09-25: see [C4](#c4), [C5](#c5), [C6](#c6), [C12](#c12), [C13](#c13), [C14](#c14).
4. **Business rules and acceptance criteria:** none exist in any document, and the repository's `CLAUDE.md` requires them before implementation. → Decided 2026-09-25: see [DN-4](#dn-4-business-rules-and-acceptance-criteria).
5. **Application codes:** assign `APP-<CODE>` codes to the 16 EIS applications so requirements and stories can use `REQ-`, `FTR-` and `STORY-` IDs. The only example in the templates is `APP-CAT`. → Decided 2026-09-25: see [DN-5](#dn-5-application-codes).

---

## Decision record

### C2
**Decision:** The MVP is the [WB:Roadmap] **Phase 1** scope: Enterprise Intelligence Suite (01), Catalog (02), Marketplace (03), AI Advisor (04), Tenant (05), IAM (06), Subscription (07), Billing (08), Order & Provisioning (09), Knowledge (11), Support (12), and the platform foundation (API, events, audit, observability).
**Reason:** The [CG] 8-app MVP lets customers buy but never receive a product (breaks P0 journeys UJ-003 and UJ-005). Audit is already built and NFR-007 is Critical. [PO] schedules every Phase 1 application by sprint 2027.1.3.
**[SUM]:** Confirms. [SUM]'s Phase 1 exit criterion, "a real customer completes buy → pay → provision → 'it works' unattended", requires Order & Provisioning in the MVP.

### C3
**Decision:** The [PO] sprint order is **authoritative** and is not re-sequenced. Phases define only what is in the MVP (C2). The MVP is complete at the end of sprint **2027.1.3**.
**Reason:** [PO] is the only dated plan. Every MVP application is scheduled by 2027.1.3, so the order no longer changes the MVP content. Dependency-ordering risks are handled inside each sprint's planning.
**[SUM]:** [SUM] ("Q3 2026 – Q1 2027 Delivery Schedule") flagged that the dated schedule places the AI Advisor before a fully proven transactional loop and said "either the principle should flex, or this schedule should". This decision resolves it: **the schedule stands and the principle flexes**, with the agent safety controls in C21 made P0 to compensate.

### C4
**Decision:** MVP is set at **application level from C2**, using the capability splits that [WB:Roadmap] Phase 1 itself makes. The [WB] MVP flags are ignored.

| Application | MVP capabilities |
|---|---|
| 01, 02, 03, 05, 06, 07, 08, 09 | All capabilities |
| 04 AI Advisor & Agent Platform | 04.01 AI Product Advisor, 04.04 AI Support Agent; from 04.05 AI Agent Orchestration, the functions "Apply guardrails" and "Audit agent action" (see C21) |
| 11 Training & Knowledge Management | 11.01 Knowledge Base |
| 12 Support & Service Management | 12.01 Support, 12.02 AI Support |
| 13 Integration & API Platform | 13.01 API Management, 13.03 Event Platform |
| 15 Administration & Governance | 15.03 Audit |
| 10, 14, 16 | None (not MVP) |

**Reason:** Follows C2 without editing the read-only source documents.
**[SUM]:** Keeping 15.03 Audit in the MVP matches [SUM]'s rule that every AI tool call is audit-logged ("Security & Governance Model").

### C5
**Decision:** **P0** = MVP scope (C4). **P1** = all other [WB:Roadmap] Phase 2 and Phase 3 scope. **P2** = "AI Autonomous Operations" (as [WB:Roadmap] marks it). The [WB] capability, feature and function priority columns are ignored.
**Reason:** Reuses the only consistent priority data in the sources and stays consistent with C4.
**Addendum (decided 2026-09-25, product owner):** 12.04 Incident & Problem Management, which [WB:Roadmap] places in no phase, is **P0 (Commit)**, so that the C20 retirement task in sprint 2027.1.3 is guaranteed. Its MVP and phase values under C4 and C6 are unchanged.

### C6
**Decision:** Phase comes from **[WB:Roadmap]**, mapped to capabilities as in C4. The [WB:Traceability] "Roadmap Phase" column is ignored. Application pages show the derived phase instead of the Traceability value.
**Reason:** One consistent source for phase, MVP (C4) and priority (C5).

### C10
**Decision:** The official name of application 01 is **"Enterprise Intelligence Suite"**.
**Note:** This is the same name as the platform (EIS), which is how [PO], [SUM] and `CLAUDE.md` use it. Where both could be meant, write "application 01 Enterprise Intelligence Suite" or use its code `APP-PRT` (DN-5). Pages that still say "Experience & Customer Portal" are to be updated.

### C11
**Decision:** The capability baseline is the **69 capabilities** in [PO] Table 1 and [WB:Capabilities].
**Reason:** It is the dated plan's own list and is already used by every sprint and application page. The [CG] 50-capability list is superseded; the "~100" figure is not used.

### C12
**Decision:** Whether a feature requires AI is **decided and justified in that feature's FRD**. The [WB] AI flags are informational only.
**Reason:** Neither the feature-level nor the function-level flags are reliable; the FRD approval step already exists.

### C13
**Decision:** [WB:Microservices] defines the **logical domain boundaries**. Each domain is built as a **module in the existing single backend** (`backend/…/modules/<domain>/`), not as a separate deployable service. The [WB:Traceability] microservice column is ignored. Each feature's design document names its module.
**Reason:** Matches the implemented architecture and the `CLAUDE.md` backend rules; keeps the workbook's domain split for future extraction.
**[SUM] reconciliation (confirmed 2026-09-25):** [SUM] ("Containerization & CI/CD Strategy") assumes a Dockerfile per microservice on EKS. Under this decision, [SUM]'s container rules apply **per deployable**, not per microservice: today one image each for the backend, the frontend and `ai-service`. Each image is built once and promoted unchanged through DEV → QA → STAGING → PRODUCTION, scanned in ECR before promotion, and tagged with its canonical requirement ID. A domain module becomes a separate container only through a new, recorded decision (for example, when it must scale independently).

### C14
**Decision:** The [WB] API catalog is **illustrative only**. Each feature's endpoints are defined in its FRD `api-requirements.md` using standard REST verbs (GET read, POST create, PUT/PATCH update, DELETE remove). For built features, the **implemented endpoints are the source of truth**. The OpenAPI specification is generated by springdoc into `docs/06-api/`.
**Reason:** The implemented controllers already use correct verbs; springdoc is already a backend dependency.
**[SUM]:** Consistent with [SUM]'s rule that existing REST endpoints become MCP tools directly, so the API catalog and the MCP tool catalog stay two views of the same endpoints.

### C16
**Decision:** Semantic search is **deferred to sprint 2027.1.3**, with Knowledge (11), which owns semantic retrieval (MS-016). Keyword search satisfies 01.03.01 for sprint 2026.3.3.

### C17
**Decision:** Global search grows **incrementally**. It stays products-only in sprint 2026.3.3. Knowledge articles (11) and support tickets (12) are added to global search in their own sprint, **2027.1.3**.

### C18
**Decision:** Send SMS is **deferred until an SMS provider and rules** (phone numbers, verification, consent, notification categories) are specified in a new FRD. Email and in-app notifications satisfy 01.04.02 for sprint 2026.3.3.

### C19
**Decision:** View spending is **deferred to sprint 2026.4.3**, with Billing & Payments (08). The dashboard keeps its current "not available" note until then.

### C20
**Decision:** Build a **simple interim status page in sprint 2026.3.3**: platform admins post per-product status and incidents manually, and customers can view them. It is replaced by real sources later:
- Per-product status → Health Monitoring (10.04), sprint **2027.1.1**.
- Incidents → Incident & Problem Management (12.04), sprint **2027.1.3**.

**Retirement:** the interim page is controlled by a single on/off configuration setting. Sprints 2027.1.1 and 2027.1.3 each carry a retirement task to switch their part to the real source; the interim page is removed after sprint 2027.1.3.
**Conditions:** requires an Approved FRD before build (DN-4). Its fields (product, status values, incident title and message, start and end time, visibility) are to be defined in that FRD.
**Addendum (decided 2026-09-25, product owner):** 12.04 Incident & Problem Management is **P0 (Commit)** (see C5), so the incident part of the interim page is retired on schedule in sprint 2027.1.3.

### C21
**Decision:** In sprint 2026.3.3, 06.02.02 Policy means the **organization MFA policy** (enforced by `MfaPolicyService`; frontend toggle pending). A general policy engine is **deferred to sprint 2027.2.2**, with Policy Management (15.02), where its requirements are defined.
**[SUM] extension (confirmed 2026-09-25):** AI agents launch with the AI Platform in sprint **2027.1.2**, before the policy engine. The following are therefore **P0 in sprint 2027.1.2**, taken from [SUM] "Security & Governance Model" and "Per-agent scoping":
- **Identity forwarding:** every agent tool call carries the acting user's identity as a scoped, short-lived token; the backend applies its normal permission check.
- **Tool allowlists per agent**, fixed at registration.
- **Human approval** for high-risk writes above a defined threshold.
- **Audit** of every agent tool call (agent identity, acting user, tool, arguments).
- **Per-agent scoping** for the MVP agents, as in [SUM]: the Advisory agent writes order drafts only (no order submission, no payment tools); the Support agent writes tickets only (no billing or payment tools, no account deletion).
- From 04.05 AI Agent Orchestration: "Apply guardrails" and "Audit agent action" (see C4).

### C22
**Decision:** Per-organization **OIDC identity-provider federation is built in sprint 2026.3.3**, in the backend `federation` module, **mirroring the SAML design** (per-organization providers, enable/disable, test).
**[SUM]:** [SUM]'s earlier 7-phase roadmap placed SSO in Phase 3. [PO] schedules all of IAM in sprint 2026.3.3 and is authoritative (C3). [SUM] also mentions SCIM; SCIM is not a function in [PO] or [WB] and is **not** part of this decision.
**Conditions:** requires an Approved FRD before build (DN-4), covering provider fields, login flow, and client-secret handling. Client secrets are never hard-coded (`CLAUDE.md` rule 7) and are encrypted at rest; the FRD decides whether to reuse the existing MFA-secret encryption approach.

### C23
**Decision:** Claim mapping becomes **configurable per identity provider, for both SAML and OIDC**, for the same four details the code already reads: **email, first name, last name, display name**. Defaults equal the current behavior, so existing SAML providers are unchanged. **Role mapping stays out of scope**: federated users still join as `MEMBER`. Built in sprint 2026.3.3.
**Conditions:** requires an Approved FRD before build (DN-4).

### C24
**Decision:** Add **one read-only backend endpoint listing the permissions a user may request** (for example `GET /me/privileged-access/requestable-permissions`). It applies only the rules `PrivilegedAccessService` already enforces: the permission is granted by some role; it is not `MANAGE_PRIVILEGED_ACCESS`; organization-scope permissions require organization membership. The request form shows the result as a dropdown.
**Conditions:** specified in FRD `privileged-access` (DN-4). No new rules are added.

### C25
**Decision (product owner, 2026-09-25):** On `/admin/registrations`, platform administrators get **edit, suspend, activate and close for organizations only**. **Close is a soft close**: the organization's status becomes CLOSED, data and audit history are kept, active members' Keycloak logins are disabled and their sessions ended, and it can be activated again. **No create** from the admin page: organizations still come only from self-registration. No hard delete. Individual customers are unchanged. 05.01.01.02–.05 are **pulled forward from 2026.4.2** and built in 2026.3.3; 05.01.01.01 Create organization stays in 2026.4.2.
**Conditions:** FRD [`organization-lifecycle`](../../02-requirements/FRD/organization-lifecycle/requirement.md) (REQ-TEN-001), approved the same day.

### C26
**Decision (product owner, 2026-09-26):** Service status page (REQ-PRT-001).
- **Status values:** Operational, Degraded, Partial outage, Major outage, Maintenance.
- **Visibility:** every signed-in customer sees the status of every product. Incident details (title, message, times) open only for products the customer's organization or account has purchased.
- **Posting:** a new platform permission `MANAGE_SERVICE_STATUS`, granted to the `ADMIN` role.
- **Setting:** `app.status-page.enabled`, on by default.
- **Placement:** `/status` in the signed-in sidebar, linked from the dashboard's Service Health card; the admin screen is `/admin/service-status`.

### C27
**Decision (product owner, 2026-09-26):** OIDC federation (REQ-IAM-006).
- **Provider fields:** name, issuer (discovery) URL, client ID, client secret, scopes (default `openid email profile`).
- **Login flow:** the same as SAML. The user enters their organization code, the platform redirects to the provider, and the callback creates the normal platform session. New users join as `MEMBER`.
- **Client secret:** encrypted with the existing TOTP-secret encryption (`TotpSecretCipher`, AES-GCM) and never returned after saving.
- **One enabled provider per organization, across SAML and OIDC together.**
- **Endpoints:** `/organization/me/oidc-providers`, mirroring the SAML paths.

### C28
**Decision (product owner, 2026-09-26):** Claim mapping (REQ-IAM-007).
- **Default OIDC claims:** `email`, `given_name`, `family_name`, `name`.
- **Order:** a configured name is tried first, then the defaults.
- **Fallbacks** (email-style NameID, display name, email local part, "SSO User") stay fixed, not configurable.

### C29
**Decision (product owner, 2026-09-26):** Organization MFA policy (REQ-IAM-001) on federated sign-in.
- After a SAML (and OIDC) sign-in, a member of an organization that requires MFA must also pass the platform's own authenticator-app step.
- A member who has not set up an authenticator yet enrols **during sign-in**: sign-in pauses on the QR set-up step, and the session is issued only after a valid code. No password is asked, because the user has just signed in. This applies to password sign-in as well.

### C30
**Decision (product owner, 2026-09-26):** Recover MFA (06.01.02.03). Organization admins may reset MFA for members of their own organization; platform admins may reset it for anyone. Every reset is audited and the user is notified.

### C31
**Decision (product owner, 2026-09-26):** Adopt the corrected sprint sequence from the shared roadmap analysis, in place of the original [PO] order:
- **2026.4.1** commits 02 Catalog **and** 05 Tenant (05 pulled forward from 2026.4.2).
- **2026.4.2** commits 13a Gateway & Events (API Management, Event Platform — pulled forward from 2027.1.1) and 15a Audit & Platform Administration (pulled forward from 2027.2.2).
- 2026.4.3 (07 Subscriptions, 08 Billing) is unchanged.
- 13b Connectors, 09 Orders, 11a Knowledge base and 16 Analytics move to 2027.1.1 (from 2027.1.1/2027.1.3, now consolidated).
- 10 Services, 03a Discovery & checkout, 12a Human support and 04a Agent platform move to 2027.1.2.
- 01b Portal live data, 04b Customer AI agents, 03b Trials & reviews, 11b Learning & certs and 15b Policy & compliance move to 2027.1.3.
- 14 Partners stays 2027.2.1; 15c Regional operations stays 2027.2.2.
**Reason:** the analysis found 2027.1.3 badly overloaded (over 80 open functions) under the original order, and 05 Tenant's organization-lifecycle piece was already pulled into 2026.3.3 (C25), so the rest of 05 belongs alongside it rather than in a separate sprint. **Reconciles with C21:** the general policy engine (06.02.02, beyond the MFA policy) stays in 2027.2.2 as C21 decided; only Platform Administration/Audit (15a) moves to 2026.4.2, not Policy Management.

### C32
**Decision (product owner, 2026-09-26):** 02.01 Product Lifecycle & Structure (sprint 2026.4.1).
- **Version product:** a plain revision counter on each product, incremented on every update after creation. Not a full content-versioning history (no past revision is kept).
- **Publish / Retire product:** two new named actions, in addition to the existing ACTIVE/INACTIVE toggle on the edit form. RETIRED is a new, distinct product status: pulled off the storefront and blocked from new subscriptions (existing subscriptions and access untouched), reversible by publishing again.
- **Product hierarchy / variants:** a product may have a parent product (self-referential; a variant is a sibling product under the same parent with its own `variantLabel`, e.g. "Enterprise"). No separate hierarchy or variant entity.
- **Dependencies:** advisory only — a product can list other products it depends on. Nothing today enforces the dependency at subscribe time (09 Orders, a later sprint, may enforce it).

### C33
**Decision (product owner, 2026-09-26):** 02.03 Plan Management (sprint 2026.4.1). Each plan gains: a currency (fixed set USD/EUR/GBP/INR — no exchange-rate or multi-currency billing engine exists; 08 Billing, a later sprint, may add one), a usage limit and included-features text, a usage price (per unit beyond the limit) and an overage charge (per unit over the limit), and a free-text tier-pricing description. A real tiered-pricing/metering **engine** is out of scope — these are data fields shown on the plan card, not enforced anywhere yet.

### C34
**Decision (product owner, 2026-09-26):** 05.03 User Management (sprint 2026.4.1).
- **Activate / Suspend / Remove** (05.03.01.03–.05) are built as three actions on an existing member: Suspend frees the seat and blocks sign-in but is reversible; Remove (the existing "remove member") is one-way, matching the existing REST semantics; Activate reactivates a Suspended member, subject to the seat limit.
- **Invite user / Create user** (05.03.01.01/.02) are **not** built this sprint: they need a new identity-creation flow (an invited person has no Customer row yet), which risks colliding with the existing registration and admin-provisioning flows within this sprint's time. Carried to 2026.4.2 — see the follow-up table below.
- **Review access** (05.03.02.03): a lightweight "reviewed by/at" stamp on each member, set by an explicit admin action. Not a scheduled or forced periodic review — this sprint only adds the record-keeping primitive.

### C35
**Decision (product owner, 2026-09-26):** 05.04.01 Groups (sprint 2026.4.1). A group is a named grouping of an organization's own members (e.g. "Engineering"), with add/remove member actions. It carries no permissions or product access of its own — that is still `OrgRole` and per-product access grants. 05.04.02 Projects is **not** built this sprint (carried to 2026.4.2 — see the follow-up table): "assign resources" needs a resource model this platform does not have yet outside of per-product access, and deciding what a Project's "resources" are needs more scoping than this sprint has time for.

### DN-2 Sprint scope, length and dates
**Decision:**
- **Sprint length:** sprints are **calendar months**. Sprint `.1`, `.2` and `.3` are the first, second and third months of the PI's calendar quarter. Example: 2026.3.3 = 1–30 Sep 2026; 2026.4.1 = 1–31 Oct 2026.
- **Scope:** each sprint **commits** the P0 (MVP) capabilities of its applications (C4, C5). P1 capabilities are **stretch** scope.
- **Carry-over:** anything not finished is recorded as carry-over on the next sprint page.

| Sprint | Dates |
|---|---|
| 2026.3.3 | 1–30 Sep 2026 |
| 2026.4.1 | 1–31 Oct 2026 |
| 2026.4.2 | 1–30 Nov 2026 |
| 2026.4.3 | 1–31 Dec 2026 |
| 2027.1.1 | 1–31 Jan 2027 |
| 2027.1.2 | 1–28 Feb 2027 |
| 2027.1.3 | 1–31 Mar 2027 |
| 2027.2.1 | 1–30 Apr 2027 |
| 2027.2.2 | 1–31 May 2027 |

### DN-4 Business rules and acceptance criteria
**Decision:**
- **New or changed features:** an **Approved FRD**, including `business-rules.md` and Given/When/Then `acceptance-criteria.md`, is required before building (`CLAUDE.md` rule 2).
- **Already-built sprint 2026.3.3 features:** FRDs documenting their current behavior from the code are written as a **non-blocking** parallel task.
**[SUM]:** Consistent with [SUM] "Bootstrap & Migration": requirements, designs, API specs and test cases live in GitHub with their canonical ID in the file front-matter from day one, until the in-house planning tools are ready.

### DN-5 Application codes
**Decision:** Three-letter mnemonic codes, following the `APP-CAT` example.

| ID | Application | Code |
|---|---|---|
| 01 | Enterprise Intelligence Suite | `APP-PRT` |
| 02 | Product & Catalog Management | `APP-CAT` |
| 03 | Marketplace | `APP-MKT` |
| 04 | AI Advisor & Agent Platform | `APP-AIP` |
| 05 | Customer / Tenant Management | `APP-TEN` |
| 06 | Identity & Access Management | `APP-IAM` |
| 07 | Subscription & Entitlement Management | `APP-SUB` |
| 08 | Billing & Payments | `APP-BIL` |
| 09 | Order & Provisioning Management | `APP-ORD` |
| 10 | Service & Resource Management | `APP-SRM` |
| 11 | Training & Knowledge Management | `APP-KNW` |
| 12 | Support & Service Management | `APP-SUP` |
| 13 | Integration & API Platform | `APP-INT` |
| 14 | Partner & Provider Management | `APP-PTR` |
| 15 | Administration & Governance | `APP-GOV` |
| 16 | Analytics & Data Platform | `APP-ANL` |

Requirement, feature, story and test IDs use the code without the `APP-` prefix, for example `REQ-IAM-001`, `FTR-IAM-001`, `STORY-IAM-001`, `TC-IAM-001`.
**[SUM]:** Matches [SUM]'s canonical ID examples (`REQ-CAT-001`, image tag `catalog-service:REQ-CAT-001-a3f9c1`). [SUM]'s generic `APP-001` form corresponds to the application IDs 01–16 above.

---

## Follow-up updates required by these decisions
These documents are **not** changed by this file. Update them to match:

| Decision | Update |
|---|---|
| C6 | Application pages: replace the per-function Traceability phase with the phase from C4 |
| C10 | Rename application 01 to "Enterprise Intelligence Suite" in the sprint, application and architecture pages |
| DN-2 | Fill "Start / end dates" on every sprint page |
| C20, C22, C23, C24 | Add to `SPRINT-2026.3.3.md` scope; create or update their FRDs (Draft) for approval |
| C20 | Add the retirement task to `SPRINT-2027.1.1.md` and `SPRINT-2027.1.3.md` |
| C19 | Add View spending to `SPRINT-2026.4.3.md` |
| DN-5 | Replace `<APP-CODE>` in the draft FRDs (for example `REQ-IAM-…` for application 06) |
| C13 | Apply the per-deployable container rules in `deployment/` and the architecture docs when the pipelines are built |
| Sources | Add `EIS_Platform_Summary.docx` to `docs/01-business/source-documents/` as **[SUM]**, and list it in that folder's README |

**Done (2026-09-26), no longer follow-up:** C31 (sprint pages 2026.4.1 through 2027.2.2, and the "Sprint" field on application pages 02, 03, 04, 05, 10, 11, 12, 13, 15, all updated to the corrected sequence — this superseded the older "C21: add the general policy engine to `SPRINT-2027.2.2.md`" and "C16, C17: add the deferred functions to `SPRINT-2027.1.3.md`" rows, and the "C21: add the agent controls to `SPRINT-2027.1.2.md`" row, which are now folded into C31's own sprint pages); C32–C35 (FRDs written and Approved, sprint 2026.4.1 built, carry-over recorded on `SPRINT-2026.4.2.md`).
