import { apiRequest } from './client'

export type ReviewStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export interface ProductReview {
  id: number
  productId: number
  productName: string
  customerId: number
  customerName: string
  rating: number
  comment?: string
  status: ReviewStatus
  createdAt: string
}

export interface ProductRatingSummary {
  averageRating: number | null
  reviewCount: number
  reviews: ProductReview[]
}

// 03.04.01 Reviews & Ratings (sprint 2027.1.3).
export const reviewsApi = {
  // Public — APPROVED reviews only.
  getRatings: (productId: number) => apiRequest<ProductRatingSummary>(`/products/${productId}/reviews`),
  // Authenticated — the caller's own review (upsert; always resets to PENDING).
  submit: (productId: number, rating: number, comment?: string) =>
    apiRequest<ProductReview>(`/me/products/${productId}/review`, { method: 'PUT', body: JSON.stringify({ rating, comment }) }),
  getMine: (productId: number) => apiRequest<ProductReview>(`/me/products/${productId}/review`),
}

// ADMIN-only on the backend (MANAGE_REVIEWS) — see SecurityConfig.
export const adminReviewsApi = {
  listAll: () => apiRequest<ProductReview[]>('/admin/reviews'),
  approve: (id: number) => apiRequest<ProductReview>(`/admin/reviews/${id}/approve`, { method: 'POST' }),
  reject: (id: number) => apiRequest<ProductReview>(`/admin/reviews/${id}/reject`, { method: 'POST' }),
}
