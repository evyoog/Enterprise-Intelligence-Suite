import '../../i18n'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { OrganizationMembersCard } from './OrganizationMembersCard'

const listMyOrgUsers = vi.fn()
const changeMemberRole = vi.fn()

vi.mock('../../api/registrationApi', () => ({
  organizationApi: {
    listMyOrgUsers: () => listMyOrgUsers(),
    changeMemberRole: (id: number, role: string) => changeMemberRole(id, role),
  },
}))

const ada = { organizationMemberId: 7, customerId: 70, firstName: 'Ada', lastName: 'Lovelace', email: 'ada@example.com', orgRole: 'ORG_ADMIN', status: 'ACTIVE' }
const bob = { organizationMemberId: 8, customerId: 80, firstName: 'Bob', lastName: 'Byte', email: 'bob@example.com', orgRole: 'MEMBER', status: 'ACTIVE' }

async function chooseRole(memberName: string, roleLabel: string) {
  const user = userEvent.setup()
  await user.click(await screen.findByRole('combobox', { name: `Role for ${memberName}` }))
  await user.click(within(await screen.findByRole('listbox')).getByRole('option', { name: roleLabel }))
}

// REQ-IAM-002 acceptance criteria AC-1, AC-2, AC-3 (UI), AC-5, AC-6.
describe('OrganizationMembersCard', () => {
  beforeEach(() => {
    listMyOrgUsers.mockReset()
    changeMemberRole.mockReset()
  })

  it('lists members and changes a role', async () => {
    listMyOrgUsers.mockResolvedValue([ada, bob])
    changeMemberRole.mockResolvedValue({ ...bob, orgRole: 'ORG_ADMIN' })
    renderWithProviders(<OrganizationMembersCard />)

    expect(await screen.findByText('bob@example.com')).toBeInTheDocument()
    await chooseRole('Bob Byte', 'Organization admin')

    expect(changeMemberRole).toHaveBeenCalledWith(8, 'ORG_ADMIN')
    await waitFor(() => expect(screen.getByRole('combobox', { name: 'Role for Bob Byte' })).toHaveTextContent('Organization admin'))
  })

  it('shows the backend refusal, for example the last administrator', async () => {
    listMyOrgUsers.mockResolvedValue([ada, bob])
    changeMemberRole.mockRejectedValue(new ApiError(400, "Cannot remove the organization's last administrator."))
    renderWithProviders(<OrganizationMembersCard />)

    await chooseRole('Ada Lovelace', 'Member')
    expect(await screen.findByText("Cannot remove the organization's last administrator.")).toBeInTheDocument()
    expect(screen.getByRole('combobox', { name: 'Role for Ada Lovelace' })).toHaveTextContent('Organization admin')
  })

  it('renders nothing without MANAGE_USERS', async () => {
    listMyOrgUsers.mockRejectedValue(new ApiError(403, 'You do not have permission to do this'))
    const { container } = renderWithProviders(<OrganizationMembersCard />)
    await waitFor(() => expect(container.querySelector('section')).toBeNull())
  })

  it('has no detectable a11y violations', async () => {
    listMyOrgUsers.mockResolvedValue([ada, bob])
    const { container } = renderWithProviders(<OrganizationMembersCard />)
    await screen.findByText('bob@example.com')
    expect(await axe(container)).toHaveNoViolations()
  })
})
