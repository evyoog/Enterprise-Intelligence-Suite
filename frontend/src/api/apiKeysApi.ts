import { apiRequest } from './client'

export type ApiKeyStatus = 'ACTIVE' | 'REVOKED' | 'EXPIRED'

/** Chip colour per key status (key section and admin page). */
export const API_KEY_STATUS_COLOR: Record<ApiKeyStatus, 'success' | 'default' | 'warning'> = {
  ACTIVE: 'success', REVOKED: 'default', EXPIRED: 'warning',
}

/** C92: a key is read-only or read-write; keys without a scope behave as read-only. */
export type ApiKeyScope = 'READ' | 'READ_WRITE'

export interface ApiKey {
  id: number
  name: string
  prefix: string
  status: ApiKeyStatus
  scope: ApiKeyScope
  /** A legacy key that belongs to a person rather than to an organization. */
  personal: boolean
  createdByName?: string | null
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
  organizationId: number | null
  organizationName: string | null
  scope: ApiKeyScope
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

/** REQ-INT-001.1 — ApiKeyController: only the legacy personal keys remain here (C92). */
export const apiKeysApi = {
  list: () => apiRequest<ApiKey[]>('/me/api-keys'),
  revoke: (id: number) => apiRequest<ApiKey>(`/me/api-keys/${id}/revoke`, { method: 'POST' }),
}

/** REQ-INT-001.1 (C92) — OrganizationApiKeyController: keys belong to the organization (`MANAGE_ORGANIZATION`). */
export const organizationApiKeysApi = {
  list: () => apiRequest<ApiKey[]>('/organization/me/api-keys'),
  create: (body: { name: string; scope: ApiKeyScope; expiresAt?: string | null }) =>
    apiRequest<ApiKey>('/organization/me/api-keys', { method: 'POST', body: JSON.stringify(body) }),
  revoke: (id: number) => apiRequest<ApiKey>(`/organization/me/api-keys/${id}/revoke`, { method: 'POST' }),
}

/** REQ-INT-001.4 — AdminApiKeyController (`MANAGE_INTEGRATIONS`). */
export const adminApiKeysApi = {
  list: (page = 0) => apiRequest<AdminApiKeyPage>(`/admin/api-keys?page=${page}`),
}
