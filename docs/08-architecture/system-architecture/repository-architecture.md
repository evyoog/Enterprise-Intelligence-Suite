# Repository architecture

How the folders in this repository fit together. One product = one repository: the plan, requirements, code, database, tests and deployment all live here and link to each other.

## 1. Folder map
```
Enterprise-Intelligence-Suite/
├── CLAUDE.md · README.md · docker-compose.yml · .gitignore
├── .github/                        workflows/ (frontend-ci, backend-ci, deploy) · ISSUE_TEMPLATE/ · pull_request_template.md
├── .devcontainer/                  GitHub Codespaces
│
├── docs/                           ── PLAN & SPECIFY (ordered by lifecycle) ──────────────────────────
│   ├── README.md                   documentation index
│   ├── 01-business/
│   │   ├── source-documents/       [PO] [CG] [WB]: the original planning documents (read-only)
│   │   ├── roadmap/
│   │   │   ├── README.md           PI → sprint → application index
│   │   │   ├── sprints/            SPRINT-<PI.N>.md (10 sprints) · _template.md
│   │   │   ├── applications/       <NN>-<application>.md (16 EIS) · thittam-*.md · thiran-*.md
│   │   │   ├── roadmap.csv         72 roadmap rows (planning-roadmap-template columns)
│   │   │   ├── open-decisions.md   conflicts C1–C15, decisions needed
│   │   │   └── EIS-document-analysis.md
│   │   ├── BRD/ · vision.md · scope.md · planning-roadmap-template.xlsx
│   ├── 02-requirements/            FRD/<feature>/ (from _template/) · functional-/non-functional-requirements/
│   ├── 03-business-rules/  04-workflows/  05-ui/  06-api/  07-database/
│   ├── 08-architecture/            system-architecture/ (this file, platform-architecture.md) · frontend-/backend-architecture/ · security/ · deployment/
│   ├── 09-integrations/  10-user-manual/  11-release-notes/
│
├── frontend/                       ── BUILD ─────────────────────────────────────────────────────────
├── backend/                        Spring Boot: modules/<domain>/controller → service → repository
├── ai-service/                     FastAPI
├── database/                       migrations/ · seed/ · views/ · functions/ · procedures/
│
├── test-cases/                     ── VERIFY ── functional/ · integration/ · api/ · ui/ · regression/ · security/ · UAT/
│
├── deployment/                     ── RUN ── docker/ · aws/ · ecs/ · nginx/ · environments/{dev,uat,prod}/
└── scripts/                        dev.sh · build.sh · deploy.sh
```

## 2. Traceability flow through the folders
```
docs/01-business/source-documents/           [PO] roadmap row, [WB] function ID
        │
        ▼
docs/01-business/roadmap/sprints/SPRINT-<PI.N>.md ─► roadmap/applications/<NN>-<app>.md
        │                                               capability → feature → function
        ▼
docs/02-requirements/FRD/<feature>/  ─►  REQ-<APP-CODE>-<NNN>.md  ─►  docs/03-business-rules/ · 04-workflows/ · 05-ui/ · 06-api/ · 07-database/
        │
        ▼
GitHub issue (User Story, "Sprint (PI.Sprint)") ─► branch dev ─► PR (pull_request_template: FTR/REQ/DES/TC/STORY)
        │
        ▼
backend/…/modules/<domain>/ · frontend/src/… · ai-service/app/ · database/migrations/V<NNN>__*.sql
        │
        ▼
test-cases/<type>/<feature>/TC-<APP-CODE>-<NNN>.md ─► .github/workflows ─► deployment/environments/{dev,uat,prod} ─► docs/11-release-notes/
```

## 3. Where each application lives
The sprint is from [PO]. The code column is observed on branch `dev`, at module level. "Not started" means there is no module in the repository yet.

