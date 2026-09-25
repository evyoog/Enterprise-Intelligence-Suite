# eVyoog EIS: Analysis of the Three Planning Documents

**Status:** Analysis. Not an approved requirement. All content comes from the three source documents below. Where a document does not say something, this analysis says **"Not specified."** Anything that is an inference, not a statement from a document, is labelled **Inference**.

## Source documents and reference codes

| Code | File | Type | What was read |
|------|------|------|---------------|
| **[PO]** | `2026Q3_ProdOps_Plan.docx` | Word, 8 tables | Every paragraph, all 8 tables, the cover-page text boxes. There are no comments, footnotes or images. |
| **[CG]** | `eVyoog_EIS_Platform_-_ChatGPT2.docx` | Word, 5 tables, about 2,300 text lines | Every paragraph, all 5 tables, all preformatted diagrams, the cover-page text boxes. There are no comments, footnotes or images. |
| **[WB:\<sheet\>]** | `Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx` | Excel, 18 sheets | Every non-empty cell of every sheet. There are no hidden sheets, cell comments, data validations or defined names. |

Sheet references use the exact sheet name, for example **[WB:Functions]**. IDs such as `02.01.01.03` are the workbook's own IDs.

---

## Contents
1. Document-by-document analysis
2. Excel workbook, sheet by sheet
3. Cross-document analysis: links, duplicates, gaps, conflicts
4. Every sprint in every document
5. Roadmap view: Application → Capability → Feature → Requirement → PI → Sprint → Deliverable
6. Final consolidated analysis
- Appendix A: [CG] application breakdown, verbatim
- Appendix B: all 444 workbook functions, grouped by sprint
- Appendix C: how this repository already lines up with the plan

---

# 1. Document-by-document analysis

## 1.1 [PO] `2026Q3_ProdOps_Plan.docx`: "Enterprise Vyoog (eVyoog) - Product Planning"

**What it is:** The official product-operations plan. It is the **only** document that contains **PI / sprint dates**.

**Cover page (text boxes):** "Ganesh", "[Company name] | [Company address]" (unfilled template placeholders), "Product Planning", "evyoog".

**Structure (headings in order):**
1. eVyoog EIS (PaaS)
2. eVyoog EIS - Vision and Strategic Context
3. eVyoog EIS - Roadmap Initiatives
4. Thittam (SaaS Product), with Vision and Strategic Context and a list of applications
5. Macro Planner, with Capabilities & Features and Roadmap Initiatives
6. Agile Planner, with Capabilities & Features and Roadmap Initiatives
7. Thiran (SaaS Product), with Vision and Strategic Context (heading only) and Thiran Applications
8. SW Life Cycle, with Capabilities & Features and Roadmap Initiatives
9. Tharav, Valam, Varthan and Yukth (SaaS Products), each with a Vision heading and a Roadmap heading

### 1.1.1 EIS (PaaS): vision (verbatim substance)
- EIS is "a **multi-lingual, multi-regional, scalable platform** that has **marketplace options**, **integrate with other platforms** and offered as **Platform as a Service (PaaS)**".
- It enables customers "to subscribe to various **Products, Solutions, Services**, etc. offered in a **SaaS** format".
- Customers can:
  - view the offerings
  - understand them through **training materials**
  - select products through **guided AI agents**
  - **self-service subscribe** to products and plans
  - **create and manage their business account and user accounts with roles and privileges**
  - **manage payments**
  - get **technical support through AI agents**
- "EIS platform can scale to include the following applications with capabilities listed below", followed by the table of 16 applications.

### 1.1.2 EIS: 16 applications and 69 capabilities ([PO] Table 1)

| Application ID | Application | Description | Capabilities |
|---|---|---|---|
| 01 | Experience & Customer Portal | Customer-facing web/mobile experience | Portal Experience; Customer Dashboard; Global Search; Notifications & Communications |
| 02 | Product & Catalog Management | Products, solutions, services, plans and content | Product Management; Offering Management; Plan Management; Product Content; Localization |
| 03 | Marketplace | Discovery, evaluation, comparison and checkout | Product Discovery; Product Evaluation; Marketplace Checkout; Reviews & Ratings |
| 04 | AI Advisor & Agent Platform | AI-guided selling, technical assistance and support | AI Product Advisor; AI Sales Agent; AI Technical Advisor; AI Support Agent; AI Agent Orchestration |
| 05 | Customer / Tenant Management | Organizations, tenants, users and projects | Organization Management; Tenant Management; User Management; Group & Project Management |
| 06 | Identity & Access Management | Authentication, authorization, roles and policies | Authentication; Authorization; Privileged Access; Identity Federation |
| 07 | Subscription & Entitlement Management | Subscriptions, licenses, quotas and entitlements | Subscription Management; Entitlement Management; License & Quota Management; Renewal & Lifecycle |
| 08 | Billing & Payments | Pricing, billing, invoices, taxes and payments | Pricing; Billing; Payment; Financial Documents; Tax & Currency |
| 09 | Order & Provisioning Management | Orders, provisioning and workflow orchestration | Order Management; Provisioning; Workflow Orchestration; Approval Management |
| 10 | Service & Resource Management | Service instances and cloud/platform resources | Service Management; Resource Management; Configuration Management; Monitoring & Health |
| 11 | Training & Knowledge Management | Documentation, courses, labs and certifications | Knowledge Base; Learning Management; Training Delivery; Certification |
| 12 | Support & Service Management | AI/human support, incidents, requests and SLAs | Support; AI Support; SLA Management; Incident & Problem Management |
| 13 | Integration & API Platform | APIs, connectors, events and webhooks | API Management; Integration Hub; Event Platform; Webhooks |
| 14 | Partner & Provider Management | Providers, publishers, onboarding and revenue sharing | Provider Onboarding; Publisher Management; Revenue Sharing; Partner Operations |
| 15 | Administration & Governance | Platform configuration, policies, audit and compliance | Platform Administration; Policy Management; Audit; Compliance; Regional Operations |
| 16 | Analytics & Data Platform | Customer, product, operational and business analytics | Customer Analytics; Product Analytics; Operational Analytics; Business Analytics; Data Platform |

The count of 69 capabilities comes from splitting the semicolon lists. This table is **word-for-word identical** to **[WB:Application Summary]**.

### 1.1.3 EIS: roadmap ([PO] Table 2)

| Application ID | Application | PI – CY Quarter | Sprint |
|---|---|---|---|
| 01 | Experience & Customer Portal | 2026.3 | 2026.3.3 |
| 02 | Product & Catalog Management | 2026.4 | 2026.4.1 |
| 03 | Marketplace | 2027.1 | 2027.1.3 |
| 04 | AI Advisor & Agent Platform | 2027.1 | 2027.1.2 |
| 05 | Customer / Tenant Management | 2026.4 | 2026.4.2 |
| 06 | Identity & Access Management | 2026.3 | 2026.3.3 |
| 07 | Subscription & Entitlement Management | 2026.4 | 2026.4.3 |
| 08 | Billing & Payments | 2026.4 | 2026.4.3 |
| 09 | Order & Provisioning Management | 2027.1 | 2027.1.1 |
| 10 | Service & Resource Management | 2027.1 | 2027.1.1 |
| 11 | Training & Knowledge Management | 2027.1 | 2027.1.3 |
| 12 | Support & Service Management | 2027.1 | 2027.1.3 |
| 13 | Integration & API Platform | 2027.1 | 2027.1.1 |
| 14 | Partner & Provider Management | 2027.2 | 2027.2.1 |
| 15 | Administration & Governance | 2027.2 | 2027.2.2 |
| 16 | Analytics & Data Platform | 2027.1 | 2027.1.1 |

Only **one PI and one sprint per application** are given. The document does not say whether this is the start sprint, the delivery sprint, or the whole scope delivered in one sprint. **Not specified.**

### 1.1.4 Thittam (SaaS product)
- Thittam "will be hosted on a Platform (PaaS)".
- It "offers multiple planning applications supporting high level planning for an **organization, division, unit, team(s), individuals** and tracking of progress and completion."
- **Vision:** "scalable and configurable extending to multi lingual, multi-regional, marketplace options, integration with other platforms, SaaS applications, etc."
- **Applications:** **Macro Planner** and **Agile Planner**.

#### Macro Planner: capabilities and features ([PO] Table 3, verbatim)

| # | Capabilities | Primary Purpose | Features |
|---|---|---|---|
| 1 | Organization & Identity | Organization, divisions, units, teams, users, roles | Organization creation / Organization profile / Organization hierarchy / Division management / Business-unit management / Department management / Location management / Cost-center management   /  / User provisioning   / Authentication   / Authorization   / Roles   / Permissions   / Groups   / SSO   / |
| 2 | Planning & Portfolio | Create/manage plans and planning hierarchy | Create plan   / Plan types   / Plan hierarchy   / Plan versioning   / Plan lifecycle   / Plan ownership   / Plan status   /  / Hierarchical planning   / Parent/child plans   / Cross-plan relationships   / Roll-up planning   / Cascading targets   / Cascading status   /  / Annual planning   / Quarterly planning   / Monthly planning   / Weekly planning   / Fiscal calendars   / Custom periods |
| 3 | Work Management | Tasks, activities, assignments, status, priorities | Tasks   / Subtasks   / Activities   / Assignments   / Priority   / Status   / Due dates   / Tags   / Checklists  /  / Kanban   / Planner board   / List   / Grid   / Calendar   / Timeline   / Gantt |
| 4 | Schedule & Dependency | Dates, milestones, dependencies, critical path | Milestones   / Gates   / Deliverables   / Approval points   /  / Start/end dates   / Duration   / Calendar   / Working days   / Baselines   / Forecast dates   / |
| 5 | Resource Management | People, teams, capacity, allocation | Team creation   / Team membership   / Team hierarchy   / Team roles   / Team responsibilities   / Team capacity  /  / Available capacity   / Planned capacity   / Allocated capacity   / Utilization   / Over-allocation   / |
| 6 | Goal & KPI Management | Objectives, KPIs, targets, actuals | KPI definition   / KPI target   / Actual   / Forecast   / Threshold   / Variance   / Trend |
| 7 | Template Management | Business/project/production/etc. plan templates | Business Plan   / Sales Plan   / Marketing Plan   / HR Plan   / Finance Plan   / Production Plan   / Purchase Plan   / Delivery Plan   / Project Plan   / Product Plan   / Training Plan   / Strategic Plan   / Operational Plan   / Capacity Plan   / Resource Plan   / Quality Plan   / Risk Plan   / Compliance Plan   / |
| 8 | Collaboration | Comments, discussions, notifications, documents | Comments   / Mentions   / Discussions   / Notifications   / Activity feed   / @user   / @team |
| 9 | Workflow & Automation | Rules, approvals, triggers, automated actions | Event engine   / Rules engine   / Workflow engine   / Approval engine   / Notification engine   / Scheduled jobs   / Automation actions |
| 10 | Analytics & Reporting | Dashboards, reports, progress, variance | Gantt chart   / Critical path   / Baseline   / Actual vs planned   / Schedule variance   / |
| 11 | Integration & API | ERP, CRM, ALM, Agile, HR, finance, external SaaS |  |
| 12 | Marketplace & Extensions | Apps, connectors, templates, plugins |  |
| 13 | Administration & Configuration | Tenant/platform configuration |  |
| 14 | Localization | Language, region, timezone, currency, calendar |  |
| 15 | Platform Operations | Security, audit, monitoring, billing, subscription |  |

Capabilities #11 to #15 have **no features listed** (Not specified).

#### Macro Planner: roadmap ([PO] Table 4)

| # | Capabilities | PI – CY Quarter | Sprint |
|---|---|---|---|
| 1 | Organization & Identity | 2026.3 | 2026.3.3 |
| 2 | Planning & Portfolio | 2026.3 | 2026.3.3 |
| 3 | Work Management | 2026.3 | 2026.3.3 |
| 4 | Schedule & Dependency | 2026.3 | 2026.3.3 |
| 5 | Resource Management | 2026.3 | 2026.3.3 |
| 6 | Goal & KPI Management | 2026.4 | 2026.4.2 |
| 7 | Template Management | 2026.4 | 2026.4.3 |
| 8 | Collaboration | 2026.4 | 2026.4.2 |
| 9 | Workflow & Automation | 2026.4 | 2027.4.1 |
| 10 | Analytics & Reporting | 2026.4 | 2027.4.1 |
| 11 | Integration & API | 2027.1 | 2027.1.1 |
| 12 | Marketplace & Extensions | 2027.1 | 2027.1.3 |
| 13 | Administration & Configuration | 2027.1 | 2027.1.1 |
| 14 | Localization | 2027.1 | 2027.1.2 |
| 15 | Platform Operations | 2027.1 | 2027.1.3 |

#### Agile Planner: capabilities and features ([PO] Table 5, verbatim)

| ID | Capability | Features |
|---|---|---|
| AP-C01 | Organization & Tenant Management | Multi-Tenant Organization / User Management / Role & Permission Management |
| AP-C02 | Portfolio / Program Management | Portfolio / Program / Goal Management |
| AP-C03 | Application / Product Management | Application/Product Registry / Product Planning |
| AP-C04 | Capability & Feature Management | Capability Management / Feature Management |
| AP-C05 | Function & Backlog Management | Function Management |
| AP-C06 | Agile Planning & Sprint Management | Sprint Management |
| AP-C07 | Team & Resource Management | Teams |
| AP-C08 | Board Management | BOARD |
| AP-C09 | Workflow & State Management | Configurable workflow engine |
| AP-C10 | Work Assignment & Collaboration | Assign work   / Reassign work   / Followers   / Comments   / Mentions   / Attachments   / Checklist   / Activity history   / Notifications   / Work log   / Time tracking   / Approval   / Escalation |
| AP-C11 | Progress & Status Management |  |
| AP-C12 | Dependency & Risk Management | Work dependency   / Feature dependency   / Team dependency   / Application dependency   / External dependency   / Blocking relationship   / Risk   / Issue   / Assumption   / Decision   / Escalation |
| AP-C13 | Metrics, Dashboards & Reporting |  |
| AP-C14 | ALM Integration & Traceability |  |
| AP-C15 | MACRO PLANNER Integration |  |
| AP-C16 | Automation & Notifications |  |
| AP-C17 | Administration, Configuration & Security |  |
| AP-C18 | Marketplace / Integration Platform |  |

AP-C11, AP-C13, AP-C14, AP-C15, AP-C16, AP-C17 and AP-C18 have **no features listed** (Not specified).

#### Agile Planner: roadmap ([PO] Table 6)

| ID | Capability | PI – CY Quarter | Sprint |
|---|---|---|---|
| AP-C01 | Organization & Tenant Management | 2026.3 | 2026.3.3 |
| AP-C02 | Portfolio / Program Management | 2026.3 | 2026.3.3 |
| AP-C03 | Application / Product Management | 2026.3 | 2026.3.3 |
| AP-C04 | Capability & Feature Management | 2026.3 | 2026.3.3 |
| AP-C05 | Function & Backlog Management | 2026.3 | 2026.3.3 |
| AP-C06 | Agile Planning & Sprint Management | 2026.3 | 2026.3.3 |
| AP-C07 | Team & Resource Management | 2026.3 | 2026.3.3 |
| AP-C08 | Board Management | 2026.3 | 2026.3.3 |
| AP-C09 | Workflow & State Management | 2026.3 | 2026.3.3 |
| AP-C10 | Work Assignment & Collaboration | 2026.3 | 2026.3.3 |
| AP-C11 | Progress & Status Management | 2026.4 | 2026.4.2 |
| AP-C12 | Dependency & Risk Management | 2026.4 | 2026.4.3 |
| AP-C13 | Metrics, Dashboards & Reporting | 2026.4 | 2026.4.2 |
| AP-C14 | ALM Integration & Traceability | 2026.4 | 2027.4.1 |
| AP-C15 | MACRO PLANNER Integration | 2026.4 | 2027.4.1 |
| AP-C16 | Automation & Notifications | 2027.1 | 2027.1.1 |
| AP-C17 | Administration, Configuration & Security | 2027.1 | 2027.1.2 |
| AP-C18 | Marketplace / Integration Platform | 2027.1 | 2027.1.3 |

### 1.1.5 Thiran (SaaS product)
- Thiran "will be hosted on a Platform (PaaS)".
- It "offers multiple applications to efficiently develop and execute **Projects, Products, Production orders**, etc. … by **configurable workflow** and enables tracking the progress and on time completion."
- The **Vision** heading has **no text** (Not specified).
- **Applications:** SW Life Cycle Management, Product Life Cycle Management, Production Management, Service Management.
  - Only **SW Life Cycle** is detailed.
  - Product Life Cycle Management, Production Management and Service Management have **no capabilities, features or roadmap** (Not specified).

#### SW Life Cycle: description (verbatim substance)
- It "will be hosted on an **in-house Platform (PaaS)**".
- It integrates with in-house applications:
  - **Macro Planner**, "to derive the Product - Application - Capabilities - Features breakdown"
  - **Agile Planner (SaaS application)**, "to derive the Features, Functions, Backlog, Sprints, Team Assignment, planned release, etc."
- It must be "scalable and configurable (multilingual, multi-regional, marketplace options, integration with other platforms, SaaS applications, etc.)".
- Teams use it "to create, manage and release **Requirements, Design, API specs, Test Cases, AI agents, Approvals, Traceability, workflow**, etc. in a configurable / customizable environment."
- AI integration:
  - "automatically generate code, design diagrams, test cases, reports, dashboard, etc."
  - "perform automated testing"
  - "deployment"

#### SW Life Cycle: capabilities ([PO] Table 7). The Features column is empty for **all 23** rows.

| ID | Capability | Features |
|---|---|---|
| SWLC-CAP-01 | Platform & Tenant Management | Not specified |
| SWLC-CAP-02 | Organization & Project Management | Not specified |
| SWLC-CAP-03 | Lifecycle / Work Item Management | Not specified |
| SWLC-CAP-04 | Requirements Management | Not specified |
| SWLC-CAP-05 | Architecture & Design Management | Not specified |
| SWLC-CAP-06 | API & Interface Management | Not specified |
| SWLC-CAP-07 | Software Configuration / Code Management | Not specified |
| SWLC-CAP-08 | Test Management | Not specified |
| SWLC-CAP-09 | Defect & Issue Management | Not specified |
| SWLC-CAP-10 | Traceability & Impact Analysis | Not specified |
| SWLC-CAP-11 | Workflow & Approval Management | Not specified |
| SWLC-CAP-12 | Change & Configuration Management | Not specified |
| SWLC-CAP-13 | Release & Deployment Management | Not specified |
| SWLC-CAP-14 | Documentation Management | Not specified |
| SWLC-CAP-15 | Risk & Compliance Management | Not specified |
| SWLC-CAP-16 | Reporting & Analytics | Not specified |
| SWLC-CAP-17 | Collaboration & Review | Not specified |
| SWLC-CAP-18 | AI Engineering Platform | Not specified |
| SWLC-CAP-19 | Automation Platform | Not specified |
| SWLC-CAP-20 | Integration & Marketplace | Not specified |
| SWLC-CAP-21 | Templates & Methodologies | Not specified |
| SWLC-CAP-22 | Administration & Governance | Not specified |
| SWLC-CAP-23 | Localization & Regionalization | Not specified |

#### SW Life Cycle: roadmap ([PO] Table 8)

| ID | Capability | PI – CY Quarter | Sprint |
|---|---|---|---|
| SWLC-CAP-01 | Platform & Tenant Management | 2026.3 | 2026.3.3 |
| SWLC-CAP-02 | Organization & Project Management | 2026.3 | 2026.3.3 |
| SWLC-CAP-03 | Lifecycle / Work Item Management | 2026.3 | 2026.3.3 |
| SWLC-CAP-04 | Requirements Management | 2026.3 | 2026.3.3 |
| SWLC-CAP-05 | Architecture & Design Management | 2026.3 | 2026.3.3 |
| SWLC-CAP-06 | API & Interface Management | 2026.3 | 2026.3.3 |
| SWLC-CAP-07 | Software Configuration / Code Management | 2026.3 | 2026.3.3 |
| SWLC-CAP-08 | Test Management | 2026.3 | 2026.3.3 |
| SWLC-CAP-09 | Defect & Issue Management | 2026.3 | 2026.3.3 |
| SWLC-CAP-10 | Traceability & Impact Analysis | 2026.3 | 2026.3.3 |
| SWLC-CAP-11 | Workflow & Approval Management | 2026.4 | 2026.4.1 |
| SWLC-CAP-12 | Change & Configuration Management | 2026.4 | 2026.4.2 |
| SWLC-CAP-13 | Release & Deployment Management | 2026.4 | 2026.4.2 |
| SWLC-CAP-14 | Documentation Management | 2026.4 | 2026.4.3 |
| SWLC-CAP-15 | Risk & Compliance Management | 2026.4 | 2026.4.3 |
| SWLC-CAP-16 | Reporting & Analytics | 2027.1 | 2027.1.1 |
| SWLC-CAP-17 | Collaboration & Review | 2027.1 | 2027.1.1 |
| SWLC-CAP-18 | AI Engineering Platform | 2027.1 | 2027.1.2 |
| SWLC-CAP-19 | Automation Platform | 2027.1 | 2027.1.2 |
| SWLC-CAP-20 | Integration & Marketplace | 2027.1 | 2027.1.3 |
| SWLC-CAP-21 | Templates & Methodologies | 2027.1 | 2027.1.3 |
| SWLC-CAP-22 | Administration & Governance | 2027.1 | 2027.1.3 |
| SWLC-CAP-23 | Localization & Regionalization | 2027.1 | 2027.1.3 |

### 1.1.6 Tharav, Valam, Varthan, Yukth
Each has only three empty headings: "(SaaS Product)", "Vision and Strategic Context" and "Roadmap Initiatives". The description, applications, capabilities and roadmap for all four are **Not specified**.

### 1.1.7 Summary of [PO]
| Item | Content |
|------|---------|
| Purpose | A product-planning baseline, with sprint allocation for the EIS PaaS and its hosted SaaS products |
| Products covered with detail | EIS (16 applications), Thittam (Macro Planner with 15 capabilities, Agile Planner with 18), Thiran (SW Life Cycle with 23) |
| Products named only | Tharav, Valam, Varthan, Yukth. Also Thiran's Product Life Cycle Management, Production Management and Service Management |
| Time span | PI 2026.3 to PI 2027.2 (sprint IDs run from 2026.3.3 to 2027.4.1, see section 4) |
| Requirements | None in the form of REQ-IDs. Capabilities and features only |
| Deliverables | Not specified |
| Dependencies | Only the stated SW Life Cycle integration with Macro Planner and Agile Planner, plus Agile Planner capability AP-C15 "MACRO PLANNER Integration" |

---

## 1.2 [CG] `eVyoog_EIS_Platform_-_ChatGPT2.docx`: "eVyoog EIS Platform"

**What it is:** A conversation transcript. It contains the product owner's prompts and ChatGPT's architecture recommendations. It is the **conceptual source** that the workbook and the application list in [PO] were derived from.

**Cover page (text boxes):** "Ganesh", "Vyoog | India", "eVyoog EIS PaaS", "CHatgpt Contents".

### 1.2.1 The original prompt (product owner's requirement, verbatim substance)
- "develop a SaaS Platform similar to AWS (multi lingual, multi regional, scalable, market place options, integration with other platforms, etc.)"
- "offer Platform as a Service by listing my Products, Solutions, Services, etc."
- The same customer capabilities as the [PO] vision.
- "breakdown this platform (SW product) as scalable applications, applications breakdown into capabilities, capabilities breakdown into features and features breakdown into functions".

A second prompt comes later: "What generative AI agents are required to support the PaaS and how they can be integrated through MCP or relevant APIs?"

### 1.2.2 Architecture recommendations, in document order
| # | Section | Key content |
|---|---------|-------------|
| 1 | Framing | A "Cloud/SaaS Marketplace + PaaS". It compares itself to AWS Marketplace, AWS Console, Salesforce AppExchange, Azure, Stripe and ServiceNow. Hierarchy: **Platform → Applications → Capabilities → Features → Functions → APIs / Services**. First-class objects: **Product, Customer, Subscription, Identity, Entitlement, Billing, Tenant**. |
| 2 | Recommended Platform Architecture | A table of **16 applications** (same order as [PO]). Application 1 is named "Experience & Portal" in the table and "Experience & Customer Portal" in its heading. |
| 3 | Applications 1 to 16 | **50 capabilities**, each with features and/or functions. The full text is in **Appendix A**. |
| 4 | Cross-Cutting Platform Capabilities | "should not be duplicated inside each application". **18 shared services** under Platform Foundation: Identity, Authorization, Tenant Management, Configuration, Notification, Search, Workflow, Event Bus, API Gateway, File/Object Storage, Audit, Observability, AI/LLM Gateway, Localization, Currency, Tax, Feature Flags, Secrets Management, Compliance. |
| 5 | Multi-Regional Architecture | Region is first-class. **Regions:** North America (US-East, US-West, Canada), Europe (EU-West, EU-Central), Asia Pacific (India, Singapore, Japan, Australia), Middle East (UAE). **Global Control Plane:** customer identity, product catalog, marketplace, global configuration, product metadata, billing policies, global AI agents, partner management. **Regional Data/Service Plane:** customer data, service instances, resource provisioning, regional billing, regional integrations, regional support, data residency. |
| 6 | Multi-Language Architecture | "Don't build language support directly into individual screens." A Localization Platform covering UI, product, documentation, knowledge base, email, notification, AI response, date/time, currency, number formatting and regional terminology. Example languages: English, Spanish, French, German, Japanese, Hindi. |
| 7 | The Most Important Domain Model | Organization contains Tenant (Users, Groups, Roles, Projects), Billing Account, and Subscriptions. A Subscription contains Product, Plan, Entitlements and Service Instances, and Service Instances contain Resources. Separately, a Provider owns Products, and a Product has Offering, Plan, Pricing, Documentation, Training and Support. |
| 8 | End-to-End Customer Journey | 16 steps: Discover → Understand → Ask AI Advisor → Evaluate → Compare → Select → Configure → Calculate Price → Subscribe → Pay → Provision → Activate → Use → Monitor → Get AI Support → Scale/Upgrade → Renew. |
| 9 | Example breakdown | Marketplace → Product Discovery → AI Product Recommendation, then 14 functions: collect requirements, understand intent, identify business objectives, identify constraints, search catalog, filter eligible, evaluate compatibility, rank, generate recommendation, explain, show alternatives, calculate estimated cost, recommend configuration, generate purchase journey. "That is the level at which your engineering teams can eventually start creating epics → user stories → APIs → services → test cases." |
| 10 | Recommended Product Decomposition Hierarchy | PLATFORM → APPLICATION → CAPABILITY → FEATURE → FUNCTION → API/Service, with Business Rules, Data Objects, User Journeys and Cross-Cutting Services. The extended form is: Platform → Domain → Application → Capability → Feature → Function → Business Rule → API → Microservice/Module → Data Entity → Event. |
| 11 | Suggested Initial MVP | **Phase 1 (8 applications):** Customer Portal, Product & Catalog, Marketplace, Customer/Tenant, IAM, Subscription & Entitlement, Billing & Payments, AI Advisor. **Phase 2:** Order & Provisioning, Service & Resource, Knowledge & Training, Support. **Phase 3:** Integration Platform, Partner Marketplace, Governance, Analytics/Data Platform. Includes a layer diagram: Customer Portal → Marketplace / AI Advisor / Customer Mgmt → Platform Services → IAM / Subscription / Billing / Workflow / Integration → Service Platform → Compute / Data/DB / APIs. |
| 12 | One Important Architectural Principle | Business capabilities come first and microservice boundaries afterwards. Example: Subscription Management could contain Subscription, Entitlement, Plan, Pricing, Quota and Renewal services. |
| 13 | The Next Artifact | A **Product Capability Map / PBS** with numbered IDs (01 → 01.01 → 01.01.01 → 01.01.01.01). The proposed per-function fields are Application, Capability, Feature, Function, Actor, Business Rule, Input, Output, API, Event, Data Entity, Permission, Region, Localization, AI Required, Priority and MVP. Example row: Marketplace / Product Discovery / AI Recommendation / Recommend Product / Customer / AI Agent / "Only recommend eligible products" / Customer requirements / Ranked products / POST /recommendations / RecommendationGenerated / Product Recommendation / MARKETPLACE_RECOMMEND / Global / Yes / Yes / P0 / Yes. It offers to build "~100 capabilities → several hundred features → functions". **The workbook is that artifact.** |
| 14 | Recommended AI-agent architecture | An AI Agent Platform with specialised domain agents, a shared MCP/tool layer, shared memory and knowledge, and governed orchestration. Diagram: users → **AI Experience Gateway** (Chat/Voice/API/UI) → **AI Agent Orchestrator** (Intent → Plan → Delegate → Verify) → Customer / Technical / Business agents → **MCP Gateway** (tool discovery, authN, authZ, rate limits, tenant isolation, audit) → PaaS APIs / MCP Servers / External APIs. |
| 15 | 18 agents | The table is in 1.2.3 below. Also an **AI Orchestrator/Supervisor**, a platform capability with an example flow for "HA PostgreSQL, 3 environments, 2 TB, backups, DR". |
| 16 | Agent details | Concierge, Product Discovery (9 tools), Solution Architect, Provisioning (a 12-step plan, and "never directly execute arbitrary infrastructure commands": Agent → Policy Engine → Provisioning API → Workflow Engine → Infrastructure), DevOps (10 tools; GitHub, GitLab, Bitbucket, Jenkins, Argo CD, Kubernetes, Terraform), Troubleshooting (correlates logs, metrics, traces, events, deployments, infrastructure, configuration, changes and incidents), Security (read-heavy; 10 capabilities; example score 82/100), FinOps (example savings $4,820/month), Billing, Subscription (9-step upgrade flow), Knowledge/Training (7 tools), Partner/Marketplace. |
| 17 | MCP integration | "Agents should not directly integrate with hundreds of internal APIs." **12 MCP domains**, MCP-01 to MCP-12, with their tools, listed in 1.2.4. |
| 18 | MCP vs REST | Use both. Interface table: REST, GraphQL, gRPC, Events, Webhooks, MCP, A2A, SDKs, CLI. |
| 19 | MCP design rules | Tools must be business-level (for example database.create, not database.executeSQL). There are 9 MCP resources (`paas://tenant/{tenantId}` and others) and 9 MCP prompts (architect_solution, troubleshoot_incident, optimize_cloud_cost, review_security_posture, design_disaster_recovery, prepare_production_deployment, explain_invoice, recommend_products, create_learning_plan). |
| 20 | Agent permissions | An **Agent Authorization Matrix** covering 11 agents with Read/Write/Destructive (see 1.2.5). Authorization chain: User → Tenant → User role → Agent identity → Agent role → Tool permission → Resource policy → Risk policy → Tool execution. |
| 21 | AI Governance Agent | Evaluates proposed actions against user permission, agent permission, environment, resource classification, backup status, change window, compliance policy and approval requirement, then returns ALLOW, DENY or REQUIRE HUMAN APPROVAL. |
| 22 | Human approval | Required for: delete production resource, change billing plan, increase spending limit, modify IAM administrator, change security policy, deploy production, transfer ownership, refund large payment, change data residency, move workloads between regions. |
| 23 | Protocols | MCP is Agent → Tools/Data, A2A is Agent → Agent, REST is Application → Platform, gRPC is Service → Service, Events is Platform → Platform. |
| 24 | AI Agent Platform (an expansion of App 04) | 20 capabilities: Agent Registry, Agent Lifecycle Management, Agent Runtime, Agent Orchestration, A2A Communication, MCP Gateway, Tool Registry, Tool Authorization, Prompt Management, Model Gateway, Memory, Knowledge/RAG, Agent Evaluation, Guardrails, Human Approval, Agent Observability, Agent Cost Management, Agent Versioning, Agent Testing, Agent Marketplace. |
| 25 | Agent Registry fields | Agent ID, Name, Version, Description, Owner, Tenant scope, Model, Tools, MCP servers, Permissions, Policies, Memory, Knowledge sources, Cost limits, Risk level, Status. |
| 26 | Model Gateway | "Don't hard-code one LLM provider". Routes between OpenAI, Anthropic and Gemini using task, quality, latency, cost and region. |
| 27 | Regional AI | A global AI control plane, with a runtime, MCP gateway, RAG and memory in each of the US, EU and APAC regions. The global layer holds agent definitions, versions, policies, tool metadata, model routing and global catalogs. The regional layer holds tenant data, operational data, logs, customer knowledge, regional resources and regional agent memory. |
| 28 | Using existing APIs as MCP tools | For example, `POST /v1/products/search` becomes `catalog.searchProducts`, `POST /v1/pricing/quote` becomes `pricing.createQuote`, and `POST /v1/provisioning` becomes `provisioning.create`. The API catalog should hold REST endpoint, OpenAPI spec, MCP tool mapping, authorization policy, agent permissions, risk classification, audit requirements and rate limit. |
| 29 | End-to-end example | 12 steps, ending with "Your environment is ready." |
| 30 | Workbook additions proposed | **10 new AI sheets:** AI Agents, Agent Capabilities, Agent Tools, MCP Servers, MCP Tools, Agent Policies, Agent-to-Agent, AI Memory, AI Evaluation, AI Observability, plus Agent Roadmap. The columns for each are in the document. **None of these sheets exist in [WB].** |
| 31 | AI implementation sequence | **Phase 1, Foundation:** Concierge, Product Discovery, Solution Architect, Knowledge, Support, Pricing, MCP Gateway, Tool Registry, Agent Registry, AI Model Gateway, Authorization/Policy Engine, Agent observability. **Phase 2, Operational AI:** Provisioning, DevOps, Troubleshooting, Security, FinOps, Billing, Subscription agents. **Phase 3, Autonomous PaaS:** AI Operations, Autonomous Optimization, Autonomous Security, Autonomous Incident Response, Agent Marketplace, Customer-created Agents, Partner-created Agents. **Autonomy levels 0 to 5:** answers, recommends, prepares, executes with approval, executes within policy, operates autonomously. Keep production changes at Levels 2 to 4 initially. |
| 32 | Engineering implementation phases | **Phase 0, Engineering Foundation (2 to 4 weeks):** AWS Organization, GitHub org, Azure DevOps, Project Online/PWA, Identity/SSO, Secrets, Integration Control Plane repo, Traceability DB, naming conventions, ID standards. *Deliverable: "all systems can authenticate and exchange a test record."* **Phase 1, "Hello PaaS":** one application, one capability, one feature (Catalog → Product Search → Search Products), proven along the path Macro Planner → Azure DevOps → Requirement → Design → Test → AI → GitHub → CI/CD → AWS → Monitoring ("golden path"). **Phase 2, AI Engineering Factory:** MCP Gateway, ALM MCP, GitHub MCP, AWS MCP, and Architecture, Coding, Test, Security and Traceability agents (for example "Implement FTR-CAT-001"). **Phase 3:** roll out to all 16 domains. **Phase 4, Autonomous Engineering.** |
| 33 | Systems of record | Macro Planner holds the portfolio, Azure DevOps holds Agile and ALM, GitHub holds source code, AWS is the runtime, the Traceability Service holds cross-system relationships, and AI does reasoning, generation and orchestration. "The AI can generate and recommend, but the authoritative state remains in the engineering systems." |
| 34 | Golden Path | Create Feature → Macro Planner → sync to Azure DevOps → Story → Requirement → Design → Test Cases → Requirement Approved → AI Context Builder → AI Coding Agent → GitHub branch → AI code → PR → Automated review → CI/CD (Build/Test/Security) → Quality Gate → AWS DEV → Integration → AWS QA → Approval → AWS PROD → CloudWatch → Traceability → Status update → Macro Planner. |
| 35 | Next deliverable proposed | A "PaaS Engineering Factory implementation workbook + starter repository structure" with 21 items: integration architecture, Macro Planner fields/templates, Azure DevOps config, ALM work-item templates, requirement/design/test templates, GitHub repo template, GitHub Actions CI/CD templates, AWS CDK templates, MCP server/tool specs, AI agent prompts, API specs, webhook/event definitions, traceability DB schema, sync jobs, Python automation scripts, environment config, IAM/OIDC policies, approval gates, deployment templates, sample `platform.yaml`, sample end-to-end Catalog Product Search. |

