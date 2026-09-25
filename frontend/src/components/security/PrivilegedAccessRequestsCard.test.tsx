import '../../i18n'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { PrivilegedAccessRequestsCard } from './PrivilegedAccessRequestsCard'

const listRequestable = vi.fn()
const listMine = vi.fn()
const request = vi.fn()
const withdraw = vi.fn()

vi.mock('../../api/privilegedAccessApi', () => ({
  myPrivilegedAccessApi: {
    listRequestable: () => listRequestable(),
    listMine: () => listMine(),
    request: (payload: unknown) => request(payload),
    withdraw: (id: number) => withdraw(id),
  },
}))

const requestable = [
  { permissionName: 'MANAGE_CATALOG', scope: 'PLATFORM' },
  { permissionName: 'MANAGE_USERS', scope: 'ORGANIZATION' },
]
const expired = {
  id: 5, scope: 'ORGANIZATION', permissionName: 'MANAGE_USERS', justification: 'x', status: 'APPROVED', effectiveStatus: 'EXPIRED',
  requestedAt: '2026-09-01T10:00:00Z', requestedDurationMinutes: 30, expiresAt: '2026-09-01T10:30:00Z', auditTrail: [],
}
const pendingMine = { ...expired, id: 6, status: 'PENDING', effectiveStatus: 'PENDING', expiresAt: undefined }

// REQ-IAM-004 acceptance criteria AC-1, AC-2 (UI), AC-8, AC-9, AC-10, AC-11 and decision C24.
describe('PrivilegedAccessRequestsCard', () => {
  beforeEach(() => {
    for (const m of [listRequestable, listMine, request, withdraw]) m.mockReset()
  })

  it('offers the requestable permissions and submits a request', async () => {
    listRequestable.mockResolvedValue(requestable)
    listMine.mockResolvedValueOnce([]).mockResolvedValueOnce([pendingMine])
    request.mockResolvedValue(pendingMine)
    renderWithProviders(<PrivilegedAccessRequestsCard />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('combobox', { name: /Permission/ }))
    const options = within(await screen.findByRole('listbox')).getAllByRole('option')
    expect(options.map((o) => o.textContent)).toEqual(['MANAGE_CATALOG (Platform)', 'MANAGE_USERS (Organization)'])
    await user.click(options[1])
    await user.type(screen.getByRole('textbox', { name: /Justification/ }), 'Covering for the admin')
    const duration = screen.getByRole('spinbutton', { name: /Duration/ })
    await user.clear(duration)
    await user.type(duration, '90')
    await user.click(screen.getByRole('button', { name: 'Request access' }))

    expect(request).toHaveBeenCalledWith({ permissionName: 'MANAGE_USERS', justification: 'Covering for the admin', durationMinutes: 90 })
    expect(await screen.findByRole('button', { name: 'Withdraw' })).toBeInTheDocument()
  })

  it('shows the backend message for an invalid duration', async () => {
    listRequestable.mockResolvedValue(requestable)
    listMine.mockResolvedValue([])
    request.mockRejectedValue(new ApiError(400, 'Requested duration must be between 1 and 480 minutes.'))
    renderWithProviders(<PrivilegedAccessRequestsCard />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('combobox', { name: /Permission/ }))
    await user.click(within(await screen.findByRole('listbox')).getAllByRole('option')[0])
    await user.type(screen.getByRole('textbox', { name: /Justification/ }), 'x')
    await user.click(screen.getByRole('button', { name: 'Request access' }))

    expect(await screen.findByText('Requested duration must be between 1 and 480 minutes.')).toBeInTheDocument()
  })

  it('shows an expired grant and lets the requester withdraw a pending one', async () => {
    listRequestable.mockResolvedValue([])
    listMine.mockResolvedValue([pendingMine, expired])
    withdraw.mockResolvedValue({ ...pendingMine, status: 'REVOKED', effectiveStatus: 'REVOKED' })
    renderWithProviders(<PrivilegedAccessRequestsCard />)

    expect(await screen.findByText('EXPIRED')).toBeInTheDocument()
    expect(screen.getByText('There are no permissions you can request.')).toBeInTheDocument()
    const withdrawButtons = screen.getAllByRole('button', { name: 'Withdraw' })
    expect(withdrawButtons).toHaveLength(1)
    await userEvent.setup().click(withdrawButtons[0])
    expect(withdraw).toHaveBeenCalledWith(6)
  })

  it('has no detectable a11y violations', async () => {
    listRequestable.mockResolvedValue(requestable)
    listMine.mockResolvedValue([pendingMine])
    const { container } = renderWithProviders(<PrivilegedAccessRequestsCard />)
    await screen.findByRole('button', { name: 'Withdraw' })
    expect(await axe(container)).toHaveNoViolations()
  })
})
