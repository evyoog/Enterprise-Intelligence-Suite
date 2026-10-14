import { apiRequest } from './client'
import type { TicketPriority } from './supportApi'

// C101 / REQ-SUP-002 — SLA targets and business hours for support tickets.
// ADMIN-only on the backend (MANAGE_SUPPORT_TICKETS).

export type BusinessDay = 'MON' | 'TUE' | 'WED' | 'THU' | 'FRI' | 'SAT' | 'SUN'

export interface SlaPolicy {
  priority: TicketPriority
  firstResponseMinutes: number
  resolutionMinutes: number
  aroundTheClock: boolean
}

export interface SlaSettings {
  businessDays: BusinessDay[]
  /** "HH:mm" */
  businessStart: string
  businessEnd: string
  timeZone: string
  /** 1-99: the share of a target after which the warning goes out. */
  warnAtPercent: number
  alertEmails: string[]
}

export interface SlaConfig {
  policies: SlaPolicy[]
  settings: SlaSettings
}

export const adminSlaApi = {
  get: () => apiRequest<SlaConfig>('/admin/support/sla'),
  updatePolicies: (policies: SlaPolicy[]) =>
    apiRequest<SlaConfig>('/admin/support/sla/policies', { method: 'PUT', body: JSON.stringify({ policies }) }),
  updateSettings: (settings: SlaSettings) =>
    apiRequest<SlaConfig>('/admin/support/sla/settings', { method: 'PUT', body: JSON.stringify(settings) }),
}
