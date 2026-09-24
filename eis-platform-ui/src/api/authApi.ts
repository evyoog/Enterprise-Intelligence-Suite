import { apiRequest } from './client'

export { ApiError } from './client'

interface TokenResponse {
  accessToken: string
  expiresInSeconds: number
  // Phase 7: whether Keycloak's own amr claim on this token shows an OTP
  // challenge was completed — informational, see AuthProvider.
  mfaVerified: boolean
}

export const authApi = {
  // totp is only ever sent on a login's second submission, after the first
  // one came back with the mfaRequired flag (see ApiError.detail) — see
  // AuthModal for where that retry actually happens.
  login: (email: string, password: string, totp?: string) =>
    apiRequest<TokenResponse>('/auth/login', { method: 'POST', body: JSON.stringify({ email, password, totp }) }),
  // Phase 2 (2026.3.3): completes a login the backend parked on a Platform
  // MFA challenge (see ApiError.detail.platformMfaRequired/mfaChallengeId) —
  // code is either a 6-digit TOTP code or a recovery code, same field either
  // way (see PlatformMfaService on the backend for how it tells them apart).
  verifyMfaChallenge: (challengeId: string, code: string) =>
    apiRequest<TokenResponse>('/auth/mfa/verify', { method: 'POST', body: JSON.stringify({ challengeId, code }) }),
  refresh: () => apiRequest<TokenResponse>('/auth/refresh', { method: 'POST' }),
  // The one "am I logged in" check — also covers cross-app auto-login (see
  // AuthController.session() on the backend): no local session but the shared
  // vyoog_sso cookie points at PMS's own login, this silently exchanges it
  // into a Vyoog-scoped token, no password, no Keycloak UI.
  session: () => apiRequest<TokenResponse>('/auth/session', { method: 'GET' }),
  // Cheap polling target for cross-tab logout detection — same contract as
  // session(), just semantically "still alive?" rather than "first load".
  ping: () => apiRequest<TokenResponse>('/auth/ping', { method: 'GET' }),
  logout: () => apiRequest<undefined>('/auth/logout', { method: 'POST' }),
  // Phase 8: always resolves the same way regardless of whether the email
  // matches a real account (anti-enumeration is enforced backend-side — see
  // PasswordResetService — this call never itself reveals anything).
  forgotPassword: (email: string) =>
    apiRequest<undefined>('/auth/forgot-password', { method: 'POST', body: JSON.stringify({ email }) }),
  resetPassword: (token: string, newPassword: string, confirmPassword: string) =>
    apiRequest<undefined>('/auth/reset-password', { method: 'POST', body: JSON.stringify({ token, newPassword, confirmPassword }) }),
}
