import { apiRequest } from './client'

export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
export type TicketStatus = 'OPEN' | 'IN_PROGRESS' | 'ESCALATED' | 'RESOLVED' | 'CLOSED'

export interface SupportTicket {
  id: number
  requestedByCustomerId: number
  requestedByName: string
  subject: string
  description: string
  category?: string
  priority: TicketPriority
  status: TicketStatus
  assignedToCustomerId?: number
  assignedToName?: string
  resolutionNote?: string
  resolvedAt?: string
  closedAt?: string
  createdAt: string
}

// 12.01.01 Ticket Management (sprint 2027.1.2) — any authenticated customer's own tickets.
export const supportApi = {
  create: (subject: string, description: string) =>
    apiRequest<SupportTicket>('/me/tickets', { method: 'POST', body: JSON.stringify({ subject, description }) }),
  myTickets: () => apiRequest<SupportTicket[]>('/me/tickets'),
  get: (id: number) => apiRequest<SupportTicket>(`/me/tickets/${id}`),
}

// ADMIN-only on the backend (MANAGE_SUPPORT_TICKETS) — see SecurityConfig.
export const adminSupportApi = {
  listAll: () => apiRequest<SupportTicket[]>('/admin/support/tickets'),
  update: (id: number, payload: { category?: string; priority?: TicketPriority; assignedToCustomerId?: number }) =>
    apiRequest<SupportTicket>(`/admin/support/tickets/${id}`, { method: 'PATCH', body: JSON.stringify(payload) }),
  escalate: (id: number) => apiRequest<SupportTicket>(`/admin/support/tickets/${id}/escalate`, { method: 'POST' }),
  resolve: (id: number, resolutionNote?: string) =>
    apiRequest<SupportTicket>(`/admin/support/tickets/${id}/resolve`, { method: 'POST', body: JSON.stringify({ resolutionNote }) }),
  close: (id: number) => apiRequest<SupportTicket>(`/admin/support/tickets/${id}/close`, { method: 'POST' }),
}
