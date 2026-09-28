import { apiRequest } from './client'

export type OrderStatus = 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'CANCELLED'

export interface Order {
  id: number
  productId: number
  productName: string
  planId?: number
  planName?: string
  status: OrderStatus
  requestedByCustomerId: number
  requestedByName: string
  decidedByCustomerId?: number
  decidedByName?: string
  decidedAt?: string
  decisionNote?: string
  createdAt: string
}

// 09 Order & Provisioning Management (sprint 2027.1.1) — organization
// purchasing only. Covered by the "/organization/me/**" authenticated()
// rule; ORG_ADMIN-only actions (approve/reject/pending) are gated
// server-side inside OrderService.
export const ordersApi = {
  submit: (productId: number, planId: number | null) =>
    apiRequest<Order>('/organization/me/orders', { method: 'POST', body: JSON.stringify({ productId, planId }) }),
  myOrders: () => apiRequest<Order[]>('/organization/me/orders'),
  pendingOrders: () => apiRequest<Order[]>('/organization/me/orders/pending'),
  approve: (orderId: number, note?: string) =>
    apiRequest<Order>(`/organization/me/orders/${orderId}/approve`, { method: 'POST', body: JSON.stringify({ note }) }),
  reject: (orderId: number, note?: string) =>
    apiRequest<Order>(`/organization/me/orders/${orderId}/reject`, { method: 'POST', body: JSON.stringify({ note }) }),
  cancel: (orderId: number) =>
    apiRequest<Order>(`/organization/me/orders/${orderId}/cancel`, { method: 'POST' }),
}
