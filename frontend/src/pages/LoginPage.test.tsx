import '../i18n'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { LoginPage } from './LoginPage'

const mockLogin = vi.fn()
let authState = { isAuthenticated: false }
vi.mock('../auth/AuthProvider', () => ({
  useAuth: () => ({ ...authState, login: mockLogin, verifyMfaChallenge: vi.fn(), error: null }),
}))

vi.mock('../api/samlApi', () => ({
  samlLoginApi: {
    ssoCheck: vi.fn().mockResolvedValue({ available: false, organizationId: null, organizationName: null }),
    loginInitUrl: (id: number) => `http://localhost:8081/api/saml/${id}/login-init`,
  },
}))

function renderAt(path: string) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/checkout/:productId" element={<div>Checkout page</div>} />
        <Route path="/register" element={<div>Register page</div>} />
        <Route path="/" element={<div>Home page</div>} />
        <Route path="/admin" element={<div>Admin console</div>} />
        <Route path="/organization/business-dashboard" element={<div>Business dashboard</div>} />
      </Routes>
    </MemoryRouter>
  )
}

describe('LoginPage', () => {
  beforeEach(() => {
    mockLogin.mockReset()
    authState = { isAuthenticated: false }
  })

  it('renders as a real page (not a dialog) with no detectable a11y violations', async () => {
    const { container } = renderAt('/login')
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(await screen.findByLabelText(/email address/i)).toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('offers new-user navigation to create an account', async () => {
    renderAt('/login')
    expect(await screen.findByText(/create one/i)).toBeInTheDocument()
  })

  it('carries a returnTo destination through to Create account', async () => {
    renderAt('/login?returnTo=%2Fcheckout%2F5')
    const user = userEvent.setup()
    await user.click(await screen.findByText(/create one/i))
    expect(await screen.findByText('Register page')).toBeInTheDocument()
  })

  it('redirects to returnTo after a successful sign-in', async () => {
    mockLogin.mockResolvedValue(['MEMBER'])
    renderAt('/login?returnTo=%2Fcheckout%2F5')
    const user = userEvent.setup()
    await user.type(await screen.findByLabelText(/email address/i), 'jane@example.com')
    await user.type(document.getElementById('auth-password') as HTMLElement, 'correct-password')
    await user.click(screen.getByRole('button', { name: 'Sign in' }))
    expect(await screen.findByText('Checkout page')).toBeInTheDocument()
  })

  it('redirects an already-signed-in visitor straight to returnTo', async () => {
    authState = { isAuthenticated: true }
    renderAt('/login?returnTo=%2Fcheckout%2F5')
    expect(await screen.findByText('Checkout page')).toBeInTheDocument()
  })
})
