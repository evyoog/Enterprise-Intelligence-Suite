import '../i18n'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../test/renderWithProviders'
import { BillingPage } from './BillingPage'

const overview = vi.fn()
const getDetails = vi.fn()
const invoices = vi.fn()
const paymentMethods = vi.fn()
const payments = vi.fn()
const saveDetails = vi.fn()

vi.mock('../api/billingApi', async () => {
  const actual = await vi.importActual<typeof import('../api/billingApi')>('../api/billingApi')
  const scoped = {
    overview: () => overview(),
    getDetails: () => getDetails(),
    saveDetails: (input: unknown) => saveDetails(input),
    invoices: () => invoices(),
    paymentMethods: () => paymentMethods(),
    payments: () => payments(),
    downloadDocument: vi.fn(),
    createPayment: vi.fn(),
    confirmPayment: vi.fn(),
    setupPaymentMethod: vi.fn(),
    confirmPaymentMethodSetup: vi.fn(),
    setDefaultPaymentMethod: vi.fn(),
    removePaymentMethod: vi.fn(),
  }
  return { ...actual, myBillingApi: scoped, organizationBillingApi: scoped }
})

const emptyOverview = {
  amountDueByCurrency: {}, spentThisPeriodByCurrency: {}, spentLastPeriodByCurrency: {},
  recentInvoices: [], gatewayConfigured: false,
}

describe('BillingPage', () => {
  beforeEach(() => {
    for (const m of [overview, getDetails, invoices, paymentMethods, payments, saveDetails]) m.mockReset()
    getDetails.mockResolvedValue(null)
    invoices.mockResolvedValue({ content: [] })
    paymentMethods.mockResolvedValue([])
    payments.mockResolvedValue({ content: [] })
  })

  it('shows the gateway-not-configured banner when Razorpay has no credentials', async () => {
    overview.mockResolvedValue(emptyOverview)
    renderWithProviders(<BillingPage />)
    expect(await screen.findByText(/Online payments are not available yet/)).toBeInTheDocument()
  })

  it('shows the amount due and recent invoices on the overview tab', async () => {
    overview.mockResolvedValue({
      ...emptyOverview, gatewayConfigured: true,
      amountDueByCurrency: { USD: 1999 },
      recentInvoices: [{ id: 1, invoiceNumber: 'INV-2026-000001', status: 'OPEN', currency: 'USD', total: 1999, subtotal: 1999, taxAmount: 0, issuedAt: '2026-10-01T00:00:00Z' }],
    })
    renderWithProviders(<BillingPage />)
    expect((await screen.findAllByText('19.99 USD')).length).toBeGreaterThan(0)
    expect(await screen.findByText('INV-2026-000001')).toBeInTheDocument()
  })

  it('lists invoices on the Invoices tab', async () => {
    overview.mockResolvedValue(emptyOverview)
    invoices.mockResolvedValue({
      content: [{ id: 2, invoiceNumber: 'INV-2026-000002', status: 'PAID', currency: 'USD', total: 500, subtotal: 500, taxAmount: 0, issuedAt: '2026-10-01T00:00:00Z' }],
    })
    renderWithProviders(<BillingPage />)
    await userEvent.setup().click(await screen.findByRole('tab', { name: 'Invoices' }))
    expect(await screen.findByText('INV-2026-000002')).toBeInTheDocument()
  })

  it('saves billing details', async () => {
    overview.mockResolvedValue(emptyOverview)
    saveDetails.mockResolvedValue({ id: 1, billingName: 'Jane', billingEmail: 'jane@example.com' })
    renderWithProviders(<BillingPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('tab', { name: 'Billing details' }))
    await user.type(await screen.findByLabelText(/Billing name/), 'Jane')
    await user.type(screen.getByLabelText(/Billing email/), 'jane@example.com')
    await user.type(screen.getByLabelText(/Address line 1/), '1 Main St')
    await user.type(screen.getByLabelText(/^City/), 'Chennai')
    await user.type(screen.getByLabelText(/State \/ region/), 'TN')
    await user.type(screen.getByLabelText(/Postal code/), '600001')
    await user.type(screen.getByLabelText(/^Country/), 'India')
    await user.click(screen.getByRole('button', { name: 'Save' }))
    await waitFor(() => expect(saveDetails).toHaveBeenCalled())
  })

  it('has no detectable a11y violations', async () => {
    overview.mockResolvedValue(emptyOverview)
    const { container } = renderWithProviders(<BillingPage />)
    await screen.findByText('Billing')
    expect(await axe(container)).toHaveNoViolations()
  })
})
