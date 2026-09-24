import { apiRequest } from './client'

export interface AuditLogEntry {
  id: number
  timestamp: string
  action: string
  actorCustomerId?: number
  actorEmail?: string
  targetType?: string
  targetId?: string
  organizationId?: number
  outcome: 'SUCCESS' | 'FAILURE'
  detail?: string
}

export interface AuditLogPage {
  items: AuditLogEntry[]
  totalElements: number
  page: number
  size: number
}

export interface AuditLogSearchParams {
  organizationId?: number
  actorCustomerId?: number
  action?: string
  page?: number
  size?: number
}

function queryString(params: AuditLogSearchParams): string {
  const query = new URLSearchParams()
  if (params.organizationId !== undefined) query.set('organizationId', String(params.organizationId))
  if (params.actorCustomerId !== undefined) query.set('actorCustomerId', String(params.actorCustomerId))
  if (params.action) query.set('action', params.action)
  query.set('page', String(params.page ?? 0))
  query.set('size', String(params.size ?? 50))
  return `?${query.toString()}`
}

// Matches AdminAuditLogController — ADMIN-only (VIEW_AUDIT_LOG).
export const auditLogApi = {
  search: (params: AuditLogSearchParams = {}) =>
    apiRequest<AuditLogPage>(`/admin/audit-logs${queryString(params)}`),
}

// Matches OrganizationAuditLogController — ORG_ADMIN-only (MANAGE_ORGANIZATION),
// always scoped to the caller's own organization server-side.
export const organizationAuditLogApi = {
  search: (params: Pick<AuditLogSearchParams, 'action' | 'page' | 'size'> = {}) =>
    apiRequest<AuditLogPage>(`/organization/me/audit-logs${queryString(params)}`),
}
