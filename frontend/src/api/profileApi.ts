import { ApiError, apiBlob, apiRequest } from './client'

// REQ "My profile" (decision C107). The caller is always resolved from the token on the
// server; no endpoint below takes the person's id except the platform-admin ones.

export type ProfileRole = 'PLATFORM_ADMIN' | 'ORG_ADMIN' | 'MEMBER' | 'INDIVIDUAL'

export interface ProfileAccount {
  email: string
  role: ProfileRole
  organizationName: string | null
  organizationCode: string | null
  status: string | null
  /** ISO-8601 */
  memberSince: string | null
  /** False when the names are managed by an external identity provider. */
  nameEditable: boolean
}

export interface ProfilePersonal {
  firstName: string | null
  lastName: string | null
  preferredName: string | null
  mobile: string | null
  alternatePhone: string | null
  bio: string | null
}
export interface ProfileWork {
  jobTitle: string | null
  department: string | null
  companyName: string | null
  industry: string | null
}
export interface ProfileAddress {
  addressLine1: string | null
  addressLine2: string | null
  city: string | null
  state: string | null
  postalCode: string | null
  country: string | null
}
export interface ProfileOnline {
  linkedinUrl: string | null
  websiteUrl: string | null
}

export interface ProfileCompletion {
  percent: number
  filled: number
  total: number
  /** Item keys still empty: photo, mobile, jobTitle, department, companyName, industry, addressLine1, city, state, postalCode, country. */
  missing: string[]
}

export interface ProfileCompletionStatus extends ProfileCompletion {
  /** False only for a signed-in person without a customer row (typically a platform admin). */
  exists: boolean
}

export interface ProfileDto {
  exists: boolean
  account: ProfileAccount
  personal: ProfilePersonal
  work: ProfileWork
  address: ProfileAddress
  online: ProfileOnline
  photo: { present: boolean; updatedAt: string | null }
  completion: ProfileCompletion
}

export interface ProfileUpdateRequest {
  personal: ProfilePersonal
  work: ProfileWork
  address: ProfileAddress
  online: ProfileOnline
}

export type ProfileSection = 'personal' | 'work' | 'address' | 'online'

/** The server's `400 {fieldErrors: {"personal.firstName": "..."}}`, or null for any other error. */
export function profileFieldErrors(e: unknown): Record<string, string> | null {
  if (!(e instanceof ApiError) || e.status !== 400) return null
  const raw = e.detail?.fieldErrors
  if (!raw || typeof raw !== 'object') return null
  const out: Record<string, string> = {}
  for (const [k, v] of Object.entries(raw as Record<string, unknown>)) if (typeof v === 'string') out[k] = v
  return Object.keys(out).length > 0 ? out : null
}

export const profileApi = {
  get: () => apiRequest<ProfileDto>('/me/profile'),
  update: (body: ProfileUpdateRequest) =>
    apiRequest<ProfileDto>('/me/profile', { method: 'PUT', body: JSON.stringify(body) }),
  completion: () => apiRequest<ProfileCompletionStatus>('/me/profile/completion'),
  /** The browser has already downsized the image; the server checks type, size and content again. */
  uploadPhoto: (photo: Blob, filename: string) => {
    const form = new FormData()
    form.append('file', photo, filename)
    return apiRequest<ProfileDto>('/me/profile/photo', { method: 'PUT', body: form })
  },
  removePhoto: () => apiRequest<ProfileDto>('/me/profile/photo', { method: 'DELETE' }),
  /** The photo bytes, or null when there is none. */
  photo: () => apiBlob('/me/profile/photo'),

  // Platform administrators (MANAGE_REGISTRATIONS): any person's record, without the photo.
  getForCustomer: (customerId: number) =>
    apiRequest<ProfileDto>(`/admin/registrations/individuals/${customerId}/profile`),
  updateForCustomer: (customerId: number, body: ProfileUpdateRequest) =>
    apiRequest<ProfileDto>(`/admin/registrations/individuals/${customerId}/profile`, {
      method: 'PUT', body: JSON.stringify(body),
    }),
}
