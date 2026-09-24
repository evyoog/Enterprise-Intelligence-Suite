# Documentation index

Product documentation for eVyoog EIS, ordered by the product lifecycle. Start with the roadmap to see what is planned for each sprint, then follow the links down to requirements, design and tests.

| Section | What is here | Start with |
|---------|--------------|------------|
| [01-business](01-business/) | Vision, scope, BRDs, planning sources, **roadmap and sprints** | [roadmap/README.md](01-business/roadmap/README.md) · [sprints](01-business/roadmap/sprints/README.md) · [applications](01-business/roadmap/applications/README.md) · [source documents](01-business/source-documents/README.md) · [vision](01-business/vision.md) · [scope](01-business/scope.md) |
| [02-requirements](02-requirements/) | FRDs per feature, requirements, user stories, NFRs | [FRD/README.md](02-requirements/FRD/README.md) |
| [03-business-rules](03-business-rules/) | Cross-feature business rules | [README](03-business-rules/README.md) |
| [04-workflows](04-workflows/) | End-to-end process flows | [README](04-workflows/README.md) |
| [05-ui](05-ui/) | Screen requirements, wireframes | [screen-requirements](05-ui/screen-requirements/README.md) |
| [06-api](06-api/) | API requirements, OpenAPI specs | [api-requirements](06-api/api-requirements/README.md) |
| [07-database](07-database/) | Data model, ERD, database design | [database-design.md](07-database/database-design.md) |
| [08-architecture](08-architecture/) | System, frontend, backend, security, deployment architecture | [platform-architecture.md](08-architecture/system-architecture/platform-architecture.md) · [repository-architecture.md](08-architecture/system-architecture/repository-architecture.md) |
| [09-integrations](09-integrations/) | External systems | [README](09-integrations/README.md) |
| [10-user-manual](10-user-manual/) | User and admin guides | [README](10-user-manual/README.md) |
| [11-release-notes](11-release-notes/) | Release notes per release | [README](11-release-notes/README.md) |

Test cases live outside `docs/`, in [`/test-cases`](../test-cases/).

## Traceability
```
source-documents → roadmap/sprints/SPRINT-<PI.N> → roadmap/applications/<app> → capability → feature → function
  → 02-requirements/FRD/<feature>/ → REQ → STORY (Sprint = PI.N) → code → test-cases/TC → UAT → 11-release-notes
```
