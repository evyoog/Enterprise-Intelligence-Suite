import '../i18n'
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/client'
import { CheckoutPage, LegacyCheckoutRedirect } from './CheckoutPage'
import { openRazorpayCheckout } from '../utils/razorpayCheckout'

const getProduct = vi.fn()
vi.mock('../api/productsApi', async () => {
  const actual = await vi.importActual<typeof import('../api/productsApi')>('../api/productsApi')
  return { ...actual, productsApi: { ...actual.productsApi, get: (id: number) => getProduct(id) } }
})

const subscribe = vi.fn()
vi.mock('../api/registrationApi', async () => {
  const actual = await vi.importActual<typeof import('../api/registrationApi')>('../api/registrationApi')
  return { ...actual, myProductsApi: { ...actual.myProductsApi, subscribe: (id: number) => subscribe(id) } }
})

const getDetails = vi.fn()
const saveDetails = vi.fn()
const checkout = vi.fn()
const payByInvoice = vi.fn()
const createPayment = vi.fn()
const confirmPayment = vi.fn()
const invoiceDetail = vi.fn()
const paymentMethods = vi.fn()
const removePaymentMethod = vi.fn()
const setPurchaseOrder = vi.fn()
vi.mock('../api/billingApi', async () => {
  const actual = await vi.importActual<typeof import('../api/billingApi')>('../api/billingApi')
  return {
    ...actual,
    myBillingApi: {
      ...actual.myBillingApi,
      getDetails: () => getDetails(),
      saveDetails: (input: unknown) => saveDetails(input),
      checkout: (p: unknown) => checkout(p),
      payByInvoice: (id: number) => payByInvoice(id),
      createPayment: (id: number, body: unknown) => createPayment(id, body),
      confirmPayment: (id: number, body: unknown) => confirmPayment(id, body),
      invoiceDetail: (id: number) => invoiceDetail(id),
      setPurchaseOrder: (id: number, po: string) => setPurchaseOrder(id, po),
      paymentMethods: () => paymentMethods(),
      removePaymentMethod: (id: number) => removePaymentMethod(id),
    },
  }
})

vi.mock('../utils/razorpayCheckout', () => ({
  openRazorpayCheckout: vi.fn().mockResolvedValue({ providerOrderId: 'order_1', providerPaymentId: 'pay_1', signature: 'sig_1' }),
}))

const freeProduct = {
  id: 7, name: 'Valam.ai', description: 'Analytics.', price: 0, status: 'ACTIVE' as const,
  ssoConnected: false, featured: false, platforms: [], plans: [], version: 1, dependsOnProductIds: [],
}

const savedDetails = {
  id: 1, billingName: 'Jane', billingEmail: 'jane@example.com', addressLine1: '1 Main St',
  city: 'Chennai', state: 'TN', postalCode: '600001', country: 'India', updatedAt: '2026-10-01T00:00:00Z',
}

const visa = { id: 5, type: 'CARD', network: 'Visa', last4: '2860', expiryMonth: 8, expiryYear: 2030, isDefault: true, expired: false }
const expiredMastercard = { id: 6, type: 'CARD', network: 'Mastercard', last4: '5014', expiryMonth: 1, expiryYear: 2024, isDefault: false, expired: true }

function summary(overrides: Record<string, unknown> = {}) {
  return {
    invoiceId: 9, invoiceNumber: 'INV-2026-000009', invoiceStatus: 'OPEN', paymentRoute: null,
    subscriptionId: 1, subscriptionStatus: 'ACTIVE', currency: 'USD',
    items: [
      { productId: 7, productName: 'Valam.ai', planName: 'Pro', billingPeriod: 'MONTHLY', amount: 1500 },
      { productId: 8, productName: 'Varthan.ai', planName: 'Team', billingPeriod: 'YEARLY', amount: 499 },
    ],
    subtotal: 1999, taxLines: [], total: 1999, dueAt: '2026-10-01T00:00:00Z', billingEmail: 'jane@example.com',
    gatewayConfigured: true, payByInvoiceAllowed: true,
    ...overrides,
  }
}

function renderAt(url: string) {
  return render(
    <MemoryRouter initialEntries={[url]}>
      <Routes>
        <Route path="/checkout" element={<CheckoutPage />} />
        <Route path="/checkout/:productId" element={<LegacyCheckoutRedirect />} />
        <Route path="/cart" element={<div>Cart page</div>} />
        <Route path="/login" element={<div>Sign in page</div>} />
      </Routes>
    </MemoryRouter>
  )
}

