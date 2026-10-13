import { apiRequest } from './client'

// REQ-GOV-002 (C92): message templates and platform defaults. Both need MANAGE_PLATFORM_SETTINGS, enforced server-side.

export interface TemplatePlaceholder {
  name: string
  description: string
  example: string
  required: boolean
}

export interface TemplateVariant {
  language: string
  customized: boolean
  subject: string
  body: string
  defaultSubject: string
  defaultBody: string
  updatedAt: string | null
}

export interface TemplateType {
  code: string
  name: string
  description: string
  channel: 'EMAIL' | 'NOTIFICATION'
  placeholders: TemplatePlaceholder[]
  variants: TemplateVariant[]
}

export interface TemplatePreview {
  subject: string
  body: string
}

export interface PlatformDefaults {
  language: string | null
  region: string | null
  currency: string | null
  timeZone: string | null
  dateFormat: string | null
  reminderDays: number | null
  updatedAt: string | null
}

export type SavePlatformDefaults = Omit<PlatformDefaults, 'updatedAt'>

const BASE = '/admin/platform-settings'

export const messageTemplatesApi = {
  list: () => apiRequest<TemplateType[]>(`${BASE}/message-templates`),
  save: (code: string, language: string, body: { subject: string; body: string }) =>
    apiRequest<TemplateType>(`${BASE}/message-templates/${code}/${language}`, { method: 'PUT', body: JSON.stringify(body) }),
  reset: (code: string, language: string) =>
    apiRequest<TemplateType>(`${BASE}/message-templates/${code}/${language}`, { method: 'DELETE' }),
  preview: (code: string, language: string, body: { subject: string; body: string }) =>
    apiRequest<TemplatePreview>(`${BASE}/message-templates/${code}/${language}/preview`, { method: 'POST', body: JSON.stringify(body) }),
}

export const platformDefaultsApi = {
  get: () => apiRequest<PlatformDefaults>(`${BASE}/defaults`),
  save: (body: SavePlatformDefaults) =>
    apiRequest<PlatformDefaults>(`${BASE}/defaults`, { method: 'PUT', body: JSON.stringify(body) }),
}
