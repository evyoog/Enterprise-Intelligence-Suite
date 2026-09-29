import { apiRequest } from './client'

export interface RecommendedProduct {
  id: number
  name: string
  category?: string
  imageUrl?: string
  launchUrl?: string
}

export interface Recommendations {
  featured: RecommendedProduct[]
  popular: RecommendedProduct[]
}

// 03.01.02 Recommendations (sprint 2027.1.2) — public, no auth needed.
export const recommendationsApi = {
  get: () => apiRequest<Recommendations>('/products/recommendations'),
}
