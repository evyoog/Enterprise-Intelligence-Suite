# Business rules — Semantic Search

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-SEM-001 | Only PUBLIC search documents (ACTIVE products, PUBLISHED articles) get passages and embeddings. A passage of a document that becomes private is deleted. The semantic query also filters `visibility = PUBLIC`. | backend (`SemanticIndexService`, `SearchSqlRepository`) | C70 |
| BR-SEM-002 | Chunking: text is split into sentences; sentences are packed into passages of at most `chunk-size` characters (600); each passage after the first starts with the previous passage's last sentences, up to `chunk-overlap` characters (100); every passage starts with the title. A product's passage text is its category, tags, platforms and description. | backend (`Chunker`) | REQ-PRT-003.2 |
| BR-SEM-003 | A passage keeps its vector while its text (SHA-256) is unchanged; changed text clears the vector until it is embedded again. A vector is only stored if the passage did not change meanwhile. | backend | REQ-PRT-003.3 |
| BR-SEM-004 | Hybrid merge: score = Σ 1/(k + rank) over the keyword and semantic lists (k = 60); exact-ID matches first; then by score. A semantic match needs cosine similarity ≥ 0.45 (stub model; re-tune with the real model). | backend (`HybridMerger`, `SemanticSearchService`) | REQ-PRT-003.4–.5 |
| BR-SEM-005 | Semantic search is skipped for keyword mode, a blank query, a quoted phrase, an exact ID, and type TICKET (`NOT_APPLICABLE`). | backend | C70 |
| BR-SEM-006 | Fallback: the query embedding has 400 ms; on timeout, error or wrong vector size, keyword results are returned with `UNAVAILABLE`. Without configuration or pgvector: `DISABLED`. | backend (`EmbeddingClient`) | REQ-PRT-003.6 |
| BR-SEM-007 | Vectors have 384 dimensions; a model with another size is reported "not available" and is not used. | backend, ai-service | C70 |
| BR-SEM-008 | Saving a record never waits for the model: passages are embedded on a background thread after commit; failures leave them pending for the next minute's retry. | backend (`SearchChangeListener`, `SearchIndexJobs`) | REQ-PRT-003.3 |
