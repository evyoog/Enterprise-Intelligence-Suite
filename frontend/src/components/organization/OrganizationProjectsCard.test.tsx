import '../../i18n'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { OrganizationProjectsCard } from './OrganizationProjectsCard'

const list = vi.fn()
const create = vi.fn()
const update = vi.fn()
const remove = vi.fn()
const addMember = vi.fn()
const removeMember = vi.fn()
const setProduct = vi.fn()
const removeProduct = vi.fn()
const listMyOrgUsers = vi.fn()
const listMyOrgProducts = vi.fn()

vi.mock('../../api/projectsApi', () => ({
  projectsApi: {
    list: () => list(),
    create: (n: string, d?: string) => create(n, d),
    update: (id: number, n: string, d?: string) => update(id, n, d),
    remove: (id: number) => remove(id),
    addMember: (id: number, m: number) => addMember(id, m),
    removeMember: (id: number, m: number) => removeMember(id, m),
    setProduct: (id: number, p: number, r: string) => setProduct(id, p, r),
    removeProduct: (id: number, p: number) => removeProduct(id, p),
  },
}))
vi.mock('../../api/registrationApi', () => ({
  organizationApi: { listMyOrgUsers: () => listMyOrgUsers(), listMyOrgProducts: () => listMyOrgProducts() },
}))

const ada = { organizationMemberId: 7, customerId: 70, firstName: 'Ada', lastName: 'Lovelace', email: 'ada@example.com', orgRole: 'ORG_ADMIN', status: 'ACTIVE' }
const bob = { organizationMemberId: 8, customerId: 80, firstName: 'Bob', lastName: 'Byte', email: 'bob@example.com', orgRole: 'MEMBER', status: 'ACTIVE' }
const valam = { productId: 3, productName: 'Valam.ai', orgSubscriptionStatus: 'ACTIVE', myAccessAssigned: false }
const lapsed = { productId: 4, productName: 'Lapsed', orgSubscriptionStatus: 'CANCELLED', myAccessAssigned: false }
const empty = { id: 1, name: 'Rollout', description: 'First wave', createdAt: '2026-10-12T00:00:00Z', members: [], products: [] }

