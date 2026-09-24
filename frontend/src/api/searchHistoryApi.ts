import { apiRequest } from './client'

export interface SearchHistoryEntry {
  query: string
  searchedAt: string
}

// Matches SearchHistoryController — authenticated only (/me/**). An
// anonymous visitor simply never calls these; ProductsPage only wires them
// up once `auth.isAuthenticated` is true.
export const searchHistoryApi = {
  list: () => apiRequest<SearchHistoryEntry[]>('/me/search-history'),

  record: (query: string) =>
    apiRequest<undefined>('/me/search-history', { method: 'POST', body: JSON.stringify({ query }) }),

  clear: () => apiRequest<undefined>('/me/search-history', { method: 'DELETE' }),
}
