import '../../i18n'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { OrganizationSubscriptionsSection } from './OrganizationSubscriptionsSection'

const list = vi.fn()
const seats = vi.fn()
const changeSeats = vi.fn()
const setAutoRenew = vi.fn()
const permissions = vi.fn()
const recipientsGet = vi.fn()

vi.mock('../../api/registrationApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/registrationApi')>('../../api/registrationApi')
  return {
    ...actual,
    organizationApi: {
      listOrganizationSubscriptions: () => list(),
      seats: (id: number) => seats(id),
      changeSeats: (id: number, q: number) => changeSeats(id, q),
      setAutoRenew: (id: number, v: boolean) => setAutoRenew(id, v),
    },
  }
})
vi.mock('../../api/myPermissionsApi', () => ({ myPermissionsApi: { get: () => permissions() } }))
vi.mock('../../api/renewalsApi', () => ({
  reminderRecipientsApi: { get: () => recipientsGet(), save: vi.fn(), candidates: () => Promise.resolve([]) },
}))

const sub = {
  id: 42, productId: 7, productName: 'Valam.ai', status: 'ACTIVE' as const, planName: 'Standard',
  expiresAt: '2026-11-02T10:00:00Z', quantity: 15, autoRenew: true,
}
// One seat for one 30-day term costs 300.00 (30000 minor units); 15 of 30 days remain.
const summary = {
  subscriptionId: 42, productName: 'Valam.ai', quantity: 15, inUse: 12, minimum: 12, organizationSeatLimit: 15, maximum: 100000,
  pendingQuantity: null, unitAmount: 30000, currency: 'INR', remainingDays: 15, termDays: 30, renewsAt: '2026-11-02T10:00:00Z',
}
const BILLING = { platform: [], organization: ['MANAGE_ORGANIZATION_BILLING', 'MANAGE_SUBSCRIPTIONS'] }

