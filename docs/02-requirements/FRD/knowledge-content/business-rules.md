# Business rules — Knowledge content

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-KCON-001 | A new item starts in Draft, version 0.1 (unpublished); the first publish is 1.0 (applied 2026-10-05, C78). | backend | REQ-KNW-002.5 |
| BR-KCON-002 | Allowed transitions only (see workflow); any other answers 409. | backend | REQ-KNW-002.5 |
| BR-KCON-003 | Only Published items whose effective date has passed and whose expiry date has not, and whose audience includes the reader (BR-KVS-001), are shown to readers or indexed for search. | backend | REQ-KNW-002.9, .11 |
| BR-KCON-004 | Publishing stores an immutable version snapshot (blocks and metadata); restoring copies a snapshot into a new draft and never changes history. | backend | REQ-KNW-002.6 |
| BR-KCON-005 | A media block stores the media item id; the reader receives a temporary URL only through the playback/download endpoints (BR-MED-001). | backend | REQ-KNW-002.2 |
| BR-KCON-006 | Slugs are unique per type; changing a slug keeps the old one redirecting (proposed). | backend | REQ-KNW-002.1 |
| BR-KCON-007 | Taxonomy rows in use cannot be deleted, only deactivated. | backend | REQ-KNW-002.4 |
| BR-KCON-008 | Every block is validated server-side against the block schema; HTML is never stored or rendered from user input (rich text is structured marks), so stored content cannot inject scripts. | backend, frontend | REQ-KNW-002.2 |

Cross-feature: [BR-KVS-001](../../../03-business-rules/BR-KVS-001-knowledge-visibility.md), [BR-MED-001](../../../03-business-rules/BR-MED-001-private-media-storage.md).
