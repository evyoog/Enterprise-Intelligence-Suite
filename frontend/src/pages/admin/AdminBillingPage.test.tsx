import '../../i18n'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminBillingPage } from './AdminBillingPage'

const invoices = vi.fn()
const payments = vi.fn()
const refund = vi.fn()
const reconcile = vi.fn()
const recordOfflinePayment = vi.fn()

vi.mock('../../api/billingApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/billingApi')>('../../api/billingApi')
  return {
    ...actual,
    adminBillingApi: {
      ...actual.adminBillingApi,
      invoices: () => invoices(),
      payments: () => payments(),
      refund: (id: number, amount: number, reason: string) => refund(id, amount, reason),
      reconcile: (id: number) => reconcile(id),
      recordOfflinePayment: (id: number, body: unknown) => recordOfflinePayment(id, body),
    },
  }
})

describe('AdminBillingPage', () => {
  beforeEach(() => {
    for (const m of [invoices, payments, refund, reconcile, recordOfflinePayment]) m.mockReset()
    invoices.mockResolvedValue({ content: [] })
    payments.mockResolvedValue({ content: [] })
  })

  it('lists every invoice with its owner', async () => {
    invoices.mockResolvedValue({
      content: [{ id: 1, invoiceNumber: 'INV-2026-000001', status: 'OPEN', currency: 'USD', total: 1999, subtotal: 1999, taxAmount: 0, issuedAt: '2026-10-01T00:00:00Z', ownerLabel: 'Jane Customer' }],
    })
    renderWithProviders(<AdminBillingPage />)
    expect(await screen.findByText('INV-2026-000001')).toBeInTheDocument()
    expect(await screen.findByText('Jane Customer')).toBeInTheDocument()
  })

  it('refunds a captured payment with a reason', async () => {
    payments.mockResolvedValue({
      content: [{ id: 5, invoiceId: 1, invoiceNumber: 'INV-2026-000001', status: 'CAPTURED', currency: 'USD', amount: 1999, refundedAmount: 0, createdAt: '2026-10-01T00:00:00Z', ownerLabel: 'Jane Customer' }],
    })
    refund.mockResolvedValue({})
    renderWithProviders(<AdminBillingPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('tab', { name: 'Payment history' }))
    await user.click(await screen.findByRole('button', { name: 'Refund' }))
    const dialog = screen.getByRole('dialog')
    await user.type(within(dialog).getByLabelText(/Amount/), '5')
    await user.type(within(dialog).getByLabelText(/Reason/), 'Customer requested cancellation')
    await user.click(within(dialog).getByRole('button', { name: 'Refund' }))
    await waitFor(() => expect(refund).toHaveBeenCalledWith(5, 500, 'Customer requested cancellation'))
  })

  const offlineInvoice = {
    id: 2, invoiceNumber: 'INV-2026-000002', status: 'OPEN', paymentRoute: 'OFFLINE', currency: 'USD', total: 1999,
    subtotal: 1999, taxAmount: 0, issuedAt: '2026-10-01T00:00:00Z', ownerLabel: 'Acme Corp',
  }

  it('offers Record offline payment only on OPEN invoices the customer chose to pay by invoice (C55)', async () => {
    invoices.mockResolvedValue({
      content: [
        offlineInvoice,
        { ...offlineInvoice, id: 3, invoiceNumber: 'INV-2026-000003', paymentRoute: 'ONLINE' },
        { ...offlineInvoice, id: 4, invoiceNumber: 'INV-2026-000004', status: 'PAID' },
      ],
    })
    renderWithProviders(<AdminBillingPage />)
    await screen.findByText('INV-2026-000004')
    expect(screen.getAllByRole('button', { name: 'Record offline payment' })).toHaveLength(1)
  })

  it('records an offline payment after validation and a confirmation step', async () => {
    invoices.mockResolvedValue({ content: [offlineInvoice] })
    recordOfflinePayment.mockResolvedValue({ ...offlineInvoice, status: 'PAID' })
    renderWithProviders(<AdminBillingPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Record offline payment' }))
    const dialog = screen.getByRole('dialog', { name: 'Record offline payment' })
    expect(await axe(dialog)).toHaveNoViolations()

    // A different amount, and a missing method/reference, are refused before anything is sent.
    const amount = within(dialog).getByLabelText(/Amount received/)
    await user.clear(amount)
    await user.type(amount, '10')
    await user.click(within(dialog).getByRole('button', { name: 'Record payment' }))
    expect(within(dialog).getByText('The amount must equal the invoice total.')).toBeInTheDocument()
    expect(within(dialog).getAllByText('This field is required.')).toHaveLength(2)

    await user.clear(amount)
    await user.type(amount, '19.99')
    await user.click(within(dialog).getByRole('combobox', { name: /Method/ }))
    await user.click(await screen.findByRole('option', { name: 'NEFT/RTGS' }))
    await user.type(within(dialog).getByLabelText(/Reference number/), 'UTR123456')
    await user.click(within(dialog).getByRole('button', { name: 'Record payment' }))
    expect(within(dialog).getByText('Mark invoice INV-2026-000002 as paid?')).toBeInTheDocument()
    expect(recordOfflinePayment).not.toHaveBeenCalled()
    await user.click(within(dialog).getByRole('button', { name: 'Mark as paid' }))

    await waitFor(() => expect(recordOfflinePayment).toHaveBeenCalledWith(2, expect.objectContaining({
      amount: 1999, method: 'NEFT_RTGS', reference: 'UTR123456',
    })))
    expect(await screen.findByText('Offline payment recorded. Invoice INV-2026-000002 is paid.')).toBeInTheDocument()
  })

  it('hides gateway refund and reconcile for offline payments', async () => {
    payments.mockResolvedValue({
      content: [{ id: 6, invoiceId: 2, invoiceNumber: 'INV-2026-000002', status: 'CAPTURED', methodType: 'OFFLINE', currency: 'USD', amount: 1999, refundedAmount: 0, createdAt: '2026-10-01T00:00:00Z', ownerLabel: 'Acme Corp' }],
    })
    renderWithProviders(<AdminBillingPage />)
    await userEvent.setup().click(await screen.findByRole('tab', { name: 'Payment history' }))
    await screen.findAllByText('INV-2026-000002')
    expect(screen.queryByRole('button', { name: 'Refund' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Reconcile' })).not.toBeInTheDocument()
  })
})
