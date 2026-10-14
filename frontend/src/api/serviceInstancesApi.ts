import { apiRequest } from './client'

// REQ-SRM-001 service instances (decision C101).
export type InstanceStatus = 'PROVISIONING' | 'ACTIVE' | 'SUSPENDED' | 'FAILED' | 'DEPROVISIONED'
export type InstanceHealth = 'HEALTHY' | 'DEGRADED' | 'DOWN' | 'UNKNOWN'
export type InstanceChangedBy = 'SYSTEM' | 'PRODUCT' | 'ADMIN'

export const INSTANCE_STATUSES: InstanceStatus[] = ['PROVISIONING', 'ACTIVE', 'SUSPENDED', 'FAILED', 'DEPROVISIONED']
export const INSTANCE_HEALTHS: InstanceHealth[] = ['HEALTHY', 'DEGRADED', 'DOWN', 'UNKNOWN']

export interface ServiceInstance {
  id: number
  subscriptionId: number
  productId: number
  productName: string
  ownerType: 'ORGANIZATION' | 'INDIVIDUAL'
  ownerOrganizationId?: number | null
  ownerName?: string | null
  status: InstanceStatus
  health: InstanceHealth
  healthMessage?: string | null
  lastReportedAt?: string | null
  /** The last report is older than 5 minutes (or there is none). */
  stale: boolean
  tenantReference?: string | null
  accessUrl?: string | null
  changedBy: InstanceChangedBy
  changeNote?: string | null
  createdAt: string
}

export interface InstanceSummary {
  total: number
  healthy: number
  degraded: number
  down: number
  unknown: number
  noRecentReport: number
}

export interface InstanceFilters {
  productId?: number
  status?: InstanceStatus
  health?: InstanceHealth
}

/** At least one of status / health; the note is required and audited. */
export interface InstanceChangePayload {
  status?: InstanceStatus
  health?: InstanceHealth
  note: string
}

export interface ReporterKey {
  id: number
  productId: number
  productName: string
  label?: string | null
  keyPrefix: string
  createdAt: string
  lastUsedAt?: string | null
  revokedAt?: string | null
}

/** The secret is returned once, on creation, and never again. */
export interface CreatedReporterKey {
  key: ReporterKey
  secret: string
}

function query(params: Record<string, string | number | undefined>) {
  const q = new URLSearchParams()
  for (const [k, v] of Object.entries(params)) if (v !== undefined && v !== '') q.set(k, String(v))
  const s = q.toString()
  return s ? `?${s}` : ''
}

// MANAGE_SERVICE_STATUS (AdminServiceInstancesController).
export const adminServiceInstancesApi = {
  list: (filters: InstanceFilters = {}) =>
    apiRequest<ServiceInstance[]>(`/admin/service-status/instances${query({ ...filters })}`),
  summary: (productId?: number) =>
    apiRequest<InstanceSummary>(`/admin/service-status/instances/summary${query({ productId })}`),
  change: (id: number, payload: InstanceChangePayload) =>
    apiRequest<ServiceInstance>(`/admin/service-status/instances/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  reporterKeys: () => apiRequest<ReporterKey[]>('/admin/service-status/reporter-keys'),
  createReporterKey: (productId: number, label?: string) =>
    apiRequest<CreatedReporterKey>('/admin/service-status/reporter-keys', {
      method: 'POST', body: JSON.stringify({ productId, label: label?.trim() || undefined }),
    }),
  revokeReporterKey: (id: number) =>
    apiRequest<ReporterKey>(`/admin/service-status/reporter-keys/${id}`, { method: 'DELETE' }),
}

// Organization administrator: read-only view of the organization's own instances.
export const organizationServiceInstancesApi = {
  list: () => apiRequest<ServiceInstance[]>('/organization/me/service-instances'),
}
