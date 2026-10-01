import { apiRequest } from './client'
import type { TaxLine } from './billingApi'

/** C59, REQ-MKT-003 (docs/06-api/api-requirements/cart-checkout.md). Amounts
 * are in the currency's smallest unit. */
export type ContinueAs = 'INDIVIDUAL' | 'ORGANIZATION_MEMBER'

export interface CartPlanOption {
  id: number
  name: string
  billingPeriod: string
  price: number
  currency: string
}

export interface CartItem {
  id: number
  productId: number
  productName?: string
  imageUrl?: string
  planId: number
  planName?: string
  billingPeriod?: string
  unitPrice: number
  unitPriceAtAdd: number
  currency: string
  amount: number
  addedAt: string
  plans: CartPlanOption[]
}

export interface Cart {
  items: CartItem[]
  itemCount: number
  currency?: string
  subtotal: number
  taxLines: TaxLine[]
  taxCalculatedAtPayment: boolean
  total: number
  continueAs: ContinueAs
}

export type CartIssueCode = 'NOT_AVAILABLE' | 'PRICE_CHANGED' | 'ALREADY_SUBSCRIBED' | 'MISSING_DEPENDENCY' | 'CURRENCY_MISMATCH'

export interface CartIssue {
  itemId: number
  code: CartIssueCode
  reason?: 'PRODUCT' | 'PLAN'
  oldPrice?: number
  newPrice?: number
  requiredProductId?: number
  requiredProductName?: string
}

export interface CartValidation {
  valid: boolean
  issues: CartIssue[]
}

export type CartCheckoutResult =
  | { kind: 'INVOICE'; invoiceId: number }
  | { kind: 'ORDER'; orderIds: number[] }

export const cartApi = {
  get: () => apiRequest<Cart>('/me/cart'),
  add: (productId: number, planId: number) =>
    apiRequest<Cart>('/me/cart/items', { method: 'POST', body: JSON.stringify({ productId, planId }) }),
  changePlan: (itemId: number, planId: number) =>
    apiRequest<Cart>(`/me/cart/items/${itemId}`, { method: 'PATCH', body: JSON.stringify({ planId }) }),
  confirmPrice: (itemId: number) =>
    apiRequest<Cart>(`/me/cart/items/${itemId}`, { method: 'PATCH', body: JSON.stringify({ confirmPrice: true }) }),
  remove: (itemId: number) => apiRequest<Cart>(`/me/cart/items/${itemId}`, { method: 'DELETE' }),
  clear: () => apiRequest<void>('/me/cart', { method: 'DELETE' }),
  validate: () => apiRequest<CartValidation>('/me/cart/validate', { method: 'POST' }),
  checkout: () => apiRequest<CartCheckoutResult>('/me/cart/checkout', { method: 'POST' }),
}