### 1.2.3 The 18 AI agents ([CG] Table 3)

| # | Agent | Primary responsibility |
|---|---|---|
| 1 | AI Concierge Agent | Main conversational entry point |
| 2 | Product Discovery Agent | Understand customer needs and find products |
| 3 | Solution Architect Agent | Design complete PaaS solutions |
| 4 | Pricing & Quotation Agent | Calculate price, discounts and quotes |
| 5 | Sales Agent | Recommend, configure and convert opportunities |
| 6 | Onboarding Agent | Create organizations, tenants and environments |
| 7 | Provisioning Agent | Deploy services/resources |
| 8 | DevOps Agent | CI/CD, deployments, environments and operations |
| 9 | Cloud Operations Agent | Monitor and operate infrastructure |
| 10 | Troubleshooting Agent | Diagnose incidents and failures |
| 11 | Security Agent | Security analysis, IAM, policies and threats |
| 12 | FinOps Agent | Usage, cost optimization and forecasting |
| 13 | Billing Agent | Invoices, payments and billing questions |
| 14 | Subscription Agent | Plans, upgrades, downgrades, renewals |
| 15 | Support Agent | Customer support and ticket management |
| 16 | Knowledge/Training Agent | Documentation, courses and learning |
| 17 | Partner/Marketplace Agent | Providers, products and marketplace |
| 18 | Platform Admin Agent | Governance, configuration and administration |

[CG] also names agents that are not in this table: **AI Orchestrator/Supervisor**, **AI Governance Agent**, **Order Agent** (in the orchestration flow), the Phase 2 engineering agents (**Architecture, Coding, Test, Traceability**), and the Phase 3 **AI Operations, Autonomous Optimization, Autonomous Security** and **Autonomous Incident Response** agents.

### 1.2.4 The 12 MCP domains and their tools ([CG] section 15)
| MCP | Tools |
|-----|-------|
| MCP-01 Identity & Tenant | identity.getUser, identity.getRoles, identity.checkPermission, tenant.get, tenant.list, tenant.getSettings |
| MCP-02 Catalog | catalog.search, catalog.getProduct, catalog.compare, catalog.getOffering, catalog.getPlans |
| MCP-03 Pricing | pricing.calculate, pricing.quote, pricing.comparePlans, pricing.getUsagePrice |
| MCP-04 Commerce | commerce.createOrder, commerce.getOrder, commerce.cancelOrder, commerce.checkout |
| MCP-05 Subscription | subscription.list, .create, .upgrade, .downgrade, .cancel |
| MCP-06 Provisioning | provisioning.plan, .create, .status, .cancel |
| MCP-07 Resources | resource.list, .get, .update, .delete, .metrics |
| MCP-08 DevOps | deployment.create, deployment.status, deployment.rollback, pipeline.run, pipeline.logs |
| MCP-09 Observability | logs.search, metrics.query, traces.search, events.search, alerts.list |
| MCP-10 Security | security.scan, security.findings, security.checkPolicy, security.getPosture |
| MCP-11 Support | support.createTicket, support.getTicket, support.searchKnowledge, support.escalate |
| MCP-12 Marketplace | marketplace.search, marketplace.publish, marketplace.update, marketplace.review |

### 1.2.5 Agent Authorization Matrix ([CG] Table 5)

| Agent | Read | Write | Destructive |
|---|---|---|---|
| Concierge | ✓ | No | No |
| Product Discovery | ✓ | No | No |
| Solution Architect | ✓ | No | No |
| Pricing | ✓ | Quote | No |
| Support | ✓ | Ticket | No |
| Security | ✓ | Findings | No |
| DevOps | ✓ | Deployment | Restricted |
| Provisioning | ✓ | Provision | Restricted |
| FinOps | ✓ | Recommendations | Restricted |
| Billing | ✓ | Billing operations | Restricted |
| Platform Admin | ✓ | ✓ | Highly restricted |

### 1.2.6 Summary of [CG]
| Item | Content |
|------|---------|
| Purpose | Architecture reasoning, from the prompt through applications, capabilities, features and functions, to the AI-agent platform, MCP, and the engineering factory |
| Applications | 16 (same as [PO] and [WB]) |
| Capabilities | **50** (the other two sources have 69; see 3.4) |
| AI | 18 agents plus the Orchestrator and Governance agents, 12 MCP domains, a 20-capability AI Agent Platform, autonomy levels 0 to 5 |
| Roadmap | **Phases only** (MVP Phase 1/2/3, AI Phase 1/2/3, Engineering Phase 0 to 4). **No PI or sprint IDs.** |
| Requirements | The original prompt. The rest is recommendation, not approved requirement |
| Deliverables | Phase 0 deliverable ("all systems can authenticate and exchange a test record"), the golden path, and a proposed 21-item engineering-factory deliverable |

---

## 1.3 [WB] `Full_SaaS_PaaS_Product_Architecture_Workbook.xlsx`
**What it is:** The structured Product Breakdown Structure that [CG] offered to build. It is the **detailed baseline**: 16 applications → 69 capabilities → 114 features → 444 functions, plus APIs, services, entities, events, roles, journeys, roadmap phases, layers, NFRs and traceability. The next section covers each sheet.

---

# 2. Excel workbook, sheet by sheet (all 18 sheets)

| # | Sheet | Rows (excl. header) × columns | Contents | How it relates to the other sources |
|---|-------|-------------------------------|----------|-------------------------------------|
| 1 | README | 18 rows (title + 17), 2 columns | Purpose, a contents list of 14 sheets, and the recommended design sequence | "Application Summary", "Core Domain Model" and "Architecture Principles" exist but are **not listed** in the README contents |
| 2 | Product Breakdown | 643 × 13 | The full 4-level hierarchy: 16 Application + 69 Capability + 114 Feature + 444 Function rows | Implements the PBS proposed in [CG] 1.2.2 #13 |
| 3 | Application Summary | 16 × 4 | Application ID, name, description, capabilities | **Identical** to [PO] Table 1 |
| 4 | Core Domain Model | 21 × 3 | 21 domain objects with purpose and relationships | Expands the [CG] "Most Important Domain Model" |
| 5 | Architecture Principles | 12 × 2 | 12 principles | Consolidates the [CG] principles |
| 6 | Capabilities | 69 × 11 | Capability catalog with actors, domain, priority, MVP and AI flags | Same 69 as [PO] Table 1 |
| 7 | Features | 114 × 10 | Feature catalog | No equivalent in [PO] for EIS |
| 8 | Functions | 444 × 13 | Functions with a suggested API path and primary actor | No equivalent in [PO] for EIS |
| 9 | APIs | 22 × 10 | API-001 to API-022 | Endpoints are consistent with the [CG] MCP-as-adapter examples |
| 10 | Microservices | 22 × 9 | MS-001 to MS-022 | [CG] principle: capability first, then service boundary |
| 11 | Data Entities | 25 × 5 | DE-001 to DE-025 | Adds UsageRecord, Course and AuditEvent to the domain model |
| 12 | Events | 20 × 8 | EVT-001 to EVT-020 | [CG] "Event" level of the hierarchy |
| 13 | User Roles | 15 × 6 | ROLE-001 to ROLE-015 | Adds concrete roles. ROLE-010 "AI Agent" links to the [CG] agent permissions |
| 14 | User Journeys | 12 × 8 | UJ-001 to UJ-012 | Breaks the 16-step [CG] customer journey into journeys |
| 15 | Roadmap | 17 × 6 | Phase 1/MVP (7 workstreams), Phase 2 (5), Phase 3 (5) | **Differs from** the [CG] MVP phases and from the [PO] sprints (see 3.5) |
| 16 | Architecture Layers | 10 × 4 | L1 to L10 | Formalises the [CG] layered diagrams |
| 17 | Non-Functional Requirements | 14 × 5 | NFR-001 to NFR-014 | The **only formal "requirements" with IDs** in any document |
| 18 | Traceability | 444 × 11 | One row per function, linking API, microservice, entity, event, role, journey and roadmap phase | Row for row it matches Functions and Product Breakdown (verified: 0 mismatches in IDs and names) |

## 2.1 README

| Full SaaS / PaaS Product Architecture Workbook |  |
|---|---|
| Purpose | Architecture and product-planning baseline for a multilingual, multi-regional, multi-tenant SaaS/PaaS marketplace platform. |
| Workbook Contents |  |
| Product Breakdown | Original 4-level hierarchy: Application → Capability → Feature → Function |
| Capabilities | Capability catalog |
| Features | Feature catalog |
| Functions | Function-level decomposition |
| APIs | Initial API catalog and endpoint proposals |
| Microservices | Suggested domain-aligned service boundaries |
| Data Entities | Core domain model |
| Events | Event-driven integration/event catalog |
| User Roles | Customer, partner, internal and AI roles |
| User Journeys | End-to-end customer/provider journeys |
| Roadmap | MVP / Phase 1, Phase 2 and Phase 3 scope |
| Architecture Layers | Logical platform architecture layers |
| Non-Functional Requirements | Scalability, security, reliability, regional and AI requirements |
| Traceability | Cross-reference from functions to APIs, services, entities, events, roles and roadmap |
| Recommended next design sequence | 1) Confirm domain model → 2) validate capability boundaries → 3) define APIs/events → 4) select service boundaries → 5) define data ownership → 6) prioritize roadmap → 7) create epics/user stories. |

## 2.2 Product Breakdown
- **Columns:** ID, Level, Application, Capability, Feature, Function, Description, Priority, MVP, AI Required, Multi-Region, Multi-Language, Notes.
- **Row counts:** 16 Application rows, 69 Capability rows, 114 Feature rows, 444 Function rows.
- **Flags:**
  - Application and Capability rows are all **P0 / MVP=Yes / AI=No**.
  - Feature rows are all **P1 / MVP=Yes / AI=No**.
  - Function rows have **no Priority**, and have MVP and AI flags that match [WB:Functions].
- **Other columns:**
  - The Description column is filled **only** on the 16 Application rows.
  - **Notes is empty on every row.**
  - Multi-Region and Multi-Language are **Yes on every row**.
- The full function content is in Appendix B.

## 2.3 Application Summary
Identical to [PO] Table 1, so it is not repeated here. See 1.1.2.

## 2.4 Core Domain Model

| Domain Object | Purpose | Key Relationships |
|---|---|---|
| Organization | Customer business account | Owns tenants, billing accounts, subscriptions |
| Tenant | Isolated customer environment | Belongs to organization; contains users/projects/resources |
| User | Human identity | Belongs to tenant; assigned groups/roles |
| Role | Collection of permissions | Assigned to users/groups |
| Permission | Atomic authorization action | Used by roles/policies |
| Product | Sellable platform item | Has offerings, plans, content |
| Offering | Marketable product/solution/service package | References products and dependencies |
| Plan | Commercial package | Belongs to offering; defines pricing and entitlements |
| Price | Commercial rate | Associated with plan/usage/region/currency |
| Subscription | Customer purchase commitment | References offering/plan and entitlements |
| Entitlement | What customer is allowed to use | Granted by subscription; checked by services |
| Order | Purchase transaction | Creates subscriptions/provisioning workflows |
| Service Instance | Activated customer service | Created by provisioning; contains resources |
| Resource | Consumable technical asset | Belongs to service instance/tenant |
| Invoice | Billable financial document | Generated from usage/subscription charges |
| Payment | Financial transaction | Settles invoices |
| Ticket | Support request | Associated with tenant/service/user |
| Knowledge Article | Support/training content | Used by portal and AI agents |
| Partner/Provider | External publisher or service provider | Owns products; participates in revenue sharing |
| Region | Geographic service boundary | Controls availability and data residency |
| Locale | Language/regional formatting | Controls translated content and presentation |

## 2.5 Architecture Principles

| Principle | Guidance |
|---|---|
| Domain-first decomposition | Define business domains and capabilities before choosing microservice boundaries. |
| Control plane / service plane | Separate global catalog, identity and governance from regional service execution and customer data. |
| Multi-tenancy by design | Tenant context should propagate through APIs, events, storage and authorization. |
| API-first | Every major capability should expose versioned APIs and events. |
| Event-driven | Use events for asynchronous lifecycle changes, integration and analytics. |
| AI as a platform | Provide shared model gateway, agent runtime, tools, guardrails, memory and observability. |
| Localization by platform | Language, currency, time zone, tax and regional terminology should be reusable platform services. |
| Security by default | Use centralized identity, policy evaluation, secrets management and immutable audit trails. |
| Idempotent operations | Orders, payments, provisioning and event handlers should be safely retryable. |
| Observability | Standardize logs, metrics, traces, health, alerts and business telemetry. |
| Extensibility | Products, providers, connectors and workflows should be pluggable. |
| Data governance | Classify data, enforce residency/retention, and maintain lineage and quality. |

## 2.6 Capabilities (69)
- **Columns:** Capability ID, Application, Capability, Business Purpose, Primary Actors, Core Domain, Priority, MVP, AI Relevant, Multi-Region, Multi-Language.
- **Placeholder values:** Every row has **Business Purpose = "Business capability supporting \<name\>"**, **Primary Actors = "Customer; Platform Admin; Partner; Support"**, **Core Domain = "Platform"**, **Priority = P0**, **MVP = Yes**, and Multi-Region and Multi-Language = Yes. These are generated placeholders, not analysed values.
- **AI Relevant = Yes** on 7 rows only: 04.01 to 04.05, 12.01 and 12.02.
- The full list is in section 5.

## 2.7 Features (114)
- **Columns:** Feature ID, Application, Capability, Feature, Feature Description, Priority, MVP, AI Required, Multi-Region, Multi-Language.
- **Placeholder values:** Every description is "Feature within \<capability\>". Every row is P1 with MVP = Yes.
- **AI Required = Yes** on 8 rows: 02.02.02 Availability, 03.01.02 Recommendations, 04.01.02 Recommendation, 04.04.01 Support Automation, 04.05.01 Agent Runtime, 11.01.02 AI Knowledge, 12.02.01 Conversational Support, 14.04.01 Partner Support.
- Odd AI flags:
  - "AI Sales Agent / Sales Assistance" and "AI Technical Advisor / Technical Guidance" are **AI Required = No** in [WB:Features], but **Yes** in [WB:Functions].
  - "Offering Management / Availability" is **Yes** in [WB:Features], but its functions are **No**.
- The full list is in section 5.

## 2.8 Functions (444)
- **Columns:** Function ID, Application, Capability, Feature, Function, Function Type (always "Business Function"), Priority (always **P1**), MVP, AI Required, Multi-Region, Multi-Language, Suggested API (`/<capability-slug>/<function-slug>`), Primary Actor.
- **Primary Actor:** Customer on 53 rows, AI Agent on 52, Platform Service on 339.
- **MVP = Yes on only 15 functions:**
  - 02.01.01.01 to .03 (Create, Update and Version product)
  - 02.01.02.01 to .03 (Define product hierarchy, variants and dependencies)
  - 07.01.01.01 to .03 (Create, Activate and Suspend subscription)
  - 07.01.02.01 to .03 (Change quantity, Change plan, Schedule change)
  - 09.01.01.01 to .03 (Create, Validate and Price order)
- **AI Required = Yes on 52 functions:** all of App 04 (31), 11.03 Training Delivery (6), 12.01 Ticket Management (7), and 12.02 AI Support (8).

| App | Functions | MVP=Yes | AI=Yes |
|-----|-----------|---------|--------|

| 01 Experience & Customer Portal | 26 | 0 | 0 |
| 02 Product & Catalog Management | 35 | 6 | 0 |
| 03 Marketplace | 27 | 0 | 0 |
| 04 AI Advisor & Agent Platform | 31 | 0 | 31 |
| 05 Customer / Tenant Management | 24 | 0 | 0 |
| 06 Identity & Access Management | 22 | 0 | 0 |
| 07 Subscription & Entitlement Management | 30 | 6 | 0 |
| 08 Billing & Payments | 33 | 0 | 0 |
| 09 Order & Provisioning Management | 26 | 3 | 0 |
| 10 Service & Resource Management | 23 | 0 | 0 |
| 11 Training & Knowledge Management | 26 | 0 | 6 |
| 12 Support & Service Management | 28 | 0 | 15 |
| 13 Integration & API Platform | 25 | 0 | 0 |
| 14 Partner & Provider Management | 23 | 0 | 0 |
| 15 Administration & Governance | 33 | 0 | 0 |
| 16 Analytics & Data Platform | 32 | 0 | 0 |
| **Total** | **444** | **15** | **52** |

## 2.9 APIs (22)

| API ID | API Name | Domain/Application | Capability | Endpoint | Purpose | Primary Consumer | Security | Interaction | Priority |
|---|---|---|---|---|---|---|---|---|---|
| API-001 | Identity API | Identity & Access Management | Authentication | POST /v1/auth/login | Authenticate user | Customer/Admin | OIDC/OAuth2 | Sync | P0 |
| API-002 | Authorization API | Identity & Access Management | Authorization | POST /v1/authz/evaluate | Evaluate permission | Platform Service | OAuth2 | Sync | P0 |
| API-003 | Tenant API | Customer / Tenant Management | Tenant Management | POST /v1/tenants | Create/configure tenant | Admin | OAuth2 | Sync | P0 |
| API-004 | Catalog API | Product & Catalog Management | Product Management | GET /v1/products | Search/retrieve catalog | Customer/Provider | OAuth2 | Sync | P0 |
| API-005 | Offering API | Product & Catalog Management | Offering Management | GET /v1/offerings/{id} | Retrieve offering | Customer | OAuth2 | Sync | P0 |
| API-006 | Pricing API | Billing & Payments | Pricing | POST /v1/pricing/quote | Calculate price | Customer/AI Agent | OAuth2 | Sync | P0 |
| API-007 | Recommendation API | AI Advisor & Agent Platform | AI Product Advisor | POST /v1/recommendations | Generate product recommendations | AI Agent | OAuth2 | Async/Sync | P0 |
| API-008 | Checkout API | Marketplace | Marketplace Checkout | POST /v1/checkout | Create marketplace purchase | Customer | OAuth2 | Sync | P0 |
| API-009 | Order API | Order & Provisioning Management | Order Management | POST /v1/orders | Create/manage order | Customer/System | OAuth2 | Async | P0 |
| API-010 | Provisioning API | Order & Provisioning Management | Provisioning | POST /v1/provisioning | Provision service | Platform Service | mTLS/OAuth2 | Async | P0 |
| API-011 | Subscription API | Subscription & Entitlement Management | Subscription Management | POST /v1/subscriptions | Create/change subscription | Customer/System | OAuth2 | Async | P0 |
| API-012 | Entitlement API | Subscription & Entitlement Management | Entitlement Management | POST /v1/entitlements/check | Check access entitlement | Platform Service | mTLS/OAuth2 | Sync | P0 |
| API-013 | Billing API | Billing & Payments | Billing | POST /v1/billing/invoices | Generate/retrieve billing | Billing Service | mTLS/OAuth2 | Async | P0 |
| API-014 | Payment API | Billing & Payments | Payment | POST /v1/payments | Process payment | Customer/System | OAuth2 | Sync | P0 |
| API-015 | Service API | Service & Resource Management | Service Management | GET /v1/services | Manage service instances | Customer | OAuth2 | Sync | P1 |
| API-016 | Support API | Support & Service Management | Support | POST /v1/support/tickets | Create support request | Customer/AI Agent | OAuth2 | Async | P0 |
| API-017 | Knowledge API | Training & Knowledge Management | Knowledge Base | POST /v1/knowledge/search | Search knowledge | Customer/AI Agent | OAuth2 | Sync | P0 |
| API-018 | Integration API | Integration & API Platform | Integration Hub | POST /v1/integrations | Manage connectors | Admin/Partner | OAuth2 | Async | P1 |
| API-019 | Event API | Integration & API Platform | Event Platform | POST /v1/events | Publish platform event | Platform Service | mTLS | Async | P0 |
| API-020 | Partner API | Partner & Provider Management | Provider Onboarding | POST /v1/providers | Onboard provider | Provider/Admin | OAuth2 | Async | P1 |
| API-021 | Analytics API | Analytics & Data Platform | Business Analytics | GET /v1/analytics | Retrieve analytics | Admin/Provider | OAuth2 | Async/Sync | P1 |
| API-022 | Audit API | Administration & Governance | Audit | GET /v1/audit/events | Query audit trail | Admin/Auditor | OAuth2 | Sync | P0 |

## 2.10 Microservices (22)

| Service ID | Microservice | Application/Domain | Responsibilities | Primary Data Store | Interface | Priority | Deployment Boundary | Stateful |
|---|---|---|---|---|---|---|---|---|
| MS-001 | Identity Service | Identity & Access Management | Users, identities, sessions | Identity DB | REST/events | P0 | Domain-aligned service | Yes |
| MS-002 | Authorization Service | Identity & Access Management | Roles, permissions, policies | Policy Store | REST | P0 | Domain-aligned service | Yes |
| MS-003 | Tenant Service | Customer / Tenant Management | Organizations, tenants, projects | Tenant DB | REST/events | P0 | Domain-aligned service | Yes |
| MS-004 | Catalog Service | Product & Catalog Management | Products, offerings, plans | Catalog DB | REST/events | P0 | Domain-aligned service | Yes |
| MS-005 | Marketplace Service | Marketplace | Discovery, checkout | Marketplace DB | REST/events | P0 | Domain-aligned service | Yes |
| MS-006 | AI Agent Gateway | AI Advisor & Agent Platform | Agent routing, tools, guardrails | AI State Store | REST/events | P0 | Domain-aligned service | No |
| MS-007 | Recommendation Service | AI Advisor & Agent Platform | Product recommendation | Vector/Feature Store | REST | P0 | Domain-aligned service | Yes |
| MS-008 | Subscription Service | Subscription & Entitlement Management | Subscription lifecycle | Subscription DB | REST/events | P0 | Domain-aligned service | Yes |
| MS-009 | Entitlement Service | Subscription & Entitlement Management | Access/feature entitlements | Entitlement DB/Cache | REST/events | P0 | Domain-aligned service | Yes |
| MS-010 | Pricing Service | Billing & Payments | Price calculation | Pricing DB | REST | P0 | Domain-aligned service | Yes |
| MS-011 | Billing Service | Billing & Payments | Invoices, usage billing | Billing DB | REST/events | P0 | Domain-aligned service | Yes |
| MS-012 | Payment Service | Billing & Payments | Payment transactions | Payment DB | REST/events | P0 | Domain-aligned service | Yes |
| MS-013 | Order Service | Order & Provisioning Management | Order lifecycle | Order DB | REST/events | P0 | Domain-aligned service | Yes |
| MS-014 | Provisioning Orchestrator | Order & Provisioning Management | Provisioning workflows | Workflow DB | Events/workflow | P0 | Domain-aligned service | No |
| MS-015 | Resource Service | Service & Resource Management | Service/resource lifecycle | Resource DB | REST/events | P1 | Domain-aligned service | Yes |
| MS-016 | Knowledge Service | Training & Knowledge Management | Content and semantic retrieval | Content + Vector DB | REST | P0 | Domain-aligned service | Yes |
| MS-017 | Support Service | Support & Service Management | Tickets, SLA, incidents | Support DB | REST/events | P0 | Domain-aligned service | Yes |
| MS-018 | Integration Service | Integration & API Platform | Connectors and transformations | Integration DB | REST/events | P1 | Domain-aligned service | Yes |
| MS-019 | Event Gateway | Integration & API Platform | Event routing | Event Bus | Events | P0 | Domain-aligned service | No |
| MS-020 | Partner Service | Partner & Provider Management | Providers, publishers, commissions | Partner DB | REST/events | P1 | Domain-aligned service | Yes |
| MS-021 | Audit Service | Administration & Governance | Immutable audit records | Audit Store | Events | P0 | Domain-aligned service | Yes |
| MS-022 | Analytics Service | Analytics & Data Platform | KPIs and reporting | Data Warehouse/Lakehouse | REST/SQL | P1 | Domain-aligned service | Yes |

## 2.11 Data Entities (25)

| Entity ID | Entity | Domain | Definition | Key Relationships |
|---|---|---|---|---|
| DE-001 | Organization | Customer Management | Business customer account | Tenant; Billing Account; Subscription |
| DE-002 | Tenant | Customer Management | Isolated customer environment | Organization; User; Project; Resource |
| DE-003 | User | Identity | Human/service identity | Tenant; Role; Group |
| DE-004 | Role | Identity | Named access role | User/Group; Permission |
| DE-005 | Permission | Identity | Atomic authorization right | Role; Policy |
| DE-006 | Product | Catalog | Sellable platform item | Offering; Plan; Content |
| DE-007 | Offering | Catalog | Marketable package | Product; Plan; Region |
| DE-008 | Plan | Catalog | Commercial package | Offering; Price; Entitlement |
| DE-009 | Price | Commercial | Price/rate definition | Plan; Currency; Region |
| DE-010 | Subscription | Commerce | Customer purchase commitment | Organization; Plan; Entitlement |
| DE-011 | Entitlement | Commerce | Customer access right | Subscription; Product/Feature |
| DE-012 | Order | Commerce | Purchase transaction | Customer; Offering; Payment; Subscription |
| DE-013 | ServiceInstance | Service | Activated customer service | Tenant; Subscription; Resource |
| DE-014 | Resource | Service | Technical asset | ServiceInstance; Region |
| DE-015 | Invoice | Finance | Financial bill | Billing Account; Charges; Payment |
| DE-016 | Payment | Finance | Payment transaction | Invoice; Customer |
| DE-017 | UsageRecord | Finance/Operations | Metered consumption | Resource; Subscription; Billing |
| DE-018 | SupportTicket | Support | Customer support request | User; Tenant; ServiceInstance; SLA |
| DE-019 | KnowledgeArticle | Knowledge | Documentation/support content | Product; Locale |
| DE-020 | Course | Learning | Training offering | Product; User; Certificate |
| DE-021 | Partner | Marketplace | Provider/publisher | Product; Contract; Payout |
| DE-022 | Region | Platform | Geographic operating region | Tenant; Product; Resource |
| DE-023 | Locale | Platform | Language/regional format | Content; User |
| DE-024 | AuditEvent | Governance | Immutable activity record | User; Tenant; Resource |
| DE-025 | Policy | Governance | Security/compliance policy | Role; Tenant; Resource |

## 2.12 Events (20)

