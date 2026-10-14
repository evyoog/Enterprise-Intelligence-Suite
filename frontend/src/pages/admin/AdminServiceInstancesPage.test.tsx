import '../../i18n'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import type { ReporterKey, ServiceInstance } from '../../api/serviceInstancesApi'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminServiceInstancesPage } from './AdminServiceInstancesPage'

const list = vi.fn()
const summary = vi.fn()
const change = vi.fn()
const reporterKeys = vi.fn()
const createReporterKey = vi.fn()
const revokeReporterKey = vi.fn()
const listAdmin = vi.fn()

vi.mock('../../api/serviceInstancesApi', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../../api/serviceInstancesApi')>()
  return {
    ...actual,
    adminServiceInstancesApi: {
      list: (f: unknown) => list(f),
      summary: (p?: number) => summary(p),
      change: (id: number, body: unknown) => change(id, body),
      reporterKeys: () => reporterKeys(),
      createReporterKey: (p: number, l?: string) => createReporterKey(p, l),
      revokeReporterKey: (id: number) => revokeReporterKey(id),
    },
  }
})
vi.mock('../../api/productsApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/productsApi')>('../../api/productsApi')
  return { ...actual, productsApi: { ...actual.productsApi, listAdmin: () => listAdmin() } }
})

const healthy: ServiceInstance = {
  id: 1, subscriptionId: 10, productId: 1, productName: 'Thiran.ai', ownerType: 'ORGANIZATION', ownerOrganizationId: 5, ownerName: 'Acme Corp',
  status: 'ACTIVE', health: 'HEALTHY', lastReportedAt: '2026-10-14T08:00:00Z', stale: false, tenantReference: 'tn-acme', changedBy: 'PRODUCT', createdAt: '2026-10-01T00:00:00Z',
}
const stale: ServiceInstance = {
  id: 2, subscriptionId: 11, productId: 2, productName: 'Valam.ai', ownerType: 'INDIVIDUAL', ownerName: 'Jo Doe',
  status: 'ACTIVE', health: 'DOWN', lastReportedAt: '2026-10-14T07:00:00Z', stale: true, changedBy: 'ADMIN', changeNote: 'Manual check', createdAt: '2026-10-02T00:00:00Z',
}
const activeKey: ReporterKey = { id: 3, productId: 1, productName: 'Thiran.ai', label: 'production', keyPrefix: 'rk_abc123', createdAt: '2026-10-01T00:00:00Z', lastUsedAt: '2026-10-14T08:00:00Z' }
const revokedKey: ReporterKey = { id: 4, productId: 2, productName: 'Valam.ai', keyPrefix: 'rk_old999', createdAt: '2026-09-01T00:00:00Z', revokedAt: '2026-09-20T00:00:00Z' }

