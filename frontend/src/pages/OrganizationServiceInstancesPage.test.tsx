import '../i18n'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/client'
import type { ServiceInstance } from '../api/serviceInstancesApi'
import { renderWithProviders } from '../test/renderWithProviders'
import { OrganizationServiceInstancesPage } from './OrganizationServiceInstancesPage'

const list = vi.fn()
vi.mock('../api/serviceInstancesApi', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../api/serviceInstancesApi')>()
  return { ...actual, organizationServiceInstancesApi: { list: () => list() } }
})

const withUrl: ServiceInstance = {
  id: 1, subscriptionId: 10, productId: 1, productName: 'Thiran.ai', ownerType: 'ORGANIZATION', ownerName: 'Acme Corp',
  status: 'ACTIVE', health: 'DEGRADED', healthMessage: 'Slow exports', lastReportedAt: '2026-10-14T08:00:00Z', stale: false,
  accessUrl: 'https://thiran.example.com/acme', changedBy: 'PRODUCT', createdAt: '2026-10-01T00:00:00Z',
}
const noReport: ServiceInstance = {
  id: 2, subscriptionId: 11, productId: 2, productName: 'Valam.ai', ownerType: 'ORGANIZATION', ownerName: 'Acme Corp',
  status: 'PROVISIONING', health: 'UNKNOWN', lastReportedAt: null, stale: true, changedBy: 'SYSTEM', createdAt: '2026-10-02T00:00:00Z',
}

describe('OrganizationServiceInstancesPage (C101)', () => {
  beforeEach(() => list.mockReset())

  it('lists the organization instances read-only, with the access URL as a link, and has no accessibility violations', async () => {
    list.mockResolvedValue([withUrl, noReport])
    const { container } = renderWithProviders(<OrganizationServiceInstancesPage />)
    const row = (await screen.findByText('Thiran.ai')).closest('tr')!
    expect(within(row).getByText('Active')).toBeInTheDocument()
    expect(within(row).getByText('Degraded')).toBeInTheDocument()
    expect(within(row).getByText('Slow exports')).toBeInTheDocument()
    expect(within(row).getByRole('link', { name: 'https://thiran.example.com/acme' })).toHaveAttribute('href', 'https://thiran.example.com/acme')
    const second = screen.getByText('Valam.ai').closest('tr')!
    expect(within(second).getByText('Provisioning')).toBeInTheDocument()
    expect(within(second).getByText('No recent report')).toBeInTheDocument()
    expect(within(second).getByText('Never')).toBeInTheDocument()
    expect(within(second).queryByRole('link')).not.toBeInTheDocument()
    expect(screen.queryByRole('button')).not.toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('shows an empty state when there are no instances', async () => {
    list.mockResolvedValue([])
    renderWithProviders(<OrganizationServiceInstancesPage />)
    expect(await screen.findByText('No service instances yet')).toBeInTheDocument()
  })

  it('shows the error and retries', async () => {
    list.mockRejectedValueOnce(new ApiError(500, 'Boom'))
    list.mockResolvedValueOnce([withUrl])
    renderWithProviders(<OrganizationServiceInstancesPage />)
    expect(await screen.findByText(/Boom/)).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Retry' }))
    expect(await screen.findByText('Thiran.ai')).toBeInTheDocument()
  })
})
