import '../i18n'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { renderWithProviders } from '../test/renderWithProviders'
import { ServiceStatusPage } from './ServiceStatusPage'
import { ServiceStatusAdminPage } from './admin/ServiceStatusAdminPage'

const get = vi.fn()
const adminGet = vi.fn()
const updateStatus = vi.fn()

vi.mock('../api/serviceStatusApi', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../api/serviceStatusApi')>()
  return {
    ...actual,
    serviceStatusApi: { get: () => get() },
    adminServiceStatusApi: {
      get: () => adminGet(),
      updateStatus: (id: number, s: string, n?: string) => updateStatus(id, s, n),
    },
  }
})

const products = [
  { productId: 1, productName: 'Thiran.ai', status: 'DEGRADED', note: 'Slow exports', updatedAt: '2026-09-26T08:00:00Z', purchased: true, openIncidents: 1 },
  { productId: 2, productName: 'Valam.ai', status: 'MAJOR_OUTAGE', note: null, updatedAt: null, purchased: false, openIncidents: 2 },
  { productId: 3, productName: 'Yukth.ai', status: 'OPERATIONAL', note: null, updatedAt: null, purchased: true, openIncidents: 0 },
]
const incident = { id: 7, productId: 1, productName: 'Thiran.ai', title: 'Slow report exports', message: 'Up to 10 minutes.', startedAt: '2026-09-26T08:15:00Z', endedAt: null, open: true }

// REQ-PRT-001 acceptance criteria (UI), decisions C20/C26.
describe('ServiceStatusPage (customer)', () => {
  beforeEach(() => get.mockReset())

  it('shows every product status, and incident details only for purchased products', async () => {
    get.mockResolvedValue({ enabled: true, products, incidents: [incident] })
    renderWithProviders(<ServiceStatusPage />)

    const table = await screen.findByRole('table', { name: 'Product status' })
    expect(within(table).getByText('Degraded')).toBeInTheDocument()
    expect(within(table).getByText('Major outage')).toBeInTheDocument()
    expect(within(table).getByText(/1 open incident/)).toBeInTheDocument()
    expect(within(table).getByText('Incident details are shown for products your organization has purchased.')).toBeInTheDocument()
    expect(screen.getByText('Thiran.ai — Slow report exports')).toBeInTheDocument()
    expect(screen.queryByText(/Valam.ai —/)).not.toBeInTheDocument()
  })

  it('shows how a status was worked out when it is based on instance reports', async () => {
    get.mockResolvedValue({
      enabled: true, incidents: [],
      products: [{ ...products[0], source: 'INSTANCES', reportingInstances: 3, downInstances: 1, degradedInstances: 0 },
        { ...products[2], source: 'INSTANCES', reportingInstances: 1, downInstances: 0, degradedInstances: 0 }, products[1]],
    })
    renderWithProviders(<ServiceStatusPage />)
    expect(await screen.findByText('Based on 3 reporting instances (1 down, 0 degraded)')).toBeInTheDocument()
    expect(screen.getByText('Based on 1 reporting instance (0 down, 0 degraded)')).toBeInTheDocument()
    expect(screen.getAllByText(/Based on/)).toHaveLength(2)
  })

  it('says so when the page is turned off', async () => {
    get.mockResolvedValue({ enabled: false, products: [], incidents: [] })
    renderWithProviders(<ServiceStatusPage />)
    expect(await screen.findByText('The service status page is turned off.')).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    get.mockResolvedValue({ enabled: true, products, incidents: [incident] })
    const { container } = renderWithProviders(<ServiceStatusPage />)
    await screen.findByRole('table', { name: 'Product status' })
    expect(await axe(container)).toHaveNoViolations()
  })
})

describe('ServiceStatusAdminPage', () => {
  beforeEach(() => {
    for (const m of [adminGet, updateStatus]) m.mockReset()
    adminGet.mockResolvedValue({ enabled: true, products, incidents: [incident] })
  })

  it('posts a new status with a note for one product', async () => {
    updateStatus.mockResolvedValue(products[2])
    renderWithProviders(<ServiceStatusAdminPage />)

    const user = userEvent.setup()
    await user.click(await screen.findByRole('combobox', { name: 'New status for Yukth.ai' }))
    await user.click(within(await screen.findByRole('listbox')).getByRole('option', { name: 'Maintenance' }))
    await user.type(screen.getByRole('textbox', { name: 'Note for Yukth.ai' }), 'Upgrade tonight')
    await user.click(screen.getByRole('button', { name: 'Save status for Yukth.ai' }))

    expect(updateStatus).toHaveBeenCalledWith(3, 'MAINTENANCE', 'Upgrade tonight')
    expect(await screen.findByText('Status saved.')).toBeInTheDocument()
  })

  it('no longer has the incident form (incidents moved to the Incidents tab)', async () => {
    renderWithProviders(<ServiceStatusAdminPage />)
    await screen.findByRole('button', { name: 'Save status for Yukth.ai' })
    expect(screen.queryByRole('button', { name: 'Post incident' })).not.toBeInTheDocument()
    expect(screen.queryByText('Slow report exports')).not.toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    const { container } = renderWithProviders(<ServiceStatusAdminPage />)
    await screen.findByRole('button', { name: 'Save status for Yukth.ai' })
    expect(await axe(container)).toHaveNoViolations()
  })
})
