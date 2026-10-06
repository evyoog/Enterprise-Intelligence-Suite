import '../../i18n'
import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { LocalePreferenceProvider } from '../../theming/LocalePreferenceProvider'
import { ThemeModeProvider } from '../../theming/ThemeModeProvider'
import { AuthAwareLayout, PublicOnly } from './AppShell'
import { useInAppShell } from './appShellContext'

const mockUseAuth = vi.fn()
vi.mock('../../auth/AuthProvider', () => ({ useAuth: () => mockUseAuth() }))
const getPermissions = vi.fn()
vi.mock('../../api/myPermissionsApi', () => ({ myPermissionsApi: { get: () => getPermissions() } }))
vi.mock('./NotificationBell', () => ({ NotificationBell: () => <button type="button">Notifications</button> }))
vi.mock('../../api/globalSearchApi', () => ({ globalSearchApi: { search: () => Promise.resolve({ products: [], knowledgeArticles: [], tickets: [] }) } }))

function Probe({ label }: { label: string }) {
  return <p>{label} {useInAppShell() ? '(in app)' : '(public)'}</p>
}

function renderAt(path: string) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <ThemeModeProvider>
        <LocalePreferenceProvider>
          <Routes>
            <Route path="/" element={<PublicOnly><Probe label="Website home" /></PublicOnly>} />
            <Route element={<AuthAwareLayout />}>
              <Route path="/products" element={<Probe label="Catalog" />} />
              <Route path="/admin" element={<Probe label="Admin home" />} />
              <Route path="/organization/business-dashboard" element={<Probe label="Dashboard" />} />
              <Route path="*" element={<Probe label="Page" />} />
            </Route>
          </Routes>
        </LocalePreferenceProvider>
      </ThemeModeProvider>
    </MemoryRouter>,
  )
}

// Role queries over the full MUI sidebar take ~0.5–1 s locally and longer on
// CI runners, past Testing Library's 1 s default wait, so the sidebar
// queries below wait up to 5 s.
const SIDEBAR_WAIT = { timeout: 5000 }

const signedIn = (isAdmin: boolean, logout = vi.fn()) =>
  mockUseAuth.mockReturnValue({ isAuthenticated: true, isAdmin, user: { username: 'ada@example.com' }, logout })

