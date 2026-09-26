import { apiRequest } from './client'

export interface PrivilegedAccessAuditEntry {
  eventType: string
  actorKeycloakSub: string
  note?: string
  createdAt: string
}

export interface PrivilegedAccessRequest {
  id: number
  scope: 'PLATFORM' | 'ORGANIZATION'
  organizationId?: number
  permissionName: string
  justification: string
  status: string
  effectiveStatus: string
  requestedAt: string
  requestedDurationMinutes: number
  decidedAt?: string
  decidedByKeycloakSub?: string
  decisionNote?: string
  expiresAt?: string
  auditTrail: PrivilegedAccessAuditEntry[]
  /** Null when the requester has no customer account. */
  requesterEmail?: string | null
}

// Matches PlatformPrivilegedAccessController — ADMIN-only (MANAGE_PRIVILEGED_ACCESS).
export const platformPrivilegedAccessApi = {
  listPending: () => apiRequest<PrivilegedAccessRequest[]>('/admin/privileged-access/pending'),
  // REQ-IAM-004.7: approved, unexpired grants — so one can be revoked early.
  listActive: () => apiRequest<PrivilegedAccessRequest[]>('/admin/privileged-access/active'),

  approve: (id: number, note?: string) =>
    apiRequest<PrivilegedAccessRequest>(`/admin/privileged-access/${id}/approve`, {
      method: 'POST',
      body: JSON.stringify({ note }),
    }),

  reject: (id: number, note?: string) =>
    apiRequest<PrivilegedAccessRequest>(`/admin/privileged-access/${id}/reject`, {
      method: 'POST',
      body: JSON.stringify({ note }),
    }),

  revoke: (id: number, note?: string) =>
    apiRequest<PrivilegedAccessRequest>(`/admin/privileged-access/${id}/revoke`, {
      method: 'POST',
      body: JSON.stringify({ note }),
    }),
}
