import '../../i18n'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { OrganizationInvitationsCard } from './OrganizationInvitationsCard'

const list = vi.fn()
const create = vi.fn()
const resend = vi.fn()
const revoke = vi.fn()
const structureNodes = vi.fn()

vi.mock('../../api/invitationsApi', () => ({
  invitationsApi: {
    list: () => list(), create: (i: unknown) => create(i), resend: (id: number) => resend(id), revoke: (id: number) => revoke(id),
    structureNodes: () => structureNodes(),
  },
}))

const inv = (id: number, email: string, status: string, extra = {}) => ({
  id, email, status, orgRole: 'MEMBER', invitedByCustomerId: 1, invitedByName: 'Asha Rao', invitedAt: '2026-10-08T00:00:00Z',
  expiresAt: '2026-10-15T00:00:00Z', lastSentAt: '2026-10-08T00:00:00Z', sendCount: 1, ...extra,
})

describe('OrganizationInvitationsCard', () => {
  beforeEach(() => {
    for (const m of [list, create, resend, revoke, structureNodes]) m.mockReset()
    list.mockResolvedValue([inv(1, 'a@x.example', 'PENDING'), inv(2, 'b@x.example', 'ACCEPTED', { acceptedAt: '2026-10-09T00:00:00Z' }),
      inv(3, 'c@x.example', 'EXPIRED'), inv(4, 'd@x.example', 'REVOKED')])
    structureNodes.mockResolvedValue([{ id: 5, name: 'Ops', type: 'DEPARTMENT', path: 'Acme › Ops' }])
  })

  it('lists invitations with the right actions per status and has no accessibility violations', async () => {
    const { container } = renderWithProviders(<OrganizationInvitationsCard canInviteAdmin />)
    const table = await screen.findByRole('table', { name: 'Invitations' })
    expect(within(table).getByRole('button', { name: 'Resend invitation to a@x.example' })).toBeInTheDocument()
    expect(within(table).getByRole('button', { name: 'Revoke invitation to a@x.example' })).toBeInTheDocument()
    expect(within(table).queryByRole('button', { name: 'Resend invitation to b@x.example' })).not.toBeInTheDocument()
    expect(within(table).getByRole('button', { name: 'Resend invitation to c@x.example' })).toBeInTheDocument()
    expect(within(table).queryByRole('button', { name: 'Revoke invitation to c@x.example' })).not.toBeInTheDocument()
    expect(within(table).getByRole('button', { name: 'Resend invitation to d@x.example' })).toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('filters by status and email', async () => {
    renderWithProviders(<OrganizationInvitationsCard canInviteAdmin />)
    await screen.findByRole('table')
    const user = userEvent.setup()
    await user.type(screen.getByLabelText('Search email'), 'b@')
    expect(screen.queryByText('a@x.example')).not.toBeInTheDocument()
    expect(screen.getByText('b@x.example')).toBeInTheDocument()
  })

  it('sends an invitation with a role and a structure node', async () => {
    create.mockResolvedValue({ invitation: inv(9, 'new@x.example', 'PENDING'), emailSent: true })
    renderWithProviders(<OrganizationInvitationsCard canInviteAdmin />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Invite user' }))
    const dialog = await screen.findByRole('dialog')
    await user.type(within(dialog).getByLabelText(/^Email/), 'new@x.example')
    await user.click(within(dialog).getByRole('combobox', { name: /Structure node/ }))
    await user.click(await screen.findByRole('option', { name: 'Acme › Ops' }))
    await user.click(within(dialog).getByRole('button', { name: 'Send invitation' }))
    expect(create).toHaveBeenCalledWith({ email: 'new@x.example', orgRole: 'MEMBER', orgNodeId: 5 })
    expect(await screen.findByText('Invitation sent to new@x.example.')).toBeInTheDocument()
  })

  it('disables the administrator role for a delegated inviter', async () => {
    renderWithProviders(<OrganizationInvitationsCard canInviteAdmin={false} />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Invite user' }))
    await user.click(await screen.findByRole('combobox', { name: 'Organization role' }))
    expect(await screen.findByRole('option', { name: 'Organization admin' })).toHaveAttribute('aria-disabled', 'true')
  })

  it('shows the server reason and offers Resend for a pending duplicate', async () => {
    create.mockRejectedValue(new ApiError(409, 'An invitation to this email is already pending. Resend it instead.'))
    renderWithProviders(<OrganizationInvitationsCard canInviteAdmin />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Invite user' }))
    const dialog = await screen.findByRole('dialog')
    await user.type(within(dialog).getByLabelText(/^Email/), 'a@x.example')
    await user.click(within(dialog).getByRole('button', { name: 'Send invitation' }))
    expect(await within(dialog).findByText(/already pending/)).toBeInTheDocument()
    expect(within(dialog).getByText(/Use Resend in the list/)).toBeInTheDocument()
  })

  it('resends, and revokes after confirming', async () => {
    resend.mockResolvedValue({ invitation: inv(1, 'a@x.example', 'PENDING'), emailSent: true })
    revoke.mockResolvedValue(inv(1, 'a@x.example', 'REVOKED'))
    renderWithProviders(<OrganizationInvitationsCard canInviteAdmin />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Resend invitation to a@x.example' }))
    expect(resend).toHaveBeenCalledWith(1)
    expect(await screen.findByText('Invitation resent to a@x.example.')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Revoke invitation to a@x.example' }))
    await user.click(within(await screen.findByRole('dialog')).getByRole('button', { name: 'Revoke' }))
    expect(revoke).toHaveBeenCalledWith(1)
  })

  it('warns when the email could not be sent', async () => {
    resend.mockResolvedValue({ invitation: inv(1, 'a@x.example', 'PENDING'), emailSent: false })
    renderWithProviders(<OrganizationInvitationsCard canInviteAdmin />)
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Resend invitation to a@x.example' }))
    expect(await screen.findByText(/email could not be sent/)).toBeInTheDocument()
  })
})
