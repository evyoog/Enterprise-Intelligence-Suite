import { useEffect, useRef, useState, type FormEvent, type KeyboardEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'
import { X } from 'lucide-react'
import { ApiError } from '../../api/client'
import { samlLoginApi } from '../../api/samlApi'
import { useAuth } from '../../auth/AuthProvider'
import { MfaSignInEnrollment } from './MfaSignInEnrollment'
import '../../styles/landing.css'

export type AuthMode = 'login' | null

interface AuthModalProps {
  mode: AuthMode
  onClose: () => void
  // Phase 5 (2026.3.3): set when the modal is opened right after a failed
  // SAML sign-in redirected back here with ?ssoError=... (see
  // AuthModalContext) — opens straight into the SSO step showing that error,
  // rather than the normal email/password form.
  initialSsoError?: string | null
  // C29: set when a SAML sign-in redirected back with ?mfaChallenge= or
  // ?mfaEnroll= (see AuthModalContext) — opens straight into that step.
  initialMfaStep?: { kind: 'verify' | 'enroll'; challengeId: string } | null
}

const FOCUSABLE_SELECTOR = 'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'

/**
 * Own-branded login: the credential form here is what actually reaches
 * Keycloak (via the backend's ROPC call, see AuthController) — there is no
 * Keycloak-hosted page involved, ever, per the SSO redesign (see CLAUDE.md's
 * SSO decisions). Registration is real, routed pages now (see
 * pages/register/) — this modal only ever handles login.
 *
 * Phase 22: a real focus trap (Tab/Shift+Tab wrap inside the dialog, Escape
 * closes it, focus moves to the first field on open and back to whatever
 * opened it on close) — previously the only accessibility touch here was one
 * aria-label on the close button.
 */
export function AuthModal({ mode, onClose, initialSsoError, initialMfaStep }: AuthModalProps) {
  const auth = useAuth()
  const navigate = useNavigate()
  const { t } = useTranslation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)
  // Phase 5 (2026.3.3): "Sign in with organization SSO" — a completely
  // separate mini-form (just an organization code) that ends in a real
  // browser navigation to the backend's login-init redirect, never a fetch
  // (see samlLoginApi's own doc for why).
  const [ssoStep, setSsoStep] = useState(false)
  const [orgCode, setOrgCode] = useState('')
  const [ssoSubmitting, setSsoSubmitting] = useState(false)
  const [ssoError, setSsoError] = useState<string | null>(null)
  // Phase 7: email+password were right, but this account also needs its
  // authenticator code — the backend's ApiError.detail.mfaRequired flag
  // (see AuthProvider.login) is what flips this, never a guess client-side.
  const [mfaStep, setMfaStep] = useState(false)
  const [totp, setTotp] = useState('')
  // Phase 2 (2026.3.3): a DIFFERENT, later gate than the one above — Keycloak's
  // own grant already succeeded, but this customer also has Platform MFA
  // enabled (see ApiError.detail.platformMfaRequired/mfaChallengeId). A
  // non-null challenge id is what drives this step; the code entered here is
  // verified via a completely separate call (auth.verifyMfaChallenge), never
  // by resubmitting email+password.
  const [platformMfaChallengeId, setPlatformMfaChallengeId] = useState<string | null>(null)
  const [platformMfaCode, setPlatformMfaCode] = useState('')
  // C29: the organization requires MFA and this member has no authenticator
  // yet — set one up before the held sign-in becomes a session.
  const [enrollChallengeId, setEnrollChallengeId] = useState<string | null>(null)
  // Adjust state when a new SAML hand-off arrives (React's "reset state on
  // prop change" pattern, rather than an effect).
  const [seenMfaStep, setSeenMfaStep] = useState<AuthModalProps['initialMfaStep']>(null)
  if (initialMfaStep && initialMfaStep !== seenMfaStep) {
    setSeenMfaStep(initialMfaStep)
    if (initialMfaStep.kind === 'enroll') setEnrollChallengeId(initialMfaStep.challengeId)
    else setPlatformMfaChallengeId(initialMfaStep.challengeId)
  }

  const dialogRef = useRef<HTMLDivElement>(null)
  const previouslyFocused = useRef<HTMLElement | null>(null)
  const firstFieldRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    if (mode === null) return
    previouslyFocused.current = document.activeElement as HTMLElement | null
    firstFieldRef.current?.focus()
    return () => {
      previouslyFocused.current?.focus?.()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [mode])


  useEffect(() => {
    if (initialSsoError) {
      setSsoStep(true)
      setSsoError(initialSsoError)
    }
  }, [initialSsoError])

  if (mode === null) return null

  const reset = () => {
    setEmail('')
    setPassword('')
    setTotp('')
    setMfaStep(false)
    setPlatformMfaChallengeId(null)
    setPlatformMfaCode('')
    setEnrollChallengeId(null)
    setFormError(null)
    setSsoStep(false)
    setOrgCode('')
    setSsoError(null)
  }

  const handleSsoSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setSsoError(null)
    setSsoSubmitting(true)
    try {
      const result = await samlLoginApi.ssoCheck(orgCode.trim())
      if (result.available && result.organizationId) {
        // A real navigation, not a fetch — the backend responds with an
        // actual redirect straight to the organization's own identity
        // provider (see samlLoginApi.loginInitUrl's own doc).
        window.location.href = samlLoginApi.loginInitUrl(result.organizationId)
        return
      }
      setSsoError(t('auth.ssoNotAvailable'))
    } catch {
      setSsoError(t('auth.ssoNotAvailable'))
    } finally {
      setSsoSubmitting(false)
    }
  }

  const afterLogin = (roles: string[]) => {
    onClose()
    reset()
    // Platform admins land on the admin console; everyone else lands on
    // their dashboard (org admins get the business dashboard, regular
    // members get bounced to their personal one — see
    // BusinessDashboardPage's own 403 handling) rather than the public
    // storefront, which is for browsing, not what you see right after
    // signing in.
    navigate(roles.includes('ADMIN') ? '/admin' : '/organization/business-dashboard')
  }

  const handleLogin = async (e: FormEvent) => {
    e.preventDefault()
    setFormError(null)
    setSubmitting(true)
    try {
      if (platformMfaChallengeId) {
        // Second half of a login the backend parked on a Platform MFA
        // challenge — a completely different call than login(), never a
        // resubmission of email+password (see AuthProvider.verifyMfaChallenge).
        const roles = await auth.verifyMfaChallenge(platformMfaChallengeId, platformMfaCode)
        afterLogin(roles)
        return
      }
      // login() resolves with this login's own fresh roles — using that
      // directly (not auth.isAdmin) avoids navigating on stale, pre-login
      // context state from before this render's state update lands.
      const roles = await auth.login(email, password, mfaStep ? totp : undefined)
      afterLogin(roles)
    } catch (err) {
      if (err instanceof ApiError && err.detail?.platformMfaEnrollmentRequired
          && typeof err.detail.mfaEnrollmentChallengeId === 'string') {
        setEnrollChallengeId(err.detail.mfaEnrollmentChallengeId)
        setFormError(null)
      } else if (err instanceof ApiError && err.detail?.platformMfaRequired && typeof err.detail.mfaChallengeId === 'string') {
        setPlatformMfaChallengeId(err.detail.mfaChallengeId)
        setFormError(null)
      } else if (err instanceof ApiError && err.detail?.mfaRequired) {
        // Same call, same credentials — just needs the code this time.
        // Password isn't re-typed, so keep it in state rather than clearing it.
        setMfaStep(true)
        setFormError(null)
      } else {
        setFormError(auth.error ?? t('auth.invalidCredentials'))
      }
    } finally {
      setSubmitting(false)
    }
  }

  const handleDialogKeyDown = (e: KeyboardEvent<HTMLDivElement>) => {
    if (e.key === 'Escape') {
      e.stopPropagation()
      onClose()
      return
    }
    if (e.key !== 'Tab' || !dialogRef.current) return

    const focusable = Array.from(dialogRef.current.querySelectorAll<HTMLElement>(FOCUSABLE_SELECTOR))
      .filter((el) => !el.hasAttribute('disabled'))
    if (focusable.length === 0) return

    const first = focusable[0]
    const last = focusable[focusable.length - 1]
    if (e.shiftKey && document.activeElement === first) {
      e.preventDefault()
      last.focus()
    } else if (!e.shiftKey && document.activeElement === last) {
      e.preventDefault()
      first.focus()
    }
  }

  return (
    <div className="vyoog-landing">
      <div className="modal open" onClick={(e) => { if (e.target === e.currentTarget) onClose() }}>
        <div
          className="modal-box auth-wrap"
          ref={dialogRef}
          role="dialog"
          aria-modal="true"
          aria-labelledby="auth-modal-title"
          onKeyDown={handleDialogKeyDown}
        >
          <button className="modal-close" onClick={onClose} type="button" aria-label={t('auth.close')}>
            <X size={22} />
          </button>

          <div className="section-label">SIGN IN</div>
          <h2 id="auth-modal-title">{t('auth.welcomeBack')}</h2>
          <p>
            {enrollChallengeId
              ? t('auth.enrollSubtitle')
              : ssoStep
              ? t('auth.ssoSubtitle')
              : platformMfaChallengeId ? t('auth.platformMfaSubtitle') : mfaStep ? t('auth.mfaSubtitle') : t('auth.signInSubtitle')}
          </p>

          {enrollChallengeId && (
            <MfaSignInEnrollment challengeId={enrollChallengeId} onDone={afterLogin} onCancel={reset} />
          )}

          {!enrollChallengeId && ssoStep && (
            <form onSubmit={handleSsoSubmit}>
              <div className="field">
                <label htmlFor="auth-org-code">{t('auth.organizationCode')}</label>
                <input
                  id="auth-org-code"
                  ref={firstFieldRef}
                  type="text"
                  placeholder="ACME-1A2B3C"
                  autoComplete="organization"
                  value={orgCode}
                  onChange={(e) => setOrgCode(e.target.value)}
                  required
                />
              </div>

              {ssoError && <div className="auth-note auth-note-error" role="alert">{ssoError}</div>}

              <button className="auth-submit" type="submit" disabled={ssoSubmitting || !orgCode.trim()}>
                {ssoSubmitting ? t('auth.ssoChecking') : t('auth.ssoContinue')}
              </button>

              <div className="switch">
                <button type="button" onClick={() => { reset() }}>
                  {t('auth.backToSignIn')}
                </button>
              </div>
            </form>
          )}

          {!enrollChallengeId && !ssoStep && (
          <form onSubmit={handleLogin}>
            {!mfaStep && !platformMfaChallengeId && (
              <>
                <div className="field">
                  <label htmlFor="auth-email">{t('auth.emailAddress')}</label>
                  <input
                    id="auth-email"
                    ref={firstFieldRef}
                    type="email"
                    placeholder="you@company.com"
                    autoComplete="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>
                <div className="field">
                  <label htmlFor="auth-password">
                    {t('auth.password')}
                    <button
                      type="button"
                      className="link-button"
                      style={{ float: 'right', fontWeight: 400 }}
                      onClick={() => { onClose(); reset(); navigate('/forgot-password') }}
                    >
                      {t('auth.forgotPassword')}
                    </button>
                  </label>
                  <input
                    id="auth-password"
                    type="password"
                    placeholder="••••••••"
                    autoComplete="current-password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </div>
              </>
            )}

            {mfaStep && (
              <div className="field">
                <label htmlFor="auth-totp">{t('auth.verificationCode')}</label>
                <input
                  id="auth-totp"
                  type="text"
                  inputMode="numeric"
                  autoComplete="one-time-code"
                  placeholder="123456"
                  value={totp}
                  onChange={(e) => setTotp(e.target.value)}
                  autoFocus
                  required
                />
              </div>
            )}

            {platformMfaChallengeId && (
              <div className="field">
                <label htmlFor="auth-platform-mfa">{t('auth.verificationCode')}</label>
                <input
                  id="auth-platform-mfa"
                  type="text"
                  autoComplete="one-time-code"
                  placeholder="123456"
                  value={platformMfaCode}
                  onChange={(e) => setPlatformMfaCode(e.target.value)}
                  autoFocus
                  required
                />
                <p className="field-hint">{t('auth.platformMfaHint')}</p>
              </div>
            )}

            {formError && <div className="auth-note auth-note-error" role="alert">{formError}</div>}

            <button className="auth-submit" type="submit" disabled={submitting}>
              {submitting ? t('auth.signingIn') : (mfaStep || platformMfaChallengeId) ? t('auth.verify') : t('auth.signIn')}
            </button>

            {mfaStep && (
              <div className="switch">
                <button type="button" onClick={() => { setMfaStep(false); setTotp(''); setFormError(null) }}>
                  {t('auth.backToSignIn')}
                </button>
              </div>
            )}

            {platformMfaChallengeId && (
              <div className="switch">
                <button type="button" onClick={() => { reset() }}>
                  {t('auth.backToSignIn')}
                </button>
              </div>
            )}
          </form>
          )}

          {!enrollChallengeId && !ssoStep && !mfaStep && !platformMfaChallengeId && (
            <div className="switch">
              <button type="button" onClick={() => { setSsoStep(true); setFormError(null) }}>
                {t('auth.useOrganizationSso')}
              </button>
            </div>
          )}

          {!enrollChallengeId && !ssoStep && !mfaStep && !platformMfaChallengeId && (
            <div className="switch">
              {t('auth.noAccount')}{' '}
              <button type="button" onClick={() => { onClose(); reset(); navigate('/register') }}>{t('auth.createOne')}</button>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
