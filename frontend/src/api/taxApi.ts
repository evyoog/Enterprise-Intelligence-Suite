import { apiRequest, ApiError } from './client'
import type { Invoice } from './billingApi'

/** C96 / REQ-BIL-002: Platform Admin → Billing → Tax Configuration. Every call needs MANAGE_BILLING. No credential is ever sent or received. */

export type TaxType = 'GST' | 'VAT' | 'SALES_TAX' | 'OTHER'
export type TaxTreatment = 'TAXABLE' | 'EXEMPT' | 'ZERO_RATED' | 'NIL_RATED' | 'REVERSE_CHARGE' | 'OUT_OF_SCOPE'
export type CustomerTypeCondition = 'ANY' | 'INDIVIDUAL' | 'BUSINESS'
export type SupplyType = 'ANY' | 'INTRA_STATE' | 'INTER_STATE' | 'CROSS_BORDER'
export type TaxIdCondition = 'ANY' | 'PROVIDED' | 'NOT_PROVIDED'
export type RuleApprovalStatus = 'DRAFT' | 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED'
export type EngineStatus = 'READY' | 'ATTENTION' | 'BLOCKING'

export interface TaxStatusMessage { level: 'INFO' | 'WARNING' | 'ERROR'; code: string; text: string }

export interface TaxStatus {
  mode: 'INTERNAL' | 'EXTERNAL'
  modeLabel: string
  externalEngineEnabled: boolean
  status: EngineStatus
  messages: TaxStatusMessage[]
  configVersion: number
  modeChangedAt?: string
  modeChangedBy?: string
  allowSelfApproval: boolean
  internal: { rulesInForce: number; pendingApproval: number; draftRules: number; countriesCovered: string[] }
  external: {
    providerCode?: string
    providerName?: string
    providers: { code: string; name: string }[]
    configured: boolean
    /** Which environment variable is set, never its value. */
    credentials: Record<string, boolean>
    endpointHost?: string
    environment: 'SANDBOX' | 'PRODUCTION'
    companyCode?: string
    timeoutSeconds: number
    maxAttempts: number
    validationStatus: 'NOT_VALIDATED' | 'PASSED' | 'FAILED'
    validatedAt?: string
    validationSummary?: string
    countries: string[]
    currencies: string[]
    transactionTypes: string[]
  }
  invoicesWaiting: number
  lastCalculation?: { at: string; engine: string; status: string; reasonCode?: string; reasonMessage?: string }
}

export interface ValidationCheck { code: string; passed: boolean; label: string; detail: string }
export interface ValidationReport {
  passed: boolean
  validatedAt: string
  checks: ValidationCheck[]
  countries: string[]
  currencies: string[]
  transactionTypes: string[]
}
export interface TaxModeResult { changed: boolean; status: TaxStatus; report?: ValidationReport }

export interface TaxComponentInput { code: string; name: string; rate: number; compound: boolean }

export interface TaxRule {
  id: number
  ruleKey: string
  version: number
  latestVersion: boolean
  name: string
  countryCode: string
  countryName: string
  stateCode?: string
  stateName?: string
  jurisdiction?: string
  taxType: TaxType
  productClassCode?: string
  customerType: CustomerTypeCondition
  supplyType: SupplyType
  taxIdCondition: TaxIdCondition
  treatment: TaxTreatment
  effectiveFrom: string
  effectiveTo?: string
  approvalStatus: RuleApprovalStatus
  active: boolean
  inForce: boolean
  notes?: string
  rejectionReason?: string
  createdAt: string
  createdBy?: string
  lastEditedBy?: string
  approvedAt?: string
  approvedBy?: string
  components: TaxComponentInput[]
  totalRate: number
}

export interface SaveTaxRuleInput {
  name: string
  countryCode: string
  stateCode?: string
  jurisdiction?: string
  taxType: TaxType
  productClassCode?: string
  customerType: CustomerTypeCondition
  supplyType: SupplyType
  taxIdCondition: TaxIdCondition
  treatment: TaxTreatment
  effectiveFrom: string
  effectiveTo?: string
  notes?: string
  components: TaxComponentInput[]
}

