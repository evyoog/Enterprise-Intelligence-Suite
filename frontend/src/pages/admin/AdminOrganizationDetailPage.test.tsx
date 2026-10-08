import '../../i18n'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminOrganizationDetailPage } from './AdminOrganizationDetailPage'

const overview = vi.fn()
const hierarchy = vi.fn()
const individual = vi.fn()
const updateOrganization = vi.fn()
const changeOrganizationLifecycle = vi.fn()

vi.mock('../../api/orgDirectoryApi', () => ({
  orgDirectoryApi: {
    overview: () => overview(),
    hierarchy: () => hierarchy(),
    hierarchyNode: vi.fn().mockResolvedValue({ node: {}, path: ['Acme'], members: [] }),
    hierarchyHistory: vi.fn().mockResolvedValue([]),
    members: vi.fn().mockResolvedValue([{ memberId: 1, customerId: 2, name: 'Asha Rao', email: 'asha@acme.example', orgRole: 'ORG_ADMIN', status: 'ACTIVE', orgNodeName: 'Acme' }]),
    subscriptions: vi.fn().mockResolvedValue([]),
    invoices: vi.fn().mockResolvedValue([]),
    tickets: vi.fn().mockResolvedValue([]),
    individual: () => individual(),
  },
}))
vi.mock('../../api/adminRegistrationApi', () => ({
  adminRegistrationApi: {
    updateOrganization: (id: number, p: unknown) => updateOrganization(id, p),
    changeOrganizationLifecycle: (id: number, action: string, reason?: string) => changeOrganizationLifecycle(id, action, reason),
    updateSeats: vi.fn(),
  },
}))
vi.mock('../../api/auditLogApi', () => ({ auditLogApi: { search: vi.fn().mockResolvedValue({ items: [], totalElements: 0, page: 0, size: 50 }) } }))

const acme = {
  id: 7, name: 'Acme', code: 'ACME', businessEmail: 'biz@acme.example', country: 'India', city: 'Chennai',
  billingSameAsAddress: true, licensedSeats: 5, activeMemberCount: 2, status: 'COMPLETED', lifecycleStatus: 'ACTIVE',
  adminKeycloakLinked: true, createdAt: '2026-09-01T00:00:00Z', allowSeatOverage: false,
}
const ov = (org = acme) => ({ organization: org, completion: { percent: 67, missing: ['WEBSITE', 'PHONE'] }, mfaRequired: true })
const node = (id: number, parentId: number | null, name: string, type: string) => ({
  id, parentId, name, type, sortOrder: 0, active: true, childCount: 0, memberCount: 0, createdAt: '', updatedAt: '',
})

const renderAt = (path: string) => renderWithProviders(
  <Routes><Route path="/admin/organizations/:kind/:id" element={<AdminOrganizationDetailPage />} /></Routes>, { route: path })

