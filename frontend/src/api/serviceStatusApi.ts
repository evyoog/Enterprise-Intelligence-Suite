import { apiRequest } from './client'

// REQ-PRT-001 interim service status page (decisions C20, C26).
export type ServiceStatusValue = 'OPERATIONAL' | 'DEGRADED' | 'PARTIAL_OUTAGE' | 'MAJOR_OUTAGE' | 'MAINTENANCE'

export const SERVICE_STATUS_VALUES: ServiceStatusValue[] = ['OPERATIONAL', 'DEGRADED', 'PARTIAL_OUTAGE', 'MAJOR_OUTAGE', 'MAINTENANCE']

export interface ProductStatus {
  productId: number
  productName: string
  status: ServiceStatusValue
  note?: string | null
  updatedAt?: string | null
  /** The viewer's organization or account has an active subscription. */
  purchased: boolean
  openIncidents: number
  /** C101 (SR-3 B): INSTANCES = derived from the instances' recent health reports; MANUAL = set by an administrator (or no instance reported recently). */
  source?: 'INSTANCES' | 'MANUAL'
  reportingInstances?: number
  downInstances?: number
  degradedInstances?: number
}

export interface Incident {
  id: number
  productId: number
  productName: string
  title: string
  message: string
  startedAt: string
  endedAt?: string | null
  open: boolean
}

export interface ServiceStatusPage {
  /** The app.status-page.enabled setting. */
  enabled: boolean
  products: ProductStatus[]
  /** Customer view: purchased products only. */
  incidents: Incident[]
}

export type IncidentPhase = 'LOGGED' | 'INVESTIGATING' | 'RESOLVED' | 'CLOSED'
export type IncidentSeverity = 'MINOR' | 'MAJOR' | 'CRITICAL'

export const INCIDENT_PHASES: IncidentPhase[] = ['LOGGED', 'INVESTIGATING', 'RESOLVED', 'CLOSED']
export const INCIDENT_SEVERITIES: IncidentSeverity[] = ['MINOR', 'MAJOR', 'CRITICAL']

export interface LinkedTicket {
  id: number
  subject: string
  status: string
  priority: string
}

/** C101 (IC-1 A): what a platform administrator sees: the public fields plus the internal ones. Never shown to customers. */
export interface AdminIncident extends Incident {
  severity: IncidentSeverity
  phase: IncidentPhase
  internalNotes?: string | null
  assignedToCustomerId?: number | null
  assignedToName?: string | null
  resolutionSummary?: string | null
  closedAt?: string | null
  problemId?: number | null
  problemTitle?: string | null
  tickets: LinkedTicket[]
}

/** The first five are the public fields. The rest are internal and optional (left out = unchanged); phase left out is worked out from endedAt. */
export interface IncidentPayload {
  productId: number
  title: string
  message: string
  startedAt: string
  endedAt?: string | null
  severity?: IncidentSeverity
  phase?: IncidentPhase
  internalNotes?: string
  assignedToCustomerId?: number
  resolutionSummary?: string
}

export const serviceStatusApi = {
  get: () => apiRequest<ServiceStatusPage>('/me/service-status'),
}

// MANAGE_SERVICE_STATUS (AdminServiceStatusController).
export const adminServiceStatusApi = {
  get: () => apiRequest<ServiceStatusPage>('/admin/service-status'),
  updateStatus: (productId: number, status: ServiceStatusValue, note?: string) =>
    apiRequest<ProductStatus>(`/admin/service-status/products/${productId}`, {
      method: 'PUT', body: JSON.stringify({ status, note: note?.trim() || undefined }),
    }),
  incidents: () => apiRequest<AdminIncident[]>('/admin/service-status/incidents'),
  incident: (id: number) => apiRequest<AdminIncident>(`/admin/service-status/incidents/${id}`),
  createIncident: (payload: IncidentPayload) =>
    apiRequest<AdminIncident>('/admin/service-status/incidents', { method: 'POST', body: JSON.stringify(payload) }),
  updateIncident: (id: number, payload: IncidentPayload) =>
    apiRequest<AdminIncident>(`/admin/service-status/incidents/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  linkTicket: (incidentId: number, ticketId: number) =>
    apiRequest<AdminIncident>(`/admin/service-status/incidents/${incidentId}/tickets/${ticketId}`, { method: 'POST' }),
  unlinkTicket: (incidentId: number, ticketId: number) =>
    apiRequest<AdminIncident>(`/admin/service-status/incidents/${incidentId}/tickets/${ticketId}`, { method: 'DELETE' }),
}
