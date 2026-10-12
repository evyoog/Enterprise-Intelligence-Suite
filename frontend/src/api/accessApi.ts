import { apiRequest } from './client'
import type { AuditLogPage } from './auditLogApi'

/** REQ-TEN-005 Access management (C90): role defaults, individual overrides, bulk changes and activity. */
export type AccessSource = 'LOCKED' | 'OVERRIDE' | 'ROLE' | 'PROJECT'

export interface PermissionCatalogEntry {
  code: string
  name: string
  description: string
  area: string
  callerCanGrant: boolean
}

export interface AccessMemberRow {
  memberId: number
  customerId: number
  name: string
  email?: string
  role: 'ORG_ADMIN' | 'MEMBER'
  status: string
  productsOn: number
  productsEntitled: number
  customAccessCount: number
  lastReviewedAt?: string | null
  lastReviewedBy?: string | null
  canChange: boolean
  lockedReason?: string | null
}

export interface AccessMemberPage {
  items: AccessMemberRow[]
  totalElements: number
  page: number
  size: number
}

export interface AccessProductItem {
  productId: number
  name: string
  plan?: string | null
  enabled: boolean
  wanted: boolean
  source: AccessSource
  productRole?: string | null
  seats: { inUse: number; quantity: number }
  lockedReason?: string | null
}

export interface AccessPermissionItem {
  code: string
  name: string
  description: string
  area: string
  enabled: boolean
  source: AccessSource
  lockedReason?: string | null
}

export interface AccessMemberDetail {
  memberId: number
  name: string
  email?: string
  role: 'ORG_ADMIN' | 'MEMBER'
  status: string
  lastReviewedAt?: string | null
  lastReviewedBy?: string | null
  canChange: boolean
  memberLockedReason?: string | null
  products: AccessProductItem[]
  permissions: AccessPermissionItem[]
}

export interface SetOverridesRequest {
  permissions?: Record<string, boolean>
  products?: Record<number, boolean>
  /** "permission:CODE" or "product:7" */
  reset?: string[]
}

export interface RoleDefaultPermission {
  code: string
  name: string
  description: string
  area: string
  enabled: boolean
  locked: boolean
  customized: boolean
}

export interface RoleDefaultProduct {
  productId: number
  name: string
  enabled: boolean
  customized: boolean
}

export interface RoleDefaults {
  role: 'ORG_ADMIN' | 'MEMBER'
  memberCount: number
  permissions: RoleDefaultPermission[]
  products: RoleDefaultProduct[]
}

export interface RoleDefaultsRequest {
  permissions?: Record<string, boolean>
  products?: Record<number, boolean>
}

export interface RoleDefaultImpactItem {
  item: string
  name: string
  from: boolean
  to: boolean
  affectedMembers: number
}

export interface RoleDefaultsResult {
  preview: boolean
  affectedMembers: number
  skippedNoSeat: number
  items: RoleDefaultImpactItem[]
  defaults: RoleDefaults
}

export interface BulkRequest {
  action: 'GRANT' | 'REMOVE'
  /** "product:7" or "permission:MANAGE_ORDERS" */
  item: string
  memberIds: number[]
  preview: boolean
}

export interface BulkResult {
  preview: boolean
  item: string
  itemName: string
  action: 'GRANT' | 'REMOVE'
  applied: number
  skipped: { memberId: number; name?: string | null; reason: string }[]
}

export interface AccessMemberFilters {
  page?: number
  size?: number
  q?: string
  role?: string
  productId?: number
  custom?: boolean
}

function query(filters: AccessMemberFilters): string {
  const p = new URLSearchParams()
  p.set('page', String(filters.page ?? 0))
  p.set('size', String(filters.size ?? 20))
  if (filters.q) p.set('q', filters.q)
  if (filters.role) p.set('role', filters.role)
  if (filters.productId !== undefined) p.set('productId', String(filters.productId))
  if (filters.custom) p.set('custom', 'true')
  return `?${p.toString()}`
}

// Authenticated; always the caller's own organization. Needs "Manage access" (checked by the backend).
export const accessApi = {
  permissions: () => apiRequest<PermissionCatalogEntry[]>('/organization/me/access/permissions'),
  members: (filters: AccessMemberFilters = {}) => apiRequest<AccessMemberPage>(`/organization/me/access/members${query(filters)}`),
  member: (memberId: number) => apiRequest<AccessMemberDetail>(`/organization/me/access/members/${memberId}`),
  setOverrides: (memberId: number, body: SetOverridesRequest) =>
    apiRequest<AccessMemberDetail>(`/organization/me/access/members/${memberId}/overrides`, { method: 'PUT', body: JSON.stringify(body) }),
  resetAll: (memberId: number) =>
    apiRequest<AccessMemberDetail>(`/organization/me/access/members/${memberId}/overrides`, { method: 'DELETE' }),
  roleDefaults: (role: string) => apiRequest<RoleDefaults>(`/organization/me/access/roles/${role}/defaults`),
  saveRoleDefaults: (role: string, body: RoleDefaultsRequest, preview: boolean) =>
    apiRequest<RoleDefaultsResult>(`/organization/me/access/roles/${role}/defaults?preview=${preview}`, { method: 'PUT', body: JSON.stringify(body) }),
  bulk: (body: BulkRequest) => apiRequest<BulkResult>('/organization/me/access/bulk', { method: 'POST', body: JSON.stringify(body) }),
  activity: (page = 0, size = 20) => apiRequest<AuditLogPage>(`/organization/me/access/activity?page=${page}&size=${size}`),
}
