import '../i18n'
import { render, screen, waitFor } from '@testing-library/react'
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
      createPayment: (id: number) => createPayment(id),
      confirmPayment: (id: number, body: unknown) => confirmPayment(id, body),
      invoiceDetail: (id: number) => invoiceDetail(id),
    },
  }
})

vi.mock('../utils/razorpayCheckout', () => ({
  openRazorpayCheckout: vi.fn().mockResolvedValue({ providerOrderId: 'order_1', providerPaymentId: 'pay_1', signature: 'sig_1' }),
}))

const product = {
  id: 7, name: 'Valam.ai', description: 'Analytics.', price: 19.99, status: 'ACTIVE' as const,
  ssoConnected: false, featured: false, platforms: [], plans: [], version: 1, dependsOnProductIds: [],
}

const savedDetails = {
  id: 1, billingName: 'Jane', billingEmail: 'jane@example.com', addressLine1: '1 Main St',
  city: 'Chennai', state: 'TN', postalCode: '600001', country: 'India', updatedAt: '2026-10-01T00:00:00Z',
}

function summary(overrides: Record<string, unknown> = {}) {
  return {
    invoiceId: 9, invoiceNumber: 'INV-2026-000009', invoiceStatus: 'OPEN', paymentRoute: null,
    subscriptionId: 1, subscriptionStatus: 'ACTIVE', currency: 'USD',
    items: [{ productId: 7, productName: 'Valam.ai', planName: 'Pro', billingPeriod: 'MONTHLY', amount: 1999, quantity: 1 }],
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
        <Route path="/my/products" element={<div>My products page</div>} />
        <Route path="/login" element={<div>Sign in page</div>} />
      </Routes>
    </MemoryRouter>
  )
}

async function fillDetails(user: ReturnType<typeof userEvent.setup>) {
  await user.type(await screen.findByLabelText(/Full name or company/), 'Jane')
  await user.type(screen.getByLabelText(/Billing email/), 'jane@example.com')
  await user.type(screen.getByLabelText(/Address line 1/), '1 Main St')
  await user.type(screen.getByLabelText(/^City/), 'Chennai')
  await user.type(screen.getByLabelText(/State \/ region/), 'TN')
  await user.type(screen.getByLabelText(/Postal code/), '600001')
  await user.type(screen.getByLabelText(/^Country/), 'India')
}

