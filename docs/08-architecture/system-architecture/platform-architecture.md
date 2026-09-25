# Platform architecture: eVyoog EIS

| Field | Value |
|---|---|
| Status | Baseline from the planning sources. Not an approved design (no DES- or ADR- IDs yet) |
| Sources | [WB:Architecture Layers], [WB:Architecture Principles], [WB:Application Summary], [WB:Microservices]; [CG] "Cross-Cutting Platform Capabilities", "Multi-Regional Architecture", "Suggested Initial MVP" ([source documents](../../01-business/source-documents/)) |

## 1. Logical layers ([WB:Architecture Layers])
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

## 2. Applications ([WB:Application Summary] = [PO] Table 1)
| Application ID | Application | Description | Capabilities |
|---|---|---|---|
| 01 | Enterprise Intelligence Suite (workbook: Experience & Customer Portal; [C10](../../01-business/roadmap/open-decisions.md#c10)) | Customer-facing web/mobile experience | Portal Experience; Customer Dashboard; Global Search; Notifications & Communications |
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

Sprint per application: see [`docs/01-business/roadmap/`](../../01-business/roadmap/README.md).

## 3. Cross-cutting platform services ([CG])
"These should not be duplicated inside each application."

```
Platform Foundation
├── Identity            ├── Workflow              ├── AI/LLM Gateway
├── Authorization       ├── Event Bus             ├── Localization
├── Tenant Management   ├── API Gateway           ├── Currency
├── Configuration       ├── File/Object Storage   ├── Tax
├── Notification        ├── Audit                 ├── Feature Flags
├── Search              ├── Observability         ├── Secrets Management
                                                  └── Compliance
```

## 4. Global control plane and regional service plane ([CG] "Multi-Regional Architecture")
```
Global Platform
├── Global Control Plane ── customer identity, product catalog, marketplace, global configuration,
│                           product metadata, billing policies, global AI agents, partner management
└── Regional Data/Service Plane ── customer data, service instances, resource provisioning, regional billing,
                                   regional integrations, regional support, data residency
    Regions named: North America (US-East, US-West, Canada) · Europe (EU-West, EU-Central)
                   Asia Pacific (India, Singapore, Japan, Australia) · Middle East (UAE)
```

## 5. Service view ([CG] "Suggested Initial MVP" diagram)
```
                   ┌──────────────────────┐
                   │   Customer Portal    │
                   └──────────┬───────────┘
             ┌────────────────┼────────────────┐
        Marketplace       AI Advisor      Customer Mgmt
             └────────────────┼────────────────┘
                  ┌───────────┴───────────┐
                  │   Platform Services   │
                  └───────────┬───────────┘
       ┌──────────────┬───────┼────────┬──────────────┐
      IAM       Subscription  Billing  Workflow    Integration
       └──────────────┴───────┼────────┴──────────────┘
                       Service Platform
                 ┌────────────┼────────────┐
              Compute      Data/DB       APIs
```

## 6. Suggested microservices ([WB:Microservices])
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

[CG] principle: "the business capability comes first; microservice boundaries come afterward."

**Decision [C13](../../01-business/roadmap/open-decisions.md#c13) (2026-09-25):** these are **logical domain boundaries**. Each one is built as a module in the single backend (`backend/…/modules/<domain>/`), not as a separate deployable. Container rules apply per deployable: one image each for the backend, the frontend and `ai-service`, each built once and promoted unchanged through DEV → QA → STAGING → PRODUCTION. A module becomes its own container only through a new, recorded decision.

## 7. Architecture principles ([WB:Architecture Principles])
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

## 8. What is implemented today (observed in this repository)
```
Browser ── React SPA (frontend/, Vite dev proxy /api) ──► Spring Boot API (backend/, :8081/api)
                 │                                             │  modules: audit, auth, authorization, dashboard,
                 │ OIDC (oidc-client-ts)                       │  federation, notification, platform, preference,
                 ▼                                             │  product, registration
            Keycloak (user.evyoog.com, realm eVyoog) ◄─────────┤  JWT validation, SAML federation, TOTP MFA
                                                               ▼
                                                   PostgreSQL (schema eis_platform)
 ai-service/ (FastAPI, health endpoint only)
```
Today the backend is a **modular monolith** with one package per domain under `modules/`, enforced by `LayeredArchitectureTest`. It is not split into the microservices in section 6. Mapping between modules and applications: [repository-architecture.md](repository-architecture.md#3-where-each-application-lives).
