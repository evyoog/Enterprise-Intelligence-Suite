import '../i18n'
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/client'
import type { BusinessDashboard } from '../api/businessDashboardApi'
import { LocalePreferenceProvider } from '../theming/LocalePreferenceProvider'
import { ThemeModeProvider } from '../theming/ThemeModeProvider'
import { BusinessDashboardPage } from './BusinessDashboardPage'

const mockNavigate = vi.fn()
const mockGet = vi.fn()
const api = {
  me: vi.fn(), status: vi.fn(), renewals: vi.fn(), members: vi.fn(), audit: vi.fn(), sessions: vi.fn(),
  permissions: vi.fn(), mfa: vi.fn(), saml: vi.fn(), oidc: vi.fn(), addFavorite: vi.fn(), removeFavorite: vi.fn(), recordLaunch: vi.fn(),
}

vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal<typeof import('react-router-dom')>()
  return { ...actual, useNavigate: () => mockNavigate }
})
vi.mock('../api/businessDashboardApi', () => ({ businessDashboardApi: { get: () => mockGet() } }))
vi.mock('../api/dashboardApi', () => ({
  dashboardApi: {
    get: () => api.me(), addFavorite: (id: number) => api.addFavorite(id), removeFavorite: (id: number) => api.removeFavorite(id),
    recordLaunch: (id: number) => api.recordLaunch(id),
  },
}))
vi.mock('../api/serviceStatusApi', () => ({ serviceStatusApi: { get: () => api.status() } }))
vi.mock('../api/renewalsApi', () => ({ renewalsApi: { myRenewals: () => api.renewals() } }))
vi.mock('../api/registrationApi', () => ({ organizationApi: { listMyOrgUsers: () => api.members() } }))
vi.mock('../api/auditLogApi', () => ({ organizationAuditLogApi: { search: () => api.audit() } }))
vi.mock('../api/sessionsApi', () => ({ sessionsApi: { list: () => api.sessions() } }))
vi.mock('../api/myPermissionsApi', () => ({ myPermissionsApi: { get: () => api.permissions() } }))
vi.mock('../api/mfaApi', () => ({ mfaApi: { getStatus: () => api.mfa() } }))
vi.mock('../api/samlApi', () => ({ samlApi: { list: () => api.saml() } }))
vi.mock('../api/oidcApi', () => ({ oidcApi: { list: () => api.oidc() } }))
vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({ isAuthenticated: true, isAdmin: false, user: { username: 'ada@acme.example', email: 'ada@acme.example', roles: [] }, logout: vi.fn() }),
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

const dashboard: BusinessDashboard = {
  organization: {
    id: 1, name: 'Acme Corp', code: 'ACME', businessEmail: 'biz@acme.example', country: 'India',
    licensedSeats: 10, activeMemberCount: 4, status: 'COMPLETED', mfaRequired: true,
  },
  applications: [
    { productId: 1, productName: 'Valam.ai', subscriptionStatus: 'ACTIVE', assignedMembers: 3, totalLaunches: 42, lastUsedAt: '2026-09-20T00:00:00Z' },
    { productId: 2, productName: 'Varthan.ai', subscriptionStatus: null, assignedMembers: 1, totalLaunches: 0, lastUsedAt: undefined },
  ],
  seatUsage: { licensedSeats: 10, activeMemberCount: 4, utilizationPercent: 40 },
  billing: {
    subscriptions: [{ id: 7, productId: 1, productName: 'Valam.ai', status: 'ACTIVE' }],
    spentThisPeriodByCurrency: { USD: 15000 },
    spentLastPeriodByCurrency: { USD: 10000 },
    note: 'Reflects paid invoices.',
  },
  serviceHealth: { platformStatus: 'UP', note: 'Live.' },
  support: { available: false, note: 'Not available yet.' },
  alerts: [{ type: 'UNDERUTILIZED_SEATS', severity: 'info', message: '6 of your 10 licensed seats are unused.' }],
}

const myDashboard = {
  organization: null, alerts: [], preferences: { widgetOrder: [], hiddenWidgets: [] },
  products: [
    { productId: 1, productName: 'Valam.ai', subscriptionStatus: 'ACTIVE', myAccessAssigned: true, favorite: true, launchCount: 9,
      lastLaunchedAt: new Date(Date.now() - 10 * 60_000).toISOString(), launchUrl: 'https://valam.example' },
    { productId: 2, productName: 'Varthan.ai', subscriptionStatus: null, myAccessAssigned: true, favorite: false, launchCount: 0 },
  ],
}

