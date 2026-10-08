import '../i18n'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../test/renderWithProviders'
import { OrganizationMembersPage } from './OrganizationMembersPage'

const getPermissions = vi.fn()
vi.mock('../api/myPermissionsApi', () => ({ myPermissionsApi: { get: () => getPermissions() } }))
vi.mock('../components/organization/OrganizationMembersCard', () => ({ OrganizationMembersCard: () => <p>members card</p> }))
vi.mock('../components/organization/OrganizationGroupsCard', () => ({ OrganizationGroupsCard: () => <p>groups card</p> }))
vi.mock('../components/organization/OrganizationInvitationsCard', () => ({ OrganizationInvitationsCard: () => <p>invitations card</p> }))
vi.mock('../components/organization/OrganizationStructurePanel', () => ({ OrganizationStructurePanel: () => <p>structure panel</p> }))

// C83/C84: structure and invitations are tabs here, each shown to people who hold its permission.
describe('OrganizationMembersPage', () => {
  beforeEach(() => getPermissions.mockReset())

  it('gives an administrator Members, Groups, Invitations and Org structure and opens a tab from the address', async () => {
    getPermissions.mockResolvedValue({ platform: [], organization: ['MANAGE_ORGANIZATION', 'MANAGE_USERS', 'INVITE_USERS'] })
    renderWithProviders(<OrganizationMembersPage />, { route: '/organization/members?tab=structure' })
    await waitFor(() => expect(screen.getAllByRole('tab').map((t) => t.textContent)).toEqual(['Members', 'Groups', 'Invitations', 'Org structure']))
    expect(screen.getByText('structure panel')).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('tab', { name: 'Invitations' }))
    expect(screen.getByText('invitations card')).toBeInTheDocument()
  })

  it('shows a member who may only invite just the Invitations tab', async () => {
    getPermissions.mockResolvedValue({ platform: [], organization: ['INVITE_USERS'] })
    renderWithProviders(<OrganizationMembersPage />)
    await waitFor(() => expect(screen.getAllByRole('tab').map((t) => t.textContent)).toEqual(['Invitations']))
    expect(screen.getByText('invitations card')).toBeInTheDocument()
    expect(screen.queryByText('members card')).not.toBeInTheDocument()
  })
})
