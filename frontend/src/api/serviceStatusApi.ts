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

export interface IncidentPayload {
  productId: number
  title: string
  message: string
  startedAt: string
  endedAt?: string | null
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
  createIncident: (payload: IncidentPayload) =>
    apiRequest<Incident>('/admin/service-status/incidents', { method: 'POST', body: JSON.stringify(payload) }),
  updateIncident: (id: number, payload: IncidentPayload) =>
    apiRequest<Incident>(`/admin/service-status/incidents/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
}
