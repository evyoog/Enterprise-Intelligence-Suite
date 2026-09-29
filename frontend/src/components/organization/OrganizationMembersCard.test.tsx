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
const changeMemberStatus = vi.fn()
const reviewMemberAccess = vi.fn()

vi.mock('../../api/registrationApi', () => ({
  organizationApi: {
    listMyOrgUsers: () => listMyOrgUsers(),
    changeMemberRole: (id: number, role: string) => changeMemberRole(id, role),
    changeMemberStatus: (id: number, action: string) => changeMemberStatus(id, action),
    reviewMemberAccess: (id: number) => reviewMemberAccess(id),
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
    changeMemberStatus.mockReset()
    reviewMemberAccess.mockReset()
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

  // 05.03.01 User Lifecycle (sprint 2026.4.1).
  it('suspends a member, then offers reactivate instead of suspend', async () => {
    const user = userEvent.setup()
    listMyOrgUsers.mockResolvedValue([ada, bob])
    changeMemberStatus.mockResolvedValue({ ...bob, status: 'SUSPENDED' })
    renderWithProviders(<OrganizationMembersCard />)

    await user.click(await screen.findByRole('button', { name: 'Suspend Bob Byte' }))
    expect(changeMemberStatus).toHaveBeenCalledWith(8, 'SUSPEND')
    expect(await screen.findByRole('button', { name: 'Reactivate Bob Byte' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Suspend Bob Byte' })).not.toBeInTheDocument()
  })

  it('asks for confirmation before removing a member, and does nothing if declined', async () => {
    const user = userEvent.setup()
    listMyOrgUsers.mockResolvedValue([ada, bob])
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(false)
    renderWithProviders(<OrganizationMembersCard />)

    await user.click(await screen.findByRole('button', { name: 'Remove Bob Byte' }))
    expect(confirmSpy).toHaveBeenCalled()
    expect(changeMemberStatus).not.toHaveBeenCalled()
    confirmSpy.mockRestore()
  })

  it('records an access review', async () => {
    const user = userEvent.setup()
    listMyOrgUsers.mockResolvedValue([ada, bob])
    reviewMemberAccess.mockResolvedValue({ ...bob, lastReviewedAt: '2026-10-01T00:00:00Z' })
    renderWithProviders(<OrganizationMembersCard />)

    expect(await screen.findAllByText('Never reviewed')).toHaveLength(2)
    await user.click(screen.getByRole('button', { name: 'Review Bob Byte' }))
    expect(reviewMemberAccess).toHaveBeenCalledWith(8)
    await waitFor(() => expect(screen.getAllByText('Never reviewed')).toHaveLength(1))
  })

  it('has no detectable a11y violations', async () => {
    listMyOrgUsers.mockResolvedValue([ada, bob])
    const { container } = renderWithProviders(<OrganizationMembersCard />)
    await screen.findByText('bob@example.com')
    expect(await axe(container)).toHaveNoViolations()
  })
})
