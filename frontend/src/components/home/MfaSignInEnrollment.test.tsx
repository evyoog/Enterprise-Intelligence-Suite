import '../../i18n'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { AuthModal } from './AuthModal'

const mockLogin = vi.fn()
const mockComplete = vi.fn()
const mockVerify = vi.fn()
const mockNavigate = vi.fn()
const startEnrollment = vi.fn()

vi.mock('../../auth/AuthProvider', () => ({
  useAuth: () => ({
    login: mockLogin,
    verifyMfaChallenge: mockVerify,
    completeSignInEnrollment: mockComplete,
    error: null,
  }),
}))
vi.mock('../../api/authApi', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../../api/authApi')>()
  return { ...actual, authApi: { ...actual.authApi, startSignInEnrollment: (id: string) => startEnrollment(id) } }
})
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal<typeof import('react-router-dom')>()
  return { ...actual, useNavigate: () => mockNavigate }
})
vi.mock('../../api/samlApi', () => ({
  samlLoginApi: { ssoCheck: vi.fn(), loginInitUrl: (id: number) => `/api/saml/${id}/login-init` },
}))

const setup = { secret: 'JBSWY3DPEHPK3PXP', otpAuthUri: 'otpauth://totp/x', qrCodePngBase64: 'iVBORw0KGgo=' }

function renderModal(initialMfaStep?: { kind: 'verify' | 'enroll'; challengeId: string }) {
  return render(
    <MemoryRouter>
      <AuthModal mode="login" onClose={vi.fn()} initialMfaStep={initialMfaStep} />
    </MemoryRouter>,
  )
}

// C29 (REQ-IAM-001): setting up an authenticator during sign-in.
describe('AuthModal — authenticator set-up during sign-in', () => {
  beforeEach(() => {
    for (const m of [mockLogin, mockComplete, mockVerify, mockNavigate, startEnrollment]) m.mockReset()
    startEnrollment.mockResolvedValue(setup)
  })

  it('switches to set-up when the organization requires MFA, then shows the recovery codes once', async () => {
    mockLogin.mockRejectedValue(new ApiError(401, 'Your organization requires two-factor authentication.', {
      platformMfaEnrollmentRequired: true, mfaEnrollmentChallengeId: 'enroll-1',
    }))
    mockComplete.mockResolvedValue({ roles: [], recoveryCodes: ['AAAA-1111', 'BBBB-2222'] })
    renderModal()

    const user = userEvent.setup()
    await user.type(screen.getByLabelText(/email address/i), 'grace@example.com')
    await user.type(screen.getByPlaceholderText('••••••••'), 'secret')
    await user.click(screen.getByRole('button', { name: 'Sign in' }))

    expect(await screen.findByRole('img', { name: 'QR code for your authenticator app' })).toBeInTheDocument()
    expect(startEnrollment).toHaveBeenCalledWith('enroll-1')
    expect(screen.getByText('JBSWY3DPEHPK3PXP')).toBeInTheDocument()

    await user.type(screen.getByLabelText('Verification code'), '123456')
    await user.click(screen.getByRole('button', { name: 'Verify and sign in' }))
    expect(mockComplete).toHaveBeenCalledWith('enroll-1', '123456')

    const codes = await screen.findByRole('list', { name: 'Recovery codes' })
    expect(codes).toHaveTextContent('AAAA-1111')
    expect(mockNavigate).not.toHaveBeenCalled()
    await user.click(screen.getByRole('button', { name: "I've saved them, continue" }))
    expect(mockNavigate).toHaveBeenCalledWith('/organization/business-dashboard')
  })

  it('opens straight into set-up after a SAML redirect with ?mfaEnroll=', async () => {
    renderModal({ kind: 'enroll', challengeId: 'saml-enroll' })
    expect(await screen.findByRole('img', { name: 'QR code for your authenticator app' })).toBeInTheDocument()
    expect(startEnrollment).toHaveBeenCalledWith('saml-enroll')
    expect(screen.queryByLabelText(/email address/i)).not.toBeInTheDocument()
  })

  it('opens straight into the code step after a SAML redirect with ?mfaChallenge=', async () => {
    mockVerify.mockResolvedValue([])
    renderModal({ kind: 'verify', challengeId: 'saml-verify' })

    const user = userEvent.setup()
    await user.type(await screen.findByLabelText('Verification code'), '654321')
    await user.click(screen.getByRole('button', { name: 'Verify' }))
    expect(mockVerify).toHaveBeenCalledWith('saml-verify', '654321')
  })

  it('shows the backend message for a wrong code and stays on set-up', async () => {
    mockComplete.mockRejectedValue(new ApiError(401, 'Invalid verification code'))
    renderModal({ kind: 'enroll', challengeId: 'e2' })

    const user = userEvent.setup()
    await screen.findByRole('img', { name: 'QR code for your authenticator app' })
    await user.type(screen.getByLabelText('Verification code'), '000000')
    await user.click(screen.getByRole('button', { name: 'Verify and sign in' }))
    expect(await screen.findByText('Invalid verification code')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Verify and sign in' })).toBeInTheDocument()
  })

  it('has no detectable a11y violations on the set-up step', async () => {
    const { container } = renderModal({ kind: 'enroll', challengeId: 'e3' })
    await screen.findByRole('img', { name: 'QR code for your authenticator app' })
    expect(await axe(container)).toHaveNoViolations()
  })
})
