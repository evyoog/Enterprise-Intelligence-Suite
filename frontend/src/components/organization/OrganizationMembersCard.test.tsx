import '../../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { OrganizationMembersCard } from './OrganizationMembersCard'

const listMyOrgUsers = vi.fn()
const inviters = vi.fn()
const setInvitePermission = vi.fn()

vi.mock('../../api/registrationApi', () => ({
  organizationApi: { listMyOrgUsers: () => listMyOrgUsers(), changeMemberRole: vi.fn(), resetMemberMfa: vi.fn() },
}))
vi.mock('../../api/invitationsApi', () => ({
  invitationsApi: { inviters: () => inviters(), setInvitePermission: (id: number, a: boolean) => setInvitePermission(id, a) },
}))

const admin = { organizationMemberId: 1, customerId: 1, firstName: 'Asha', lastName: 'Rao', email: 'asha@x.example', orgRole: 'ORG_ADMIN', status: 'ACTIVE' }
const john = { organizationMemberId: 2, customerId: 2, firstName: 'John', lastName: 'Doe', email: 'john@x.example', orgRole: 'MEMBER', status: 'ACTIVE' }

// REQ-TEN-008: an administrator allows one member to send invitations.
describe('OrganizationMembersCard — Can invite', () => {
  beforeEach(() => {
    for (const m of [listMyOrgUsers, inviters, setInvitePermission]) m.mockReset()
    listMyOrgUsers.mockResolvedValue([admin, john])
  })

  it('lets an administrator switch invitations on for a member (not for administrators)', async () => {
    inviters.mockResolvedValue({ memberIds: [] })
    setInvitePermission.mockResolvedValue({ memberIds: [2] })
    renderWithProviders(<OrganizationMembersCard />)
    const sw = await screen.findByRole('switch', { name: 'Allow John Doe to send invitations' })
    expect(sw).not.toBeChecked()
    expect(screen.queryByRole('switch', { name: 'Allow Asha Rao to send invitations' })).not.toBeInTheDocument()
    expect(screen.getByText('Always')).toBeInTheDocument()
    await userEvent.setup().click(sw)
    expect(setInvitePermission).toHaveBeenCalledWith(2, true)
    expect(await screen.findByRole('switch', { name: 'Allow John Doe to send invitations' })).toBeChecked()
  })

  it('hides the column when the caller may not manage it', async () => {
    inviters.mockRejectedValue(new ApiError(403, 'You do not have permission to do this'))
    renderWithProviders(<OrganizationMembersCard />)
    await screen.findByText('John Doe')
    expect(screen.queryByText('Can invite')).not.toBeInTheDocument()
  })
})
