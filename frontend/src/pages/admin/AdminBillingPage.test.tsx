import '../../i18n'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminBillingPage } from './AdminBillingPage'

const invoices = vi.fn()
const payments = vi.fn()
const refund = vi.fn()
const reconcile = vi.fn()

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
    },
  }
})

describe('AdminBillingPage', () => {
  beforeEach(() => {
    for (const m of [invoices, payments, refund, reconcile]) m.mockReset()
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
})
