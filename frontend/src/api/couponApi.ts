import { apiRequest } from './client'

/** C100 (PB-1 note): Platform Admin → Billing → Coupons. Every call needs MANAGE_BILLING. Amounts are in the currency's smallest unit. */

export type CouponDiscountType = 'PERCENT' | 'FIXED'
export type CouponStatus = 'ACTIVE' | 'DISABLED' | 'EXPIRED'

export interface Coupon {
  id: number
  code: string
  description?: string | null
  discountType: CouponDiscountType
  /** PERCENT only: 1 to 99. */
  percentOff?: number | null
  /** FIXED only: the amount off, in the currency's smallest unit. */
  amountOff?: number | null
  currency?: string | null
  expiresAt: string
  enabled: boolean
  status: CouponStatus
  redeemedCount: number
  createdAt: string
}

export interface SaveCouponInput {
  code: string
  description: string
  discountType: CouponDiscountType
  percentOff: number | null
  amountOff: number | null
  currency: string | null
  /** An ISO instant. */
  expiresAt: string
}

const base = '/admin/billing/coupons'

export const couponApi = {
  list: () => apiRequest<Coupon[]>(base),
  create: (body: SaveCouponInput) => apiRequest<Coupon>(base, { method: 'POST', body: JSON.stringify(body) }),
  update: (id: number, body: SaveCouponInput) => apiRequest<Coupon>(`${base}/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  setEnabled: (id: number, enabled: boolean) =>
    apiRequest<Coupon>(`${base}/${id}/enabled`, { method: 'PATCH', body: JSON.stringify({ enabled }) }),
  /** 409 once the coupon has been redeemed. */
  remove: (id: number) => apiRequest<void>(`${base}/${id}`, { method: 'DELETE' }),
}
