import { apiRequest } from './client'
import type { ClaimMapping } from './claimMapping'

// REQ-IAM-006 (C27): the organization's OIDC providers (OrganizationOidcProviderController).
export interface OidcProvider {
  id: number
  name: string
  issuerUrl: string
  clientId: string
  /** The secret itself is never returned. */
  clientSecretSet: boolean
  scopes: string
  enabled: boolean
  /** Register this at the identity provider. */
  redirectUri: string
  createdAt: string
  updatedAt: string
  /** REQ-IAM-007 */
  claimMapping?: ClaimMapping | null
}

export interface OidcProviderPayload {
  name: string
  issuerUrl: string
  clientId: string
  /** Required on create; blank on update keeps the stored one. */
  clientSecret?: string
  scopes?: string
}

export interface OidcProviderTestResult {
  success: boolean
  checks: string[]
  errors: string[]
}

const base = '/organization/me/oidc-providers'

export const oidcApi = {
  list: () => apiRequest<OidcProvider[]>(base),
  create: (payload: OidcProviderPayload) => apiRequest<OidcProvider>(base, { method: 'POST', body: JSON.stringify(payload) }),
  update: (id: number, payload: OidcProviderPayload) =>
    apiRequest<OidcProvider>(`${base}/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  enable: (id: number) => apiRequest<OidcProvider>(`${base}/${id}/enable`, { method: 'POST' }),
  disable: (id: number) => apiRequest<OidcProvider>(`${base}/${id}/disable`, { method: 'POST' }),
  test: (id: number) => apiRequest<OidcProviderTestResult>(`${base}/${id}/test`, { method: 'POST' }),
  remove: (id: number) => apiRequest<undefined>(`${base}/${id}`, { method: 'DELETE' }),
  // REQ-IAM-007 (C28)
  updateClaimMapping: (id: number, mapping: ClaimMapping) =>
    apiRequest<OidcProvider>(`${base}/${id}/claim-mapping`, { method: 'PUT', body: JSON.stringify(mapping) }),
}
