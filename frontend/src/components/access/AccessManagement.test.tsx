import '../../i18n'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AccessManagementPage } from '../../pages/AccessManagementPage'

const members = vi.fn()
const member = vi.fn()
const setOverrides = vi.fn()
const resetAll = vi.fn()
const roleDefaults = vi.fn()
const saveRoleDefaults = vi.fn()
const bulk = vi.fn()
const permissions = vi.fn()
const activity = vi.fn()
const reviewMemberAccess = vi.fn()
const changeMemberRole = vi.fn()
const listForOrganization = vi.fn()
const approveRequest = vi.fn()
const rejectRequest = vi.fn()

vi.mock('../../api/accessApi', () => ({
  accessApi: {
    members: (f: unknown) => members(f),
    member: (id: number) => member(id),
    setOverrides: (id: number, b: unknown) => setOverrides(id, b),
    resetAll: (id: number) => resetAll(id),
    roleDefaults: (r: string) => roleDefaults(r),
    saveRoleDefaults: (r: string, b: unknown, p: boolean) => saveRoleDefaults(r, b, p),
    bulk: (b: unknown) => bulk(b),
    permissions: () => permissions(),
    activity: (p: number, s: number) => activity(p, s),
  },
}))
vi.mock('../../api/registrationApi', () => ({
  organizationApi: { reviewMemberAccess: (id: number) => reviewMemberAccess(id), changeMemberRole: (id: number, r: string) => changeMemberRole(id, r) },
}))
vi.mock('../../api/accessRequestApi', () => ({
  accessRequestApi: {
    listForOrganization: () => listForOrganization(),
    approve: (id: number) => approveRequest(id),
    reject: (id: number, note: string) => rejectRequest(id, note),
  },
}))

const ada = { memberId: 7, customerId: 70, name: 'Ada Lovelace', email: 'ada@example.com', role: 'ORG_ADMIN', status: 'ACTIVE', productsOn: 2, productsEntitled: 2, customAccessCount: 0, lastReviewedAt: null, lastReviewedBy: null, canChange: false, lockedReason: 'You cannot change your own access.' }
const bob = { memberId: 8, customerId: 80, name: 'Bob Byte', email: 'bob@example.com', role: 'MEMBER', status: 'ACTIVE', productsOn: 1, productsEntitled: 2, customAccessCount: 2, lastReviewedAt: '2026-09-30T10:00:00Z', lastReviewedBy: 'Ada Lovelace', canChange: true, lockedReason: null }
const cy = { memberId: 9, customerId: 90, name: 'Cy Core', email: 'cy@example.com', role: 'MEMBER', status: 'ACTIVE', productsOn: 2, productsEntitled: 2, customAccessCount: 0, lastReviewedAt: null, lastReviewedBy: null, canChange: true, lockedReason: null }
const page = { items: [ada, bob, cy], totalElements: 3, page: 0, size: 20 }

const bobDetail = {
  memberId: 8, name: 'Bob Byte', email: 'bob@example.com', role: 'MEMBER', status: 'ACTIVE', lastReviewedAt: '2026-09-30T10:00:00Z', lastReviewedBy: 'Ada Lovelace',
  canChange: true, memberLockedReason: null,
  products: [
    { productId: 3, name: 'Valam.ai', plan: 'Standard', enabled: true, wanted: true, source: 'ROLE', productRole: 'USER', seats: { inUse: 2, quantity: 5 }, lockedReason: null },
    { productId: 4, name: 'Varthan.ai', plan: null, enabled: false, wanted: true, source: 'ROLE', productRole: null, seats: { inUse: 1, quantity: 1 }, lockedReason: 'No free seats on Varthan.ai.' },
  ],
  permissions: [
    { code: 'MANAGE_ORDERS', name: 'Approve orders', description: 'Approve or reject members\' orders', area: 'ORDERS', enabled: false, source: 'ROLE', lockedReason: null },
    { code: 'MANAGE_SUBSCRIPTIONS', name: 'Manage subscriptions', description: 'Suspend, cancel and change plans', area: 'SUBSCRIPTIONS', enabled: true, source: 'OVERRIDE', lockedReason: null },
    { code: 'MANAGE_ACCESS', name: 'Manage access', description: 'Manage access', area: 'MEMBERS_ACCESS', enabled: false, source: 'ROLE', lockedReason: 'You do not hold this permission, so you cannot grant or remove it.' },
  ],
}

