import { apiRequest } from './client'

export interface Permission {
  id: number
  name: string
  description?: string
  /** Seeded permissions the backend refuses to delete. */
  systemManaged: boolean
  /** How many roles currently grant it — a permission still granted by a role cannot be deleted. */
  roleCount: number
}

// REQ-IAM-003 — AdminPermissionController, requires the MANAGE_PERMISSIONS platform permission.
export const permissionsApi = {
  list: () => apiRequest<Permission[]>('/admin/permissions'),
  create: (payload: { name: string; description?: string }) =>
    apiRequest<Permission>('/admin/permissions', { method: 'POST', body: JSON.stringify(payload) }),
  // The name cannot change after creation (UpdatePermissionRequest).
  update: (id: number, payload: { description?: string }) =>
    apiRequest<Permission>(`/admin/permissions/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  remove: (id: number) => apiRequest<undefined>(`/admin/permissions/${id}`, { method: 'DELETE' }),
}
