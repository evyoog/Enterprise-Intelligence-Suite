import { apiRequest } from './client'

/**
 * REQ-TEN-010 (C103, "application access"): a member asking for their own
 * access to a product their organization already subscribes to, and
 * removing their own access immediately. All endpoints are authenticated
 * and scoped to the caller's own organization; the admin-only endpoints
 * (listForOrganization/approve/reject) need "Manage product access" or
 * being an org admin — the backend, not this file, enforces that and
 * returns 403 otherwise.
 */
export type AccessRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'

export interface AccessRequestDto {
  id: number
  productId: number
  productName: string
  memberId: number
  memberName: string
  status: AccessRequestStatus
  message?: string | null
  createdAt: string
  decidedByName?: string | null
  decidedAt?: string | null
  decisionNote?: string | null
}

export const accessRequestApi = {
  request: (productId: number, message?: string) =>
    apiRequest<AccessRequestDto>('/organization/me/access-requests', { method: 'POST', body: JSON.stringify({ productId, message }) }),
  myRequests: () => apiRequest<AccessRequestDto[]>('/organization/me/access-requests'),
  cancel: (id: number) =>
    apiRequest<AccessRequestDto>(`/organization/me/access-requests/${id}/cancel`, { method: 'POST' }),
  removeMyAccess: (productId: number) =>
    apiRequest<undefined>(`/organization/me/products/${productId}/access`, { method: 'DELETE' }),
  // Admin side — needs "Manage product access" or being an org admin.
  listForOrganization: () => apiRequest<AccessRequestDto[]>('/organization/me/access-requests/all'),
  approve: (id: number) =>
    apiRequest<AccessRequestDto>(`/organization/me/access-requests/${id}/approve`, { method: 'POST' }),
  reject: (id: number, note: string) =>
    apiRequest<AccessRequestDto>(`/organization/me/access-requests/${id}/reject`, { method: 'POST', body: JSON.stringify({ note }) }),
}
