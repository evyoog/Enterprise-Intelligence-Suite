import { apiRequest } from './client'
import type { Subscription } from './registrationApi'
import type { SupportTicket } from './supportApi'

/** C102 (REQ-MKT-005): what a customer can do before buying — start a free trial or ask for a demo. */
export interface DemoRequestPayload {
  productId: number
  preferredTime?: string
  message?: string
}

export const evaluationApi = {
  /** An individual starts a free trial: 400 when the product offers none or the customer already has/had a subscription. */
  startTrial: (productId: number) =>
    apiRequest<Subscription>('/me/trials', { method: 'POST', body: JSON.stringify({ productId }) }),
  /** The organization starts it: 403 unless the caller is an organization admin or has Manage billing. */
  startOrganizationTrial: (productId: number) =>
    apiRequest<Subscription>('/organization/me/trials', { method: 'POST', body: JSON.stringify({ productId }) }),
  /** Creates a support ticket in the category "Demo request" and returns it. */
  requestDemo: (payload: DemoRequestPayload) =>
    apiRequest<SupportTicket>('/me/demo-requests', { method: 'POST', body: JSON.stringify(payload) }),
}