| Event ID | Event Name | Domain | Aggregate | Payload Entity | Meaning | Producer | Typical Consumers |
|---|---|---|---|---|---|---|---|
| EVT-001 | UserCreated | Identity | User | User | User created | Identity Service | Tenant/Analytics |
| EVT-002 | TenantCreated | Customer | Tenant | Tenant | Tenant provisioned | Tenant Service | Provisioning/Audit |
| EVT-003 | ProductPublished | Catalog | Product | Product | Product published | Catalog Service | Marketplace/Search |
| EVT-004 | OfferingUpdated | Catalog | Offering | Offering | Offering changed | Catalog Service | Marketplace/AI |
| EVT-005 | CheckoutCompleted | Commerce | Order | Order | Checkout completed | Marketplace Service | Order/Billing |
| EVT-006 | OrderSubmitted | Commerce | Order | Order | Order submitted | Order Service | Provisioning/Billing |
| EVT-007 | PaymentAuthorized | Finance | Payment | Payment | Payment authorized | Payment Service | Order/Billing |
| EVT-008 | PaymentFailed | Finance | Payment | Payment | Payment failed | Payment Service | Notification/Support |
| EVT-009 | SubscriptionCreated | Commerce | Subscription | Subscription | Subscription activated | Subscription Service | Entitlement/Provisioning |
| EVT-010 | SubscriptionChanged | Commerce | Subscription | Subscription | Plan or quantity changed | Subscription Service | Billing/Entitlement |
| EVT-011 | EntitlementGranted | Commerce | Entitlement | Entitlement | Access granted | Entitlement Service | Resource/Portal |
| EVT-012 | ProvisioningStarted | Operations | ServiceInstance | ServiceInstance | Provisioning initiated | Provisioning Orchestrator | Resource Service |
| EVT-013 | ProvisioningCompleted | Operations | ServiceInstance | ServiceInstance | Service ready | Provisioning Orchestrator | Notification/Portal |
| EVT-014 | ProvisioningFailed | Operations | ServiceInstance | ServiceInstance | Provisioning failed | Provisioning Orchestrator | Support/Notification |
| EVT-015 | UsageRecorded | Operations | UsageRecord | UsageRecord | Consumption recorded | Resource Service | Billing/Analytics |
| EVT-016 | InvoiceGenerated | Finance | Invoice | Invoice | Invoice generated | Billing Service | Customer/Payment |
| EVT-017 | TicketCreated | Support | SupportTicket | SupportTicket | Support request opened | Support Service | AI/Human Support |
| EVT-018 | TicketResolved | Support | SupportTicket | SupportTicket | Support request resolved | Support Service | Customer/Analytics |
| EVT-019 | AIRecommendationGenerated | AI | Recommendation | Recommendation | AI recommendation produced | Recommendation Service | Portal/Marketplace |
| EVT-020 | AuditRecorded | Governance | AuditEvent | AuditEvent | Auditable action recorded | Audit Service | Compliance |

## 2.13 User Roles (15)

| Role ID | Role | Role Type | Purpose | Primary Access Scope | Risk Level |
|---|---|---|---|---|---|
| ROLE-001 | Customer Owner | Customer | Full organization administration | Organization, billing, users, subscriptions | High |
| ROLE-002 | Customer Admin | Customer | Operational administration | Users, roles, services, support | High |
| ROLE-003 | Billing Admin | Customer | Financial administration | Payment methods, invoices, billing | High |
| ROLE-004 | Developer | Customer | Technical service management | APIs, resources, deployments | Medium |
| ROLE-005 | Operator | Customer | Day-to-day service operations | Service instances, monitoring | Medium |
| ROLE-006 | End User | Customer | Consume assigned products/services | Assigned products, training, support | Low |
| ROLE-007 | Provider Admin | Partner | Manage provider account | Products, offerings, pricing | High |
| ROLE-008 | Publisher | Partner | Publish marketplace products | Catalog, content, offers | High |
| ROLE-009 | Support Agent | Internal | Resolve customer support | Tickets, knowledge, service context | High |
| ROLE-010 | AI Agent | System | Perform authorized AI actions | Search, recommendation, support tools | Controlled |
| ROLE-011 | Platform Admin | Internal | Global platform administration | Tenants, catalog, policies, regions | Critical |
| ROLE-012 | Security Admin | Internal | Security and access governance | IAM, policies, audit | Critical |
| ROLE-013 | Finance Admin | Internal | Financial operations | Billing, payments, revenue | Critical |
| ROLE-014 | Compliance Auditor | Internal | Audit and compliance review | Audit, policies, evidence | High |
| ROLE-015 | Partner Manager | Internal | Manage partner ecosystem | Providers, contracts, payouts | High |

## 2.14 User Journeys (12)

| Journey ID | Journey | Persona | Journey Path | Business Goal | Journey Steps | Key Capabilities | Priority |
|---|---|---|---|---|---|---|---|
| UJ-001 | Discover and Learn | Prospective Customer | Portal → Catalog → Knowledge/Training | Find and understand a solution | Search → View offering → Read docs → Watch training → Compare | Product, Content, Search | P0 |
| UJ-002 | AI Guided Selection | Prospective Customer | Portal → AI Advisor → Catalog | Identify best-fit products | Answer questions → AI analyzes requirements → Recommendations → Explain → Compare | AI, Catalog, Pricing | P0 |
| UJ-003 | Self-Service Purchase | Customer | Marketplace → Checkout → Payment | Subscribe without human intervention | Select → Configure → Quote → Accept → Pay → Order | Marketplace, Pricing, Payment, Order | P0 |
| UJ-004 | Tenant Onboarding | Customer Owner | Signup → Organization → Tenant → Users | Establish business account | Register → Verify → Create tenant → Invite users → Assign roles | Identity, Tenant, IAM | P0 |
| UJ-005 | Provision Service | Customer/System | Order → Provisioning → Service | Make purchased service usable | Order accepted → Entitlement → Provision → Activate → Notify | Order, Entitlement, Provisioning | P0 |
| UJ-006 | Manage Subscription | Customer Admin | Portal → Subscription | Change commercial relationship | View → Upgrade/Downgrade → Reprice → Confirm → Apply | Subscription, Pricing, Billing | P0 |
| UJ-007 | Manage Billing | Billing Admin | Portal → Billing | Manage financial account | Add payment → View invoice → Pay → Download receipt | Billing, Payment | P0 |
| UJ-008 | AI Technical Support | Customer | Portal → AI Support → Knowledge/Service | Resolve technical issue quickly | Describe issue → Diagnose → Recommend → Execute safe action → Escalate | AI, Knowledge, Support, Service | P0 |
| UJ-009 | Human Support Escalation | Customer/Support | AI → Ticket → Agent | Transfer unresolved issue with context | AI summary → Ticket → Assignment → Investigation → Resolution | Support, AI, Knowledge | P0 |
| UJ-010 | Provider Product Publishing | Provider | Partner Portal → Catalog → Marketplace | List and commercialize a product | Onboard → Create product → Price → Content → Submit → Approve → Publish | Partner, Catalog, Marketplace | P1 |
| UJ-011 | Regional Expansion | Platform Admin | Admin → Region → Catalog/Service | Launch offering in new region | Configure region → Data policies → Product availability → Provisioning → Monitor | Regional Ops, Governance | P1 |
| UJ-012 | Renewal and Expansion | Customer | Portal → Subscription → Pricing | Retain and grow account | Usage insight → AI recommendation → Upgrade → Renewal → Payment | Analytics, AI, Subscription, Billing | P1 |

## 2.15 Roadmap (17 rows)

| Phase | Workstream | Applications/Capabilities | Scope | Priority | MVP |
|---|---|---|---|---|---|
| Phase 1 / MVP | Foundation | Portal, Tenant, IAM | Customer signup, organization/tenant, users, RBAC, dashboard | P0 | Yes |
| Phase 1 / MVP | Catalog & Marketplace | Product/Catalog, Marketplace | Products, offerings, plans, search, product detail, checkout | P0 | Yes |
| Phase 1 / MVP | Commerce | Pricing, Subscription, Billing, Payment | Quotes, subscription, invoices, payment method, payment processing | P0 | Yes |
| Phase 1 / MVP | Provisioning | Order, Entitlement, Provisioning | Order lifecycle, entitlement, automated provisioning | P0 | Yes |
| Phase 1 / MVP | AI | AI Advisor, Knowledge | Guided product selection, semantic knowledge search | P0 | Yes |
| Phase 1 / MVP | Support | Support | AI support, ticket creation, human escalation | P0 | Yes |
| Phase 1 / MVP | Platform Foundation | API, Events, Audit, Observability | API gateway, event bus, audit, logs/metrics/traces | P0 | Yes |
| Phase 2 | Service Platform | Service/Resource Management | Service instances, resource lifecycle, monitoring, quotas | P1 | No |
| Phase 2 | Learning | Training/Knowledge | Courses, labs, assessments, certifications | P1 | No |
| Phase 2 | Integrations | Integration/API | Connectors, webhooks, external identity/CRM/ERP integrations | P1 | No |
| Phase 2 | AI Expansion | AI Agents | Sales agent, technical advisor, agent orchestration/tool execution | P1 | No |
| Phase 2 | Partner Ecosystem | Partner/Provider | Provider onboarding, publishing workflow, partner portal | P1 | No |
| Phase 3 | Global Scale | Regional Operations | Multi-region control/service planes, residency, regional failover | P1 | No |
| Phase 3 | Marketplace Ecosystem | Partner/Provider | Revenue sharing, payouts, advanced marketplace operations | P1 | No |
| Phase 3 | Advanced Governance | Governance/Compliance | Policy engine, compliance evidence, advanced audit | P1 | No |
| Phase 3 | Analytics | Analytics/Data Platform | Lakehouse/warehouse, product/customer analytics, executive BI | P1 | No |
| Phase 3 | AI Autonomous Operations | AI Platform | Agentic remediation, proactive support, optimization | P2 | No |

## 2.16 Architecture Layers (10)

| Layer | Name | Components | Purpose |
|---|---|---|---|
| L1 | Experience Layer | Web portal, admin portal, partner portal, mobile | Customers, partners, admins |
| L2 | AI Interaction Layer | AI advisor, agents, conversational UI, agent tools | AI-guided journeys |
| L3 | API & Integration Layer | API gateway, BFF, webhooks, connectors | External/internal APIs |
| L4 | Business Domain Layer | Catalog, marketplace, customer, IAM, commerce, support | Core business logic |
| L5 | Orchestration Layer | Workflow, provisioning, approvals, rules | Long-running processes |
| L6 | Platform Services Layer | Search, notification, localization, configuration, feature flags | Shared platform primitives |
| L7 | Data & Event Layer | Operational DBs, cache, event bus, object store, lakehouse | State and integration |
| L8 | Infrastructure Layer | Compute, containers, Kubernetes, storage, network, secrets | Runtime platform |
| L9 | Security & Governance | IAM, policy, audit, encryption, compliance, residency | Trust boundary |
| L10 | Observability | Logs, metrics, traces, SLOs, alerts, business telemetry | Reliability |

## 2.17 Non-Functional Requirements (14)

| NFR ID | Category | Scope | Requirement | Criticality |
|---|---|---|---|---|
| NFR-001 | Availability | Platform | 99.9% MVP; evolve to 99.99% for critical services | Critical |
| NFR-002 | Scalability | Platform | Horizontal scaling; stateless APIs; asynchronous processing | Critical |
| NFR-003 | Multi-Tenancy | Security | Strong tenant isolation at API, authorization, data and event layers | Critical |
| NFR-004 | Data Residency | Regional | Customer data must remain in configured jurisdiction where required | Critical |
| NFR-005 | Performance | Experience | Target p95 API latency < 500 ms for common synchronous operations | High |
| NFR-006 | Security | Security | OIDC/OAuth2, MFA, encryption, secrets management, least privilege | Critical |
| NFR-007 | Auditability | Governance | Immutable audit trail for security, admin and financial actions | Critical |
| NFR-008 | Resilience | Operations | Retries, idempotency, circuit breakers, rollback/compensation | Critical |
| NFR-009 | Observability | Operations | Centralized logs, metrics, traces, alerting and SLO monitoring | High |
| NFR-010 | Localization | Experience | Externalized translations, locale, timezone and currency handling | High |
| NFR-011 | AI Safety | AI | Tool permissions, guardrails, source grounding, human escalation | Critical |
| NFR-012 | API Compatibility | Integration | Versioned APIs with backward compatibility policy | High |
| NFR-013 | Disaster Recovery | Platform | Documented RPO/RTO by service criticality | Critical |
| NFR-014 | Compliance | Governance | Support applicable privacy, financial and security controls by region | Critical |

## 2.18 Traceability (444)
- **Columns:** Application, Capability, Feature, Example Function, Primary API, Microservice, Key Entity, Key Event, Primary Role, Journey, Roadmap Phase. The row-level mappings are in Appendix B.
- **Coverage of the other sheets:**
  - **Primary API** is filled on 159 of 444 rows ("-" on the others) and uses API-001 to API-022.
  - **Microservice** uses 15 of the 22 services. Portal functions have none. Never used: MS-002 Authorization, MS-007 Recommendation, MS-009 Entitlement, MS-011 Billing, MS-012 Payment, MS-014 Provisioning Orchestrator, MS-019 Event Gateway.
  - **Key Entity** uses 9 of the 25 entities.
  - **Key Event** uses 8 of the 20 events.
- **Placeholder values:**
  - **Journey:** UJ-005 "Provision Service" on **383** rows (including portal login), UJ-002 on 52, UJ-003 on 9. Only 3 of the 12 journeys are ever used. This looks like a **default value**, not a real mapping.
  - **Roadmap Phase:** "Phase 1 / MVP" on the **15** MVP functions, and "Phase 2" on all others. **"Phase 3" never appears**, although [WB:Roadmap] has five Phase 3 workstreams.
- **Mapping errors that need correcting before use:**
  - All Billing & Payments functions (Billing, Payment, Financial Documents, Tax) map to **Pricing Service**, not MS-011 Billing or MS-012 Payment.
  - Entitlement and License functions map to **Subscription Service**, not MS-009 Entitlement.
  - Provisioning and Workflow functions map to **Order Service**, not MS-014 Provisioning Orchestrator.
  - Authorization functions map to **Identity Service**, not MS-002 Authorization.
  - Marketplace functions have Key Entity **Subscription** and Key Event **CheckoutCompleted** for *every* function, including "Browse categories" and "Submit review".
  - Write functions point at read endpoints. For example, "Create product" maps to `API-004 GET /v1/products`, and "Create service instance" maps to `API-015 GET /v1/services`.

---

# 3. Cross-document analysis

## 3.1 How the three documents relate
```
 [CG] ChatGPT conversation ── concept, reasoning, architecture, AI/MCP design, engineering golden path
        │  "build a complete 4-level decomposition … in a structured Excel-style PBS"
        ▼
 [WB] Architecture Workbook ── 16 apps → 69 capabilities → 114 features → 444 functions,
        │                      plus APIs, services, entities, events, roles, journeys, NFRs, Phase 1/2/3
        │  Application Summary copied verbatim
        ▼
 [PO] ProdOps Plan ─────────── the same 16 apps / 69 capabilities, assigned PI + sprint,
                               plus the hosted SaaS products (Thittam, Thiran, …) and their sprints
```
**Inference.** The order is based on content, because the documents have no dates. [CG] says "Given the architecture workbook we already created", which means the workbook was created during the [CG] conversation.

## 3.2 Interlinked information
| Topic | [PO] | [CG] | [WB] |
|-------|------|------|------|
| Platform vision (multilingual, multi-regional, marketplace, PaaS/SaaS, AI-guided, self-service, roles and privileges, payments, AI support) | Vision section | Original prompt | README "Purpose" |
| 16 applications | Table 1, Table 2 | Table 1 and Applications 1 to 16 | Application Summary, Product Breakdown |
| Capabilities | 69 (Table 1) | 50 | 69 (Capabilities) |
| Features and functions | None for EIS | Per capability (Appendix A) | 114 features, 444 functions |
| Hierarchy App → Cap → Feature → Function | Implicit | Explicit | Product Breakdown IDs |
| Domain model | None | "Most Important Domain Model" | Core Domain Model, Data Entities |
| Multi-region control plane / service plane | None | Multi-Regional Architecture | Architecture Principles, NFR-004, Roadmap Phase 3 "Global Scale" |
| Localization | "multi-lingual" | Multi-Language Architecture | Capability 02.05, Principle "Localization by platform", NFR-010 |
| AI agents | "guided AI agents", "AI agents" support | 18+ agents, MCP, governance | App 04, MS-006 and MS-007, ROLE-010, NFR-011, UJ-002 and UJ-008 |
| MVP / phases | Sprints only | MVP Phase 1/2/3 | Roadmap Phase 1/2/3, MVP flags |
| PI / sprint | **Yes, the only source** | None | None |
| Engineering tooling and traceability | SW Life Cycle, Agile Planner, Macro Planner | Macro Planner, Azure DevOps, GitHub, AWS, Traceability Service | Traceability sheet |
| Hosted SaaS products | Thittam, Thiran, Tharav, Valam, Varthan, Yukth | None | None |

## 3.3 Duplicate information
1. [PO] Table 1 and [WB:Application Summary] are **identical**.
2. [WB:Product Breakdown], [WB:Capabilities], [WB:Features], [WB:Functions] and [WB:Traceability] repeat the same hierarchy. The row-by-row check found **0 mismatches** in IDs or names.
3. There are duplicate function names inside [WB]:
   - "Estimate cost" appears in 03.02.02.03 and 04.01.02.06.
   - "Create ticket" appears in 04.04.01.05, 12.01.01.01 and 12.02.02.01.
   - "Diagnose issue" appears in 04.04.01.03 and 12.02.01.03.
   - "Recommend resolution" appears in 04.04.01.04 and 12.02.01.04.
   - "Configure currency" appears in 02.05.02.01 and 08.05.02.01.
   - "Generate invoice" appears in 08.02.02.01 and 08.04.01.01.
   - "Create policy", "Assign policy" and "Evaluate policy" appear in both 06.02.02 and 15.02.01.
   - "Assign role" appears in 05.03.02.01 and 06.02.01.03.
   - "Configure service" appears in 09.02.01.02 and 10.01.01.02.
   - "Escalate" appears in 09.03.01.06 and 09.04.01.05.
   - "Analyze churn" / "Calculate churn" appear in 16.01.02.03, 16.02.02.03 and 16.04.02.02.
   - "Analyze usage" appears in 16.01.01.01 and 16.02.02.01.
   - "Publish product" appears in 02.01.01.04 and 14.02.01.04.
   - "Transform data" appears in 13.02.02.02 and 16.05.02.01.
4. AI support is split across **App 04 "AI Support Agent"** and **App 12 "AI Support"** with overlapping functions. [CG] has the same overlap.
5. Policy is defined in **06.02 Authorization / Policy** and again in **15.02 Policy Management**.
6. Regional settings are spread across **02.05 Localization / Regionalization**, **08.05 Tax & Currency**, **15.01 Platform Configuration** and **15.05 Regional Operations**.
7. Organization, Identity, Roles and SSO are platform capabilities (Apps 05 and 06), but they are **repeated inside the SaaS products**: Macro Planner #1 "Organization & Identity", Agile Planner AP-C01, SWLC-CAP-01 and CAP-02. Macro Planner #15 "Platform Operations (Security, audit, monitoring, billing, subscription)" also repeats Apps 06, 07, 08 and 15. [CG] says cross-cutting capabilities "should not be duplicated inside each application".

## 3.4 Missing information
**Capabilities in [PO] and [WB] (69) but not described in [CG] (50).** These 19 have no [CG] text:

| Capability ID | Application | Capability |
|---|---|---|
| 01.04 | Experience & Customer Portal | Notifications & Communications |
| 02.05 | Product & Catalog Management | Localization |
| 03.04 | Marketplace | Reviews & Ratings |
| 05.04 | Customer / Tenant Management | Group & Project Management |
| 06.04 | Identity & Access Management | Identity Federation |
| 07.03 | Subscription & Entitlement Management | License & Quota Management |
| 07.04 | Subscription & Entitlement Management | Renewal & Lifecycle |
| 08.05 | Billing & Payments | Tax & Currency |
| 09.04 | Order & Provisioning Management | Approval Management |
| 10.03 | Service & Resource Management | Configuration Management |
| 10.04 | Service & Resource Management | Monitoring & Health |
| 11.03 | Training & Knowledge Management | Training Delivery |
| 11.04 | Training & Knowledge Management | Certification |
| 12.04 | Support & Service Management | Incident & Problem Management |
| 14.04 | Partner & Provider Management | Partner Operations |
| 15.04 | Administration & Governance | Compliance |
| 15.05 | Administration & Governance | Regional Operations |
| 16.03 | Analytics & Data Platform | Operational Analytics |
| 16.05 | Analytics & Data Platform | Data Platform |

**Not specified anywhere:**
- **Sprint calendar.** Sprint length, start and end dates, and sprints per PI are not given. Sprint numbers `.1`, `.2` and `.3` appear, which suggests 3 sprints per quarter (**Inference**).
- **Sprint-level scope.** [PO] assigns one sprint per EIS application. It does **not** say which capabilities, features or functions go into that sprint, or whether the application must be finished in it.
- **Deliverables per sprint, acceptance criteria, Definition of Done, owners, team and capacity.**
- **Dependencies between sprints and applications.** None are stated for EIS. The only stated dependencies are SW Life Cycle on Macro Planner and Agile Planner, and AP-C15 "MACRO PLANNER Integration".
- **Requirements with IDs.** There are no REQ, FTR or STORY IDs in any document. The only ID'd requirements are NFR-001 to NFR-014. [CG] mentions the "FTR-CAT-001" format as an example only.
- **Business rules.** [CG] recommends a Business Rule level, and its only example is "Only recommend eligible products". [WB] has **no business rules column or sheet**.
- **The 10 AI sheets [CG] proposed** (AI Agents, Agent Tools, MCP Servers and so on) are **not in [WB]**.
- **Features and functions for Macro Planner #11 to #15, Agile Planner AP-C11 and AP-C13 to AP-C18, and all 23 SW Life Cycle capabilities.**
- **Product Life Cycle Management, Production Management and Service Management (Thiran), and all of Tharav, Valam, Varthan and Yukth.**
- **Thiran's vision** (the heading exists, the text is empty).
- **Application IDs for the SaaS applications.** Macro Planner, Agile Planner and SW Life Cycle have capability IDs but no application ID.
- **Mapping of the hosted products to the EIS catalog.** The repository README names Valam.ai and the others as product suites. [PO] names them as SaaS products. Neither document says how they relate to App 02 Product & Catalog.
- **In [WB]:** Business Purpose, Feature Description, Notes, and realistic Priority, MVP and Journey values (see 2.6 to 2.8 and 2.18).

## 3.5 Conflicts
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

---

# 4. Every sprint in every document

**Where sprints appear:** Only **[PO]** contains PI/sprint values, in 4 tables with 72 rows in total: EIS 16, Macro Planner 15, Agile Planner 18, SW Life Cycle 23. **[CG]** has no sprints (phases only). **[WB]** has no sprints (Phase 1/2/3 only).

**Format:** `PI = <calendar year>.<quarter>` and `Sprint = <PI>.<n>`. Sprint length and dates are Not specified.


**Distinct sprint IDs found (10):** `2026.3.3`, `2026.4.1`, `2026.4.2`, `2026.4.3`, `2027.1.1`, `2027.1.2`, `2027.1.3`, `2027.2.1`, `2027.2.2`, `2027.4.1`.

## 4.1 Master sprint table (all 72 rows)
Column notes:
- **Features/capabilities** for EIS rows are the [WB] capabilities and features of that application. For SaaS rows they are the features in [PO].
- **Requirements** for EIS rows are the number of [WB] functions (functional-requirement candidates) plus the applicable NFRs.
- **Dependencies** are only what the documents state or directly imply through [WB] relationships. Each one is labelled with its source.


