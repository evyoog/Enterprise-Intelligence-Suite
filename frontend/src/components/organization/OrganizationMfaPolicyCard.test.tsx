import '../../i18n'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { OrganizationMfaPolicyCard } from './OrganizationMfaPolicyCard'

const getMyOrganization = vi.fn()
const updateMfaPolicy = vi.fn()

vi.mock('../../api/registrationApi', () => ({
  organizationApi: {
    getMyOrganization: () => getMyOrganization(),
    updateMfaPolicy: (v: boolean) => updateMfaPolicy(v),
  },
}))

// REQ-IAM-001 acceptance criteria AC-1, AC-2, AC-3 (UI), AC-6.
describe('OrganizationMfaPolicyCard', () => {
  beforeEach(() => {
    getMyOrganization.mockReset()
    updateMfaPolicy.mockReset()
  })

  it('shows the current policy and turns it on', async () => {
    getMyOrganization.mockResolvedValue({ id: 1, mfaRequired: false })
    updateMfaPolicy.mockResolvedValue({ id: 1, mfaRequired: true })
    renderWithProviders(<OrganizationMfaPolicyCard />)

    const toggle = await screen.findByRole('switch', { name: 'Require MFA for all members' })
    expect(toggle).not.toBeChecked()
    await userEvent.setup().click(toggle)

    expect(updateMfaPolicy).toHaveBeenCalledWith(true)
    await waitFor(() => expect(toggle).toBeChecked())
    expect(screen.getByText(/MFA is required for every member/)).toBeInTheDocument()
  })

  it('shows the backend message when the change is refused', async () => {
    getMyOrganization.mockResolvedValue({ id: 1, mfaRequired: false })
    updateMfaPolicy.mockRejectedValue(new ApiError(403, 'You do not have permission to do this'))
    renderWithProviders(<OrganizationMfaPolicyCard />)

    await userEvent.setup().click(await screen.findByRole('switch'))
    expect(await screen.findByText('You do not have permission to do this')).toBeInTheDocument()
    expect(screen.getByRole('switch')).not.toBeChecked()
  })

  it('renders nothing for an account without organization access', async () => {
    getMyOrganization.mockRejectedValue(new ApiError(404, 'You are not a member of an organization'))
    const { container } = renderWithProviders(<OrganizationMfaPolicyCard />)
    await waitFor(() => expect(container.querySelector('section')).toBeNull())
  })

  it('has no detectable a11y violations', async () => {
    getMyOrganization.mockResolvedValue({ id: 1, mfaRequired: true })
    const { container } = renderWithProviders(<OrganizationMfaPolicyCard />)
    await screen.findByRole('switch')
    expect(await axe(container)).toHaveNoViolations()
  })
})
