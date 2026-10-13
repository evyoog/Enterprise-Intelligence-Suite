import { apiRequest } from './client'

/** C94, REQ-MKT-004 (docs/06-api/api-requirements/wishlist.md): the signed-in user's saved products, as product ids, newest first. */
export const wishlistApi = {
  list: () => apiRequest<number[]>('/me/wishlist'),
  add: (productId: number) => apiRequest<number[]>(`/me/wishlist/${productId}`, { method: 'PUT' }),
  remove: (productId: number) => apiRequest<number[]>(`/me/wishlist/${productId}`, { method: 'DELETE' }),
}
