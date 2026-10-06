# Workflow — Semantic Search

## Passage lifecycle
| From | To | Actor | Condition / rule | Side effects |
|------|----|-------|------------------|--------------|
| — | Pending (no vector) | System | A public record is indexed or its text changes (BR-SEM-002, BR-SEM-003) | — |
| Pending | Embedded | System (background, or every minute) | The model answers (BR-SEM-008) | — |
| Pending | Pending | System | The model is down — retried next minute | Admin page shows the pending count |
| Embedded | Pending | Platform admin | Rebuild and re-embed (model changed) | All vectors cleared, then embedded again |
| Any | Deleted | System | The record is deleted or no longer public (BR-SEM-001) | — |

## Hybrid search
1. Keyword search (REQ-PRT-002).
2. If BR-SEM-005 allows: embed the query (400 ms) → nearest 40 passages of public records → keep best passage per record with similarity ≥ threshold.
3. Merge (BR-SEM-004); on any model failure use keyword results only (BR-SEM-006).