const catalogue = [
  { code: 'MANAGE_ORDERS', name: 'Approve orders', description: '', area: 'ORDERS', callerCanGrant: true },
  { code: 'MANAGE_ACCESS', name: 'Manage access', description: '', area: 'MEMBERS_ACCESS', callerCanGrant: false },
]
const memberDefaults = {
  role: 'MEMBER', memberCount: 2,
  permissions: [{ code: 'MANAGE_ORDERS', name: 'Approve orders', description: 'Approve or reject members\' orders', area: 'ORDERS', enabled: false, locked: false, customized: false }],
  products: [{ productId: 3, name: 'Valam.ai', enabled: true, customized: false }],
}

// REQ-TEN-005 Access management (C90).
describe('AccessManagementPage', () => {
  beforeEach(() => {
    ;[members, member, setOverrides, resetAll, roleDefaults, saveRoleDefaults, bulk, permissions, activity, reviewMemberAccess, changeMemberRole, listForOrganization, approveRequest, rejectRequest].forEach((m) => m.mockReset())
    members.mockResolvedValue(page)
    permissions.mockResolvedValue(catalogue)
    roleDefaults.mockResolvedValue(memberDefaults)
    member.mockResolvedValue(bobDetail)
    window.confirm = vi.fn(() => true)
  })

  it('lists members with their products, custom access and last review, and has no accessibility violations', async () => {
    const { container } = renderWithProviders(<AccessManagementPage />)
    expect(await screen.findByText('Bob Byte')).toBeInTheDocument()
    const row = screen.getByText('Bob Byte').closest('tr') as HTMLElement
    expect(within(row).getByText('1 of 2')).toBeInTheDocument()
    expect(within(row).getByText('2 custom')).toBeInTheDocument()
    expect(within(row).getByText('Member')).toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('filters members by search text and by custom access', async () => {
    const user = userEvent.setup()
    renderWithProviders(<AccessManagementPage />)
    await screen.findByText('Bob Byte')

    await user.click(screen.getByRole('button', { name: 'Has custom access' }))
    await waitFor(() => expect(members).toHaveBeenLastCalledWith(expect.objectContaining({ custom: true })))
    await user.type(screen.getByLabelText('Search name or email'), 'bob')
    await waitFor(() => expect(members).toHaveBeenLastCalledWith(expect.objectContaining({ q: 'bob', custom: true })))
  })

  it('stages a product and a permission change in the member panel and saves them together', async () => {
    const user = userEvent.setup()
    setOverrides.mockResolvedValue({ ...bobDetail, permissions: [{ ...bobDetail.permissions[0], enabled: true, source: 'OVERRIDE' }, ...bobDetail.permissions.slice(1)] })
    const { baseElement } = renderWithProviders(<AccessManagementPage />)
    await screen.findByText('Bob Byte')
    await user.click(screen.getByRole('button', { name: 'Manage access for Bob Byte' }))

    const panel = await screen.findByRole('region', { name: 'Bob Byte' })
    expect(within(panel).getByText(/Seats 2\/5/)).toBeInTheDocument()
    expect(within(panel).getByText('No free seats on Varthan.ai.')).toBeInTheDocument()
    expect(within(panel).getByRole('switch', { name: 'Varthan.ai' })).toBeDisabled()
    expect(within(panel).getByRole('switch', { name: 'Manage access' })).toBeDisabled()
    expect(within(panel).getByText('No changes')).toBeInTheDocument()

    await user.click(within(panel).getByRole('switch', { name: 'Valam.ai' }))
    await user.click(within(panel).getByRole('switch', { name: 'Approve orders' }))
    expect(within(panel).getByText('2 unsaved changes')).toBeInTheDocument()
    expect(await axe(baseElement)).toHaveNoViolations()

    await user.click(within(panel).getByRole('button', { name: 'Save changes' }))
    await waitFor(() => expect(setOverrides).toHaveBeenCalledWith(8, { permissions: { MANAGE_ORDERS: true }, products: { 3: false }, reset: [] }))
    expect(await within(panel).findByText('Access updated for Bob Byte')).toBeInTheDocument()
  })

  it('stages a reset to the role default for an individual override', async () => {
    const user = userEvent.setup()
    setOverrides.mockResolvedValue(bobDetail)
    renderWithProviders(<AccessManagementPage />)
    await user.click(await screen.findByRole('button', { name: 'Manage access for Bob Byte' }))
    const panel = await screen.findByRole('region', { name: 'Bob Byte' })

    await user.click(within(panel).getByRole('button', { name: 'Reset Manage subscriptions to the role default' }))
    expect(within(panel).getByText('Will reset')).toBeInTheDocument()
    await user.click(within(panel).getByRole('button', { name: 'Save changes' }))
    await waitFor(() => expect(setOverrides).toHaveBeenCalledWith(8, { permissions: {}, products: {}, reset: ['permission:MANAGE_SUBSCRIPTIONS'] }))
  })

  it('shows a read-only panel with the reason when the member cannot be changed', async () => {
    const user = userEvent.setup()
    member.mockResolvedValue({ ...bobDetail, name: 'Ada Lovelace', role: 'ORG_ADMIN', canChange: false, memberLockedReason: 'You cannot change your own access.' })
    renderWithProviders(<AccessManagementPage />)
    await user.click(await screen.findByRole('button', { name: 'Manage access for Ada Lovelace' }))
    const panel = await screen.findByRole('region', { name: 'Ada Lovelace' })

    expect(within(panel).getAllByText('You cannot change your own access.').length).toBeGreaterThan(0)
    expect(within(panel).getByRole('switch', { name: 'Valam.ai' })).toBeDisabled()
    expect(within(panel).queryByRole('button', { name: 'Save changes' })).not.toBeInTheDocument()
  })

  it('shows a backend refusal on the panel', async () => {
    const user = userEvent.setup()
    setOverrides.mockRejectedValue(new ApiError(409, 'No free seats on Valam.ai. Add seats to the subscription.'))
    renderWithProviders(<AccessManagementPage />)
    await user.click(await screen.findByRole('button', { name: 'Manage access for Bob Byte' }))
    const panel = await screen.findByRole('region', { name: 'Bob Byte' })
    await user.click(within(panel).getByRole('switch', { name: 'Approve orders' }))
    await user.click(within(panel).getByRole('button', { name: 'Save changes' }))
    expect(await within(panel).findByText(/No free seats on Valam.ai/)).toBeInTheDocument()
  })

  it('previews and applies a bulk change, listing who was skipped and why', async () => {
    const user = userEvent.setup()
    bulk.mockResolvedValueOnce({ preview: true, item: 'product:3', itemName: 'Valam.ai', action: 'GRANT', applied: 1, skipped: [{ memberId: 9, name: 'Cy Core', reason: 'No free seats on Valam.ai.' }] })
    bulk.mockResolvedValueOnce({ preview: false, item: 'product:3', itemName: 'Valam.ai', action: 'GRANT', applied: 1, skipped: [{ memberId: 9, name: 'Cy Core', reason: 'No free seats on Valam.ai.' }] })
    const { baseElement } = renderWithProviders(<AccessManagementPage />)
    await screen.findByText('Bob Byte')
    await user.click(screen.getByRole('checkbox', { name: 'Select Bob Byte' }))
    await user.click(screen.getByRole('checkbox', { name: 'Select Cy Core' }))
    expect(screen.getByRole('checkbox', { name: 'Select Ada Lovelace' })).toBeDisabled()
    await user.click(screen.getByRole('button', { name: 'Bulk change (2)' }))

    const dialog = await screen.findByRole('dialog')
    await user.click(within(dialog).getByRole('combobox', { name: 'Product or permission' }))
    await user.click(await screen.findByRole('option', { name: 'Product: Valam.ai' }))
    await user.click(within(dialog).getByRole('button', { name: 'Preview' }))
    expect(await within(dialog).findByText(/1 members would change, 1 skipped/)).toBeInTheDocument()
    expect(within(dialog).getByText('No free seats on Valam.ai.')).toBeInTheDocument()
    expect(bulk).toHaveBeenLastCalledWith({ action: 'GRANT', item: 'product:3', memberIds: [8, 9], preview: true })
    expect(await axe(baseElement)).toHaveNoViolations()

    await user.click(within(dialog).getByRole('button', { name: 'Apply to members' }))
    expect(await within(dialog).findByText(/1 members changed, 1 skipped/)).toBeInTheDocument()
    expect(bulk).toHaveBeenLastCalledWith({ action: 'GRANT', item: 'product:3', memberIds: [8, 9], preview: false })
  })

  it('edits role defaults, shows the impact first and then applies', async () => {
    const user = userEvent.setup()
    saveRoleDefaults.mockResolvedValueOnce({ preview: true, affectedMembers: 2, skippedNoSeat: 0, items: [{ item: 'permission:MANAGE_ORDERS', name: 'Approve orders', from: false, to: true, affectedMembers: 2 }], defaults: memberDefaults })
    saveRoleDefaults.mockResolvedValueOnce({ preview: false, affectedMembers: 2, skippedNoSeat: 0, items: [], defaults: { ...memberDefaults, permissions: [{ ...memberDefaults.permissions[0], enabled: true, customized: true }] } })
    renderWithProviders(<AccessManagementPage />, { route: '/?tab=roles' })

    await user.click(await screen.findByRole('switch', { name: 'Approve orders' }))
    await user.click(screen.getByRole('button', { name: 'Review changes' }))
    const dialog = await screen.findByRole('dialog')
    expect(within(dialog).getByText(/changes access for 2 members/)).toBeInTheDocument()
    expect(within(dialog).getByText('Approve orders: Off → On (2 members)')).toBeInTheDocument()
    expect(saveRoleDefaults).toHaveBeenLastCalledWith('MEMBER', { permissions: { MANAGE_ORDERS: true }, products: {} }, true)

    await user.click(within(dialog).getByRole('button', { name: 'Apply' }))
    expect(await screen.findByText('Role defaults saved. 2 members affected.')).toBeInTheDocument()
    expect(saveRoleDefaults).toHaveBeenLastCalledWith('MEMBER', { permissions: { MANAGE_ORDERS: true }, products: {} }, false)
  })

  it('locks every permission for the Organization admin role', async () => {
    const user = userEvent.setup()
    roleDefaults.mockImplementation((role: string) => Promise.resolve(role === 'ORG_ADMIN'
      ? { ...memberDefaults, role: 'ORG_ADMIN', permissions: [{ ...memberDefaults.permissions[0], enabled: true, locked: true }] }
      : memberDefaults))
    renderWithProviders(<AccessManagementPage />, { route: '/?tab=roles' })
    await screen.findByRole('switch', { name: 'Approve orders' })
    await user.click(screen.getByRole('button', { name: 'Organization admin' }))
    expect(await screen.findByText('Organization admins always have every feature permission.')).toBeInTheDocument()
    expect(screen.getByRole('switch', { name: 'Approve orders' })).toBeDisabled()
  })

  it('lists the access changes in the Activity tab', async () => {
    activity.mockResolvedValue({ items: [{ id: 1, timestamp: '2026-10-12T09:00:00Z', action: 'ACCESS_PERMISSION_CHANGED', actorEmail: 'ada@example.com', outcome: 'SUCCESS', detail: 'Member 8: Approve orders off -> on' }], totalElements: 1, page: 0, size: 20 })
    const { container } = renderWithProviders(<AccessManagementPage />, { route: '/?tab=activity' })
    expect(await screen.findByText('Permission changed')).toBeInTheDocument()
    expect(screen.getByText('Member 8: Approve orders off -> on')).toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('shows the backend message when the members cannot be loaded', async () => {
    members.mockRejectedValue(new ApiError(403, 'You do not have permission to do this'))
    renderWithProviders(<AccessManagementPage />)
    expect(await screen.findByText('You do not have permission to do this')).toBeInTheDocument()
  })

  const pendingRequest = {
    id: 1, productId: 3, productName: 'Valam.ai', memberId: 9, memberName: 'Cy Core',
    status: 'PENDING' as const, message: 'Need it for the Q1 launch', createdAt: '2027-01-05T00:00:00Z',
  }

  it('lists access requests and approves one', async () => {
    const user = userEvent.setup()
    listForOrganization.mockResolvedValue([pendingRequest])
    approveRequest.mockResolvedValue({ ...pendingRequest, status: 'APPROVED', decidedByName: 'Ada Lovelace', decidedAt: '2027-01-06T00:00:00Z' })
    const { container } = renderWithProviders(<AccessManagementPage />, { route: '/?tab=requests' })

    expect(await screen.findByText('Cy Core')).toBeInTheDocument()
    expect(screen.getByText('Need it for the Q1 launch')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Approve' }))
    expect(approveRequest).toHaveBeenCalledWith(1)
    expect(await axe(container)).toHaveNoViolations()
  })

  it('requires a note to reject an access request', async () => {
    const user = userEvent.setup()
    listForOrganization.mockResolvedValue([pendingRequest])
    rejectRequest.mockResolvedValue({ ...pendingRequest, status: 'REJECTED', decisionNote: 'No seats left' })
    renderWithProviders(<AccessManagementPage />, { route: '/?tab=requests' })

    await user.click(await screen.findByRole('button', { name: 'Reject' }))
    const dialog = await screen.findByRole('dialog')
    await user.click(within(dialog).getByRole('button', { name: 'Reject' }))
    expect(rejectRequest).not.toHaveBeenCalled()

    await user.type(within(dialog).getByRole('textbox', { name: 'Reason' }), 'No seats left')
    await user.click(within(dialog).getByRole('button', { name: 'Reject' }))
    expect(rejectRequest).toHaveBeenCalledWith(1, 'No seats left')
  })

  it('shows an empty state when there are no access requests', async () => {
    listForOrganization.mockResolvedValue([])
    renderWithProviders(<AccessManagementPage />, { route: '/?tab=requests' })
    expect(await screen.findByText('No access requests')).toBeInTheDocument()
  })
})
