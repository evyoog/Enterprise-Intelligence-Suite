import '../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../test/renderWithProviders'
import { OrganizationMembersPage } from './OrganizationMembersPage'

vi.mock('../components/organization/OrganizationMembersCard', () => ({ OrganizationMembersCard: () => <p>members card</p> }))
vi.mock('../components/organization/OrganizationGroupsCard', () => ({ OrganizationGroupsCard: () => <p>groups card</p> }))
vi.mock('../components/organization/OrganizationStructurePanel', () => ({ OrganizationStructurePanel: () => <p>structure panel</p> }))

// C83: the organization structure is a tab here, not a separate sidebar entry.
describe('OrganizationMembersPage', () => {
  it('has Members, Groups and Org structure tabs and opens the structure from the address', async () => {
    renderWithProviders(<OrganizationMembersPage />, { route: '/organization/members?tab=structure' })
    expect(screen.getAllByRole('tab').map((t) => t.textContent)).toEqual(['Members', 'Groups', 'Org structure'])
    expect(screen.getByText('structure panel')).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('tab', { name: 'Members' }))
    expect(screen.getByText('members card')).toBeInTheDocument()
  })
})