// 05.04.02 Projects (REQ-TEN-009, C88).
describe('OrganizationProjectsCard', () => {
  beforeEach(() => {
    ;[list, create, update, remove, addMember, removeMember, setProduct, removeProduct, listMyOrgUsers, listMyOrgProducts].forEach((m) => m.mockReset())
    listMyOrgUsers.mockResolvedValue([ada, bob])
    listMyOrgProducts.mockResolvedValue([valam, lapsed])
  })

  it('creates a project with a description', async () => {
    const user = userEvent.setup()
    list.mockResolvedValue([])
    create.mockResolvedValue(empty)
    renderWithProviders(<OrganizationProjectsCard canManageUsers canManageProducts />)

    await screen.findByText(/No projects yet/)
    await user.type(screen.getByLabelText('Project name'), 'Rollout')
    await user.type(screen.getByLabelText('Description (optional)'), 'First wave')
    await user.click(screen.getByRole('button', { name: 'Create project' }))

    expect(create).toHaveBeenCalledWith('Rollout', 'First wave')
    expect(await screen.findByText('Rollout')).toBeInTheDocument()
    expect(screen.getByText('First wave')).toBeInTheDocument()
  })

  it('adds a member and a subscribed product with a role, and offers no product without a subscription', async () => {
    const user = userEvent.setup()
    list.mockResolvedValue([empty])
    addMember.mockResolvedValue({ ...empty, members: [{ memberId: 8, customerId: 80, name: 'Bob Byte', email: 'bob@example.com', orgRole: 'MEMBER' }] })
    setProduct.mockResolvedValue({ ...empty, products: [{ productId: 3, productName: 'Valam.ai', productRole: 'PMS_USER' }] })
    renderWithProviders(<OrganizationProjectsCard canManageUsers canManageProducts />)
    const card = await screen.findByRole('article', { name: 'Rollout' })

    await user.click(within(card).getByLabelText('Add member · Rollout'))
    await user.click(await screen.findByRole('option', { name: 'Bob Byte' }))
    await user.click(within(card).getAllByRole('button', { name: 'Add' })[0])
    expect(addMember).toHaveBeenCalledWith(1, 8)
    expect(await within(card).findByText('Bob Byte')).toBeInTheDocument()

    await user.click(within(card).getByLabelText('Add product · Rollout'))
    expect(screen.queryByRole('option', { name: 'Lapsed' })).not.toBeInTheDocument()
    await user.click(await screen.findByRole('option', { name: 'Valam.ai' }))
    await user.type(within(card).getByLabelText('Role · Rollout'), 'PMS_USER')
    await user.click(within(card).getAllByRole('button', { name: 'Add' })[1])
    expect(setProduct).toHaveBeenCalledWith(1, 3, 'PMS_USER')
    expect(await within(card).findByText('Valam.ai · PMS_USER')).toBeInTheDocument()
  })

  it('removes a member and a product and deletes the project', async () => {
    const user = userEvent.setup()
    const full = { ...empty, members: [{ memberId: 8, customerId: 80, name: 'Bob Byte', email: 'bob@example.com', orgRole: 'MEMBER' }], products: [{ productId: 3, productName: 'Valam.ai', productRole: 'PMS_USER' }] }
    list.mockResolvedValue([full])
    removeMember.mockResolvedValue({ ...full, members: [] })
    removeProduct.mockResolvedValue({ ...full, members: [], products: [] })
    remove.mockResolvedValue(undefined)
    renderWithProviders(<OrganizationProjectsCard canManageUsers canManageProducts />)
    const card = await screen.findByRole('article', { name: 'Rollout' })

    await user.click(within(card).getByLabelText('Remove Bob Byte'))
    await waitFor(() => expect(removeMember).toHaveBeenCalledWith(1, 8))
    await user.click(await within(card).findByLabelText('Remove Valam.ai'))
    await waitFor(() => expect(removeProduct).toHaveBeenCalledWith(1, 3))
    await user.click(within(card).getByRole('button', { name: 'Delete Rollout' }))
    await waitFor(() => expect(remove).toHaveBeenCalledWith(1))
    await waitFor(() => expect(screen.queryByRole('article', { name: 'Rollout' })).not.toBeInTheDocument())
  })

  it('renames a project and shows a server message', async () => {
    const user = userEvent.setup()
    list.mockResolvedValue([empty])
    update.mockRejectedValue(new ApiError(409, 'A project with this name already exists.'))
    renderWithProviders(<OrganizationProjectsCard canManageUsers canManageProducts />)
    await screen.findByRole('article', { name: 'Rollout' })

    await user.click(screen.getByRole('button', { name: 'Edit Rollout' }))
    const name = within(screen.getByRole('article', { name: 'Rollout' })).getByLabelText('Project name')
    await user.clear(name)
    await user.type(name, 'Delhi')
    await user.click(screen.getByRole('button', { name: 'Save' }))
    expect(update).toHaveBeenCalledWith(1, 'Delhi', 'First wave')
    expect(await screen.findByText('A project with this name already exists.')).toBeInTheDocument()
  })

  it('is read-only for people when the caller may only manage products, and hides on a 403', async () => {
    list.mockResolvedValue([{ ...empty, members: [{ memberId: 8, customerId: 80, name: 'Bob Byte', orgRole: 'MEMBER' }] }])
    const { unmount } = renderWithProviders(<OrganizationProjectsCard canManageUsers={false} canManageProducts />)
    const card = await screen.findByRole('article', { name: 'Rollout' })
    expect(screen.queryByRole('button', { name: 'Create project' })).not.toBeInTheDocument()
    expect(within(card).queryByLabelText('Remove Bob Byte')).not.toBeInTheDocument()
    expect(within(card).getByLabelText('Add product · Rollout')).toBeInTheDocument()
    unmount()

    list.mockRejectedValue(new ApiError(403, 'no'))
    const { container } = renderWithProviders(<OrganizationProjectsCard canManageUsers canManageProducts />)
    await waitFor(() => expect(container).toBeEmptyDOMElement())
  })

  it('has no accessibility violations', async () => {
    list.mockResolvedValue([{ ...empty, members: [{ memberId: 8, customerId: 80, name: 'Bob Byte', orgRole: 'MEMBER' }], products: [{ productId: 3, productName: 'Valam.ai', productRole: 'PMS_USER' }] }])
    const { container } = renderWithProviders(<OrganizationProjectsCard canManageUsers canManageProducts />)
    await screen.findByRole('article', { name: 'Rollout' })
    expect(await axe(container)).toHaveNoViolations()
  })
})
