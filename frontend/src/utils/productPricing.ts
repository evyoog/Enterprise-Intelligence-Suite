import type { BillingPeriod, Currency } from '../api/productsApi'

const SUFFIX: Record<BillingPeriod, string> = { MONTHLY: '/mo', YEARLY: '/yr', ONE_TIME: '' }

/** A plan price with its currency and billing suffix, e.g. "₹499.00/mo". */
export function formatPlanPrice(price: number, currency: Currency, period: BillingPeriod): string {
  let amount: string
  try {
    amount = new Intl.NumberFormat(undefined, { style: 'currency', currency, minimumFractionDigits: 2 }).format(price)
  } catch {
    amount = `${currency} ${price.toFixed(2)}`
  }
  return amount + SUFFIX[period]
}
