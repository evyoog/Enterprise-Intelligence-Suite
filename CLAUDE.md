# eVyoog Enterprise Intelligence Suite (EIS)

## Project
Cloud PaaS platform hosting the eVyoog product suites (Valam.ai, Varthan.ai, Thittam.ai, Thiran.ai, Yukth.ai, Tharav.ai) on a shared platform layer: catalog, registration and tenancy, subscriptions, identity (Keycloak SSO, MFA, SAML federation), RBAC and privileged access, dashboards, notifications and audit.

One product = one repository. Requirements, code, database scripts, tests and deployment config all live here.

## Components
| Folder | What | Stack |
|--------|------|-------|
| `frontend/` | Web app | React 19, TypeScript, Vite, MUI, oidc-client-ts, i18next, Vitest |
| `backend/` | Platform API (`/api`, port 8081) | Java 21, Spring Boot 3.2.5, Maven, Spring Security (OAuth2 resource server), JPA |
| `ai-service/` | AI service (port 8000) | Python, FastAPI, pytest |
| `database/` | Migrations, seed data, views, functions, procedures | PostgreSQL 16, schema `eis_platform` |
| `docs/` | Product documentation, ordered by lifecycle | Markdown, Mermaid, OpenAPI |
| `test-cases/` | QA test cases | Markdown |
| `deployment/` | Docker, AWS, ECS, nginx, per-environment config | |

Identity provider: Keycloak at `https://user.evyoog.com`, realm `eVyoog`.

## Development rules
1. Read the relevant requirements under `/docs` before implementing.
2. Do not implement functionality that is not defined or approved (FRD status **Approved**).
3. Business rules must be documented before implementation.
4. Database changes require migration scripts in `database/migrations/`.
5. Every new feature requires test cases in `test-cases/`.
6. Do not modify existing business logic without checking its dependencies.
7. Do not hard-code credentials. Secrets come from environment variables or a secrets manager.
8. Run tests before creating a PR (`scripts/build.sh` runs everything CI runs).

## Requirement sources
| What | Where |
|------|-------|
| Business context | `docs/01-business/` |
| Feature requirements (FRD) | `docs/02-requirements/FRD/<feature>/` |
| Business rules | `docs/03-business-rules/` (cross-feature) and `FRD/<feature>/business-rules.md` |
| Workflows | `docs/04-workflows/` |
| UI | `docs/05-ui/` |
| API | `docs/06-api/` |
| Database | `docs/07-database/` |
| Architecture | `docs/08-architecture/` |
| Test cases | `test-cases/` |

## Feature traceability
Every feature uses the same kebab-case name everywhere:

```
docs/02-requirements/FRD/<feature>/       requirement, business rules, workflow, UI, API, acceptance criteria
frontend/src/...                          UI implementation
backend/src/main/java/com/vyoog/eisplatform/modules/<feature>/   controller → service → repository
database/migrations/V<NNN>__<description>.sql
test-cases/functional/<feature>/TC-<APP-CODE>-<NNN>.md
```

Flow: Requirement → FRD → Business rules → UI → API → Database → Frontend → Backend → Test cases → UAT.

Start a new feature by copying `docs/02-requirements/FRD/_template/`.

## Backend rules
- Java 21, Spring Boot 3.2.5, Maven, PostgreSQL, REST, Keycloak JWTs.
- One package per domain under `modules/`, layered `controller → service → repository` (enforced by `LayeredArchitectureTest`).
- Before creating a new API:
  1. Check the API requirements in `docs/06-api/` and the feature's FRD.
  2. Check existing controllers.
  3. Check existing services.
  4. Check existing entities and repositories.
  5. Reuse existing functionality where possible.
- Schema: Flyway is currently **disabled**. The live schema is `backend/src/main/resources/db/schema.sql`, applied by hand. Add every change there **and** as a migration in `database/migrations/`.
- Test: `cd backend && mvn -B verify`.

## Frontend rules
- React, TypeScript, Vite, MUI.
- API calls go through `frontend/src/api/`. Do not duplicate API calls.
- All user-facing text goes through i18n (`frontend/src/i18n/locales/`).
- Before creating a component:
  1. Check existing components in `frontend/src/components/`.
  2. Check the UI requirements in `docs/05-ui/`.
  3. Reuse existing components.
  4. Follow the existing design system (`theme.ts`, `theming/`).
  5. Do not duplicate API calls.
- Test: `cd frontend && npm run lint && npm test && npm run build`. Lint currently has pre-existing errors and is non-blocking in CI until they are fixed. Do not add new ones.

## AI service rules
- FastAPI app in `ai-service/app/`, tests in `ai-service/tests/`.
- Test: `cd ai-service && pytest`.

## Local development
```bash
docker compose up -d      # PostgreSQL, with the eis_platform schema loaded on first start
scripts/dev.sh            # Postgres + backend + frontend (+ ai-service if its .venv exists)
scripts/build.sh          # build and test everything
```

## Branches
Work on `dev`. Do not push directly to `main`.
