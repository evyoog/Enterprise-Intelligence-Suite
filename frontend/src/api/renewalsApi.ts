import { apiRequest } from './client'

/** REQ-SUB-004.8 — the signed-in user's renewal reminder settings. */
export interface RenewalReminderPreferences {
  enabled: boolean
  /** null = the platform default. */
  daysBefore: number | null
  sendTime: string | null
  effectiveDaysBefore: number
  effectiveSendTime: string
  effectiveTimeZone: string
  platformDaysBefore: number
  platformSendTime: string
  minDays: number
  maxDays: number
}

export interface Renewal {
  subscriptionId: number
  productName: string
  planName: string | null
  autoRenew: boolean
  renewalDate: string
  remindersEnabled: boolean
  /** In the user's time zone, ISO with offset; null when off or none left. */
  nextReminderAt: string | null
}

/** REQ-SUB-004.4/.5 — platform defaults (`MANAGE_BILLING`). */
export interface RenewalReminderDefaults {
  daysBefore: number
  sendTime: string
  timeZone: string
  updatedAt?: string
}

export const renewalsApi = {
  preferences: () => apiRequest<RenewalReminderPreferences>('/me/notification-preferences/renewal-reminders'),
  savePreferences: (body: { enabled: boolean; daysBefore: number | null; sendTime: string | null }) =>
    apiRequest<RenewalReminderPreferences>('/me/notification-preferences/renewal-reminders', { method: 'PUT', body: JSON.stringify(body) }),
  myRenewals: () => apiRequest<Renewal[]>('/me/renewals'),
}

export const adminRenewalsApi = {
  defaults: () => apiRequest<RenewalReminderDefaults>('/admin/billing/settings/renewal-reminders'),
  saveDefaults: (body: Omit<RenewalReminderDefaults, 'updatedAt'>) =>
    apiRequest<RenewalReminderDefaults>('/admin/billing/settings/renewal-reminders', { method: 'PUT', body: JSON.stringify(body) }),
}