function mockAllSources() {
  api.me.mockResolvedValue(myDashboard)
  api.status.mockResolvedValue({ enabled: true, incidents: [], products: [
    { productId: 1, productName: 'Valam.ai', status: 'OPERATIONAL', purchased: true, openIncidents: 0 },
  ] })
  api.renewals.mockResolvedValue([{ subscriptionId: 7, productName: 'Valam.ai', planName: null, autoRenew: true, renewalDate: '2026-10-30', remindersEnabled: true, nextReminderAt: null }])
  api.members.mockResolvedValue([
    { organizationMemberId: 1, customerId: 11, firstName: 'Ada', lastName: 'Lovelace', email: 'ada@acme.example', orgRole: 'ORG_ADMIN', status: 'ACTIVE' },
    { organizationMemberId: 2, customerId: 12, firstName: 'Bob', email: 'bob@acme.example', orgRole: 'MEMBER', status: 'ACTIVE' },
  ])
  api.audit.mockResolvedValue({ page: 0, size: 8, totalElements: 2, items: [
    { id: 1, timestamp: new Date().toISOString(), action: 'SUBSCRIPTION_RENEWED', actorEmail: 'ada@acme.example', outcome: 'SUCCESS' },
    { id: 2, timestamp: new Date().toISOString(), action: 'GROUP_MEMBER_ADDED', actorEmail: 'ada@acme.example', outcome: 'SUCCESS' },
  ] })
  api.sessions.mockResolvedValue([{ id: 's1', startedAt: '2026-10-03T04:54:00Z', clients: [], current: true }])
  api.permissions.mockResolvedValue({ platform: [], organization: ['MANAGE_ORGANIZATION', 'MANAGE_ORDERS'] })
  api.mfa.mockResolvedValue({ enabled: false, remainingRecoveryCodes: 0 })
  api.saml.mockResolvedValue([{ id: 1, enabled: true }])
  api.oidc.mockResolvedValue([])
  api.addFavorite.mockResolvedValue(undefined)
  api.removeFavorite.mockResolvedValue(undefined)
}

beforeEach(() => {
  mockNavigate.mockReset()
  mockGet.mockReset()
  Object.values(api).forEach((m) => m.mockReset())
  localStorage.clear()
  // Deterministic numbers: the count-up and drawing animations are skipped with Reduce motion on.
  localStorage.setItem('vyoog-reduced-motion', 'true')
})

describe('BusinessDashboardPage — routes the wrong audience to their own dashboard', () => {
  it('redirects to /my/products on 403 (an org member without MANAGE_ORGANIZATION)', async () => {
    mockGet.mockRejectedValue(new ApiError(403, 'You do not have permission to do this'))
    renderPage()
    await waitFor(() => expect(mockNavigate).toHaveBeenCalledWith('/my/products', { replace: true }))
  })

  it('redirects to /my/products on 404 (an individual customer with no organization at all)', async () => {
    mockGet.mockRejectedValue(new ApiError(404, 'Not a member of an organization'))
    renderPage()
    await waitFor(() => expect(mockNavigate).toHaveBeenCalledWith('/my/products', { replace: true }))
  })

  it('does not redirect on a genuine server error — shows it instead', async () => {
    mockGet.mockRejectedValue(new ApiError(500, 'Something broke'))
    renderPage()
    await screen.findByText('Something broke')
    expect(mockNavigate).not.toHaveBeenCalled()
  })
})

