import { apiRequest } from './client'

export type PlatformStatus = 'ACTIVE' | 'INACTIVE'

export interface Platform {
  id: number
  name: string
  description?: string
  imageUrl?: string
  // C66 showcase colour and catalog settings.
  primaryColor?: string | null
  status?: PlatformStatus
  showInCatalog?: boolean
  displayOrder?: number
}

export interface PlatformCreateRequest {
  name: string
  description?: string
  imageUrl?: string
  primaryColor?: string | null
  status?: PlatformStatus
  showInCatalog?: boolean
  displayOrder?: number
}

export const platformsApi = {
  // Matches PlatformController#listPlatforms — ADMIN-only, no public listing.
  list: () => apiRequest<Platform[]>('/platforms'),

  get: (id: number) => apiRequest<Platform>(`/platforms/${id}`),

  create: (payload: PlatformCreateRequest) =>
    apiRequest<Platform>('/platforms', { method: 'POST', body: JSON.stringify(payload) }),

  update: (id: number, payload: PlatformCreateRequest) =>
    apiRequest<Platform>(`/platforms/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),

  uploadImage: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return apiRequest<{ url: string }>('/platforms/images', { method: 'POST', body: formData })
  },

  // Matches PlatformController#deletePlatform — ADMIN-only. Safe to call even
  // when products are assigned to this platform (the join table cascades).
  delete: (id: number) => apiRequest<undefined>(`/platforms/${id}`, { method: 'DELETE' }),
}
