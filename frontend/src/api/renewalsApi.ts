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
  /** C100 RR-1 B: set while the term has ended and the 7-day read-only grace period runs. */
  graceEndsAt?: string | null
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

/** C100 RR-2 B / RR-5: one person who gets the organization's renewal reminders. */
export interface ReminderRecipient {
  customerId: number
  name: string
  email: string
  reason: 'ORG_ADMIN' | 'MANAGE_BILLING' | 'ADDED'
  emailEnabled: boolean
  inAppEnabled: boolean
}

/** `defaults` always get reminders (read-only); `added` are chosen by an administrator. */
export interface ReminderRecipients {
  defaults: ReminderRecipient[]
  added: ReminderRecipient[]
}

export interface SaveReminderRecipientsBody {
  recipients: { customerId: number; email: boolean; inApp: boolean }[]
}

/** Needs the organization's Manage billing permission. */
export const reminderRecipientsApi = {
  get: () => apiRequest<ReminderRecipients>('/organization/me/renewal-reminder-recipients'),
  save: (body: SaveReminderRecipientsBody) =>
    apiRequest<ReminderRecipients>('/organization/me/renewal-reminder-recipients', { method: 'PUT', body: JSON.stringify(body) }),
}
