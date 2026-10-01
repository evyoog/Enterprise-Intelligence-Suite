/** Payment brands EIS may show (C59, payment-brand-assets.md). */
export type PaymentBrand = 'visa' | 'mastercard' | 'rupay' | 'amex' | 'upi' | 'gpay' | 'phonepe' | 'paytm' | 'bhim' | 'razorpay'

export const BRAND_NAMES: Record<PaymentBrand, string> = {
  visa: 'Visa',
  mastercard: 'Mastercard',
  rupay: 'RuPay',
  amex: 'American Express',
  upi: 'UPI',
  gpay: 'Google Pay',
  phonepe: 'PhonePe',
  paytm: 'Paytm',
  bhim: 'BHIM',
  razorpay: 'Razorpay',
}