export interface TaxClass { id: number; code: string; name: string; hsnSacCode?: string; description?: string; enabled: boolean; productCount: number }
export interface SaveTaxClassInput { code: string; name: string; hsnSacCode?: string; description?: string; enabled: boolean }
export interface ProductTaxClass { productId: number; name: string; status: string; category?: string; taxClassCode?: string }

export interface TaxRegistration {
  id?: number
  countryCode: string
  countryName: string
  stateCode?: string
  stateName?: string
  taxType: TaxType
  registrationNumber: string
  effectiveFrom?: string
  effectiveTo?: string
  enabled: boolean
  fromBusinessProfile: boolean
}
export interface SaveTaxRegistrationInput {
  countryCode: string; stateCode?: string; taxType: TaxType; registrationNumber: string; effectiveFrom?: string; effectiveTo?: string; enabled: boolean
}

export type ExemptionStatus = 'PENDING_APPROVAL' | 'APPROVED' | 'REVOKED'
export interface TaxExemption {
  id: number
  ownerType: 'CUSTOMER' | 'ORGANIZATION'
  ownerId: number
  ownerLabel: string
  countryCode: string
  countryName: string
  reason: string
  certificateNumber?: string
  effectiveFrom: string
  effectiveTo?: string
  status: ExemptionStatus
  inForce: boolean
  createdBy?: string
  createdAt: string
  approvedBy?: string
  approvedAt?: string
}
export interface SaveTaxExemptionInput {
  ownerType: 'CUSTOMER' | 'ORGANIZATION'; ownerId: number; countryCode: string; reason: string; certificateNumber?: string; effectiveFrom: string; effectiveTo?: string
}

export interface TaxReference {
  countries: { code: string; name: string }[]
  states: Record<string, { code: string; name: string; gstCode?: string }[]>
  taxTypes: TaxType[]
  treatments: TaxTreatment[]
  customerTypes: CustomerTypeCondition[]
  supplyTypes: SupplyType[]
  taxIdConditions: TaxIdCondition[]
  supplierCountryCode?: string
  supplierStateCode?: string
}

export interface TaxPreviewInput {
  amount: number; currency: string; country: string; state?: string; productClassCode?: string
  customerType?: CustomerTypeCondition; taxId?: string; date?: string
}
export interface TaxPreview {
  status: 'CALCULATED' | 'BLOCKED'
  blockCode?: string
  blockMessage?: string
  placeOfSupply?: string
  taxableAmount: number
  totalTax: number
  total: number
  components: { code: string; name: string; taxType?: string; jurisdiction?: string; treatment: string; rate?: number; taxableAmount: number; taxAmount: number; note?: string }[]
}

export interface BlockedInvoice {
  invoiceId: number; ownerLabel?: string; currency: string; subtotal: number; createdAt: string; blockCode?: string; blockReason?: string; taxEngine?: string
}

const base = '/admin/billing/tax'
const json = (body: unknown) => JSON.stringify(body)

