# API requirements — Knowledge Base

| Method | Path | Purpose | Permission | Success | Errors |
|--------|------|---------|------------|---------|--------|
| GET | `/knowledge-base/articles` | Search published articles (`?q=`) | Public | 200 (`KnowledgeArticleDto[]`) | - |
| GET | `/knowledge-base/articles/{id}` | Read one published article | Public | 200 (`KnowledgeArticleDto`) | 404 (not published or nonexistent) |
| GET | `/admin/knowledge-base/articles` | List every article, any status | `MANAGE_KNOWLEDGE_BASE` | 200 (`KnowledgeArticleDto[]`) | 403 |
| GET | `/admin/knowledge-base/articles/{id}` | Read one article, any status | `MANAGE_KNOWLEDGE_BASE` | 200 (`KnowledgeArticleDto`) | 403, 404 |
| POST | `/admin/knowledge-base/articles` | Create (BR-KNW-001) | `MANAGE_KNOWLEDGE_BASE` | 200 (`KnowledgeArticleDto`) | 400, 403 |
| PUT | `/admin/knowledge-base/articles/{id}` | Edit (BR-KNW-002) | `MANAGE_KNOWLEDGE_BASE` | 200 (`KnowledgeArticleDto`) | 400, 403, 404 |
| POST | `/admin/knowledge-base/articles/{id}/publish` | Publish (BR-KNW-003) | `MANAGE_KNOWLEDGE_BASE` | 200 (`KnowledgeArticleDto`) | 403, 404 |
| POST | `/admin/knowledge-base/articles/{id}/unpublish` | Unpublish (BR-KNW-003) | `MANAGE_KNOWLEDGE_BASE` | 200 (`KnowledgeArticleDto`) | 403, 404 |
| DELETE | `/admin/knowledge-base/articles/{id}` | Delete | `MANAGE_KNOWLEDGE_BASE` | 200 | 403, 404 |

## Request / response
```json
// GET /knowledge-base/articles?q=password
[
  { "id": 4, "title": "Resetting your password", "body": "Use the forgot-password link.", "status": "PUBLISHED", "version": 2, "updatedAt": "2027-01-04T10:00:00Z" }
]
```
```json
// POST /admin/knowledge-base/articles
{ "title": "Getting started", "body": "How to sign in to your account." }
```

OpenAPI contract: not maintained separately — `KnowledgeArticleController`/`AdminKnowledgeArticleController`/`KnowledgeArticleDto` and this file are the source of truth ([C14](../../../01-business/roadmap/open-decisions.md#c14)).
