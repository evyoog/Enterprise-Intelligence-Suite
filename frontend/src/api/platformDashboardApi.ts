import { apiRequest } from './client'

export interface OrganizationsOverview {
  total: number
  active: number
}

export interface CatalogOverview {
  totalProducts: number
  activeProducts: number
  totalPlatforms: number
}

export interface SubscriptionsOverview {
  active: number
  byStatus: Record<string, number>
}

export interface PlatformBillingOverview {
  revenueThisPeriodByCurrency: Record<string, number>
  revenueLastPeriodByCurrency: Record<string, number>
  note: string
}

export interface ServiceHealth {
  platformStatus: string
  note: string
}

export interface TopProduct {
  productId: number
  productName: string
  totalLaunches: number
}

export interface PlatformDashboard {
  organizations: OrganizationsOverview
  catalog: CatalogOverview
  subscriptions: SubscriptionsOverview
  billing: PlatformBillingOverview
  openSupportTicketCount: number
  pendingReviewCount: number
  serviceHealth: ServiceHealth
  topProductsByLaunches: TopProduct[]
}

// Matches PlatformDashboardController — real access check (VIEW_PLATFORM_DASHBOARD,
// seeded for ADMIN) lives server-side via SecurityConfig.
export const platformDashboardApi = {
  get: () => apiRequest<PlatformDashboard>('/admin/platform-dashboard'),
}
