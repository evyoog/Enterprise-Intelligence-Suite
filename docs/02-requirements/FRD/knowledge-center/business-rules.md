# Business rules — Knowledge Center

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-KCEN-001 | Every list, hub, recommendation, search result, count and download in the Knowledge Center applies [BR-KVS-001](../../../03-business-rules/BR-KVS-001-knowledge-visibility.md) before ranking or counting. Content counts on product cards count only what the reader may see. | backend | REQ-KNW-005 |
| BR-KCEN-002 | A ticket created from a knowledge page is prefilled only with fields the reader may see (current content, product, module, query, error code, role); the reader can edit or remove any of them before sending. | frontend, backend | REQ-KNW-005.9 |
| BR-KCEN-003 | Feedback: one Yes/No per reader per content version (a second vote replaces the first); a No needs a reason; comments are 1–1,000 characters (proposed). | backend | REQ-KNW-005.13 |
| BR-KCEN-004 | Bookmarks, recently viewed and learning progress are per user and never shown to others. | backend | REQ-KNW-005.8 |
| BR-KCEN-005 | Glossary links are added at render time from GLOSSARY_TERM items the reader may see; the stored text is unchanged. | frontend | REQ-KNW-005.21 |
| BR-KCEN-006 | A "direct action" link goes only to an EIS route; it is shown only if the reader can open that page (route permission check), otherwise hidden. | frontend | REQ-KNW-005.12 |
