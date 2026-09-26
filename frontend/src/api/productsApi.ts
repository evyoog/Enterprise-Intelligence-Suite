import { apiRequest } from './client'

export type ProductStatus = 'ACTIVE' | 'INACTIVE' | 'RETIRED'
export type BillingPeriod = 'MONTHLY' | 'YEARLY' | 'ONE_TIME'
export type Currency = 'USD' | 'EUR' | 'GBP' | 'INR'

export interface ProductPlan {
  id: number
  name: string
  price: number
  billingPeriod: BillingPeriod
  sortOrder?: number
  // 02.03 Plan Management (sprint 2026.4.1).
  currency: Currency
  usageLimit?: number
  includedFeatures?: string
  usagePrice?: number
  tierPricing?: string
  overageCharge?: number
}

export interface ProductPlanInput {
  name: string
  price: number
  billingPeriod: BillingPeriod
  sortOrder?: number
  currency?: Currency
  usageLimit?: number
  includedFeatures?: string
  usagePrice?: number
  tierPricing?: string
  overageCharge?: number
}

/** The slice of a Platform embedded on a Product — just enough for a "belongs to" chip. */
export interface PlatformSummary {
  id: number
  name: string
}

export interface Product {
  id: number
  name: string
  description?: string
  /** Legacy flat one-time price — only shown when `plans` is empty. */
  price: number
  imageUrl?: string
  /** Where the "Launch" button opens — the running app this product tile points to. */
  launchUrl?: string
  category?: string
  status: ProductStatus
  /** Admin-set — whether this app is wired into the Vyoog SSO bridge. */
  ssoConnected: boolean
  /** Which high-level platform(s) (e.g. Thittam) this app is assigned to. */
  platforms: PlatformSummary[]
  plans: ProductPlan[]
  // 02.01 Product Lifecycle & Structure (sprint 2026.4.1).
  version: number
  parentProductId?: number
  variantLabel?: string
  dependsOnProductIds: number[]
}

export interface ProductCreateRequest {
  name: string
  description?: string
  price: number
  imageUrl?: string
  launchUrl?: string
  category?: string
  status?: ProductStatus
  ssoConnected?: boolean
  platformIds?: number[]
  plans?: ProductPlanInput[]
  parentProductId?: number
  variantLabel?: string
  dependsOnProductIds?: number[]
}

export interface CategoryFacet {
  category: string
  count: number
}

export interface PlatformFacet {
  id: number
  name: string
  count: number
}

export interface ProductSearchParams {
  q?: string
  category?: string
  platformId?: number
  sortBy?: 'name' | 'price'
  sortDir?: 'asc' | 'desc'
}

export interface ProductSearchResponse {
  items: Product[]
  facets: {
    categories: CategoryFacet[]
    platforms: PlatformFacet[]
  }
}

function searchQueryString(params: ProductSearchParams): string {
  const query = new URLSearchParams()
  if (params.q) query.set('q', params.q)
  if (params.category) query.set('category', params.category)
  if (params.platformId !== undefined) query.set('platformId', String(params.platformId))
  if (params.sortBy) query.set('sortBy', params.sortBy)
  if (params.sortDir) query.set('sortDir', params.sortDir)
  const qs = query.toString()
  return qs ? `?${qs}` : ''
}

export const productsApi = {
  // Matches ProductController#listProducts on the backend — no auth needed,
  // it's the one endpoint SecurityConfig permits publicly. ACTIVE products only.
  list: () => apiRequest<Product[]>('/products'),

  // Matches ProductController#listAllProducts — ADMIN-only, every status.
  listAdmin: () => apiRequest<Product[]>('/products/admin'),

  // Matches ProductController#getProduct — public, same as `list`. Used to
  // prefill the edit form without needing the whole admin list in context.
  get: (id: number) => apiRequest<Product>(`/products/${id}`),

  // Matches ProductController#createProduct — ADMIN-only on the backend. The
  // access token is attached automatically by apiRequest via the token
  // provider AuthProvider registers; a non-admin token gets a 403 from the
  // server regardless of whether this function is even reachable in the UI.
  create: (payload: ProductCreateRequest) =>
    apiRequest<Product>('/products', { method: 'POST', body: JSON.stringify(payload) }),

  // Matches ProductController#updateProduct — ADMIN-only. Same payload shape
  // as create: the backend replaces every field (including the whole plans
  // list) with what's sent here, not a partial patch.
  update: (id: number, payload: ProductCreateRequest) =>
    apiRequest<Product>(`/products/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),

  // Matches ProductController#uploadImage — ADMIN-only. Returns the URL to use
  // as the product's imageUrl; upload happens as its own step, separate from
  // creating the product itself.
  uploadImage: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return apiRequest<{ url: string }>('/products/images', { method: 'POST', body: formData })
  },

  // Matches ProductController#deleteProduct — ADMIN-only. Rejected with a 409
  // (surfaced as an ApiError) if the product has any real subscription/access/
  // favorite/usage history — set status to INACTIVE instead in that case.
  delete: (id: number) => apiRequest<undefined>(`/products/${id}`, { method: 'DELETE' }),

  // 02.01.01.04 Publish product (sprint 2026.4.1) — ADMIN-only.
  publish: (id: number) => apiRequest<Product>(`/products/${id}/publish`, { method: 'POST' }),

  // 02.01.01.05 Retire product (sprint 2026.4.1) — ADMIN-only. Reversible via `publish`.
  retire: (id: number) => apiRequest<Product>(`/products/${id}/retire`, { method: 'POST' }),

  // Phase 17 — matches ProductController#searchProducts. Public, ACTIVE-only
  // (same visibility as `list`), but with a real backend query, category/
  // platform filters, sorting, and facet counts instead of the old
  // client-side substring filter.
  search: (params: ProductSearchParams = {}) =>
    apiRequest<ProductSearchResponse>(`/products/search${searchQueryString(params)}`),

  // Matches ProductController#searchAllProducts — ADMIN-only, every status.
  searchAdmin: (params: ProductSearchParams = {}) =>
    apiRequest<ProductSearchResponse>(`/products/admin/search${searchQueryString(params)}`),
}
