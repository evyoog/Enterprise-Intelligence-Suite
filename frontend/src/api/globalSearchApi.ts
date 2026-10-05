import { apiRequest } from './client'

export type SearchResultType = 'PRODUCT' | 'KNOWLEDGE' | 'TICKET'

/** C70: which rule matched — shown as a label next to each result. */
export type SearchMatchType = 'EXACT_ID' | 'EXACT_PHRASE' | 'KEYWORD' | 'PARTIAL' | 'TYPO' | 'SEMANTIC'

/** USED: results include meaning matches. UNAVAILABLE: the model did not
 * answer, so only keyword results are shown. DISABLED: not configured.
 * NOT_APPLICABLE: keyword-only search (blank query, quotes, an ID, tickets). */
export type SemanticStatus = 'USED' | 'UNAVAILABLE' | 'DISABLED' | 'NOT_APPLICABLE'

export interface SearchHighlight {
  start: number
  length: number
}

export interface SearchResultItem {
  type: SearchResultType
  id: number
  title: string
  snippet?: string | null
  reference?: string
  matchType?: SearchMatchType
  score?: number
  titleHighlights?: SearchHighlight[]
  snippetHighlights?: SearchHighlight[]
}

export interface GlobalSearchResult {
  products: SearchResultItem[]
  knowledgeArticles: SearchResultItem[]
  tickets: SearchResultItem[]
  /** Every result, best first (C70). */
  results?: SearchResultItem[]
  didYouMean?: string | null
  semanticStatus?: SemanticStatus
  engine?: 'POSTGRES' | 'BASIC'
  tookMs?: number
}

export interface SearchSuggestion {
  type: SearchResultType
  id: number
  title: string
  reference: string
}

export interface SearchOptions {
  type?: SearchResultType
  mode?: 'hybrid' | 'keyword'
  /** Count this search in search insights (the results page sets it for searches the user ran). */
  track?: boolean
}

/** The ranked list, also for responses from before C70 (grouped lists only). */
export function rankedResults(result: GlobalSearchResult): SearchResultItem[] {
  return result.results ?? [...result.products, ...result.knowledgeArticles, ...result.tickets]
}

// 01.03.01 Unified Search — keyword search (REQ-PRT-002) and semantic
// search (REQ-PRT-003), C70. Public; ticket results only appear for a
// signed-in caller, and only their own tickets.
export const globalSearchApi = {
  search: (q: string, options: SearchOptions = {}) => {
    const params = new URLSearchParams()
    if (q) params.set('q', q)
    if (options.type) params.set('type', options.type)
    if (options.mode) params.set('mode', options.mode)
    if (options.track) params.set('track', 'true')
    const qs = params.toString()
    return apiRequest<GlobalSearchResult>(`/search${qs ? `?${qs}` : ''}`)
  },

  suggest: (q: string) => apiRequest<SearchSuggestion[]>(`/search/suggest?q=${encodeURIComponent(q)}`),
}

export const SEARCH_LINK_FOR: Record<SearchResultType, (id: number) => string> = {
  PRODUCT: (id) => `/products/${id}`,
  KNOWLEDGE: () => '/knowledge-base',
  TICKET: () => '/support/tickets',
}
