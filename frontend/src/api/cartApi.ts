import { apiRequest } from './client'
import type { TaxLine } from './billingApi'

/** C59, REQ-MKT-003 (docs/06-api/api-requirements/cart-checkout.md). Amounts
 * are in the currency's smallest unit. */
export type ContinueAs = 'INDIVIDUAL' | 'ORGANIZATION_BUYER' | 'ORGANIZATION_MEMBER'

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
  /** Organization cart: the seats the subscription starts with (the plan's price is per seat); 1 for an individual. amount = unitPrice x seats. */
  seats: number
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
  /** Net of the coupon discount (subtotal - discount). */
  total: number
  /** INDIVIDUAL and ORGANIZATION_BUYER (an organization admin or a Manage billing holder) buy directly; ORGANIZATION_MEMBER raises a purchase request with a reason (C100 CA-2). */
  continueAs: ContinueAs
  /** C100 (PB-1 note): the applied coupon code and its discount; couponMessage is set when the code can no longer be used (the code stays so it can be removed). */
  couponCode?: string | null
  discount: number
  couponMessage?: string | null
}

export type CartIssueCode = 'NOT_AVAILABLE' | 'PRICE_CHANGED' | 'ALREADY_SUBSCRIBED' | 'MISSING_DEPENDENCY' | 'CURRENCY_MISMATCH' | 'NOT_ELIGIBLE'

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
  /** `reason` is required (up to 500 characters) when an organization member's cart becomes a purchase request (C100 CA-2). */
  checkout: (reason?: string) =>
    apiRequest<CartCheckoutResult>('/me/cart/checkout', { method: 'POST', ...(reason !== undefined ? { body: JSON.stringify({ reason }) } : {}) }),
  /** C100 (PB-1 note): one coupon code per cart; 400 with a message when it cannot be used. */
  applyCoupon: (code: string) => apiRequest<Cart>('/me/cart/coupon', { method: 'PUT', body: JSON.stringify({ code }) }),
  removeCoupon: () => apiRequest<Cart>('/me/cart/coupon', { method: 'DELETE' }),
}
