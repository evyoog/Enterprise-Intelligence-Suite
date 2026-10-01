import '../i18n'
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/client'
import type { BusinessDashboard } from '../api/businessDashboardApi'
import { LocalePreferenceProvider } from '../theming/LocalePreferenceProvider'
import { ThemeModeProvider } from '../theming/ThemeModeProvider'
import { BusinessDashboardPage } from './BusinessDashboardPage'

const mockNavigate = vi.fn()
const mockGet = vi.fn()

vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal<typeof import('react-router-dom')>()
  return { ...actual, useNavigate: () => mockNavigate }
})

vi.mock('../api/businessDashboardApi', () => ({
  businessDashboardApi: { get: () => mockGet() },
}))

vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({ isAuthenticated: true, isAdmin: false, user: { username: 'ada@example.com' }, logout: vi.fn() }),
}))

vi.mock('../auth/AuthModalContext', () => ({
  useAuthModal: () => ({ openLogin: vi.fn(), openRegister: vi.fn() }),
}))

vi.mock('../api/productsApi', () => ({
  productsApi: { list: vi.fn().mockResolvedValue([]) },
}))

function renderPage() {
  return render(
    <MemoryRouter>
      <ThemeModeProvider>
        <LocalePreferenceProvider>
          <BusinessDashboardPage />
        </LocalePreferenceProvider>
      </ThemeModeProvider>
    </MemoryRouter>
  )
}

describe('BusinessDashboardPage — routes the wrong audience to their own dashboard', () => {
  beforeEach(() => {
    mockNavigate.mockReset()
    mockGet.mockReset()
  })

  it('redirects to /my/products on 403 (an org member without MANAGE_ORGANIZATION)', async () => {
    mockGet.mockRejectedValue(new ApiError(403, 'You do not have permission to do this'))
    renderPage()
    await waitFor(() => expect(mockNavigate).toHaveBeenCalledWith('/my/products', { replace: true }))
  })

  it('redirects to /my/products on 404 (an individual customer with no organization at all)', async () => {
    mockGet.mockRejectedValue(new ApiError(404, 'You are not a member of an organization'))
    renderPage()
    await waitFor(() => expect(mockNavigate).toHaveBeenCalledWith('/my/products', { replace: true }))
  })

  it('does not redirect on a genuine server error — shows it instead', async () => {
    mockGet.mockRejectedValue(new ApiError(500, 'Something broke'))
    const { findByText } = renderPage()
    await findByText('Something broke')
    expect(mockNavigate).not.toHaveBeenCalled()
  })
})

const dashboard: BusinessDashboard = {
  organization: {
    id: 1, name: 'Acme Corp', code: 'ACME', businessEmail: 'biz@acme.example', country: 'India',
    licensedSeats: 10, activeMemberCount: 4, status: 'COMPLETED', mfaRequired: false,
  },
  applications: [
    { productId: 1, productName: 'Valam.ai', subscriptionStatus: 'ACTIVE', assignedMembers: 3, totalLaunches: 42, lastUsedAt: '2026-09-20T00:00:00Z' },
    { productId: 2, productName: 'Varthan.ai', subscriptionStatus: null, assignedMembers: 1, totalLaunches: 5, lastUsedAt: undefined },
  ],
  seatUsage: { licensedSeats: 10, activeMemberCount: 4, utilizationPercent: 40 },
  billing: {
    subscriptions: [],
    spentThisPeriodByCurrency: { USD: 15000 },
    spentLastPeriodByCurrency: { USD: 10000 },
    note: 'Reflects paid invoices from Billing & Payments (REQ-BIL-001).',
  },
  serviceHealth: { platformStatus: 'UP', note: 'Live.' },
  support: { available: false, note: 'Not available yet.' },
  alerts: [],
}

describe('BusinessDashboardPage — interactive charts and table (C54)', () => {
  beforeEach(() => {
    mockNavigate.mockReset()
    mockGet.mockReset()
    mockGet.mockResolvedValue(dashboard)
  })

  it('clicking a donut segment filters the applications table, and clicking it again clears the filter', async () => {
    renderPage()
    const user = userEvent.setup()
    await screen.findByText('Launches by application')
    const table = () => within(screen.getAllByRole('table')[0])

    const donutSegmentButton = () => screen.getAllByRole('button', { name: /Not subscribed \/ inactive/ })[0]

    await user.click(donutSegmentButton())
    expect(table().queryByText('Valam.ai')).not.toBeInTheDocument()
    expect(table().getByText('Varthan.ai')).toBeInTheDocument()

    await user.click(donutSegmentButton())
    expect(table().getByText('Valam.ai')).toBeInTheDocument()
    expect(table().getByText('Varthan.ai')).toBeInTheDocument()
  })

  it('clicking a bar filters the table to that application, with a Clear filter action', async () => {
    renderPage()
    const user = userEvent.setup()
    await screen.findByText('Launches by application')
    const table = () => within(screen.getAllByRole('table')[0])

    await user.click(screen.getByRole('button', { name: /Varthan\.ai/ }))
    expect(table().queryByText('Valam.ai')).not.toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Clear filter' }))
    expect(table().getByText('Valam.ai')).toBeInTheDocument()
  })

  it('sorts the table by a clicked column', async () => {
    renderPage()
    const user = userEvent.setup()
    await screen.findByText('Launches by application')
    const table = () => within(screen.getAllByRole('table')[0])

    const rowsAscending = () => table().getAllByRole('row').slice(1).map((r) => within(r).getAllByRole('cell')[0].textContent)
    expect(rowsAscending()).toEqual(['Valam.ai', 'Varthan.ai'])

    await user.click(table().getByRole('button', { name: 'Application' }))
    expect(rowsAscending()).toEqual(['Varthan.ai', 'Valam.ai'])
  })

  it('shows a trend arrow on the spend tile computed from this vs last period', async () => {
    renderPage()
    await screen.findByText('150.00 USD')
    expect(screen.getByText((_, node) => node?.textContent === '50.0%')).toBeInTheDocument()
  })
})
