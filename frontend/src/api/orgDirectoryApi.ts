import { apiRequest } from './client'
import type { OrganizationAdmin } from './adminRegistrationApi'
import type { OrgNodeDetail, OrgNodeHistoryEntry, OrgTree } from './orgHierarchyApi'

/** REQ-TEN-007 Organizations directory (platform administrators, read-only; MANAGE_REGISTRATIONS). */
export type DirectoryKind = 'ORGANIZATION' | 'INDIVIDUAL'

export interface DirectoryRow {
  kind: DirectoryKind
  id: number
  name: string
  code?: string
  type?: string
  industry?: string
  country?: string
  state?: string
  city?: string
  email?: string
  phone?: string
  contactName?: string
  contactEmail?: string
  seatsLicensed: number
  seatsUsed: number
  status: string
  lifecycleStatus?: string
  regionId?: number
  regionName?: string
  parentOrganizationId?: number
  parentOrganizationName?: string
  createdAt?: string
  signInLinked: boolean
  mfaRequired: boolean
  productCount: number
  hierarchyNodes: number
  openTickets: number
  outstandingInvoices: number
  profileCompletion: number
  missingFields: string[]
}

export interface DirectorySummary {
  total: number
  organizations: number
  individuals: number
  profileComplete: number
  profileInProgress: number
  profileNotStarted: number
  averageCompletion: number
}

export interface Completion { percent: number; missing: string[] }
export interface OrganizationOverview { organization: OrganizationAdmin; completion: Completion; mfaRequired: boolean }
export interface MemberRow {
  memberId: number; customerId: number; name?: string; email?: string; orgRole: string; status: string
  joinedAt?: string; orgNodeId?: number; orgNodeName?: string
}
export interface SubscriptionRow {
  id: number; productId: number; productName?: string; status: string; quantity: number
  startedAt?: string; expiresAt?: string; autoRenew: boolean
}
export interface InvoiceRow { id: number; number?: string; status: string; currency?: string; total: number; issuedAt?: string; dueAt?: string }
export interface TicketRow { id: number; subject: string; status: string; priority: string; requestedBy?: string; createdAt?: string }
export interface IndividualDetail {
  id: number; firstName?: string; lastName?: string; email: string; mobile?: string; country?: string
  companyName?: string; jobTitle?: string; industry?: string; status: string; signInLinked: boolean; createdAt?: string
  completion: Completion; subscriptions: SubscriptionRow[]; invoices: InvoiceRow[]; tickets: TicketRow[]
}

const base = '/admin/organizations'

export const orgDirectoryApi = {
  directory: () => apiRequest<{ rows: DirectoryRow[]; summary: DirectorySummary }>(`${base}/directory`),
  overview: (id: number) => apiRequest<OrganizationOverview>(`${base}/${id}`),
  members: (id: number) => apiRequest<MemberRow[]>(`${base}/${id}/members`),
  subscriptions: (id: number) => apiRequest<SubscriptionRow[]>(`${base}/${id}/subscriptions`),
  invoices: (id: number) => apiRequest<InvoiceRow[]>(`${base}/${id}/invoices`),
  tickets: (id: number) => apiRequest<TicketRow[]>(`${base}/${id}/tickets`),
  hierarchy: (id: number) =>
    apiRequest<OrgTree>(`${base}/${id}/org-hierarchy`)
      .then((t) => ({ ...t, nodes: t.nodes.map((n) => ({ ...n, parentId: n.parentId ?? null })) })),
  hierarchyNode: (id: number, nodeId: number) => apiRequest<OrgNodeDetail>(`${base}/${id}/org-hierarchy/nodes/${nodeId}`),
  hierarchyHistory: (id: number, nodeId: number) =>
    apiRequest<OrgNodeHistoryEntry[]>(`${base}/${id}/org-hierarchy/nodes/${nodeId}/history`),
  individual: (customerId: number) => apiRequest<IndividualDetail>(`${base}/individuals/${customerId}`),
}
