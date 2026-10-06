# Workflow — Global Search

Searching is a read. The search index changes only as a side effect of other features' changes, or by an admin rebuild.

## Index lifecycle
| From | To | Actor | Condition / rule | Side effects |
|------|----|-------|------------------|--------------|
| No index row | Indexed | System (after commit) | A searchable record is created, or becomes searchable (BR-SRCH-002) | Passages queued for embedding when public (REQ-PRT-003) |
| Indexed | Indexed (updated) | System (after commit) | The record changes | Changed passages lose their embedding until embedded again |
| Indexed | No index row | System (after commit) | The record is deleted, retired or unpublished | Passages deleted |
| Any | Rebuilt | Platform admin (`MANAGE_SEARCH`), or startup backfill when the index is empty | BR-SRCH-011 | `search_index_run` row; "Did you mean" word list refreshed |

## Search request
1. Parse the query (words, quoted phrases, ID).
2. Blank query or no index → basic engine (BR-SRCH-010).
3. Keyword query with visibility and type applied first (BR-SRCH-001), tiers BR-SRCH-003; if nothing matches, typo correction (BR-SRCH-006).
4. Hybrid mode: semantic search on public passages (REQ-PRT-003); merge; if the model fails, keyword results only.
5. Highlight, group, return; log for insights when `track=true` (BR-SRCH-009).
