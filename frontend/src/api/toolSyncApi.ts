import { apiRequest } from './client'

export type TenantStatus = 'PENDING' | 'READY' | 'FAILED'
export type DeliveryStatus = 'PENDING' | 'DELIVERED' | 'FAILED'
export type ConnectorStatus = 'ACTIVE' | 'PAUSED'

export interface SyncTenant {
  organizationId: number
  organizationName: string | null
  status: TenantStatus
  schemaVersion: string | null
  lastSyncedAt: string | null
  lastError: string | null
  pending: number
  failed: number
  lastDeliveredAt: string | null
}

export interface SyncConnector {
  id: number
  productCode: string
  baseMcpUrl: string
  clientId: string
  status: ConnectorStatus
  tenants: SyncTenant[]
}

export interface ToolDelivery {
  id: number
  eventId: string
  connectorId: number
  productCode: string | null
  organizationId: number
  eventType: string
  aggregateType: string
  aggregateId: string
  status: DeliveryStatus
  attempts: number
  nextAttemptAt: string | null
  lastError: string | null
  sentVersion: number | null
  createdAt: string
  deliveredAt: string | null
}

export interface ToolDeliveryPage {
  items: ToolDelivery[]
  totalElements: number
  page: number
  size: number
}

export interface ReconcileType {
  aggregateType: string
  platformCount: number
  toolCount: number | null
  inSync: boolean
  resent: number
  extraInTool: number
}

export interface ReconcileResult {
  organizationId: number
  productCode: string
  reachable: boolean
  problem: string | null
  inSync: boolean
  resent: number
  types: ReconcileType[]
}

export interface ActionResult {
  message: string
}

/** REQ-INT-003 — AdminToolSyncController (`MANAGE_INTEGRATIONS`, every action audited). */
export const toolSyncApi = {
  overview: () => apiRequest<SyncConnector[]>('/admin/events/tool-sync/overview'),
  deliveries: (filter: { status?: DeliveryStatus | ''; organizationId?: number; page?: number } = {}) => {
    const query = new URLSearchParams()
    if (filter.status) query.set('status', filter.status)
    if (filter.organizationId) query.set('organizationId', String(filter.organizationId))
    query.set('page', String(filter.page ?? 0))
    return apiRequest<ToolDeliveryPage>(`/admin/events/tool-sync/deliveries?${query.toString()}`)
  },
  retry: (id: number) => apiRequest<ActionResult>(`/admin/events/tool-sync/deliveries/${id}/retry`, { method: 'POST' }),
  replay: (id: number) => apiRequest<ActionResult>(`/admin/events/tool-sync/deliveries/${id}/replay`, { method: 'POST' }),
  pause: (connectorId: number) => apiRequest<ActionResult>(`/admin/events/tool-sync/connectors/${connectorId}/pause`, { method: 'POST' }),
  resume: (connectorId: number) => apiRequest<ActionResult>(`/admin/events/tool-sync/connectors/${connectorId}/resume`, { method: 'POST' }),
  start: (organizationId: number, connectorId: number) =>
    apiRequest<ActionResult>(`/admin/events/tool-sync/tenants/${organizationId}/${connectorId}/start`, { method: 'POST' }),
  reconcile: (organizationId: number, connectorId: number, repair = true) =>
    apiRequest<ReconcileResult>(`/admin/events/tool-sync/tenants/${organizationId}/${connectorId}/reconcile?repair=${repair}`, { method: 'POST' }),
}
