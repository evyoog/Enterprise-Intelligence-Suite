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
}

// Matches PlatformPrivilegedAccessController — ADMIN-only (MANAGE_PRIVILEGED_ACCESS).
export const platformPrivilegedAccessApi = {
  listPending: () => apiRequest<PrivilegedAccessRequest[]>('/admin/privileged-access/pending'),

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