| Application ID | Application | Product | Capability (SaaS rows) | PI – CY Quarter | Sprint | Planned work | Features / capabilities | Requirements | Expected deliverables | Dependencies | Source |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 01 | Experience & Customer Portal | EIS (PaaS) | (whole application) | 2026.3 | 2026.3.3 | Customer-facing web/mobile experience | 01.01 Portal Experience (Responsive Web Portal, Accessibility); 01.02 Customer Dashboard (Business Overview, Service Health); 01.03 Global Search (Unified Search); 01.04 Notifications & Communications (Notification Center, Outbound Communications) | 26 functions ([WB:Functions] 01.*). NFRs: all 14 are platform-wide; most relevant by category (Inference): NFR-005, NFR-010 | Not specified | Stated: Not specified. Implied: Consumes data from most apps (dashboard shows subscriptions, spending, usage, service health, tickets: [WB:Functions] 01.02) | [PO] "eVyoog EIS - Roadmap Initiatives" table |
| 06 | Identity & Access Management | EIS (PaaS) | (whole application) | 2026.3 | 2026.3.3 | Authentication, authorization, roles and policies | 06.01 Authentication (Credentials, MFA); 06.02 Authorization (RBAC, Policy); 06.03 Privileged Access (Administrative Access); 06.04 Identity Federation (SSO) | 22 functions ([WB:Functions] 06.*). NFRs: all 14 are platform-wide; most relevant by category (Inference): NFR-006, NFR-003 | Not specified | Stated: Not specified. Implied: Foundation for all apps ([WB:Architecture Principles] "Security by default") | [PO] "eVyoog EIS - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-01 Platform & Tenant Management | 2026.3 | 2026.3.3 | Not specified | SWLC-CAP-01 Platform & Tenant Management: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-02 Organization & Project Management | 2026.3 | 2026.3.3 | Not specified | SWLC-CAP-02 Organization & Project Management: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-03 Lifecycle / Work Item Management | 2026.3 | 2026.3.3 | Not specified | SWLC-CAP-03 Lifecycle / Work Item Management: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-04 Requirements Management | 2026.3 | 2026.3.3 | Not specified | SWLC-CAP-04 Requirements Management: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-05 Architecture & Design Management | 2026.3 | 2026.3.3 | Not specified | SWLC-CAP-05 Architecture & Design Management: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-06 API & Interface Management | 2026.3 | 2026.3.3 | Not specified | SWLC-CAP-06 API & Interface Management: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-07 Software Configuration / Code Management | 2026.3 | 2026.3.3 | Not specified | SWLC-CAP-07 Software Configuration / Code Management: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-08 Test Management | 2026.3 | 2026.3.3 | Not specified | SWLC-CAP-08 Test Management: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-09 Defect & Issue Management | 2026.3 | 2026.3.3 | Not specified | SWLC-CAP-09 Defect & Issue Management: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-10 Traceability & Impact Analysis | 2026.3 | 2026.3.3 | Not specified | SWLC-CAP-10 Traceability & Impact Analysis: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| Not specified | Agile Planner | Thittam (SaaS) | AP-C01 Organization & Tenant Management | 2026.3 | 2026.3.3 | Not specified | AP-C01 Organization & Tenant Management: Multi-Tenant Organization, User Management, Role & Permission Management | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Agile Planner - Roadmap Initiatives" table |
| Not specified | Agile Planner | Thittam (SaaS) | AP-C02 Portfolio / Program Management | 2026.3 | 2026.3.3 | Not specified | AP-C02 Portfolio / Program Management: Portfolio, Program, Goal Management | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Agile Planner - Roadmap Initiatives" table |
| Not specified | Agile Planner | Thittam (SaaS) | AP-C03 Application / Product Management | 2026.3 | 2026.3.3 | Not specified | AP-C03 Application / Product Management: Application, Product Registry, Product Planning | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Agile Planner - Roadmap Initiatives" table |
| Not specified | Agile Planner | Thittam (SaaS) | AP-C04 Capability & Feature Management | 2026.3 | 2026.3.3 | Not specified | AP-C04 Capability & Feature Management: Capability Management, Feature Management | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Agile Planner - Roadmap Initiatives" table |
| Not specified | Agile Planner | Thittam (SaaS) | AP-C05 Function & Backlog Management | 2026.3 | 2026.3.3 | Not specified | AP-C05 Function & Backlog Management: Function Management | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Agile Planner - Roadmap Initiatives" table |
| Not specified | Agile Planner | Thittam (SaaS) | AP-C06 Agile Planning & Sprint Management | 2026.3 | 2026.3.3 | Not specified | AP-C06 Agile Planning & Sprint Management: Sprint Management | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Agile Planner - Roadmap Initiatives" table |
| Not specified | Agile Planner | Thittam (SaaS) | AP-C07 Team & Resource Management | 2026.3 | 2026.3.3 | Not specified | AP-C07 Team & Resource Management: Teams | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Agile Planner - Roadmap Initiatives" table |
| Not specified | Agile Planner | Thittam (SaaS) | AP-C08 Board Management | 2026.3 | 2026.3.3 | Not specified | AP-C08 Board Management: BOARD | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Agile Planner - Roadmap Initiatives" table |
| Not specified | Agile Planner | Thittam (SaaS) | AP-C09 Workflow & State Management | 2026.3 | 2026.3.3 | Not specified | AP-C09 Workflow & State Management: Configurable workflow engine | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Agile Planner - Roadmap Initiatives" table |
| Not specified | Agile Planner | Thittam (SaaS) | AP-C10 Work Assignment & Collaboration | 2026.3 | 2026.3.3 | Not specified | AP-C10 Work Assignment & Collaboration: Assign work, Reassign work, Followers, Comments, Mentions, Attachments, Checklist, Activity history, Notifications, Work log, Time tracking, Approval, Escalation | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Agile Planner - Roadmap Initiatives" table |
| Not specified | Macro Planner | Thittam (SaaS) | #1 Organization & Identity | 2026.3 | 2026.3.3 | Organization, divisions, units, teams, users, roles | #1 Organization & Identity: Organization creation, Organization profile, Organization hierarchy, Division management, Business-unit management, Department management, Location management, Cost-center management, User provisioning, Authentication, Authorization, Roles, Permissions, Groups, SSO | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Macro Planner - Roadmap Initiatives" table |
| Not specified | Macro Planner | Thittam (SaaS) | #2 Planning & Portfolio | 2026.3 | 2026.3.3 | Create/manage plans and planning hierarchy | #2 Planning & Portfolio: Create plan, Plan types, Plan hierarchy, Plan versioning, Plan lifecycle, Plan ownership, Plan status, Hierarchical planning, Parent, child plans, Cross-plan relationships, Roll-up planning, Cascading targets, Cascading status, Annual planning, Quarterly planning, Monthly planning, Weekly planning, Fiscal calendars, Custom periods | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Macro Planner - Roadmap Initiatives" table |
| Not specified | Macro Planner | Thittam (SaaS) | #3 Work Management | 2026.3 | 2026.3.3 | Tasks, activities, assignments, status, priorities | #3 Work Management: Tasks, Subtasks, Activities, Assignments, Priority, Status, Due dates, Tags, Checklists, Kanban, Planner board, List, Grid, Calendar, Timeline, Gantt | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Macro Planner - Roadmap Initiatives" table |
| Not specified | Macro Planner | Thittam (SaaS) | #4 Schedule & Dependency | 2026.3 | 2026.3.3 | Dates, milestones, dependencies, critical path | #4 Schedule & Dependency: Milestones, Gates, Deliverables, Approval points, Start, end dates, Duration, Calendar, Working days, Baselines, Forecast dates | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Macro Planner - Roadmap Initiatives" table |
| Not specified | Macro Planner | Thittam (SaaS) | #5 Resource Management | 2026.3 | 2026.3.3 | People, teams, capacity, allocation | #5 Resource Management: Team creation, Team membership, Team hierarchy, Team roles, Team responsibilities, Team capacity, Available capacity, Planned capacity, Allocated capacity, Utilization, Over-allocation | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Macro Planner - Roadmap Initiatives" table |
| 02 | Product & Catalog Management | EIS (PaaS) | (whole application) | 2026.4 | 2026.4.1 | Products, solutions, services, plans and content | 02.01 Product Management (Product Lifecycle, Product Structure); 02.02 Offering Management (Offering Definition, Availability); 02.03 Plan Management (Plan Definition, Pricing Models); 02.04 Product Content (Product Documentation, Rich Media); 02.05 Localization (Content Localization, Regionalization) | 35 functions ([WB:Functions] 02.*). NFRs: all 14 are platform-wide; most relevant by category (Inference): NFR-010, NFR-012 | Not specified | Stated: Not specified. Implied: EVT-003 ProductPublished and EVT-004 OfferingUpdated are consumed by Marketplace, Search and AI ([WB:Events]) | [PO] "eVyoog EIS - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-11 Workflow & Approval Management | 2026.4 | 2026.4.1 | Not specified | SWLC-CAP-11 Workflow & Approval Management: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| 05 | Customer / Tenant Management | EIS (PaaS) | (whole application) | 2026.4 | 2026.4.2 | Organizations, tenants, users and projects | 05.01 Organization Management (Organization Lifecycle); 05.02 Tenant Management (Tenant Lifecycle); 05.03 User Management (User Lifecycle, Role Assignment); 05.04 Group & Project Management (Groups, Projects) | 24 functions ([WB:Functions] 05.*). NFRs: all 14 are platform-wide; most relevant by category (Inference): NFR-003 | Not specified | Stated: Not specified. Implied: Needs Identity (06) per UJ-004 ([WB:User Journeys]). EVT-002 TenantCreated feeds Provisioning and Audit | [PO] "eVyoog EIS - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-12 Change & Configuration Management | 2026.4 | 2026.4.2 | Not specified | SWLC-CAP-12 Change & Configuration Management: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-13 Release & Deployment Management | 2026.4 | 2026.4.2 | Not specified | SWLC-CAP-13 Release & Deployment Management: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| Not specified | Agile Planner | Thittam (SaaS) | AP-C11 Progress & Status Management | 2026.4 | 2026.4.2 | Not specified | AP-C11 Progress & Status Management: Not specified | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Agile Planner - Roadmap Initiatives" table |
| Not specified | Agile Planner | Thittam (SaaS) | AP-C13 Metrics, Dashboards & Reporting | 2026.4 | 2026.4.2 | Not specified | AP-C13 Metrics, Dashboards & Reporting: Not specified | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Agile Planner - Roadmap Initiatives" table |
| Not specified | Macro Planner | Thittam (SaaS) | #6 Goal & KPI Management | 2026.4 | 2026.4.2 | Objectives, KPIs, targets, actuals | #6 Goal & KPI Management: KPI definition, KPI target, Actual, Forecast, Threshold, Variance, Trend | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Macro Planner - Roadmap Initiatives" table |
| Not specified | Macro Planner | Thittam (SaaS) | #8 Collaboration | 2026.4 | 2026.4.2 | Comments, discussions, notifications, documents | #8 Collaboration: Comments, Mentions, Discussions, Notifications, Activity feed, @user, @team | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Macro Planner - Roadmap Initiatives" table |
| 08 | Billing & Payments | EIS (PaaS) | (whole application) | 2026.4 | 2026.4.3 | Pricing, billing, invoices, taxes and payments | 08.01 Pricing (Price Books, Promotions); 08.02 Billing (Usage Billing, Invoice Generation); 08.03 Payment (Payment Methods, Transactions); 08.04 Financial Documents (Documents); 08.05 Tax & Currency (Tax, Currency) | 33 functions ([WB:Functions] 08.*). NFRs: all 14 are platform-wide; most relevant by category (Inference): NFR-007, NFR-008, NFR-014 | Not specified | Stated: Not specified. Implied: Needs Subscription (07) and Order (09). EVT-015 UsageRecorded from Resource (10) ([WB:Events]) | [PO] "eVyoog EIS - Roadmap Initiatives" table |
| 07 | Subscription & Entitlement Management | EIS (PaaS) | (whole application) | 2026.4 | 2026.4.3 | Subscriptions, licenses, quotas and entitlements | 07.01 Subscription Management (Subscription Lifecycle, Subscription Changes); 07.02 Entitlement Management (Entitlements, Quota); 07.03 License & Quota Management (Licensing, Usage Limits); 07.04 Renewal & Lifecycle (Renewals, Lifecycle) | 30 functions ([WB:Functions] 07.*). NFRs: all 14 are platform-wide; most relevant by category (Inference): NFR-008 | Not specified | Stated: Not specified. Implied: Needs Catalog plans (02), Order (09) and Pricing (08). EVT-009 SubscriptionCreated feeds Entitlement and Provisioning ([WB:Events]) | [PO] "eVyoog EIS - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-14 Documentation Management | 2026.4 | 2026.4.3 | Not specified | SWLC-CAP-14 Documentation Management: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-15 Risk & Compliance Management | 2026.4 | 2026.4.3 | Not specified | SWLC-CAP-15 Risk & Compliance Management: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| Not specified | Agile Planner | Thittam (SaaS) | AP-C12 Dependency & Risk Management | 2026.4 | 2026.4.3 | Not specified | AP-C12 Dependency & Risk Management: Work dependency, Feature dependency, Team dependency, Application dependency, External dependency, Blocking relationship, Risk, Issue, Assumption, Decision, Escalation | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Agile Planner - Roadmap Initiatives" table |
| Not specified | Macro Planner | Thittam (SaaS) | #7 Template Management | 2026.4 | 2026.4.3 | Business/project/production/etc. plan templates | #7 Template Management: Business Plan, Sales Plan, Marketing Plan, HR Plan, Finance Plan, Production Plan, Purchase Plan, Delivery Plan, Project Plan, Product Plan, Training Plan, Strategic Plan, Operational Plan, Capacity Plan, Resource Plan, Quality Plan, Risk Plan, Compliance Plan | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Macro Planner - Roadmap Initiatives" table |
| 16 | Analytics & Data Platform | EIS (PaaS) | (whole application) | 2027.1 | 2027.1.1 | Customer, product, operational and business analytics | 16.01 Customer Analytics (Customer Usage, Customer Value); 16.02 Product Analytics (Product Performance, Product Usage); 16.03 Operational Analytics (Operations); 16.04 Business Analytics (Financial KPIs, Growth KPIs); 16.05 Data Platform (Data Ingestion, Data Management) | 32 functions ([WB:Functions] 16.*). NFRs: all 14 are platform-wide; most relevant by category (Inference): Not specified | Not specified | Stated: Not specified. Implied: Consumes events from all apps (EVT-001, EVT-015, EVT-018 consumers = Analytics) ([WB:Events]) | [PO] "eVyoog EIS - Roadmap Initiatives" table |
| 13 | Integration & API Platform | EIS (PaaS) | (whole application) | 2027.1 | 2027.1.1 | APIs, connectors, events and webhooks | 13.01 API Management (API Lifecycle, API Security); 13.02 Integration Hub (Connectors, Data Integration); 13.03 Event Platform (Event Bus); 13.04 Webhooks (Webhook Management) | 25 functions ([WB:Functions] 13.*). NFRs: all 14 are platform-wide; most relevant by category (Inference): NFR-012 | Not specified | Stated: Not specified. Implied: Needs IAM (06) for API security | [PO] "eVyoog EIS - Roadmap Initiatives" table |
| 09 | Order & Provisioning Management | EIS (PaaS) | (whole application) | 2027.1 | 2027.1.1 | Orders, provisioning and workflow orchestration | 09.01 Order Management (Order Lifecycle); 09.02 Provisioning (Service Provisioning); 09.03 Workflow Orchestration (Workflow Runtime, Workflow Design); 09.04 Approval Management (Approvals) | 26 functions ([WB:Functions] 09.*). NFRs: all 14 are platform-wide; most relevant by category (Inference): NFR-008 | Not specified | Stated: Not specified. Implied: Needs Checkout (03), Subscription (07), Entitlement (07) and Payment (08). EVT-006 and EVT-012 to EVT-014 ([WB:Events]); UJ-005 | [PO] "eVyoog EIS - Roadmap Initiatives" table |
| 10 | Service & Resource Management | EIS (PaaS) | (whole application) | 2027.1 | 2027.1.1 | Service instances and cloud/platform resources | 10.01 Service Management (Service Instance); 10.02 Resource Management (Resource Lifecycle); 10.03 Configuration Management (Configuration); 10.04 Monitoring & Health (Health Monitoring, Usage Monitoring) | 23 functions ([WB:Functions] 10.*). NFRs: all 14 are platform-wide; most relevant by category (Inference): NFR-009, NFR-013 | Not specified | Stated: Not specified. Implied: Needs Provisioning (09) and Entitlement (07). EVT-011 EntitlementGranted and EVT-012 ProvisioningStarted ([WB:Events]) | [PO] "eVyoog EIS - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-16 Reporting & Analytics | 2027.1 | 2027.1.1 | Not specified | SWLC-CAP-16 Reporting & Analytics: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-17 Collaboration & Review | 2027.1 | 2027.1.1 | Not specified | SWLC-CAP-17 Collaboration & Review: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| Not specified | Agile Planner | Thittam (SaaS) | AP-C16 Automation & Notifications | 2027.1 | 2027.1.1 | Not specified | AP-C16 Automation & Notifications: Not specified | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Agile Planner - Roadmap Initiatives" table |
| Not specified | Macro Planner | Thittam (SaaS) | #11 Integration & API | 2027.1 | 2027.1.1 | ERP, CRM, ALM, Agile, HR, finance, external SaaS | #11 Integration & API: Not specified | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Macro Planner - Roadmap Initiatives" table |
| Not specified | Macro Planner | Thittam (SaaS) | #13 Administration & Configuration | 2027.1 | 2027.1.1 | Tenant/platform configuration | #13 Administration & Configuration: Not specified | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Macro Planner - Roadmap Initiatives" table |
| 04 | AI Advisor & Agent Platform | EIS (PaaS) | (whole application) | 2027.1 | 2027.1.2 | AI-guided selling, technical assistance and support | 04.01 AI Product Advisor (Requirement Discovery, Recommendation); 04.02 AI Sales Agent (Sales Assistance); 04.03 AI Technical Advisor (Technical Guidance); 04.04 AI Support Agent (Support Automation); 04.05 AI Agent Orchestration (Agent Runtime) | 31 functions ([WB:Functions] 04.*). NFRs: all 14 are platform-wide; most relevant by category (Inference): NFR-011 | Not specified | Stated: Not specified. Implied: Needs Catalog (02), Pricing (08) and Knowledge (11) per UJ-002 and UJ-008 ([WB:User Journeys]). [CG]: MCP Gateway, Model Gateway, Policy Engine | [PO] "eVyoog EIS - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-18 AI Engineering Platform | 2027.1 | 2027.1.2 | Not specified | SWLC-CAP-18 AI Engineering Platform: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-19 Automation Platform | 2027.1 | 2027.1.2 | Not specified | SWLC-CAP-19 Automation Platform: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| Not specified | Agile Planner | Thittam (SaaS) | AP-C17 Administration, Configuration & Security | 2027.1 | 2027.1.2 | Not specified | AP-C17 Administration, Configuration & Security: Not specified | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Agile Planner - Roadmap Initiatives" table |
| Not specified | Macro Planner | Thittam (SaaS) | #14 Localization | 2027.1 | 2027.1.2 | Language, region, timezone, currency, calendar | #14 Localization: Not specified | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Macro Planner - Roadmap Initiatives" table |
| 03 | Marketplace | EIS (PaaS) | (whole application) | 2027.1 | 2027.1.3 | Discovery, evaluation, comparison and checkout | 03.01 Product Discovery (Catalog Browsing, Recommendations); 03.02 Product Evaluation (Evaluation, Comparison); 03.03 Marketplace Checkout (Checkout, Purchase Validation); 03.04 Reviews & Ratings (Customer Feedback) | 27 functions ([WB:Functions] 03.*). NFRs: all 14 are platform-wide; most relevant by category (Inference): NFR-005 | Not specified | Stated: Not specified. Implied: Needs Catalog (02), Pricing (08), Order (09) and Payment (08) per UJ-003 ([WB:User Journeys]). EVT-005 CheckoutCompleted feeds Order and Billing | [PO] "eVyoog EIS - Roadmap Initiatives" table |
| 12 | Support & Service Management | EIS (PaaS) | (whole application) | 2027.1 | 2027.1.3 | AI/human support, incidents, requests and SLAs | 12.01 Support (Ticket Management); 12.02 AI Support (Conversational Support, Human Handoff); 12.03 SLA Management (SLA Policy, SLA Monitoring); 12.04 Incident & Problem Management (Incident, Problem) | 28 functions ([WB:Functions] 12.*). NFRs: all 14 are platform-wide; most relevant by category (Inference): NFR-011 | Not specified | Stated: Not specified. Implied: Needs AI (04), Knowledge (11) and Service (10) per UJ-008 and UJ-009 ([WB:User Journeys]) | [PO] "eVyoog EIS - Roadmap Initiatives" table |
| 11 | Training & Knowledge Management | EIS (PaaS) | (whole application) | 2027.1 | 2027.1.3 | Documentation, courses, labs and certifications | 11.01 Knowledge Base (Knowledge Articles, AI Knowledge); 11.02 Learning Management (Courses, Learning Paths); 11.03 Training Delivery (Labs & Assessments, Video Learning); 11.04 Certification (Certificates) | 26 functions ([WB:Functions] 11.*). NFRs: all 14 are platform-wide; most relevant by category (Inference): NFR-010 | Not specified | Stated: Not specified. Implied: Needs Catalog/Product (02) (DE-019 KnowledgeArticle → Product; DE-020 Course → Product) ([WB:Data Entities]) | [PO] "eVyoog EIS - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-20 Integration & Marketplace | 2027.1 | 2027.1.3 | Not specified | SWLC-CAP-20 Integration & Marketplace: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-21 Templates & Methodologies | 2027.1 | 2027.1.3 | Not specified | SWLC-CAP-21 Templates & Methodologies: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-22 Administration & Governance | 2027.1 | 2027.1.3 | Not specified | SWLC-CAP-22 Administration & Governance: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| Not specified | SW Life Cycle | Thiran (SaaS) | SWLC-CAP-23 Localization & Regionalization | 2027.1 | 2027.1.3 | Not specified | SWLC-CAP-23 Localization & Regionalization: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: integrates with Macro Planner and Agile Planner ([PO] SW Life Cycle description) | [PO] "SW Life Cycle - Roadmap Initiatives" table |
| Not specified | Agile Planner | Thittam (SaaS) | AP-C18 Marketplace / Integration Platform | 2027.1 | 2027.1.3 | Not specified | AP-C18 Marketplace / Integration Platform: Not specified | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Agile Planner - Roadmap Initiatives" table |
| Not specified | Macro Planner | Thittam (SaaS) | #12 Marketplace & Extensions | 2027.1 | 2027.1.3 | Apps, connectors, templates, plugins | #12 Marketplace & Extensions: Not specified | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Macro Planner - Roadmap Initiatives" table |
| Not specified | Macro Planner | Thittam (SaaS) | #15 Platform Operations | 2027.1 | 2027.1.3 | Security, audit, monitoring, billing, subscription | #15 Platform Operations: Not specified | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Macro Planner - Roadmap Initiatives" table |
| 14 | Partner & Provider Management | EIS (PaaS) | (whole application) | 2027.2 | 2027.2.1 | Providers, publishers, onboarding and revenue sharing | 14.01 Provider Onboarding (Provider Lifecycle, Contracts); 14.02 Publisher Management (Publisher Catalog, Publisher Analytics); 14.03 Revenue Sharing (Commission, Payouts); 14.04 Partner Operations (Partner Support) | 23 functions ([WB:Functions] 14.*). NFRs: all 14 are platform-wide; most relevant by category (Inference): Not specified | Not specified | Stated: Not specified. Implied: Needs Catalog (02) and Marketplace (03) per UJ-010. Revenue sharing needs Billing and Payments (08) | [PO] "eVyoog EIS - Roadmap Initiatives" table |
| 15 | Administration & Governance | EIS (PaaS) | (whole application) | 2027.2 | 2027.2.2 | Platform configuration, policies, audit and compliance | 15.01 Platform Administration (Platform Configuration, Global Settings); 15.02 Policy Management (Policy Lifecycle); 15.03 Audit (Audit Logging, Audit Search); 15.04 Compliance (Compliance Controls, Data Governance); 15.05 Regional Operations (Region Management, Data Residency) | 33 functions ([WB:Functions] 15.*). NFRs: all 14 are platform-wide; most relevant by category (Inference): NFR-004, NFR-007, NFR-014 | Not specified | Stated: Not specified. Implied: Consumes EVT-020 AuditRecorded from all apps. Regional Operations per UJ-011 | [PO] "eVyoog EIS - Roadmap Initiatives" table |
| Not specified | Agile Planner | Thittam (SaaS) | AP-C14 ALM Integration & Traceability | 2026.4 | 2027.4.1 ⚠ C1 | Not specified | AP-C14 ALM Integration & Traceability: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: Not specified. Inference: SW Life Cycle SWLC-CAP-10 Traceability & Impact Analysis (the ALM application in [PO]) | [PO] "Agile Planner - Roadmap Initiatives" table |
| Not specified | Agile Planner | Thittam (SaaS) | AP-C15 MACRO PLANNER Integration | 2026.4 | 2027.4.1 ⚠ C1 | Not specified | AP-C15 MACRO PLANNER Integration: Not specified | Not specified (no requirements or functions in any document) | Not specified | Stated: Macro Planner (the capability itself is "MACRO PLANNER Integration") | [PO] "Agile Planner - Roadmap Initiatives" table |
| Not specified | Macro Planner | Thittam (SaaS) | #10 Analytics & Reporting | 2026.4 | 2027.4.1 ⚠ C1 | Dashboards, reports, progress, variance | #10 Analytics & Reporting: Gantt chart, Critical path, Baseline, Actual vs planned, Schedule variance | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Macro Planner - Roadmap Initiatives" table |
| Not specified | Macro Planner | Thittam (SaaS) | #9 Workflow & Automation | 2026.4 | 2027.4.1 ⚠ C1 | Rules, approvals, triggers, automated actions | #9 Workflow & Automation: Event engine, Rules engine, Workflow engine, Approval engine, Notification engine, Scheduled jobs, Automation actions | Not specified (no requirements or functions in any document) | Not specified | Not specified | [PO] "Macro Planner - Roadmap Initiatives" table |

⚠ **C1:** the sprint ID is not inside the stated PI (see 3.5).

**Planned work** for EIS is the [PO]/[WB] application description. For Macro Planner it is the "Primary Purpose" column. For Agile Planner and SW Life Cycle it is **Not specified**, because those tables have no purpose column.

## 4.2 Sprint-by-sprint view

### Sprint 2026.3.3  (PI stated: 2026.3)
- **EIS 01 Experience & Customer Portal**: 4 capabilities (Portal Experience, Customer Dashboard, Global Search, Notifications & Communications), 26 functions. Detail is in section 5 and Appendix B.
- **EIS 06 Identity & Access Management**: 4 capabilities (Authentication, Authorization, Privileged Access, Identity Federation), 22 functions. Detail is in section 5 and Appendix B.
- **Thiran (SaaS) / SW Life Cycle**: SWLC-CAP-01 Platform & Tenant Management; SWLC-CAP-02 Organization & Project Management; SWLC-CAP-03 Lifecycle / Work Item Management; SWLC-CAP-04 Requirements Management; SWLC-CAP-05 Architecture & Design Management; SWLC-CAP-06 API & Interface Management; SWLC-CAP-07 Software Configuration / Code Management; SWLC-CAP-08 Test Management; SWLC-CAP-09 Defect & Issue Management; SWLC-CAP-10 Traceability & Impact Analysis
- **Thittam (SaaS) / Agile Planner**: AP-C01 Organization & Tenant Management; AP-C02 Portfolio / Program Management; AP-C03 Application / Product Management; AP-C04 Capability & Feature Management; AP-C05 Function & Backlog Management; AP-C06 Agile Planning & Sprint Management; AP-C07 Team & Resource Management; AP-C08 Board Management; AP-C09 Workflow & State Management; AP-C10 Work Assignment & Collaboration
- **Thittam (SaaS) / Macro Planner**: #1 Organization & Identity; #2 Planning & Portfolio; #3 Work Management; #4 Schedule & Dependency; #5 Resource Management

### Sprint 2026.4.1  (PI stated: 2026.4)
- **EIS 02 Product & Catalog Management**: 5 capabilities (Product Management, Offering Management, Plan Management, Product Content, Localization), 35 functions. Detail is in section 5 and Appendix B.
- **Thiran (SaaS) / SW Life Cycle**: SWLC-CAP-11 Workflow & Approval Management

### Sprint 2026.4.2  (PI stated: 2026.4)
- **EIS 05 Customer / Tenant Management**: 4 capabilities (Organization Management, Tenant Management, User Management, Group & Project Management), 24 functions. Detail is in section 5 and Appendix B.
- **Thiran (SaaS) / SW Life Cycle**: SWLC-CAP-12 Change & Configuration Management; SWLC-CAP-13 Release & Deployment Management
- **Thittam (SaaS) / Agile Planner**: AP-C11 Progress & Status Management; AP-C13 Metrics, Dashboards & Reporting
- **Thittam (SaaS) / Macro Planner**: #6 Goal & KPI Management; #8 Collaboration

### Sprint 2026.4.3  (PI stated: 2026.4)
- **EIS 07 Subscription & Entitlement Management**: 4 capabilities (Subscription Management, Entitlement Management, License & Quota Management, Renewal & Lifecycle), 30 functions. Detail is in section 5 and Appendix B.
- **EIS 08 Billing & Payments**: 5 capabilities (Pricing, Billing, Payment, Financial Documents, Tax & Currency), 33 functions. Detail is in section 5 and Appendix B.
- **Thiran (SaaS) / SW Life Cycle**: SWLC-CAP-14 Documentation Management; SWLC-CAP-15 Risk & Compliance Management
- **Thittam (SaaS) / Agile Planner**: AP-C12 Dependency & Risk Management
- **Thittam (SaaS) / Macro Planner**: #7 Template Management

### Sprint 2027.1.1  (PI stated: 2027.1)
- **EIS 09 Order & Provisioning Management**: 4 capabilities (Order Management, Provisioning, Workflow Orchestration, Approval Management), 26 functions. Detail is in section 5 and Appendix B.
- **EIS 10 Service & Resource Management**: 4 capabilities (Service Management, Resource Management, Configuration Management, Monitoring & Health), 23 functions. Detail is in section 5 and Appendix B.
- **EIS 13 Integration & API Platform**: 4 capabilities (API Management, Integration Hub, Event Platform, Webhooks), 25 functions. Detail is in section 5 and Appendix B.
- **EIS 16 Analytics & Data Platform**: 5 capabilities (Customer Analytics, Product Analytics, Operational Analytics, Business Analytics, Data Platform), 32 functions. Detail is in section 5 and Appendix B.
- **Thiran (SaaS) / SW Life Cycle**: SWLC-CAP-16 Reporting & Analytics; SWLC-CAP-17 Collaboration & Review
- **Thittam (SaaS) / Agile Planner**: AP-C16 Automation & Notifications
- **Thittam (SaaS) / Macro Planner**: #11 Integration & API; #13 Administration & Configuration

### Sprint 2027.1.2  (PI stated: 2027.1)
- **EIS 04 AI Advisor & Agent Platform**: 5 capabilities (AI Product Advisor, AI Sales Agent, AI Technical Advisor, AI Support Agent, AI Agent Orchestration), 31 functions. Detail is in section 5 and Appendix B.
- **Thiran (SaaS) / SW Life Cycle**: SWLC-CAP-18 AI Engineering Platform; SWLC-CAP-19 Automation Platform
- **Thittam (SaaS) / Agile Planner**: AP-C17 Administration, Configuration & Security
- **Thittam (SaaS) / Macro Planner**: #14 Localization

### Sprint 2027.1.3  (PI stated: 2027.1)
- **EIS 03 Marketplace**: 4 capabilities (Product Discovery, Product Evaluation, Marketplace Checkout, Reviews & Ratings), 27 functions. Detail is in section 5 and Appendix B.
- **EIS 11 Training & Knowledge Management**: 4 capabilities (Knowledge Base, Learning Management, Training Delivery, Certification), 26 functions. Detail is in section 5 and Appendix B.
- **EIS 12 Support & Service Management**: 4 capabilities (Support, AI Support, SLA Management, Incident & Problem Management), 28 functions. Detail is in section 5 and Appendix B.
- **Thiran (SaaS) / SW Life Cycle**: SWLC-CAP-20 Integration & Marketplace; SWLC-CAP-21 Templates & Methodologies; SWLC-CAP-22 Administration & Governance; SWLC-CAP-23 Localization & Regionalization
- **Thittam (SaaS) / Agile Planner**: AP-C18 Marketplace / Integration Platform
- **Thittam (SaaS) / Macro Planner**: #12 Marketplace & Extensions; #15 Platform Operations

### Sprint 2027.2.1  (PI stated: 2027.2)
- **EIS 14 Partner & Provider Management**: 4 capabilities (Provider Onboarding, Publisher Management, Revenue Sharing, Partner Operations), 23 functions. Detail is in section 5 and Appendix B.

### Sprint 2027.2.2  (PI stated: 2027.2)
- **EIS 15 Administration & Governance**: 5 capabilities (Platform Administration, Policy Management, Audit, Compliance, Regional Operations), 33 functions. Detail is in section 5 and Appendix B.

### Sprint 2027.4.1  (PI stated: 2026.4)  ⚠ C1
- **Thittam (SaaS) / Agile Planner**: AP-C14 ALM Integration & Traceability; AP-C15 MACRO PLANNER Integration
- **Thittam (SaaS) / Macro Planner**: #9 Workflow & Automation; #10 Analytics & Reporting


## 4.3 What each PI contains

- **PI 2026.3**: EIS apps: 01 Experience & Customer Portal (2026.3.3), 06 Identity & Access Management (2026.3.3). SaaS capabilities: Macro Planner ×5, Agile Planner ×10, SW Life Cycle ×10.
- **PI 2026.4**: EIS apps: 02 Product & Catalog Management (2026.4.1), 05 Customer / Tenant Management (2026.4.2), 07 Subscription & Entitlement Management (2026.4.3), 08 Billing & Payments (2026.4.3). SaaS capabilities: Macro Planner ×5, Agile Planner ×5, SW Life Cycle ×5.
- **PI 2027.1**: EIS apps: 03 Marketplace (2027.1.3), 04 AI Advisor & Agent Platform (2027.1.2), 09 Order & Provisioning Management (2027.1.1), 10 Service & Resource Management (2027.1.1), 11 Training & Knowledge Management (2027.1.3), 12 Support & Service Management (2027.1.3), 13 Integration & API Platform (2027.1.1), 16 Analytics & Data Platform (2027.1.1). SaaS capabilities: Macro Planner ×5, Agile Planner ×3, SW Life Cycle ×8.
- **PI 2027.2**: EIS apps: 14 Partner & Provider Management (2027.2.1), 15 Administration & Governance (2027.2.2). SaaS capabilities: none.

---

# 5. Roadmap view: Application → Capability → Feature → Requirement → PI/Quarter → Sprint → Deliverable

**How to read this:**
- **PI and Sprint** are from [PO] Table 2, at application level. [PO] does not assign capabilities or features to sprints, so each row inherits its application's sprint.
- **Requirement** is the [WB:Functions] list for that feature. There are no REQ-IDs in any document; the functions are the nearest thing to functional requirements.
- **Deliverable** is **Not specified** in all sources. The column shows the [WB:APIs] endpoint and [WB:Microservices] service linked in [WB:Traceability], which is the nearest implied deliverable.

## 5.1 EIS (PaaS)

### 01 Experience & Customer Portal  (PI 2026.3, Sprint 2026.3.3)
| Capability | Feature | Requirement (functions) | PI | Sprint | Deliverable |
|---|---|---|---|---|---|
| 01.01 Portal Experience | 01.01.01 Responsive Web Portal | Login; Navigate portal; Select language; Select region; Customize preferences | 2026.3 | 2026.3.3 | Not specified  |
| 01.01 Portal Experience | 01.01.02 Accessibility | Configure accessibility preferences; Use keyboard navigation; Support screen readers | 2026.3 | 2026.3.3 | Not specified  |
| 01.02 Customer Dashboard | 01.02.01 Business Overview | View organization summary; View subscriptions; View spending; View usage | 2026.3 | 2026.3.3 | Not specified  |
| 01.02 Customer Dashboard | 01.02.02 Service Health | View service status; View alerts; View incidents | 2026.3 | 2026.3.3 | Not specified  |
| 01.03 Global Search | 01.03.01 Unified Search | Keyword search; Semantic search; Filter results; Sort results; View search history | 2026.3 | 2026.3.3 | Not specified  |
| 01.04 Notifications & Communications | 01.04.01 Notification Center | View notifications; Mark notification read; Configure notification preferences | 2026.3 | 2026.3.3 | Not specified  |
| 01.04 Notifications & Communications | 01.04.02 Outbound Communications | Send email; Send SMS; Send in-app notification | 2026.3 | 2026.3.3 | Not specified  |

### 02 Product & Catalog Management  (PI 2026.4, Sprint 2026.4.1)
| Capability | Feature | Requirement (functions) | PI | Sprint | Deliverable |
|---|---|---|---|---|---|
| 02.01 Product Management | 02.01.01 Product Lifecycle | Create product [MVP]; Update product [MVP]; Version product [MVP]; Publish product; Retire product | 2026.4 | 2026.4.1 | Not specified (implied: API-004 GET /v1/products; Catalog Service) |
| 02.01 Product Management | 02.01.02 Product Structure | Define product hierarchy [MVP]; Define variants [MVP]; Define dependencies [MVP] | 2026.4 | 2026.4.1 | Not specified (implied: API-004 GET /v1/products; Catalog Service) |
| 02.02 Offering Management | 02.02.01 Offering Definition | Create offering; Bundle products; Define prerequisites; Define compatibility | 2026.4 | 2026.4.1 | Not specified (implied: API-005 GET /v1/offerings/{id}; Catalog Service) |
| 02.02 Offering Management | 02.02.02 Availability | Define regions; Define channels; Define eligibility | 2026.4 | 2026.4.1 | Not specified (implied: API-005 GET /v1/offerings/{id}; Catalog Service) |
| 02.03 Plan Management | 02.03.01 Plan Definition | Create plan; Define billing frequency; Define usage limits; Define included features | 2026.4 | 2026.4.1 | Not specified (implied: Catalog Service) |
| 02.03 Plan Management | 02.03.02 Pricing Models | Define subscription price; Define usage price; Define tier price; Define overage charge | 2026.4 | 2026.4.1 | Not specified (implied: Catalog Service) |
| 02.04 Product Content | 02.04.01 Product Documentation | Upload datasheet; Publish documentation; Version content | 2026.4 | 2026.4.1 | Not specified (implied: Catalog Service) |
| 02.04 Product Content | 02.04.02 Rich Media | Upload images; Upload videos; Manage case studies | 2026.4 | 2026.4.1 | Not specified (implied: Catalog Service) |
| 02.05 Localization | 02.05.01 Content Localization | Translate product content; Translate documentation; Publish localized content | 2026.4 | 2026.4.1 | Not specified (implied: Catalog Service) |
| 02.05 Localization | 02.05.02 Regionalization | Configure currency; Configure date/time format; Configure regional terminology | 2026.4 | 2026.4.1 | Not specified (implied: Catalog Service) |

### 03 Marketplace  (PI 2027.1, Sprint 2027.1.3)
| Capability | Feature | Requirement (functions) | PI | Sprint | Deliverable |
|---|---|---|---|---|---|
| 03.01 Product Discovery | 03.01.01 Catalog Browsing | Browse categories; Search products; Filter products; Sort products | 2027.1 | 2027.1.3 | Not specified (implied: Marketplace Service) |
| 03.01 Product Discovery | 03.01.02 Recommendations | Recommend products; Show featured products; Show popular products | 2027.1 | 2027.1.3 | Not specified (implied: Marketplace Service) |
| 03.02 Product Evaluation | 03.02.01 Evaluation | Start trial; Request demo; Launch sandbox; View prerequisites | 2027.1 | 2027.1.3 | Not specified (implied: Marketplace Service) |
| 03.02 Product Evaluation | 03.02.02 Comparison | Compare products; Compare plans; Estimate cost | 2027.1 | 2027.1.3 | Not specified (implied: Marketplace Service) |
| 03.03 Marketplace Checkout | 03.03.01 Checkout | Select product; Select plan; Configure options; Apply discount; Accept terms; Submit order | 2027.1 | 2027.1.3 | Not specified (implied: API-008 POST /v1/checkout; Marketplace Service) |
| 03.03 Marketplace Checkout | 03.03.02 Purchase Validation | Validate eligibility; Validate payment; Validate dependencies | 2027.1 | 2027.1.3 | Not specified (implied: API-008 POST /v1/checkout; Marketplace Service) |
| 03.04 Reviews & Ratings | 03.04.01 Customer Feedback | Submit review; Rate product; Moderate review; View ratings | 2027.1 | 2027.1.3 | Not specified (implied: Marketplace Service) |