describe('CheckoutPage (C55)', () => {
  beforeEach(() => {
    for (const m of [getProduct, subscribe, getDetails, saveDetails, checkout, payByInvoice, createPayment, confirmPayment, invoiceDetail]) m.mockReset()
    vi.mocked(openRazorpayCheckout).mockClear()
    getProduct.mockResolvedValue(product)
    getDetails.mockRejectedValue(new ApiError(404, 'No billing details saved yet'))
  })

  it('forwards the legacy /checkout/:productId link and shows the billing-details form first', async () => {
    renderAt('/checkout/7')
    expect(await screen.findByRole('heading', { name: 'Billing details' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Continue to payment' })).toBeInTheDocument()
    expect(getProduct).toHaveBeenCalledWith(7)
    // Stepper marks the current step for assistive tech.
    expect(screen.getByText('Billing details', { selector: 'p' }).closest('li')).toHaveAttribute('aria-current', 'step')
  })

  it('sends the visitor to sign in when the session has expired', async () => {
    getDetails.mockRejectedValue(new ApiError(401, 'Not authenticated'))
    renderAt('/checkout?productId=7')
    expect(await screen.findByText('Sign in page')).toBeInTheDocument()
  })

  it('validates required billing fields before continuing', async () => {
    renderAt('/checkout?productId=7')
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Continue to payment' }))
    expect((await screen.findAllByText('This field is required.')).length).toBeGreaterThan(0)
    expect(saveDetails).not.toHaveBeenCalled()
  })

  it('goes straight to a free-plan confirmation when the subscription has no invoice', async () => {
    saveDetails.mockResolvedValue({})
    subscribe.mockResolvedValue({ id: 1, productId: 7, productName: 'Valam.ai', status: 'ACTIVE' })
    checkout.mockResolvedValue(summary({ invoiceId: null, invoiceNumber: null, invoiceStatus: null, total: 0, subtotal: 0 }))
    renderAt('/checkout?productId=7')
    const user = userEvent.setup()
    await fillDetails(user)
    await user.click(screen.getByRole('button', { name: 'Continue to payment' }))
    expect(await screen.findByText("You're subscribed")).toBeInTheDocument()
    expect(subscribe).toHaveBeenCalledWith(7)
  })

  it('shows four payment options and keeps Pay disabled until an option and consent are chosen', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary())
    renderAt('/checkout?invoiceId=9&step=payment')

    const radios = await screen.findAllByRole('radio')
    expect(radios.map((r) => r.textContent)).toEqual([
      expect.stringContaining('Card'), expect.stringContaining('UPI'),
      expect.stringContaining('Other online methods'), expect.stringContaining('Pay by invoice'),
    ])
    const user = userEvent.setup()
    const pay = () => screen.getByRole('button', { name: /Pay \$19\.99|Generate invoice/ })
    expect(pay()).toBeDisabled()
    await user.click(radios[0])
    expect(radios[0]).toHaveAttribute('aria-checked', 'true')
    expect(pay()).toBeDisabled()
    await user.click(screen.getByRole('checkbox', { name: /I agree/ }))
    expect(pay()).toBeEnabled()
  })

  it('never renders a card input: the card panel holds no text fields (BR-BIL-001)', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary())
    const { container } = renderAt('/checkout?invoiceId=9&step=payment')
    await userEvent.setup().click((await screen.findAllByRole('radio'))[0])
    expect(screen.getByText(/never stored by eVyoog/)).toBeInTheDocument()
    expect(container.querySelectorAll('input:not([type="checkbox"])')).toHaveLength(0)
    expect(screen.queryByRole('textbox')).not.toBeInTheDocument()
  })

  it('pays by card through Razorpay with the card method prefilled and shows success', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary())
    createPayment.mockResolvedValue({ paymentId: 3, providerOrderId: 'order_1', amount: 1999, currency: 'USD', keyId: 'rzp_test_1' })
    confirmPayment.mockResolvedValue({ status: 'CAPTURED', amount: 1999, currency: 'USD', provider: 'RAZORPAY' })
    renderAt('/checkout?invoiceId=9&step=payment')
    const user = userEvent.setup()
    await user.click((await screen.findAllByRole('radio'))[0])
    await user.click(screen.getByRole('checkbox', { name: /I agree/ }))
    await user.click(screen.getByRole('button', { name: /Pay \$19\.99/ }))

    await waitFor(() => expect(confirmPayment).toHaveBeenCalledWith(3, { providerOrderId: 'order_1', providerPaymentId: 'pay_1', signature: 'sig_1' }))
    expect(vi.mocked(openRazorpayCheckout).mock.calls[0][0]).toMatchObject({ method: 'card' })
    expect(await screen.findByRole('heading', { name: 'Payment successful' })).toBeInTheDocument()
  })

  it('shows a failed result with retry options when the customer closes Razorpay', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary())
    createPayment.mockResolvedValue({ paymentId: 3, providerOrderId: 'order_1', amount: 1999, currency: 'USD', keyId: 'rzp_test_1' })
    vi.mocked(openRazorpayCheckout).mockRejectedValueOnce(new Error('cancelled'))
    renderAt('/checkout?invoiceId=9&step=payment')
    const user = userEvent.setup()
    await user.click((await screen.findAllByRole('radio'))[1])
    await user.click(screen.getByRole('checkbox', { name: /I agree/ }))
    await user.click(screen.getByRole('button', { name: /Pay \$19\.99/ }))
    expect(await screen.findByRole('heading', { name: 'Payment failed' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Try again' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Choose another method' })).toBeInTheDocument()
    expect(confirmPayment).not.toHaveBeenCalled()
  })

  it('disables online options when the gateway is not configured but keeps Pay by invoice', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary({ gatewayConfigured: false }))
    renderAt('/checkout?invoiceId=9&step=payment')
    const radios = await screen.findAllByRole('radio')
    expect(screen.getByText(/Online payments are not available/i)).toBeInTheDocument()
    expect(radios[0]).toHaveAttribute('aria-disabled', 'true')
    expect(radios[1]).toHaveAttribute('aria-disabled', 'true')
    expect(radios[2]).toHaveAttribute('aria-disabled', 'true')
    expect(radios[3]).not.toHaveAttribute('aria-disabled')
    await userEvent.setup().click(radios[0])
    expect(radios[0]).toHaveAttribute('aria-checked', 'false')
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
    const user = userEvent.setup()
    await user.click((await screen.findAllByRole('radio'))[3])
    await user.click(screen.getByRole('checkbox', { name: /I agree/ }))
    await user.click(screen.getByRole('button', { name: 'Generate invoice' }))

    expect(await screen.findByRole('heading', { name: 'Invoice generated' })).toBeInTheDocument()
    expect(payByInvoice).toHaveBeenCalledWith(9)
    expect(createPayment).not.toHaveBeenCalled()
    expect(screen.getByText('0011223344')).toBeInTheDocument()
    expect(screen.getByText(/Quote invoice number INV-2026-000009/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Copy Account number' })).toBeInTheDocument()
  })

  it('shows tax lines, or "none for this region" when there are none', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary())
    renderAt('/checkout?invoiceId=9&step=payment')
    expect(await screen.findByText('Tax: none for this region')).toBeInTheDocument()
    expect(screen.getByRole('complementary', { name: 'Order summary' })).toHaveTextContent('Valam.ai')
  })

  it('has no detectable accessibility violations on the payment step', async () => {
    getDetails.mockResolvedValue(savedDetails)
    checkout.mockResolvedValue(summary())
    const { container } = renderAt('/checkout?invoiceId=9&step=payment')
    await userEvent.setup().click((await screen.findAllByRole('radio'))[0])
    expect(await axe(container)).toHaveNoViolations()
  })

  it('has no detectable accessibility violations on the billing-details step', async () => {
    const { container } = renderAt('/checkout?productId=7')
    await screen.findByRole('heading', { name: 'Billing details' })
    expect(await axe(container)).toHaveNoViolations()
  })
})
