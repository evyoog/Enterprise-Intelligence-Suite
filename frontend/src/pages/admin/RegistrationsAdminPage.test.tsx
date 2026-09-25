import '../../i18n'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { RegistrationsAdminPage } from './RegistrationsAdminPage'

const listAllOrganizations = vi.fn()
const updateOrganization = vi.fn()
const changeOrganizationLifecycle = vi.fn()

vi.mock('../../api/adminRegistrationApi', () => ({
  adminRegistrationApi: {
    listAllOrganizations: () => listAllOrganizations(),
    updateOrganization: (id: number, p: unknown) => updateOrganization(id, p),
    changeOrganizationLifecycle: (id: number, action: string, reason?: string) => changeOrganizationLifecycle(id, action, reason),
    listAllIndividuals: vi.fn().mockResolvedValue([]),
    listPendingProvisioning: vi.fn().mockResolvedValue([]),
    updateSeats: vi.fn(),
    linkKeycloakUser: vi.fn(),
  },
}))

const acme = {
  id: 7, name: 'Acme', code: 'ACME', businessEmail: 'biz@acme.example', country: 'India', city: 'Chennai',
  billingSameAsAddress: true, licensedSeats: 5, activeMemberCount: 2, status: 'COMPLETED', lifecycleStatus: 'ACTIVE',
  adminKeycloakLinked: true, createdAt: '2026-09-01T00:00:00Z',
}

// REQ-TEN-001 acceptance criteria AC-1..AC-8 (UI).
describe('RegistrationsAdminPage — organization lifecycle', () => {
  beforeEach(() => {
    for (const m of [listAllOrganizations, updateOrganization, changeOrganizationLifecycle]) m.mockReset()
  })

  it('edits the company details and leaves the code alone', async () => {
    listAllOrganizations.mockResolvedValue([acme])
    updateOrganization.mockResolvedValue({ ...acme, name: 'Acme Corp' })
    renderWithProviders(<RegistrationsAdminPage />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Edit Acme' }))
    const dialog = await screen.findByRole('dialog')
    const name = within(dialog).getByRole('textbox', { name: /^Name/ })
    await user.clear(name)
    await user.type(name, 'Acme Corp')
    await user.click(within(dialog).getByRole('button', { name: 'Save changes' }))

    expect(updateOrganization).toHaveBeenCalledWith(7, expect.objectContaining({
      name: 'Acme Corp', businessEmail: 'biz@acme.example', country: 'India', city: 'Chennai', billingSameAsAddress: true,
    }))
    expect(updateOrganization.mock.calls[0][1]).not.toHaveProperty('code')
    expect(await screen.findByText('Acme Corp')).toBeInTheDocument()
  })

  it('disables Save while a required field is empty', async () => {
    listAllOrganizations.mockResolvedValue([acme])
    renderWithProviders(<RegistrationsAdminPage />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Edit Acme' }))
    const dialog = await screen.findByRole('dialog')
    await user.clear(within(dialog).getByRole('textbox', { name: /^Name/ }))
    expect(within(dialog).getByRole('button', { name: 'Save changes' })).toBeDisabled()
  })

  it('suspends with a reason and reports how many logins were disabled', async () => {
    listAllOrganizations.mockResolvedValue([acme])
    changeOrganizationLifecycle.mockResolvedValue({
      organization: { ...acme, lifecycleStatus: 'SUSPENDED' }, accountsUpdated: 2, accountsNotUpdated: [],
    })
    renderWithProviders(<RegistrationsAdminPage />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Suspend Acme' }))
    const dialog = await screen.findByRole('dialog')
    await user.type(within(dialog).getByRole('textbox', { name: /Reason/ }), 'Unpaid invoice')
    await user.click(within(dialog).getByRole('button', { name: 'Suspend' }))

    expect(changeOrganizationLifecycle).toHaveBeenCalledWith(7, 'suspend', 'Unpaid invoice')
    expect(await screen.findByText(/Acme is suspended\. 2 member login\(s\) disabled\./)).toBeInTheDocument()
    expect(screen.getByText('Lifecycle: Suspended')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Activate Acme' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Suspend Acme' })).not.toBeInTheDocument()
  })

  it('warns about logins Keycloak did not update', async () => {
    listAllOrganizations.mockResolvedValue([acme])
    changeOrganizationLifecycle.mockResolvedValue({
      organization: { ...acme, lifecycleStatus: 'CLOSED' }, accountsUpdated: 1, accountsNotUpdated: ['ops@acme.example'],
    })
    renderWithProviders(<RegistrationsAdminPage />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Close Acme' }))
    await user.click(within(await screen.findByRole('dialog')).getByRole('button', { name: 'Close organization' }))

    expect(changeOrganizationLifecycle).toHaveBeenCalledWith(7, 'close', '')
    expect(await screen.findByText(/ops@acme\.example\. Repeat the same action to retry\./)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Edit Acme' })).toBeDisabled()
  })

  it('shows the backend refusal and keeps the dialog open', async () => {
    listAllOrganizations.mockResolvedValue([acme])
    changeOrganizationLifecycle.mockRejectedValue(
      new ApiError(400, 'You are a member of this organization and cannot suspend or close it yourself.'),
    )
    renderWithProviders(<RegistrationsAdminPage />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Suspend Acme' }))
    const dialog = await screen.findByRole('dialog')
    await user.click(within(dialog).getByRole('button', { name: 'Suspend' }))

    expect(await within(dialog).findByText(/cannot suspend or close it yourself/)).toBeInTheDocument()
  })

  it('offers Activate and hides Close for a closed organization', async () => {
    listAllOrganizations.mockResolvedValue([{ ...acme, lifecycleStatus: 'CLOSED' }])
    renderWithProviders(<RegistrationsAdminPage />)

    expect(await screen.findByRole('button', { name: 'Activate Acme' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Close Acme' })).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Edit Acme' })).toBeDisabled()
  })

  it('has no detectable a11y violations', async () => {
    listAllOrganizations.mockResolvedValue([acme])
    const { container } = renderWithProviders(<RegistrationsAdminPage />)
    await screen.findByRole('button', { name: 'Edit Acme' })
    expect(await axe(container)).toHaveNoViolations()
  })
})
