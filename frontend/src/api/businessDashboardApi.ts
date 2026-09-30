import { apiRequest } from './client'
import type { DashboardAlert, DashboardOrganization } from './dashboardApi'
import type { SubscriptionStatus } from './registrationApi'

export interface BusinessApplication {
  productId: number
  productName: string
  category?: string
  subscriptionStatus: SubscriptionStatus | null
  assignedMembers: number
  totalLaunches: number
  lastUsedAt?: string
}

export interface SeatUsage {
  licensedSeats: number
  activeMemberCount: number
  utilizationPercent: number
}

export interface SubscriptionSummary {
  id: number
  productId: number
  productName: string
  status: SubscriptionStatus
  startedAt?: string
  expiresAt?: string
}

export interface BillingOverview {
  subscriptions: SubscriptionSummary[]
  // 01.02.01 View spending (REQ-BIL-001.16, C46): real totals from paid
  // invoices, keyed by currency code; empty when there are none yet.
  spentThisPeriodByCurrency: Record<string, number>
  spentLastPeriodByCurrency: Record<string, number>
  note: string
}

export interface ServiceHealth {
  platformStatus: string
  note: string
}

export interface SupportOverview {
  available: boolean
  note: string
}

export interface BusinessDashboard {
  organization: DashboardOrganization
  applications: BusinessApplication[]
  seatUsage: SeatUsage
  billing: BillingOverview
  serviceHealth: ServiceHealth
  support: SupportOverview
  alerts: DashboardAlert[]
}

// Matches BusinessDashboardController — real access check lives server-side
// (MANAGE_ORGANIZATION, i.e. ORG_ADMIN); a non-admin member calling this gets
// a clean 403, same philosophy as every other admin-only action in this app.
export const businessDashboardApi = {
  get: () => apiRequest<BusinessDashboard>('/organization/me/business-dashboard'),
}
