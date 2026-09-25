import '../../i18n'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { RolesAdminPage } from './RolesAdminPage'

const rolesList = vi.fn()
const rolesCreate = vi.fn()
const rolesUpdate = vi.fn()
const rolesRemove = vi.fn()
const permissionsList = vi.fn()

vi.mock('../../api/rolesApi', () => ({
  rolesApi: {
    list: () => rolesList(),
    create: (p: unknown) => rolesCreate(p),
    update: (id: number, p: unknown) => rolesUpdate(id, p),
    remove: (id: number) => rolesRemove(id),
  },
}))
vi.mock('../../api/permissionsApi', () => ({ permissionsApi: { list: () => permissionsList() } }))

const adminRole = { id: 1, name: 'ADMIN', scope: 'PLATFORM', description: 'Platform administrator', permissionNames: ['MANAGE_CATALOG'], systemManaged: true }
const permissions = [
  { id: 10, name: 'MANAGE_CATALOG', systemManaged: true, roleCount: 1 },
  { id: 11, name: 'VIEW_REPORTS', systemManaged: false, roleCount: 0 },
]

// REQ-IAM-003 acceptance criteria AC-1, AC-2, AC-4, AC-5, AC-10.
describe('RolesAdminPage', () => {
  beforeEach(() => {
    for (const m of [rolesList, rolesCreate, rolesUpdate, rolesRemove, permissionsList]) m.mockReset()
    permissionsList.mockResolvedValue(permissions)
  })

  it('creates a role with the selected permissions', async () => {
    rolesList.mockResolvedValue([adminRole])
    rolesCreate.mockResolvedValue({ id: 2 })
    renderWithProviders(<RolesAdminPage />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'New role' }))
    const dialog = await screen.findByRole('dialog')
    await user.type(within(dialog).getByRole('textbox', { name: /Name/ }), 'AUDITOR')
    await user.click(within(dialog).getByRole('checkbox', { name: 'VIEW_REPORTS' }))
    await user.click(within(dialog).getByRole('button', { name: 'Create' }))

    expect(rolesCreate).toHaveBeenCalledWith({ name: 'AUDITOR', scope: 'PLATFORM', description: '', permissionIds: [11] })
  })

  it('shows the backend message when an organization-scope name is refused', async () => {
    rolesList.mockResolvedValue([])
    rolesCreate.mockRejectedValue(new ApiError(400, 'An organization-scope role name must be one of [ORG_ADMIN, MEMBER]'))
    renderWithProviders(<RolesAdminPage />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'New role' }))
    const dialog = await screen.findByRole('dialog')
    await user.type(within(dialog).getByRole('textbox', { name: /Name/ }), 'SUPERVISOR')
    await user.click(within(dialog).getByRole('combobox', { name: /Scope/ }))
    await user.click(within(await screen.findByRole('listbox')).getByRole('option', { name: 'ORGANIZATION' }))
    await user.click(within(dialog).getByRole('button', { name: 'Create' }))

    expect(await within(dialog).findByText(/must be one of \[ORG_ADMIN, MEMBER\]/)).toBeInTheDocument()
  })

  it('edits only the description and permissions; name and scope are read-only', async () => {
    rolesList.mockResolvedValue([adminRole])
    rolesUpdate.mockResolvedValue(adminRole)
    renderWithProviders(<RolesAdminPage />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Edit ADMIN' }))
    const dialog = await screen.findByRole('dialog')
    expect(within(dialog).getByRole('textbox', { name: /Name/ })).toBeDisabled()
    expect(within(dialog).getByRole('checkbox', { name: 'MANAGE_CATALOG' })).toBeChecked()
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))

    expect(rolesUpdate).toHaveBeenCalledWith(1, { description: 'Platform administrator', permissionIds: [10] })
  })

  it('shows the backend refusal to delete a system-managed role', async () => {
    rolesList.mockResolvedValue([adminRole])
    rolesRemove.mockRejectedValue(new ApiError(403, "The 'ADMIN' role is required by the platform and cannot be deleted"))
    renderWithProviders(<RolesAdminPage />)

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Delete ADMIN' }))
    expect(await screen.findByText(/required by the platform/)).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    rolesList.mockResolvedValue([adminRole])
    const { container } = renderWithProviders(<RolesAdminPage />)
    await screen.findByText('ADMIN')
    expect(await axe(container)).toHaveNoViolations()
  })
})
