import '../../i18n'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { LocalePreferenceProvider } from '../../theming/LocalePreferenceProvider'
import { ThemeModeProvider } from '../../theming/ThemeModeProvider'
import { SiteNavbar } from './SiteNavbar'

const mockUseAuth = vi.fn()
vi.mock('../../auth/AuthProvider', () => ({
  useAuth: () => mockUseAuth(),
}))

vi.mock('../../auth/AuthModalContext', () => ({
  useAuthModal: () => ({ openLogin: vi.fn(), openRegister: vi.fn() }),
}))

vi.mock('../../api/productsApi', () => ({
  productsApi: { list: vi.fn().mockResolvedValue([]) },
}))

function renderNavbar() {
  return render(
    <MemoryRouter>
      <ThemeModeProvider>
        <LocalePreferenceProvider>
          <SiteNavbar />
        </LocalePreferenceProvider>
      </ThemeModeProvider>
    </MemoryRouter>
  )
}

describe('SiteNavbar — skip link, menu semantics, keyboard behavior', () => {
  beforeEach(() => {
    mockUseAuth.mockReturnValue({ isAuthenticated: false, isAdmin: false, user: null, logout: vi.fn() })
  })

  it('renders a skip link as the very first focusable element', async () => {
    renderNavbar()
    const user = userEvent.setup()
    await user.tab()
    expect(document.activeElement).toHaveTextContent(/skip to content/i)
    expect(document.activeElement).toHaveAttribute('href', '#main-content')
  })

  it('the Products menu trigger reports its open/closed state via aria-expanded', async () => {
    renderNavbar()
    const user = userEvent.setup()
    const trigger = screen.getByRole('button', { name: /products/i })
    expect(trigger).toHaveAttribute('aria-expanded', 'false')

    await user.click(trigger);
    expect(trigger).toHaveAttribute('aria-expanded', 'true')
  })

  it('closes an open menu on Escape', async () => {
    renderNavbar()
    const user = userEvent.setup()
    const trigger = screen.getByRole('button', { name: /products/i })
    await user.click(trigger)
    expect(trigger).toHaveAttribute('aria-expanded', 'true')

    await user.keyboard('{Escape}')
    expect(trigger).toHaveAttribute('aria-expanded', 'false')
  })

  it('the primary nav is a labelled landmark', () => {
    renderNavbar()
    expect(screen.getByRole('navigation', { name: /primary/i })).toBeInTheDocument()
  })

  it('has no detectable a11y violations in its default (signed-out) state', async () => {
    const { container } = renderNavbar()
    const results = await axe(container)
    expect(results).toHaveNoViolations()
  })
})

describe('SiteNavbar — Phase 7: a consistent, persistent link back to the workspace', () => {
  it('an authenticated non-platform-admin always sees a link to the business dashboard route, not a fixed /my/products link', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: true, isAdmin: false, user: { username: 'ada@example.com' }, logout: vi.fn() })
    renderNavbar()

    const link = screen.getByRole('link', { name: /workspace/i })
    // Deliberately the business-dashboard route regardless of whether this
    // account turns out to be an org admin, a plain member, or an
    // individual with no organization — see BusinessDashboardPage's own
    // 403/404 handling for why that page is what sorts the three cases out,
    // not the navbar (this used to be a hardcoded /my/products link with no
    // way back to an org admin's own business dashboard once they'd
    // navigated away from it).
    expect(link).toHaveAttribute('href', '/organization/business-dashboard')
  })

  it('a platform admin sees the Admin Panel link instead', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: true, isAdmin: true, user: { username: 'admin@example.com' }, logout: vi.fn() })
    renderNavbar()

    expect(screen.getByRole('link', { name: /admin panel/i })).toHaveAttribute('href', '/admin')
    expect(screen.queryByRole('link', { name: /workspace/i })).not.toBeInTheDocument()
  })

  it('has a Home link to the website home page (C79)', () => {
    renderNavbar()
    expect(screen.getByRole('link', { name: 'Home' })).toHaveAttribute('href', '/')
  })

  it('signing out opens the website home page (C79)', async () => {
    const logout = vi.fn()
    mockUseAuth.mockReturnValue({ isAuthenticated: true, isAdmin: false, user: { username: 'ada@example.com' }, logout })
    function Where() { return <p>at {useLocation().pathname}</p> }
    render(
      <MemoryRouter initialEntries={['/register/verify']}>
        <ThemeModeProvider>
          <LocalePreferenceProvider>
            <SiteNavbar />
            <Routes><Route path="*" element={<Where />} /></Routes>
          </LocalePreferenceProvider>
        </ThemeModeProvider>
      </MemoryRouter>
    )
    const user = userEvent.setup()
    await user.click(screen.getByRole('button', { name: 'Sign out' }))
    expect(logout).toHaveBeenCalled()
    expect(screen.getByText('at /')).toBeInTheDocument()
  })
})