// REQ-TEN-007 (C83) and the organization lifecycle actions of REQ-TEN-001 (moved here from "Registrations").
describe('AdminOrganizationDetailPage — organization', () => {
  beforeEach(() => {
    for (const m of [overview, hierarchy, individual, updateOrganization, changeOrganizationLifecycle]) m.mockReset()
    overview.mockResolvedValue(ov())
    hierarchy.mockResolvedValue({
      levels: [{ type: 'ORGANIZATION', label: 'Organization', rank: 0 }, { type: 'DIVISION', label: 'Division', rank: 1 }],
      nodes: [node(1, null, 'Acme', 'ORGANIZATION'), node(2, 1, 'Engineering', 'DIVISION')],
    })
  })

  it('shows the overview with profile completion and what is missing, and the eight tabs', async () => {
    renderAt('/admin/organizations/organization/7')
    expect(await screen.findByRole('heading', { name: 'Acme' })).toBeInTheDocument()
    expect(screen.getAllByText('Profile 67% complete').length).toBeGreaterThan(0)
    expect(screen.getAllByText('Website').length).toBeGreaterThan(0)
    expect(screen.getAllByRole('tab').map((t) => t.textContent)).toEqual(
      ['Overview', 'Structure', 'Members', 'Subscriptions', 'Billing', 'Support', 'Security', 'Activity'])
  })

  it('shows the structure as a read-only org chart', async () => {
    renderAt('/admin/organizations/organization/7?tab=structure')
    expect(await screen.findByRole('article', { name: 'Acme' })).toBeInTheDocument()
    expect(screen.getByRole('article', { name: 'Engineering' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Add node' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Actions for/ })).not.toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Details: Engineering' }))
    expect(await screen.findByRole('heading', { name: 'Engineering' })).toBeInTheDocument()
  })

  it('tells the administrator when the organization has not set up a structure', async () => {
    hierarchy.mockResolvedValue({ levels: [], nodes: [] })
    renderAt('/admin/organizations/organization/7?tab=structure')
    expect(await screen.findByText('This organization has not set up its structure yet.')).toBeInTheDocument()
  })

  it('shows the members and the security settings', async () => {
    renderAt('/admin/organizations/organization/7?tab=members')
    expect(await screen.findByText('Asha Rao')).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('tab', { name: 'Security' }))
    expect(await screen.findByText('Required')).toBeInTheDocument()
  })

  it('edits the company details and leaves the code alone', async () => {
    updateOrganization.mockResolvedValue({ ...acme, name: 'Acme Corp' })
    renderAt('/admin/organizations/organization/7')
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Edit Acme' }))
    const dialog = await screen.findByRole('dialog')
    const name = within(dialog).getByRole('textbox', { name: /^Name/ })
    await user.clear(name)
    await user.type(name, 'Acme Corp')
    await user.click(within(dialog).getByRole('button', { name: 'Save changes' }))
    expect(updateOrganization).toHaveBeenCalledWith(7, expect.objectContaining({ name: 'Acme Corp', businessEmail: 'biz@acme.example' }))
    expect(updateOrganization.mock.calls[0][1]).not.toHaveProperty('code')
    expect(await screen.findByRole('heading', { name: 'Acme Corp' })).toBeInTheDocument()
  })

  it('suspends with a reason and reports how many logins were disabled', async () => {
    changeOrganizationLifecycle.mockResolvedValue({ organization: { ...acme, lifecycleStatus: 'SUSPENDED' }, accountsUpdated: 2, accountsNotUpdated: [] })
    renderAt('/admin/organizations/organization/7')
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Suspend Acme' }))
    const dialog = await screen.findByRole('dialog')
    await user.type(within(dialog).getByRole('textbox', { name: /Reason/ }), 'Unpaid invoice')
    await user.click(within(dialog).getByRole('button', { name: 'Suspend' }))
    expect(changeOrganizationLifecycle).toHaveBeenCalledWith(7, 'suspend', 'Unpaid invoice')
    expect(await screen.findByText(/Acme is suspended\. 2 member login\(s\) disabled\./)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Activate Acme' })).toBeInTheDocument()
  })

  it('opens the dialog chosen in the list row menu and shows the backend refusal', async () => {
    changeOrganizationLifecycle.mockRejectedValue(new ApiError(400, 'You are a member of this organization and cannot suspend or close it yourself.'))
    renderAt('/admin/organizations/organization/7?action=suspend')
    const dialog = await screen.findByRole('dialog')
    await userEvent.setup().click(within(dialog).getByRole('button', { name: 'Suspend' }))
    expect(await within(dialog).findByText(/cannot suspend or close it yourself/)).toBeInTheDocument()
  })

  it('offers Activate, hides Close and disables Edit for a closed organization', async () => {
    overview.mockResolvedValue(ov({ ...acme, lifecycleStatus: 'CLOSED' }))
    renderAt('/admin/organizations/organization/7')
    expect(await screen.findByRole('button', { name: 'Activate Acme' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Close Acme' })).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Edit Acme' })).toBeDisabled()
  })

  it('has no accessibility violations', async () => {
    const { container } = renderAt('/admin/organizations/organization/7')
    await screen.findByRole('heading', { name: 'Acme' })
    expect(await axe(container)).toHaveNoViolations()
  })
})

describe('AdminOrganizationDetailPage — individual', () => {
  it('shows the profile with completion and its five tabs', async () => {
    individual.mockReset()
    individual.mockResolvedValue({
      id: 9, firstName: 'Dina', lastName: 'Rao', email: 'dina@example.com', country: 'India', status: 'EMAIL_VERIFIED', signInLinked: true,
      completion: { percent: 40, missing: ['MOBILE'] }, subscriptions: [], invoices: [], tickets: [],
    })
    renderAt('/admin/organizations/individual/9')
    expect(await screen.findByRole('heading', { name: 'Dina Rao' })).toBeInTheDocument()
    expect(screen.getAllByText('Profile 40% complete').length).toBeGreaterThan(0)
    expect(screen.getAllByRole('tab').map((t) => t.textContent)).toEqual(['Profile', 'Subscriptions', 'Billing', 'Support', 'Activity'])
  })
})