export const taxApi = {
  status: () => apiRequest<TaxStatus>(`${base}/status`),
  /**
   * Switching on runs the provider checks in the same request. A refused switch-on comes back as HTTP 409 with the same body
   * ({ changed: false, report }), which is returned here instead of thrown so the screen can list the failed checks.
   */
  setMode: async (externalEngineEnabled: boolean): Promise<TaxModeResult> => {
    try {
      return await apiRequest<TaxModeResult>(`${base}/mode`, { method: 'PUT', body: json({ externalEngineEnabled }) })
    } catch (e) {
      if (e instanceof ApiError && e.status === 409 && e.detail && 'report' in e.detail) return e.detail as unknown as TaxModeResult
      throw e
    }
  },
  saveProvider: (body: { providerCode: string; companyCode?: string; environment: string; timeoutSeconds: number; maxAttempts: number }) =>
    apiRequest<TaxStatus>(`${base}/provider`, { method: 'PUT', body: json(body) }),
  validateProvider: () => apiRequest<ValidationReport>(`${base}/provider/validate`, { method: 'POST' }),
  saveApprovalPolicy: (allowSelfApproval: boolean) => apiRequest<TaxStatus>(`${base}/approval-policy`, { method: 'PUT', body: json({ allowSelfApproval }) }),
  reference: () => apiRequest<TaxReference>(`${base}/reference`),

  rules: () => apiRequest<TaxRule[]>(`${base}/rules`),
  createRule: (body: SaveTaxRuleInput) => apiRequest<TaxRule>(`${base}/rules`, { method: 'POST', body: json(body) }),
  updateRule: (id: number, body: SaveTaxRuleInput) => apiRequest<TaxRule>(`${base}/rules/${id}`, { method: 'PUT', body: json(body) }),
  deleteRule: (id: number) => apiRequest<void>(`${base}/rules/${id}`, { method: 'DELETE' }),
  submitRule: (id: number) => apiRequest<TaxRule>(`${base}/rules/${id}/submit`, { method: 'POST' }),
  approveRule: (id: number) => apiRequest<TaxRule>(`${base}/rules/${id}/approve`, { method: 'POST' }),
  rejectRule: (id: number, reason: string) => apiRequest<TaxRule>(`${base}/rules/${id}/reject`, { method: 'POST', body: json({ reason }) }),
  activateRule: (id: number) => apiRequest<TaxRule>(`${base}/rules/${id}/activate`, { method: 'POST' }),
  deactivateRule: (id: number) => apiRequest<TaxRule>(`${base}/rules/${id}/deactivate`, { method: 'POST' }),
  newRuleVersion: (id: number) => apiRequest<TaxRule>(`${base}/rules/${id}/new-version`, { method: 'POST' }),
  preview: (body: TaxPreviewInput) => apiRequest<TaxPreview>(`${base}/preview`, { method: 'POST', body: json(body) }),

  classes: () => apiRequest<TaxClass[]>(`${base}/classes`),
  createClass: (body: SaveTaxClassInput) => apiRequest<TaxClass>(`${base}/classes`, { method: 'POST', body: json(body) }),
  updateClass: (id: number, body: SaveTaxClassInput) => apiRequest<TaxClass>(`${base}/classes/${id}`, { method: 'PUT', body: json(body) }),
  products: () => apiRequest<ProductTaxClass[]>(`${base}/products`),
  assignClass: (productId: number, taxClassCode: string | null) =>
    apiRequest<ProductTaxClass>(`${base}/products/${productId}/class`, { method: 'PUT', body: json({ taxClassCode }) }),

  registrations: () => apiRequest<TaxRegistration[]>(`${base}/registrations`),
  createRegistration: (body: SaveTaxRegistrationInput) => apiRequest<TaxRegistration>(`${base}/registrations`, { method: 'POST', body: json(body) }),
  updateRegistration: (id: number, body: SaveTaxRegistrationInput) => apiRequest<TaxRegistration>(`${base}/registrations/${id}`, { method: 'PUT', body: json(body) }),
  deleteRegistration: (id: number) => apiRequest<void>(`${base}/registrations/${id}`, { method: 'DELETE' }),

  exemptions: () => apiRequest<TaxExemption[]>(`${base}/exemptions`),
  createExemption: (body: SaveTaxExemptionInput) => apiRequest<TaxExemption>(`${base}/exemptions`, { method: 'POST', body: json(body) }),
  approveExemption: (id: number) => apiRequest<TaxExemption>(`${base}/exemptions/${id}/approve`, { method: 'POST' }),
  revokeExemption: (id: number) => apiRequest<TaxExemption>(`${base}/exemptions/${id}/revoke`, { method: 'POST' }),

  blockedInvoices: () => apiRequest<BlockedInvoice[]>(`${base}/blocked-invoices`),
  retryBlockedInvoice: (invoiceId: number) => apiRequest<Invoice>(`${base}/blocked-invoices/${invoiceId}/retry`, { method: 'POST' }),
}
