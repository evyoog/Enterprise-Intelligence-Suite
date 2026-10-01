import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { ThemeModeProvider } from '../../theming/ThemeModeProvider'
import { PlatformAdminDashboardPage } from './PlatformAdminDashboardPage'

const mockGet = vi.fn()

vi.mock('../../api/platformDashboardApi', () => ({
  platformDashboardApi: { get: () => mockGet() },
}))

const dashboard = {
  organizations: { total: 12, active: 10 },
  catalog: { totalProducts: 8, activeProducts: 6, totalPlatforms: 3 },
  subscriptions: { active: 20, byStatus: { ACTIVE: 20, SUSPENDED: 2, CANCELLED: 1, EXPIRED: 0 } },
  billing: {
    revenueThisPeriodByCurrency: { USD: 150000 },
    revenueLastPeriodByCurrency: { USD: 100000 },
    note: 'Reflects paid invoices from Billing & Payments (REQ-BIL-001), platform-wide.',
  },
  openSupportTicketCount: 4,
  pendingReviewCount: 2,
  serviceHealth: { platformStatus: 'UP', note: 'Live Actuator status.' },
  topProductsByLaunches: [
    { productId: 1, productName: 'Valam.ai', totalLaunches: 42 },
    { productId: 2, productName: 'Varthan.ai', totalLaunches: 17 },
  ],
}

function renderPage() {
  return render(
    <MemoryRouter>
      <ThemeModeProvider>
        <PlatformAdminDashboardPage />
      </ThemeModeProvider>
    </MemoryRouter>
  )
}

describe('PlatformAdminDashboardPage', () => {
  it('shows real platform-wide figures from the backend, including a revenue trend', async () => {
    mockGet.mockResolvedValue(dashboard)
    renderPage()

    expect(await screen.findByText('12')).toBeInTheDocument()
    expect(screen.getByText('10 active')).toBeInTheDocument()
    expect(screen.getByText('1500.00 USD')).toBeInTheDocument()
    expect(screen.getByText((_, node) => node?.textContent === '50.0%')).toBeInTheDocument()
    expect(screen.getByText('vs last period')).toBeInTheDocument()
    expect(screen.getByText('Valam.ai')).toBeInTheDocument()
    expect(screen.getByText('4')).toBeInTheDocument()
  })

  it('shows the honest no-revenue note instead of a fabricated figure when nothing has been paid', async () => {
    mockGet.mockResolvedValue({
      ...dashboard,
      billing: { revenueThisPeriodByCurrency: {}, revenueLastPeriodByCurrency: {}, note: 'No paid invoices yet (REQ-BIL-001).' },
    })
    renderPage()

    expect(await screen.findByText('No paid invoices yet (REQ-BIL-001).')).toBeInTheDocument()
  })

  it('shows the backend error message on failure', async () => {
    mockGet.mockRejectedValue(new ApiError(500, 'Something broke'))
    renderPage()

    await waitFor(() => expect(screen.getByText('Something broke')).toBeInTheDocument())
  })
})