describe('Public website vs signed-in tool', () => {
  beforeEach(() => {
    mockUseAuth.mockReset()
    getPermissions.mockReset()
  })

  it('shows the public website to a visitor, with no sidebar', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: false, isAdmin: false, user: null })
    renderAt('/')
    expect(screen.getByText('Website home (public)')).toBeInTheDocument()
    expect(screen.queryByRole('navigation', { name: 'Application' })).not.toBeInTheDocument()
  })

  it('renders shared pages as public pages for a visitor', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: false, isAdmin: false, user: null })
    renderAt('/products')
    expect(screen.getByText('Catalog (public)')).toBeInTheDocument()
    expect(getPermissions).not.toHaveBeenCalled()
  })

  it('sends a signed-in organization user from "/" into the tool with a role-filtered sidebar', async () => {
    signedIn(false)
    getPermissions.mockResolvedValue({ platform: [], organization: ['MANAGE_ORGANIZATION'] })
    renderAt('/')

    expect(screen.getByText('Dashboard (in app)')).toBeInTheDocument()
    const sidebar = screen.getByRole('navigation', { name: 'Application' })
    expect(await within(sidebar).findByRole('link', { name: 'Sign-in security' }, SIDEBAR_WAIT)).toBeInTheDocument()
    expect(within(sidebar).getByRole('link', { name: 'Dashboard' })).toHaveAttribute('aria-current', 'page')
    expect(within(sidebar).queryByRole('link', { name: 'Registrations' })).not.toBeInTheDocument()
  })

  it('sends a signed-in platform admin from "/" to the admin console', async () => {
    signedIn(true)
    getPermissions.mockResolvedValue({ platform: ['MANAGE_CATALOG', 'MANAGE_REGISTRATIONS'], organization: [] })
    renderAt('/')

    expect(screen.getByText('Admin home (in app)')).toBeInTheDocument()
    const sidebar = screen.getByRole('navigation', { name: 'Application' })
    expect(await within(sidebar).findByRole('link', { name: 'Registrations' }, SIDEBAR_WAIT)).toBeInTheDocument()
    expect(within(sidebar).queryByRole('link', { name: 'Roles & permissions' })).not.toBeInTheDocument()
    expect(within(sidebar).queryByRole('link', { name: 'My applications' })).not.toBeInTheDocument()
  })

  it('offers account pages and sign out from the user menu', async () => {
    const logout = vi.fn()
    signedIn(false, logout)
    getPermissions.mockResolvedValue({ platform: [], organization: [] })
    renderAt('/products')

    const user = userEvent.setup()
    await user.click(screen.getByRole('button', { name: 'Account menu for ada@example.com' }))
    const menu = await screen.findByRole('menu')
    expect(within(menu).getByRole('menuitem', { name: 'Security' })).toBeInTheDocument()
    await user.click(within(menu).getByRole('menuitem', { name: 'Sign out' }))
    expect(logout).toHaveBeenCalled()
  })

  it('opens the website home page after signing out, from any page (C79)', async () => {
    const logout = vi.fn(() => mockUseAuth.mockReturnValue({ isAuthenticated: false, isAdmin: false, user: null }))
    signedIn(false, logout)
    getPermissions.mockResolvedValue({ platform: [], organization: [] })
    renderAt('/products')

    const user = userEvent.setup()
    await user.click(screen.getByRole('button', { name: 'Sign out' }))
    expect(logout).toHaveBeenCalled()
    expect(await screen.findByText('Website home (public)')).toBeInTheDocument()
  })

  it('shows the website home page to a signed-in user who clicks Home (C79)', async () => {
    signedIn(false)
    getPermissions.mockResolvedValue({ platform: [], organization: [] })
    renderAt('/products')

    const user = userEvent.setup()
    await user.click(screen.getByRole('link', { name: 'Home' }))
    expect(await screen.findByText('Website home (public)')).toBeInTheDocument()
    expect(screen.queryByRole('navigation', { name: 'Application' })).not.toBeInTheDocument()
  })

  it('still works when the permission lookup fails', async () => {
    signedIn(false)
    getPermissions.mockRejectedValue(new Error('offline'))
    renderAt('/products')
    const sidebar = screen.getByRole('navigation', { name: 'Application' })
    expect(within(sidebar).getByRole('link', { name: 'Product catalog' })).toHaveAttribute('aria-current', 'page')
    expect(within(sidebar).getByRole('link', { name: 'My applications' })).toBeInTheDocument()
  })

  it('has no detectable a11y violations', async () => {
    signedIn(true)
    getPermissions.mockResolvedValue({ platform: ['MANAGE_CATALOG'], organization: [] })
    const { container } = renderAt('/admin')
    const sidebar = screen.getByRole('navigation', { name: 'Application' })
    await within(sidebar).findByRole('link', { name: 'Products' }, SIDEBAR_WAIT)
    expect(await axe(container)).toHaveNoViolations()
  }, 20000)
})

