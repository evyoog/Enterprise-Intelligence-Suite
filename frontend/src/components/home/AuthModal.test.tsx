import '../../i18n'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AuthModal } from './AuthModal'

const mockLogin = vi.fn()
const mockNavigate = vi.fn()

vi.mock('../../auth/AuthProvider', () => ({
  useAuth: () => ({
    login: mockLogin,
    verifyMfaChallenge: vi.fn(),
    error: null,
  }),
}))

vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal<typeof import('react-router-dom')>()
  return { ...actual, useNavigate: () => mockNavigate }
})

vi.mock('../../api/samlApi', () => ({
  samlLoginApi: {
    ssoCheck: vi.fn().mockResolvedValue({ available: false, organizationId: null, organizationName: null }),
    loginInitUrl: (id: number) => `http://localhost:8081/api/saml/${id}/login-init`,
  },
}))

function renderModal(onClose = vi.fn()) {
  return render(
    <MemoryRouter>
      <AuthModal mode="login" onClose={onClose} />
    </MemoryRouter>
  )
}

describe('AuthModal — dialog semantics and keyboard behavior', () => {
  beforeEach(() => {
    mockLogin.mockReset()
    mockNavigate.mockReset()
  })

  it('renders as a real, labelled dialog with no detectable a11y violations', async () => {
    const { container } = renderModal()
    const dialog = screen.getByRole('dialog')
    expect(dialog).toHaveAttribute('aria-modal', 'true')
    expect(dialog).toHaveAttribute('aria-labelledby', 'auth-modal-title')

    const results = await axe(container)
    expect(results).toHaveNoViolations()
  })

  it('moves focus to the first field when opened', async () => {
    renderModal()
    await waitFor(() => expect(screen.getByLabelText(/email address/i)).toHaveFocus())
  })

  it('closes on Escape', async () => {
    const onClose = vi.fn()
    const user = userEvent.setup()
    renderModal(onClose)
    await user.keyboard('{Escape}')
    expect(onClose).toHaveBeenCalledTimes(1)
  })

  it('traps Tab within the dialog — Shift+Tab from the first field wraps to the last focusable element', async () => {
    const user = userEvent.setup()
    renderModal()
    const emailField = await screen.findByLabelText(/email address/i)
    expect(emailField).toHaveFocus()

    await user.tab({ shift: true })
    // Whatever the last focusable element is, it must stay inside the dialog
    // (a real focus trap), never escape to document.body.
    expect(document.activeElement).not.toBe(document.body)
    expect(screen.getByRole('dialog')).toContainElement(document.activeElement as HTMLElement)
  })

  it('switches to the organization-SSO step via keyboard and shows an organization-code field', async () => {
    const user = userEvent.setup()
    renderModal()
    const ssoToggle = screen.getByText(/organization's SSO/i)
    ssoToggle.focus()
    await user.keyboard('{Enter}')
    expect(await screen.findByLabelText(/organization code/i)).toBeInTheDocument()
  })

  it('announces a login failure so a screen reader user hears it', async () => {
    mockLogin.mockRejectedValue(new Error('nope'))
    const user = userEvent.setup()
    renderModal()

    await user.type(screen.getByLabelText(/email address/i), 'someone@example.com')
    await user.type(document.getElementById('auth-password') as HTMLElement, 'wrong-password')
    await user.click(screen.getByRole('button', { name: 'Sign in' }))

    const alert = await screen.findByRole('alert')
    expect(alert).toBeInTheDocument()
  })
})
