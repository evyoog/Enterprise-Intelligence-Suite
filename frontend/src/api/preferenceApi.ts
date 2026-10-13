import { apiRequest } from './client'
import type { PlatformDefaults } from './messageTemplatesApi'

export interface CustomerPreference {
  language: string | null
  region: string | null
  timeZone: string | null
  themeMode: string | null
  reducedMotion: boolean
  /** C92: what new users start with (null fields = no platform default). Read-only; sent back as-is and ignored. */
  defaults?: PlatformDefaults
}

// Phase 6 (2026.3.3): account-level personalization — see the backend's own
// CustomerPreference javadoc for why language/region/timeZone are three
// separate fields, and PreferenceSync (theming/PreferenceSync.tsx) for how
// this is kept in step with the client-side theme/locale/i18n providers.
export const preferenceApi = {
  get: () => apiRequest<CustomerPreference>('/me/preferences'),
  update: (preference: CustomerPreference) =>
    apiRequest<CustomerPreference>('/me/preferences', { method: 'PUT', body: JSON.stringify(preference) }),
}
