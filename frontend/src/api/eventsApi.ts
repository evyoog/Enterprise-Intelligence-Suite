import { apiRequest } from './client'

export type EventStatus = 'PENDING' | 'DELIVERED' | 'FAILED'

export interface PlatformEventSummary {
  id: number
  eventId: string
  eventType: string
  aggregateType: string
  aggregateId: string
  occurredAt: string
  status: EventStatus
  attempts: number
  nextAttemptAt: string | null
  lastError: string | null
}

export interface PlatformEventDetail extends PlatformEventSummary {
  deliveredAt: string | null
  payload: string
  receipts: { handler: string; processedAt: string }[]
}

export interface PlatformEventPage {
  items: PlatformEventSummary[]
  totalElements: number
  page: number
  size: number
}

export interface PlatformEventFilter {
  type?: string
  status?: EventStatus | ''
  from?: string
  to?: string
  page?: number
}

/** REQ-INT-002.9 (C91, EV-1 B): re-send the delivered events of a time range (UTC dates, inclusive) to one receiver. */
export interface ReplayRequest {
  receiver: string
  from: string
  to: string
  eventType?: string
  preview: boolean
}

export interface ReplayResult {
  preview: boolean
  receiver: string
  matched: number
  replayed: number
  failed: number
  limit: number
  failures: { eventId: string; eventType: string; error: string }[]
}

/** REQ-INT-002 (C91, EV-3 and EV-4): how long delivered events are kept and who is told when one fails for good. */
export interface EventPlatformSettings {
  retentionDays: number
  alertRecipients: string[]
  alertInApp: boolean
  alertEmail: boolean
  updatedAt: string
}

export interface EventSummaryCounts {
  pending: number
  failed: number
}

/** REQ-INT-002.6 — AdminEventController (`MANAGE_INTEGRATIONS`). */
export const adminEventsApi = {
  list: (filter: PlatformEventFilter = {}) => {
    const query = new URLSearchParams()
    if (filter.type) query.set('type', filter.type)
    if (filter.status) query.set('status', filter.status)
    if (filter.from) query.set('from', filter.from)
    if (filter.to) query.set('to', filter.to)
    query.set('page', String(filter.page ?? 0))
    return apiRequest<PlatformEventPage>(`/admin/events?${query.toString()}`)
  },
  types: () => apiRequest<string[]>('/admin/events/types'),
  get: (id: number) => apiRequest<PlatformEventDetail>(`/admin/events/${id}`),
  retry: (id: number) => apiRequest<PlatformEventDetail>(`/admin/events/${id}/retry`, { method: 'POST' }),
  summary: () => apiRequest<EventSummaryCounts>('/admin/events/summary'),
  receivers: () => apiRequest<string[]>('/admin/events/receivers'),
  replay: (body: ReplayRequest) => apiRequest<ReplayResult>('/admin/events/replay', { method: 'POST', body: JSON.stringify(body) }),
  settings: () => apiRequest<EventPlatformSettings>('/admin/events/settings'),
  updateSettings: (body: { retentionDays?: number; alertRecipients?: string[]; alertInApp?: boolean; alertEmail?: boolean }) =>
    apiRequest<EventPlatformSettings>('/admin/events/settings', { method: 'PUT', body: JSON.stringify(body) }),
}