// C80: one menu config, three kinds of user.
describe('Sidebar by kind of user (C80)', () => {
  beforeEach(() => {
    mockUseAuth.mockReset()
    getPermissions.mockReset()
  })

  const groupLabels = (sidebar: HTMLElement) =>
    Array.from(sidebar.querySelectorAll('.MuiListSubheader-root')).map((e) => e.textContent)
  const linkNames = (sidebar: HTMLElement) => within(sidebar).queryAllByRole('link').map((e) => e.textContent)

  it('shows a regular member the short menu with Help and Billing groups and no admin module', async () => {
    signedIn(false)
    getPermissions.mockResolvedValue({ platform: [], organization: [] })
    renderAt('/my/products')
    const sidebar = screen.getByRole('navigation', { name: 'Application' })
    await within(sidebar).findByRole('link', { name: 'Invoices & payments' }, SIDEBAR_WAIT)
    expect(groupLabels(sidebar)).toEqual(['Workspace', 'Help', 'Billing', 'Account'])
    // Knowledge Center's children stay folded until the user is inside it.
    expect(linkNames(sidebar)).toEqual([
      'My applications', 'Product catalog', 'Knowledge Center', 'Support', 'Overview', 'Invoices & payments', 'Security', 'Preferences',
    ])
    expect(within(sidebar).queryByRole('link', { name: /service status|audit|partners|members/i })).not.toBeInTheDocument()
  })

  it('shows an organization admin Organization, Platform (status only) and Operations with Support once', async () => {
    signedIn(false)
    getPermissions.mockResolvedValue({
      platform: [],
      organization: ['MANAGE_ORGANIZATION', 'MANAGE_USERS', 'MANAGE_PRIVILEGED_ACCESS', 'MANAGE_ORDERS'],
    })
    renderAt('/organization/members')
    const sidebar = screen.getByRole('navigation', { name: 'Application' })
    await within(sidebar).findByRole('link', { name: 'Members' }, SIDEBAR_WAIT)
    expect(groupLabels(sidebar)).toEqual(['Workspace', 'Organization', 'Platform', 'Operations', 'Account'])
    expect(within(sidebar).getAllByRole('link', { name: 'Support' })).toHaveLength(1)
    expect(within(sidebar).getAllByRole('link', { name: 'Service status' })).toHaveLength(1)
    expect(within(sidebar).queryByRole('link', { name: 'Applications' })).not.toBeInTheDocument()
  })

  it('shows an intermediate role (billing manager) exactly its Billing items, with no empty groups', async () => {
    signedIn(true)
    getPermissions.mockResolvedValue({ platform: ['MANAGE_BILLING'], organization: [] })
    renderAt('/admin/billing/payment-gateway')
    const sidebar = screen.getByRole('navigation', { name: 'Application' })
    await within(sidebar).findByRole('link', { name: 'Payment gateway' }, SIDEBAR_WAIT)
    expect(groupLabels(sidebar)).toEqual(['Workspace', 'Platform', 'Operations', 'Account'])
    expect(within(sidebar).getByRole('link', { name: 'Payment gateway' })).toHaveAttribute('aria-current', 'page')
    expect(within(sidebar).getByRole('link', { name: 'Billing settings' })).toBeInTheDocument()
    expect(within(sidebar).queryByRole('link', { name: /audit log|reviews|partners|support/i })).not.toBeInTheDocument()
  })

  it('opens only the group holding the current page, on a deep link or refresh', async () => {
    signedIn(true)
    getPermissions.mockResolvedValue({ platform: ['MANAGE_INTEGRATIONS', 'MANAGE_BILLING'], organization: [] })
    renderAt('/admin/integrations/api-keys')
    const sidebar = screen.getByRole('navigation', { name: 'Application' })
    expect(await within(sidebar).findByRole('link', { name: 'API keys' }, SIDEBAR_WAIT)).toHaveAttribute('aria-current', 'page')
    expect(within(sidebar).getByRole('link', { name: 'Platform events' })).toBeInTheDocument()
    expect(within(sidebar).queryByRole('link', { name: 'Payment gateway' })).not.toBeInTheDocument()
    expect(within(sidebar).getByRole('button', { name: 'Integrations' })).toHaveAttribute('aria-expanded', 'true')
    expect(within(sidebar).getByRole('button', { name: 'Billing' })).toHaveAttribute('aria-expanded', 'false')
  })

  it('makes Knowledge Center one clickable entry whose children are the readers\' ones', async () => {
    signedIn(false)
    getPermissions.mockResolvedValue({ platform: [], organization: [] })
    renderAt('/knowledge/guides')
    const sidebar = screen.getByRole('navigation', { name: 'Application' })
    expect(await within(sidebar).findByRole('link', { name: 'Guides' }, SIDEBAR_WAIT)).toHaveAttribute('aria-current', 'page')
    expect(within(sidebar).getByRole('link', { name: 'Knowledge Center' })).toHaveAttribute('href', '/knowledge')
    expect(within(sidebar).getAllByRole('link', { name: /knowledge/i })).toHaveLength(1)
    expect(within(sidebar).getByRole('link', { name: 'FAQs' })).toBeInTheDocument()
  })

  it('shows breadcrumbs on a nested page', async () => {
    signedIn(true)
    getPermissions.mockResolvedValue({ platform: ['MANAGE_INTEGRATIONS'], organization: [] })
    renderAt('/admin/integrations/api-keys')
    const crumbs = await screen.findByRole('navigation', { name: 'Breadcrumb' })
    expect(within(crumbs).getByText('Platform')).toBeInTheDocument()
    expect(within(crumbs).getByText('API keys')).toHaveAttribute('aria-current', 'page')
  })
})
