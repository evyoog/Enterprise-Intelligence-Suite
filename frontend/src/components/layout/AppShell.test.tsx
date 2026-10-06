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
    expect(await within(sidebar).findByRole('link', { name: 'Identity federation' }, SIDEBAR_WAIT)).toBeInTheDocument()
    expect(within(sidebar).getByRole('link', { name: 'Business dashboard' })).toHaveAttribute('aria-current', 'page')
    expect(within(sidebar).queryByRole('link', { name: 'Registrations' })).not.toBeInTheDocument()
  })

  it('sends a signed-in platform admin from "/" to the admin console', async () => {
    signedIn(true)
    getPermissions.mockResolvedValue({ platform: ['MANAGE_CATALOG', 'MANAGE_REGISTRATIONS'], organization: [] })
    renderAt('/')

    expect(screen.getByText('Admin home (in app)')).toBeInTheDocument()
    const sidebar = screen.getByRole('navigation', { name: 'Application' })
    expect(await within(sidebar).findByRole('link', { name: 'Registrations' }, SIDEBAR_WAIT)).toBeInTheDocument()
    expect(within(sidebar).queryByRole('link', { name: 'Roles' })).not.toBeInTheDocument()
    expect(within(sidebar).queryByRole('link', { name: 'My products' })).not.toBeInTheDocument()
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
    expect(within(sidebar).getByRole('link', { name: 'My products' })).toBeInTheDocument()
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
