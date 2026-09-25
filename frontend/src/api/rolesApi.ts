import { apiRequest } from './client'

export type RoleScope = 'PLATFORM' | 'ORGANIZATION'

export interface Role {
  id: number
  name: string
  scope: RoleScope
  description?: string
  permissionNames: string[]
  /** ADMIN, ORG_ADMIN and MEMBER — the backend refuses to delete these. */
  systemManaged: boolean
}

export interface CreateRolePayload {
  name: string
  scope: RoleScope
  description?: string
  permissionIds: number[]
}

/** Name and scope cannot change after creation (UpdateRoleRequest). */
export interface UpdateRolePayload {
  description?: string
  permissionIds?: number[]
}

// REQ-IAM-003 — AdminRoleController, requires the MANAGE_ROLES platform permission.
export const rolesApi = {
  list: () => apiRequest<Role[]>('/admin/roles'),
  create: (payload: CreateRolePayload) =>
    apiRequest<Role>('/admin/roles', { method: 'POST', body: JSON.stringify(payload) }),
  update: (id: number, payload: UpdateRolePayload) =>
    apiRequest<Role>(`/admin/roles/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  remove: (id: number) => apiRequest<undefined>(`/admin/roles/${id}`, { method: 'DELETE' }),
}
