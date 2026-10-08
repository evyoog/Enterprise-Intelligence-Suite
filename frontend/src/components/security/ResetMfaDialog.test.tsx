import '../../i18n'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { OrganizationMembersCard } from '../organization/OrganizationMembersCard'
import { AdminOrganizationsPage } from '../../pages/admin/AdminOrganizationsPage'

const listMyOrgUsers = vi.fn()
const resetMemberMfa = vi.fn()
const resetMfa = vi.fn()

vi.mock('../../api/registrationApi', () => ({
  organizationApi: {
    listMyOrgUsers: () => listMyOrgUsers(),
    changeMemberRole: vi.fn(),
    resetMemberMfa: (id: number) => resetMemberMfa(id),
  },
}))
vi.mock('../../api/adminRegistrationApi', () => ({
  adminRegistrationApi: {
    listAllOrganizations: vi.fn().mockResolvedValue([]),
    listAllIndividuals: vi.fn().mockResolvedValue([]),
    listPendingProvisioning: vi.fn().mockResolvedValue([]),
    resetMfa: (email: string) => resetMfa(email),
  },
}))

vi.mock('../../api/orgDirectoryApi', () => ({
  orgDirectoryApi: {
    directory: () => Promise.resolve({
      rows: [],
      summary: { total: 0, organizations: 0, individuals: 0, profileComplete: 0, profileInProgress: 0, profileNotStarted: 0, averageCompletion: 0 },
    }),
  },
}))

const member = { organizationMemberId: 9, customerId: 3, firstName: 'Sam', lastName: 'Lee', email: 'sam@example.com', orgRole: 'MEMBER', status: 'ACTIVE' }

// C30 (06.01.02 Recover MFA): admin-assisted two-factor reset.
describe('Two-factor reset by an administrator', () => {
  beforeEach(() => {
    for (const m of [listMyOrgUsers, resetMemberMfa, resetMfa]) m.mockReset()
  })

  it('lets an organization admin reset a member after confirming', async () => {
    listMyOrgUsers.mockResolvedValue([member])
    resetMemberMfa.mockResolvedValue(undefined)
    renderWithProviders(<OrganizationMembersCard />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Reset two-factor authentication for Sam Lee' }))
    const dialog = await screen.findByRole('dialog')
    expect(within(dialog).getByText('Reset two-factor authentication for Sam Lee?')).toBeInTheDocument()
    await user.click(within(dialog).getByRole('button', { name: 'Reset two-factor' }))

    expect(resetMemberMfa).toHaveBeenCalledWith(9)
    expect(await screen.findByText('Two-factor authentication was reset for Sam Lee.')).toBeInTheDocument()
  })

  it('shows the backend refusal and keeps the dialog open', async () => {
    listMyOrgUsers.mockResolvedValue([member])
    resetMemberMfa.mockRejectedValue(new ApiError(400, 'This user has not set up two-factor authentication.'))
    renderWithProviders(<OrganizationMembersCard />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Reset two-factor authentication for Sam Lee' }))
    const dialog = await screen.findByRole('dialog')
    await user.click(within(dialog).getByRole('button', { name: 'Reset two-factor' }))
    expect(await within(dialog).findByText('This user has not set up two-factor authentication.')).toBeInTheDocument()
  })

  it('lets a platform admin reset any account by email', async () => {
    resetMfa.mockResolvedValue(undefined)
    renderWithProviders(<AdminOrganizationsPage />)

    const user = userEvent.setup()
    await user.click(screen.getByRole('button', { name: "Reset a user's 2FA" }))
    const dialog = await screen.findByRole('dialog')
    const confirm = within(dialog).getByRole('button', { name: 'Reset two-factor' })
    expect(confirm).toBeDisabled()
    await user.type(within(dialog).getByRole('textbox', { name: "User's email address" }), 'kim@example.com')
    await user.click(confirm)

    expect(resetMfa).toHaveBeenCalledWith('kim@example.com')
    expect(await screen.findByText('Two-factor authentication was reset for kim@example.com.')).toBeInTheDocument()
  })

  it('has no detectable a11y violations with the dialog open', async () => {
    listMyOrgUsers.mockResolvedValue([member])
    const { container } = renderWithProviders(<OrganizationMembersCard />)
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Reset two-factor authentication for Sam Lee' }))
    await screen.findByRole('dialog')
    expect(await axe(container)).toHaveNoViolations()
  })
})