### 04 AI Advisor & Agent Platform  (PI 2027.1, Sprint 2027.1.2)
| Capability | Feature | Requirement (functions) | PI | Sprint | Deliverable |
|---|---|---|---|---|---|
| 04.01 AI Product Advisor | 04.01.01 Requirement Discovery | Ask customer questions; Capture requirements; Identify constraints | 2027.1 | 2027.1.2 | Not specified (implied: API-007 POST /v1/recommendations; AI Agent Gateway) |
| 04.01 AI Product Advisor | 04.01.02 Recommendation | Search catalog; Evaluate compatibility; Rank products; Explain recommendation; Recommend configuration; Estimate cost | 2027.1 | 2027.1.2 | Not specified (implied: API-007 POST /v1/recommendations; AI Agent Gateway) |
| 04.02 AI Sales Agent | 04.02.01 Sales Assistance | Qualify lead; Explain pricing; Generate proposal; Generate quote; Recommend upsell; Recommend cross-sell | 2027.1 | 2027.1.2 | Not specified (implied: AI Agent Gateway) |
| 04.03 AI Technical Advisor | 04.03.01 Technical Guidance | Recommend architecture; Explain configuration; Troubleshoot issue; Recommend best practice | 2027.1 | 2027.1.2 | Not specified (implied: AI Agent Gateway) |
| 04.04 AI Support Agent | 04.04.01 Support Automation | Understand request; Search knowledge base; Diagnose issue; Recommend resolution; Create ticket; Escalate to human | 2027.1 | 2027.1.2 | Not specified (implied: AI Agent Gateway) |
| 04.05 AI Agent Orchestration | 04.05.01 Agent Runtime | Register agent; Route request; Select tools; Manage context; Apply guardrails; Audit agent action | 2027.1 | 2027.1.2 | Not specified (implied: AI Agent Gateway) |

### 05 Customer / Tenant Management  (PI 2026.4, Sprint 2026.4.2)
| Capability | Feature | Requirement (functions) | PI | Sprint | Deliverable |
|---|---|---|---|---|---|
| 05.01 Organization Management | 05.01.01 Organization Lifecycle | Create organization; Update organization; Suspend organization; Activate organization; Close organization | 2026.4 | 2026.4.2 | Not specified (implied: Tenant Service) |
| 05.02 Tenant Management | 05.02.01 Tenant Lifecycle | Create tenant; Configure tenant; Assign region; Configure isolation; Configure tenant policies | 2026.4 | 2026.4.2 | Not specified (implied: API-003 POST /v1/tenants; Tenant Service) |
| 05.03 User Management | 05.03.01 User Lifecycle | Invite user; Create user; Activate user; Suspend user; Remove user | 2026.4 | 2026.4.2 | Not specified (implied: Tenant Service) |
| 05.03 User Management | 05.03.02 Role Assignment | Assign role; Assign group; Review access | 2026.4 | 2026.4.2 | Not specified (implied: Tenant Service) |
| 05.04 Group & Project Management | 05.04.01 Groups | Create group; Add member; Remove member | 2026.4 | 2026.4.2 | Not specified (implied: Tenant Service) |
| 05.04 Group & Project Management | 05.04.02 Projects | Create project; Assign users; Assign resources | 2026.4 | 2026.4.2 | Not specified (implied: Tenant Service) |

### 06 Identity & Access Management  (PI 2026.3, Sprint 2026.3.3)
| Capability | Feature | Requirement (functions) | PI | Sprint | Deliverable |
|---|---|---|---|---|---|
| 06.01 Authentication | 06.01.01 Credentials | Sign in; Sign out; Reset password; Manage sessions | 2026.3 | 2026.3.3 | Not specified (implied: API-001 POST /v1/auth/login; Identity Service) |
| 06.01 Authentication | 06.01.02 MFA | Enroll MFA; Verify MFA; Recover MFA | 2026.3 | 2026.3.3 | Not specified (implied: API-001 POST /v1/auth/login; Identity Service) |
| 06.02 Authorization | 06.02.01 RBAC | Create role; Define permission; Assign role; Evaluate permission | 2026.3 | 2026.3.3 | Not specified (implied: API-002 POST /v1/authz/evaluate; Identity Service) |
| 06.02 Authorization | 06.02.02 Policy | Create policy; Assign policy; Evaluate policy | 2026.3 | 2026.3.3 | Not specified (implied: API-002 POST /v1/authz/evaluate; Identity Service) |
| 06.03 Privileged Access | 06.03.01 Administrative Access | Request elevated access; Approve access; Grant temporary access; Revoke access | 2026.3 | 2026.3.3 | Not specified (implied: Identity Service) |
| 06.04 Identity Federation | 06.04.01 SSO | Configure SAML; Configure OIDC; Map claims; Test federation | 2026.3 | 2026.3.3 | Not specified (implied: Identity Service) |

### 07 Subscription & Entitlement Management  (PI 2026.4, Sprint 2026.4.3)
| Capability | Feature | Requirement (functions) | PI | Sprint | Deliverable |
|---|---|---|---|---|---|
| 07.01 Subscription Management | 07.01.01 Subscription Lifecycle | Create subscription [MVP]; Activate subscription [MVP]; Suspend subscription [MVP]; Upgrade; Downgrade; Renew; Cancel | 2026.4 | 2026.4.3 | Not specified (implied: API-011 POST /v1/subscriptions; Subscription Service) |
| 07.01 Subscription Management | 07.01.02 Subscription Changes | Change quantity [MVP]; Change plan [MVP]; Schedule change [MVP] | 2026.4 | 2026.4.3 | Not specified (implied: API-011 POST /v1/subscriptions; Subscription Service) |
| 07.02 Entitlement Management | 07.02.01 Entitlements | Grant entitlement; Revoke entitlement; Validate entitlement; Check feature access | 2026.4 | 2026.4.3 | Not specified (implied: API-012 POST /v1/entitlements/check; Subscription Service) |
| 07.02 Entitlement Management | 07.02.02 Quota | Check quota; Allocate quota; Adjust quota | 2026.4 | 2026.4.3 | Not specified (implied: API-012 POST /v1/entitlements/check; Subscription Service) |
| 07.03 License & Quota Management | 07.03.01 Licensing | Issue license; Validate license; Expire license; Revoke license | 2026.4 | 2026.4.3 | Not specified (implied: Subscription Service) |
| 07.03 License & Quota Management | 07.03.02 Usage Limits | Define quota; Monitor quota; Enforce quota | 2026.4 | 2026.4.3 | Not specified (implied: Subscription Service) |
| 07.04 Renewal & Lifecycle | 07.04.01 Renewals | Schedule renewal; Notify renewal; Auto-renew; Process renewal | 2026.4 | 2026.4.3 | Not specified (implied: Subscription Service) |
| 07.04 Renewal & Lifecycle | 07.04.02 Lifecycle | Expire subscription; Reactivate subscription | 2026.4 | 2026.4.3 | Not specified (implied: Subscription Service) |

### 08 Billing & Payments  (PI 2026.4, Sprint 2026.4.3)
| Capability | Feature | Requirement (functions) | PI | Sprint | Deliverable |
|---|---|---|---|---|---|
| 08.01 Pricing | 08.01.01 Price Books | Create price; Define tiers; Define volume pricing; Define customer pricing | 2026.4 | 2026.4.3 | Not specified (implied: API-006 POST /v1/pricing/quote; Pricing Service) |
| 08.01 Pricing | 08.01.02 Promotions | Create discount; Create coupon; Apply promotion | 2026.4 | 2026.4.3 | Not specified (implied: API-006 POST /v1/pricing/quote; Pricing Service) |
| 08.02 Billing | 08.02.01 Usage Billing | Collect usage; Calculate charges; Apply discounts; Calculate taxes | 2026.4 | 2026.4.3 | Not specified (implied: API-013 POST /v1/billing/invoices; Pricing Service) |
| 08.02 Billing | 08.02.02 Invoice Generation | Generate invoice; Adjust invoice; Credit invoice; Finalize invoice | 2026.4 | 2026.4.3 | Not specified (implied: API-013 POST /v1/billing/invoices; Pricing Service) |
| 08.03 Payment | 08.03.01 Payment Methods | Add payment method; Remove payment method; Set default payment method | 2026.4 | 2026.4.3 | Not specified (implied: API-014 POST /v1/payments; Pricing Service) |
| 08.03 Payment | 08.03.02 Transactions | Authorize payment; Capture payment; Refund payment; Retry payment; Reconcile payment | 2026.4 | 2026.4.3 | Not specified (implied: API-014 POST /v1/payments; Pricing Service) |
| 08.04 Financial Documents | 08.04.01 Documents | Generate invoice; Generate receipt; Generate credit note; Download document | 2026.4 | 2026.4.3 | Not specified (implied: Pricing Service) |
| 08.05 Tax & Currency | 08.05.01 Tax | Configure tax rules; Calculate tax; Validate tax | 2026.4 | 2026.4.3 | Not specified (implied: Pricing Service) |
| 08.05 Tax & Currency | 08.05.02 Currency | Configure currency; Convert currency; Format currency | 2026.4 | 2026.4.3 | Not specified (implied: Pricing Service) |

### 09 Order & Provisioning Management  (PI 2027.1, Sprint 2027.1.1)
| Capability | Feature | Requirement (functions) | PI | Sprint | Deliverable |
|---|---|---|---|---|---|
| 09.01 Order Management | 09.01.01 Order Lifecycle | Create order [MVP]; Validate order [MVP]; Price order [MVP]; Submit order; Approve order; Cancel order; Track order | 2027.1 | 2027.1.1 | Not specified (implied: API-009 POST /v1/orders; Order Service) |
| 09.02 Provisioning | 09.02.01 Service Provisioning | Provision service; Configure service; Activate service; Suspend service; Deprovision service | 2027.1 | 2027.1.1 | Not specified (implied: API-010 POST /v1/provisioning; Order Service) |
| 09.03 Workflow Orchestration | 09.03.01 Workflow Runtime | Trigger workflow; Execute workflow; Retry step; Rollback; Compensate; Escalate | 2027.1 | 2027.1.1 | Not specified (implied: Order Service) |
| 09.03 Workflow Orchestration | 09.03.02 Workflow Design | Define workflow; Configure step; Set dependency | 2027.1 | 2027.1.1 | Not specified (implied: Order Service) |
| 09.04 Approval Management | 09.04.01 Approvals | Create approval; Route approval; Approve; Reject; Escalate | 2027.1 | 2027.1.1 | Not specified (implied: Order Service) |

### 10 Service & Resource Management  (PI 2027.1, Sprint 2027.1.1)
| Capability | Feature | Requirement (functions) | PI | Sprint | Deliverable |
|---|---|---|---|---|---|
| 10.01 Service Management | 10.01.01 Service Instance | Create service instance; Configure service; Start service; Stop service; Restart service; Scale service; Delete service | 2027.1 | 2027.1.1 | Not specified (implied: API-015 GET /v1/services; Resource Service) |
| 10.02 Resource Management | 10.02.01 Resource Lifecycle | Create resource; Update resource; Scale resource; Monitor resource; Delete resource | 2027.1 | 2027.1.1 | Not specified (implied: Resource Service) |
| 10.03 Configuration Management | 10.03.01 Configuration | Create configuration; Validate configuration; Apply configuration; Rollback configuration | 2027.1 | 2027.1.1 | Not specified (implied: Resource Service) |
| 10.04 Monitoring & Health | 10.04.01 Health Monitoring | Collect health status; Detect anomaly; Create alert; View health | 2027.1 | 2027.1.1 | Not specified (implied: Resource Service) |
| 10.04 Monitoring & Health | 10.04.02 Usage Monitoring | Collect metrics; View usage; Set threshold | 2027.1 | 2027.1.1 | Not specified (implied: Resource Service) |

### 11 Training & Knowledge Management  (PI 2027.1, Sprint 2027.1.3)
| Capability | Feature | Requirement (functions) | PI | Sprint | Deliverable |
|---|---|---|---|---|---|
| 11.01 Knowledge Base | 11.01.01 Knowledge Articles | Create article; Edit article; Publish article; Search article; Version article | 2027.1 | 2027.1.3 | Not specified (implied: API-017 POST /v1/knowledge/search; Knowledge Service) |
| 11.01 Knowledge Base | 11.01.02 AI Knowledge | Index content; Retrieve relevant content; Validate source | 2027.1 | 2027.1.3 | Not specified (implied: API-017 POST /v1/knowledge/search; Knowledge Service) |
| 11.02 Learning Management | 11.02.01 Courses | Create course; Publish course; Enroll user; Track progress; Complete course | 2027.1 | 2027.1.3 | Not specified (implied: Knowledge Service) |
| 11.02 Learning Management | 11.02.02 Learning Paths | Create learning path; Assign learning path; Track path progress | 2027.1 | 2027.1.3 | Not specified (implied: Knowledge Service) |
| 11.03 Training Delivery | 11.03.01 Labs & Assessments | Launch lab; Submit assessment; Score assessment; Track completion | 2027.1 | 2027.1.3 | Not specified (implied: Knowledge Service) |
| 11.03 Training Delivery | 11.03.02 Video Learning | Stream video; Track watch progress | 2027.1 | 2027.1.3 | Not specified (implied: Knowledge Service) |
| 11.04 Certification | 11.04.01 Certificates | Define certification; Issue certificate; Verify certificate; Expire certificate | 2027.1 | 2027.1.3 | Not specified (implied: Knowledge Service) |

### 12 Support & Service Management  (PI 2027.1, Sprint 2027.1.3)
| Capability | Feature | Requirement (functions) | PI | Sprint | Deliverable |
|---|---|---|---|---|---|
| 12.01 Support | 12.01.01 Ticket Management | Create ticket; Categorize ticket; Prioritize ticket; Assign ticket; Escalate ticket; Resolve ticket; Close ticket | 2027.1 | 2027.1.3 | Not specified (implied: API-016 POST /v1/support/tickets; Support Service) |
| 12.02 AI Support | 12.02.01 Conversational Support | Start conversation; Search knowledge; Diagnose issue; Recommend resolution; Execute permitted remediation | 2027.1 | 2027.1.3 | Not specified (implied: Support Service) |
| 12.02 AI Support | 12.02.02 Human Handoff | Create ticket; Transfer conversation; Provide AI summary | 2027.1 | 2027.1.3 | Not specified (implied: Support Service) |
| 12.03 SLA Management | 12.03.01 SLA Policy | Define SLA; Assign SLA; Calculate SLA | 2027.1 | 2027.1.3 | Not specified (implied: Support Service) |
| 12.03 SLA Management | 12.03.02 SLA Monitoring | Monitor SLA; Warn before breach; Escalate breach | 2027.1 | 2027.1.3 | Not specified (implied: Support Service) |
| 12.04 Incident & Problem Management | 12.04.01 Incident | Log incident; Investigate incident; Resolve incident; Close incident | 2027.1 | 2027.1.3 | Not specified (implied: Support Service) |
| 12.04 Incident & Problem Management | 12.04.02 Problem | Create problem; Perform root cause analysis; Track corrective action | 2027.1 | 2027.1.3 | Not specified (implied: Support Service) |

### 13 Integration & API Platform  (PI 2027.1, Sprint 2027.1.1)
| Capability | Feature | Requirement (functions) | PI | Sprint | Deliverable |
|---|---|---|---|---|---|
| 13.01 API Management | 13.01.01 API Lifecycle | Register API; Publish API; Version API; Deprecate API | 2027.1 | 2027.1.1 | Not specified (implied: Integration Service) |
| 13.01 API Management | 13.01.02 API Security | Authenticate API; Authorize API; Rate limit API; Monitor API | 2027.1 | 2027.1.1 | Not specified (implied: Integration Service) |
| 13.02 Integration Hub | 13.02.01 Connectors | Create connector; Authenticate connector; Test connector; Enable connector | 2027.1 | 2027.1.1 | Not specified (implied: API-018 POST /v1/integrations; Integration Service) |
| 13.02 Integration Hub | 13.02.02 Data Integration | Synchronize data; Transform data; Handle integration error | 2027.1 | 2027.1.1 | Not specified (implied: API-018 POST /v1/integrations; Integration Service) |
| 13.03 Event Platform | 13.03.01 Event Bus | Publish event; Subscribe to event; Route event; Retry event; Replay event | 2027.1 | 2027.1.1 | Not specified (implied: API-019 POST /v1/events; Integration Service) |
| 13.04 Webhooks | 13.04.01 Webhook Management | Register webhook; Authenticate webhook; Trigger webhook; Retry webhook; Monitor webhook | 2027.1 | 2027.1.1 | Not specified (implied: Integration Service) |

### 14 Partner & Provider Management  (PI 2027.2, Sprint 2027.2.1)
| Capability | Feature | Requirement (functions) | PI | Sprint | Deliverable |
|---|---|---|---|---|---|
| 14.01 Provider Onboarding | 14.01.01 Provider Lifecycle | Register provider; Verify provider; Approve provider; Activate provider | 2027.2 | 2027.2.1 | Not specified (implied: API-020 POST /v1/providers; Partner Service) |
| 14.01 Provider Onboarding | 14.01.02 Contracts | Create contract; Manage terms; Track expiration | 2027.2 | 2027.2.1 | Not specified (implied: API-020 POST /v1/providers; Partner Service) |
| 14.02 Publisher Management | 14.02.01 Publisher Catalog | Create publisher product; Manage pricing; Manage content; Publish product | 2027.2 | 2027.2.1 | Not specified (implied: Partner Service) |
| 14.02 Publisher Management | 14.02.02 Publisher Analytics | View product performance; View sales; View usage | 2027.2 | 2027.2.1 | Not specified (implied: Partner Service) |
| 14.03 Revenue Sharing | 14.03.01 Commission | Define commission; Calculate revenue share; Generate statement | 2027.2 | 2027.2.1 | Not specified (implied: Partner Service) |
| 14.03 Revenue Sharing | 14.03.02 Payouts | Calculate payout; Approve payout; Reconcile payout | 2027.2 | 2027.2.1 | Not specified (implied: Partner Service) |
| 14.04 Partner Operations | 14.04.01 Partner Support | Create partner ticket; Assign partner manager; Track partner SLA | 2027.2 | 2027.2.1 | Not specified (implied: Partner Service) |

### 15 Administration & Governance  (PI 2027.2, Sprint 2027.2.2)
| Capability | Feature | Requirement (functions) | PI | Sprint | Deliverable |
|---|---|---|---|---|---|
| 15.01 Platform Administration | 15.01.01 Platform Configuration | Configure platform; Configure languages; Configure currencies; Configure feature flags | 2027.2 | 2027.2.2 | Not specified (implied: Audit Service) |
| 15.01 Platform Administration | 15.01.02 Global Settings | Configure regions; Configure defaults; Manage templates | 2027.2 | 2027.2.2 | Not specified (implied: Audit Service) |
| 15.02 Policy Management | 15.02.01 Policy Lifecycle | Create policy; Assign policy; Evaluate policy; Enforce policy; Manage exception | 2027.2 | 2027.2.2 | Not specified (implied: Audit Service) |
| 15.03 Audit | 15.03.01 Audit Logging | Record activity; Record login; Record configuration change; Record financial transaction | 2027.2 | 2027.2.2 | Not specified (implied: API-022 GET /v1/audit/events; Audit Service) |
| 15.03 Audit | 15.03.02 Audit Search | Search audit logs; Filter audit logs; Export audit logs | 2027.2 | 2027.2.2 | Not specified (implied: API-022 GET /v1/audit/events; Audit Service) |
| 15.04 Compliance | 15.04.01 Compliance Controls | Define control; Map requirement; Collect evidence; Track remediation | 2027.2 | 2027.2.2 | Not specified (implied: Audit Service) |
| 15.04 Compliance | 15.04.02 Data Governance | Classify data; Define retention; Apply retention | 2027.2 | 2027.2.2 | Not specified (implied: Audit Service) |
| 15.05 Regional Operations | 15.05.01 Region Management | Create region; Configure region; Activate region; Suspend region | 2027.2 | 2027.2.2 | Not specified (implied: Audit Service) |
| 15.05 Regional Operations | 15.05.02 Data Residency | Define residency policy; Validate residency; Report residency | 2027.2 | 2027.2.2 | Not specified (implied: Audit Service) |

### 16 Analytics & Data Platform  (PI 2027.1, Sprint 2027.1.1)
| Capability | Feature | Requirement (functions) | PI | Sprint | Deliverable |
|---|---|---|---|---|---|
| 16.01 Customer Analytics | 16.01.01 Customer Usage | Analyze usage; Analyze adoption; Analyze engagement; Calculate customer health | 2027.1 | 2027.1.1 | Not specified (implied: Analytics Service) |
| 16.01 Customer Analytics | 16.01.02 Customer Value | Analyze spending; Calculate customer lifetime value; Analyze churn | 2027.1 | 2027.1.1 | Not specified (implied: Analytics Service) |
| 16.02 Product Analytics | 16.02.01 Product Performance | Analyze views; Analyze trials; Analyze conversions; Analyze subscriptions | 2027.1 | 2027.1.1 | Not specified (implied: Analytics Service) |
| 16.02 Product Analytics | 16.02.02 Product Usage | Analyze usage; Analyze feature adoption; Analyze churn | 2027.1 | 2027.1.1 | Not specified (implied: Analytics Service) |
| 16.03 Operational Analytics | 16.03.01 Operations | Analyze incidents; Analyze SLA; Analyze provisioning time; Analyze service health | 2027.1 | 2027.1.1 | Not specified (implied: Analytics Service) |
| 16.04 Business Analytics | 16.04.01 Financial KPIs | Calculate revenue; Calculate ARR; Calculate MRR; Calculate marketplace GMV | 2027.1 | 2027.1.1 | Not specified (implied: API-021 GET /v1/analytics; Analytics Service) |
| 16.04 Business Analytics | 16.04.02 Growth KPIs | Calculate acquisition; Calculate churn; Calculate conversion | 2027.1 | 2027.1.1 | Not specified (implied: API-021 GET /v1/analytics; Analytics Service) |
| 16.05 Data Platform | 16.05.01 Data Ingestion | Ingest operational data; Ingest event data; Validate data | 2027.1 | 2027.1.1 | Not specified (implied: Analytics Service) |
| 16.05 Data Platform | 16.05.02 Data Management | Transform data; Catalog data; Manage lineage; Manage data quality | 2027.1 | 2027.1.1 | Not specified (implied: Analytics Service) |


## 5.2 Thittam: Macro Planner

| Capability | Primary purpose | Features | Requirement | PI | Sprint | Deliverable |
|---|---|---|---|---|---|---|
| #1 Organization & Identity | Organization, divisions, units, teams, users, roles | Organization creation, Organization profile, Organization hierarchy, Division management, Business-unit management, Department management, Location management, Cost-center management, , User provisioning, Authentication, Authorization, Roles, Permissions, Groups, SSO | Not specified | 2026.3 | 2026.3.3 | Not specified |
| #2 Planning & Portfolio | Create/manage plans and planning hierarchy | Create plan, Plan types, Plan hierarchy, Plan versioning, Plan lifecycle, Plan ownership, Plan status, , Hierarchical planning, Parent, child plans, Cross-plan relationships, Roll-up planning, Cascading targets, Cascading status, , Annual planning, Quarterly planning, Monthly planning, Weekly planning, Fiscal calendars, Custom periods | Not specified | 2026.3 | 2026.3.3 | Not specified |
| #3 Work Management | Tasks, activities, assignments, status, priorities | Tasks, Subtasks, Activities, Assignments, Priority, Status, Due dates, Tags, Checklists, , Kanban, Planner board, List, Grid, Calendar, Timeline, Gantt | Not specified | 2026.3 | 2026.3.3 | Not specified |
| #4 Schedule & Dependency | Dates, milestones, dependencies, critical path | Milestones, Gates, Deliverables, Approval points, , Start, end dates, Duration, Calendar, Working days, Baselines, Forecast dates | Not specified | 2026.3 | 2026.3.3 | Not specified |
| #5 Resource Management | People, teams, capacity, allocation | Team creation, Team membership, Team hierarchy, Team roles, Team responsibilities, Team capacity, , Available capacity, Planned capacity, Allocated capacity, Utilization, Over-allocation | Not specified | 2026.3 | 2026.3.3 | Not specified |
| #6 Goal & KPI Management | Objectives, KPIs, targets, actuals | KPI definition, KPI target, Actual, Forecast, Threshold, Variance, Trend | Not specified | 2026.4 | 2026.4.2 | Not specified |
| #7 Template Management | Business/project/production/etc. plan templates | Business Plan, Sales Plan, Marketing Plan, HR Plan, Finance Plan, Production Plan, Purchase Plan, Delivery Plan, Project Plan, Product Plan, Training Plan, Strategic Plan, Operational Plan, Capacity Plan, Resource Plan, Quality Plan, Risk Plan, Compliance Plan | Not specified | 2026.4 | 2026.4.3 | Not specified |
| #8 Collaboration | Comments, discussions, notifications, documents | Comments, Mentions, Discussions, Notifications, Activity feed, @user, @team | Not specified | 2026.4 | 2026.4.2 | Not specified |
| #9 Workflow & Automation | Rules, approvals, triggers, automated actions | Event engine, Rules engine, Workflow engine, Approval engine, Notification engine, Scheduled jobs, Automation actions | Not specified | 2026.4 | 2027.4.1 ⚠ C1 | Not specified |
| #10 Analytics & Reporting | Dashboards, reports, progress, variance | Gantt chart, Critical path, Baseline, Actual vs planned, Schedule variance | Not specified | 2026.4 | 2027.4.1 ⚠ C1 | Not specified |
| #11 Integration & API | ERP, CRM, ALM, Agile, HR, finance, external SaaS | Not specified | Not specified | 2027.1 | 2027.1.1 | Not specified |
| #12 Marketplace & Extensions | Apps, connectors, templates, plugins | Not specified | Not specified | 2027.1 | 2027.1.3 | Not specified |
| #13 Administration & Configuration | Tenant/platform configuration | Not specified | Not specified | 2027.1 | 2027.1.1 | Not specified |
| #14 Localization | Language, region, timezone, currency, calendar | Not specified | Not specified | 2027.1 | 2027.1.2 | Not specified |
| #15 Platform Operations | Security, audit, monitoring, billing, subscription | Not specified | Not specified | 2027.1 | 2027.1.3 | Not specified |

## 5.3 Thittam: Agile Planner

| Capability | Features | Requirement | PI | Sprint | Deliverable |
|---|---|---|---|---|---|
| AP-C01 Organization & Tenant Management | Multi-Tenant Organization, User Management, Role & Permission Management | Not specified | 2026.3 | 2026.3.3 | Not specified |
| AP-C02 Portfolio / Program Management | Portfolio, Program, Goal Management | Not specified | 2026.3 | 2026.3.3 | Not specified |
| AP-C03 Application / Product Management | Application, Product Registry, Product Planning | Not specified | 2026.3 | 2026.3.3 | Not specified |
| AP-C04 Capability & Feature Management | Capability Management, Feature Management | Not specified | 2026.3 | 2026.3.3 | Not specified |
| AP-C05 Function & Backlog Management | Function Management | Not specified | 2026.3 | 2026.3.3 | Not specified |
| AP-C06 Agile Planning & Sprint Management | Sprint Management | Not specified | 2026.3 | 2026.3.3 | Not specified |
| AP-C07 Team & Resource Management | Teams | Not specified | 2026.3 | 2026.3.3 | Not specified |
| AP-C08 Board Management | BOARD | Not specified | 2026.3 | 2026.3.3 | Not specified |
| AP-C09 Workflow & State Management | Configurable workflow engine | Not specified | 2026.3 | 2026.3.3 | Not specified |
| AP-C10 Work Assignment & Collaboration | Assign work, Reassign work, Followers, Comments, Mentions, Attachments, Checklist, Activity history, Notifications, Work log, Time tracking, Approval, Escalation | Not specified | 2026.3 | 2026.3.3 | Not specified |
| AP-C11 Progress & Status Management | Not specified | Not specified | 2026.4 | 2026.4.2 | Not specified |
| AP-C12 Dependency & Risk Management | Work dependency, Feature dependency, Team dependency, Application dependency, External dependency, Blocking relationship, Risk, Issue, Assumption, Decision, Escalation | Not specified | 2026.4 | 2026.4.3 | Not specified |
| AP-C13 Metrics, Dashboards & Reporting | Not specified | Not specified | 2026.4 | 2026.4.2 | Not specified |
| AP-C14 ALM Integration & Traceability | Not specified | Not specified | 2026.4 | 2027.4.1 ⚠ C1 | Not specified |
| AP-C15 MACRO PLANNER Integration | Not specified | Not specified | 2026.4 | 2027.4.1 ⚠ C1 | Not specified |
| AP-C16 Automation & Notifications | Not specified | Not specified | 2027.1 | 2027.1.1 | Not specified |
| AP-C17 Administration, Configuration & Security | Not specified | Not specified | 2027.1 | 2027.1.2 | Not specified |
| AP-C18 Marketplace / Integration Platform | Not specified | Not specified | 2027.1 | 2027.1.3 | Not specified |

## 5.4 Thiran: SW Life Cycle

| Capability | Features | Requirement | PI | Sprint | Deliverable |
|---|---|---|---|---|---|
| SWLC-CAP-01 Platform & Tenant Management | Not specified | Not specified | 2026.3 | 2026.3.3 | Not specified |
| SWLC-CAP-02 Organization & Project Management | Not specified | Not specified | 2026.3 | 2026.3.3 | Not specified |
| SWLC-CAP-03 Lifecycle / Work Item Management | Not specified | Not specified | 2026.3 | 2026.3.3 | Not specified |
| SWLC-CAP-04 Requirements Management | Not specified | Not specified | 2026.3 | 2026.3.3 | Not specified |
| SWLC-CAP-05 Architecture & Design Management | Not specified | Not specified | 2026.3 | 2026.3.3 | Not specified |
| SWLC-CAP-06 API & Interface Management | Not specified | Not specified | 2026.3 | 2026.3.3 | Not specified |
| SWLC-CAP-07 Software Configuration / Code Management | Not specified | Not specified | 2026.3 | 2026.3.3 | Not specified |
| SWLC-CAP-08 Test Management | Not specified | Not specified | 2026.3 | 2026.3.3 | Not specified |
| SWLC-CAP-09 Defect & Issue Management | Not specified | Not specified | 2026.3 | 2026.3.3 | Not specified |
| SWLC-CAP-10 Traceability & Impact Analysis | Not specified | Not specified | 2026.3 | 2026.3.3 | Not specified |
| SWLC-CAP-11 Workflow & Approval Management | Not specified | Not specified | 2026.4 | 2026.4.1 | Not specified |
| SWLC-CAP-12 Change & Configuration Management | Not specified | Not specified | 2026.4 | 2026.4.2 | Not specified |
| SWLC-CAP-13 Release & Deployment Management | Not specified | Not specified | 2026.4 | 2026.4.2 | Not specified |
| SWLC-CAP-14 Documentation Management | Not specified | Not specified | 2026.4 | 2026.4.3 | Not specified |
| SWLC-CAP-15 Risk & Compliance Management | Not specified | Not specified | 2026.4 | 2026.4.3 | Not specified |
| SWLC-CAP-16 Reporting & Analytics | Not specified | Not specified | 2027.1 | 2027.1.1 | Not specified |
| SWLC-CAP-17 Collaboration & Review | Not specified | Not specified | 2027.1 | 2027.1.1 | Not specified |
| SWLC-CAP-18 AI Engineering Platform | Not specified | Not specified | 2027.1 | 2027.1.2 | Not specified |
| SWLC-CAP-19 Automation Platform | Not specified | Not specified | 2027.1 | 2027.1.2 | Not specified |
| SWLC-CAP-20 Integration & Marketplace | Not specified | Not specified | 2027.1 | 2027.1.3 | Not specified |
| SWLC-CAP-21 Templates & Methodologies | Not specified | Not specified | 2027.1 | 2027.1.3 | Not specified |
| SWLC-CAP-22 Administration & Governance | Not specified | Not specified | 2027.1 | 2027.1.3 | Not specified |
| SWLC-CAP-23 Localization & Regionalization | Not specified | Not specified | 2027.1 | 2027.1.3 | Not specified |

