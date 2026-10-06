import { apiRequest } from './client'

export interface SearchIndexRun {
  id: number
  trigger: 'BACKFILL' | 'ADMIN'
  status: 'RUNNING' | 'DONE' | 'FAILED'
  documents: number
  chunks: number
  embedded: number
  error?: string | null
  startedAt: string
  finishedAt?: string | null
}

export interface EmbeddingStatus {
  configured: boolean
  available: boolean
  provider?: string | null
  model?: string | null
  dimension?: number | null
  message?: string | null
}

export interface SearchIndexStatus {
  engine: 'POSTGRES' | 'BASIC'
  keywordIndexInstalled: boolean
  semanticInstalled: boolean
  documents: Record<string, number>
  chunks: number
  embeddedChunks: number
  pendingChunks: number
  embedding: EmbeddingStatus
  lastRun?: SearchIndexRun | null
  settings: Record<string, number>
}

export interface QueryCount {
  query: string
  count: number
}

export interface SearchInsights {
  days: number
  totalSearches: number
  zeroResultSearches: number
  zeroResultRate: number
  semanticUsedRate: number
  averageTookMs: number
  p95TookMs: number
  topQueries: QueryCount[]
  topZeroResultQueries: QueryCount[]
}

export interface SynonymGroup {
  id: number
  terms: string[]
  createdAt: string
}

// C70: search administration — MANAGE_SEARCH.
export const searchAdminApi = {
  status: () => apiRequest<SearchIndexStatus>('/admin/search/index'),
  rebuild: (reembed = false) =>
    apiRequest<undefined>(`/admin/search/index/rebuild${reembed ? '?reembed=true' : ''}`, { method: 'POST' }),
  insights: (days: number) => apiRequest<SearchInsights>(`/admin/search/insights?days=${days}`),
  synonyms: () => apiRequest<SynonymGroup[]>('/admin/search/synonyms'),
  createSynonym: (terms: string[]) =>
    apiRequest<SynonymGroup>('/admin/search/synonyms', { method: 'POST', body: JSON.stringify({ terms }) }),
  deleteSynonym: (id: number) => apiRequest<undefined>(`/admin/search/synonyms/${id}`, { method: 'DELETE' }),
}
