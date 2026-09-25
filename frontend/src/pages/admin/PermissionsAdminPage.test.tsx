import '../../i18n'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { PermissionsAdminPage } from './PermissionsAdminPage'

const list = vi.fn()
const create = vi.fn()
const update = vi.fn()
const remove = vi.fn()

vi.mock('../../api/permissionsApi', () => ({
  permissionsApi: {
    list: () => list(),
    create: (p: unknown) => create(p),
    update: (id: number, p: unknown) => update(id, p),
    remove: (id: number) => remove(id),
  },
}))

const granted = { id: 11, name: 'VIEW_REPORTS', description: 'Read reports', systemManaged: false, roleCount: 1 }

// REQ-IAM-003 acceptance criteria AC-6, AC-7, AC-10.
describe('PermissionsAdminPage', () => {
  beforeEach(() => {
    for (const m of [list, create, update, remove]) m.mockReset()
  })

  it('creates a permission', async () => {
    list.mockResolvedValue([])
    create.mockResolvedValue({ id: 12 })
    renderWithProviders(<PermissionsAdminPage />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'New permission' }))
    const dialog = await screen.findByRole('dialog')
    await user.type(within(dialog).getByRole('textbox', { name: /Name/ }), 'EXPORT_DATA')
    await user.type(within(dialog).getByRole('textbox', { name: /Description/ }), 'Export data')
    await user.click(within(dialog).getByRole('button', { name: 'Create' }))

    expect(create).toHaveBeenCalledWith({ name: 'EXPORT_DATA', description: 'Export data' })
  })

  it('edits only the description', async () => {
    list.mockResolvedValue([granted])
    update.mockResolvedValue(granted)
    renderWithProviders(<PermissionsAdminPage />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Edit VIEW_REPORTS' }))
    const dialog = await screen.findByRole('dialog')
    expect(within(dialog).getByRole('textbox', { name: /Name/ })).toBeDisabled()
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    expect(update).toHaveBeenCalledWith(11, { description: 'Read reports' })
  })

  it('shows the backend refusal to delete a permission still granted by a role', async () => {
    list.mockResolvedValue([granted])
    remove.mockRejectedValue(new ApiError(400, 'This permission is still granted by 1 role(s) ([AUDITOR]) — remove it from those roles first.'))
    renderWithProviders(<PermissionsAdminPage />)

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Delete VIEW_REPORTS' }))
    expect(await screen.findByText(/remove it from those roles first/)).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    list.mockResolvedValue([granted])
    const { container } = renderWithProviders(<PermissionsAdminPage />)
    await screen.findByText('VIEW_REPORTS')
    expect(await axe(container)).toHaveNoViolations()
  })
})