## 5.5 Timeline (application level)
```
PI      2026.3        2026.4                      2027.1                                   2027.2
Sprint  .3            .1       .2        .3       .1              .2          .3           .1      .2
EIS     01 Portal     02 Cat   05 Tenant 07 Subs  09 Order        04 AI       03 Market    14 Part 15 Gov
        06 IAM                           08 Bill  10 Service                  11 Training
                                                  13 Integration              12 Support
                                                  16 Analytics
MacroP  #1-#5                  #6 #8     #7       #11 #13         #14         #12 #15
AgileP  C01-C10                C11 C13   C12      C16             C17         C18
SWLC    CAP-01..10    CAP-11   CAP-12,13 CAP-14,15 CAP-16,17      CAP-18,19   CAP-20..23
Unclear sprint 2027.4.1 (PI stated 2026.4): Macro Planner #9, #10; Agile Planner AP-C14, AP-C15   ⚠ C1
```

---

# 6. Final consolidated analysis

## 6.1 What the overall project is
**eVyoog Enterprise Intelligence Suite (EIS)** is a cloud **PaaS** built as an **AWS-like SaaS marketplace platform**. It is multilingual, multi-regional, multi-tenant and AI-native, and it hosts eVyoog's own **SaaS products** (Thittam, Thiran, Tharav, Valam, Varthan, Yukth) and, later, partner products.

Customers:
- discover and learn about offerings (catalog, training)
- are guided by AI agents
- self-subscribe and pay
- manage their organization, users, roles and privileges
- consume provisioned services
- get AI-first support

The platform is structured as **16 applications → 69 capabilities → 114 features → 444 functions**, with shared cross-cutting services and a global control plane / regional service plane split ([PO], [CG], [WB]).

## 6.2 What each application does
| ID | Application | What it does (source text) | Capabilities | Features / functions ([WB]) | Sprint ([PO]) |
|----|-------------|----------------------------|--------------|-----------------------------|---------------|

| 01 | Experience & Customer Portal | Customer-facing web/mobile experience | Portal Experience; Customer Dashboard; Global Search; Notifications & Communications | 7 / 26 | 2026.3.3 |
| 02 | Product & Catalog Management | Products, solutions, services, plans and content | Product Management; Offering Management; Plan Management; Product Content; Localization | 10 / 35 | 2026.4.1 |
| 03 | Marketplace | Discovery, evaluation, comparison and checkout | Product Discovery; Product Evaluation; Marketplace Checkout; Reviews & Ratings | 7 / 27 | 2027.1.3 |
| 04 | AI Advisor & Agent Platform | AI-guided selling, technical assistance and support | AI Product Advisor; AI Sales Agent; AI Technical Advisor; AI Support Agent; AI Agent Orchestration | 6 / 31 | 2027.1.2 |
| 05 | Customer / Tenant Management | Organizations, tenants, users and projects | Organization Management; Tenant Management; User Management; Group & Project Management | 6 / 24 | 2026.4.2 |
| 06 | Identity & Access Management | Authentication, authorization, roles and policies | Authentication; Authorization; Privileged Access; Identity Federation | 6 / 22 | 2026.3.3 |
| 07 | Subscription & Entitlement Management | Subscriptions, licenses, quotas and entitlements | Subscription Management; Entitlement Management; License & Quota Management; Renewal & Lifecycle | 8 / 30 | 2026.4.3 |
| 08 | Billing & Payments | Pricing, billing, invoices, taxes and payments | Pricing; Billing; Payment; Financial Documents; Tax & Currency | 9 / 33 | 2026.4.3 |
| 09 | Order & Provisioning Management | Orders, provisioning and workflow orchestration | Order Management; Provisioning; Workflow Orchestration; Approval Management | 5 / 26 | 2027.1.1 |
| 10 | Service & Resource Management | Service instances and cloud/platform resources | Service Management; Resource Management; Configuration Management; Monitoring & Health | 5 / 23 | 2027.1.1 |
| 11 | Training & Knowledge Management | Documentation, courses, labs and certifications | Knowledge Base; Learning Management; Training Delivery; Certification | 7 / 26 | 2027.1.3 |
| 12 | Support & Service Management | AI/human support, incidents, requests and SLAs | Support; AI Support; SLA Management; Incident & Problem Management | 7 / 28 | 2027.1.3 |
| 13 | Integration & API Platform | APIs, connectors, events and webhooks | API Management; Integration Hub; Event Platform; Webhooks | 6 / 25 | 2027.1.1 |
| 14 | Partner & Provider Management | Providers, publishers, onboarding and revenue sharing | Provider Onboarding; Publisher Management; Revenue Sharing; Partner Operations | 7 / 23 | 2027.2.1 |
| 15 | Administration & Governance | Platform configuration, policies, audit and compliance | Platform Administration; Policy Management; Audit; Compliance; Regional Operations | 9 / 33 | 2027.2.2 |
| 16 | Analytics & Data Platform | Customer, product, operational and business analytics | Customer Analytics; Product Analytics; Operational Analytics; Business Analytics; Data Platform | 9 / 32 | 2027.1.1 |

**The SaaS products hosted on EIS ([PO]):**
- **Thittam:** planning. **Macro Planner** covers organization-to-individual planning, portfolio, work, schedule, resources, KPIs, templates, collaboration and automation. **Agile Planner** covers the portfolio → product → capability → feature → function → backlog → sprint hierarchy, with teams, boards, workflow, dependencies and risk.
- **Thiran:** execution of projects, products and production orders. **SW Life Cycle** covers requirements, design, API, code, test, defects, traceability, release, and an AI engineering platform. **Product Life Cycle Management, Production Management and Service Management** are named only.
- **Tharav, Valam, Varthan, Yukth:** named only.

## 6.3 What is planned in each PI

**PI 2026.3**
- 2026.3.3: EIS 01 Experience & Customer Portal: Portal Experience, Customer Dashboard, Global Search, Notifications & Communications
- 2026.3.3: EIS 06 Identity & Access Management: Authentication, Authorization, Privileged Access, Identity Federation
- 2026.3.3: SW Life Cycle: SWLC-CAP-01 Platform & Tenant Management
- 2026.3.3: SW Life Cycle: SWLC-CAP-02 Organization & Project Management
- 2026.3.3: SW Life Cycle: SWLC-CAP-03 Lifecycle / Work Item Management
- 2026.3.3: SW Life Cycle: SWLC-CAP-04 Requirements Management
- 2026.3.3: SW Life Cycle: SWLC-CAP-05 Architecture & Design Management
- 2026.3.3: SW Life Cycle: SWLC-CAP-06 API & Interface Management
- 2026.3.3: SW Life Cycle: SWLC-CAP-07 Software Configuration / Code Management
- 2026.3.3: SW Life Cycle: SWLC-CAP-08 Test Management
- 2026.3.3: SW Life Cycle: SWLC-CAP-09 Defect & Issue Management
- 2026.3.3: SW Life Cycle: SWLC-CAP-10 Traceability & Impact Analysis
- 2026.3.3: Macro Planner: #1 Organization & Identity
- 2026.3.3: Macro Planner: #2 Planning & Portfolio
- 2026.3.3: Macro Planner: #3 Work Management
- 2026.3.3: Macro Planner: #4 Schedule & Dependency
- 2026.3.3: Macro Planner: #5 Resource Management
- 2026.3.3: Agile Planner: AP-C01 Organization & Tenant Management
- 2026.3.3: Agile Planner: AP-C02 Portfolio / Program Management
- 2026.3.3: Agile Planner: AP-C03 Application / Product Management
- 2026.3.3: Agile Planner: AP-C04 Capability & Feature Management
- 2026.3.3: Agile Planner: AP-C05 Function & Backlog Management
- 2026.3.3: Agile Planner: AP-C06 Agile Planning & Sprint Management
- 2026.3.3: Agile Planner: AP-C07 Team & Resource Management
- 2026.3.3: Agile Planner: AP-C08 Board Management
- 2026.3.3: Agile Planner: AP-C09 Workflow & State Management
- 2026.3.3: Agile Planner: AP-C10 Work Assignment & Collaboration

**PI 2026.4**
- 2026.4.1: EIS 02 Product & Catalog Management: Product Management, Offering Management, Plan Management, Product Content, Localization
- 2026.4.1: SW Life Cycle: SWLC-CAP-11 Workflow & Approval Management
- 2026.4.2: EIS 05 Customer / Tenant Management: Organization Management, Tenant Management, User Management, Group & Project Management
- 2026.4.2: SW Life Cycle: SWLC-CAP-12 Change & Configuration Management
- 2026.4.2: SW Life Cycle: SWLC-CAP-13 Release & Deployment Management
- 2026.4.2: Macro Planner: #6 Goal & KPI Management
- 2026.4.2: Macro Planner: #8 Collaboration
- 2026.4.2: Agile Planner: AP-C11 Progress & Status Management
- 2026.4.2: Agile Planner: AP-C13 Metrics, Dashboards & Reporting
- 2026.4.3: EIS 08 Billing & Payments: Pricing, Billing, Payment, Financial Documents, Tax & Currency
- 2026.4.3: EIS 07 Subscription & Entitlement Management: Subscription Management, Entitlement Management, License & Quota Management, Renewal & Lifecycle
- 2026.4.3: SW Life Cycle: SWLC-CAP-14 Documentation Management
- 2026.4.3: SW Life Cycle: SWLC-CAP-15 Risk & Compliance Management
- 2026.4.3: Macro Planner: #7 Template Management
- 2026.4.3: Agile Planner: AP-C12 Dependency & Risk Management
- 2027.4.1 ⚠ C1: Macro Planner: #10 Analytics & Reporting
- 2027.4.1 ⚠ C1: Macro Planner: #9 Workflow & Automation
- 2027.4.1 ⚠ C1: Agile Planner: AP-C14 ALM Integration & Traceability
- 2027.4.1 ⚠ C1: Agile Planner: AP-C15 MACRO PLANNER Integration

**PI 2027.1**
- 2027.1.1: EIS 16 Analytics & Data Platform: Customer Analytics, Product Analytics, Operational Analytics, Business Analytics, Data Platform
- 2027.1.1: EIS 13 Integration & API Platform: API Management, Integration Hub, Event Platform, Webhooks
- 2027.1.1: EIS 09 Order & Provisioning Management: Order Management, Provisioning, Workflow Orchestration, Approval Management
- 2027.1.1: EIS 10 Service & Resource Management: Service Management, Resource Management, Configuration Management, Monitoring & Health
- 2027.1.1: SW Life Cycle: SWLC-CAP-16 Reporting & Analytics
- 2027.1.1: SW Life Cycle: SWLC-CAP-17 Collaboration & Review
- 2027.1.1: Macro Planner: #11 Integration & API
- 2027.1.1: Macro Planner: #13 Administration & Configuration
- 2027.1.1: Agile Planner: AP-C16 Automation & Notifications
- 2027.1.2: EIS 04 AI Advisor & Agent Platform: AI Product Advisor, AI Sales Agent, AI Technical Advisor, AI Support Agent, AI Agent Orchestration
- 2027.1.2: SW Life Cycle: SWLC-CAP-18 AI Engineering Platform
- 2027.1.2: SW Life Cycle: SWLC-CAP-19 Automation Platform
- 2027.1.2: Macro Planner: #14 Localization
- 2027.1.2: Agile Planner: AP-C17 Administration, Configuration & Security
- 2027.1.3: EIS 03 Marketplace: Product Discovery, Product Evaluation, Marketplace Checkout, Reviews & Ratings
- 2027.1.3: EIS 12 Support & Service Management: Support, AI Support, SLA Management, Incident & Problem Management
- 2027.1.3: EIS 11 Training & Knowledge Management: Knowledge Base, Learning Management, Training Delivery, Certification
- 2027.1.3: SW Life Cycle: SWLC-CAP-20 Integration & Marketplace
- 2027.1.3: SW Life Cycle: SWLC-CAP-21 Templates & Methodologies
- 2027.1.3: SW Life Cycle: SWLC-CAP-22 Administration & Governance
- 2027.1.3: SW Life Cycle: SWLC-CAP-23 Localization & Regionalization
- 2027.1.3: Macro Planner: #12 Marketplace & Extensions
- 2027.1.3: Macro Planner: #15 Platform Operations
- 2027.1.3: Agile Planner: AP-C18 Marketplace / Integration Platform

**PI 2027.2**
- 2027.2.1: EIS 14 Partner & Provider Management: Provider Onboarding, Publisher Management, Revenue Sharing, Partner Operations
- 2027.2.2: EIS 15 Administration & Governance: Platform Administration, Policy Management, Audit, Compliance, Regional Operations


## 6.4 What is planned in each sprint
See **4.2** for the sprint-by-sprint list. See **Appendix B** for every EIS function under the sprint of its application. Every sprint has these gaps: **expected deliverables, acceptance criteria, owners and capacity are Not specified**.

## 6.5 How the documents and sheets connect
```
[CG] prompt (vision) ───────────────► [PO] Vision (same text)     ► [WB:README] Purpose
[CG] 16 apps / 50 capabilities ─────► [WB:Application Summary] = [PO] Table 1 (69 capabilities)
[CG] features + functions ──────────► [WB:Product Breakdown] ─┬─► [WB:Capabilities] (69)
                                                               ├─► [WB:Features] (114)
                                                               └─► [WB:Functions] (444) ─► [WB:Traceability] (444)
[CG] domain model ──────────────────► [WB:Core Domain Model] (21) ─► [WB:Data Entities] (25)
[CG] "API / Service / Event" levels ► [WB:APIs] (22) · [WB:Microservices] (22) · [WB:Events] (20)
                                        all three joined per function in [WB:Traceability]
[CG] customer journey (16 steps) ───► [WB:User Journeys] (12) ─► [WB:Traceability] Journey
[CG] layered diagrams ──────────────► [WB:Architecture Layers] (10)
[CG] principles, multi-region, AI ──► [WB:Architecture Principles] (12) · [WB:Non-Functional Requirements] (14)
[CG] MVP Phase 1/2/3 ───────────────► [WB:Roadmap] (Phase 1/2/3, different scope: C2)
[WB] applications ──────────────────► [PO] Table 2: PI + Sprint per application (C3 order conflicts)
[PO] Thittam / Thiran products ─────► no counterpart in [CG] or [WB]
[CG] golden path, Macro Planner / ALM ► [PO] Macro Planner, Agile Planner, SW Life Cycle (C9)
```