describe('AdminServiceInstancesPage (C101)', () => {
  beforeEach(() => {
    for (const m of [list, summary, change, reporterKeys, createReporterKey, revokeReporterKey, listAdmin]) m.mockReset()
    list.mockResolvedValue([healthy, stale])
    summary.mockResolvedValue({ total: 2, healthy: 1, degraded: 0, down: 1, unknown: 0, noRecentReport: 1 })
    reporterKeys.mockResolvedValue([activeKey, revokedKey])
    listAdmin.mockResolvedValue([{ id: 1, name: 'Thiran.ai' }, { id: 2, name: 'Valam.ai' }])
  })

  it('shows the summary, the instances and the reporter keys, and has no accessibility violations', async () => {
    const { container } = renderWithProviders(<AdminServiceInstancesPage />)
    const row = (await screen.findByText('Acme Corp')).closest('tr')!
    expect(within(row).getByText('Thiran.ai')).toBeInTheDocument()
    expect(within(row).getByText('Active')).toBeInTheDocument()
    expect(within(row).getByText('Healthy')).toBeInTheDocument()
    expect(within(row).getByText('Product')).toBeInTheDocument()
    expect(within(row).getByText('tn-acme')).toBeInTheDocument()
    const staleRow = screen.getByText('Jo Doe').closest('tr')!
    expect(within(staleRow).getByText('No recent report')).toBeInTheDocument()
    expect(within(staleRow).queryByText('Down')).not.toBeInTheDocument()
    expect(within(staleRow).getByText('Administrator')).toBeInTheDocument()
    expect(within(staleRow).getByText('Manual check')).toBeInTheDocument()
    expect(screen.getByText('Instances').closest('div')!.parentElement).toHaveTextContent('2')
    expect(screen.getByText(/X-Reporter-Key/)).toBeInTheDocument()
    expect(screen.getByText(/\/product-reports\/health/)).toBeInTheDocument()
    const keyRow = (await screen.findByText('rk_abc123…')).closest('tr')!
    expect(within(keyRow).getByText('production')).toBeInTheDocument()
    expect(within(keyRow).getByRole('button', { name: /Revoke/ })).toBeInTheDocument()
    const revokedRow = screen.getByText('rk_old999…').closest('tr')!
    expect(within(revokedRow).queryByRole('button', { name: /Revoke/ })).not.toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('filters by product, status and health', async () => {
    renderWithProviders(<AdminServiceInstancesPage />)
    await screen.findByText('Acme Corp')
    const user = userEvent.setup()
    await user.click(screen.getByRole('combobox', { name: 'Product' }))
    await user.click(within(await screen.findByRole('listbox')).getByRole('option', { name: 'Valam.ai' }))
    await waitFor(() => expect(list).toHaveBeenLastCalledWith({ productId: 2, status: undefined, health: undefined }))
    expect(summary).toHaveBeenLastCalledWith(2)
    await user.click(screen.getByRole('combobox', { name: 'Health' }))
    await user.click(within(await screen.findByRole('listbox')).getByRole('option', { name: 'Down' }))
    await waitFor(() => expect(list).toHaveBeenLastCalledWith({ productId: 2, status: undefined, health: 'DOWN' }))
  })

  it('shows an empty state and an error with retry', async () => {
    list.mockResolvedValueOnce([])
    const { unmount } = renderWithProviders(<AdminServiceInstancesPage />)
    expect(await screen.findByText('No service instances')).toBeInTheDocument()
    unmount()
    list.mockRejectedValueOnce(new ApiError(500, 'Boom'))
    renderWithProviders(<AdminServiceInstancesPage />)
    expect(await screen.findByText(/Boom/)).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Retry' }))
    expect(await screen.findByText('Acme Corp')).toBeInTheDocument()
  })

  it('requires a note before changing an instance by hand, then sends only what changed', async () => {
    change.mockResolvedValue({ ...healthy, health: 'DEGRADED', changedBy: 'ADMIN' })
    renderWithProviders(<AdminServiceInstancesPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: /Change Thiran.ai \(Acme Corp\) by hand/ }))
    const dialog = await screen.findByRole('dialog', { name: 'Change Thiran.ai by hand' })
    expect(await axe(dialog)).toHaveNoViolations()
    await user.click(within(dialog).getByRole('combobox', { name: 'Health' }))
    await user.click(within(await screen.findByRole('listbox')).getByRole('option', { name: 'Degraded' }))
    await user.click(within(dialog).getByRole('button', { name: 'Save change' }))
    expect(within(dialog).getByText('Enter a note.')).toBeInTheDocument()
    expect(change).not.toHaveBeenCalled()
    await user.type(within(dialog).getByRole('textbox', { name: /Note/ }), 'Investigating slow responses')
    await user.click(within(dialog).getByRole('button', { name: 'Save change' }))
    await waitFor(() => expect(change).toHaveBeenCalledTimes(1))
    expect(change).toHaveBeenCalledWith(1, { status: undefined, health: 'DEGRADED', note: 'Investigating slow responses' })
    expect(await screen.findByText('Instance updated.')).toBeInTheDocument()
    expect(list).toHaveBeenCalledTimes(2)
  })

  it('asks for a change when nothing was changed and shows what the server refuses', async () => {
    change.mockRejectedValue(new ApiError(400, 'An instance cannot be set to this status.'))
    renderWithProviders(<AdminServiceInstancesPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: /Change Thiran.ai/ }))
    const dialog = await screen.findByRole('dialog')
    await user.type(within(dialog).getByRole('textbox', { name: /Note/ }), 'Because')
    await user.click(within(dialog).getByRole('button', { name: 'Save change' }))
    expect(within(dialog).getByText('Change the status or the health first.')).toBeInTheDocument()
    expect(change).not.toHaveBeenCalled()
    await user.click(within(dialog).getByRole('combobox', { name: 'Status' }))
    await user.click(within(await screen.findByRole('listbox')).getByRole('option', { name: 'Suspended' }))
    await user.click(within(dialog).getByRole('button', { name: 'Save change' }))
    expect(await within(dialog).findByText('An instance cannot be set to this status.')).toBeInTheDocument()
  })

  it('creates a reporter key and shows the secret once, with a copy button', async () => {
    createReporterKey.mockResolvedValue({ key: { ...activeKey, id: 9, keyPrefix: 'rk_new' }, secret: 'rk_new_SECRET_VALUE' })
    const writeText = vi.fn().mockResolvedValue(undefined)
    const user = userEvent.setup()
    Object.defineProperty(navigator, 'clipboard', { value: { writeText }, configurable: true })
    renderWithProviders(<AdminServiceInstancesPage />)
    await user.click(await screen.findByRole('button', { name: 'Create key' }))
    const dialog = await screen.findByRole('dialog', { name: 'Create a reporter key' })
    await user.click(within(dialog).getByRole('button', { name: 'Create key' }))
    expect(within(dialog).getByText('Choose a product.')).toBeInTheDocument()
    expect(createReporterKey).not.toHaveBeenCalled()
    await user.click(within(dialog).getByRole('combobox', { name: /Product/ }))
    await user.click(within(await screen.findByRole('listbox')).getByRole('option', { name: 'Thiran.ai' }))
    await user.type(within(dialog).getByRole('textbox', { name: /Label/ }), ' staging ')
    await user.click(within(dialog).getByRole('button', { name: 'Create key' }))
    await waitFor(() => expect(createReporterKey).toHaveBeenCalledWith(1, ' staging '))

    const secretDialog = await screen.findByRole('dialog', { name: 'Reporter key for Thiran.ai' })
    expect(within(secretDialog).getByText('Copy it now. It will not be shown again.')).toBeInTheDocument()
    expect(within(secretDialog).getByRole('textbox', { name: 'Reporter key secret' })).toHaveValue('rk_new_SECRET_VALUE')
    expect(await axe(secretDialog)).toHaveNoViolations()
    await user.click(within(secretDialog).getByRole('button', { name: 'Copy' }))
    await waitFor(() => expect(writeText).toHaveBeenCalledWith('rk_new_SECRET_VALUE'))
    expect(await within(secretDialog).findByText('Copied to the clipboard.')).toBeInTheDocument()
    expect(reporterKeys).toHaveBeenCalledTimes(2)

    await user.click(within(secretDialog).getByRole('button', { name: 'I have copied it' }))
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
    expect(screen.queryByText('rk_new_SECRET_VALUE')).not.toBeInTheDocument()
    expect(screen.queryByDisplayValue('rk_new_SECRET_VALUE')).not.toBeInTheDocument()
  })

  it('revokes a key only after confirmation', async () => {
    revokeReporterKey.mockResolvedValue({ ...activeKey, revokedAt: '2026-10-14T09:00:00Z' })
    renderWithProviders(<AdminServiceInstancesPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Revoke the Thiran.ai key rk_abc123' }))
    const dialog = await screen.findByRole('dialog', { name: 'Revoke the Thiran.ai key?' })
    await user.click(within(dialog).getByRole('button', { name: 'Cancel' }))
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
    expect(revokeReporterKey).not.toHaveBeenCalled()
    await user.click(screen.getByRole('button', { name: 'Revoke the Thiran.ai key rk_abc123' }))
    await user.click(within(await screen.findByRole('dialog')).getByRole('button', { name: 'Revoke' }))
    await waitFor(() => expect(revokeReporterKey).toHaveBeenCalledWith(3))
    expect(await screen.findByText('Key for Thiran.ai revoked.')).toBeInTheDocument()
  })
})
