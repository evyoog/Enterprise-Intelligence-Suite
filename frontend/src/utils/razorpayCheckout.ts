/** Loads Razorpay's own Checkout script on first use and opens it — card/UPI
 * entry happens entirely inside that window (BR-BIL-001: EIS never collects
 * card data itself). Resolves with the result Razorpay's own handler
 * receives; rejects if the customer dismisses the window without paying. */

interface RazorpayCheckoutOptions {
  key: string
  amount: number
  currency: string
  order_id: string
  name: string
  description: string
  handler: (response: { razorpay_order_id: string; razorpay_payment_id: string; razorpay_signature: string }) => void
  modal?: { ondismiss?: () => void }
  prefill?: { method?: string }
  theme?: { color?: string }
}

declare global {
  interface Window {
    Razorpay?: new (options: RazorpayCheckoutOptions) => { open: () => void }
  }
}

let scriptPromise: Promise<void> | null = null

function loadCheckoutScript(): Promise<void> {
  if (window.Razorpay) return Promise.resolve()
  if (!scriptPromise) {
    scriptPromise = new Promise((resolve, reject) => {
      const script = document.createElement('script')
      script.src = 'https://checkout.razorpay.com/v1/checkout.js'
      script.onload = () => resolve()
      script.onerror = () => reject(new Error('Could not load the payment window.'))
      document.body.appendChild(script)
    })
  }
  return scriptPromise
}

export interface RazorpayResult {
  providerOrderId: string
  providerPaymentId: string
  signature: string
}

export async function openRazorpayCheckout(options: {
  keyId: string
  orderId: string
  amount: number
  currency: string
  name: string
  description: string
  /** C55: preselects a method inside Razorpay's window ("card", "upi").
   * Card/UPI details are still entered only there (BR-BIL-001). */
  method?: 'card' | 'upi'
}): Promise<RazorpayResult> {
  await loadCheckoutScript()
  return new Promise((resolve, reject) => {
    const checkout = new window.Razorpay!({
      key: options.keyId,
      amount: options.amount,
      currency: options.currency,
      order_id: options.orderId,
      name: options.name,
      description: options.description,
      handler: (response) =>
        resolve({
          providerOrderId: response.razorpay_order_id,
          providerPaymentId: response.razorpay_payment_id,
          signature: response.razorpay_signature,
        }),
      modal: { ondismiss: () => reject(new Error('cancelled')) },
      ...(options.method ? { prefill: { method: options.method } } : {}),
      theme: { color: '#4c63ff' },
    })
    checkout.open()
  })
}
