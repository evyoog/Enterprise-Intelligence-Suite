import { apiRequest } from './client'

/** Matches MyPermissionsDto: the caller's own permission names, for UI gating only
 * (every endpoint is still checked by the backend). */
export interface MyPermissions {
  platform: string[]
  organization: string[]
}

export const myPermissionsApi = {
  get: () => apiRequest<MyPermissions>('/me/permissions'),
}
