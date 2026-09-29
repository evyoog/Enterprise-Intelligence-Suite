import { apiRequest } from './client'
import type { Product } from './productsApi'

export type RegistrationStatus =
  | 'PENDING_EMAIL_VERIFICATION'
  | 'EMAIL_VERIFIED'
  | 'PENDING_SUBSCRIPTION'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'EXPIRED'

export type SubscriptionStatus = 'PENDING_SUBSCRIPTION' | 'ACTIVE' | 'SUSPENDED' | 'CANCELLED' | 'EXPIRED'

// Only `name`, `businessEmail`, `gstin`, `phone`, and `firstAdmin.{email,
// password,confirmPassword}` are ever actually collected by the real
// registration form now — everything else here is optional and, if
// omitted, defaulted server-side (see RegistrationService's own javadoc:
// auto-generated code, country defaults to India, licensedSeats defaults to
// 5, firstAdmin name defaults to the organization's own name — Keycloak
// requires a non-blank first/last name to log in at all).
export interface OrganizationRegistrationRequest {
  name: string
  code?: string
  type?: string
  industry?: string
  website?: string
  businessEmail: string
  phone?: string
  country?: string
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
  productIds?: number[]
  licensedSeats?: number
  firstAdmin: {
    firstName?: string
    lastName?: string
    email: string
    mobile?: string
    password: string
    confirmPassword: string
  }
}

export interface RegistrationAcceptedResponse {
  registrationId: string
  message: string
}

export interface RegistrationStatusResponse {
  registrationId: string
  status: RegistrationStatus
}

export interface MyProductAccess {
  productId: number
  productName: string
  category?: string
  /** null = never subscribed. */
  subscriptionStatus: SubscriptionStatus | null
}

export interface OrgProductAccess {
  productId: number
  productName: string
  category?: string
  /** null = the organization has never subscribed to this product. */
  orgSubscriptionStatus: SubscriptionStatus | null
  myAccessAssigned: boolean
  myProductRole?: string
}

export interface Subscription {
  id: number
  productId: number
  productName: string
  status: SubscriptionStatus
  startedAt?: string
  expiresAt?: string
  /** 07.01.02 Change plan (sprint 2026.4.3) — null means the product's flat price. */
  planId?: number
  planName?: string
}

export interface OrgMember {
  organizationMemberId: number
  customerId: number
  firstName?: string
  lastName?: string
  email?: string
  orgRole: 'ORG_ADMIN' | 'MEMBER'
  status: 'ACTIVE' | 'SUSPENDED' | 'INACTIVE'
  /** 05.03.02 Review access (sprint 2026.4.1) — set once an admin has confirmed
   * this member's role and access, undefined if never reviewed. */
  lastReviewedAt?: string
}

export type MemberStatusAction = 'SUSPEND' | 'REACTIVATE' | 'REMOVE'

export interface Organization {
  id: number
  name: string
  code: string
  type?: string
  industry?: string
  website?: string
  businessEmail: string
  country: string
  licensedSeats: number
  activeMemberCount: number
  status: RegistrationStatus
  /** Organization MFA policy (REQ-IAM-001) — always present on OrganizationDto. */
  mfaRequired: boolean
}

// Public — matches RegistrationController, permitAll on the backend.
export const registrationApi = {
  registerOrganization: (payload: OrganizationRegistrationRequest) =>
    apiRequest<RegistrationAcceptedResponse>('/register/organization', { method: 'POST', body: JSON.stringify(payload) }),

  verifyEmail: (token: string) =>
    apiRequest<RegistrationStatusResponse>('/register/verify-email', { method: 'POST', body: JSON.stringify({ token }) }),

  resendVerification: (registrationId: string) =>
    apiRequest<undefined>('/register/resend-verification', { method: 'POST', body: JSON.stringify({ registrationId }) }),

  // Reuses the same real product catalog as the public storefront — the
  // wizard's "which products" step can never drift from what actually exists.
  listProducts: () => apiRequest<Product[]>('/register/products'),

  getStatus: (registrationId: string) => apiRequest<RegistrationStatusResponse>(`/register/status/${registrationId}`),
}

// Authenticated — an individual customer's own products/subscriptions.
export const myProductsApi = {
  listProducts: () => apiRequest<MyProductAccess[]>('/me/products'),
  listSubscriptions: () => apiRequest<Subscription[]>('/me/subscriptions'),
  subscribe: (productId: number) =>
    apiRequest<Subscription>('/me/subscriptions', { method: 'POST', body: JSON.stringify({ productId }) }),
  // 07.01.01 Suspend/Reactivate/Cancel, 07.04.01 Renew (sprint 2026.4.3).
  suspend: (subscriptionId: number) =>
    apiRequest<Subscription>(`/me/subscriptions/${subscriptionId}/suspend`, { method: 'POST' }),
  reactivate: (subscriptionId: number) =>
    apiRequest<Subscription>(`/me/subscriptions/${subscriptionId}/reactivate`, { method: 'POST' }),
  cancel: (subscriptionId: number) =>
    apiRequest<Subscription>(`/me/subscriptions/${subscriptionId}/cancel`, { method: 'POST' }),
  renew: (subscriptionId: number) =>
    apiRequest<Subscription>(`/me/subscriptions/${subscriptionId}/renew`, { method: 'POST' }),
  // 07.01.02 Change plan (sprint 2026.4.3) — null planId clears the plan.
  changePlan: (subscriptionId: number, planId: number | null) =>
    apiRequest<Subscription>(`/me/subscriptions/${subscriptionId}/plan`, { method: 'PATCH', body: JSON.stringify({ planId }) }),
}

// Authenticated — always scoped to the caller's own organization on the backend.
export const organizationApi = {
  getMyOrganization: () => apiRequest<Organization>('/organization/me'),
  listMyOrgUsers: () => apiRequest<OrgMember[]>('/organization/me/users'),
  // REQ-IAM-001 — requires MANAGE_ORGANIZATION (enforced server-side).
  updateMfaPolicy: (mfaRequired: boolean) =>
    apiRequest<Organization>('/organization/me/mfa-policy', { method: 'PATCH', body: JSON.stringify({ mfaRequired }) }),
  // REQ-IAM-002 — requires MANAGE_USERS on the target member (enforced server-side).
  changeMemberRole: (memberId: number, orgRole: OrgMember['orgRole']) =>
    apiRequest<OrgMember>(`/organization/me/members/${memberId}/role`, { method: 'PATCH', body: JSON.stringify({ orgRole }) }),
  // C30: reset a member's two-factor authentication (MANAGE_USERS, same organization).
  resetMemberMfa: (memberId: number) =>
    apiRequest<undefined>(`/organization/me/members/${memberId}/mfa/reset`, { method: 'POST' }),
  // 05.03.01 User Lifecycle (sprint 2026.4.1) — suspend/reactivate/remove.
  changeMemberStatus: (memberId: number, action: MemberStatusAction) =>
    apiRequest<OrgMember>(`/organization/me/members/${memberId}/status`, { method: 'PATCH', body: JSON.stringify({ action }) }),
  // 05.03.02 Review access (sprint 2026.4.1).
  reviewMemberAccess: (memberId: number) =>
    apiRequest<OrgMember>(`/organization/me/members/${memberId}/access-review`, { method: 'POST' }),
  listMyOrgProducts: () => apiRequest<OrgProductAccess[]>('/organization/me/products'),
  listMyOrgSubscription: () => apiRequest<Subscription[]>('/organization/me/subscription'),
}
