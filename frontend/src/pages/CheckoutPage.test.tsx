import '../i18n'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/client'
import { CheckoutPage } from './CheckoutPage'

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
const invoices = vi.fn()
const overview = vi.fn()
const createPayment = vi.fn()
const confirmPayment = vi.fn()
vi.mock('../api/billingApi', async () => {
  const actual = await vi.importActual<typeof import('../api/billingApi')>('../api/billingApi')
  return {
    ...actual,
    myBillingApi: {
      ...actual.myBillingApi,
      getDetails: () => getDetails(),
      saveDetails: (input: unknown) => saveDetails(input),
      invoices: () => invoices(),
      overview: () => overview(),
      createPayment: (id: number) => createPayment(id),
      confirmPayment: (id: number, body: unknown) => confirmPayment(id, body),
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

function renderCheckout() {
  return render(
    <MemoryRouter initialEntries={['/checkout/7']}>
      <Routes>
        <Route path="/checkout/:productId" element={<CheckoutPage />} />
        <Route path="/my/products" element={<div>My products page</div>} />
        <Route path="/login" element={<div>Sign in page</div>} />
      </Routes>
    </MemoryRouter>
  )
}

describe('CheckoutPage', () => {
  beforeEach(() => {
    for (const m of [getProduct, subscribe, getDetails, saveDetails, invoices, overview, createPayment, confirmPayment]) m.mockReset()
    getProduct.mockResolvedValue(product)
    // 404 is how the backend signals "nothing saved yet" (never a null 200
    // body — see BillingDetailsService's own doc) — every test that doesn't
    // override this exercises the first-time, blank-form path.
    getDetails.mockRejectedValue(new ApiError(404, 'No billing details saved yet'))
  })

  it('shows the billing-details form first when none are on file', async () => {
    renderCheckout()
    expect(await screen.findByText('Confirm your billing details')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Save & continue' })).toBeInTheDocument()
  })

  it('sends the visitor to sign in (with a way back here) when the session has expired, instead of a confusing "could not load" message', async () => {
    getDetails.mockRejectedValue(new ApiError(401, 'Not authenticated'))
    renderCheckout()
    expect(await screen.findByText('Sign in page')).toBeInTheDocument()
    expect(screen.queryByText('Could not load this product.')).not.toBeInTheDocument()
  })

  it('sends the visitor to sign in if the session expires while starting the subscription', async () => {
    saveDetails.mockRejectedValue(new ApiError(401, 'Not authenticated'))
    renderCheckout()

    const user = userEvent.setup()
    await user.type(await screen.findByLabelText(/Billing name/), 'Jane')
    await user.type(screen.getByLabelText(/Billing email/), 'jane@example.com')
    await user.type(screen.getByLabelText(/Address line 1/), '1 Main St')
    await user.type(screen.getByLabelText(/^City/), 'Chennai')
    await user.type(screen.getByLabelText(/State \/ region/), 'TN')
    await user.type(screen.getByLabelText(/Postal code/), '600001')
    await user.type(screen.getByLabelText(/^Country/), 'India')
    await user.click(screen.getByRole('button', { name: 'Save & continue' }))

    expect(await screen.findByText('Sign in page')).toBeInTheDocument()
  })

  it('goes straight to a free-plan confirmation when the plan has no price', async () => {
    saveDetails.mockResolvedValue({})
    subscribe.mockResolvedValue({ id: 1, productId: 7, productName: 'Valam.ai', status: 'ACTIVE' })
    invoices.mockResolvedValue({ content: [] })
    renderCheckout()

    const user = userEvent.setup()
    await user.type(await screen.findByLabelText(/Billing name/), 'Jane')
    await user.type(screen.getByLabelText(/Billing email/), 'jane@example.com')
    await user.type(screen.getByLabelText(/Address line 1/), '1 Main St')
    await user.type(screen.getByLabelText(/^City/), 'Chennai')
    await user.type(screen.getByLabelText(/State \/ region/), 'TN')
    await user.type(screen.getByLabelText(/Postal code/), '600001')
    await user.type(screen.getByLabelText(/^Country/), 'India')
    await user.click(screen.getByRole('button', { name: 'Save & continue' }))

    expect(await screen.findByText("You're subscribed.")).toBeInTheDocument()
    expect(subscribe).toHaveBeenCalledWith(7)
  })

  it('collects payment for a paid plan through Razorpay Checkout', async () => {
    getDetails.mockResolvedValue({
      id: 1, billingName: 'Jane', billingEmail: 'jane@example.com', addressLine1: '1 Main St',
      city: 'Chennai', state: 'TN', postalCode: '600001', country: 'India', updatedAt: '2026-10-01T00:00:00Z',
    })
    subscribe.mockResolvedValue({ id: 1, productId: 7, productName: 'Valam.ai', status: 'ACTIVE' })
    invoices.mockResolvedValue({
      content: [{ id: 9, invoiceNumber: 'INV-2026-000009', status: 'OPEN', currency: 'USD', total: 1999, subtotal: 1999, taxAmount: 0, issuedAt: '2026-10-01T00:00:00Z' }],
    })
    overview.mockResolvedValue({ gatewayConfigured: true })
    createPayment.mockResolvedValue({ paymentId: 3, providerOrderId: 'order_1', amount: 1999, currency: 'USD', keyId: 'rzp_test_1' })
    confirmPayment.mockResolvedValue({ status: 'CAPTURED' })
    renderCheckout()

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Continue to payment' }))

    expect(await screen.findByText('INV-2026-000009')).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: /Pay 19\.99 USD/ }))

    await waitFor(() => expect(confirmPayment).toHaveBeenCalledWith(3, { providerOrderId: 'order_1', providerPaymentId: 'pay_1', signature: 'sig_1' }))
    expect(await screen.findByText('Payment received — you\'re all set.')).toBeInTheDocument()
  })
})
