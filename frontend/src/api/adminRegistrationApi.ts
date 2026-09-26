import { apiRequest } from './client'
import type { RegistrationStatus } from './registrationApi'

export interface PendingProvisioning {
  customerId: number
  kind: 'INDIVIDUAL' | 'ORG_ADMIN'
  firstName: string
  lastName: string
  email: string
  organizationName?: string
}

export interface OrganizationAdmin {
  id: number
  name: string
  code: string
  type?: string
  industry?: string
  website?: string
  businessEmail: string
  phone?: string
  country: string
  state?: string
  city?: string
  address?: string
  gstin?: string
  pan?: string
  companyRegistrationNumber?: string
  taxVatNumber?: string
  billingSameAsAddress: boolean
  billingAddress?: string
  billingCountry?: string
  billingState?: string
  billingCity?: string
  parentOrganizationId?: number
  licensedSeats: number
  activeMemberCount: number
  status: RegistrationStatus
  lifecycleStatus: OrganizationLifecycleStatus
  adminFirstName?: string
  adminLastName?: string
  adminEmail?: string
  adminKeycloakLinked: boolean
  createdAt?: string
}

// REQ-TEN-001: platform-admin lifecycle, separate from the registration status.
export type OrganizationLifecycleStatus = 'ACTIVE' | 'SUSPENDED' | 'CLOSED'

export type OrganizationLifecycleAction = 'suspend' | 'activate' | 'close'

// Matches UpdateOrganizationRequest. Code, seats, MFA policy and parent are not editable here.
export interface UpdateOrganizationPayload {
  name: string
  type?: string
  industry?: string
  website?: string
  businessEmail: string
  phone?: string
  country: string
  state?: string
  city?: string
  address?: string
  gstin?: string
  pan?: string
  companyRegistrationNumber?: string
  taxVatNumber?: string
  billingSameAsAddress: boolean
  billingAddress?: string
  billingCountry?: string
  billingState?: string
  billingCity?: string
}

export interface OrganizationLifecycleResult {
  organization: OrganizationAdmin
  accountsUpdated: number
  accountsNotUpdated: string[]
}

export interface CustomerAdmin {
  id: number
  email: string
  firstName: string
  lastName: string
  mobile?: string
  country?: string
  companyName?: string
  jobTitle?: string
  industry?: string
  status: RegistrationStatus
  keycloakLinked: boolean
  createdAt?: string
}

// ADMIN-only (hasRole("ADMIN") on the backend — reused as this phase's
// PLATFORM_ADMIN, see AdminRegistrationController).
export const adminRegistrationApi = {
  listPendingProvisioning: () => apiRequest<PendingProvisioning[]>('/admin/registrations/pending-provisioning'),
  linkKeycloakUser: (customerId: number, keycloakSub: string) =>
    apiRequest<undefined>(`/admin/registrations/${customerId}/link-keycloak-user`, {
      method: 'POST', body: JSON.stringify({ keycloakSub }),
    }),
  // Every registered organization, full company details — not just the ones
  // still waiting on a Keycloak link.
  listAllOrganizations: () => apiRequest<OrganizationAdmin[]>('/admin/registrations/organizations'),
  // Every individual customer who never joined an organization (an org's own
  // admin/members show up via listAllOrganizations instead).
  listAllIndividuals: () => apiRequest<CustomerAdmin[]>('/admin/registrations/individuals'),
  // Matches AdminRegistrationController#updateSeats.
  updateSeats: (organizationId: number, licensedSeats: number) =>
    apiRequest<{ licensedSeats: number }>(`/admin/registrations/organizations/${organizationId}/seats`, {
      method: 'PATCH',
      body: JSON.stringify({ licensedSeats }),
    }),
  // C30: reset any account's two-factor authentication, by email.
  resetMfa: (email: string) =>
    apiRequest<undefined>('/admin/registrations/mfa-reset', { method: 'POST', body: JSON.stringify({ email }) }),
  // REQ-TEN-001 — AdminRegistrationController#updateOrganization.
  updateOrganization: (organizationId: number, payload: UpdateOrganizationPayload) =>
    apiRequest<OrganizationAdmin>(`/admin/registrations/organizations/${organizationId}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    }),
  // REQ-TEN-001 — suspend / activate / close (soft; nothing is deleted).
  changeOrganizationLifecycle: (organizationId: number, action: OrganizationLifecycleAction, reason?: string) =>
    apiRequest<OrganizationLifecycleResult>(`/admin/registrations/organizations/${organizationId}/${action}`, {
      method: 'POST',
      body: JSON.stringify({ reason: reason?.trim() || undefined }),
    }),
}
