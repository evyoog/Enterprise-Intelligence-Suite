# Business rules — Knowledge analytics

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-KANL-001 | An event is recorded only for content the reader was allowed to see (the same request that served it). | backend | REQ-KNW-006.1 |
| BR-KANL-002 | A view counts once per reader (or anonymous session) per item per 30 minutes (proposed). | backend | REQ-KNW-006.1 |
| BR-KANL-003 | Video completion = progress ≥ 95% (proposed); average watch time from seconds watched. | backend | REQ-KNW-006.3 |
| BR-KANL-004 | Analytics screens show aggregates only; no reader identities. | backend, frontend | REQ-KNW-006.6 |
