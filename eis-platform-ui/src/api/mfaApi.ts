import { apiRequest } from './client'

export interface MfaStatus {
  enabled: boolean
  enrolledAt?: string
  remainingRecoveryCodes: number
}

export interface MfaEnrollResponse {
  secret: string
  otpAuthUri: string
  /** Base64-encoded PNG — render directly as a data URI, nothing to persist. */
  qrCodePngBase64: string
}

export interface MfaRecoveryCodes {
  codes: string[]
}

// Self-service Platform MFA management — always scoped to the caller's own
// account (see MfaController on the backend). Every state-changing call
// requires the current password, re-verified fresh against Keycloak.
export const mfaApi = {
  getStatus: () => apiRequest<MfaStatus>('/me/mfa'),
  enroll: (currentPassword: string) =>
    apiRequest<MfaEnrollResponse>('/me/mfa/enroll', { method: 'POST', body: JSON.stringify({ currentPassword }) }),
  verifyEnrollment: (code: string) =>
    apiRequest<MfaRecoveryCodes>('/me/mfa/verify-enrollment', { method: 'POST', body: JSON.stringify({ code }) }),
  disable: (currentPassword: string, code: string) =>
    apiRequest<undefined>('/me/mfa/disable', { method: 'POST', body: JSON.stringify({ currentPassword, code }) }),
  regenerateRecoveryCodes: (currentPassword: string, code: string) =>
    apiRequest<MfaRecoveryCodes>('/me/mfa/recovery-codes/regenerate', { method: 'POST', body: JSON.stringify({ currentPassword, code }) }),
}
