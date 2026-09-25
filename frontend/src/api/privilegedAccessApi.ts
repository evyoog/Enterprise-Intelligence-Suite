import { apiRequest } from './client'
import type { PrivilegedAccessRequest } from './platformPrivilegedAccessApi'

export interface RequestablePermission {
  permissionName: string
  scope: PrivilegedAccessRequest['scope']
  description?: string
}

export interface CreatePrivilegedAccessRequestPayload {
  permissionName: string
  justification: string
  /** 1–480, enforced server-side (PrivilegedAccessService). */
  durationMinutes: number
}

const decision = (note?: string) => ({ method: 'POST', body: JSON.stringify({ note }) })

// REQ-IAM-004 — the requester's own requests (PrivilegedAccessController).
export const myPrivilegedAccessApi = {
  // Decision C24: what the request form may offer this caller.
  listRequestable: () => apiRequest<RequestablePermission[]>('/me/privileged-access/requestable-permissions'),
  listMine: () => apiRequest<PrivilegedAccessRequest[]>('/me/privileged-access/requests'),
  request: (payload: CreatePrivilegedAccessRequestPayload) =>
    apiRequest<PrivilegedAccessRequest>('/me/privileged-access/requests', { method: 'POST', body: JSON.stringify(payload) }),
  withdraw: (id: number, note?: string) =>
    apiRequest<PrivilegedAccessRequest>(`/me/privileged-access/requests/${id}/revoke`, decision(note)),
}

// REQ-IAM-004 — ORGANIZATION-scope review, always the caller's own organization
// (OrganizationController, standing MANAGE_PRIVILEGED_ACCESS). The PLATFORM-scope
// equivalent is platformPrivilegedAccessApi.
export const organizationPrivilegedAccessApi = {
  listPending: () => apiRequest<PrivilegedAccessRequest[]>('/organization/me/privileged-access/pending'),
  approve: (id: number, note?: string) =>
    apiRequest<PrivilegedAccessRequest>(`/organization/me/privileged-access/${id}/approve`, decision(note)),
  reject: (id: number, note?: string) =>
    apiRequest<PrivilegedAccessRequest>(`/organization/me/privileged-access/${id}/reject`, decision(note)),
  revoke: (id: number, note?: string) =>
    apiRequest<PrivilegedAccessRequest>(`/organization/me/privileged-access/${id}/revoke`, decision(note)),
}