describe('OrganizationSubscriptionsSection', () => {
  beforeEach(() => {
    for (const m of [list, seats, changeSeats, setAutoRenew, permissions, recipientsGet]) m.mockReset()
    list.mockResolvedValue([sub])
    seats.mockResolvedValue(summary)
    permissions.mockResolvedValue(BILLING)
    recipientsGet.mockResolvedValue({ defaults: [], added: [] })
  })

  it('shows seats in use and an estimate for an increase, then the result with an invoice link', async () => {
    const user = userEvent.setup()
    changeSeats.mockResolvedValue({ ...summary, quantity: 17, effect: 'NOW', invoiceId: 9 })
    renderWithProviders(<OrganizationSubscriptionsSection />)
    expect(await screen.findByText('Seats: 12 in use of 15')).toBeInTheDocument()
    expect(screen.getByText('Auto-renew on')).toBeInTheDocument()
    expect(await screen.findByRole('button', { name: 'Add a seat' })).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Add a seat' }))
    await user.click(screen.getByRole('button', { name: 'Add a seat' }))
    // 2 added seats x 300.00 x 15/30 = 300.00
    expect(screen.getByText(/Charged now for 15 of 30 days: .*300\.00/)).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Save' }))
    const dialog = screen.getByRole('dialog', { name: 'Change seats from 15 to 17?' })
    expect(within(dialog).getByText(/Charged now for 15 of 30 days/)).toBeInTheDocument()
    await user.click(within(dialog).getByRole('button', { name: 'Change seats' }))
    expect(changeSeats).toHaveBeenCalledWith(42, 17)
    expect(await screen.findByText('Seats increased to 17. An invoice was created for the added seats.')).toBeInTheDocument()
    expect(await screen.findByRole('link', { name: 'View invoices' })).toHaveAttribute('href', '/organization/billing?tab=invoices')
  })

  it('schedules a decrease for the next renewal with no estimate charge', async () => {
    const user = userEvent.setup()
    changeSeats.mockResolvedValue({ ...summary, effect: 'AT_RENEWAL', pendingQuantity: 13 })
    renderWithProviders(<OrganizationSubscriptionsSection />)
    await screen.findByText('Seats: 12 in use of 15')
    await user.click(await screen.findByRole('button', { name: 'Remove a seat' }))
    await user.click(screen.getByRole('button', { name: 'Remove a seat' }))
    expect(screen.queryByText(/Charged now/)).not.toBeInTheDocument()
    expect(screen.getByText(/The decrease takes effect on .* Nothing is credited\./)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Save' }))
    const dialog = screen.getByRole('dialog', { name: 'Change seats from 15 to 13?' })
    expect(within(dialog).getByText(/You keep all 15 seats until your next renewal/)).toBeInTheDocument()
    await user.click(within(dialog).getByRole('button', { name: 'Change seats' }))
    expect(changeSeats).toHaveBeenCalledWith(42, 13)
    expect(await screen.findByText(/Seats will fall to 13 on .* Nothing is credited\./)).toBeInTheDocument()
    expect(screen.getByText(/Falls to 13 seats on /)).toBeInTheDocument()
  })

  it('shows a pending decrease and withdraws it by asking for the current quantity', async () => {
    const user = userEvent.setup()
    seats.mockResolvedValue({ ...summary, pendingQuantity: 13 })
    changeSeats.mockResolvedValue({ ...summary, effect: 'WITHDRAWN', pendingQuantity: null })
    renderWithProviders(<OrganizationSubscriptionsSection />)
    expect(await screen.findByText(/Falls to 13 seats on .*Nothing is credited\./)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Withdraw' }))
    expect(changeSeats).toHaveBeenCalledWith(42, 15)
    expect(await screen.findByText('The scheduled decrease was withdrawn. You keep 15 seats.')).toBeInTheDocument()
    expect(screen.queryByText(/Falls to 13 seats/)).not.toBeInTheDocument()
  })

  it('shows the server message when a change is refused', async () => {
    const user = userEvent.setup()
    changeSeats.mockRejectedValue(new ApiError(409, 'This term has ended. Renew the subscription before changing seats.'))
    renderWithProviders(<OrganizationSubscriptionsSection />)
    await screen.findByText('Seats: 12 in use of 15')
    await user.click(await screen.findByRole('button', { name: 'Add a seat' }))
    await user.click(screen.getByRole('button', { name: 'Save' }))
    await user.click(screen.getByRole('button', { name: 'Change seats' }))
    expect(await screen.findByText('This term has ended. Renew the subscription before changing seats.')).toBeInTheDocument()
  })

  it('does not go below the seats in use', async () => {
    renderWithProviders(<OrganizationSubscriptionsSection />)
    await screen.findByText('Seats: 12 in use of 15')
    const input = await screen.findByLabelText('Seats')
    const user = userEvent.setup()
    await user.clear(input)
    await user.type(input, '11')
    expect(screen.getByRole('button', { name: 'Save' })).toBeDisabled()
  })

  it('hides the seat controls and the recipients from people without Manage billing', async () => {
    permissions.mockResolvedValue({ platform: [], organization: ['MANAGE_SUBSCRIPTIONS'] })
    renderWithProviders(<OrganizationSubscriptionsSection />)
    expect(await screen.findByText('Seats: 12 in use of 15')).toBeInTheDocument()
    expect(await screen.findByText('Only people with the Manage billing permission can change seats.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Add a seat' })).not.toBeInTheDocument()
    expect(screen.queryByLabelText('Seats')).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Save' })).not.toBeInTheDocument()
    expect(recipientsGet).not.toHaveBeenCalled()
    // Viewing, and auto-renew, stay available to Manage subscriptions.
    expect(screen.getByRole('switch', { name: 'Auto-renew' })).toBeInTheDocument()
  })

  it('stays hidden for members without organization management', async () => {
    list.mockRejectedValue(new ApiError(403, 'Forbidden'))
    const { container } = renderWithProviders(<OrganizationSubscriptionsSection />)
    await waitFor(() => expect(list).toHaveBeenCalled())
    expect(container).toBeEmptyDOMElement()
  })

  it('confirms before turning auto-renew off, explaining the 7-day read-only grace period', async () => {
    const user = userEvent.setup()
    const graceEndsAt = new Date(Date.now() + 5 * 86_400_000).toISOString()
    setAutoRenew.mockResolvedValue({ ...sub, autoRenew: false, graceEndsAt })
    renderWithProviders(<OrganizationSubscriptionsSection />)
    await user.click(await screen.findByRole('switch', { name: 'Auto-renew' }))
    const dialog = screen.getByRole('dialog', { name: 'Turn off auto-renew for Valam.ai?' })
    expect(within(dialog).getByText(/read-only for 7 days/)).toBeInTheDocument()
    expect(setAutoRenew).not.toHaveBeenCalled()
    await user.click(within(dialog).getByRole('button', { name: 'Turn off auto-renew' }))
    expect(setAutoRenew).toHaveBeenCalledWith(42, false)
    expect(await screen.findByText(/^Read-only until /)).toBeInTheDocument()
    expect(screen.getByText(/The term of Valam\.ai has ended\. It is read-only until/)).toBeInTheDocument()
    // Seats cannot change while the subscription is read-only.
    expect(screen.queryByRole('button', { name: 'Add a seat' })).not.toBeInTheDocument()
    expect(screen.getByText(/Seats cannot be changed while the subscription is read-only/)).toBeInTheDocument()
    expect(await screen.findByRole('switch', { name: 'Auto-renew' })).not.toBeChecked()
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderWithProviders(<OrganizationSubscriptionsSection />)
    await screen.findByText('Seats: 12 in use of 15')
    await screen.findByRole('button', { name: 'Add a seat' })
    expect(await axe(container)).toHaveNoViolations()
  })

  // C102 (REQ-MKT-005): a trial has no auto-renew, renewal date or seat change; Subscribe goes to the product page.
  it('shows a trial with its end and Subscribe, and no auto-renew, renewal or seat controls', async () => {
    const trial = { ...sub, trial: true, autoRenew: false, expiresAt: new Date(Date.now() + 9.5 * 86_400_000).toISOString() }
    list.mockResolvedValue([trial])
    renderWithProviders(<OrganizationSubscriptionsSection />)
    expect(await screen.findByText('Free trial')).toBeInTheDocument()
    expect(screen.getByText(/^Ends .* \(10 days left\)$/)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Subscribe to Valam.ai' })).toHaveAttribute('href', '/products/7')
    expect(screen.queryByRole('switch')).not.toBeInTheDocument()
    expect(screen.queryByText(/Renews on/)).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Add a seat' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Save' })).not.toBeInTheDocument()
  })
})
