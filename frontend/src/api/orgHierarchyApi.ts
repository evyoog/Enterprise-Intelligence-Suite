import { apiRequest } from './client'

/** REQ-TEN-006 Organization hierarchy. Always scoped to the caller's own organization; needs MANAGE_ORGANIZATION. */
export interface OrgLevel { type: string; label: string; rank: number }

export interface OrgNode {
  id: number
  parentId: number | null
  name: string
  type: string
  code?: string | null
  description?: string | null
  sortOrder: number
  active: boolean
  childCount: number
  memberCount: number
  createdAt: string
  updatedAt: string
}

export interface OrgTree { levels: OrgLevel[]; nodes: OrgNode[] }

export interface OrgNodeMember {
  memberId: number
  customerId: number
  firstName?: string
  lastName?: string
  email?: string
  status: string
}

export interface OrgNodeDetail { node: OrgNode; path: string[]; members: OrgNodeMember[] }

export interface OrgNodeHistoryEntry {
  id: number
  previousParentId: number | null
  previousParentName: string | null
  newParentId: number | null
  newParentName: string | null
  changedByCustomerId: number | null
  effectiveAt: string
}

export interface OrgNodeInput {
  name: string
  type: string
  code?: string
  description?: string
  sortOrder?: number
}

export interface OrgImportResult { created: number; failed: number; errors: { row: number; message: string }[] }

const base = '/organization/me/org-hierarchy'

export const orgHierarchyApi = {
  tree: () => apiRequest<OrgTree>(base),
  detail: (id: number) => apiRequest<OrgNodeDetail>(`${base}/nodes/${id}`),
  history: (id: number) => apiRequest<OrgNodeHistoryEntry[]>(`${base}/nodes/${id}/history`),
  create: (parentId: number, input: OrgNodeInput) =>
    apiRequest<OrgNode>(`${base}/nodes`, { method: 'POST', body: JSON.stringify({ parentId, ...input }) }),
  update: (id: number, input: OrgNodeInput & { active?: boolean; force?: boolean }) =>
    apiRequest<OrgNode>(`${base}/nodes/${id}`, { method: 'PUT', body: JSON.stringify(input) }),
  move: (id: number, newParentId: number) =>
    apiRequest<OrgNode>(`${base}/nodes/${id}/move`, { method: 'PATCH', body: JSON.stringify({ newParentId }) }),
  remove: (id: number) => apiRequest<undefined>(`${base}/nodes/${id}`, { method: 'DELETE' }),
  placeMember: (id: number, memberId: number) =>
    apiRequest<OrgNodeDetail>(`${base}/nodes/${id}/members/${memberId}`, { method: 'PUT' }),
  removeMember: (id: number, memberId: number) =>
    apiRequest<OrgNodeDetail>(`${base}/nodes/${id}/members/${memberId}`, { method: 'DELETE' }),
  importCsv: (file: File) => {
    const form = new FormData()
    form.append('file', file)
    return apiRequest<OrgImportResult>(`${base}/import`, { method: 'POST', body: form })
  },
  levels: () => apiRequest<OrgLevel[]>(`${base}/levels`),
  saveLevels: (levels: { type: string; label?: string }[]) =>
    apiRequest<OrgLevel[]>(`${base}/levels`, { method: 'PUT', body: JSON.stringify({ levels }) }),
}
