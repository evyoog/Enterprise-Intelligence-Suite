import { apiRequest } from './client'
import type { Currency, Product, ProductPlan } from './productsApi'

/** C102, REQ-MKT-005 (TR-2 A): the compare page. Amounts are in major units (12.5 = 12.50) in each item's own currency. */
export type CompareTaxStatus = 'ESTIMATED' | 'NOT_SIGNED_IN' | 'NEEDS_BILLING_DETAILS' | 'AT_CHECKOUT'

export interface CompareTax {
  status: CompareTaxStatus
  amount: number | null
  total: number | null
  message: string | null
}

export interface CompareItem {
  product: Product
  /** The chosen plan, else the entry (cheapest) plan; null for a product without plans (its flat price is `unitPrice`). */
  plan: ProductPlan | null
  currency: Currency
  unitPrice: number
  billingPeriod: string | null
  seats: number
  subtotal: number
  tax: CompareTax
  includedFeatures: string[]
  trialDays: number | null
}

export interface Comparison {
  seats: number
  items: CompareItem[]
}

/** One side of the comparison: a product, or a product with one of its plans. */
export interface CompareRef {
  productId: number
  planId?: number
}

export const MIN_COMPARE_ITEMS = 2
export const MAX_COMPARE_ITEMS = 4
export const MIN_SEATS = 1
export const MAX_SEATS = 100000

/** `1,2:5` — the same text the page keeps in its URL and the API takes. */
export function formatCompareItems(items: CompareRef[]): string {
  return items.map((i) => (i.planId != null ? `${i.productId}:${i.planId}` : String(i.productId))).join(',')
}

export const compareApi = {
  // Matches EvaluationController#compare — public; the token (when there is one) only adds the tax estimate.
  compare: (items: CompareRef[], seats?: number) => {
    const query = new URLSearchParams({ items: formatCompareItems(items) })
    if (seats !== undefined) query.set('seats', String(seats))
    return apiRequest<Comparison>(`/products/compare?${query.toString().replace(/%2C/g, ',').replace(/%3A/g, ':')}`)
  },
}