const tiles = () => within(screen.getByRole('radiogroup', { name: 'Payment' })).getAllByRole('radio')
const payButton = () => screen.getByRole('button', { name: /Pay \| \$19\.99|Generate invoice/ })

describe('CheckoutPage (C55, C59 redesign)', () => {
  beforeEach(() => {
    for (const m of [getProduct, subscribe, getDetails, saveDetails, checkout, payByInvoice, createPayment, confirmPayment, invoiceDetail, paymentMethods, removePaymentMethod, setPurchaseOrder]) m.mockReset()
    vi.mocked(openRazorpayCheckout).mockClear()
    getProduct.mockResolvedValue(freeProduct)
    getDetails.mockRejectedValue(new ApiError(404, 'No billing details saved yet'))
    paymentMethods.mockResolvedValue([])
  })

  it('forwards the legacy /checkout/:productId link to the cart', async () => {
    renderAt('/checkout/7')
    expect(await screen.findByText('Cart page')).toBeInTheDocument()
  })

  it('sends the visitor to sign in when the session has expired', async () => {
    getDetails.mockRejectedValue(new ApiError(401, 'Not authenticated'))
    renderAt('/checkout?productId=7')
    expect(await screen.findByText('Sign in page')).toBeInTheDocument()
  })

  it('validates required billing fields before continuing', async () => {
    renderAt('/checkout?productId=7')
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Continue to payment' }))
    expect((await screen.findAllByText('This field is required.')).length).toBeGreaterThan(0)
    expect(saveDetails).not.toHaveBeenCalled()
  })

  it('keeps the free-plan flow: subscribe on continue and show the free confirmation', async () => {
    getDetails.mockResolvedValue(savedDetails)
    subscribe.mockResolvedValue({ id: 1, productId: 7, productName: 'Valam.ai', status: 'ACTIVE' })
    checkout.mockResolvedValue(summary({ invoiceId: null, invoiceNumber: null, invoiceStatus: null, total: 0, subtotal: 0 }))
    renderAt('/checkout?productId=7')
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Continue to payment' }))
    expect(await screen.findByText("You're subscribed")).toBeInTheDocument()
    expect(subscribe).toHaveBeenCalledWith(7)
  })

  it('shows the breadcrumb from the cart, the billing summary with Change, the amount due and the cart summary', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary())
    renderAt('/checkout?invoiceId=9&from=cart&step=payment')

    await screen.findByRole('radiogroup', { name: 'Payment' })
    const crumbs = screen.getByRole('navigation', { name: 'Checkout progress' })
    expect(within(crumbs).getByRole('link', { name: 'Cart' })).toHaveAttribute('href', '/cart')
    expect(within(crumbs).getByText('Payment').closest('li')).toHaveAttribute('aria-current', 'step')
    expect(screen.getByText('jane@example.com')).toBeInTheDocument()
    expect(screen.getByRole('img', { name: 'Amount due $19.99' })).toBeInTheDocument()
    const aside = screen.getByRole('complementary', { name: /Order summary/ })
    expect(aside).toHaveTextContent('Valam.ai')
    expect(aside).toHaveTextContent('Varthan.ai')
    expect(within(aside).getByText('Tax: none for this region')).toBeInTheDocument()
    expect(within(aside).getByRole('link', { name: 'Edit cart' })).toHaveAttribute('href', '/cart')

    await userEvent.setup().click(screen.getByRole('button', { name: 'Change Contact' }))
    expect(await screen.findByRole('textbox', { name: /Billing email/ })).toHaveValue('jane@example.com')
  })

  it('shows the real tax components, rates and the place of supply from the invoice (C96)', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary({
      subtotal: 100000, total: 118000, taxLines: [{ name: 'Central GST', ratePercent: 9, amount: 9000 }, { name: 'State GST', ratePercent: 9, amount: 9000 }],
      placeOfSupply: 'Tamil Nadu, India',
    }))
    renderAt('/checkout?invoiceId=9&from=cart&step=payment')
    await screen.findByRole('radiogroup', { name: 'Payment' })
    const aside = screen.getByRole('complementary', { name: /Order summary/ })
    expect(within(aside).getByText('Central GST (9 %)')).toBeInTheDocument()
    expect(within(aside).getByText('State GST (9 %)')).toBeInTheDocument()
    expect(within(aside).getByText('Place of supply: Tamil Nadu, India')).toBeInTheDocument()
    expect(within(aside).queryByText('Tax: none for this region')).not.toBeInTheDocument()
  })

  it('does not offer payment for a Draft invoice whose tax could not be determined (C96)', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary({ invoiceStatus: 'DRAFT', invoiceNumber: null, taxPending: true }))
    renderAt('/checkout?invoiceId=9&from=cart')
    expect(await screen.findByRole('heading', { name: 'We are preparing your invoice' })).toBeInTheDocument()
    expect(screen.getByText(/nothing can be paid right now/)).toBeInTheDocument()
    expect(screen.queryByRole('radiogroup', { name: 'Payment' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Pay \| / })).not.toBeInTheDocument()
    expect(createPayment).not.toHaveBeenCalled()
  })

  it('shows five method tiles and keeps Pay disabled until a tile and the terms are chosen', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary())
    renderAt('/checkout?invoiceId=9&step=payment')
    await screen.findByRole('radiogroup', { name: 'Payment' })

    expect(tiles().map((r) => r.textContent)).toEqual([
      expect.stringContaining('Card'), expect.stringContaining('UPI'), expect.stringContaining('Netbanking'),
      expect.stringContaining('Wallets'), expect.stringContaining('Pay by invoice'),
    ])
    const user = userEvent.setup()
    expect(payButton()).toBeDisabled()
    // AC-34: the footer's Complete order never starts a payment and stays disabled here.
    expect(screen.getByRole('button', { name: 'Complete order' })).toBeDisabled()
    await user.click(tiles()[2])
    expect(tiles()[2]).toHaveAttribute('aria-checked', 'true')
    expect(payButton()).toBeDisabled()
    await user.click(screen.getByRole('checkbox', { name: /I agree/ }))
    expect(payButton()).toBeEnabled()
  })

  it('moves between tiles with the arrow keys', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary())
    renderAt('/checkout?invoiceId=9&step=payment')
    await screen.findByRole('radiogroup', { name: 'Payment' })
    const user = userEvent.setup()
    await user.click(tiles()[0])
    await user.keyboard('{ArrowRight}')
    expect(tiles()[1]).toHaveAttribute('aria-checked', 'true')
    expect(tiles()[1]).toHaveFocus()
  })

  it('pays with the default saved card; an expired card cannot be chosen', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary())
    paymentMethods.mockResolvedValue([visa, expiredMastercard])
    createPayment.mockResolvedValue({ paymentId: 3, providerOrderId: 'order_1', amount: 1999, currency: 'USD', keyId: 'rzp_test_1' })
    confirmPayment.mockResolvedValue({ status: 'CAPTURED', amount: 1999, currency: 'USD', methodType: 'card', methodNetwork: 'Visa', methodLast4: '2860' })
    renderAt('/checkout?invoiceId=9&step=payment')
    await screen.findByRole('radiogroup', { name: 'Payment' })
    const user = userEvent.setup()
    await user.click(tiles()[0])

    const saved = screen.getByRole('radiogroup', { name: 'Saved cards' })
    expect(within(saved).getByRole('radio', { name: 'Visa ending 2860' })).toHaveAttribute('aria-checked', 'true')
    const expired = within(saved).getByRole('radio', { name: 'Mastercard ending 5014' })
    expect(expired).toHaveAttribute('aria-disabled', 'true')
    expect(within(saved).getByText('Expired')).toBeInTheDocument()
    await user.click(expired)
    expect(expired).toHaveAttribute('aria-checked', 'false')

    await user.click(screen.getByRole('checkbox', { name: /I agree/ }))
    await user.click(payButton())
    await waitFor(() => expect(confirmPayment).toHaveBeenCalledWith(3, { providerOrderId: 'order_1', providerPaymentId: 'pay_1', signature: 'sig_1' }))
    expect(createPayment).toHaveBeenCalledWith(9, { method: 'card', paymentMethodId: 5 })
    expect(vi.mocked(openRazorpayCheckout).mock.calls[0][0]).toMatchObject({ method: 'card' })
    expect(await screen.findByRole('heading', { name: 'Payment successful' })).toBeInTheDocument()
  })

  it('never renders a card input: the "use another card" fields are placeholders (BR-BIL-001)', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary())
    const { container } = renderAt('/checkout?invoiceId=9&step=payment')
    await screen.findByRole('radiogroup', { name: 'Payment' })
    await userEvent.setup().click(tiles()[0])
    expect(screen.getByText(/never stored by eVyoog/)).toBeInTheDocument()
    // The only text box is the optional purchase-order number (C97 BI-9); there is never a card field.
    expect(container.querySelectorAll('input:not([type="checkbox"])')).toHaveLength(1)
    expect(screen.getAllByRole('textbox')).toHaveLength(1)
    expect(screen.getByRole('textbox', { name: /Purchase order number/ })).toBeInTheDocument()
  })

  it('removes a saved card from its menu after confirmation', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary())
    paymentMethods.mockResolvedValue([visa])
    removePaymentMethod.mockResolvedValue(undefined)
    renderAt('/checkout?invoiceId=9&step=payment')
    await screen.findByRole('radiogroup', { name: 'Payment' })
    const user = userEvent.setup()
    await user.click(tiles()[0])
    await user.click(screen.getByRole('button', { name: 'More actions for the card ending 2860' }))
    await user.click(screen.getByRole('menuitem', { name: 'Remove' }))
    await user.click(within(screen.getByRole('dialog', { name: 'Remove saved card?' })).getByRole('button', { name: 'Remove' }))
    await waitFor(() => expect(removePaymentMethod).toHaveBeenCalledWith(5))
    await waitFor(() => expect(screen.queryByRole('radiogroup', { name: 'Saved cards' })).not.toBeInTheDocument())
  })

  it('pays by netbanking with that method preselected in Razorpay', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary())
    createPayment.mockResolvedValue({ paymentId: 3, providerOrderId: 'order_1', amount: 1999, currency: 'USD', keyId: 'rzp_test_1' })
    confirmPayment.mockResolvedValue({ status: 'CAPTURED', amount: 1999, currency: 'USD' })
    renderAt('/checkout?invoiceId=9&step=payment')
    await screen.findByRole('radiogroup', { name: 'Payment' })
    const user = userEvent.setup()
    await user.click(tiles()[2])
    expect(screen.getByText(/choose your bank in the payment provider/)).toBeInTheDocument()
    await user.click(screen.getByRole('checkbox', { name: /I agree/ }))
    await user.click(payButton())
    await waitFor(() => expect(createPayment).toHaveBeenCalledWith(9, { method: 'netbanking' }))
    expect(vi.mocked(openRazorpayCheckout).mock.calls[0][0]).toMatchObject({ method: 'netbanking' })
  })

  it('shows a failed result with retry options when the customer closes Razorpay', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary())
    createPayment.mockResolvedValue({ paymentId: 3, providerOrderId: 'order_1', amount: 1999, currency: 'USD', keyId: 'rzp_test_1' })
    vi.mocked(openRazorpayCheckout).mockRejectedValueOnce(new Error('cancelled'))
    renderAt('/checkout?invoiceId=9&step=payment')
    await screen.findByRole('radiogroup', { name: 'Payment' })
    const user = userEvent.setup()
    await user.click(tiles()[1])
    await user.click(screen.getByRole('checkbox', { name: /I agree/ }))
    await user.click(payButton())
    expect(await screen.findByRole('heading', { name: 'Payment failed' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Try again' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Choose another method' })).toBeInTheDocument()
    expect(confirmPayment).not.toHaveBeenCalled()
  })

  it('disables the online tiles when the gateway is not configured but keeps Pay by invoice', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary({ gatewayConfigured: false }))
    renderAt('/checkout?invoiceId=9&step=payment')
    await screen.findByRole('radiogroup', { name: 'Payment' })
    expect(screen.getByText(/Online payments are not available/i)).toBeInTheDocument()
    const all = tiles()
    for (const tile of all.slice(0, 4)) expect(tile).toHaveAttribute('aria-disabled', 'true')
    expect(all[4]).not.toHaveAttribute('aria-disabled')
    await userEvent.setup().click(all[0])
    expect(all[0]).toHaveAttribute('aria-checked', 'false')
  })

  it('generates an offline invoice and shows the bank details to pay into', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary())
    payByInvoice.mockResolvedValue({
      invoiceId: 9, invoiceNumber: 'INV-2026-000009', total: 1999, currency: 'USD', dueAt: '2026-10-01T00:00:00Z',
      billingEmail: 'jane@example.com', subscriptionStatus: 'ACTIVE',
      bankDetails: { accountName: 'eVyoog Pvt Ltd', bankName: 'Demo Bank', accountNumber: '0011223344', ifsc: 'DEMO0001234', swiftBic: null },
    })
    renderAt('/checkout?invoiceId=9&step=payment')
    await screen.findByRole('radiogroup', { name: 'Payment' })
    const user = userEvent.setup()
    await user.click(tiles()[4])
    await user.click(screen.getByRole('checkbox', { name: /I agree/ }))
    await user.click(screen.getByRole('button', { name: 'Generate invoice' }))

    expect(await screen.findByRole('heading', { name: 'Invoice generated' })).toBeInTheDocument()
    expect(payByInvoice).toHaveBeenCalledWith(9)
    expect(createPayment).not.toHaveBeenCalled()
    expect(screen.getByText('0011223344')).toBeInTheDocument()
    expect(screen.getByText(/Quote invoice number INV-2026-000009/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Copy Account number' })).toBeInTheDocument()
  })

  it('shows the order-submitted result for an organization member', async () => {
    const { container } = renderAt('/checkout?orders=15,16&step=complete')
    expect(await screen.findByRole('heading', { name: 'Order submitted for approval' })).toBeInTheDocument()
    expect(screen.getByText('Order #15, #16')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'View my orders' })).toHaveAttribute('href', '/organization/orders')
    expect(checkout).not.toHaveBeenCalled()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('has no detectable accessibility violations on the payment step with saved cards', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary())
    paymentMethods.mockResolvedValue([visa, expiredMastercard])
    const { container } = renderAt('/checkout?invoiceId=9&from=cart&step=payment')
    await screen.findByRole('radiogroup', { name: 'Payment' })
    await userEvent.setup().click(tiles()[0])
    expect(await axe(container)).toHaveNoViolations()
  })

  it('has no detectable accessibility violations on the billing-details step', async () => {
    const { container } = renderAt('/checkout?productId=7')
    await screen.findByRole('heading', { name: 'Billing details' })
    expect(await axe(container)).toHaveNoViolations()
  })

  it('links the consent sentence to the Terms, Refund and Privacy pages set in Billing settings (C97 BI-7)', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary({
      termsUrl: 'https://www.evyoog.com/terms', privacyUrl: 'https://www.evyoog.com/privacy', refundPolicyUrl: 'https://www.evyoog.com/refunds',
    }))
    renderAt('/checkout?invoiceId=9&step=payment')
    await screen.findByRole('radiogroup', { name: 'Payment' })

    expect(screen.getByRole('link', { name: 'Terms & Conditions' })).toHaveAttribute('href', 'https://www.evyoog.com/terms')
    expect(screen.getByRole('link', { name: 'Refund policy' })).toHaveAttribute('href', 'https://www.evyoog.com/refunds')
    expect(screen.getByRole('link', { name: 'Privacy policy' })).toHaveAttribute('href', 'https://www.evyoog.com/privacy')
  })

  it('shows the consent sentence without links when no page is set', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary())
    renderAt('/checkout?invoiceId=9&step=payment')
    await screen.findByRole('radiogroup', { name: 'Payment' })

    expect(screen.queryByRole('link', { name: 'Terms & Conditions' })).not.toBeInTheDocument()
    expect(screen.getByRole('checkbox', { name: /I agree to the Terms & Conditions, Refund policy and Privacy policy/ })).toBeInTheDocument()
  })

  it('saves an optional purchase-order number when the field loses focus, and refuses a bad one (C97 BI-9)', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary())
    setPurchaseOrder.mockResolvedValue({})
    renderAt('/checkout?invoiceId=9&step=payment')
    await screen.findByRole('radiogroup', { name: 'Payment' })
    const user = userEvent.setup()
    const field = screen.getByRole('textbox', { name: /Purchase order number/ })

    await user.type(field, 'PO-2026/17')
    await user.tab()
    await waitFor(() => expect(setPurchaseOrder).toHaveBeenCalledWith(9, 'PO-2026/17'))

    await user.clear(field)
    await user.type(field, '#bad!')
    await user.tab()
    expect(screen.getByText(/Use up to 50 letters, digits/)).toBeInTheDocument()
    expect(setPurchaseOrder).toHaveBeenCalledTimes(1)
  })

  it('names the active gateway in the footer when it is not Razorpay (C97 BI-8)', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary({ gatewayCode: 'STRIPE', gatewayName: 'Stripe' }))
    renderAt('/checkout?invoiceId=9&step=payment')
    await screen.findByRole('radiogroup', { name: 'Payment' })

    expect(screen.getByText('Stripe')).toBeInTheDocument()
  })
})
