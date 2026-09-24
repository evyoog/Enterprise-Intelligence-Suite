import { apiRequest } from './client'

export type NotificationCategory = 'SECURITY' | 'SUBSCRIPTION' | 'ORGANIZATION' | 'PRIVILEGED_ACCESS' | 'SYSTEM'
export type NotificationSeverity = 'INFO' | 'WARNING' | 'CRITICAL'

export interface Notification {
  id: number
  category: NotificationCategory
  severity: NotificationSeverity
  title: string
  message: string
  read: boolean
  createdAt: string
}

export interface NotificationSummary {
  unreadCount: number
  notifications: Notification[]
}

export interface NotificationPreferences {
  emailDisabledCategories: NotificationCategory[]
}

// Matches NotificationController — authenticated only (/me/**).
export const notificationsApi = {
  list: () => apiRequest<NotificationSummary>('/me/notifications'),

  markRead: (id: number) => apiRequest<undefined>(`/me/notifications/${id}/read`, { method: 'PUT' }),

  markAllRead: () => apiRequest<undefined>('/me/notifications/read-all', { method: 'PUT' }),

  getPreferences: () => apiRequest<NotificationPreferences>('/me/notifications/preferences'),

  updatePreferences: (preferences: NotificationPreferences) =>
    apiRequest<NotificationPreferences>('/me/notifications/preferences', {
      method: 'PUT',
      body: JSON.stringify(preferences),
    }),
}
