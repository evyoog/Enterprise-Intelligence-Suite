import type { BillingPeriod, Currency } from '../api/productsApi'

const SUFFIX: Record<BillingPeriod, string> = { MONTHLY: '/mo', YEARLY: '/yr', ONE_TIME: '' }

/** The currency amount only, no billing-period suffix, e.g. "₹499.00". */
export function formatMoney(price: number, currency: Currency): string {
  try {
    return new Intl.NumberFormat(undefined, { style: 'currency', currency, minimumFractionDigits: 2 }).format(price)
  } catch {
    return `${currency} ${price.toFixed(2)}`
  }
}

/** A plan price with its currency and billing suffix, e.g. "₹499.00/mo". */
export function formatPlanPrice(price: number, currency: Currency, period: BillingPeriod): string {
  return formatMoney(price, currency) + SUFFIX[period]
}
