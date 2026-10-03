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
}
