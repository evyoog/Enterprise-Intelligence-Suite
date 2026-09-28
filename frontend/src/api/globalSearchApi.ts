import { apiRequest } from './client'

export type SearchResultType = 'PRODUCT' | 'KNOWLEDGE' | 'TICKET'

export interface SearchResultItem {
  type: SearchResultType
  id: number
  title: string
  snippet?: string
}

export interface GlobalSearchResult {
  products: SearchResultItem[]
  knowledgeArticles: SearchResultItem[]
  tickets: SearchResultItem[]
}

// 01.03.01 Unified Search (sprint 2027.1.3) — keyword only, no semantic
// search (no embeddings/vector-store infrastructure exists — see the
// backend's own GlobalSearchService javadoc). Public; ticket results only
// appear for a signed-in caller.
export const globalSearchApi = {
  search: (q: string, type?: SearchResultType) => {
    const params = new URLSearchParams()
    if (q) params.set('q', q)
    if (type) params.set('type', type)
    const qs = params.toString()
    return apiRequest<GlobalSearchResult>(`/search${qs ? `?${qs}` : ''}`)
  },
}
