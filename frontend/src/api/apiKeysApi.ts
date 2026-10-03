import { apiRequest } from './client'

export type ApiKeyStatus = 'ACTIVE' | 'REVOKED' | 'EXPIRED'

/** Chip colour per key status (key section and admin page). */
export const API_KEY_STATUS_COLOR: Record<ApiKeyStatus, 'success' | 'default' | 'warning'> = {
  ACTIVE: 'success', REVOKED: 'default', EXPIRED: 'warning',
}

export interface ApiKey {
  id: number
  name: string
  prefix: string
  status: ApiKeyStatus
  createdAt: string
  expiresAt: string | null
  lastUsedAt: string | null
  requestCount: number
  /** Only in the create response — shown once (REQ-INT-001 BR-1). */
  key?: string
}

export interface AdminApiKey {
  id: number
  ownerCustomerId: number
  ownerEmail: string | null
  name: string
  prefix: string
  status: ApiKeyStatus
  createdAt: string
  expiresAt: string | null
  lastUsedAt: string | null
  requestCount: number
}

export interface AdminApiKeyPage {
  items: AdminApiKey[]
  totalElements: number
  page: number
  size: number
}

/** REQ-INT-001.1 — ApiKeyController (the signed-in user's own keys). */
export const apiKeysApi = {
  list: () => apiRequest<ApiKey[]>('/me/api-keys'),
  create: (body: { name: string; expiresAt?: string | null }) =>
    apiRequest<ApiKey>('/me/api-keys', { method: 'POST', body: JSON.stringify(body) }),
  revoke: (id: number) => apiRequest<ApiKey>(`/me/api-keys/${id}/revoke`, { method: 'POST' }),
}

/** REQ-INT-001.4 — AdminApiKeyController (`MANAGE_INTEGRATIONS`). */
export const adminApiKeysApi = {
  list: (page = 0) => apiRequest<AdminApiKeyPage>(`/admin/api-keys?page=${page}`),
}
