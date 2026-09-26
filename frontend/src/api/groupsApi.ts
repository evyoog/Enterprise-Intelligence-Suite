import { apiRequest } from './client'
import type { OrgMember } from './registrationApi'

/** 05.04.01 Groups (sprint 2026.4.1). */
export interface Group {
  id: number
  name: string
  members: OrgMember[]
}

// Authenticated — always scoped to the caller's own organization on the
// backend, same as organizationApi (see OrganizationSelfService's own doc).
export const groupsApi = {
  list: () => apiRequest<Group[]>('/organization/me/groups'),
  create: (name: string) => apiRequest<Group>('/organization/me/groups', { method: 'POST', body: JSON.stringify({ name }) }),
  remove: (groupId: number) => apiRequest<undefined>(`/organization/me/groups/${groupId}`, { method: 'DELETE' }),
  addMember: (groupId: number, memberId: number) =>
    apiRequest<Group>(`/organization/me/groups/${groupId}/members/${memberId}`, { method: 'POST' }),
  removeMember: (groupId: number, memberId: number) =>
    apiRequest<Group>(`/organization/me/groups/${groupId}/members/${memberId}`, { method: 'DELETE' }),
}
