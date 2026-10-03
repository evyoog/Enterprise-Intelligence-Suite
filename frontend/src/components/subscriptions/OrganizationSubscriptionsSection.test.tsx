import '../../i18n'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { OrganizationSubscriptionsSection } from './OrganizationSubscriptionsSection'

const list = vi.fn()
const seats = vi.fn()
const changeSeats = vi.fn()

vi.mock('../../api/registrationApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/registrationApi')>('../../api/registrationApi')
  return {
    ...actual,
    organizationApi: {
      listOrganizationSubscriptions: () => list(),
      seats: (id: number) => seats(id),
      changeSeats: (id: number, q: number) => changeSeats(id, q),
    },
  }
})

const sub = {
  id: 42, productId: 7, productName: 'Valam.ai', status: 'ACTIVE' as const, planName: 'Standard',
  expiresAt: '2026-11-02T10:00:00Z', quantity: 15, autoRenew: true,
}
const summary = { subscriptionId: 42, productName: 'Valam.ai', quantity: 15, inUse: 12, minimum: 12, organizationSeatLimit: 15, maximum: 100000 }

describe('OrganizationSubscriptionsSection', () => {
  beforeEach(() => {
    for (const m of [list, seats, changeSeats]) m.mockReset()
    list.mockResolvedValue([sub])
    seats.mockResolvedValue(summary)
  })

  it('shows seats in use and changes the quantity after confirmation', async () => {
    const user = userEvent.setup()
    changeSeats.mockResolvedValue({ ...summary, quantity: 16 })
    renderWithProviders(<OrganizationSubscriptionsSection />)
    expect(await screen.findByText('Seats: 12 in use of 15')).toBeInTheDocument()
    expect(screen.getByText('Auto-renew on')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Add a seat' }))
    await user.click(screen.getByRole('button', { name: 'Save' }))
    expect(screen.getByRole('dialog', { name: 'Change seats from 15 to 16?' })).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Change seats' }))
    expect(changeSeats).toHaveBeenCalledWith(42, 16)
    expect(await screen.findByText('Seats updated.')).toBeInTheDocument()
  })

  it('does not go below the seats in use', async () => {
    renderWithProviders(<OrganizationSubscriptionsSection />)
    await screen.findByText('Seats: 12 in use of 15')
    const input = screen.getByLabelText('Seats')
    const user = userEvent.setup()
    await user.clear(input)
    await user.type(input, '11')
    expect(screen.getByRole('button', { name: 'Save' })).toBeDisabled()
  })

  it('stays hidden for members without organization management', async () => {
    list.mockRejectedValue(new ApiError(403, 'Forbidden'))
    const { container } = renderWithProviders(<OrganizationSubscriptionsSection />)
    await waitFor(() => expect(list).toHaveBeenCalled())
    expect(container).toBeEmptyDOMElement()
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderWithProviders(<OrganizationSubscriptionsSection />)
    await screen.findByText('Seats: 12 in use of 15')
    expect(await axe(container)).toHaveNoViolations()
  })
})