| ID | Application | Sprint | Roadmap page | Backend (`backend/src/main/java/com/vyoog/eisplatform/`) | Frontend (`frontend/src/`) |
|----|-------------|--------|--------------|------------------------------------------------------------|----------------------------|
| 01 | Experience & Customer Portal | 2026.3.3 | [01](../../01-business/roadmap/applications/01-experience-customer-portal.md) | `modules/dashboard`, `modules/notification`, `modules/preference` | `pages/HomePage.tsx`, `pages/BusinessDashboardPage.tsx`, `pages/PreferencesPage.tsx`, `components/layout/` |
| 02 | Product & Catalog Management | 2026.4.1 | [02](../../01-business/roadmap/applications/02-product-catalog-management.md) | `modules/product`, `modules/platform` | `pages/ProductsPage.tsx`, `pages/admin/*Product*`, `pages/admin/*Platform*` |
| 03 | Marketplace | 2027.1.3 | [03](../../01-business/roadmap/applications/03-marketplace.md) | Not started | Not started |
| 04 | AI Advisor & Agent Platform | 2027.1.2 | [04](../../01-business/roadmap/applications/04-ai-advisor-agent-platform.md) | Not started (`ai-service/` skeleton only) | Not started |
| 05 | Customer / Tenant Management | 2026.4.2 | [05](../../01-business/roadmap/applications/05-customer-tenant-management.md) | `modules/registration` (organization, members, seats) | `pages/register/`, `pages/admin/RegistrationsAdminPage.tsx` |
| 06 | Identity & Access Management | 2026.3.3 | [06](../../01-business/roadmap/applications/06-identity-access-management.md) | `modules/auth`, `modules/authorization`, `modules/federation`, `config/` | `auth/`, `pages/SecuritySettingsPage.tsx`, `pages/OrganizationSamlProvidersPage.tsx`, `pages/admin/AdminPrivilegedAccessPage.tsx` |
| 07 | Subscription & Entitlement Management | 2026.4.3 | [07](../../01-business/roadmap/applications/07-subscription-entitlement-management.md) | `modules/registration` (subscriptions, product access) | `pages/MyProductsPage.tsx` |
| 08 | Billing & Payments | 2026.4.3 | [08](../../01-business/roadmap/applications/08-billing-payments.md) | Not started | Not started |
| 09 | Order & Provisioning Management | 2027.1.1 | [09](../../01-business/roadmap/applications/09-order-provisioning-management.md) | Not started | Not started |
| 10 | Service & Resource Management | 2027.1.1 | [10](../../01-business/roadmap/applications/10-service-resource-management.md) | Not started | Not started |
| 11 | Training & Knowledge Management | 2027.1.3 | [11](../../01-business/roadmap/applications/11-training-knowledge-management.md) | Not started | Not started |
| 12 | Support & Service Management | 2027.1.3 | [12](../../01-business/roadmap/applications/12-support-service-management.md) | Not started | Not started |
| 13 | Integration & API Platform | 2027.1.1 | [13](../../01-business/roadmap/applications/13-integration-api-platform.md) | Not started | Not started |
| 14 | Partner & Provider Management | 2027.2.1 | [14](../../01-business/roadmap/applications/14-partner-provider-management.md) | Not started | Not started |
| 15 | Administration & Governance | 2027.2.2 | [15](../../01-business/roadmap/applications/15-administration-governance.md) | `modules/audit` | `pages/admin/AdminAuditLogPage.tsx`, `pages/admin/settings/` |
| 16 | Analytics & Data Platform | 2027.1.1 | [16](../../01-business/roadmap/applications/16-analytics-data-platform.md) | Not started | Not started |

The hosted SaaS products (Thittam: Macro Planner and Agile Planner; Thiran: SW Life Cycle) are planned in the roadmap. They have no code in this repository, and [PO] does not say which repository will hold them (Not specified).

## 4. Naming conventions
| Artifact | Location | Name |
|----------|----------|------|
| Sprint | `docs/01-business/roadmap/sprints/` | `SPRINT-<PI.Sprint>.md`, for example `SPRINT-2026.4.1.md` |
| Application page | `docs/01-business/roadmap/applications/` | `<NN>-<application-kebab>.md` for EIS; `<product>-<application>.md` for SaaS |
| Feature FRD | `docs/02-requirements/FRD/` | `<feature-kebab>/` (copy `_template/`) |
| Requirement / story | `docs/02-requirements/functional-requirements/` | `REQ-<APP-CODE>-<NNN>.md` / `STORY-<APP-CODE>-<NNN>.md` |
| Design / ADR | `docs/08-architecture/system-architecture/` | `DES-<APP-CODE>-<NNN>.md` / `ADR-<NNN>-<slug>.md` |
| Migration | `database/migrations/` | `V<NNN>__<description>.sql` |
| Test plan / case | `test-cases/` | `TESTPLAN-<APP-CODE>-<NNN>.md` / `<type>/<feature>/TC-<APP-CODE>-<NNN>.md` |
| Release notes | `docs/11-release-notes/` | `vX.Y.Z.md` or `YYYY.PI.Sprint.md` |

`<APP-CODE>` values have not been assigned yet. The only example in the templates is `APP-CAT` / `CAT` in `planning-roadmap-template.xlsx`. See [open-decisions.md](../../01-business/roadmap/open-decisions.md).
