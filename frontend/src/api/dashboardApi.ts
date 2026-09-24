import { apiRequest } from './client'
import type { Organization, SubscriptionStatus } from './registrationApi'

export interface DashboardProduct {
  productId: number
  productName: string
  category?: string
  launchUrl?: string
  /** null = never subscribed (individual) / org never subscribed (org member). */
  subscriptionStatus: SubscriptionStatus | null
  /** null for an individual caller — the concept only applies to org members. */
  myAccessAssigned: boolean | null
  myProductRole?: string
  favorite: boolean
  lastLaunchedAt?: string
  launchCount: number
}

export interface DashboardAlert {
  type: string
  severity: 'info' | 'warning' | 'error' | string
  message: string
}

// Same shape registrationApi's Organization already returns — this endpoint
// just also includes the org's MFA policy (see OrganizationDto on the backend).
export type DashboardOrganization = Organization & { mfaRequired: boolean }

export interface DashboardPreferences {
  widgetOrder: string[]
  hiddenWidgets: string[]
}

export interface Dashboard {
  organization: DashboardOrganization | null
  products: DashboardProduct[]
  alerts: DashboardAlert[]
  preferences: DashboardPreferences
}

// Authenticated — always the caller's own dashboard (see DashboardController).
export const dashboardApi = {
  get: () => apiRequest<Dashboard>('/me/dashboard'),
  addFavorite: (productId: number) =>
    apiRequest<undefined>(`/me/dashboard/favorites/${productId}`, { method: 'POST' }),
  removeFavorite: (productId: number) =>
    apiRequest<undefined>(`/me/dashboard/favorites/${productId}`, { method: 'DELETE' }),
  // Fired when the Launch button is clicked — the entire real basis for
  // "recently used"/"frequently used" (see DashboardService's own javadoc).
  recordLaunch: (productId: number) =>
    apiRequest<undefined>(`/me/dashboard/launches/${productId}`, { method: 'POST' }),
  updatePreferences: (preferences: DashboardPreferences) =>
    apiRequest<DashboardPreferences>('/me/dashboard/preferences', { method: 'PUT', body: JSON.stringify(preferences) }),
}
