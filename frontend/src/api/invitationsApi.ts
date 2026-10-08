import { apiRequest } from './client'

/** REQ-TEN-008 Invite user. Inviter calls are scoped to the caller's own organization; the public calls use the link token only. */
export type InvitationStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'EXPIRED' | 'REVOKED'

export interface Invitation {
  id: number
  email: string
  status: InvitationStatus
  orgRole: string
  orgNodeId?: number
  orgNodeName?: string
  invitedByCustomerId: number
  invitedByName?: string
  invitedAt: string
  expiresAt: string
  acceptedAt?: string
  declinedAt?: string
  revokedAt?: string
  lastSentAt: string
  sendCount: number
  acceptedByName?: string
}

export interface CreateInvitationInput { email: string; orgRole: string; orgNodeId?: number | null }
export interface InvitationResult { invitation: Invitation; emailSent: boolean }
export interface NodeOption { id: number; name: string; type: string; path: string }

export interface InvitationPreview {
  status: InvitationStatus
  email?: string
  organizationName?: string
  inviterName?: string
  orgRole?: string
  orgNodeName?: string
  expiresAt?: string
  accountExists: boolean
  organizationAvailable: boolean
}

export interface AccountInput { firstName: string; lastName: string; password: string; confirmPassword: string }

const base = '/organization/me'

export const invitationsApi = {
  list: (status?: string) => apiRequest<Invitation[]>(`${base}/invitations${status ? `?status=${status}` : ''}`),
  create: (input: CreateInvitationInput) =>
    apiRequest<InvitationResult>(`${base}/invitations`, { method: 'POST', body: JSON.stringify(input) }),
  resend: (id: number) => apiRequest<InvitationResult>(`${base}/invitations/${id}/resend`, { method: 'POST' }),
  revoke: (id: number) => apiRequest<Invitation>(`${base}/invitations/${id}/revoke`, { method: 'POST' }),
  structureNodes: () => apiRequest<NodeOption[]>(`${base}/invitations/structure-nodes`),
  inviters: () => apiRequest<{ memberIds: number[] }>(`${base}/invitations/inviters`),
  setInvitePermission: (memberId: number, allowed: boolean) =>
    apiRequest<{ memberIds: number[] }>(`${base}/members/${memberId}/invite-permission`, { method: 'PUT', body: JSON.stringify({ allowed }) }),
  // Public (the token is the credential); accept needs a signed-in account.
  preview: (token: string) => apiRequest<InvitationPreview>(`/invitations/${encodeURIComponent(token)}`),
  createAccount: (token: string, input: AccountInput) =>
    apiRequest<{ accepted: boolean; organizationName: string }>(`/invitations/${encodeURIComponent(token)}/account`, { method: 'POST', body: JSON.stringify(input) }),
  accept: (token: string) =>
    apiRequest<{ accepted: boolean; organizationName: string }>(`/invitations/${encodeURIComponent(token)}/accept`, { method: 'POST' }),
  decline: (token: string) => apiRequest<undefined>(`/invitations/${encodeURIComponent(token)}/decline`, { method: 'POST' }),
}
