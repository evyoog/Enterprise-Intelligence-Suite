import '../../i18n'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { OrganizationGroupsCard } from './OrganizationGroupsCard'

const list = vi.fn()
const create = vi.fn()
const remove = vi.fn()
const addMember = vi.fn()
const removeMember = vi.fn()
const listMyOrgUsers = vi.fn()

vi.mock('../../api/groupsApi', () => ({
  groupsApi: {
    list: () => list(),
    create: (name: string) => create(name),
    remove: (id: number) => remove(id),
    addMember: (groupId: number, memberId: number) => addMember(groupId, memberId),
    removeMember: (groupId: number, memberId: number) => removeMember(groupId, memberId),
  },
}))

vi.mock('../../api/registrationApi', () => ({
  organizationApi: { listMyOrgUsers: () => listMyOrgUsers() },
}))

const bob = { organizationMemberId: 8, customerId: 80, firstName: 'Bob', lastName: 'Byte', email: 'bob@example.com', orgRole: 'MEMBER', status: 'ACTIVE' }
const ada = { organizationMemberId: 7, customerId: 70, firstName: 'Ada', lastName: 'Lovelace', email: 'ada@example.com', orgRole: 'ORG_ADMIN', status: 'ACTIVE' }

// 05.04.01 Groups (sprint 2026.4.1).
describe('OrganizationGroupsCard', () => {
  beforeEach(() => {
    list.mockReset()
    create.mockReset()
    remove.mockReset()
    addMember.mockReset()
    removeMember.mockReset()
    listMyOrgUsers.mockReset()
    listMyOrgUsers.mockResolvedValue([ada, bob])
  })

  it('creates a group', async () => {
    const user = userEvent.setup()
    list.mockResolvedValue([])
    create.mockResolvedValue({ id: 1, name: 'Engineering', members: [] })
    renderWithProviders(<OrganizationGroupsCard />)

    await screen.findByText('No groups yet.')
    await user.type(screen.getByLabelText('New group name'), 'Engineering')
    await user.click(screen.getByRole('button', { name: 'Create group' }))

    expect(create).toHaveBeenCalledWith('Engineering')
    expect(await screen.findByText('Engineering')).toBeInTheDocument()
  })

  it('adds and removes a member from a group', async () => {
    const user = userEvent.setup()
    list.mockResolvedValue([{ id: 1, name: 'Engineering', members: [] }])
    addMember.mockResolvedValue({ id: 1, name: 'Engineering', members: [bob] })
    removeMember.mockResolvedValue({ id: 1, name: 'Engineering', members: [] })
    renderWithProviders(<OrganizationGroupsCard />)

    await screen.findByText('Engineering')
    await user.click(screen.getByLabelText('Add member'))
    await user.click(await screen.findByRole('option', { name: 'Bob Byte' }))
    await user.click(screen.getByRole('button', { name: 'Add' }))

    expect(addMember).toHaveBeenCalledWith(1, 8)
    expect(await screen.findByText('Bob Byte')).toBeInTheDocument()

    await user.click(screen.getByTestId('CancelIcon'))
    await waitFor(() => expect(removeMember).toHaveBeenCalledWith(1, 8))
  })

  it('deletes a group', async () => {
    const user = userEvent.setup()
    list.mockResolvedValue([{ id: 1, name: 'Temporary', members: [] }])
    remove.mockResolvedValue(undefined)
    renderWithProviders(<OrganizationGroupsCard />)

    await user.click(await screen.findByRole('button', { name: 'Delete Temporary' }))
    expect(remove).toHaveBeenCalledWith(1)
    await waitFor(() => expect(screen.queryByText('Temporary')).not.toBeInTheDocument())
  })

  it('renders nothing without MANAGE_USERS', async () => {
    list.mockRejectedValue(new ApiError(403, 'You do not have permission to do this'))
    const { container } = renderWithProviders(<OrganizationGroupsCard />)
    await waitFor(() => expect(container.querySelector('section')).toBeNull())
  })

  it('has no detectable a11y violations', async () => {
    list.mockResolvedValue([{ id: 1, name: 'Engineering', members: [bob] }])
    const { container } = renderWithProviders(<OrganizationGroupsCard />)
    await screen.findByText('Engineering')
    expect(await axe(container)).toHaveNoViolations()
  })
})