describe('BusinessDashboardPage — C69 workspace', () => {
  beforeEach(() => {
    mockGet.mockResolvedValue(dashboard)
    mockAllSources()
  })

  it('shows the organization, a greeting with the real user name, real status and seats', async () => {
    renderPage()
    expect(await screen.findByRole('heading', { level: 1, name: 'Acme Corp' })).toBeInTheDocument()
    expect(await screen.findByText(/^Good (morning|afternoon|evening), Ada Lovelace$/)).toBeInTheDocument()
    expect(screen.getAllByText('All systems operational').length).toBeGreaterThan(0)
    expect(screen.getByRole('link', { name: /6 unused seats/ })).toHaveAttribute('href', '/organization/members')
    // Quick actions only use existing routes.
    const quick = screen.getByRole('navigation', { name: 'Quick actions' })
    expect(within(quick).getAllByRole('link').map((a) => a.getAttribute('href')))
      .toEqual(['/my/products', '/products', '/organization/members', '/organization/billing', '/support/tickets'])
  })

  it('derives the KPIs from the business dashboard (no invented trends)', async () => {
    renderPage()
    const kpis = await screen.findByRole('region', { name: 'Overview' })
    expect(within(kpis).getByRole('link', { name: /Licensed seats: 10/ })).toHaveAttribute('href', '/organization/members')
    expect(within(kpis).getByRole('link', { name: /Active members: 4/ })).toBeInTheDocument()
    expect(within(kpis).getByRole('link', { name: /Applications: 2/ })).toBeInTheDocument()
    expect(within(kpis).getByRole('link', { name: /Application launches: 42/ })).toBeInTheDocument()
    expect(within(kpis).getByText('40% used')).toBeInTheDocument()
    expect(screen.queryByText(/vs last month/)).not.toBeInTheDocument()
  })

  it('a bar opens the application details; the donut and the filters narrow the table', async () => {
    renderPage()
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: /Open details: Valam\.ai/ }))
    expect(mockNavigate).toHaveBeenCalledWith('/products/1')

    const table = () => within(screen.getByRole('table', { name: 'Applications' }))
    const donut = () => within(screen.getByRole('region', { name: 'Subscription status' })).getByRole('button', { name: /Not subscribed \/ inactive/ })
    await user.click(donut())
    expect(table().queryByText('Valam.ai')).not.toBeInTheDocument()
    expect(table().getByText('Varthan.ai')).toBeInTheDocument()
    await user.click(donut())
    expect(table().getByText('Valam.ai')).toBeInTheDocument()

    await user.click(screen.getByRole('combobox', { name: 'Application' }))
    await user.click(await screen.findByRole('option', { name: 'Varthan.ai' }))
    expect(table().queryByText('Valam.ai')).not.toBeInTheDocument()
    await user.click(screen.getAllByRole('button', { name: 'Clear filters' })[0])
    expect(table().getByText('Valam.ai')).toBeInTheDocument()
  })

  it('sorts the table by a clicked column', async () => {
    renderPage()
    const user = userEvent.setup()
    const table = within(await screen.findByRole('table', { name: 'Applications' }))
    const names = () => table.getAllByRole('row').slice(1).map((r) => within(r).getAllByRole('cell')[0].textContent)
    expect(names()).toEqual(['Valam.ai', 'Varthan.ai'])
    await user.click(table.getByRole('button', { name: 'Application' }))
    expect(names()).toEqual(['Varthan.ai', 'Valam.ai'])
  })

  it('shows the signed-in user from the member list, permissions and identity', async () => {
    renderPage()
    const account = await screen.findByRole('region', { name: 'Your account' })
    expect(await within(account).findByText('Organization admin')).toBeInTheDocument()
    expect(within(account).getByText('ada@acme.example')).toBeInTheDocument()
    expect(within(account).getByText('Active')).toBeInTheDocument()
    expect(within(account).getByText('Not enabled')).toBeInTheDocument()
    expect(within(account).getByText('Required by your organization')).toBeInTheDocument()
    expect(within(account).getByText('Connected (1 provider)')).toBeInTheDocument()
    expect(within(account).getByText('Manage organization')).toBeInTheDocument()
    expect(within(account).getByRole('link', { name: 'Security settings' })).toHaveAttribute('href', '/account/security')
  })

  it('lists recent activity and favourites, and can remove a favourite', async () => {
    renderPage()
    const user = userEvent.setup()
    const activity = await screen.findByRole('region', { name: 'Recent activity' })
    expect(await within(activity).findByText('Subscription renewed')).toBeInTheDocument()
    expect(within(activity).getByText('Group member added')).toBeInTheDocument()

    const favorites = screen.getByRole('region', { name: 'Favorites' })
    expect(within(favorites).getByText('Valam.ai')).toBeInTheDocument()
    await user.click(within(favorites).getByRole('button', { name: 'Remove Valam.ai from favorites' }))
    expect(api.removeFavorite).toHaveBeenCalledWith(1)
    expect(await within(screen.getByRole('region', { name: 'Favorites' })).findByText('No favorite applications yet.')).toBeInTheDocument()
  })

  it('shows only real attention items, each with its action, and the billing summary', async () => {
    renderPage()
    const attention = await screen.findByRole('region', { name: 'Attention required' })
    expect(await within(attention).findByText('Unused licensed seats')).toBeInTheDocument()
    expect(within(attention).getByRole('link', { name: 'Manage members' })).toHaveAttribute('href', '/organization/members')
    expect(within(attention).queryByText('Upcoming renewal')).not.toBeInTheDocument()

    const billing = screen.getByRole('region', { name: 'Billing' })
    expect(await within(billing).findByText(/Valam\.ai · /)).toBeInTheDocument()
    expect(within(billing).getByText('150.00 USD')).toBeInTheDocument()
    expect(within(billing).getByText((_, node) => node?.textContent === '50.0%')).toBeInTheDocument()
  })

  it("says you're all caught up when nothing needs attention", async () => {
    mockGet.mockResolvedValue({ ...dashboard, alerts: [] })
    renderPage()
    expect(await screen.findByText("You're all caught up")).toBeInTheDocument()
  })

  it('reports a degraded service instead of "operational"', async () => {
    api.status.mockResolvedValue({ enabled: true, incidents: [], products: [
      { productId: 1, productName: 'Valam.ai', status: 'DEGRADED', purchased: true, openIncidents: 1 },
    ] })
    renderPage()
    expect((await screen.findAllByText('Degraded performance')).length).toBeGreaterThan(0)
    expect(screen.queryByText('All systems operational')).not.toBeInTheDocument()
  })

  it('a failing section shows Retry without breaking the rest of the dashboard', async () => {
    api.audit.mockRejectedValueOnce(new ApiError(500, 'boom'))
    renderPage()
    const activity = await screen.findByRole('region', { name: 'Recent activity' })
    expect(await within(activity).findByText('Recent activity is unavailable right now.')).toBeInTheDocument()
    expect(screen.getByRole('table', { name: 'Applications' })).toBeInTheDocument()
    await userEvent.setup().click(within(activity).getByRole('button', { name: 'Retry' }))
    expect(await within(screen.getByRole('region', { name: 'Recent activity' })).findByText('Subscription renewed')).toBeInTheDocument()
  })

  it('has no detectable accessibility violations', async () => {
    const { container } = renderPage()
    await screen.findByText('Subscription renewed')
    expect(await axe(container)).toHaveNoViolations()
  })
})
