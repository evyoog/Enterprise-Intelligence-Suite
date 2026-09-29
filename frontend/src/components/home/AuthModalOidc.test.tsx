import '../../i18n'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { AuthModal } from './AuthModal'

const ssoCheck = vi.fn()
vi.mock('../../auth/AuthProvider', () => ({ useAuth: () => ({ login: vi.fn(), verifyMfaChallenge: vi.fn(), error: null }) }))
vi.mock('../../api/samlApi', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../../api/samlApi')>()
  return { ...actual, samlLoginApi: { ...actual.samlLoginApi, ssoCheck: (code: string) => ssoCheck(code) } }
})

// REQ-IAM-006 (C27): the organization code starts OIDC sign-in when OIDC is the enabled protocol.
describe('AuthModal — organization SSO picks SAML or OIDC', () => {
  const original = window.location
  let assigned = ''
  beforeEach(() => {
    ssoCheck.mockReset()
    assigned = ''
    Object.defineProperty(window, 'location', {
      configurable: true,
      value: { ...original, set href(v: string) { assigned = v }, get href() { return assigned } },
    })
  })
  afterEach(() => { Object.defineProperty(window, 'location', { configurable: true, value: original }) })

  it.each([['OIDC', '/oidc/42/login-init'], ['SAML', '/saml/42/login-init']])('navigates to the %s login-init', async (protocol, path) => {
    ssoCheck.mockResolvedValue({ available: true, organizationId: 42, organizationName: 'Acme', protocol })
    render(<MemoryRouter><AuthModal mode="login" onClose={vi.fn()} /></MemoryRouter>)

    const user = userEvent.setup()
    await user.click(screen.getByRole('button', { name: "Sign in with your organization's SSO instead" }))
    await user.type(screen.getByLabelText('Organization code'), 'ACME')
    await user.click(screen.getByRole('button', { name: 'Continue' }))

    await vi.waitFor(() => expect(assigned).toContain(path))
  })
})
