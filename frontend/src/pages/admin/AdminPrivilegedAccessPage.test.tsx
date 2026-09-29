import '../../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminPrivilegedAccessPage } from './AdminPrivilegedAccessPage'

const listPending = vi.fn()
const listActive = vi.fn()
const approve = vi.fn()
const reject = vi.fn()
const revoke = vi.fn()

vi.mock('../../api/platformPrivilegedAccessApi', () => ({
  platformPrivilegedAccessApi: {
    listPending: () => listPending(),
    listActive: () => listActive(),
    approve: (id: number, note?: string) => approve(id, note),
    reject: (id: number, note?: string) => reject(id, note),
    revoke: (id: number, note?: string) => revoke(id, note),
  },
}))

const base = {
  scope: 'PLATFORM', permissionName: 'MANAGE_CATALOG', justification: 'Fix pricing', requestedAt: '2026-09-26T08:00:00Z',
  requestedDurationMinutes: 60, auditTrail: [], requesterEmail: 'ops@vyoog.example',
}
const pending = { ...base, id: 1, status: 'PENDING', effectiveStatus: 'PENDING' }
const active = { ...base, id: 2, status: 'APPROVED', effectiveStatus: 'APPROVED', expiresAt: '2026-09-26T09:00:00Z' }

// REQ-IAM-004 platform scope, and REQ-IAM-004.7 early revocation.
describe('AdminPrivilegedAccessPage', () => {
  beforeEach(() => {
    for (const m of [listPending, listActive, approve, reject, revoke]) m.mockReset()
  })

  it('approves a pending request and refreshes the active grants', async () => {
    listPending.mockResolvedValueOnce([pending]).mockResolvedValueOnce([])
    listActive.mockResolvedValueOnce([]).mockResolvedValueOnce([{ ...pending, ...active, id: 1 }])
    approve.mockResolvedValue({ ...pending, status: 'APPROVED' })
    renderWithProviders(<AdminPrivilegedAccessPage />)

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Approve' }))
    expect(approve).toHaveBeenCalledWith(1, undefined)
    expect(await screen.findByRole('button', { name: 'Revoke MANAGE_CATALOG for ops@vyoog.example' })).toBeInTheDocument()
  })

  it('revokes an active platform grant with a note', async () => {
    listPending.mockResolvedValue([])
    listActive.mockResolvedValueOnce([active]).mockResolvedValueOnce([])
    revoke.mockResolvedValue({ ...active, status: 'REVOKED' })
    renderWithProviders(<AdminPrivilegedAccessPage />)

    const user = userEvent.setup()
    await user.type(await screen.findByLabelText('Note (optional)'), 'Work finished')
    await user.click(screen.getByRole('button', { name: 'Revoke MANAGE_CATALOG for ops@vyoog.example' }))
    expect(revoke).toHaveBeenCalledWith(2, 'Work finished')
    expect(await screen.findByText('No active grants.')).toBeInTheDocument()
  })

  it('shows the backend message when a grant is no longer active', async () => {
    listPending.mockResolvedValue([])
    listActive.mockResolvedValue([active])
    revoke.mockRejectedValue(new ApiError(400, 'This request is no longer active.'))
    renderWithProviders(<AdminPrivilegedAccessPage />)

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Revoke MANAGE_CATALOG for ops@vyoog.example' }))
    expect(await screen.findByText('This request is no longer active.')).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    listPending.mockResolvedValue([pending])
    listActive.mockResolvedValue([active])
    const { container } = renderWithProviders(<AdminPrivilegedAccessPage />)
    await screen.findByText(/Requested by ops@vyoog.example/)
    expect(await axe(container)).toHaveNoViolations()
  })
})
