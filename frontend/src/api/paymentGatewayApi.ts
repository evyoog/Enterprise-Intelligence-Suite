import { apiRequest } from './client'

/** C98 / REQ-BIL-003: Platform Admin → Billing → Payment Gateways. Every call needs MANAGE_BILLING. No secret is ever sent back: a field says only
 * whether it is set, where it comes from and (for a public identifier) a masked form. */

export type GatewayImplementation = 'IMPLEMENTED' | 'PLANNED'
export type ConfigurationStatus = 'NOT_AVAILABLE' | 'NOT_CONFIGURED' | 'CONFIGURATION_INCOMPLETE' | 'CONFIGURED'
export type ConnectionStatus =
  | 'NOT_CONFIGURED' | 'CONFIGURATION_INCOMPLETE' | 'NOT_TESTED' | 'CONNECTION_SUCCESSFUL' | 'AUTHENTICATION_FAILED'
  | 'PROVIDER_UNAVAILABLE' | 'UNSUPPORTED_CONFIGURATION' | 'TEST_FAILED'

export interface GatewayActions { configure: boolean; test: boolean; activate: boolean; deactivate: boolean; setDefault: boolean }

export interface Gateway {
  code: string
  name: string
  description: string
  logo: string
  implementation: GatewayImplementation
  supportedMethods: string[]
  enabledMethods: string[]
  currencies: string[]
  countries: string[]
  environment?: 'TEST' | 'LIVE'
  configurationStatus: ConfigurationStatus
  connectionStatus: ConnectionStatus
  connectionMessage?: string
  testedAt?: string
  activated: boolean
  defaultGateway: boolean
  actions: GatewayActions
}

export interface GatewaySummary { registered: number; implemented: number; configured: number; active: number; needsConfiguration: number }

export interface GatewayField {
  key: string
  label: string
  secret: boolean
  required: boolean
  webhook: boolean
  help?: string
  configured: boolean
  source: 'STORED' | 'ENVIRONMENT' | 'NONE'
  maskedValue?: string
}

export interface GatewayWebhook {
  url: string
  supported: boolean
  secretConfigured: boolean
  signatureStatus: 'VERIFIED' | 'NOT_SEEN' | 'NOT_APPLICABLE'
  lastEventAt?: string
  lastEventType?: string
  failureCount: number
  lastFailureAt?: string
  lastFailureReason?: string
  events: string[]
  reachableFromInternet: boolean
}

export interface GatewayEvidence { authenticated: boolean; webhookReceived: boolean; paymentCaptured: boolean; refundMade: boolean }

export interface GatewayDetail {
  gateway: Gateway
  transactionTypes: string[]
  environments: string[]
  webhookSupport: boolean
  refundSupport: boolean
  recurringSupport: boolean
  fields: GatewayField[]
  setupSteps: string[]
  webhook: GatewayWebhook
  evidence: GatewayEvidence
  configVersion: number
  credentialsUpdatedAt?: string
  encryptionAvailable: boolean
  credentialsUnreadable: boolean
  recentEvents: { eventType: string; receivedAt: string; processedAt?: string; paymentId?: number }[]
  history: { at: string; action: string; actor?: string; detail?: string }[]
}

export interface GatewayConnection { status: ConnectionStatus; message?: string; testedAt?: string; successful: boolean }

const base = '/admin/billing/gateways'
const json = (body: unknown) => JSON.stringify(body)

export const paymentGatewayApi = {
  list: () => apiRequest<Gateway[]>(base),
  summary: () => apiRequest<GatewaySummary>(`${base}/summary`),
  detail: (code: string) => apiRequest<GatewayDetail>(`${base}/${code}`),
  /** A blank field keeps the stored value. An active gateway is re-tested in the same request; a failing test saves nothing (HTTP 409). */
  saveConfiguration: (code: string, body: { environment?: string; fields: Record<string, string> }) =>
    apiRequest<GatewayDetail>(`${base}/${code}/configuration`, { method: 'PUT', body: json(body) }),
  test: (code: string) => apiRequest<GatewayConnection>(`${base}/${code}/test`, { method: 'POST' }),
  activate: (code: string) => apiRequest<GatewayDetail>(`${base}/${code}/activate`, { method: 'POST' }),
  deactivate: (code: string) => apiRequest<GatewayDetail>(`${base}/${code}/deactivate`, { method: 'POST' }),
  setMethods: (code: string, methods: string[]) => apiRequest<GatewayDetail>(`${base}/${code}/methods`, { method: 'PUT', body: json({ methods }) }),
  setDefault: (code: string) => apiRequest<Gateway[]>(`${base}/default`, { method: 'PUT', body: json({ code }) }),
}
