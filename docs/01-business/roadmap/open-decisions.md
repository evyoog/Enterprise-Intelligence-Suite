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
6. **D3 Billing scope for the MVP** (REQ-BIL-001 Open question 2): confirm the MVP excludes price books, promotions and usage billing. → Decided 2026-10-01: see [C50](#c50).
7. **D4 Tax calculation** (REQ-BIL-001 Open question 1): how is tax calculated — GST only, provider-calculated, or admin-configured rates per region? → Decided 2026-10-01: see [C51](#c51).
8. **D5 Entitlements:** is an entitlement a stored grant or derived at runtime from active subscriptions? → Decided 2026-10-01: see [C52](#c52).
9. **D6 Provisioning:** how does EIS create and change a customer's tenant in a hosted product? → Decided 2026-10-01 (option B): see [C56](#c56).
10. **D7 Service instance:** what is a "service instance" for Service & Resource Management (10)? → Decided 2026-10-01 (option A): see [C57](#c57).
11. **D10 Vector store:** where are embeddings for semantic search and AI knowledge stored? → Decided 2026-10-01 (option A): see [C58](#c58).
12. **D17 Cart:** does the platform have a cart, or does Buy go straight to checkout? → Decided 2026-10-01 (dedicated cart): see [C59](#c59).
13. **D12 API management:** where are API keys, rate limits and versioning handled? → Decided 2026-10-03 (option B, inside the backend): see [C61](#c61).
14. **D13 Platform events:** what carries platform events? → Decided 2026-10-03 (option A, outbox table): see [C62](#c62).
15. **D14 Seats and quantity:** what does quantity mean on a subscription? → Decided 2026-10-03 (option A, seats): see [C63](#c63).
16. **D15 Auto-renewal:** do subscriptions renew automatically, and how are customers reminded? → Decided 2026-10-03 (option A, with reminder rules): see [C64](#c64).
17. **D16 Organization subscription and billing permissions:** who manages an organization's shared subscriptions and billing? → Decided 2026-10-03 (A and B combined: organization admins by default, delegable through feature permissions): see [C65](#c65).
18. **D23 Product media storage:** where are uploaded files stored? → Decided 2026-10-05 (option A, AWS S3, private bucket, presigned URLs) for knowledge media: see [C72](#c72).

The D-numbers (D3–D23) come from the product owner's decision list, which is not stored in this repository. Only the D-items answered so far are listed above. D8 (LLM provider), D19 (webhooks), D27 (platform templates) and D42 (cloud API gateway) are referenced by records below but are still **not decided**; D23 was decided on 2026-10-05 (C72). D12–D15 were decided on 2026-10-03 (C61–C64), and D16 on 2026-10-03 (C65).

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

### C36
**Decision (product owner, 2026-09-27):** 15.01 Platform Administration (sprint 2026.4.2).
- **Configure currencies:** enable/disable only, on the fixed set already introduced by C33 (USD/EUR/GBP/INR). Disabling one only narrows the plan editor's currency choices; it does not touch a plan that already uses it, and no currency conversion or billing behavior is added.
- **Configure regions:** freely admin-defined (code + name), not a fixed geography list — nothing in any source specifies which regions this platform must support. This is the list 05.02.01.03 Assign region (C37) reads from.
- **Configure feature flags:** a runtime, admin-toggleable key/enabled/description row, distinct from a Spring config property fixed at deploy time. "groups_enabled" (05.04.01 Groups, built in 2026.3.3/2026.4.1) is the first real flag, seeded true so nothing already built stops working; toggling it off hides Groups for every organization.
- **Configure languages:** read-only — a language only appears if this platform actually ships translated strings for it (today: en, es). There is no add/remove; a "language" with no translation behind it would just show blank or English text under a foreign-language label.
- **Configure defaults and Manage templates** (15.01.02) are **not** built this sprint — see the follow-up table.

### C37
**Decision (product owner, 2026-09-27):** 05.02 Tenant Lifecycle (sprint 2026.4.2, carried from 2026.4.1).
- **Create tenant / Configure tenant** (05.02.01.01/.02): satisfied by the existing organization registration and admin-edit flows — "tenant" in this platform **is** the Organization (C25's model); there is no separate tenant entity or creation step.
- **Assign region** (05.02.01.03): a platform admin sets an organization's region from the list C36 introduces, on the same admin-edit form as the rest of REQ-TEN-001.
- **Configure isolation** (05.02.01.04): satisfied by the platform's existing architecture — every table is scoped by `organization_id`, enforced throughout (see `OrganizationSelfService#resolveMembership` and every `requirePermissionOnMember`-style check). No new capability is needed; this is a documentation-only decision.
- **Configure tenant policies** (05.02.01.05): one real policy this sprint — `allowSeatOverage`, a platform-admin-only flag on the organization that bypasses the licensed-seat check when true. Further policies (if any) are Not specified and are not invented here.

### C38
**Decision (product owner, 2026-09-27):** 07.01 Subscription Lifecycle & Changes, 07.04 Renewal & Lifecycle (sprint 2026.4.3, individual-customer subscriptions only — see the carry-over note below for organization-owned ones).
- **Suspend / Reactivate** (07.01.01): a new `SUSPENDED` status, reversible — same pattern as `MembershipStatus.SUSPENDED` (C34) and `ProductStatus.RETIRED` (C32). Suspend requires ACTIVE; Reactivate requires SUSPENDED.
- **Cancel** (07.01.01): one-way, matching `CANCELLED`'s existing semantics — no plan change, renewal or reactivation is possible afterward.
- **Upgrade / Downgrade** (07.01.01) and **Change plan** (07.01.02) are the same one action here: `changePlan`, validated only against "does this plan belong to the subscription's own product" — there is no separate upgrade/downgrade distinction to enforce (no proration, no billing impact exists yet; see the no-payment-integration note below).
- **Renew / Process renewal** (07.01.01, 07.04.01): since no payment integration exists anywhere in this codebase (subscribing already goes straight to ACTIVE — see `SubscriptionService`'s own javadoc), renewal simply extends the term; there is nothing to charge. The extension period is approximated from the subscription's plan: 30 days for a MONTHLY plan, 365 for YEARLY. A subscription with no plan (flat price) or a ONE_TIME plan can still be renewed if it already carries an `expiresAt` (extended by the same 30-day default), otherwise renewal is refused — there is nothing recurring to extend.
- **Change quantity** and **Schedule change** (07.01.02) are **not** built this sprint: neither concept exists on `ProductSubscription` today (no seat/quantity field distinct from a plan, no scheduled-for-later change mechanism), and inventing one risks colliding with 09 Orders' eventual provisioning model. Carried — see the follow-up table.
- **Expire subscription / Reactivate subscription** (07.04.02): a scheduled job (hourly) flips any ACTIVE subscription whose `expiresAt` has passed to EXPIRED; renewing an EXPIRED subscription reactivates it (folds "Reactivate" into the same `renewSubscription` action rather than a separate endpoint, since the trigger is identical — pay/extend the term).
- **Schedule renewal / Notify renewal / Auto-renew** (07.04.01) are **not** built: there is no billing/payment step for a scheduled job to trigger, and "auto-renew" without a real charge would just silently extend every subscription forever, which is not a decision this sprint should make unilaterally. Carried, pending 08 Billing.
- **07.02 Entitlement Management and 07.03 License & Quota Management are not built this sprint**: this platform already has a separate, working "product access" concept (per-organization-member `OrganizationProductAccess`, seat limits) that already does most of what "entitlement"/"license" would do here. Building a second, parallel entitlement/license/quota model risks duplicating that without a clear decision on how the two relate. Carried — needs a scoping decision, not just an implementation, before it's built.
- **Organization-owned subscriptions:** `listOrganizationSubscriptions` (the business dashboard's read-only view, built in Phase 19) is unaffected, but none of this sprint's new suspend/reactivate/cancel/renew/change-plan actions are exposed for organization-owned subscriptions yet — only an individual customer's own (`/me/subscriptions/...`). An organization admin acting on the organization's own subscription needs its own permission model (who on the org side may suspend/cancel a shared subscription?), which is Not specified in any source. Carried.
- **08 Billing & Payments is not built this sprint**: every capability under 08 (pricing engine, invoicing, payment methods, transactions, tax) needs a decision on which payment provider/gateway this platform integrates with, which no source specifies. Carried — see the follow-up table.
**Reason:** ships the reversible-lifecycle and plan-change actions the roadmap marks P0 with what the codebase already has (subscriptions, plans, no billing), while flagging everything that actually needs a payment provider or a scoping decision rather than inventing one.

### C39
**Decision (product owner, 2026-09-28):** 09 Order & Provisioning Management, 11a Knowledge Base (sprint 2027.1.1).
- **09.01 Order Lifecycle, organization purchasing only:** an individual customer already gets instant self-serve activation via `SubscriptionService#subscribe` (07.01, sprint 2026.4.3) — an approval step there would only add friction. Orders are for an ORGANIZATION's purchasing instead: any member may submit one (`SubmitOrderRequest`: product + optional plan), and an ORG_ADMIN (new `MANAGE_ORDERS` permission) must approve it before it provisions. Create/Validate/Price/Submit (09.01.01.01–.04) are folded into one `submitOrder` call — there is no separate draft state, and "price" is just read off the product/plan (no billing engine exists to quote against, same reasoning as 07.01.01's price-order gap). Cancel (09.01.01.06) is the requester's own, only while still SUBMITTED. Track order (09.01.01.07) is a plain list of the caller's own orders (or, for an ORG_ADMIN, the pending queue).
- **09.02 Service Provisioning:** folded into Approve, synchronously — approving an order calls a new `SubscriptionService#subscribeOrganization` (the organization-owned equivalent of `subscribe`, which did not exist before this sprint) in the same transaction. No separate PROVISIONED status: APPROVED already means provisioned. Configure/Suspend/Deprovision service (09.02.01.02/.04/.05) are **not** built — managing an org-owned subscription once it exists is still the same gap C38 already carried (no permission model yet for who on the org side may suspend/cancel a *shared* subscription); an order only gets one as far as existing.
- **09.03 Workflow Orchestration is not built, and is not planned as a generic engine**: this platform has exactly one orchestrated process (submit → decide → provision), implemented directly in `OrderService`. Trigger/execute/retry/rollback/compensate/escalate an arbitrary, admin-defined multi-step workflow (09.03.01, 09.03.02) has no second consumer anywhere in this codebase — building a generic engine for one hard-coded process would be pure speculation. This is a scoping decision, not a gap to carry forward, unless a second orchestrated process is ever added.
- **09.04 Approval Management:** single-hop only — Create approval is implicit in submission, Approve/Reject (09.04.01.03/.04) go straight to any ORG_ADMIN of the same organization. Route approval and Escalate (09.04.01.02/.06) are **not** built — there is no multi-level approval chain or escalation target defined anywhere, and inventing one has no real requirement behind it yet.
- **11.01.01 Knowledge Articles:** Create/Edit/Publish/Search/Version, platform-admin-managed (new `MANAGE_KNOWLEDGE_BASE` permission), readable by anyone once published — same reversible publish/unpublish pattern as `ProductStatus.RETIRED` (C32), and the same plain revision counter as `Product#version` (not a full content-versioning history). Search is plain case-insensitive text matching on title/body.
- **11.01.02 AI Knowledge (index/retrieve/validate against an embedding store) is not built**: no vector-store or embeddings-model decision exists anywhere in this codebase (`ai-service/` has no indexing pipeline today), and plain text search already satisfies "Search article" (11.01.01.04) without one. Carried, pending an AI-infrastructure decision — see the follow-up table.
- **13b Connectors & Webhooks and 16 Analytics & Data Platform are not built this sprint**: both are P1/stretch under the roadmap's own MVP priority (C4/C5) — 16.03 Operational Analytics alone has no priority given at all. Neither has a real consumer yet either (13b depends on 13a's Event Platform having a first real subscriber; 16 has no data pipeline to report on beyond what the existing dashboards already show). Carried — see the follow-up table.
**Reason:** ships the one real gap the roadmap's P0 commitment actually points at — an organization can't yet commit itself to a new subscription without an admin's say-so — while declining to build a generic workflow engine, a second entitlement model, or an AI-backed search feature that nothing in this codebase yet needs or can plug into.

### C40
**Decision (product owner, 2026-09-28):** 03.01.02 Recommendations, 12.01.01 Ticket Management (sprint 2027.1.2, of the four applications this sprint nominally covers).
- **03.01.01 Catalog Browsing is already satisfied**: browse/search/filter/sort by category and platform already exist (`ProductController#searchProducts`, Phase 17) — nothing new to build.
- **03.01.02 Recommendations, rule-based, not AI** ([WB]'s own AI flag is informational only, [C12](#c12)): "Show featured products" is a plain admin-set `Product.featured` flag; "Show popular products" reads the existing launch-count usage data (`ProductUsage`, Phase 16) aggregated across every customer, most-launched first; "Recommend products" is these two lists together. No separate recommendation model, collaborative filtering, or AI service is invented — none is needed to satisfy these three functions, and no purchase-history data exists yet to personalize further.
- **03.03 Marketplace Checkout is not built**: "Configure options" and "Apply discount" have no defined data model anywhere in this codebase (no product-options concept, no discount/coupon engine — 08 Billing, still not built, is the natural home for the latter). "Select product/plan" and "Submit order" are already served by the existing individual self-serve subscribe flow (07.01) and the organization Order flow (09.01, C39) — a third, separate "checkout" submission path would only duplicate one of those two. Carried, pending a decision on what "configure options" even means on this platform. See the follow-up table.
- **12.01.01 Ticket Management, not AI-driven**: every function's roadmap actor is "AI Agent," but that belongs to 12.02 AI Support / 04b (later sprint, needs an agent/LLM framework this platform doesn't have — same reasoning as 04a below), so every action here is a human instead — the ticket's own requester, or a platform admin holding a new `MANAGE_SUPPORT_TICKETS` permission. Categorize/Prioritize/Assign (12.01.01.02–.04) are folded into one `updateTicket` call; assigning an OPEN ticket also moves it to IN_PROGRESS. Escalate concretely raises the ticket to URGENT priority and an ESCALATED status — Not specified beyond that in any source, and no multi-level routing/on-call chain is invented. Close is only reachable from RESOLVED (Resolve and Close read as two distinct steps in the source; reopening a closed/resolved ticket is not built — Not specified).
- **12.03 SLA Management and 12.04 Incident & Problem Management are not built**: both need their own real data model — an SLA policy needs defined response/resolution-time targets per priority (nothing in any source specifies these), and Incident/Problem overlaps with the existing `service_incident` table (C20, sprint 2026.3.3) without a decided relationship between the two (is an "Incident" here the same row, or a new one that references it?). Building either without that decision risks duplicating or contradicting what already exists. Carried — see the follow-up table.
- **04a AI Agent Orchestration (the whole application, including the C21 agent-controls addendum: identity forwarding, tool allowlists, human approval, audit) is not built**: it needs an actual agent runtime, a model gateway, and a decision on which LLM/agent framework this platform integrates with — none of which exists anywhere in this codebase (`ai-service/` is a bare FastAPI shell with no agent framework, same gap as 11.01.02 AI Knowledge, C39). Building guardrails/approval/audit controls (C21) around an agent runtime that doesn't exist yet would be pure speculation — there is nothing real for them to guard. Carried, pending that framework decision — see the follow-up table.
- **10 Service & Resource Management is not built**: every one of its capabilities is P1/stretch under [C4](#c4)/[C5](#c5) (10.03 Configuration Management isn't even prioritized), and this platform has no actual cloud/infrastructure resources of its own to manage — 09 Provisioning (C39) already models "the organization now has access to a product," which is as far as "service instance" goes here. The [C20](#c20) retirement task (switch the interim status page to Health Monitoring) still can't land, since its prerequisite (10.04) isn't built — carried again, same as it already was once. See the follow-up table.
**Reason:** ships the two functions genuinely buildable from what already exists (a flag plus existing usage data; a ticket lifecycle with a real human-admin permission) and declines four applications/features that each need a real decision this sprint has no basis to make unilaterally — a checkout data model, an SLA/incident data model, an agent/LLM framework choice, and (for 10) actual infrastructure to manage.

### C41
**Decision (product owner, 2026-09-28):** 01.03.01 Unified Search, 03.04.01 Reviews & Ratings (sprint 2027.1.3, of the five application slices this sprint nominally covers — this is the corrected sequence's own "busiest sprint," and its own note explicitly allows 03b and 11b to move out if overloaded).
- **01.03.01 Keyword search, Filter results, Sort results, View search history:** a new `GlobalSearchService` — the first place in this codebase that queries across products, published knowledge articles, and (signed-in only) the caller's own support tickets in one call. None of the three existed as a combined search before this: `ProductService#searchProducts`, `KnowledgeArticleService#searchPublished` and `SupportTicketService#listMyTickets` each already existed independently (from 2026.4.1/Phase 17, 2027.1.1, and 2027.1.2 respectively) but nothing combined them. Filtering is the `type` query parameter; sorting is each source's own existing natural order — there is no cross-type relevance score to sort by. View search history reuses the existing `SearchHistoryService` (Phase 17) exactly as the product catalog page already does; recording stays a frontend concern, not duplicated into the new endpoint.
- **01.03.01.02 Semantic search is not built**: no vector-store/embeddings infrastructure exists anywhere in this codebase — the same gap as 11.01.02 AI Knowledge (C39) and 04a/04b (C40, below). Plain keyword matching already satisfies the other four functions.
- **01.02 Customer Dashboard on live data is already satisfied**: `BusinessDashboardService`/`BusinessDashboardDto` (built in earlier phases, well before this roadmap correction existed) already return real organization summary, subscriptions, per-application usage, and service-status alerts (via the existing `servicestatus` module, C26) — nothing new was needed here. "View spending" remains explicitly unavailable (`BillingOverviewDto.note`) since no payment/invoice model exists (08 Billing, still not built) — a pre-existing, disclosed gap, not a new one.
- **03.04.01 Submit review / Rate product** are one action (`submitReview`): a customer's rating (1–5) and optional comment for one product, upserted — resubmitting edits the existing row rather than creating a second, and always resets it to PENDING, even editing a previously-APPROVED review. **Moderate review** is a platform admin action (new `MANAGE_REVIEWS` permission), approve or reject, one-way once decided. **View ratings** is public: only APPROVED reviews are ever shown or averaged. A brand-new customer-facing product detail page (`/products/:id`) was built to host this — no such page existed before this sprint (the catalog page only ever linked out to a product's own `launchUrl`).
- **03.02 Product Evaluation (trials/demos/sandboxes) and 03.04's own Comparison sub-feature are not built**: trials/sandboxes need 10 Service & Resource Management (still not built, C40) to actually provision something to trial against — there is nothing for "Start trial"/"Launch sandbox" to do yet. Comparison (compare products/plans/estimate cost) has no new requirement beyond what the existing catalog and plan data already let a customer read side by side manually. Carried — see the follow-up table.
- **04b Customer-facing AI agents and 12.02 AI Support are not built**: same reasoning as 04a (C40) — no agent/LLM framework exists in this codebase, and 04b is explicitly built ON TOP of 04a's orchestration foundation, which itself was deferred pending that same framework decision. Building the customer-facing layer before its own foundation exists is not possible, let alone sensible. Carried — see the follow-up table.
- **11b Learning & Certification is not built**: every one of its capabilities is P1/stretch under [C4](#c4)/[C5](#c5), and the sprint's own note explicitly allows deferring it with nothing else depending on it. Carried — see the follow-up table.
- **15b Policy Management and Compliance are not built**: 15.02 Policy Lifecycle's priority is Not specified in any source (not even nominally P1), and 15.04 Compliance is P1/stretch; a general, admin-configurable policy engine (distinct from the org MFA policy already built, per [C21](#c21)) needs its own scoping decision — what is a "policy" generically on this platform, and what does it act on — that no source answers. Carried — see the follow-up table.
**Reason:** ships the one genuinely new cross-cutting capability (a real, if AI-less, unified search) and the one genuinely new, fully self-contained feature (reviews, needing nothing this codebase doesn't already have), while declining everything else in this sprint's five-slice nominal scope that needs a framework decision (04b/AI Support), a prerequisite this platform hasn't built yet (03.02 trials, needing 10), or its own real scoping (15b's policy engine) — or is simply P1/stretch with an explicit escape hatch already written into the sprint page (11b, 03.04's Comparison).

### C42
**Decision (product owner, 2026-09-28):** 14.01 Provider Onboarding (sprint 2027.2.1 — application 14 Partner & Provider Management is this sprint's entire nominal scope, and every one of its capabilities is P1/stretch or unprioritized under [C4](#c4)/[C5](#c5)/[C6](#c6); there is no P0 to commit under [DN-2](#dn-2-sprint-scope-length-and-dates), so this sprint's build is entirely stretch capacity, spent on the one slice the rest of the application actually depends on).
- **14.01.01 Register/Verify/Approve/Activate provider:** a new `modules/partner` package models a `Provider` with a `ProviderStatus` lifecycle (`REGISTERED → VERIFIED → APPROVED → ACTIVE`, or `REJECTED` from any non-terminal stage). Register is public self-service (`POST /partners/apply`, no Keycloak account required — mirrors `Customer` registration's own person-identity-before-login pattern, [`CurrentCustomerResolver`](../../../backend/src/main/java/com/vyoog/eisplatform/modules/registration/service/CurrentCustomerResolver.java)); Verify, Approve and Activate are separate platform-admin actions (new `MANAGE_PARTNERS` permission) because each is a genuinely distinct gate made at a different time, unlike the single-action folds used elsewhere (Order's `submitOrder`, Review's `submitReview`) — there is no single moment here that legitimately collapses all four.
- **14.01.02 Contracts:** a new `PartnerContract` (terms text, start/end date), one active contract per provider, created/edited via a single `createOrUpdateContract` call (Create contract and Manage terms are the same upsert, same fold pattern as Review's submit/edit). Track expiration reuses the existing scheduled-auto-expiry pattern (`SubscriptionService`, 07.04.01, sprint 2026.4.3): a contract past its end date is flipped from `ACTIVE` to `EXPIRED` by a scheduled job, not computed ad hoc on read.
- **14.02 Publisher Management is not built**: `Product` has no publisher/vendor/owner field anywhere in this codebase — every product today is implicitly platform-owned (`MANAGE_CATALOG`-gated only). Scoping "a partner can publish/manage only their own catalog listings" needs a real ownership model on `Product` (a new FK, a new authorization layer distinguishing "my listings" from the platform admin's, and a decision on whether a still-`REGISTERED`/unverified provider may publish at all) that this decision has no basis to make unilaterally in the same sprint that first introduces the `Provider` concept. Carried — see the follow-up table.
- **14.03 Revenue Sharing (Commission, Payouts) is not built**: needs Billing (08), which still doesn't exist anywhere in this codebase (same gap C38 already carried) — there is no invoice, transaction or ledger model for a commission or payout to reference. Carried — see the follow-up table.
- **14.04 Partner Operations is not built**: its priority is Not specified in any source (not even nominally P1, unlike the rest of application 14), and "Assign partner manager" needs a Partner Manager role/identity that doesn't exist yet (`RbacSeeder`'s own javadoc already names it as future scope). It could reuse the existing Support Ticket module's lifecycle almost as-is once a provider identity exists to be the requester — genuinely buildable later, just not decided as this sprint's priority over 14.01. Carried — see the follow-up table.
**Reason:** ships the only slice of application 14 nothing else in this sprint depends on and that this codebase already has every prerequisite for (a person-identity-before-login pattern to copy, an admin-moderation pattern to copy, a scheduled-expiry pattern to copy), while declining the three slices that each need a real decision or prerequisite this sprint has no basis to make unilaterally — a product-ownership model, a billing engine, and a partner-identity/role decision.

### C43
**Decision (product owner, 2026-09-28):** sprint 2027.2.2 (the last sprint in the corrected sequence) ships no new code — its entire nominal scope (application 15c Regional Operations, 15.05) is either already satisfied by earlier work or blocked by gaps this sprint has no new basis to resolve.
- **15.05.01 Region Management is already satisfied**: `PlatformAdministrationService`/`PlatformRegionRepository` (`modules/administration`, built sprint 2026.4.2 per REQ-GOV-001.2) already let an admin create, rename, enable and disable a region, with a delete blocked while any organization is assigned to it (`PlatformRegionUsageGuard`). Create region = `createRegion`; Configure region = `updateRegion`'s rename; Activate/Suspend region = the same `updateRegion` call's `enabled` toggle — the same one-call-covers-several-functions fold used throughout this roadmap (Order's `submitOrder`, Review's `submitReview`, C42's `createOrUpdateContract`), just decided a sprint earlier than this capability's own nominal slot, the same way 01.02 Customer Dashboard was already satisfied before its own slot (C41). No new module, entity, migration or endpoint is needed.
- **15.05.02 Data Residency is not built**: Define/Validate/Report residency policy needs a real, admin-configurable policy engine to define a "residency policy" against — 15b's general policy engine remains unresolved (C41: "needs its own scoping decision — what is a 'policy' generically on this platform, and what does it act on — that no source answers") — and needs 10 Service & Resource Management to actually exist so there is a real per-region resource to validate or report residency for, which also remains unbuilt (C40, still unresolved through C42). Building a residency feature now would mean inventing both a policy shape and a resource model this sprint has no basis to invent unilaterally, on top of two gaps already carried by name through three prior decisions. Carried — see the follow-up table.
**Reason:** the roadmap's own P1/stretch, cross-cutting-dependency structure means the last sprint's honest close-out is confirming what earlier sprints already covered and naming, precisely, the two still-open prerequisites (a general policy engine, real service/resource infrastructure) that every remaining carried item in this codebase — 15.05.02 here, 03.02 trials (C41), 04b/12.02 (C40/C41) — ultimately waits on, rather than inventing a narrower one-off model for this single feature alone.

### C44
**Decision (product owner, 2026-09-29):** a platform-wide UI/UX consistency pass, outside the C31 sprint sequence (that sequence ended at 2027.2.2, C43) — requested directly, not tied to a new application slice.
- **Catalog seeding:** the platform's own catalog must contain every real eVyoog product suite named in `docs/01-business/vision.md`/`scope.md` (Valam.ai, Varthan.ai, Thittam.ai, Thiran.ai, Yukth.ai, Tharav.ai), not just whichever ones an admin happened to create by hand. A new `CatalogSeeder` (`modules/product`, same idempotent `ApplicationRunner` pattern as `RbacSeeder`/`PlatformAdministrationSeeder`) seeds all six as `Platform`s with one flagship `Product` each, `ACTIVE` and `featured`, on every backend startup. `evyoog.com` itself could not be reached from this build environment (network egress to the whole `vyoog.com` domain family is blocked here, confirmed against both the marketing site and the logo image URL the app already loads) — no domain feature copy is invented for the suites this codebase has no real description for (Thittam.ai, Yukth.ai, Tharav.ai); their seeded description says so plainly. Valam.ai, Varthan.ai and Thiran.ai's descriptions are sourced from this repository's own existing fixtures/analysis docs, not invented. `launchUrl`/`imageUrl` are left blank rather than guessing a URL this environment could not verify.
- **Public homepage:** already pulls every catalog product from the real API (no hardcoded list) and already has a pricing-section CTA that opens the register flow — confirmed by reading `HomePage.tsx` before assuming a gap existed. Added: an explicit "Subscribe" button and category badge on each product card in the main products section (previously only an "Explore" link existed there), so the ask ("a subscription button that goes to the get-started page") is satisfied by name, not just implied by the pricing section below it.
- **Global Search moved to the top bar:** a new `TopBarSearch` (debounced, grouped live-result dropdown, "See all results" deep-links to `/search`) replaces the sidebar's "Search" nav item — the explicit example given for "don't create a separate screen for every function." `/search` remains a real route.
- **Not done in this pass** (documented, not built): merging Security+Preferences into one Account screen, merging Roles+Permissions+Privileged-Access into one Access Control screen, merging the Platforms/Apps admin screens into one Catalog screen with a view toggle, merging My Products+My Subscriptions, moving product/platform creation out of Settings into list-page "+Create" actions, and a shared `<DataTable>`/`<FilterBar>` component to retrofit onto every admin list screen. See `docs/08-architecture/ui-ux-redesign.md` for the full audit and the reasoning behind each one — each is a real, scoped follow-up, not an open-ended TODO.
**Reason:** ships the two changes with a real, verifiable gap (an incomplete catalog; search as a dead-end sidebar destination instead of a utility) and the one the user named explicitly by example, while writing up — rather than half-implementing — the larger navigation consolidation, since retrofitting ~15 existing screens onto shared components in the same pass this session already touched 6 files risks leaving some screens redesigned and others not, which is worse than a consistent "not yet" for all of them.

### C45
**Decision (product owner, 2026-09-30):** unify the app's two separate color systems into one, refine the three placeholder product descriptions, and give every product its own subscribe action and access-flow explanation. `evyoog.com` was requested again as the reference and was reconfirmed unreachable from this build environment (same network policy as C44, re-tested).
- **One color system, not two:** `theme.ts` (the signed-in MUI app — admin console and every customer screen) used a generic Tailwind-style blue (`#2563eb`) with no secondary/accent color, while `styles/landing.css` (the public marketing page) already had its own real blue/violet/cyan triad (`--blue #4c63ff`, `--violet #733dff`, `--cyan #42d8ff`). These were never the same brand — a visitor saw one palette on `/` and a different one the moment they signed in. `theme.ts` now uses that same triad as `primary`/`secondary`/`info` (light and dark variants derived from it, `info` darkened for text contrast in light mode), and a handful of `landing.css` rules that had drifted to slightly different hardcoded hex (the product-card icon gradients, the featured-plan button, the auth-modal focus/link colors) now reference the same `--blue`/`--violet`/`--cyan` custom properties the rest of that file already used, instead of their own one-off values.
- **Product descriptions:** Thittam.ai, Yukth.ai and Tharav.ai's placeholder copy from C44 ("full product description not yet available") is replaced with a short, professional, generic-but-honest one-liner and category each (Planning / Automation / Insights) — still no specific feature, integration or certification claim invented, since no source describes what these three suites actually do, but no longer reads as an apology either.
- **Subscribe + access-flow on the product page:** `/products/:id` (`ProductDetailPage`) gets an explicit **Subscribe** button (signed out → opens the register flow, same as the homepage; signed in → calls the real individual-subscription endpoint directly) plus a secondary link to the existing organization-purchase flow (`/organization/orders`), and a four-step "How you get access" showcase (Discover → Subscribe → Get access → Launch). This is the platform's own real access flow, not a fabricated per-product business process — this codebase has no source describing any of the six suites' internal workflows, so the showcase only depicts what subscribing and launching actually do here.
**Reason:** every change here fixes something concretely verifiable in the existing code (two palettes instead of one; three descriptions that apologized instead of describing; a product detail page with no way to act on it) rather than a new guess at branding this session still cannot fetch from the real source.

### C46
**Decision (product owner, 2026-09-30):** the MVP **must include online payment** (answer to pending decision D1, option A), and **Razorpay** is the payment provider. The product owner will provide the Razorpay credentials later.
- **Build the screens and backend now**, before credentials exist: billing details, invoices, pay invoice, saved payment methods, payment history, billing admin with refunds and reconcile, and a read-only payment-gateway status screen. Until the credentials are present the platform runs in **Payment gateway not configured** mode (every Razorpay-dependent action disabled with a clear message; billing details and invoices still work). Requirement: [REQ-BIL-001](../../02-requirements/FRD/billing-payments/requirement.md) (Draft).
- **Card details:** the product owner asked for card-details collection screens. To comply with card-industry (PCI-DSS) and RBI card-on-file tokenization rules, card and UPI details are entered only in Razorpay's secure checkout window; EIS screens hold every other field and store only a token reference and display details (network, last 4, expiry). Rule: [BR-BIL-001](../../03-business-rules/BR-BIL-001-no-raw-card-data.md).
- **One common file for API keys and passwords:** `config/secrets.env` (git-ignored), with the committed template `config/secrets.env.example`. Rule: [BR-SEC-001](../../03-business-rules/BR-SEC-001-central-secrets-file.md). The `spring.datasource` block in `application.yml` stays exactly as it is, by product-owner instruction.
- **Documentation placement:** UI requirements in `docs/05-ui/screen-requirements/`, API requirements in `docs/06-api/api-requirements/`, cross-feature rules in `docs/03-business-rules/`, the cross-feature flow in `docs/04-workflows/`, the data model in `docs/07-database/data-model/`, the integration in `docs/09-integrations/`; the FRD folder keeps the requirement, feature rules, feature workflow and acceptance criteria, and indexes the rest.
- **Still open (block FRD approval):** tax calculation (D4), billing scope excluding price books/promotions/usage billing (D3), whether activation waits for payment, invoice number format and payment terms, who handles organization billing (D16), and the legal fields on invoice documents. Auto-debit for renewals (D15), extra payment methods, and a second provider for international customers (rest of D2) are also not decided.

**Reason:** matches the MVP defined in C2 (buy → pay → provision → use without help) and lets the screens be built and tested while the Razorpay account is being set up.

### C47
**Decision:** REQ-BIL-001 is built per C46, with the following pragmatic engineering assumptions where the FRD's own open questions are still unanswered by the product owner. None of these are product decisions — they are the narrowest choice that lets the code compile and behave sensibly today, and every one is called out in the FRD/screen docs themselves as **Not specified** or **Open question**, not silently assumed. The FRD stays **Draft**; none of this closes an open question.
- **Tax (Open question 1):** every invoice carries a `taxAmount` column, always 0 in this pass. No rate, region or provider-tax-calculation logic exists yet.
- **Invoice number (Open question 4):** `INV-<year>-<id, zero-padded to 6>` — unique and immutable by construction (the id never changes), simplest format that satisfies BR-3 without inventing a per-year sequence counter.
- **Payment terms / due date (Open question 4):** an invoice is due immediately (`dueAt` = `issuedAt`) — no net-terms concept exists.
- **Activation vs payment (Open question 3):** unchanged from today — a subscription activates on subscribe/order-approval exactly as before; invoice generation is a side effect afterward, never a gate. `SubscriptionService#subscribe`/`#subscribeOrganization`/`#renewSubscription` each call `InvoiceService#generateForSubscription` directly.
- **Who handles organization billing (Open question 5, D16):** the existing `MANAGE_ORGANIZATION` permission, not a new one — organization billing is treated as part of running the organization, not a separate assignable responsibility, until told otherwise.
- **Invoice/receipt documents (Open question 8):** REQ-BIL-001.11 is satisfied with a plain-text downloadable document (invoice number, lines, totals, bill-to) rather than a formatted PDF — the legal/registration fields a real document needs are exactly what Open question 8 hasn't answered yet, so formatting it now would need redoing anyway.
- **Saved payment methods (FRD Open question 7 area):** Razorpay's separate Customer/Token API (for a token the platform can charge again without the customer's involvement) is out of scope for this pass. A saved method is verified the same way a real payment is (a small authorization transaction, BR-5), and its resulting Razorpay payment id is stored as the display/reference token. This is sufficient to list and remove a saved method, but **not** sufficient for unattended auto-charging — that still needs the real Customer/Token integration, tracked alongside D15 (auto-renewal, still undecided).
- **Refund policy (Open question 6):** admins only (`MANAGE_BILLING`), full or partial, reason required — matches the FRD's own stated assumption.

**Reason:** C46 said build now, before every answer exists; this records exactly which gaps were bridged with an engineering default versus a real product decision, so nothing here gets mistaken for the latter later.

### C48
**Decision (product owner, 2026-09-30):** the purchase flow itself needed a real screen, not an inline action — clicking Subscribe now sends a signed-in customer straight to a payment-details/checkout page, and a signed-out one to a full sign-in page (not the modal) that also offers new-user (create account) navigation and returns to checkout afterward.
- **`/checkout/:productId`** (new page): confirm/save billing details (skipped straight through if already on file) → `POST /me/subscriptions` (unchanged; still activates and generates the invoice in one call, REQ-BIL-001.2) → if that produced an OPEN invoice, pay it through Razorpay Checkout right there (reusing the same `createPayment`/`confirmPayment` calls Billing's own Pay-invoice dialog uses); a $0 plan skips straight to a plain confirmation since no invoice exists to pay (`InvoiceService#generateForSubscription`'s own rule). Card/UPI entry still only ever happens inside Razorpay's own window (BR-BIL-001) — this page collects billing address and shows amounts, never card fields.
- **`/login`** (new page): the exact same sign-in form as the existing modal (MFA, SSO, enrollment included) — extracted into a shared `AuthCredentialsForm` so the logic exists once — but as a real page with `?returnTo=`, and its own "Don't have an account? Create one" link to `/register`. The in-app "Login" links (navbar, hero) keep using the modal; this page exists specifically for flows that need to come back somewhere other than the default post-login destination.
- **Catalog interactivity:** a quick-action Subscribe button was added directly to the public catalog's product cards (`ProductTile`, `/products`) and the homepage's product cards, both routing through the same signed-in/signed-out rule above — buying no longer requires opening the detail page first. The product detail page also gained a "What's included" list sourced from the plan's own `includedFeatures` field (real data, not fabricated) and a subtle hover treatment on its access-flow cards.
- **Known simplification:** matching the invoice a checkout just created relies on ordering (invoices are returned newest-first, and the one `/me/subscriptions` just generated is always the newest) rather than a dedicated "invoices by subscription" lookup, since `InvoiceDto` doesn't carry a subscription/product id on its list rows. Safe for one purchase at a time, which is this flow's only real use case.

**Reason:** a purchase is normally the single most important flow ANY commercial platform has — it deserved a real, dedicated screen (billing details → payment) rather than a background action with an inline success/error alert, and a returning customer deserved a way to sign in (not just register) when Subscribe interrupts them signed out.

### C49
**Decision (product owner, 2026-09-30):** `/products/:id` (the page opened from "Reviews & ratings") needed more interactive UI, not just more content.
- Reorganized into three tabs — **Overview** (description, What's included, the access-flow showcase), **Pricing** (every real plan as its own hoverable card, not just the one implicit default), **Reviews** (count shown right on the tab) — instead of one long scroll.
- **Rating distribution** (5★ down to 1★) computed client-side from this product's own approved reviews (real counts, nothing fabricated) as clickable bars: clicking a bar filters the review list to that star value, clicking it again (or "Clear filter") returns to all reviews.
- The header's average-rating line is itself a click target that jumps to the Reviews tab.
- Kept from C45/C48: the Subscribe button's signed-in/signed-out routing, the access-flow showcase, and the "What's included" list — none of that changed, just where it sits.

**Reason:** three separate concerns (what it is, what it costs, what people think of it) read better as three tabs than one page everyone has to scroll past to reach the part they want, and a static bar chart is a missed chance to also let a visitor narrow the reviews to what they actually care about.

### C50
**Decision (product owner, 2026-10-01):** Billing scope for the MVP (answer to [D3](#decisions-needed), option B). The MVP includes recurring subscription billing, invoices, card and UPI payments, tax and currency. Later (not MVP): usage billing (08.02.01), price books beyond plan prices (08.01.01), promotions and coupons (08.01.02).

**Reason:** covers every plan type that exists today; usage billing needs metering that does not exist.

### C51
**Decision (product owner, 2026-10-01):** Tax calculation (answer to [D4](#decisions-needed), options B and C together). Each region has a tax method: **Admin rate** (admins set the tax name and rate per region in EIS) or **Tax service** (calculated by an external tax service). The admin rate is the fallback whenever the tax service is not configured, unavailable, or returns an error. Which tax service is used is **Not specified** (Razorpay processes payments but is not a tax-calculation service) — this interpretation is recorded here and is **to confirm by the product owner**.

**Reason:** gives every region a working tax rule today (the admin rate), without blocking on a tax-service vendor choice that has not been made.

### C52
**Decision (product owner, 2026-10-01):** Entitlements (answer to [D5](#decisions-needed), option A). An entitlement is derived at runtime from an ACTIVE subscription plus its plan's included features and usage limits (fields from REQ-CAT-002). No new entitlement table. Quota consumption (counting usage against limits) is out of scope because no usage metering exists ([C50](#c50)). Licensing and quantity (07.03, D14) are not decided.

**Reason:** the data already exists on the subscription and its plan; a derived check avoids a second, duplicate store that could drift from the subscription it is supposed to describe.

### C53
**Decision (product owner, 2026-10-01):** `/organization/business-dashboard` needed a more interactive, visual presentation, and platform admins asked for their own equivalent overview screen — both referencing a shared dashboard-layout reference image (stat tiles with trend, a bar chart, a donut chart, "View Report" actions).
- **Business dashboard (org-admin, existing screen, visual-only change):** the Seat usage and Applications sections are now presented with stat tiles (seats, active members, utilization — unchanged figures), a **donut chart** of applications by subscription status (ACTIVE / not subscribed — real counts from the existing `applications` list), and a **bar chart** of total launches per application (the existing `totalLaunches` field, nothing new). The Spend-this-period stat tile shows a trend (+/-% vs last period), computed from the two totals `BusinessDashboardDto.billing` already returns. No backend change: every number shown already existed in `BusinessDashboardDto`.
- **New platform admin dashboard** (`/admin/dashboard`, permission `VIEW_PLATFORM_DASHBOARD`, seeded for ADMIN): a platform-wide overview in the same visual style — organizations (total/active), catalog (products/platforms), subscriptions by status (donut chart), revenue this/last period from paid invoices platform-wide (same honest "no paid invoices yet" note pattern as `BillingOverviewDto` when empty — no gateway, no numbers, not fabricated), open support tickets, pending reviews, the platform's own live service health, and a bar chart of the 5 most-launched products platform-wide (reusing the existing `ProductUsageRepository.topProductIdsByTotalLaunches` idea, extended to also return each product's total). Every figure is a real `count()`/`SUM()` query against existing tables — nothing here is a stored snapshot or a fabricated trend line.
- **Navigation:** added as a new **Dashboard** item in the admin sidebar, first position — but the existing admin index route stays **Platforms** ([C44](#c44)'s own documented landing-page decision is not changed by this one; a platform admin who wants the new overview clicks Dashboard explicitly).
- **Not done:** no new time-series data was invented for either screen — a "last 6 periods" revenue trend (as sketched for the Billing screen's own Overview tab, still Draft/not built) is a separate, larger aggregation this decision does not attempt. Both screens show point-in-time counts/sums and one period-over-period comparison (this vs last 30 days), which is exactly the data the backend already aggregates elsewhere (`BillingOverviewDto`'s own 30-day windows).

**Reason:** the reference image's own strongest ideas — trend-labelled stat tiles, one bar chart, one donut chart, grouped sections instead of one long page — translate directly onto data this platform already has; anything the reference shows that this platform cannot back with a real number (a 12-point daily revenue curve, an hour-of-day order-time breakdown) was left out rather than faked.

### C54
**Decision (product owner, 2026-10-01):** the business dashboard needed to be more interactive (not just better-looking), the numbers on it needed to be populated (most organizations have no usage/billing history yet), and every signed-in screen was too narrow on a wide monitor.

- **Interactivity (business dashboard, C53's screen):** the applications-by-subscription donut and the launches-per-application bar list are now click-to-filter, the same pattern [C49](#c49) already established for the product-page rating distribution — clicking a donut segment or a bar filters the Applications & usage table to matching rows; clicking the same one again (or a **Clear filter** chip) returns to the full list. The table's column headers are now sortable (click to sort, click again to reverse). No new data, no new endpoint — purely client-side, same `applications` list already in `BusinessDashboardDto`.
- **Demo data (`DemoDataSeeder`, new, `@Profile("!test")`):** real organizations, customers and subscriptions are never fabricated — this seeder only ENRICHES what already genuinely exists, idempotently, on every non-test startup: a launch-count/last-used `ProductUsage` row for every ACTIVE member's ACTIVE product access that doesn't have one yet; two backdated PAID invoices (last month, two months ago) for every ACTIVE organization subscription that has no invoice history yet at all (amount/currency/line exactly mirroring `InvoiceService#generateForSubscription`'s own logic, only the issue date and already-PAID status differ — see `InvoiceRepository#backdateIssuedAtForDemoData`); and a small fixed set of clearly-labeled ("Demo: ...") support tickets and product reviews, topped up only to a floor (5 tickets, 4 reviews) platform-wide, never touching a platform that already has real ones. Recorded in [docs/07-database/demo-data.md](../../07-database/demo-data.md) per `database/seed/README.md`'s own rule: demo data only, never real customer data, dev/uat only.
- **Full width:** `BusinessDashboardPage`, `MyProductsPage`, `OrganizationSamlProvidersPage`, `SecuritySettingsPage`, and the signed-in catalog grid on `ProductsPage` (when opened inside the app shell) no longer cap their content at a fixed `Container maxWidth` — they now use the full width the app shell's main content area already provides. The narrow, centered pre-auth forms (forgot/reset password, check-email, verify-email, organization self-registration) are deliberately **not** widened — a single-column form reads worse, not better, at full browser width, and the product owner's "all screens" ask is read as every screen the earlier dashboard work actually touched, not a mandate to break a considered single-purpose layout.

**Reason:** interactivity on real data beats a static chart; demo data lets the dashboards being built actually be evaluated instead of showing zeros; and a dashboard built for a wide monitor should use it.

### C55
**Decision (product owner, 2026-10-01):** The payment step offers **online payment** (card, UPI and other Razorpay methods) and an **offline "Pay by invoice"** option: EIS generates the invoice immediately; the customer pays by bank transfer, NEFT/RTGS or cheque; a platform billing administrator records the payment when it is received, which marks the invoice PAID. Card fields keep the reference-design look but must never send card data to EIS ([BR-BIL-001](../../03-business-rules/BR-BIL-001-no-raw-card-data.md)). Coupons are excluded ([C50](#c50)); there is no shipping (subscription software). Requirements: REQ-BIL-001.18–.21; screen: [checkout-payment.md](../../05-ui/screen-requirements/checkout-payment.md).

**Card-panel approach (recorded 2026-10-01, to re-verify):** no Razorpay product offering provider-hosted, embeddable card fields (iframes the merchant page cannot read) could be confirmed — Razorpay's Custom UI integration has the merchant page collect card number, expiry and CVV itself and pass them to `razorpay.createPayment`, which would put card data in EIS frontend state and is therefore not allowed by BR-BIL-001. The card panel therefore uses **read-only placeholder fields** in the reference layout, and **Pay opens Razorpay Standard Checkout with the card method preselected**; card entry and card validation messages happen only inside Razorpay's window. razorpay.com could not be fetched from the build environment (network egress blocked), so this is to be re-checked against Razorpay's current documentation or with Razorpay before Phase 2.

**Numbering note:** the request for this decision referred to the billing-scope, tax and entitlement decisions as C47, C48 and C49; in this file they are [C50](#c50), [C51](#c51) and [C52](#c52) (C47–C49 were already used), and this decision is C55.

**Still open:** who may use Pay by invoice; payment terms (days until due) and reminder emails; the bank details printed on invoices (fields) and where admins maintain them; whether activation waits for payment (REQ-BIL-001 Open question 3); whether a purchase-order number field is needed; Terms & Conditions and Privacy Policy URLs (no legal routes exist in the app today); which other online methods (netbanking, wallets) are offered.

**Layout superseded by [C59](#c59) (2026-10-01):** the checkout layout is redesigned (breadcrumb Cart › Billing details › Payment › Complete, payment-method tiles, saved cards, cart summary, brand logos); everything else in this record — Pay by invoice, Record offline payment, offline bank details, the card-panel approach and the open questions — still applies. Card-panel approach re-checked on 2026-10-01: razorpay.com was still not reachable from the build environment and a web search found no official Razorpay documentation for embeddable secure card fields, so the placeholder + Razorpay Checkout approach stays (REQ-BIL-001 Open question 15).

**Built (Phase 2, 2026-10-01, at the user's request "do code"):** the checkout screen `/checkout?productId|subscriptionId|invoiceId[&scope=organization]` (the C48 link `/checkout/:productId` now redirects to it; Billing → **Pay** opens it in place of the Pay-invoice dialog), the admin **Record offline payment** dialog and the **Billing settings** page (`/admin/billing/settings`), with schema change `V014__checkout_offline_payment.sql`. Until the open questions above are answered, the build uses these **engineering defaults** (each a single place in code, to be replaced by the decided rule):

| Open question | Default built | Where |
|---|---|---|
| Who may use Pay by invoice (OQ 9) | Every customer | `CheckoutService.payByInvoiceAllowed` |
| Payment terms / due date (OQ 10) | Due date = issue date (as [C47](#c47)); no reminder emails | `InvoiceService` |
| Activation before payment (REQ-BIL-001 OQ 3) | Unchanged: subscriptions activate as today | — |
| Bank detail fields (OQ 11) | Account name, bank, account number (required); IFSC, SWIFT/BIC (optional); kept in table `billing_settings`, not in the secrets file (not secrets) | `OfflinePaymentService` |
| Terms / Privacy URLs (OQ 12) | Consent text without links; checkbox still required | `checkout.consent` |
| Other online methods (OQ 14) | No method prefill; Razorpay shows what the account has enabled | `razorpayCheckout.ts` |
| Partial offline payments (OQ 16) | Refused: the amount must equal the invoice total; only OPEN invoices with route OFFLINE | `OfflinePaymentService.recordOfflinePayment` |
| Refund / reconcile of offline payments | Refused by the API and hidden in the UI (settled outside Razorpay) | `PaymentService`, `AdminBillingPage` |
| Pending online payment | Poll every 5 s for up to 2 min, then "taking longer than usual" | `CheckoutPage` |

**Reason:** organizations commonly pay by bank transfer against an invoice rather than by card; offering both routes in one checkout keeps a single flow (C44) while BR-BIL-001 keeps EIS out of card-data scope.

### C56
**Decision (product owner, 2026-10-01) — answer to D6, option B:** When a subscription starts, is suspended, is resumed or is cancelled, EIS notifies the hosted product through a defined **event or webhook contract** that each product implements. The product creates or changes the customer's tenant and reports the result back to EIS. The existing internal sign-in bridge stays the way users reach the product. Every hosted product team must implement the contract. The delivery mechanism (D13 events or D19 webhooks) is **not decided**.

FRD: [REQ-ORD-002 Provisioning contract](../../02-requirements/FRD/provisioning-contract/requirement.md) (Draft, sprint [2027.1.1](sprints/SPRINT-2027.1.1.md)). Its message shape is a proposal to confirm with the hosted product teams.

**Reason:** EIS cannot create tenants inside products it does not run. A contract that each product implements keeps tenant logic in the product, and lets EIS track the outcome for every product in the same way.

### C57
**Decision (product owner, 2026-10-01) — answer to D7, option A:** A **service instance** is one subscription's provisioned tenant in a hosted product, with a status and a health value that the product reports. This is what Service & Resource Management (application 10) manages. The interim status page ([C20](#c20)) switches its per-product status to this source.

FRD: [REQ-SRM-001 Service instances](../../02-requirements/FRD/service-instances/requirement.md) (Draft, sprint [2027.1.2](sprints/SPRINT-2027.1.2.md)).

**Reason:** EIS hosts other teams' products and does not run their infrastructure. The tenant that provisioning ([C56](#c56)) creates is the only resource EIS can track for each subscription.

### C58
**Decision (product owner, 2026-10-01) — answer to D10, option A:** Semantic search (01.03.01.02, [C16](#c16)) and AI knowledge (11.01.02) store their embeddings with **pgvector** in the existing PostgreSQL database. Embedding model: **Not specified**. It depends on D8, the LLM provider.

Sprints: [2027.1.1](sprints/SPRINT-2027.1.1.md) (11.01.02 AI knowledge) and [2027.1.3](sprints/SPRINT-2027.1.3.md) (semantic search). No FRD changes are needed until those features are scoped.

**Reason:** this keeps one database to operate, back up and secure. pgvector is a PostgreSQL extension, so the platform does not need a separate vector service.

### C59
**Decision (product owner, 2026-10-01) — answer to D17, a dedicated cart:** The platform has a **cart**. **Buy** on any paid plan adds the plan to the cart and opens the cart page. An individual's cart proceeds to billing details and payment. An organization member's cart is submitted as an order that an organization admin approves (existing [REQ-ORD-001](../../02-requirements/FRD/order-lifecycle/requirement.md) rules); payment follows approval. Marketplace checkout (03.03) is **pulled forward into sprint [2026.4.3](sprints/SPRINT-2026.4.3.md)**, to be built with Billing.

This replaces the earlier recommendation of no separate cart, where Buy went straight to checkout. That recommendation was never recorded in this file; it was the behaviour of the C48 page `/checkout/:productId` and of [C55](#c55).

FRD: [REQ-MKT-003 Cart and checkout](../../02-requirements/FRD/cart-checkout/requirement.md) (Draft). Screens: [cart.md](../../05-ui/screen-requirements/cart.md) and the redesigned [checkout-payment.md](../../05-ui/screen-requirements/checkout-payment.md), with logos per [payment-brand-assets.md](../../05-ui/screen-requirements/payment-brand-assets.md).

**Built (2026-10-01, at the product owner's request "start develop the code", before REQ-MKT-003 and the REQ-BIL-001 changes were marked Approved):** the cart (`/cart`, module `cart`, `/me/cart` API, tables `cart` and `cart_item`, migration `V015__cart_checkout.sql`), Buy → cart on every Subscribe button, the top-bar cart badge, and the redesigned checkout. Until the open questions are answered, the build uses these **engineering defaults** (each in one place in code):

| Open question | Default built | Where |
|---|---|---|
| REQ-MKT-003 OQ 1 — several products, one invoice | Yes: an individual's cart is billed on **one invoice**, one line per subscription (`invoice_line.subscription_id`; the invoice's own `subscription_id` is the first item's). All items must share a currency (`CURRENCY_MISMATCH` issue otherwise). An organization member's cart becomes **one order per item**, because a REQ-ORD-001 order holds one product | `CartService.checkout`, `InvoiceService.generateForSubscriptions` |
| OQ 2 — organization admin's own cart | Goes through approval like any member's (existing REQ-ORD-001 rules) | `CartService.checkout` |
| OQ 3 — cart expiry | None | — |
| OQ 5 — anonymous add to cart | No anonymous cart; Buy signs the visitor in, then adds the item (`/cart?add=…`) | `useBuy`, `CartPage` |
| OQ 6 — second Buy, persistence, audit | Replaces the plan; stored on the server; cart changes not audited | `CartService` |
| OQ 7 — fees line | None | — |
| OQ 8 — price-change confirmation | Per item (**Confirm new price**) | `CartPage`, `PATCH /me/cart/items/{id}` |
| Who is an "organization member" | A customer with an ACTIVE organization membership | `CartService.membership` |
| Default plan for a product-level Buy | The product's Monthly paid plan, else its first paid plan; a product with no paid plan keeps the free-plan flow | `CartPage` |
| Repeated checkout (BR-10) | A second checkout within 2 minutes of a successful one returns the same invoice or orders; the cart row is locked during checkout | `CartService.checkout` |
| Tax in the cart | No tax lines (REQ-BIL-002 tax engine not built); "Tax calculated at payment" without billing details | `CartService.toDto` |
| REQ-BIL-001 OQ 19 — wallet brands | Generic wallet icon | `CheckoutPage` |
| REQ-BIL-001 OQ 22 — Cart crumb for an invoice paid from Billing | Hidden | `CheckoutPage` `Breadcrumb` |
| REQ-BIL-001.23 — logo files | No official file obtained yet (brand kits not reachable from the build environment); each brand shows as its name in a text chip until a file and its licence are recorded in `frontend/src/assets/payment-logos/ATTRIBUTION.md` | `PaymentLogos` |
| Legal links (OQ 12, 20) | Not rendered (URLs unknown); consent text names them without links | `CheckoutPage` |

**Numbering note:** the request for this decision called the earlier billing decisions C46 (Razorpay), C47 (no coupons or usage billing), C48 (tax), C49 (entitlements) and C50 (offline Pay by invoice). In this file they are [C46](#c46), [C50](#c50), [C51](#c51), [C52](#c52) and [C55](#c55). C47–C49 were already used for other records. C50 (offline Pay by invoice) in the request is [C55](#c55) here, and it was already recorded, so it is not recorded again.

**Reason:** one cart lets a customer review several purchases, re-checks them before paying, and gives organization members a single path into the existing order approval.

### C60
**Decision (product owner, 2026-10-01):** "Corporate standard design in every screen, apply multi colors"; the Billing settings screen gets "all necessary fields", and a Razorpay credentials view "like the Razorpay credential screen" on the same screen.

**Built (2026-10-01):**
- **Corporate design system** (all signed-in screens): `theme.ts` — firmer headings, gradient primary buttons and tab indicator, tinted upper-case table headers, accent-bordered alerts, rounder cards and dialogs, tuned success/warning/error colours in light and dark mode; `theming/accents.ts` — ten accent colours (blue, violet, teal, amber, rose, emerald, cyan, indigo, orange, pink) with light- and dark-mode shades; `PageHeader` — brand multi-colour stripe, accent icon tile and translated area label, now used with its own icon and accent on every signed-in page (pages that had a bare title were moved to it); the sidebar gives each item its own accent icon tile and a brand stripe under the logo. The brand primary/secondary colours of [C45](#c45) are unchanged; accents only colour icons, markers and status tiles.
- **Billing settings** (`/admin/billing/settings`, [admin-billing-settings.md](../../05-ui/screen-requirements/admin-billing-settings.md)), four tabs: **Business & invoicing** (legal name, trade name, GSTIN, PAN, CIN, registered address, billing contact, invoice number prefix, payment terms, invoice footer note); **Offline payments** (adds branch, account type, MICR, IBAN, direct UPI ID, cheque payee and address, accepted offline methods, payment instructions to [C55](#c55)'s fields); **Payment methods** (switch Cards, UPI, Netbanking, Wallets and Pay by invoice on or off; Razorpay Checkout name, description and colour); **Razorpay gateway** (credentials view). Schema `V016__billing_settings_full.sql`.
- **Effects:** new invoices use the prefix (`PREFIX-YEAR-NUMBER`; issued numbers never change) and are due on issue date + payment terms (0 keeps [C47](#c47)'s "due on issue"); invoices and receipts print the issuer block and footer, and an OPEN offline invoice prints the payment details; the checkout hides switched-off methods and the API refuses them; Pay by invoice can be switched off for everyone (an admin control for REQ-BIL-001 Open question 9 — who may use it per customer is still open); the Record offline payment dialog and API accept only enabled offline methods; Razorpay Checkout shows the configured name, description and colour.
- **Razorpay credentials view — BR-SEC-001 kept:** keys are **not** entered on the screen. [BR-SEC-001](../../03-business-rules/BR-SEC-001-central-secrets-file.md) rules 1 and 5 require keys to live only in `config/secrets.env` and never be shown. The view shows each key as Present / Missing (key ID masked), Test / Live mode, the webhook URL to register, the last webhook, **Test connection**, and the five steps to add or rotate keys in the secrets file (variable names with Copy). Entering keys on a screen would first need BR-SEC-001 changed (encrypted storage, rotation and audit) — **not decided**.

**Engineering defaults:** formats only for GSTIN, PAN, CIN, IFSC, MICR, SWIFT/BIC, IBAN and UPI ID (no checksum or directory lookup); before an admin saves anything, every method is on, the prefix is `INV` and terms are 0 days; at least one payment method and one offline method must stay on.

**Reason:** the invoice needs a legal issuer to be usable, admins need to control what customers can pay with without a code change, and one consistent, colour-coded design makes the many admin screens easier to scan.

### C61
**Decision (product owner, 2026-10-03) — answer to D12, option B:** API management is built **inside the existing backend now**: API keys, rate limits and API versioning. A cloud API gateway is added at deployment (D42 not decided); when it is, any overlapping logic is moved or removed.

FRD: [REQ-INT-001 API management](../../02-requirements/FRD/api-management/requirement.md) (sprint [2026.4.2](sprints/SPRINT-2026.4.2.md)).

**Reason:** integrations need keys and protection from overload before a deployment gateway exists; keeping it in the backend now avoids blocking on D42.

### C62
**Decision (product owner, 2026-10-03) — answer to D13, option A:** Platform events use an **outbox table in the existing Postgres database**: a business change and its event are saved in the same transaction, then a background dispatcher delivers events with retries. This is the platform's event mechanism; no external message broker is planned.

FRD: [REQ-INT-002 Platform events](../../02-requirements/FRD/event-platform/requirement.md) (sprint [2026.4.2](sprints/SPRINT-2026.4.2.md)). Cross-feature flow: [platform-events.md](../../04-workflows/platform-events.md).

**Reason:** the outbox guarantees an event exists exactly when its change was committed, with no extra infrastructure to run; provisioning ([C56](#c56)), webhooks (D19) and reminders can all build on it.

### C63
**Decision (product owner, 2026-10-03) — answer to D14, option A:** **Quantity means seats** on organization subscriptions, reusing the existing organization seat limits. Seat changes **take effect immediately**; scheduled (future-dated) changes come later. Individual subscriptions always have quantity 1.

FRD: [REQ-SUB-003 Seats and quantity](../../02-requirements/FRD/subscription-seats/requirement.md) (sprint [2026.4.3](sprints/SPRINT-2026.4.3.md)).

**Reason:** organizations buy for a number of people; the platform already counts and enforces seats per organization, so quantity reuses that instead of adding a second licence model.

### C64
**Decision (product owner, 2026-10-03) — answer to D15, option A with the product owner's reminder rules:** Subscriptions **auto-renew by default**; the renewal charge happens through Billing once Razorpay is configured. Renewal **reminder emails**: a platform default of **7 days** before renewal, sent **daily** at a configured time **until renewed**, with user control over on/off, how many days before, and the send time.

FRD: [REQ-SUB-004 Auto-renewal and renewal reminders](../../02-requirements/FRD/renewal-reminders/requirement.md) (sprint [2026.4.3](sprints/SPRINT-2026.4.3.md)).

**Reason:** subscriptions should not lapse by accident, and customers should know in good time, on their own schedule, when a renewal is coming.

### C65
**Decision (product owner, 2026-10-03) — answer to D16, options A and B combined:** **Organization admins** manage the organization's shared subscriptions by default (suspend, reactivate, cancel, change plan, change seats). An organization admin can **delegate** this to any member through a new assignable **feature permission**, "Manage subscriptions". More broadly, access inside an organization is managed through **role defaults plus individual overrides**, on a redesigned **Access management** screen ([REQ-TEN-005](../../02-requirements/FRD/access-management/requirement.md)).
- **Terms adopted:** *Product access* (formerly "app access"), *Feature permissions* (formerly "feature access"), *Role defaults*, *Individual override*, *Delegated administrator* (a member holding "Manage access").
- **New feature permissions:** *Manage subscriptions* (D16), *Manage access* (delegation), and *Manage billing* for organization billing — the last is **proposed, confirm** ([REQ-BIL-001](../../02-requirements/FRD/billing-payments/requirement.md) Open question 5).
- Unchanged: [C52](#c52) (entitlements derived at runtime), [C63](#c63) (seats on organization subscriptions), [C44](#c44) (one screen per area, not per function), [C45](#c45) (unified theme). This supersedes the [C47](#c47) engineering default that organization billing uses `MANAGE_ORGANIZATION`, once REQ-TEN-005 is approved and built.

FRD: [REQ-TEN-005 Access management](../../02-requirements/FRD/access-management/requirement.md) (sprint [2026.4.2](sprints/SPRINT-2026.4.2.md); the organization-subscription part also under 09.02.01 in sprint [2027.1.1](sprints/SPRINT-2027.1.1.md)).

**Reason:** keeps the existing admin rule as the default while allowing delegation, the same pattern as the billing and user-admin roles in Microsoft 365, Google Workspace and Salesforce.

### C66
**Decision (product owner, 2026-10-03) — UI/UX redesign with the Product Catalog as the reference design:** the existing frontend is restyled, not rebuilt, into a clean enterprise SaaS design: indigo primary (#6366F1 brand; #4F46E5 for buttons and text so white text meets WCAG AA), #F7F8FC background, white bordered cards, Inter, light sidebar, minimal header and one shared page header. Platforms and apps share one showcase card. Administrators set a platform colour, status, "show in catalog" and display order, and an app accent colour (or inherit the platform's), feature tags and documentation/support links, with a live preview in the forms. A public catalog API (`GET /catalog/platforms`) serves the platform cards.
- **Data rule:** nothing is hard-coded or invented. Counts, categories and tags are derived from real apps; data the backend does not have is omitted. There is **no release-version data**, so the forms have no Versions section and cards show no versions; `products.version` (an edit counter, [C32](#c32)) is shown only on the edit page as "Revision N".
- **Unchanged:** routes, API field names, authentication, permissions (`MANAGE_CATALOG`), CRUD (C44 create-from-All-Apps kept), the SSO badge meaning and hint text, and the rule "price shown only if no pricing tiers are added". "Product structure" is renamed **Product relationships** in the form.
- **Supersedes** the page-chrome part of [C60](#c60) (gradient buttons, brand stripe, multi-colour accent tiles); C60 accent keys remain accepted by `PageHeader` and show as a small icon.
- **Redesigned:** Product Catalog, platform details, app details, app card, Platforms list, Create/Edit Platform, All Apps, Create/Edit App, theme and shell. **Not redesigned** (theme, shell and page header only): dashboard, registrations, privileged access, roles, permissions, audit log, service status, knowledge base, support, reviews, partners, billing, Settings screens.

FRD: [REQ-CAT-003 Catalog Showcase](../../02-requirements/FRD/catalog-showcase/requirement.md) (sprint [2026.4.1](sprints/SPRINT-2026.4.1.md)). Design: [design-system.md](../../05-ui/screen-requirements/design-system.md).

**Reason:** one consistent, accessible design across the product, with the catalog showing each product family the way the business presents it — using only real data.

### C67
**Decision (product owner, 2026-10-03) — Preferences page redesign and personal accent colour:** Account → Preferences becomes one centred column with exactly four sections — **Appearance**, **Language & Formats**, **Notifications**, **Renewal Reminders** — each a heading, a description and label/control rows. No backend, API, route, database, authentication or permission change.
- **Accent colour (new):** ten predefined accents (Indigo default, Blue, Violet, Purple, Green, Teal, Orange, Rose, Red, Slate), no custom HEX. The choice replaces the theme's **primary** palette only (buttons, links, active navigation, tabs, switches, focus, accent icons); backgrounds, text, borders, status colours and product showcase colours (C66) stay as they are. Every shade meets WCAG AA (4.5:1) in light and dark mode.
- **Formats (new):** date format (DD/MM/YYYY, MM/DD/YYYY, YYYY-MM-DD), time format (12-/24-hour) and, until removed on 2026-10-03 at the product owner's request, first day of week; each defaulting to **Region default** so nothing changes until chosen. Date and time formats apply wherever the app uses the shared formatters (`formatDate`, `formatDateTime`).
- **Storage:** the account preference API (`/me/preferences`) has no fields for accent or formats and the database is not to change, so these are stored **in the browser** (localStorage), like the signed-out fallback of the other preferences. They persist across refresh and sign-in on the same browser, not across devices.
- **Notifications:** reuse the existing email opt-out per category (`/me/notifications/preferences`, also edited from the bell menu). Rows map to backend categories: Products & applications = ORDER; Billing = BILLING, SUBSCRIPTION; Platform & support = SYSTEM, ORGANIZATION (support ticket replies are sent as SYSTEM, so Support and Platform cannot be separated); Security = SECURITY, PRIVILEGED_ACCESS, recommended on, with a warning when off. **Email notifications** turns all of them off or on. **In-app notifications** are always recorded by the backend, so the page shows them as *Always on* instead of a switch.
- **Renewal reminders:** behaviour unchanged (REQ-SUB-004, C64): own save button, 7-day / 09:00 platform defaults when empty; the summary reads "Daily reminder from N days before renewal at HH:mm Zone." and updates as values change.
- Theme becomes a dropdown (Light / Dark / System); reduced motion is unchanged.

Screen: [preferences.md](../../05-ui/screen-requirements/preferences.md). Tests: [TESTPLAN-PRT-002](../../../test-cases/functional/portal-preferences/TESTPLAN-PRT-002.md).

**Reason:** a simple, scannable settings page; a personal accent lets each user make the platform their own without making it less professional.

### C68
**Decision (product owner, 2026-10-03) — Preferences as an enterprise settings navigation:** supersedes only the *layout* of [C67](#c67); its settings, storage and notification mapping are unchanged. The page uses the full content width with a **Preferences navigation** on the left (Appearance, Language & Formats, Notifications, Renewal Reminders — small icon, name, short hint) and **one focused settings panel** on the right showing only the selected section. Not a top tab bar, accordion, stacked long form or dashboard.
- Selected item: light accent background, accent icon and text. The selection is kept in `?section=` (refresh and deep links); arrow keys move through the navigation (vertical tab list).
- Rows: label and hint on the left, controls in one fixed 220 px column on the right so every control starts on the same edge; selects 220 px, days input 96 px, time 160 px, compact switches. Rows stack when the panel itself is narrower than 560 px (container query).
- Phones (below 900 px): the navigation becomes a **Section** selector above the panel. Tablets: narrower navigation without hints.
- Renewal Reminders: summary in a light box, **Save reminder settings** at the bottom-right of the panel.
- **International time zones (2026-10-03, product owner):** the time zone is a searchable picker over every IANA zone (not 7 presets), on Language & Formats **and** on Renewal Reminders; both edit the account time zone that reminder emails already use (`RenewalReminderService` reads `CustomerPreference.timeZone`, else the platform default). The admin platform-default reminder zone (Billing settings) uses the same picker. The backend already accepted any valid zone; no backend change.
- Small fixes found while checking the page: languages show their own names (English, Español) on this page; a browser time zone outside the short list is shown instead of a blank field; "Browser default" wording.

Screen: [preferences.md](../../05-ui/screen-requirements/preferences.md).

**Reason:** a focused, scannable settings experience consistent with enterprise SaaS products, without changing any behaviour.

### C69
**Decision (product owner, 2026-10-03) — Business dashboard redesign:** `/organization/business-dashboard` (organization admins) becomes a structured workspace: organization header (Identity Federation kept), welcome with live platform status and seat call-out, quick actions, four KPIs, usage analytics, organization health, your workspace, your account, an applications table, recent activity, attention required and a billing summary. Subtle motion (count-up, bar/donut/meter drawing, fade-in, hover lift), all switched off by the EIS *Reduce motion* preference or the OS `prefers-reduced-motion`.
- **Data rule:** every value comes from an existing API; nothing is invented. Sources: business dashboard, personal dashboard (launch URLs, recently used, favourites), service status, renewals, organization members (the signed-in user's role and status), organization audit log, sessions (current sign-in), permissions, MFA status, SAML/OIDC providers. Each section loads and fails on its own (Retry).
- **Omitted because the data does not exist:** a launches-over-time line chart and 7/30/90-day period filters (launches are stored only as a total and last-launch time per user and application — no history); "+N this month" and trend percentages on members and launches (no join dates or history); a separate *Support* category status and *Authentication*/*Billing* component statuses (not monitored); "View audit activity" (no organization audit page). The spend trend stays (it is computed from paid invoices). Filters offered instead: application and subscription status.
- **Moved, not removed:** members, groups, MFA policy and privileged-access approvals move unchanged to a new **Organization settings** page (`/organization/settings`, `MANAGE_ORGANIZATION`, new sidebar item). The full billing table is replaced by a billing summary linking to `/organization/billing`.
- **Interactions:** KPI cards, quick actions, bars (→ application details), table rows, service status, attention items and billing link to existing routes only. The C54 table filtering and sorting are kept; the donut filters the table.
- *First day of week* is removed from Preferences (product owner, 2026-10-03).

Screen: [business-dashboard.md](../../05-ui/screen-requirements/business-dashboard.md). Tests: [TESTPLAN-PRT-003](../../../test-cases/functional/business-dashboard/TESTPLAN-PRT-003.md).

**Reason:** the dashboard should answer "what is happening and what needs me" at a glance; detailed administration belongs in its own module.

### C70
**Decision (product owner, 2026-10-05) — Build keyword and semantic search now; FRDs approved:** improved keyword search ([REQ-PRT-002](../../02-requirements/FRD/global-search/requirement.md)) and semantic search ([REQ-PRT-003](../../02-requirements/FRD/semantic-search/requirement.md)) are built now, ahead of their planned sprint [2027.1.3](sprints/SPRINT-2027.1.3.md), whose dates do not change. Both FRDs are **Approved** with these defaults:
- **Keyword engine:** PostgreSQL full-text search + `pg_trgm`.
- **Semantic:** hybrid search with pgvector (as [C58](#c58)).
- **Embedding model:** option B — an open-source multilingual model (English + Spanish) running in the existing `ai-service`. Which model is **Not specified**; the index uses 384-dimensional vectors. Any key or setting for the model goes only in `config/secrets.env` (template `config/secrets.env.example`, empty).
- **Scope of semantic search:** public content only (catalog and documentation). User records (support tickets) use keyword search only.
- **Typo threshold, chunk size, similarity threshold:** tuned with the test set and documented — [quality report](../../02-requirements/FRD/semantic-search/quality-report.md).
- **No AI-written answers** above results (later).
- **Rules:** sprint dates unchanged; documents of a changed feature updated in the same commit; existing search, filters, sort and history keep working; results filtered to what the user may see before ranking, with a test proving one organization never sees another's records; anything not in the prompt is written as Not specified.

**Built (2026-10-05):** see the progress note on [SPRINT-2027.1.3](sprints/SPRINT-2027.1.3.md). Engineering defaults (Not specified in the prompt): the detailed specification `PROMPT-keyword-and-semantic-search.md` was not in the repository, and the product owner chose to build from the prompt alone; exact IDs are "#<number>"; "Did you mean" only when nothing matches, using words of public content only; a typo match needs every query word; up to 8 suggestions and 5 recent searches; searches count in history and insights only when the user runs them (Enter, a suggestion, "Did you mean", or arriving from the top bar); search-insight rows keep no user identity and have no retention limit; a new platform permission `MANAGE_SEARCH` (seeded for ADMIN) for rebuild, synonyms and insights; filters beyond type and paging beyond 50 results are Not specified.

**Limitation:** no real embedding model could be downloaded (huggingface.co is blocked in the build environment). Every semantic test and the quality report use the `stub` test model (word hashing), so the pipeline, fallback and speed are verified but the quality of a real model is not; the similarity threshold and chunk size must be re-tuned when a model is chosen. D8 (LLM provider for agents) is unaffected.

**Reason:** search is a daily tool for every user; ranking, word forms, typos and both languages make it useful now, and the semantic layer degrades to keyword search whenever the model is not there.

### C71
**Decision (product owner, 2026-10-05) — Who creates knowledge content:** only **platform administrators** and the **persons a platform administrator assigns** may create or upload knowledge content (articles, videos, documents, images, templates, study materials, release notes, FAQs, troubleshooting entries, glossary terms, courses). Everyone else only views content, according to its audience and visibility. Enforcement is on the backend for every create, upload-URL, update, publish, delete and replace call; every grant or removal of a knowledge permission and every content action is audited.
- **Proposed — confirm** ([REQ-KNW-008](../../02-requirements/FRD/knowledge-permissions/requirement.md) open questions): two platform permissions, *Knowledge contributor* (create and edit drafts, upload, metadata, transcripts, chapters, submit for review) and *Knowledge publisher* (review, approve, schedule, publish, unpublish, deprecate, archive, restore, delete, replace media, manage the knowledge search index). Following the reuse rule, the existing `MANAGE_KNOWLEDGE_BASE` becomes the publisher permission (no `KNOWLEDGE_PUBLISH` duplicate) and one new permission `KNOWLEDGE_CONTRIBUTE` is added. ADMIN keeps every knowledge permission.
- **Open:** may organization admins publish content visible only to their own organization? Default: **no**. How a platform administrator assigns a knowledge role to a person (no per-person platform-role screen exists; platform roles come from Keycloak client roles).

### C72
**Decision (product owner, 2026-10-05) — answer to D23, option A: file storage is AWS S3.** A **private bucket** (Block Public Access on, bucket owner enforced, versioning where appropriate). Uploads and downloads use **presigned URLs**; large files never pass through the EIS backend; AWS credentials never reach the browser or any API response; no permanent S3 URL is ever returned. Applies to knowledge videos, documents, images, audio and templates ([REQ-KNW-003](../../02-requirements/FRD/knowledge-media/requirement.md), [aws-s3.md](../../09-integrations/aws-s3.md)). New non-secret settings live under `eis.knowledge.*` with environment overrides; keys only in `config/secrets.env`. Product and platform images (`ProductImageService`, local disk) are **not** moved by this decision: whether 02.04 Product Content also uses S3 is Not specified.

### C73
**Decision (product owner, 2026-10-05) — Video sources:** `YOUTUBE`, `AWS_S3` and `EXTERNAL_URL`, behind an extensible provider design (`VideoSourceProvider`). YouTube details come from the YouTube Data API when a key is configured, otherwise from YouTube oEmbed ([REQ-KNW-004](../../02-requirements/FRD/knowledge-videos/requirement.md), [youtube.md](../../09-integrations/youtube.md)).

### C74
**Decision (product owner, 2026-10-05) — Knowledge taxonomy is data:** products, modules and categories used by knowledge content are backend data managed by publishers. The module lists in the Knowledge Center prompt (Valam.ai … Tharav.ai) are **initial seed data only**; nothing is hard-coded. Knowledge products reference the existing catalog products.

### C75
**Decision (product owner, 2026-10-05) — AI Knowledge Assistant prepared, not built:** the "Ask eVyoog Knowledge" panel design and API contract are specified now ([REQ-KNW-007](../../02-requirements/FRD/knowledge-assistant/requirement.md)); answer generation is **not built** until D8 (LLM provider) is decided. Until then the panel says "Coming soon" and the endpoint answers "not configured".

### C76
**Decision (product owner, 2026-10-05) — Knowledge search reuses platform search:** the Knowledge Center uses keyword and hybrid search (REQ-PRT-002, REQ-PRT-003, pgvector, D10/[C58](#c58), [C70](#c70)); no second search system and no second vector store. The search index is extended with the new knowledge types, transcripts and chapters, and with the audience rules, applied before ranking.

### C77
**Decision (product owner, 2026-10-05) — Knowledge Center placement and sprint dates:** the work belongs to application 11 (Knowledge Base, sprint [2027.1.1](sprints/SPRINT-2027.1.1.md); Learning, Training Delivery, Certification and video learning, sprint [2027.1.3](sprints/SPRINT-2027.1.3.md)) and to application 01 for search ([2027.1.3](sprints/SPRINT-2027.1.3.md)). **No sprint's planned dates change.** FRDs REQ-KNW-001 (update) to REQ-KNW-008 are written as **Draft** on 2026-10-05 (Phase 1, documents only); each is built only after the product owner replies "Approved" for it, and the affected sprint pages then get "Built early on <date>" notes with the commits. The Academy (11b, P1 stretch, C4/C5) is built only if the product owner confirms; otherwise its data model and navigation are prepared.

### C78
**Decision (product owner, 2026-10-05) — Knowledge Center FRDs approved: "start develop the code".** REQ-KNW-001 (update) and REQ-KNW-002 to REQ-KNW-008 are **Approved** and were built early on 2026-10-05 (commits `0ccbd12` and the docs commit that follows). The open questions were not answered, so the recommended answers were applied as defaults, each recorded in its FRD under "Answers applied on 2026-10-05"; the product owner can change any of them:
- permissions: new `KNOWLEDGE_CONTRIBUTE` (contributor) and the existing `MANAGE_KNOWLEDGE_BASE` as publisher; a person gets them through a Keycloak client role mapped to a platform role; organization admins do not publish;
- signed-out readers see Public content; route `/knowledge`; feedback needs sign-in;
- upload limits video 5 GB, document 100 MB, image 10 MB, audio 100 MB; URL expiry upload 15 min, download 5 min, playback 1 h; multipart above 100 MB; SVG not accepted (cannot be sanitised); no virus scanning yet;
- replaced/deleted objects deleted, bucket versioning keeps old versions (90 days proposed);
- YouTube: oEmbed unless `EIS_YOUTUBE_API_KEY` is set; automatic transcripts and thumbnails are integration points only;
- analytics: organization id plus a one-way reader hash on events, gap = ≥ 5 zero-result knowledge searches, lowest rated needs ≥ 5 votes;
- Academy (11b): data model prepared only, navigation hidden, built only if confirmed (C77);
- AI assistant: not built until D8; `ask` answers 501 ASSISTANT_NOT_CONFIGURED (C75).
**Still Not specified:** bucket names and region per environment; retention periods for events and replaced files; Developer and Partner audiences; error-code format owner; expiry notifications; organization-authored content. **Limitation:** no real S3 bucket was available in the build environment, so uploads, downloads and playback were tested against an in-memory store and the real AWS SDK signer (offline); the UAT script [UAT-KNW-003](../../../test-cases/UAT/knowledge/UAT-KNW-003-videos-and-s3.md) needs a test bucket.

### C79
**Decision (product owner, 2026-10-06) — Home button and sign-out destination:** "add home button on the navbar for website's landing page, after logout also it should go to the home page." Built on 2026-10-06 (frontend only; no sprint dates change):
- The signed-in top bar ([application layout](../../05-ui/screen-requirements/application-layout.md)) and the public website header (`SiteNavbar`, desktop and mobile menu) show a **Home** link to the website landing page `/`.
- A signed-in user who chooses Home (or the website logo) sees the website at `/`, with "My Workspace" / "Admin Panel" and Sign out in its header. Opening `/` any other way (typing the address, signing in, returning from SSO) still opens the tool, as before.
- **Sign out** from any page (sidebar, account menu, website header, mobile menu) ends the session and opens the website home page `/`.
- **Follow-up (product owner, 2026-10-06): "after logout there is no data related to that customer is visible in the screen."** Built the same day:
  - A session that ends any other way (sign-out in another tab or in PMS, expiry) also opens `/` (`SignedOutRedirect`), so no page keeps showing the person's data.
  - A session check that was already running when the person signed out can no longer put the old session back, and checks wait until the server-side logout has finished (`AuthProvider`). Before this fix, a check that happened to be in flight could sign the person straight back in.
  - The browser's recent knowledge searches are forgotten when a session ends; a cart count that arrives after sign-out is dropped.
  - Every API answer carries `Cache-Control: no-store` (Spring Security default, now covered by `NoCacheHeadersTest`), so Back or a shared computer cannot show cached customer data.
  - Device settings that are not customer data (theme, language, region, time zone) stay in the browser.

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
| C36, C37 | Create FRDs `platform-administration`, `tenant-lifecycle` (Approved); add to `SPRINT-2026.4.2.md` scope |
| C34, C35 | Invite/Create user (05.03.01.01/.02) and 05.04.02 Projects, carried again from 2026.4.2 — not built this sprint either; still pending a decision on the identity-creation flow and the Project resource model |
| C38 | Create FRD `subscription-lifecycle` (Approved); add to `SPRINT-2026.4.3.md` scope. 07.01.02 Change quantity/Schedule change, 07.02 Entitlement Management, 07.03 License & Quota Management, 07.04.01 Schedule/Notify/Auto-renew, organization-owned-subscription actions, and all of 08 Billing & Payments carried to a later sprint — still pending, respectively, a quantity/scheduling model, an entitlement-vs-product-access scoping decision, and a payment-provider decision |
| C39 | Create FRDs `order-lifecycle` (REQ-ORD-001), `knowledge-base` (REQ-KNW-001) (both Approved); add to `SPRINT-2027.1.1.md` scope. 09.02.01.02/.04/.05 (configure/suspend/deprovision an org subscription), 09.04.01.02/.06 (route/escalate approval), 11.01.02 AI Knowledge, 13b Connectors & Webhooks and 16 Analytics & Data Platform carried to a later sprint — still pending, respectively, the same organization-subscription permission gap C38 already carries, a multi-level approval requirement, an AI/embeddings-infrastructure decision, and a real first consumer/data pipeline for 13b/16 |
| C40 | Create FRDs `product-recommendations` (REQ-MKT-001), `ticket-management` (REQ-SUP-001) (both Approved); add to `SPRINT-2027.1.2.md` scope. 03.03 Marketplace Checkout, 12.03 SLA Management, 12.04 Incident & Problem Management, 04a AI Agent Orchestration (whole application, including the C21 addendum), and 10 Service & Resource Management (whole application, including the C20 retirement task) carried to a later sprint — still pending, respectively, a product-options/discount data model, an SLA-policy data model, a decision on Incident/Problem's relationship to the existing `service_incident` table, an agent/LLM framework choice, and real infrastructure for 10 to manage |
| C41 | Create FRDs `global-search` (REQ-PRT-002), `product-reviews` (REQ-MKT-002) (both Approved); add to `SPRINT-2027.1.3.md` scope. 01.03.01.02 Semantic search, 03.02 Product Evaluation, 03.04's Comparison sub-feature, 04b Customer-facing AI agents (+ 12.02 AI Support), 11b Learning & Certification, and 15b Policy Management & Compliance carried to a later sprint — still pending, respectively, an embeddings/vector-store decision, 10 Service & Resource Management actually existing to trial against, the same agent/LLM framework decision 04a/11.01.02 already carry, and (for 15b) a general policy-engine scoping decision; 11b has no dependents and no urgency |
| C42 | Create FRD `provider-onboarding` (REQ-PTR-001, Approved); add to `SPRINT-2027.2.1.md` scope. 14.02 Publisher Management, 14.03 Revenue Sharing and 14.04 Partner Operations carried to a later sprint — still pending, respectively, a product-ownership/publisher model, the same Billing (08) gap C38 already carries, and a Partner Manager role/identity decision |
| C43 | Add a note to `SPRINT-2027.2.2.md` that 15.05.01 Region Management is already satisfied by `REQ-GOV-001.2` (no new FRD). 15.05.02 Data Residency carried, with no further sprint scheduled — still pending the same general policy-engine decision (C41) and 10 Service & Resource Management (C40) every other carried item in this table already waits on |
| C44 | `docs/08-architecture/ui-ux-redesign.md` created (this decision's own writeup). The larger navigation consolidation (Account, Access Control, Catalog, My Products merges; shared `<DataTable>`/`<FilterBar>`; moving create-actions out of Settings) is carried — still pending someone picking it up as its own scoped pass, screen by screen, per that doc's own phased order |
| C45 | Update `docs/08-architecture/ui-ux-redesign.md`'s brand section to record the palette unification and that `evyoog.com` is still unreachable. The larger navigation consolidation carried by C44 remains carried — nothing in C45 built any of it |
| C46 | REQ-BIL-001 built (see C47 for the pragmatic assumptions made along the way). Add the Razorpay credentials to `config/secrets.env` when provided, then register the webhook (`/admin/billing/payment-gateway` shows the exact URL). Load `config/secrets.env` in `scripts/dev.sh` and the Docker `env_file` (BR-SEC-001) |
| C47 | Answer the FRD's own open questions (tax, invoice number/terms, organization-billing permission, invoice legal fields, refund/payment-method policy) as real product decisions, then update the code where this decision's assumption differs from the answer. Build the real Razorpay Customer/Token API integration before relying on saved payment methods for unattended auto-charging (also needed for D15). Replace the plain-text invoice/receipt document with a formatted one once Open question 8 is answered |
| C48 | If plan selection is ever added to individual self-serve subscribe (today there is none — `/me/subscriptions` always uses the product's single implicit plan), `/checkout/:productId` needs a plan picker before its billing-details step. The "newest OPEN invoice = the one just created" simplification would need a real subscription-scoped invoice lookup if this flow is ever used for more than one purchase at a time (e.g. a cart) |
| C50 | REQ-BIL-001 `requirement.md`: Open question 2 (billing scope) answered; Out of scope wording updated to "later, per C50". Price books (08.01.01), promotions (08.01.02) and usage billing (08.02.01) remain carried with no FRD and no sprint scheduled |
| C51 | Create FRD `tax-rules` (REQ-BIL-002, Draft; blocked by its own open questions — see the FRD); add to `SPRINT-2026.4.3.md` scope. Update REQ-BIL-001's Open question 1 (tax) to point to REQ-BIL-002, and its invoice-line requirement (REQ-BIL-001.2) to show the tax name/rate/amount and method used. Which external tax service to use is still **Not specified** — confirm with the product owner before REQ-BIL-002 can be approved |
| C52 | Create FRD `entitlements` (REQ-SUB-002, Draft); add to `SPRINT-2026.4.3.md` scope. 07.02.02 Quota (consumption/enforcement), 07.03 License & Quota Management (quantity, D14) and an entitlement grant/revoke history remain carried — still pending, respectively, usage metering (C50), a licensing/quantity decision, and a store this decision deliberately does not create |
| C53 | A real "last N periods" revenue/order trend chart (as already sketched, not built, for the Billing screen's own Overview tab) is carried — needs a new aggregation query this decision did not build. The platform admin dashboard's "top 5 products by launches" and the business dashboard's charts use data already returned by existing endpoints/queries; no further follow-up needed for those |
| C55 | REQ-BIL-001.18–.21 added (Draft); new screen `checkout-payment.md`, new `admin-billing-settings.md`, Record offline payment dialog in `admin-billing.md`; API and data model updated. Answer the open questions listed in C55 before approval. Re-verify the card-panel approach against Razorpay's documentation. In Phase 2, `/checkout?subscriptionId|invoiceId` replaces the C48 page `/checkout/:productId` and the Billing "Pay invoice" dialog (`billing-pay-invoice.md`) |
| C56 | Create FRD `provisioning-contract` (REQ-ORD-002, Draft); add it to `SPRINT-2027.1.1.md`. Confirm the proposed message shape with every hosted product team, and decide the delivery mechanism (D13 events or D19 webhooks), retries and ordering before approval |
| C57 | Create FRD `service-instances` (REQ-SRM-001, Draft); add it to `SPRINT-2027.1.2.md`. When it is built, switch the interim status page's per-product status ([C20](#c20), REQ-PRT-001) to service-instance health |
| C58 | Note pgvector on `SPRINT-2027.1.1.md` (11.01.02) and `SPRINT-2027.1.3.md` (semantic search). Choose the embedding model once D8 (LLM provider) is decided. The pgvector extension must be available on every environment's PostgreSQL 16 (local Docker image, AWS RDS) before either feature is built |
| C59 | Create FRD `cart-checkout` (REQ-MKT-003, Draft); add it to `SPRINT-2026.4.3.md` and mark 03.03 as moved out of `SPRINT-2027.1.2.md`. New screens `cart.md` and `payment-brand-assets.md`; `checkout-payment.md` redesigned; cart icon in `application-layout.md`; REQ-BIL-001.18 updated for the redesigned checkout (Draft). Answer the REQ-MKT-003 open questions before approval |
| C60 | Confirm the Billing settings field list (REQ-BIL-001 Open question 11) and that GSTIN/PAN/CIN are the registrations to print (Open question 8). Decide whether Pay by invoice is allowed per customer or organization (Open question 9) on top of the global switch. Decide whether BR-SEC-001 should ever allow entering Razorpay keys on a screen. A PDF invoice layout using the issuer block is still carried (C47) |
| C61 | FRD and sprint rows done; built 2026-10-03 (API keys, rate limits, `/v1`, admin usage) with engineering defaults. Remaining: answer REQ-INT-001 open questions (who gets keys, rate-limit values, versioning scheme, scopes). When D42 adds a cloud gateway, move or remove overlapping rate-limit/key logic |
| C62 | FRD and sprint rows done; built 2026-10-03 (outbox, dispatcher, admin events view). Remaining: answer retry schedule, retention and failure-notification questions. Connect provisioning (REQ-ORD-002) and webhooks (D19) as handlers when they are built |
| C63 | FRD and sprint rows done; built 2026-10-03 (seat quantity, change, enforcement). Remaining: answer billing of mid-term seat changes, who may change seats, pool vs named seats, and whether seat changes need order approval |
| C64 | FRD and sprint rows done; built 2026-10-03 (auto-renew job, renewal invoice, reminders, user and platform settings). Remaining: answer the eight REQ-SUB-004 open questions. Unattended charging of a saved method needs the Razorpay Customer/Token integration ([C47](#c47) follow-up) |
| C65 | Create FRD `access-management` (REQ-TEN-005, Draft) with UI, API, data model and UAT documents; add to `SPRINT-2026.4.2.md` and, for 09.02.01, `SPRINT-2027.1.1.md`; point REQ-BIL-001 Open question 5 here. Answer the REQ-TEN-005 open questions, then build (organization subscription actions behind *Manage subscriptions*, Access management and Roles & permissions screens). Build waits for approval |
| C66 | FRD, screens, API, data model and test cases done; built 2026-10-03. Remaining: apply the design to the screens listed as *Not redesigned*, one area at a time, each with its own screen spec; decide whether release versions should be tracked (needs data and an FRD) before any Versions section is shown |
| C67 | Built 2026-10-03 (frontend only). Remaining: decide whether accent and formats should follow the user across devices (needs `customer_preferences` columns and an API change); route the screens that still format dates directly with `toLocale…` through the shared formatters; decide whether support replies need their own notification category |
| C68 | Built 2026-10-03 (frontend layout only). Same remaining items as C67 |
| C69 | Built 2026-10-03 (frontend only). Remaining: decide whether to record launch events (a small additive table and endpoint) so a launches-over-time chart and period filters can be shown with real data; an organization audit-log page for "View audit activity" |
| C70 | Built 2026-10-05; FRDs REQ-PRT-002 (C70 additions) and REQ-PRT-003 Approved; screens, API, data model, test cases, application page 01 and sprint 2027.1.3 updated. Remaining: choose the embedding model, allow its download (huggingface.co) or provide it as a file, set `EMBEDDING_*` in config/secrets.env, rebuild with re-embed, re-run the quality report and re-tune the similarity threshold and chunk size; apply V020 to each database (pgvector may need to be enabled on RDS); decide a retention period for search insights |
| C71 | Built 2026-10-05 (C78 defaults). FRD REQ-KNW-008. Remaining: confirm the two permissions, the org-admin question and how a person is given a knowledge role; then build phase 1 |
| C72 | Built 2026-10-05 (C78 defaults). FRD REQ-KNW-003, `docs/09-integrations/aws-s3.md`. Remaining: bucket names and region per environment, size limits, URL expiries, retention of replaced files; whether product images (02.04) move to S3 |
| C73 | Built 2026-10-05 (oEmbed + optional key). FRD REQ-KNW-004, `docs/09-integrations/youtube.md`. Remaining: YouTube Data API key or oEmbed only |
| C74 | Built 2026-10-05: taxonomy tables and seed data |
| C75 | Built 2026-10-05 as "not configured". Remaining: D8 |
| C76 | Built 2026-10-05: public knowledge of every type, transcripts and chapters in the platform index; restricted content searched among visible items only |
| C77 | Sprint pages 2027.1.1 and 2027.1.3 (and 2026.4.1 for D23, 2027.1.2 for ticket prefill) note the Draft FRDs; dates unchanged. Remaining: "Approved" per FRD, then build phases with "Built early" notes |
| C78 | Built 2026-10-05 (all knowledge FRDs Approved with default answers). Remaining: confirm or change the defaults; bucket names and region; retention periods; run UAT-KNW-001–004 with a test bucket |
| C79 | Built 2026-10-06 (with the follow-up: no customer data after sign-out); [application layout](../../05-ui/screen-requirements/application-layout.md) updated; TC-PRT-032 to TC-PRT-034 |
| C54 | The wider navigation-consolidation / shared `<DataTable>`/`<FilterBar>` pass ([C44](#c44)) would make the business dashboard's new click-to-filter table and sort behavior reusable elsewhere instead of page-local — still carried, same as before. `database/seed/README.md` now points to [docs/07-database/demo-data.md](../../07-database/demo-data.md) for exactly what `DemoDataSeeder` adds |

**Done (2026-09-26), no longer follow-up:** C31 (sprint pages 2026.4.1 through 2027.2.2, and the "Sprint" field on application pages 02, 03, 04, 05, 10, 11, 12, 13, 15, all updated to the corrected sequence — this superseded the older "C21: add the general policy engine to `SPRINT-2027.2.2.md`" and "C16, C17: add the deferred functions to `SPRINT-2027.1.3.md`" rows, and the "C21: add the agent controls to `SPRINT-2027.1.2.md`" row, which are now folded into C31's own sprint pages); C32–C35 (FRDs written and Approved, sprint 2026.4.1 built, carry-over recorded on `SPRINT-2026.4.2.md`).
