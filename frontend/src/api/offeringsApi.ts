import { apiRequest } from './client'

/** REQ-CAT-005 Offering management. Browsing is public; the admin calls need MANAGE_CATALOG. */
export type OfferingStatus = 'DRAFT' | 'ACTIVE' | 'RETIRED'
export type ProductAudience = 'BOTH' | 'INDIVIDUAL' | 'ORGANIZATION'

export interface OfferingProduct { id: number; name: string; status: string; imageUrl?: string }

export interface Offering {
  id: number
  name: string
  description?: string
  status: OfferingStatus
  products: OfferingProduct[]
  createdAt: string
  updatedAt: string
}

export interface OfferingInput {
  name: string
  description?: string
  status?: OfferingStatus
  productIds: number[]
}

export interface ProductRule {
  productId: number
  productName: string
  productStatus: string
  audience: ProductAudience
  worksWithProductIds: number[]
}

export interface ProductRuleInput { audience: ProductAudience; worksWithProductIds: number[] }

export interface WorksWith { id: number; name: string }

export const offeringsApi = {
  list: () => apiRequest<Offering[]>('/offerings'),
  get: (id: number) => apiRequest<Offering>(`/offerings/${id}`),
  worksWith: (productId: number) => apiRequest<WorksWith[]>(`/products/${productId}/works-with`),
}

export const adminOfferingsApi = {
  list: () => apiRequest<Offering[]>('/admin/offerings'),
  create: (input: OfferingInput) => apiRequest<Offering>('/admin/offerings', { method: 'POST', body: JSON.stringify(input) }),
  update: (id: number, input: OfferingInput) => apiRequest<Offering>(`/admin/offerings/${id}`, { method: 'PUT', body: JSON.stringify(input) }),
  remove: (id: number) => apiRequest<void>(`/admin/offerings/${id}`, { method: 'DELETE' }),
  rules: () => apiRequest<ProductRule[]>('/admin/offerings/product-rules'),
  setRules: (productId: number, input: ProductRuleInput) =>
    apiRequest<ProductRule>(`/admin/offerings/product-rules/${productId}`, { method: 'PUT', body: JSON.stringify(input) }),
}
