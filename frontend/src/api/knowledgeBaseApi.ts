import { apiRequest } from './client'

export type ArticleStatus = 'DRAFT' | 'PUBLISHED'

export interface KnowledgeArticle {
  id: number
  title: string
  body: string
  status: ArticleStatus
  version: number
  updatedAt: string
}

export interface KnowledgeArticleInput {
  title: string
  body: string
}

// 11.01 Knowledge Base (sprint 2027.1.1) — public reads, no auth needed.
export const knowledgeBaseApi = {
  search: (query?: string) => apiRequest<KnowledgeArticle[]>(`/knowledge-base/articles${query ? `?q=${encodeURIComponent(query)}` : ''}`),
  get: (id: number) => apiRequest<KnowledgeArticle>(`/knowledge-base/articles/${id}`),
}

// ADMIN-only on the backend (MANAGE_KNOWLEDGE_BASE) — see SecurityConfig.
export const adminKnowledgeBaseApi = {
  listAll: () => apiRequest<KnowledgeArticle[]>('/admin/knowledge-base/articles'),
  get: (id: number) => apiRequest<KnowledgeArticle>(`/admin/knowledge-base/articles/${id}`),
  create: (payload: KnowledgeArticleInput) =>
    apiRequest<KnowledgeArticle>('/admin/knowledge-base/articles', { method: 'POST', body: JSON.stringify(payload) }),
  update: (id: number, payload: KnowledgeArticleInput) =>
    apiRequest<KnowledgeArticle>(`/admin/knowledge-base/articles/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  publish: (id: number) => apiRequest<KnowledgeArticle>(`/admin/knowledge-base/articles/${id}/publish`, { method: 'POST' }),
  unpublish: (id: number) => apiRequest<KnowledgeArticle>(`/admin/knowledge-base/articles/${id}/unpublish`, { method: 'POST' }),
  delete: (id: number) => apiRequest<undefined>(`/admin/knowledge-base/articles/${id}`, { method: 'DELETE' }),
}