## 6.6 Decisions needed before writing sprint documents
1. **C1:** confirm sprint 2027.4.1 against PI 2026.4 (Macro Planner #9 and #10, Agile Planner AP-C14 and AP-C15).
2. **C2 and C3:** confirm the MVP and the order of applications. [PO] sprints and [CG]/[WB] phases disagree.
3. **Sprint scope:** decide which capabilities and features of an application go into its [PO] sprint, and the sprint length and dates.
4. **C4 to C6, C12 to C14:** clean up the [WB] MVP, Priority, AI, Journey, Phase, microservice and API mappings.
5. **C8:** decide whether Thittam and Thiran (starting 2026.3.3) wait for EIS tenancy and subscription (2026.4.2 and 2026.4.3) or use their own.
6. **C9:** decide the ALM system of record: Azure DevOps and Project Online ([CG]), or in-house Agile Planner and SW Life Cycle ([PO]).
7. **Business rules and acceptance criteria:** none exist in any document, and the repository's `CLAUDE.md` requires them before implementation.

---

# Appendix A: [CG] application breakdown (verbatim lists)

### Application 1: Experience & Customer Portal
> This is the equivalent of the customer-facing portion of an AWS/Azure-style console.
- **1.1 Portal Experience**. **Features:** Web portal; Responsive UI; Mobile application; Personalized dashboard; Multi-language UI; Multi-region UI; Accessibility. **Functions:** Login; Logout; Language selection; Region selection; Theme selection; Dashboard customization; Notification display; Search; Navigation; Favorites; Recently used products
- **1.2 Customer Dashboard**. **Features:** Business overview; Subscription overview; Usage overview; Billing overview; Service health; Support status. **Functions:** Display active subscriptions; Display spending; Display invoices; Display service status; Display open support tickets; Display consumption; Display alerts; Display recommendations
- **1.3 Global Search**. **Features:** Product search; Documentation search; Marketplace search; Support search; Semantic AI search. **Functions:** Keyword search; Filter; Sort; Faceted search; Semantic search; Search suggestions; Search history

### Application 2: Product & Catalog Management
> This is one of the most important core applications. It allows you to list your: Products SaaS applications APIs Solutions Professional services Managed services Training Consulting Infrastructure services AI services Third-party products
- **2.1 Product Management**. **Features:** Product definition; Product hierarchy; Product lifecycle; Product versions; Product variants. **Functions:** Create product; Update product; Publish product; Unpublish product; Version product; Retire product; Clone product; Archive product
- **2.2 Offering Management**. **Features:** Product offering; Solution offering; Service offering; Bundle; Package. **Functions:** Create offering; Associate products; Create bundles; Define dependencies; Define prerequisites; Define compatibility; Define availability
- **2.3 Plan Management**. **Features:** Free plan; Trial plan; Basic; Professional; Enterprise; Usage-based; Subscription-based; Consumption-based; Hybrid pricing. **Functions:** Create plan; Define price; Define billing frequency; Define usage limits; Define quotas; Define included features; Define overage charges; Define discounts
- **2.4 Product Content**. **Features:** Product description; Images; Videos; Datasheets; Documentation; FAQs; Case studies; Training materials. **Functions:** Upload content; Version content; Localize content; Publish content; Associate content with products

### Application 3: Marketplace
> This is the AWS Marketplace-like component.
- **3.1 Product Discovery**. **Features:** Categories; Search; Filters; Recommendations; Featured products; Popular products. **Functions:** Browse catalog; Search products; Filter products; Compare products; View product details; View reviews; View pricing
- **3.2 Product Evaluation**. **Features:** Product comparison; Trial; Demo; Sandbox; Documentation; Training. **Functions:** Start trial; Request demo; Launch sandbox; Compare products; View prerequisites; Estimate cost
- **3.3 Marketplace Checkout**. **Functions:** Select product; Select plan; Select quantity; Configure options; Apply discount; Calculate price; Accept terms; Submit order; Make payment

### Application 4: AI Advisor & Agent Platform
> I would make this a platform-level capability, rather than building individual AI agents independently. You can then build multiple specialized agents on top.
- **4.1 AI Product Advisor**. **Features:** Requirement discovery; Product recommendation; Solution recommendation; Product comparison. **Functions:** Ask customer questions; Understand business requirements; Identify constraints; Recommend products; Recommend bundles; Explain recommendations; Compare alternatives; Estimate cost
- **4.2 AI Sales Agent**. **Functions:** Lead qualification; Product explanation; Pricing explanation; Proposal generation; Quote generation; Upselling; Cross-selling
- **4.3 AI Technical Advisor**. **Functions:** Architecture recommendations; Configuration assistance; Troubleshooting; Deployment guidance; Best-practice recommendations; Error explanation
- **4.4 AI Support Agent**. **Functions:** Understand support request; Search knowledge base; Diagnose issue; Recommend resolution; Execute permitted remediation; Create support ticket; Escalate to human; Track resolution
- **4.5 AI Agent Orchestration**. **Functions:** Agent registration; Agent routing; Tool selection; Context management; Conversation management; Memory; Guardrails; Human escalation; Agent authorization; Agent audit; This becomes extremely important later.

### Application 5: Customer / Tenant Management
> This should implement your multi-tenant SaaS model.
- **5.1 Organization Management**. **Functions:** Create organization; Update organization; Suspend organization; Activate organization; Delete organization; Manage organization profile
- **5.2 Tenant Management**. **Features:** Tenant; Sub-tenant; Business unit; Department; Project. **Functions:** Create tenant; Configure tenant; Configure tenant isolation; Assign tenant region; Configure tenant policies
- **5.3 User Management**. **Functions:** Invite user; Create user; Activate user; Suspend user; Remove user; Assign roles; Assign groups

### Application 6: Identity & Access Management
> This is effectively your own IAM system.
- **6.1 Authentication**. **Functions:** Username/password; MFA; SSO; OAuth/OIDC; SAML; Social login; Password reset; Session management
- **6.2 Authorization**. **Features:** RBAC; ABAC; Resource-based permissions; Policy-based authorization. **Functions:** Create role; Assign role; Define permission; Evaluate permission; Create policy; Evaluate policy
- **6.3 Privileged Access**. **Functions:** Admin access; Temporary access; Approval-based access; Delegated administration; Privilege escalation; Access review

### Application 7: Subscription & Entitlement Management
> This is another core platform application. Think: What did the customer buy? What are they allowed to use?
- **7.1 Subscription Management**. **Functions:** Create subscription; Activate subscription; Suspend subscription; Upgrade; Downgrade; Renew; Cancel; Reactivate
- **7.2 Entitlement Management**. **Features:** Feature entitlement; License entitlement; Usage entitlement; API entitlement; Resource entitlement. **Functions:** Grant entitlement; Revoke entitlement; Validate entitlement; Check quota; Check license; Check expiration

### Application 8: Billing & Payments
- **8.1 Pricing**. **Functions:** Define price; Usage pricing; Tier pricing; Volume pricing; Subscription pricing; Promotional pricing; Customer-specific pricing
- **8.2 Billing**. **Functions:** Generate bill; Calculate usage; Calculate taxes; Apply discounts; Generate invoice; Credit invoice; Debit invoice; Handle adjustments
- **8.3 Payment**. **Functions:** Add payment method; Authorize payment; Capture payment; Refund; Payment retry; Payment failure handling; Payment reconciliation
- **8.4 Financial Documents**. **Functions:** Generate invoice; Generate receipt; Generate credit note; Download invoice; Email invoice; View payment history

### Application 9: Order & Provisioning Management
> This application turns: "I purchased Product X" into: "Product X is provisioned and available to the customer."
- **9.1 Order Management**. **Functions:** Create order; Validate order; Price order; Submit order; Approve order; Cancel order; Modify order; Track order
- **9.2 Provisioning**. **Functions:** Provision service; Configure service; Create resources; Assign resources; Activate service; Suspend service; Deprovision service
- **9.3 Workflow Orchestration**. **Functions:** Trigger workflow; Execute workflow; Parallel processing; Sequential processing; Retry; Rollback; Compensation; Approval; Human intervention

### Application 10: Service & Resource Management
> This is closer to the AWS/Azure resource management experience.
- **10.1 Service Management**. **Functions:** Create service instance; Configure service; Start service; Stop service; Restart service; Scale service; Delete service
- **10.2 Resource Management**. **Functions:** Create resource; Update resource; Scale resource; Monitor resource; Delete resource. **Notes:** Depending on your PaaS offerings:; Compute; Storage; Database; API; Container; Kubernetes; AI/ML; Networking; Messaging

### Application 11: Training & Knowledge Management
> This directly supports your requirement that customers should be able to understand offerings before purchasing.
- **11.1 Knowledge Base**. **Functions:** Create article; Edit article; Publish article; Search article; Localize article; Version article
- **11.2 Learning Management**. **Features:** Courses; Learning paths; Videos; Tutorials; Labs; Assessments; Certifications. **Functions:** Enroll user; Start course; Track progress; Complete course; Take assessment; Issue certificate

### Application 12: Support & Service Management
- **12.1 Support**. **Functions:** Create ticket; Categorize ticket; Prioritize ticket; Assign ticket; Escalate ticket; Resolve ticket; Close ticket
- **12.2 AI Support**. **Functions:** Conversational support; Knowledge search; Problem diagnosis; Resolution recommendation; Automated remediation; Ticket creation; Human escalation
- **12.3 SLA Management**. **Functions:** Define SLA; Calculate SLA; Monitor SLA; SLA warning; SLA breach; Escalation

### Application 13: Integration & API Platform
> This is critical if your vision is to become a platform rather than merely a SaaS storefront.
- **13.1 API Management**. **Functions:** API registration; API publishing; API versioning; API authentication; API authorization; Rate limiting; API analytics
- **13.2 Integration Hub**. **Functions:** Create connector; Authenticate connector; Synchronize data; Transform data; Trigger workflow; Handle errors. **Notes:** Connect to:; CRM; ERP; HR; Finance; ITSM; Payment providers; Identity providers; Cloud providers; Communication platforms
- **13.3 Event Platform**. **Functions:** Publish event; Subscribe to event; Event routing; Event transformation; Event retry; Event replay
- **13.4 Webhooks**. **Functions:** Register webhook; Trigger webhook; Authenticate webhook; Retry webhook; Monitor webhook

### Application 14: Partner & Provider Management
> If your marketplace eventually allows other companies to list their products, this becomes essential.
- **14.1 Provider Onboarding**. **Functions:** Provider registration; Verification; Contract management; Approval; Activation
- **14.2 Publisher Management**. **Functions:** Create publisher; Manage publisher products; Manage publisher pricing; Manage publisher content; Manage publisher analytics
- **14.3 Revenue Sharing**. **Functions:** Define commission; Calculate revenue share; Calculate partner payout; Generate partner statement; Reconcile payments

### Application 15: Administration & Governance
- **15.1 Platform Administration**. **Functions:** Configure platform; Configure regions; Configure languages; Configure currencies; Configure tax rules; Configure feature flags
- **15.2 Policy Management**. **Functions:** Create policy; Assign policy; Evaluate policy; Enforce policy; Policy exception; Policy audit
- **15.3 Audit**. **Functions:** Record activity; Record login; Record configuration change; Record financial transaction; Record administrative action; Search audit logs; Export audit logs

### Application 16: Analytics & Data Platform
- **16.1 Customer Analytics**. **Functions:** Customer usage; Customer spending; Customer adoption; Customer engagement; Customer health
- **16.2 Product Analytics**. **Functions:** Product views; Product conversions; Trial conversion; Subscription conversion; Churn; Usage
- **16.3 Business Analytics**. **Functions:** Revenue; ARR; MRR; Customer acquisition; Customer lifetime value; Churn; Marketplace GMV; Partner revenue


# Appendix B: all 444 workbook functions, grouped by sprint
Source: [WB:Functions] and [WB:Traceability]. The sprint is inherited from the application ([PO] Table 2). Columns: Function ID, Function, MVP, AI, Actor, Suggested API, Primary API, Microservice, Entity, Event, Journey, Phase.

## Sprint 2026.3.3
### 01 Experience & Customer Portal
| ID | Capability › Feature › Function | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 01.01.01.01 | Portal Experience › Responsive Web Portal › **Login** | No | No | Customer | /portal-experience/login | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.01.01.02 | Portal Experience › Responsive Web Portal › **Navigate portal** | No | No | Customer | /portal-experience/navigate-portal | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.01.01.03 | Portal Experience › Responsive Web Portal › **Select language** | No | No | Customer | /portal-experience/select-language | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.01.01.04 | Portal Experience › Responsive Web Portal › **Select region** | No | No | Customer | /portal-experience/select-region | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.01.01.05 | Portal Experience › Responsive Web Portal › **Customize preferences** | No | No | Customer | /portal-experience/customize-preferences | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.01.02.01 | Portal Experience › Accessibility › **Configure accessibility preferences** | No | No | Customer | /portal-experience/configure-accessibility-preferences | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.01.02.02 | Portal Experience › Accessibility › **Use keyboard navigation** | No | No | Customer | /portal-experience/use-keyboard-navigation | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.01.02.03 | Portal Experience › Accessibility › **Support screen readers** | No | No | Customer | /portal-experience/support-screen-readers | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.02.01.01 | Customer Dashboard › Business Overview › **View organization summary** | No | No | Customer | /customer-dashboard/view-organization-summary | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.02.01.02 | Customer Dashboard › Business Overview › **View subscriptions** | No | No | Customer | /customer-dashboard/view-subscriptions | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.02.01.03 | Customer Dashboard › Business Overview › **View spending** | No | No | Customer | /customer-dashboard/view-spending | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.02.01.04 | Customer Dashboard › Business Overview › **View usage** | No | No | Customer | /customer-dashboard/view-usage | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.02.02.01 | Customer Dashboard › Service Health › **View service status** | No | No | Customer | /customer-dashboard/view-service-status | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.02.02.02 | Customer Dashboard › Service Health › **View alerts** | No | No | Customer | /customer-dashboard/view-alerts | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.02.02.03 | Customer Dashboard › Service Health › **View incidents** | No | No | Customer | /customer-dashboard/view-incidents | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.03.01.01 | Global Search › Unified Search › **Keyword search** | No | No | Customer | /global-search/keyword-search | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.03.01.02 | Global Search › Unified Search › **Semantic search** | No | No | Customer | /global-search/semantic-search | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.03.01.03 | Global Search › Unified Search › **Filter results** | No | No | Customer | /global-search/filter-results | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.03.01.04 | Global Search › Unified Search › **Sort results** | No | No | Customer | /global-search/sort-results | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.03.01.05 | Global Search › Unified Search › **View search history** | No | No | Customer | /global-search/view-search-history | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.04.01.01 | Notifications & Communications › Notification Center › **View notifications** | No | No | Customer | /notifications-communications/view-notifications | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.04.01.02 | Notifications & Communications › Notification Center › **Mark notification read** | No | No | Customer | /notifications-communications/mark-notification-read | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.04.01.03 | Notifications & Communications › Notification Center › **Configure notification preferences** | No | No | Customer | /notifications-communications/configure-notification-preferences | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.04.02.01 | Notifications & Communications › Outbound Communications › **Send email** | No | No | Customer | /notifications-communications/send-email | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.04.02.02 | Notifications & Communications › Outbound Communications › **Send SMS** | No | No | Customer | /notifications-communications/send-sms | - | - | - | - | UJ-005 Provision Service | Phase 2 |
| 01.04.02.03 | Notifications & Communications › Outbound Communications › **Send in-app notification** | No | No | Customer | /notifications-communications/send-in-app-notification | - | - | - | - | UJ-005 Provision Service | Phase 2 |

### 06 Identity & Access Management
| ID | Capability › Feature › Function | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 06.01.01.01 | Authentication › Credentials › **Sign in** | No | No | Platform Service | /authentication/sign-in | API-001 POST /v1/auth/login | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.01.01.02 | Authentication › Credentials › **Sign out** | No | No | Platform Service | /authentication/sign-out | API-001 POST /v1/auth/login | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.01.01.03 | Authentication › Credentials › **Reset password** | No | No | Platform Service | /authentication/reset-password | API-001 POST /v1/auth/login | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.01.01.04 | Authentication › Credentials › **Manage sessions** | No | No | Platform Service | /authentication/manage-sessions | API-001 POST /v1/auth/login | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.01.02.01 | Authentication › MFA › **Enroll MFA** | No | No | Platform Service | /authentication/enroll-mfa | API-001 POST /v1/auth/login | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.01.02.02 | Authentication › MFA › **Verify MFA** | No | No | Platform Service | /authentication/verify-mfa | API-001 POST /v1/auth/login | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.01.02.03 | Authentication › MFA › **Recover MFA** | No | No | Platform Service | /authentication/recover-mfa | API-001 POST /v1/auth/login | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.02.01.01 | Authorization › RBAC › **Create role** | No | No | Platform Service | /authorization/create-role | API-002 POST /v1/authz/evaluate | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.02.01.02 | Authorization › RBAC › **Define permission** | No | No | Platform Service | /authorization/define-permission | API-002 POST /v1/authz/evaluate | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.02.01.03 | Authorization › RBAC › **Assign role** | No | No | Platform Service | /authorization/assign-role | API-002 POST /v1/authz/evaluate | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.02.01.04 | Authorization › RBAC › **Evaluate permission** | No | No | Platform Service | /authorization/evaluate-permission | API-002 POST /v1/authz/evaluate | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.02.02.01 | Authorization › Policy › **Create policy** | No | No | Platform Service | /authorization/create-policy | API-002 POST /v1/authz/evaluate | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.02.02.02 | Authorization › Policy › **Assign policy** | No | No | Platform Service | /authorization/assign-policy | API-002 POST /v1/authz/evaluate | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.02.02.03 | Authorization › Policy › **Evaluate policy** | No | No | Platform Service | /authorization/evaluate-policy | API-002 POST /v1/authz/evaluate | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.03.01.01 | Privileged Access › Administrative Access › **Request elevated access** | No | No | Platform Service | /privileged-access/request-elevated-access | - | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.03.01.02 | Privileged Access › Administrative Access › **Approve access** | No | No | Platform Service | /privileged-access/approve-access | - | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.03.01.03 | Privileged Access › Administrative Access › **Grant temporary access** | No | No | Platform Service | /privileged-access/grant-temporary-access | - | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.03.01.04 | Privileged Access › Administrative Access › **Revoke access** | No | No | Platform Service | /privileged-access/revoke-access | - | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.04.01.01 | Identity Federation › SSO › **Configure SAML** | No | No | Platform Service | /identity-federation/configure-saml | - | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.04.01.02 | Identity Federation › SSO › **Configure OIDC** | No | No | Platform Service | /identity-federation/configure-oidc | - | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.04.01.03 | Identity Federation › SSO › **Map claims** | No | No | Platform Service | /identity-federation/map-claims | - | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |
| 06.04.01.04 | Identity Federation › SSO › **Test federation** | No | No | Platform Service | /identity-federation/test-federation | - | Identity Service | User | UserCreated | UJ-005 Provision Service | Phase 2 |

## Sprint 2026.4.1
### 02 Product & Catalog Management
| ID | Capability › Feature › Function | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 02.01.01.01 | Product Management › Product Lifecycle › **Create product** | Yes | No | Platform Service | /product-management/create-product | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 1 / MVP |
| 02.01.01.02 | Product Management › Product Lifecycle › **Update product** | Yes | No | Platform Service | /product-management/update-product | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 1 / MVP |
| 02.01.01.03 | Product Management › Product Lifecycle › **Version product** | Yes | No | Platform Service | /product-management/version-product | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 1 / MVP |
| 02.01.01.04 | Product Management › Product Lifecycle › **Publish product** | No | No | Platform Service | /product-management/publish-product | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.01.01.05 | Product Management › Product Lifecycle › **Retire product** | No | No | Platform Service | /product-management/retire-product | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.01.02.01 | Product Management › Product Structure › **Define product hierarchy** | Yes | No | Platform Service | /product-management/define-product-hierarchy | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 1 / MVP |
| 02.01.02.02 | Product Management › Product Structure › **Define variants** | Yes | No | Platform Service | /product-management/define-variants | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 1 / MVP |
| 02.01.02.03 | Product Management › Product Structure › **Define dependencies** | Yes | No | Platform Service | /product-management/define-dependencies | API-004 GET /v1/products | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 1 / MVP |
| 02.02.01.01 | Offering Management › Offering Definition › **Create offering** | No | No | Platform Service | /offering-management/create-offering | API-005 GET /v1/offerings/{id} | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.02.01.02 | Offering Management › Offering Definition › **Bundle products** | No | No | Platform Service | /offering-management/bundle-products | API-005 GET /v1/offerings/{id} | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.02.01.03 | Offering Management › Offering Definition › **Define prerequisites** | No | No | Platform Service | /offering-management/define-prerequisites | API-005 GET /v1/offerings/{id} | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.02.01.04 | Offering Management › Offering Definition › **Define compatibility** | No | No | Platform Service | /offering-management/define-compatibility | API-005 GET /v1/offerings/{id} | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.02.02.01 | Offering Management › Availability › **Define regions** | No | No | Platform Service | /offering-management/define-regions | API-005 GET /v1/offerings/{id} | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.02.02.02 | Offering Management › Availability › **Define channels** | No | No | Platform Service | /offering-management/define-channels | API-005 GET /v1/offerings/{id} | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.02.02.03 | Offering Management › Availability › **Define eligibility** | No | No | Platform Service | /offering-management/define-eligibility | API-005 GET /v1/offerings/{id} | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.03.01.01 | Plan Management › Plan Definition › **Create plan** | No | No | Platform Service | /plan-management/create-plan | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.03.01.02 | Plan Management › Plan Definition › **Define billing frequency** | No | No | Platform Service | /plan-management/define-billing-frequency | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.03.01.03 | Plan Management › Plan Definition › **Define usage limits** | No | No | Platform Service | /plan-management/define-usage-limits | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.03.01.04 | Plan Management › Plan Definition › **Define included features** | No | No | Platform Service | /plan-management/define-included-features | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.03.02.01 | Plan Management › Pricing Models › **Define subscription price** | No | No | Platform Service | /plan-management/define-subscription-price | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.03.02.02 | Plan Management › Pricing Models › **Define usage price** | No | No | Platform Service | /plan-management/define-usage-price | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.03.02.03 | Plan Management › Pricing Models › **Define tier price** | No | No | Platform Service | /plan-management/define-tier-price | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.03.02.04 | Plan Management › Pricing Models › **Define overage charge** | No | No | Platform Service | /plan-management/define-overage-charge | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.04.01.01 | Product Content › Product Documentation › **Upload datasheet** | No | No | Platform Service | /product-content/upload-datasheet | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.04.01.02 | Product Content › Product Documentation › **Publish documentation** | No | No | Platform Service | /product-content/publish-documentation | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.04.01.03 | Product Content › Product Documentation › **Version content** | No | No | Platform Service | /product-content/version-content | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.04.02.01 | Product Content › Rich Media › **Upload images** | No | No | Platform Service | /product-content/upload-images | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.04.02.02 | Product Content › Rich Media › **Upload videos** | No | No | Platform Service | /product-content/upload-videos | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.04.02.03 | Product Content › Rich Media › **Manage case studies** | No | No | Platform Service | /product-content/manage-case-studies | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.05.01.01 | Localization › Content Localization › **Translate product content** | No | No | Platform Service | /localization/translate-product-content | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.05.01.02 | Localization › Content Localization › **Translate documentation** | No | No | Platform Service | /localization/translate-documentation | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.05.01.03 | Localization › Content Localization › **Publish localized content** | No | No | Platform Service | /localization/publish-localized-content | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.05.02.01 | Localization › Regionalization › **Configure currency** | No | No | Platform Service | /localization/configure-currency | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.05.02.02 | Localization › Regionalization › **Configure date/time format** | No | No | Platform Service | /localization/configure-date/time-format | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |
| 02.05.02.03 | Localization › Regionalization › **Configure regional terminology** | No | No | Platform Service | /localization/configure-regional-terminology | - | Catalog Service | Product | ProductPublished | UJ-005 Provision Service | Phase 2 |

## Sprint 2026.4.2
### 05 Customer / Tenant Management
| ID | Capability › Feature › Function | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 05.01.01.01 | Organization Management › Organization Lifecycle › **Create organization** | No | No | Platform Service | /organization-management/create-organization | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.01.01.02 | Organization Management › Organization Lifecycle › **Update organization** | No | No | Platform Service | /organization-management/update-organization | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.01.01.03 | Organization Management › Organization Lifecycle › **Suspend organization** | No | No | Platform Service | /organization-management/suspend-organization | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.01.01.04 | Organization Management › Organization Lifecycle › **Activate organization** | No | No | Platform Service | /organization-management/activate-organization | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.01.01.05 | Organization Management › Organization Lifecycle › **Close organization** | No | No | Platform Service | /organization-management/close-organization | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.02.01.01 | Tenant Management › Tenant Lifecycle › **Create tenant** | No | No | Platform Service | /tenant-management/create-tenant | API-003 POST /v1/tenants | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.02.01.02 | Tenant Management › Tenant Lifecycle › **Configure tenant** | No | No | Platform Service | /tenant-management/configure-tenant | API-003 POST /v1/tenants | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.02.01.03 | Tenant Management › Tenant Lifecycle › **Assign region** | No | No | Platform Service | /tenant-management/assign-region | API-003 POST /v1/tenants | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.02.01.04 | Tenant Management › Tenant Lifecycle › **Configure isolation** | No | No | Platform Service | /tenant-management/configure-isolation | API-003 POST /v1/tenants | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.02.01.05 | Tenant Management › Tenant Lifecycle › **Configure tenant policies** | No | No | Platform Service | /tenant-management/configure-tenant-policies | API-003 POST /v1/tenants | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.03.01.01 | User Management › User Lifecycle › **Invite user** | No | No | Platform Service | /user-management/invite-user | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.03.01.02 | User Management › User Lifecycle › **Create user** | No | No | Platform Service | /user-management/create-user | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.03.01.03 | User Management › User Lifecycle › **Activate user** | No | No | Platform Service | /user-management/activate-user | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.03.01.04 | User Management › User Lifecycle › **Suspend user** | No | No | Platform Service | /user-management/suspend-user | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.03.01.05 | User Management › User Lifecycle › **Remove user** | No | No | Platform Service | /user-management/remove-user | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.03.02.01 | User Management › Role Assignment › **Assign role** | No | No | Platform Service | /user-management/assign-role | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.03.02.02 | User Management › Role Assignment › **Assign group** | No | No | Platform Service | /user-management/assign-group | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.03.02.03 | User Management › Role Assignment › **Review access** | No | No | Platform Service | /user-management/review-access | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.04.01.01 | Group & Project Management › Groups › **Create group** | No | No | Platform Service | /group-project-management/create-group | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.04.01.02 | Group & Project Management › Groups › **Add member** | No | No | Platform Service | /group-project-management/add-member | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.04.01.03 | Group & Project Management › Groups › **Remove member** | No | No | Platform Service | /group-project-management/remove-member | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.04.02.01 | Group & Project Management › Projects › **Create project** | No | No | Platform Service | /group-project-management/create-project | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.04.02.02 | Group & Project Management › Projects › **Assign users** | No | No | Platform Service | /group-project-management/assign-users | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |
| 05.04.02.03 | Group & Project Management › Projects › **Assign resources** | No | No | Platform Service | /group-project-management/assign-resources | - | Tenant Service | Organization | - | UJ-005 Provision Service | Phase 2 |

## Sprint 2026.4.3
### 07 Subscription & Entitlement Management
| ID | Capability › Feature › Function | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 07.01.01.01 | Subscription Management › Subscription Lifecycle › **Create subscription** | Yes | No | Platform Service | /subscription-management/create-subscription | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 1 / MVP |
| 07.01.01.02 | Subscription Management › Subscription Lifecycle › **Activate subscription** | Yes | No | Platform Service | /subscription-management/activate-subscription | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 1 / MVP |
| 07.01.01.03 | Subscription Management › Subscription Lifecycle › **Suspend subscription** | Yes | No | Platform Service | /subscription-management/suspend-subscription | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 1 / MVP |
| 07.01.01.04 | Subscription Management › Subscription Lifecycle › **Upgrade** | No | No | Platform Service | /subscription-management/upgrade | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.01.01.05 | Subscription Management › Subscription Lifecycle › **Downgrade** | No | No | Platform Service | /subscription-management/downgrade | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.01.01.06 | Subscription Management › Subscription Lifecycle › **Renew** | No | No | Platform Service | /subscription-management/renew | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.01.01.07 | Subscription Management › Subscription Lifecycle › **Cancel** | No | No | Platform Service | /subscription-management/cancel | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.01.02.01 | Subscription Management › Subscription Changes › **Change quantity** | Yes | No | Platform Service | /subscription-management/change-quantity | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 1 / MVP |
| 07.01.02.02 | Subscription Management › Subscription Changes › **Change plan** | Yes | No | Platform Service | /subscription-management/change-plan | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 1 / MVP |
| 07.01.02.03 | Subscription Management › Subscription Changes › **Schedule change** | Yes | No | Platform Service | /subscription-management/schedule-change | API-011 POST /v1/subscriptions | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 1 / MVP |
| 07.02.01.01 | Entitlement Management › Entitlements › **Grant entitlement** | No | No | Platform Service | /entitlement-management/grant-entitlement | API-012 POST /v1/entitlements/check | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.02.01.02 | Entitlement Management › Entitlements › **Revoke entitlement** | No | No | Platform Service | /entitlement-management/revoke-entitlement | API-012 POST /v1/entitlements/check | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.02.01.03 | Entitlement Management › Entitlements › **Validate entitlement** | No | No | Platform Service | /entitlement-management/validate-entitlement | API-012 POST /v1/entitlements/check | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.02.01.04 | Entitlement Management › Entitlements › **Check feature access** | No | No | Platform Service | /entitlement-management/check-feature-access | API-012 POST /v1/entitlements/check | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.02.02.01 | Entitlement Management › Quota › **Check quota** | No | No | Platform Service | /entitlement-management/check-quota | API-012 POST /v1/entitlements/check | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.02.02.02 | Entitlement Management › Quota › **Allocate quota** | No | No | Platform Service | /entitlement-management/allocate-quota | API-012 POST /v1/entitlements/check | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.02.02.03 | Entitlement Management › Quota › **Adjust quota** | No | No | Platform Service | /entitlement-management/adjust-quota | API-012 POST /v1/entitlements/check | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.03.01.01 | License & Quota Management › Licensing › **Issue license** | No | No | Platform Service | /license-quota-management/issue-license | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.03.01.02 | License & Quota Management › Licensing › **Validate license** | No | No | Platform Service | /license-quota-management/validate-license | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.03.01.03 | License & Quota Management › Licensing › **Expire license** | No | No | Platform Service | /license-quota-management/expire-license | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.03.01.04 | License & Quota Management › Licensing › **Revoke license** | No | No | Platform Service | /license-quota-management/revoke-license | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.03.02.01 | License & Quota Management › Usage Limits › **Define quota** | No | No | Platform Service | /license-quota-management/define-quota | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.03.02.02 | License & Quota Management › Usage Limits › **Monitor quota** | No | No | Platform Service | /license-quota-management/monitor-quota | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.03.02.03 | License & Quota Management › Usage Limits › **Enforce quota** | No | No | Platform Service | /license-quota-management/enforce-quota | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.04.01.01 | Renewal & Lifecycle › Renewals › **Schedule renewal** | No | No | Platform Service | /renewal-lifecycle/schedule-renewal | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.04.01.02 | Renewal & Lifecycle › Renewals › **Notify renewal** | No | No | Platform Service | /renewal-lifecycle/notify-renewal | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.04.01.03 | Renewal & Lifecycle › Renewals › **Auto-renew** | No | No | Platform Service | /renewal-lifecycle/auto-renew | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.04.01.04 | Renewal & Lifecycle › Renewals › **Process renewal** | No | No | Platform Service | /renewal-lifecycle/process-renewal | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.04.02.01 | Renewal & Lifecycle › Lifecycle › **Expire subscription** | No | No | Platform Service | /renewal-lifecycle/expire-subscription | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 07.04.02.02 | Renewal & Lifecycle › Lifecycle › **Reactivate subscription** | No | No | Platform Service | /renewal-lifecycle/reactivate-subscription | - | Subscription Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |

### 08 Billing & Payments
| ID | Capability › Feature › Function | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 08.01.01.01 | Pricing › Price Books › **Create price** | No | No | Platform Service | /pricing/create-price | API-006 POST /v1/pricing/quote | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.01.01.02 | Pricing › Price Books › **Define tiers** | No | No | Platform Service | /pricing/define-tiers | API-006 POST /v1/pricing/quote | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.01.01.03 | Pricing › Price Books › **Define volume pricing** | No | No | Platform Service | /pricing/define-volume-pricing | API-006 POST /v1/pricing/quote | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.01.01.04 | Pricing › Price Books › **Define customer pricing** | No | No | Platform Service | /pricing/define-customer-pricing | API-006 POST /v1/pricing/quote | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.01.02.01 | Pricing › Promotions › **Create discount** | No | No | Platform Service | /pricing/create-discount | API-006 POST /v1/pricing/quote | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.01.02.02 | Pricing › Promotions › **Create coupon** | No | No | Platform Service | /pricing/create-coupon | API-006 POST /v1/pricing/quote | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.01.02.03 | Pricing › Promotions › **Apply promotion** | No | No | Platform Service | /pricing/apply-promotion | API-006 POST /v1/pricing/quote | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.02.01.01 | Billing › Usage Billing › **Collect usage** | No | No | Platform Service | /billing/collect-usage | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.02.01.02 | Billing › Usage Billing › **Calculate charges** | No | No | Platform Service | /billing/calculate-charges | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.02.01.03 | Billing › Usage Billing › **Apply discounts** | No | No | Platform Service | /billing/apply-discounts | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.02.01.04 | Billing › Usage Billing › **Calculate taxes** | No | No | Platform Service | /billing/calculate-taxes | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.02.02.01 | Billing › Invoice Generation › **Generate invoice** | No | No | Platform Service | /billing/generate-invoice | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.02.02.02 | Billing › Invoice Generation › **Adjust invoice** | No | No | Platform Service | /billing/adjust-invoice | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.02.02.03 | Billing › Invoice Generation › **Credit invoice** | No | No | Platform Service | /billing/credit-invoice | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.02.02.04 | Billing › Invoice Generation › **Finalize invoice** | No | No | Platform Service | /billing/finalize-invoice | API-013 POST /v1/billing/invoices | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.03.01.01 | Payment › Payment Methods › **Add payment method** | No | No | Platform Service | /payment/add-payment-method | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase | Phase 2 |
| 08.03.01.02 | Payment › Payment Methods › **Remove payment method** | No | No | Platform Service | /payment/remove-payment-method | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase | Phase 2 |
| 08.03.01.03 | Payment › Payment Methods › **Set default payment method** | No | No | Platform Service | /payment/set-default-payment-method | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase | Phase 2 |
| 08.03.02.01 | Payment › Transactions › **Authorize payment** | No | No | Platform Service | /payment/authorize-payment | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase | Phase 2 |
| 08.03.02.02 | Payment › Transactions › **Capture payment** | No | No | Platform Service | /payment/capture-payment | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase | Phase 2 |
| 08.03.02.03 | Payment › Transactions › **Refund payment** | No | No | Platform Service | /payment/refund-payment | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase | Phase 2 |
| 08.03.02.04 | Payment › Transactions › **Retry payment** | No | No | Platform Service | /payment/retry-payment | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase | Phase 2 |
| 08.03.02.05 | Payment › Transactions › **Reconcile payment** | No | No | Platform Service | /payment/reconcile-payment | API-014 POST /v1/payments | Pricing Service | Invoice | PaymentAuthorized | UJ-003 Self-Service Purchase | Phase 2 |
| 08.04.01.01 | Financial Documents › Documents › **Generate invoice** | No | No | Platform Service | /financial-documents/generate-invoice | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.04.01.02 | Financial Documents › Documents › **Generate receipt** | No | No | Platform Service | /financial-documents/generate-receipt | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.04.01.03 | Financial Documents › Documents › **Generate credit note** | No | No | Platform Service | /financial-documents/generate-credit-note | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.04.01.04 | Financial Documents › Documents › **Download document** | No | No | Platform Service | /financial-documents/download-document | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.05.01.01 | Tax & Currency › Tax › **Configure tax rules** | No | No | Platform Service | /tax-currency/configure-tax-rules | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.05.01.02 | Tax & Currency › Tax › **Calculate tax** | No | No | Platform Service | /tax-currency/calculate-tax | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.05.01.03 | Tax & Currency › Tax › **Validate tax** | No | No | Platform Service | /tax-currency/validate-tax | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.05.02.01 | Tax & Currency › Currency › **Configure currency** | No | No | Platform Service | /tax-currency/configure-currency | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.05.02.02 | Tax & Currency › Currency › **Convert currency** | No | No | Platform Service | /tax-currency/convert-currency | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |
| 08.05.02.03 | Tax & Currency › Currency › **Format currency** | No | No | Platform Service | /tax-currency/format-currency | - | Pricing Service | Invoice | PaymentAuthorized | UJ-005 Provision Service | Phase 2 |

## Sprint 2027.1.1
### 09 Order & Provisioning Management
| ID | Capability › Feature › Function | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 09.01.01.01 | Order Management › Order Lifecycle › **Create order** | Yes | No | Platform Service | /order-management/create-order | API-009 POST /v1/orders | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 1 / MVP |
| 09.01.01.02 | Order Management › Order Lifecycle › **Validate order** | Yes | No | Platform Service | /order-management/validate-order | API-009 POST /v1/orders | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 1 / MVP |
| 09.01.01.03 | Order Management › Order Lifecycle › **Price order** | Yes | No | Platform Service | /order-management/price-order | API-009 POST /v1/orders | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 1 / MVP |
| 09.01.01.04 | Order Management › Order Lifecycle › **Submit order** | No | No | Platform Service | /order-management/submit-order | API-009 POST /v1/orders | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.01.01.05 | Order Management › Order Lifecycle › **Approve order** | No | No | Platform Service | /order-management/approve-order | API-009 POST /v1/orders | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.01.01.06 | Order Management › Order Lifecycle › **Cancel order** | No | No | Platform Service | /order-management/cancel-order | API-009 POST /v1/orders | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.01.01.07 | Order Management › Order Lifecycle › **Track order** | No | No | Platform Service | /order-management/track-order | API-009 POST /v1/orders | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.02.01.01 | Provisioning › Service Provisioning › **Provision service** | No | No | Platform Service | /provisioning/provision-service | API-010 POST /v1/provisioning | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.02.01.02 | Provisioning › Service Provisioning › **Configure service** | No | No | Platform Service | /provisioning/configure-service | API-010 POST /v1/provisioning | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.02.01.03 | Provisioning › Service Provisioning › **Activate service** | No | No | Platform Service | /provisioning/activate-service | API-010 POST /v1/provisioning | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.02.01.04 | Provisioning › Service Provisioning › **Suspend service** | No | No | Platform Service | /provisioning/suspend-service | API-010 POST /v1/provisioning | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.02.01.05 | Provisioning › Service Provisioning › **Deprovision service** | No | No | Platform Service | /provisioning/deprovision-service | API-010 POST /v1/provisioning | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.03.01.01 | Workflow Orchestration › Workflow Runtime › **Trigger workflow** | No | No | Platform Service | /workflow-orchestration/trigger-workflow | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.03.01.02 | Workflow Orchestration › Workflow Runtime › **Execute workflow** | No | No | Platform Service | /workflow-orchestration/execute-workflow | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.03.01.03 | Workflow Orchestration › Workflow Runtime › **Retry step** | No | No | Platform Service | /workflow-orchestration/retry-step | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.03.01.04 | Workflow Orchestration › Workflow Runtime › **Rollback** | No | No | Platform Service | /workflow-orchestration/rollback | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.03.01.05 | Workflow Orchestration › Workflow Runtime › **Compensate** | No | No | Platform Service | /workflow-orchestration/compensate | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.03.01.06 | Workflow Orchestration › Workflow Runtime › **Escalate** | No | No | Platform Service | /workflow-orchestration/escalate | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.03.02.01 | Workflow Orchestration › Workflow Design › **Define workflow** | No | No | Platform Service | /workflow-orchestration/define-workflow | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.03.02.02 | Workflow Orchestration › Workflow Design › **Configure step** | No | No | Platform Service | /workflow-orchestration/configure-step | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.03.02.03 | Workflow Orchestration › Workflow Design › **Set dependency** | No | No | Platform Service | /workflow-orchestration/set-dependency | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.04.01.01 | Approval Management › Approvals › **Create approval** | No | No | Platform Service | /approval-management/create-approval | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.04.01.02 | Approval Management › Approvals › **Route approval** | No | No | Platform Service | /approval-management/route-approval | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.04.01.03 | Approval Management › Approvals › **Approve** | No | No | Platform Service | /approval-management/approve | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.04.01.04 | Approval Management › Approvals › **Reject** | No | No | Platform Service | /approval-management/reject | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 09.04.01.05 | Approval Management › Approvals › **Escalate** | No | No | Platform Service | /approval-management/escalate | - | Order Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |

### 10 Service & Resource Management
| ID | Capability › Feature › Function | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 10.01.01.01 | Service Management › Service Instance › **Create service instance** | No | No | Platform Service | /service-management/create-service-instance | API-015 GET /v1/services | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.01.01.02 | Service Management › Service Instance › **Configure service** | No | No | Platform Service | /service-management/configure-service | API-015 GET /v1/services | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.01.01.03 | Service Management › Service Instance › **Start service** | No | No | Platform Service | /service-management/start-service | API-015 GET /v1/services | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.01.01.04 | Service Management › Service Instance › **Stop service** | No | No | Platform Service | /service-management/stop-service | API-015 GET /v1/services | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.01.01.05 | Service Management › Service Instance › **Restart service** | No | No | Platform Service | /service-management/restart-service | API-015 GET /v1/services | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.01.01.06 | Service Management › Service Instance › **Scale service** | No | No | Platform Service | /service-management/scale-service | API-015 GET /v1/services | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.01.01.07 | Service Management › Service Instance › **Delete service** | No | No | Platform Service | /service-management/delete-service | API-015 GET /v1/services | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.02.01.01 | Resource Management › Resource Lifecycle › **Create resource** | No | No | Platform Service | /resource-management/create-resource | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.02.01.02 | Resource Management › Resource Lifecycle › **Update resource** | No | No | Platform Service | /resource-management/update-resource | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.02.01.03 | Resource Management › Resource Lifecycle › **Scale resource** | No | No | Platform Service | /resource-management/scale-resource | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.02.01.04 | Resource Management › Resource Lifecycle › **Monitor resource** | No | No | Platform Service | /resource-management/monitor-resource | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.02.01.05 | Resource Management › Resource Lifecycle › **Delete resource** | No | No | Platform Service | /resource-management/delete-resource | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.03.01.01 | Configuration Management › Configuration › **Create configuration** | No | No | Platform Service | /configuration-management/create-configuration | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.03.01.02 | Configuration Management › Configuration › **Validate configuration** | No | No | Platform Service | /configuration-management/validate-configuration | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.03.01.03 | Configuration Management › Configuration › **Apply configuration** | No | No | Platform Service | /configuration-management/apply-configuration | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.03.01.04 | Configuration Management › Configuration › **Rollback configuration** | No | No | Platform Service | /configuration-management/rollback-configuration | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.04.01.01 | Monitoring & Health › Health Monitoring › **Collect health status** | No | No | Platform Service | /monitoring-health/collect-health-status | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.04.01.02 | Monitoring & Health › Health Monitoring › **Detect anomaly** | No | No | Platform Service | /monitoring-health/detect-anomaly | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.04.01.03 | Monitoring & Health › Health Monitoring › **Create alert** | No | No | Platform Service | /monitoring-health/create-alert | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.04.01.04 | Monitoring & Health › Health Monitoring › **View health** | No | No | Platform Service | /monitoring-health/view-health | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.04.02.01 | Monitoring & Health › Usage Monitoring › **Collect metrics** | No | No | Platform Service | /monitoring-health/collect-metrics | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.04.02.02 | Monitoring & Health › Usage Monitoring › **View usage** | No | No | Platform Service | /monitoring-health/view-usage | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |
| 10.04.02.03 | Monitoring & Health › Usage Monitoring › **Set threshold** | No | No | Platform Service | /monitoring-health/set-threshold | - | Resource Service | - | ProvisioningStarted | UJ-005 Provision Service | Phase 2 |

### 13 Integration & API Platform
| ID | Capability › Feature › Function | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 13.01.01.01 | API Management › API Lifecycle › **Register API** | No | No | Platform Service | /api-management/register-api | - | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.01.01.02 | API Management › API Lifecycle › **Publish API** | No | No | Platform Service | /api-management/publish-api | - | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.01.01.03 | API Management › API Lifecycle › **Version API** | No | No | Platform Service | /api-management/version-api | - | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.01.01.04 | API Management › API Lifecycle › **Deprecate API** | No | No | Platform Service | /api-management/deprecate-api | - | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.01.02.01 | API Management › API Security › **Authenticate API** | No | No | Platform Service | /api-management/authenticate-api | - | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.01.02.02 | API Management › API Security › **Authorize API** | No | No | Platform Service | /api-management/authorize-api | - | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.01.02.03 | API Management › API Security › **Rate limit API** | No | No | Platform Service | /api-management/rate-limit-api | - | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.01.02.04 | API Management › API Security › **Monitor API** | No | No | Platform Service | /api-management/monitor-api | - | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.02.01.01 | Integration Hub › Connectors › **Create connector** | No | No | Platform Service | /integration-hub/create-connector | API-018 POST /v1/integrations | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.02.01.02 | Integration Hub › Connectors › **Authenticate connector** | No | No | Platform Service | /integration-hub/authenticate-connector | API-018 POST /v1/integrations | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.02.01.03 | Integration Hub › Connectors › **Test connector** | No | No | Platform Service | /integration-hub/test-connector | API-018 POST /v1/integrations | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.02.01.04 | Integration Hub › Connectors › **Enable connector** | No | No | Platform Service | /integration-hub/enable-connector | API-018 POST /v1/integrations | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.02.02.01 | Integration Hub › Data Integration › **Synchronize data** | No | No | Platform Service | /integration-hub/synchronize-data | API-018 POST /v1/integrations | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.02.02.02 | Integration Hub › Data Integration › **Transform data** | No | No | Platform Service | /integration-hub/transform-data | API-018 POST /v1/integrations | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.02.02.03 | Integration Hub › Data Integration › **Handle integration error** | No | No | Platform Service | /integration-hub/handle-integration-error | API-018 POST /v1/integrations | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.03.01.01 | Event Platform › Event Bus › **Publish event** | No | No | Platform Service | /event-platform/publish-event | API-019 POST /v1/events | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.03.01.02 | Event Platform › Event Bus › **Subscribe to event** | No | No | Platform Service | /event-platform/subscribe-to-event | API-019 POST /v1/events | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.03.01.03 | Event Platform › Event Bus › **Route event** | No | No | Platform Service | /event-platform/route-event | API-019 POST /v1/events | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.03.01.04 | Event Platform › Event Bus › **Retry event** | No | No | Platform Service | /event-platform/retry-event | API-019 POST /v1/events | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.03.01.05 | Event Platform › Event Bus › **Replay event** | No | No | Platform Service | /event-platform/replay-event | API-019 POST /v1/events | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.04.01.01 | Webhooks › Webhook Management › **Register webhook** | No | No | Platform Service | /webhooks/register-webhook | - | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.04.01.02 | Webhooks › Webhook Management › **Authenticate webhook** | No | No | Platform Service | /webhooks/authenticate-webhook | - | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.04.01.03 | Webhooks › Webhook Management › **Trigger webhook** | No | No | Platform Service | /webhooks/trigger-webhook | - | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.04.01.04 | Webhooks › Webhook Management › **Retry webhook** | No | No | Platform Service | /webhooks/retry-webhook | - | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |
| 13.04.01.05 | Webhooks › Webhook Management › **Monitor webhook** | No | No | Platform Service | /webhooks/monitor-webhook | - | Integration Service | - | - | UJ-005 Provision Service | Phase 2 |

### 16 Analytics & Data Platform
| ID | Capability › Feature › Function | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 16.01.01.01 | Customer Analytics › Customer Usage › **Analyze usage** | No | No | Platform Service | /customer-analytics/analyze-usage | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.01.01.02 | Customer Analytics › Customer Usage › **Analyze adoption** | No | No | Platform Service | /customer-analytics/analyze-adoption | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.01.01.03 | Customer Analytics › Customer Usage › **Analyze engagement** | No | No | Platform Service | /customer-analytics/analyze-engagement | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.01.01.04 | Customer Analytics › Customer Usage › **Calculate customer health** | No | No | Platform Service | /customer-analytics/calculate-customer-health | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.01.02.01 | Customer Analytics › Customer Value › **Analyze spending** | No | No | Platform Service | /customer-analytics/analyze-spending | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.01.02.02 | Customer Analytics › Customer Value › **Calculate customer lifetime value** | No | No | Platform Service | /customer-analytics/calculate-customer-lifetime-value | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.01.02.03 | Customer Analytics › Customer Value › **Analyze churn** | No | No | Platform Service | /customer-analytics/analyze-churn | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.02.01.01 | Product Analytics › Product Performance › **Analyze views** | No | No | Platform Service | /product-analytics/analyze-views | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.02.01.02 | Product Analytics › Product Performance › **Analyze trials** | No | No | Platform Service | /product-analytics/analyze-trials | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.02.01.03 | Product Analytics › Product Performance › **Analyze conversions** | No | No | Platform Service | /product-analytics/analyze-conversions | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.02.01.04 | Product Analytics › Product Performance › **Analyze subscriptions** | No | No | Platform Service | /product-analytics/analyze-subscriptions | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.02.02.01 | Product Analytics › Product Usage › **Analyze usage** | No | No | Platform Service | /product-analytics/analyze-usage | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.02.02.02 | Product Analytics › Product Usage › **Analyze feature adoption** | No | No | Platform Service | /product-analytics/analyze-feature-adoption | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.02.02.03 | Product Analytics › Product Usage › **Analyze churn** | No | No | Platform Service | /product-analytics/analyze-churn | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.03.01.01 | Operational Analytics › Operations › **Analyze incidents** | No | No | Platform Service | /operational-analytics/analyze-incidents | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.03.01.02 | Operational Analytics › Operations › **Analyze SLA** | No | No | Platform Service | /operational-analytics/analyze-sla | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.03.01.03 | Operational Analytics › Operations › **Analyze provisioning time** | No | No | Platform Service | /operational-analytics/analyze-provisioning-time | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.03.01.04 | Operational Analytics › Operations › **Analyze service health** | No | No | Platform Service | /operational-analytics/analyze-service-health | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.04.01.01 | Business Analytics › Financial KPIs › **Calculate revenue** | No | No | Platform Service | /business-analytics/calculate-revenue | API-021 GET /v1/analytics | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.04.01.02 | Business Analytics › Financial KPIs › **Calculate ARR** | No | No | Platform Service | /business-analytics/calculate-arr | API-021 GET /v1/analytics | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.04.01.03 | Business Analytics › Financial KPIs › **Calculate MRR** | No | No | Platform Service | /business-analytics/calculate-mrr | API-021 GET /v1/analytics | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.04.01.04 | Business Analytics › Financial KPIs › **Calculate marketplace GMV** | No | No | Platform Service | /business-analytics/calculate-marketplace-gmv | API-021 GET /v1/analytics | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.04.02.01 | Business Analytics › Growth KPIs › **Calculate acquisition** | No | No | Platform Service | /business-analytics/calculate-acquisition | API-021 GET /v1/analytics | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.04.02.02 | Business Analytics › Growth KPIs › **Calculate churn** | No | No | Platform Service | /business-analytics/calculate-churn | API-021 GET /v1/analytics | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.04.02.03 | Business Analytics › Growth KPIs › **Calculate conversion** | No | No | Platform Service | /business-analytics/calculate-conversion | API-021 GET /v1/analytics | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.05.01.01 | Data Platform › Data Ingestion › **Ingest operational data** | No | No | Platform Service | /data-platform/ingest-operational-data | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.05.01.02 | Data Platform › Data Ingestion › **Ingest event data** | No | No | Platform Service | /data-platform/ingest-event-data | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.05.01.03 | Data Platform › Data Ingestion › **Validate data** | No | No | Platform Service | /data-platform/validate-data | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.05.02.01 | Data Platform › Data Management › **Transform data** | No | No | Platform Service | /data-platform/transform-data | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.05.02.02 | Data Platform › Data Management › **Catalog data** | No | No | Platform Service | /data-platform/catalog-data | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.05.02.03 | Data Platform › Data Management › **Manage lineage** | No | No | Platform Service | /data-platform/manage-lineage | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |
| 16.05.02.04 | Data Platform › Data Management › **Manage data quality** | No | No | Platform Service | /data-platform/manage-data-quality | - | Analytics Service | - | - | UJ-005 Provision Service | Phase 2 |

## Sprint 2027.1.2
### 04 AI Advisor & Agent Platform
| ID | Capability › Feature › Function | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 04.01.01.01 | AI Product Advisor › Requirement Discovery › **Ask customer questions** | No | Yes | AI Agent | /ai-product-advisor/ask-customer-questions | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.01.01.02 | AI Product Advisor › Requirement Discovery › **Capture requirements** | No | Yes | AI Agent | /ai-product-advisor/capture-requirements | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.01.01.03 | AI Product Advisor › Requirement Discovery › **Identify constraints** | No | Yes | AI Agent | /ai-product-advisor/identify-constraints | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.01.02.01 | AI Product Advisor › Recommendation › **Search catalog** | No | Yes | AI Agent | /ai-product-advisor/search-catalog | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.01.02.02 | AI Product Advisor › Recommendation › **Evaluate compatibility** | No | Yes | AI Agent | /ai-product-advisor/evaluate-compatibility | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.01.02.03 | AI Product Advisor › Recommendation › **Rank products** | No | Yes | AI Agent | /ai-product-advisor/rank-products | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.01.02.04 | AI Product Advisor › Recommendation › **Explain recommendation** | No | Yes | AI Agent | /ai-product-advisor/explain-recommendation | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.01.02.05 | AI Product Advisor › Recommendation › **Recommend configuration** | No | Yes | AI Agent | /ai-product-advisor/recommend-configuration | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.01.02.06 | AI Product Advisor › Recommendation › **Estimate cost** | No | Yes | AI Agent | /ai-product-advisor/estimate-cost | API-007 POST /v1/recommendations | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.02.01.01 | AI Sales Agent › Sales Assistance › **Qualify lead** | No | Yes | AI Agent | /ai-sales-agent/qualify-lead | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.02.01.02 | AI Sales Agent › Sales Assistance › **Explain pricing** | No | Yes | AI Agent | /ai-sales-agent/explain-pricing | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.02.01.03 | AI Sales Agent › Sales Assistance › **Generate proposal** | No | Yes | AI Agent | /ai-sales-agent/generate-proposal | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.02.01.04 | AI Sales Agent › Sales Assistance › **Generate quote** | No | Yes | AI Agent | /ai-sales-agent/generate-quote | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.02.01.05 | AI Sales Agent › Sales Assistance › **Recommend upsell** | No | Yes | AI Agent | /ai-sales-agent/recommend-upsell | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.02.01.06 | AI Sales Agent › Sales Assistance › **Recommend cross-sell** | No | Yes | AI Agent | /ai-sales-agent/recommend-cross-sell | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.03.01.01 | AI Technical Advisor › Technical Guidance › **Recommend architecture** | No | Yes | AI Agent | /ai-technical-advisor/recommend-architecture | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.03.01.02 | AI Technical Advisor › Technical Guidance › **Explain configuration** | No | Yes | AI Agent | /ai-technical-advisor/explain-configuration | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.03.01.03 | AI Technical Advisor › Technical Guidance › **Troubleshoot issue** | No | Yes | AI Agent | /ai-technical-advisor/troubleshoot-issue | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.03.01.04 | AI Technical Advisor › Technical Guidance › **Recommend best practice** | No | Yes | AI Agent | /ai-technical-advisor/recommend-best-practice | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.04.01.01 | AI Support Agent › Support Automation › **Understand request** | No | Yes | AI Agent | /ai-support-agent/understand-request | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.04.01.02 | AI Support Agent › Support Automation › **Search knowledge base** | No | Yes | AI Agent | /ai-support-agent/search-knowledge-base | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.04.01.03 | AI Support Agent › Support Automation › **Diagnose issue** | No | Yes | AI Agent | /ai-support-agent/diagnose-issue | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.04.01.04 | AI Support Agent › Support Automation › **Recommend resolution** | No | Yes | AI Agent | /ai-support-agent/recommend-resolution | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.04.01.05 | AI Support Agent › Support Automation › **Create ticket** | No | Yes | AI Agent | /ai-support-agent/create-ticket | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.04.01.06 | AI Support Agent › Support Automation › **Escalate to human** | No | Yes | AI Agent | /ai-support-agent/escalate-to-human | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.05.01.01 | AI Agent Orchestration › Agent Runtime › **Register agent** | No | Yes | AI Agent | /ai-agent-orchestration/register-agent | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.05.01.02 | AI Agent Orchestration › Agent Runtime › **Route request** | No | Yes | AI Agent | /ai-agent-orchestration/route-request | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.05.01.03 | AI Agent Orchestration › Agent Runtime › **Select tools** | No | Yes | AI Agent | /ai-agent-orchestration/select-tools | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.05.01.04 | AI Agent Orchestration › Agent Runtime › **Manage context** | No | Yes | AI Agent | /ai-agent-orchestration/manage-context | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.05.01.05 | AI Agent Orchestration › Agent Runtime › **Apply guardrails** | No | Yes | AI Agent | /ai-agent-orchestration/apply-guardrails | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |
| 04.05.01.06 | AI Agent Orchestration › Agent Runtime › **Audit agent action** | No | Yes | AI Agent | /ai-agent-orchestration/audit-agent-action | - | AI Agent Gateway | - | AIRecommendationGenerated | UJ-002 AI Guided Selection | Phase 2 |

## Sprint 2027.1.3
### 03 Marketplace
| ID | Capability › Feature › Function | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 03.01.01.01 | Product Discovery › Catalog Browsing › **Browse categories** | No | No | Customer | /product-discovery/browse-categories | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.01.01.02 | Product Discovery › Catalog Browsing › **Search products** | No | No | Customer | /product-discovery/search-products | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.01.01.03 | Product Discovery › Catalog Browsing › **Filter products** | No | No | Customer | /product-discovery/filter-products | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.01.01.04 | Product Discovery › Catalog Browsing › **Sort products** | No | No | Customer | /product-discovery/sort-products | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.01.02.01 | Product Discovery › Recommendations › **Recommend products** | No | No | Customer | /product-discovery/recommend-products | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.01.02.02 | Product Discovery › Recommendations › **Show featured products** | No | No | Customer | /product-discovery/show-featured-products | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.01.02.03 | Product Discovery › Recommendations › **Show popular products** | No | No | Customer | /product-discovery/show-popular-products | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.02.01.01 | Product Evaluation › Evaluation › **Start trial** | No | No | Customer | /product-evaluation/start-trial | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.02.01.02 | Product Evaluation › Evaluation › **Request demo** | No | No | Customer | /product-evaluation/request-demo | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.02.01.03 | Product Evaluation › Evaluation › **Launch sandbox** | No | No | Customer | /product-evaluation/launch-sandbox | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.02.01.04 | Product Evaluation › Evaluation › **View prerequisites** | No | No | Customer | /product-evaluation/view-prerequisites | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.02.02.01 | Product Evaluation › Comparison › **Compare products** | No | No | Customer | /product-evaluation/compare-products | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.02.02.02 | Product Evaluation › Comparison › **Compare plans** | No | No | Customer | /product-evaluation/compare-plans | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.02.02.03 | Product Evaluation › Comparison › **Estimate cost** | No | No | Customer | /product-evaluation/estimate-cost | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.03.01.01 | Marketplace Checkout › Checkout › **Select product** | No | No | Customer | /marketplace-checkout/select-product | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.03.01.02 | Marketplace Checkout › Checkout › **Select plan** | No | No | Customer | /marketplace-checkout/select-plan | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.03.01.03 | Marketplace Checkout › Checkout › **Configure options** | No | No | Customer | /marketplace-checkout/configure-options | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.03.01.04 | Marketplace Checkout › Checkout › **Apply discount** | No | No | Customer | /marketplace-checkout/apply-discount | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.03.01.05 | Marketplace Checkout › Checkout › **Accept terms** | No | No | Customer | /marketplace-checkout/accept-terms | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.03.01.06 | Marketplace Checkout › Checkout › **Submit order** | No | No | Customer | /marketplace-checkout/submit-order | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.03.02.01 | Marketplace Checkout › Purchase Validation › **Validate eligibility** | No | No | Customer | /marketplace-checkout/validate-eligibility | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.03.02.02 | Marketplace Checkout › Purchase Validation › **Validate payment** | No | No | Customer | /marketplace-checkout/validate-payment | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-003 Self-Service Purchase | Phase 2 |
| 03.03.02.03 | Marketplace Checkout › Purchase Validation › **Validate dependencies** | No | No | Customer | /marketplace-checkout/validate-dependencies | API-008 POST /v1/checkout | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.04.01.01 | Reviews & Ratings › Customer Feedback › **Submit review** | No | No | Customer | /reviews-ratings/submit-review | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.04.01.02 | Reviews & Ratings › Customer Feedback › **Rate product** | No | No | Customer | /reviews-ratings/rate-product | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.04.01.03 | Reviews & Ratings › Customer Feedback › **Moderate review** | No | No | Customer | /reviews-ratings/moderate-review | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |
| 03.04.01.04 | Reviews & Ratings › Customer Feedback › **View ratings** | No | No | Customer | /reviews-ratings/view-ratings | - | Marketplace Service | Subscription | CheckoutCompleted | UJ-005 Provision Service | Phase 2 |

### 11 Training & Knowledge Management
| ID | Capability › Feature › Function | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 11.01.01.01 | Knowledge Base › Knowledge Articles › **Create article** | No | No | Platform Service | /knowledge-base/create-article | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.01.01.02 | Knowledge Base › Knowledge Articles › **Edit article** | No | No | Platform Service | /knowledge-base/edit-article | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.01.01.03 | Knowledge Base › Knowledge Articles › **Publish article** | No | No | Platform Service | /knowledge-base/publish-article | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.01.01.04 | Knowledge Base › Knowledge Articles › **Search article** | No | No | Platform Service | /knowledge-base/search-article | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.01.01.05 | Knowledge Base › Knowledge Articles › **Version article** | No | No | Platform Service | /knowledge-base/version-article | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.01.02.01 | Knowledge Base › AI Knowledge › **Index content** | No | No | Platform Service | /knowledge-base/index-content | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.01.02.02 | Knowledge Base › AI Knowledge › **Retrieve relevant content** | No | No | Platform Service | /knowledge-base/retrieve-relevant-content | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.01.02.03 | Knowledge Base › AI Knowledge › **Validate source** | No | No | Platform Service | /knowledge-base/validate-source | API-017 POST /v1/knowledge/search | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.02.01.01 | Learning Management › Courses › **Create course** | No | No | Platform Service | /learning-management/create-course | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.02.01.02 | Learning Management › Courses › **Publish course** | No | No | Platform Service | /learning-management/publish-course | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.02.01.03 | Learning Management › Courses › **Enroll user** | No | No | Platform Service | /learning-management/enroll-user | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.02.01.04 | Learning Management › Courses › **Track progress** | No | No | Platform Service | /learning-management/track-progress | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.02.01.05 | Learning Management › Courses › **Complete course** | No | No | Platform Service | /learning-management/complete-course | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.02.02.01 | Learning Management › Learning Paths › **Create learning path** | No | No | Platform Service | /learning-management/create-learning-path | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.02.02.02 | Learning Management › Learning Paths › **Assign learning path** | No | No | Platform Service | /learning-management/assign-learning-path | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.02.02.03 | Learning Management › Learning Paths › **Track path progress** | No | No | Platform Service | /learning-management/track-path-progress | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.03.01.01 | Training Delivery › Labs & Assessments › **Launch lab** | No | Yes | AI Agent | /training-delivery/launch-lab | - | Knowledge Service | KnowledgeArticle | - | UJ-002 AI Guided Selection | Phase 2 |
| 11.03.01.02 | Training Delivery › Labs & Assessments › **Submit assessment** | No | Yes | AI Agent | /training-delivery/submit-assessment | - | Knowledge Service | KnowledgeArticle | - | UJ-002 AI Guided Selection | Phase 2 |
| 11.03.01.03 | Training Delivery › Labs & Assessments › **Score assessment** | No | Yes | AI Agent | /training-delivery/score-assessment | - | Knowledge Service | KnowledgeArticle | - | UJ-002 AI Guided Selection | Phase 2 |
| 11.03.01.04 | Training Delivery › Labs & Assessments › **Track completion** | No | Yes | AI Agent | /training-delivery/track-completion | - | Knowledge Service | KnowledgeArticle | - | UJ-002 AI Guided Selection | Phase 2 |
| 11.03.02.01 | Training Delivery › Video Learning › **Stream video** | No | Yes | AI Agent | /training-delivery/stream-video | - | Knowledge Service | KnowledgeArticle | - | UJ-002 AI Guided Selection | Phase 2 |
| 11.03.02.02 | Training Delivery › Video Learning › **Track watch progress** | No | Yes | AI Agent | /training-delivery/track-watch-progress | - | Knowledge Service | KnowledgeArticle | - | UJ-002 AI Guided Selection | Phase 2 |
| 11.04.01.01 | Certification › Certificates › **Define certification** | No | No | Platform Service | /certification/define-certification | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.04.01.02 | Certification › Certificates › **Issue certificate** | No | No | Platform Service | /certification/issue-certificate | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.04.01.03 | Certification › Certificates › **Verify certificate** | No | No | Platform Service | /certification/verify-certificate | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |
| 11.04.01.04 | Certification › Certificates › **Expire certificate** | No | No | Platform Service | /certification/expire-certificate | - | Knowledge Service | KnowledgeArticle | - | UJ-005 Provision Service | Phase 2 |

### 12 Support & Service Management
| ID | Capability › Feature › Function | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 12.01.01.01 | Support › Ticket Management › **Create ticket** | No | Yes | AI Agent | /support/create-ticket | API-016 POST /v1/support/tickets | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.01.01.02 | Support › Ticket Management › **Categorize ticket** | No | Yes | AI Agent | /support/categorize-ticket | API-016 POST /v1/support/tickets | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.01.01.03 | Support › Ticket Management › **Prioritize ticket** | No | Yes | AI Agent | /support/prioritize-ticket | API-016 POST /v1/support/tickets | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.01.01.04 | Support › Ticket Management › **Assign ticket** | No | Yes | AI Agent | /support/assign-ticket | API-016 POST /v1/support/tickets | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.01.01.05 | Support › Ticket Management › **Escalate ticket** | No | Yes | AI Agent | /support/escalate-ticket | API-016 POST /v1/support/tickets | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.01.01.06 | Support › Ticket Management › **Resolve ticket** | No | Yes | AI Agent | /support/resolve-ticket | API-016 POST /v1/support/tickets | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.01.01.07 | Support › Ticket Management › **Close ticket** | No | Yes | AI Agent | /support/close-ticket | API-016 POST /v1/support/tickets | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.02.01.01 | AI Support › Conversational Support › **Start conversation** | No | Yes | AI Agent | /ai-support/start-conversation | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.02.01.02 | AI Support › Conversational Support › **Search knowledge** | No | Yes | AI Agent | /ai-support/search-knowledge | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.02.01.03 | AI Support › Conversational Support › **Diagnose issue** | No | Yes | AI Agent | /ai-support/diagnose-issue | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.02.01.04 | AI Support › Conversational Support › **Recommend resolution** | No | Yes | AI Agent | /ai-support/recommend-resolution | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.02.01.05 | AI Support › Conversational Support › **Execute permitted remediation** | No | Yes | AI Agent | /ai-support/execute-permitted-remediation | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.02.02.01 | AI Support › Human Handoff › **Create ticket** | No | Yes | AI Agent | /ai-support/create-ticket | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.02.02.02 | AI Support › Human Handoff › **Transfer conversation** | No | Yes | AI Agent | /ai-support/transfer-conversation | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.02.02.03 | AI Support › Human Handoff › **Provide AI summary** | No | Yes | AI Agent | /ai-support/provide-ai-summary | - | Support Service | SupportTicket | TicketCreated | UJ-002 AI Guided Selection | Phase 2 |
| 12.03.01.01 | SLA Management › SLA Policy › **Define SLA** | No | No | Platform Service | /sla-management/define-sla | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |
| 12.03.01.02 | SLA Management › SLA Policy › **Assign SLA** | No | No | Platform Service | /sla-management/assign-sla | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |
| 12.03.01.03 | SLA Management › SLA Policy › **Calculate SLA** | No | No | Platform Service | /sla-management/calculate-sla | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |
| 12.03.02.01 | SLA Management › SLA Monitoring › **Monitor SLA** | No | No | Platform Service | /sla-management/monitor-sla | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |
| 12.03.02.02 | SLA Management › SLA Monitoring › **Warn before breach** | No | No | Platform Service | /sla-management/warn-before-breach | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |
| 12.03.02.03 | SLA Management › SLA Monitoring › **Escalate breach** | No | No | Platform Service | /sla-management/escalate-breach | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |
| 12.04.01.01 | Incident & Problem Management › Incident › **Log incident** | No | No | Platform Service | /incident-problem-management/log-incident | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |
| 12.04.01.02 | Incident & Problem Management › Incident › **Investigate incident** | No | No | Platform Service | /incident-problem-management/investigate-incident | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |
| 12.04.01.03 | Incident & Problem Management › Incident › **Resolve incident** | No | No | Platform Service | /incident-problem-management/resolve-incident | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |
| 12.04.01.04 | Incident & Problem Management › Incident › **Close incident** | No | No | Platform Service | /incident-problem-management/close-incident | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |
| 12.04.02.01 | Incident & Problem Management › Problem › **Create problem** | No | No | Platform Service | /incident-problem-management/create-problem | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |
| 12.04.02.02 | Incident & Problem Management › Problem › **Perform root cause analysis** | No | No | Platform Service | /incident-problem-management/perform-root-cause-analysis | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |
| 12.04.02.03 | Incident & Problem Management › Problem › **Track corrective action** | No | No | Platform Service | /incident-problem-management/track-corrective-action | - | Support Service | SupportTicket | TicketCreated | UJ-005 Provision Service | Phase 2 |

## Sprint 2027.2.1
### 14 Partner & Provider Management
| ID | Capability › Feature › Function | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 14.01.01.01 | Provider Onboarding › Provider Lifecycle › **Register provider** | No | No | Platform Service | /provider-onboarding/register-provider | API-020 POST /v1/providers | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.01.01.02 | Provider Onboarding › Provider Lifecycle › **Verify provider** | No | No | Platform Service | /provider-onboarding/verify-provider | API-020 POST /v1/providers | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.01.01.03 | Provider Onboarding › Provider Lifecycle › **Approve provider** | No | No | Platform Service | /provider-onboarding/approve-provider | API-020 POST /v1/providers | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.01.01.04 | Provider Onboarding › Provider Lifecycle › **Activate provider** | No | No | Platform Service | /provider-onboarding/activate-provider | API-020 POST /v1/providers | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.01.02.01 | Provider Onboarding › Contracts › **Create contract** | No | No | Platform Service | /provider-onboarding/create-contract | API-020 POST /v1/providers | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.01.02.02 | Provider Onboarding › Contracts › **Manage terms** | No | No | Platform Service | /provider-onboarding/manage-terms | API-020 POST /v1/providers | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.01.02.03 | Provider Onboarding › Contracts › **Track expiration** | No | No | Platform Service | /provider-onboarding/track-expiration | API-020 POST /v1/providers | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.02.01.01 | Publisher Management › Publisher Catalog › **Create publisher product** | No | No | Platform Service | /publisher-management/create-publisher-product | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.02.01.02 | Publisher Management › Publisher Catalog › **Manage pricing** | No | No | Platform Service | /publisher-management/manage-pricing | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.02.01.03 | Publisher Management › Publisher Catalog › **Manage content** | No | No | Platform Service | /publisher-management/manage-content | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.02.01.04 | Publisher Management › Publisher Catalog › **Publish product** | No | No | Platform Service | /publisher-management/publish-product | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.02.02.01 | Publisher Management › Publisher Analytics › **View product performance** | No | No | Platform Service | /publisher-management/view-product-performance | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.02.02.02 | Publisher Management › Publisher Analytics › **View sales** | No | No | Platform Service | /publisher-management/view-sales | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.02.02.03 | Publisher Management › Publisher Analytics › **View usage** | No | No | Platform Service | /publisher-management/view-usage | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.03.01.01 | Revenue Sharing › Commission › **Define commission** | No | No | Platform Service | /revenue-sharing/define-commission | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.03.01.02 | Revenue Sharing › Commission › **Calculate revenue share** | No | No | Platform Service | /revenue-sharing/calculate-revenue-share | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.03.01.03 | Revenue Sharing › Commission › **Generate statement** | No | No | Platform Service | /revenue-sharing/generate-statement | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.03.02.01 | Revenue Sharing › Payouts › **Calculate payout** | No | No | Platform Service | /revenue-sharing/calculate-payout | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.03.02.02 | Revenue Sharing › Payouts › **Approve payout** | No | No | Platform Service | /revenue-sharing/approve-payout | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.03.02.03 | Revenue Sharing › Payouts › **Reconcile payout** | No | No | Platform Service | /revenue-sharing/reconcile-payout | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.04.01.01 | Partner Operations › Partner Support › **Create partner ticket** | No | No | Platform Service | /partner-operations/create-partner-ticket | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.04.01.02 | Partner Operations › Partner Support › **Assign partner manager** | No | No | Platform Service | /partner-operations/assign-partner-manager | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |
| 14.04.01.03 | Partner Operations › Partner Support › **Track partner SLA** | No | No | Platform Service | /partner-operations/track-partner-sla | - | Partner Service | Partner | - | UJ-005 Provision Service | Phase 2 |

## Sprint 2027.2.2
### 15 Administration & Governance
| ID | Capability › Feature › Function | MVP | AI | Actor | Suggested API | Primary API | Microservice | Entity | Event | Journey | Phase |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 15.01.01.01 | Platform Administration › Platform Configuration › **Configure platform** | No | No | Platform Service | /platform-administration/configure-platform | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.01.01.02 | Platform Administration › Platform Configuration › **Configure languages** | No | No | Platform Service | /platform-administration/configure-languages | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.01.01.03 | Platform Administration › Platform Configuration › **Configure currencies** | No | No | Platform Service | /platform-administration/configure-currencies | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.01.01.04 | Platform Administration › Platform Configuration › **Configure feature flags** | No | No | Platform Service | /platform-administration/configure-feature-flags | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.01.02.01 | Platform Administration › Global Settings › **Configure regions** | No | No | Platform Service | /platform-administration/configure-regions | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.01.02.02 | Platform Administration › Global Settings › **Configure defaults** | No | No | Platform Service | /platform-administration/configure-defaults | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.01.02.03 | Platform Administration › Global Settings › **Manage templates** | No | No | Platform Service | /platform-administration/manage-templates | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.02.01.01 | Policy Management › Policy Lifecycle › **Create policy** | No | No | Platform Service | /policy-management/create-policy | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.02.01.02 | Policy Management › Policy Lifecycle › **Assign policy** | No | No | Platform Service | /policy-management/assign-policy | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.02.01.03 | Policy Management › Policy Lifecycle › **Evaluate policy** | No | No | Platform Service | /policy-management/evaluate-policy | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.02.01.04 | Policy Management › Policy Lifecycle › **Enforce policy** | No | No | Platform Service | /policy-management/enforce-policy | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.02.01.05 | Policy Management › Policy Lifecycle › **Manage exception** | No | No | Platform Service | /policy-management/manage-exception | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.03.01.01 | Audit › Audit Logging › **Record activity** | No | No | Platform Service | /audit/record-activity | API-022 GET /v1/audit/events | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.03.01.02 | Audit › Audit Logging › **Record login** | No | No | Platform Service | /audit/record-login | API-022 GET /v1/audit/events | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.03.01.03 | Audit › Audit Logging › **Record configuration change** | No | No | Platform Service | /audit/record-configuration-change | API-022 GET /v1/audit/events | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.03.01.04 | Audit › Audit Logging › **Record financial transaction** | No | No | Platform Service | /audit/record-financial-transaction | API-022 GET /v1/audit/events | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.03.02.01 | Audit › Audit Search › **Search audit logs** | No | No | Platform Service | /audit/search-audit-logs | API-022 GET /v1/audit/events | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.03.02.02 | Audit › Audit Search › **Filter audit logs** | No | No | Platform Service | /audit/filter-audit-logs | API-022 GET /v1/audit/events | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.03.02.03 | Audit › Audit Search › **Export audit logs** | No | No | Platform Service | /audit/export-audit-logs | API-022 GET /v1/audit/events | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.04.01.01 | Compliance › Compliance Controls › **Define control** | No | No | Platform Service | /compliance/define-control | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.04.01.02 | Compliance › Compliance Controls › **Map requirement** | No | No | Platform Service | /compliance/map-requirement | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.04.01.03 | Compliance › Compliance Controls › **Collect evidence** | No | No | Platform Service | /compliance/collect-evidence | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.04.01.04 | Compliance › Compliance Controls › **Track remediation** | No | No | Platform Service | /compliance/track-remediation | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.04.02.01 | Compliance › Data Governance › **Classify data** | No | No | Platform Service | /compliance/classify-data | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.04.02.02 | Compliance › Data Governance › **Define retention** | No | No | Platform Service | /compliance/define-retention | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.04.02.03 | Compliance › Data Governance › **Apply retention** | No | No | Platform Service | /compliance/apply-retention | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.05.01.01 | Regional Operations › Region Management › **Create region** | No | No | Platform Service | /regional-operations/create-region | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.05.01.02 | Regional Operations › Region Management › **Configure region** | No | No | Platform Service | /regional-operations/configure-region | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.05.01.03 | Regional Operations › Region Management › **Activate region** | No | No | Platform Service | /regional-operations/activate-region | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.05.01.04 | Regional Operations › Region Management › **Suspend region** | No | No | Platform Service | /regional-operations/suspend-region | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.05.02.01 | Regional Operations › Data Residency › **Define residency policy** | No | No | Platform Service | /regional-operations/define-residency-policy | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.05.02.02 | Regional Operations › Data Residency › **Validate residency** | No | No | Platform Service | /regional-operations/validate-residency | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |
| 15.05.02.03 | Regional Operations › Data Residency › **Report residency** | No | No | Platform Service | /regional-operations/report-residency | - | Audit Service | AuditEvent | AuditRecorded | UJ-005 Provision Service | Phase 2 |


# Appendix C: how this repository already lines up with the plan
This compares the plan with the current code on branch `dev`. It is **not** from the three documents.

| EIS application ([PO] sprint) | Already in this repository (`backend/…/modules`, `frontend/src`) |
|-------------------------------|-------------------------------------------------------------------|
| 01 Experience & Customer Portal (2026.3.3) | Portal pages, customer and business dashboards, search history, notifications and notification preferences, theme and locale preferences, favorites and recent product usage |
| 06 Identity & Access Management (2026.3.3) | Keycloak login and sessions, password reset, platform TOTP MFA with recovery codes, RBAC roles and permissions, privileged-access (JIT) requests, SAML identity federation per organization |
| 02 Product & Catalog Management (2026.4.1) | Products, platforms, plans with billing period, product search facets, product images |
| 05 Customer / Tenant Management (2026.4.2) | Organization registration with email verification, members and org roles, org MFA policy |
| 07 Subscription & Entitlement Management (2026.4.3) | Product subscriptions, seat limits, per-organization product access |
| 15 Administration & Governance (2027.2.2) | Platform and organization audit logs |

The sprint-2026.3.3 applications (01 and 06) are therefore largely implemented already. Parts of 02, 05, 07 and 15 have also been built ahead of their [PO] sprints.
