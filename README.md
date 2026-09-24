# Enterprise-Intelligence-Suite

eVyoog Enterprise Intelligence Suite (EIS) is a Platform hosted in the cloud and offered as Platform as a Service (PaaS) hosting multiple product suites (Valam.ai, Varthan.ai, Thittam.ai, Thiran.ai, Yukth.ai, Tharav.ai), plus the shared platform layer (IAM, security, notifications, analytics) they all depend on.

This repository is **one product**: code, documentation, database scripts, test cases and deployment configuration together, so the whole product context is in one place.

```
                    EIS Platform
                         │
       ┌─────────────────┼─────────────────┐
       ▼                 ▼                 ▼
   FRONTEND          BACKEND           AI SERVICE
    React          Spring Boot          FastAPI
       └─────────────────┼─────────────────┘
                         ▼
                 DATABASE (PostgreSQL)
                         ▼
                 PRODUCT DOCUMENTS
            ┌────────────┼────────────┐
            ▼            ▼            ▼
           FRD     Business Rules  Workflows
            └────────────┼────────────┘
                         ▼
                    TEST CASES
```

## Repository structure

```
Enterprise-Intelligence-Suite/
├── frontend/                  # React + TypeScript + Vite web app
├── backend/                   # Spring Boot platform API
├── ai-service/                # Python/FastAPI AI service
│   ├── app/
│   ├── tests/
│   └── requirements.txt
├── docs/                      # Product documentation, ordered by lifecycle
│   ├── 01-business/           # BRD/, vision.md, scope.md
│   ├── 02-requirements/       # FRD/<feature>/, functional-, non-functional-requirements/
│   ├── 03-business-rules/
│   ├── 04-workflows/
│   ├── 05-ui/                 # screen-requirements/, wireframes/
│   ├── 06-api/                # api-requirements/, openapi/
│   ├── 07-database/           # data-model/, ERD/, database-design.md
│   ├── 08-architecture/       # system-, frontend-, backend-architecture/, security/, deployment/
│   ├── 09-integrations/
│   ├── 10-user-manual/
│   └── 11-release-notes/
├── test-cases/                # functional/, integration/, api/, ui/, regression/, security/, UAT/
├── database/                  # migrations/, seed/, views/, functions/, procedures/
├── deployment/                # docker/, aws/, ecs/, nginx/, environments/{dev,uat,prod}/
├── scripts/                   # dev.sh, build.sh, deploy.sh
├── .devcontainer/             # GitHub Codespaces
├── .github/                   # workflows/, ISSUE_TEMPLATE/, pull_request_template.md
├── CLAUDE.md                  # Instructions for Claude Code
├── docker-compose.yml         # Local PostgreSQL (+ ai-service profile)
└── .gitignore
```

## Feature traceability

A feature keeps the same name across every layer:

```
docs/02-requirements/FRD/<feature>/     →  requirement, business rules, workflow, UI, API, acceptance criteria
database/migrations/V<NNN>__*.sql       →  schema change
frontend/src/…  ·  backend/…/modules/<feature>/  →  implementation
test-cases/functional/<feature>/TC-*.md →  verification  →  UAT
```

Start a new feature by copying `docs/02-requirements/FRD/_template/`.

## Getting started

Prerequisites: Java 21, Maven, Node 22, Python 3.12, Docker. Or open the repo in a GitHub Codespace (`.devcontainer/`), which sets all of these up.

```bash
docker compose up -d     # PostgreSQL with the eis_platform schema
scripts/dev.sh           # run backend (:8081/api), frontend (:5173), ai-service (:8000)
scripts/build.sh         # build and test everything, as CI does
```

Component details: [`frontend/README.md`](frontend/README.md), [`backend/README.md`](backend/README.md), [`ai-service/README.md`](ai-service/README.md).

## Workflow

Business discussion → approved requirement in `docs/` → GitHub issue → implementation on `dev` → test cases → PR → GitHub Actions → UAT → production.
