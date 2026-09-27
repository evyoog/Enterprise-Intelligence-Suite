import { apiRequest } from './client'

// 15.01 Platform Administration (sprint 2026.4.2). ADMIN-only (MANAGE_PLATFORM_SETTINGS),
// enforced server-side.

export interface PlatformCurrency {
  code: string
  name: string
  enabled: boolean
}

export interface PlatformRegion {
  id: number
  code: string
  name: string
  enabled: boolean
}

export interface PlatformFeatureFlag {
  flagKey: string
  enabled: boolean
  description?: string
}

export interface SupportedLanguage {
  code: string
  label: string
}

export const platformAdministrationApi = {
  listCurrencies: () => apiRequest<PlatformCurrency[]>('/admin/platform-settings/currencies'),
  updateCurrency: (code: string, enabled: boolean) =>
    apiRequest<PlatformCurrency>(`/admin/platform-settings/currencies/${code}`, { method: 'PATCH', body: JSON.stringify({ enabled }) }),

  listRegions: () => apiRequest<PlatformRegion[]>('/admin/platform-settings/regions'),
  createRegion: (code: string, name: string) =>
    apiRequest<PlatformRegion>('/admin/platform-settings/regions', { method: 'POST', body: JSON.stringify({ code, name }) }),
  updateRegion: (id: number, name: string, enabled: boolean) =>
    apiRequest<PlatformRegion>(`/admin/platform-settings/regions/${id}`, { method: 'PUT', body: JSON.stringify({ name, enabled }) }),
  deleteRegion: (id: number) => apiRequest<undefined>(`/admin/platform-settings/regions/${id}`, { method: 'DELETE' }),

  listFeatureFlags: () => apiRequest<PlatformFeatureFlag[]>('/admin/platform-settings/feature-flags'),
  createFeatureFlag: (flagKey: string, enabled: boolean, description?: string) =>
    apiRequest<PlatformFeatureFlag>('/admin/platform-settings/feature-flags', { method: 'POST', body: JSON.stringify({ flagKey, enabled, description }) }),
  updateFeatureFlag: (flagKey: string, enabled: boolean, description?: string) =>
    apiRequest<PlatformFeatureFlag>(`/admin/platform-settings/feature-flags/${flagKey}`, { method: 'PATCH', body: JSON.stringify({ enabled, description }) }),
  deleteFeatureFlag: (flagKey: string) => apiRequest<undefined>(`/admin/platform-settings/feature-flags/${flagKey}`, { method: 'DELETE' }),

  listSupportedLanguages: () => apiRequest<SupportedLanguage[]>('/admin/platform-settings/languages'),
}
