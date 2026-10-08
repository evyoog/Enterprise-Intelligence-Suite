import '../../i18n'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminOrganizationsPage } from './AdminOrganizationsPage'

const directory = vi.fn()
vi.mock('../../api/orgDirectoryApi', () => ({ orgDirectoryApi: { directory: () => directory() } }))
vi.mock('../../api/adminRegistrationApi', () => ({ adminRegistrationApi: { resetMfa: vi.fn() } }))

const base = {
  seatsLicensed: 0, seatsUsed: 0, signInLinked: true, mfaRequired: false, productCount: 0, hierarchyNodes: 0, openTickets: 0,
  outstandingInvoices: 0, missingFields: [] as string[], status: 'COMPLETED', createdAt: '2026-09-01T00:00:00Z',
}
const rows = [
  { ...base, kind: 'ORGANIZATION', id: 1, name: 'Acme', code: 'ACME', industry: 'Software', country: 'India', city: 'Chennai', email: 'biz@acme.example',
    contactName: 'Asha Rao', contactEmail: 'asha@acme.example', seatsLicensed: 5, seatsUsed: 5, lifecycleStatus: 'ACTIVE', regionName: 'APAC',
    productCount: 2, hierarchyNodes: 4, profileCompletion: 75, missingFields: ['WEBSITE', 'PHONE', 'REGION'] },
  { ...base, kind: 'ORGANIZATION', id: 2, name: 'Beta Corp', code: 'BETA', country: 'Germany', lifecycleStatus: 'SUSPENDED', seatsLicensed: 3, seatsUsed: 1,
    profileCompletion: 100, createdAt: '2026-08-01T00:00:00Z' },
  { ...base, kind: 'INDIVIDUAL', id: 9, name: 'Dina Rao', email: 'dina@example.com', country: 'India', signInLinked: false, profileCompletion: 40,
    missingFields: ['MOBILE', 'JOB_TITLE', 'INDUSTRY'], status: 'EMAIL_VERIFIED' },
]
const summary = { total: 3, organizations: 2, individuals: 1, profileComplete: 1, profileInProgress: 2, profileNotStarted: 0, averageCompletion: 72 }

const pick = async (label: string, option: string) => {
  const user = userEvent.setup()
  await user.click(screen.getByRole('combobox', { name: label }))
  await user.click(await screen.findByRole('option', { name: option }))
}

// REQ-TEN-007 (C83).
describe('AdminOrganizationsPage', () => {
  beforeEach(() => {
    directory.mockReset()
    directory.mockResolvedValue({ rows, summary })
  })

  it('lists organizations and individuals together with profile completion and a summary, and has no separate tabs', async () => {
    renderWithProviders(<AdminOrganizationsPage />)
    const table = await screen.findByRole('table', { name: 'Organizations' })
    expect(within(table).getByText('Acme')).toBeInTheDocument()
    expect(within(table).getByText('Beta Corp')).toBeInTheDocument()
    expect(within(table).getByText('Dina Rao')).toBeInTheDocument()
    expect(within(table).getByText('75%')).toBeInTheDocument()
    expect(within(table).getByText('Not linked')).toBeInTheDocument()
    expect(screen.getByText('72%')).toBeInTheDocument()
    expect(screen.queryByRole('tab')).not.toBeInTheDocument()
    expect(screen.queryByText(/Keycloak provisioning/i)).not.toBeInTheDocument()
  })

  it('narrows by type, by profile completion and by search, and clears the filters', async () => {
    renderWithProviders(<AdminOrganizationsPage />)
    await screen.findByRole('table')
    const user = userEvent.setup()

    await pick('Type', 'Individual')
    expect(screen.queryByText('Acme')).not.toBeInTheDocument()
    expect(screen.getByText('Dina Rao')).toBeInTheDocument()
    expect(screen.getByRole('status')).toHaveTextContent('1 of 3 shown')

    await user.click(screen.getByRole('button', { name: 'Clear filters' }))
    expect(screen.getByText('Acme')).toBeInTheDocument()

    await pick('Profile', 'Complete')
    expect(screen.getByText('Beta Corp')).toBeInTheDocument()
    expect(screen.queryByText('Acme')).not.toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Clear filters' }))

    await user.type(screen.getByLabelText('Search name, code or email'), 'asha@')
    expect(screen.getByText('Acme')).toBeInTheDocument()
    expect(screen.queryByText('Beta Corp')).not.toBeInTheDocument()
  })

  it('filters by seat usage and lifecycle', async () => {
    renderWithProviders(<AdminOrganizationsPage />)
    await screen.findByRole('table')
    await pick('Seats', 'Full')
    expect(screen.getByText('Acme')).toBeInTheDocument()
    expect(screen.queryByText('Beta Corp')).not.toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Clear filters' }))
    await pick('Lifecycle', 'Suspended')
    expect(screen.getByText('Beta Corp')).toBeInTheDocument()
    expect(screen.queryByText('Acme')).not.toBeInTheDocument()
  })

  it('links every row to its detail page and offers lifecycle actions for organizations only', async () => {
    renderWithProviders(<AdminOrganizationsPage />)
    expect(await screen.findByRole('link', { name: 'Open Acme' })).toHaveAttribute('href', '/admin/organizations/organization/1')
    expect(screen.getByRole('link', { name: 'Open Dina Rao' })).toHaveAttribute('href', '/admin/organizations/individual/9')
    expect(screen.getByRole('button', { name: 'Actions for Acme' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Actions for Dina Rao' })).not.toBeInTheDocument()
  })

  it('has no accessibility violations', async () => {
    const { container } = renderWithProviders(<AdminOrganizationsPage />)
    await screen.findByRole('table')
    expect(await axe(container)).toHaveNoViolations()
  })
})
