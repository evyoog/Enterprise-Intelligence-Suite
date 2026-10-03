import { apiRequest } from './client'
import type { Product } from './productsApi'

/** C66: a platform as the public Product Catalog shows it. Counts, categories
 * and feature tags are derived by the backend from its ACTIVE apps. */
export interface CatalogPlatform {
  id: number
  name: string
  description?: string | null
  imageUrl?: string | null
  primaryColor?: string | null
  appCount: number
  categories: string[]
  featureTags: string[]
  /** Only on the detail endpoint. */
  apps?: Product[]
}

/** CatalogController — public, no sign-in needed. */
export const catalogApi = {
  listPlatforms: () => apiRequest<CatalogPlatform[]>('/catalog/platforms'),
  getPlatform: (id: number) => apiRequest<CatalogPlatform>(`/catalog/platforms/${id}`),
}
