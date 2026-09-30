import { apiDownload, apiRequest } from './client'
import type { Currency } from './productsApi'

export type InvoiceStatus = 'OPEN' | 'PAID' | 'PARTIALLY_REFUNDED' | 'REFUNDED' | 'VOID'
export type PaymentStatus = 'CREATED' | 'CAPTURED' | 'FAILED' | 'PARTIALLY_REFUNDED' | 'REFUNDED'
export type PaymentMethodType = 'CARD' | 'UPI'

export interface BillingDetails {
  id: number
  billingName: string
  billingEmail: string
  addressLine1: string
  addressLine2?: string
  city: string
  state: string
  postalCode: string
  country: string
  taxId?: string
  updatedAt: string
}

export interface SaveBillingDetailsInput {
  billingName: string
  billingEmail: string
  addressLine1: string
  addressLine2?: string
  city: string
  state: string
  postalCode: string
  country: string
  taxId?: string
}

export interface InvoiceLine {
  description: string
  periodStart?: string
  periodEnd?: string
  quantity: number
  unitAmount: number
  amount: number
}

export interface Payment {
  id: number
  invoiceId: number
  invoiceNumber?: string
  ownerLabel?: string
  status: PaymentStatus
  currency: Currency
  amount: number
  refundedAmount: number
  methodType?: string
  methodNetwork?: string
  methodLast4?: string
  providerPaymentId?: string
  failureReason?: string
  createdAt: string
  capturedAt?: string
  refunds?: { id: number; amount: number; reason: string; status: string; createdAt: string }[]
  webhookEvents?: { eventType: string; receivedAt: string }[]
}

export interface Invoice {
  id: number
  invoiceNumber: string
  status: InvoiceStatus
  currency: Currency
  subtotal: number
  taxAmount: number
  total: number
  periodStart?: string
  periodEnd?: string
  issuedAt: string
  dueAt?: string
  billToSnapshot?: string
  ownerLabel?: string
  lines?: InvoiceLine[]
  payments?: Payment[]
}

export interface PaymentMethod {
  id: number
  type: PaymentMethodType
  network?: string
  last4?: string
  expiryMonth?: number
  expiryYear?: number
  cardType?: string
  issuer?: string
  upiMasked?: string
  isDefault: boolean
  expired: boolean
}

export interface BillingOverview {
  amountDueByCurrency: Record<string, number>
  nextInvoiceDate?: string
  nextInvoicePlanName?: string
  spentThisPeriodByCurrency: Record<string, number>
  spentLastPeriodByCurrency: Record<string, number>
  defaultPaymentMethod?: PaymentMethod
  recentInvoices: Invoice[]
  gatewayConfigured: boolean
}

export interface CreatePaymentResponse {
  paymentId: number
  providerOrderId: string
  amount: number
  currency: string
  keyId: string
}

export interface PaymentMethodSetupResponse {
  providerOrderId: string
  amount: number
  currency: string
  keyId: string
}

export interface GatewayStatus {
  provider: string
  configured: boolean
  liveMode: boolean
  maskedKeyId?: string
  keySecretSet: boolean
  webhookSecretSet: boolean
  webhookUrl: string
  lastWebhookReceivedAt?: string
  lastWebhookEventType?: string
}

interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
}

function scopedApi(base: string) {
  return {
    overview: () => apiRequest<BillingOverview>(`${base}/overview`),
    // 404 (not a nullable 200) when nothing is saved yet — callers catch it
    // and treat it as "no billing details yet", same convention as
    // reviewsApi.getMine elsewhere in this app.
    getDetails: () => apiRequest<BillingDetails>(`${base}/details`),
    saveDetails: (input: SaveBillingDetailsInput) =>
      apiRequest<BillingDetails>(`${base}/details`, { method: 'PUT', body: JSON.stringify(input) }),
    invoices: (page = 0) => apiRequest<Page<Invoice>>(`${base}/invoices?page=${page}`),
    invoiceDetail: (invoiceId: number) => apiRequest<Invoice>(`${base}/invoices/${invoiceId}`),
    downloadDocument: (invoiceId: number, type: 'invoice' | 'receipt') =>
      apiDownload(`${base}/invoices/${invoiceId}/document?type=${type}`),
    createPayment: (invoiceId: number) => apiRequest<CreatePaymentResponse>(`${base}/invoices/${invoiceId}/payments`, { method: 'POST' }),
    confirmPayment: (paymentId: number, body: { providerOrderId: string; providerPaymentId: string; signature: string }) =>
      apiRequest<Payment>(`${base}/payments/${paymentId}/confirm`, { method: 'POST', body: JSON.stringify(body) }),
    payments: (page = 0) => apiRequest<Page<Payment>>(`${base}/payments?page=${page}`),
    paymentMethods: () => apiRequest<PaymentMethod[]>(`${base}/payment-methods`),
    setupPaymentMethod: (type: PaymentMethodType, consent: boolean, makeDefault: boolean) =>
      apiRequest<PaymentMethodSetupResponse>(`${base}/payment-methods/setup`, {
        method: 'POST', body: JSON.stringify({ type, consent, makeDefault }),
      }),
    confirmPaymentMethodSetup: (body: {
      providerOrderId: string; providerPaymentId: string; signature: string
      type: PaymentMethodType; consent: boolean; makeDefault: boolean
    }) => apiRequest<PaymentMethod>(`${base}/payment-methods/setup/confirm`, { method: 'POST', body: JSON.stringify(body) }),
    setDefaultPaymentMethod: (methodId: number) => apiRequest<void>(`${base}/payment-methods/${methodId}/default`, { method: 'POST' }),
    removePaymentMethod: (methodId: number) => apiRequest<void>(`${base}/payment-methods/${methodId}`, { method: 'DELETE' }),
  }
}

/** An individual customer's own billing (REQ-BIL-001, "/me" scope). */
export const myBillingApi = scopedApi('/me/billing')

/** An organization's billing (REQ-BIL-001, "/organization/me" scope). */
export const organizationBillingApi = scopedApi('/organization/me/billing')

/** Platform admin (`MANAGE_BILLING`) — all invoices/payments, refunds, gateway status. */
export const adminBillingApi = {
  invoices: (page = 0) => apiRequest<Page<Invoice>>(`/admin/billing/invoices?page=${page}`),
  invoiceDetail: (invoiceId: number) => apiRequest<Invoice>(`/admin/billing/invoices/${invoiceId}`),
  payments: (page = 0) => apiRequest<Page<Payment>>(`/admin/billing/payments?page=${page}`),
  paymentDetail: (paymentId: number) => apiRequest<Payment>(`/admin/billing/payments/${paymentId}`),
  refund: (paymentId: number, amount: number, reason: string) =>
    apiRequest<Payment>(`/admin/billing/payments/${paymentId}/refunds`, { method: 'POST', body: JSON.stringify({ amount, reason }) }),
  reconcile: (paymentId: number) => apiRequest<Payment>(`/admin/billing/payments/${paymentId}/reconcile`, { method: 'POST' }),
  gatewayStatus: () => apiRequest<GatewayStatus>('/admin/billing/gateway'),
  testGateway: () => apiRequest<void>('/admin/billing/gateway/test', { method: 'POST' }),
}
