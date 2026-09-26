import '../../i18n'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { OrganizationPrivilegedAccessCard } from './OrganizationPrivilegedAccessCard'

const listPending = vi.fn()
const approve = vi.fn()
const reject = vi.fn()
const revoke = vi.fn()
const listActive = vi.fn()

vi.mock('../../api/privilegedAccessApi', () => ({
  organizationPrivilegedAccessApi: {
    listPending: () => listPending(),
    listActive: () => listActive(),
    approve: (id: number, note?: string) => approve(id, note),
    reject: (id: number, note?: string) => reject(id, note),
    revoke: (id: number, note?: string) => revoke(id, note),
  },
}))

const pending = {
  id: 42, scope: 'ORGANIZATION', organizationId: 1, permissionName: 'MANAGE_USERS', justification: 'Covering this week',
  status: 'PENDING', effectiveStatus: 'PENDING', requestedAt: '2026-09-20T10:00:00Z', requestedDurationMinutes: 60, auditTrail: [],
}

// REQ-IAM-004 acceptance criteria AC-4, AC-5 (UI), AC-6, AC-10.
describe('OrganizationPrivilegedAccessCard', () => {
  beforeEach(() => {
    for (const m of [listPending, approve, reject, revoke, listActive]) m.mockReset()
    listActive.mockResolvedValue([])
  })

  it('approves a pending request with a note and reloads', async () => {
    listPending.mockResolvedValueOnce([pending]).mockResolvedValueOnce([])
    approve.mockResolvedValue({ ...pending, status: 'APPROVED' })
    renderWithProviders(<OrganizationPrivilegedAccessCard />)

    const user = userEvent.setup()
    await user.type(await screen.findByLabelText('Note (optional)'), 'ok for coverage')
    await user.click(screen.getByRole('button', { name: 'Approve' }))

    expect(approve).toHaveBeenCalledWith(42, 'ok for coverage')
    expect(await screen.findByText('No pending requests.')).toBeInTheDocument()
  })

  it('shows the backend refusal of a self-approval', async () => {
    listPending.mockResolvedValue([pending])
    approve.mockRejectedValue(new ApiError(403, 'You cannot approve your own request.'))
    renderWithProviders(<OrganizationPrivilegedAccessCard />)

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Approve' }))
    expect(await screen.findByText('You cannot approve your own request.')).toBeInTheDocument()
  })

  it('renders nothing without MANAGE_PRIVILEGED_ACCESS', async () => {
    listPending.mockRejectedValue(new ApiError(403, 'You do not have permission to do this'))
    const { container } = renderWithProviders(<OrganizationPrivilegedAccessCard />)
    await waitFor(() => expect(container.querySelector('section')).toBeNull())
  })

  it('has no detectable a11y violations', async () => {
    listPending.mockResolvedValue([pending])
    const { container } = renderWithProviders(<OrganizationPrivilegedAccessCard />)
    await screen.findByText('MANAGE_USERS')
    expect(await axe(container)).toHaveNoViolations()
  })

  // REQ-IAM-004.7 (sprint audit 2026-09-26): revoke an active grant early.
  it('lists active grants with who holds them and revokes one early', async () => {
    const active = { ...pending, id: 50, status: 'APPROVED', effectiveStatus: 'APPROVED', expiresAt: '2026-09-26T12:00:00Z', requesterEmail: 'sam@example.com' }
    listPending.mockResolvedValue([])
    listActive.mockResolvedValueOnce([active]).mockResolvedValueOnce([])
    revoke.mockResolvedValue({ ...active, status: 'REVOKED', effectiveStatus: 'REVOKED' })
    renderWithProviders(<OrganizationPrivilegedAccessCard />)

    expect(await screen.findByText(/Requested by sam@example.com/)).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Revoke MANAGE_USERS for sam@example.com' }))
    expect(revoke).toHaveBeenCalledWith(50, undefined)
    expect(await screen.findByText('No active grants.')).toBeInTheDocument()
  })
})
