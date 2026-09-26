import { apiRequest, apiUrl } from './client'
import type { ClaimMapping } from './claimMapping'

export interface SamlProvider {
  id: number
  name: string
  entityId: string
  ssoUrl: string
  certificatePem: string
  certificateFingerprint: string | null
  certificateExpiresAt: string | null
  certificateExpired: boolean
  enabled: boolean
  createdAt: string
  updatedAt: string
  /** REQ-IAM-007 */
  claimMapping?: ClaimMapping | null
}

export interface CreateSamlProviderPayload {
  name: string
  metadataXml?: string
  entityId?: string
  ssoUrl?: string
  certificatePem?: string
}

export interface SamlProviderTestResult {
  success: boolean
  checks: string[]
  errors: string[]
}

export interface SsoCheckResult {
  available: boolean
  organizationId: number | null
  organizationName: string | null
  /** REQ-IAM-006: which sign-in to start. */
  protocol?: 'SAML' | 'OIDC' | null
}

// ORG_ADMIN self-service Identity Federation — always the caller's own
// organization (enforced server-side, see OrganizationSamlProviderController).
export const samlApi = {
  list: () => apiRequest<SamlProvider[]>('/organization/me/saml-providers'),
  create: (payload: CreateSamlProviderPayload) =>
    apiRequest<SamlProvider>('/organization/me/saml-providers', { method: 'POST', body: JSON.stringify(payload) }),
  update: (id: number, payload: Partial<CreateSamlProviderPayload>) =>
    apiRequest<SamlProvider>(`/organization/me/saml-providers/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  enable: (id: number) => apiRequest<SamlProvider>(`/organization/me/saml-providers/${id}/enable`, { method: 'POST' }),
  disable: (id: number) => apiRequest<SamlProvider>(`/organization/me/saml-providers/${id}/disable`, { method: 'POST' }),
  test: (id: number) => apiRequest<SamlProviderTestResult>(`/organization/me/saml-providers/${id}/test`, { method: 'POST' }),
  remove: (id: number) => apiRequest<undefined>(`/organization/me/saml-providers/${id}`, { method: 'DELETE' }),
  // REQ-IAM-007 (C28)
  updateClaimMapping: (id: number, mapping: ClaimMapping) =>
    apiRequest<SamlProvider>(`/organization/me/saml-providers/${id}/claim-mapping`, { method: 'PUT', body: JSON.stringify(mapping) }),
}

// Phase 5 (2026.3.3): the actual SAML login flow — public, pre-login (no
// Vyoog account/token exists yet at this point in the flow).
export const samlLoginApi = {
  // A plain fetch — used to show an inline "Sign in with {org}" state before
  // committing to any navigation.
  ssoCheck: (organizationCode: string) =>
    apiRequest<SsoCheckResult>(`/saml/sso-check?organizationCode=${encodeURIComponent(organizationCode)}`),
  // A real top-level navigation, never fetch — the backend responds with an
  // actual HTTP redirect straight to the organization's own identity
  // provider (see SamlLoginController).
  loginInitUrl: (organizationId: number, protocol: SsoCheckResult['protocol'] = 'SAML') =>
    apiUrl(protocol === 'OIDC' ? `/oidc/${organizationId}/login-init` : `/saml/${organizationId}/login-init`),
}
