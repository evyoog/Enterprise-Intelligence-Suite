import { useEffect, useRef, useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { ApiError } from '../../api/client'
import { samlLoginApi } from '../../api/samlApi'
import { useAuth } from '../../auth/AuthProvider'
import { MfaSignInEnrollment } from './MfaSignInEnrollment'

export interface AuthCredentialsFormProps {
  // Phase 5 (2026.3.3): set when a failed SAML sign-in redirected back here
  // with ?ssoError=... — opens straight into the SSO step showing that error.
  initialSsoError?: string | null
  // C29: set when a SAML sign-in redirected back with ?mfaChallenge= or
  // ?mfaEnroll= — opens straight into that step.
  initialMfaStep?: { kind: 'verify' | 'enroll'; challengeId: string } | null
  /** Called once sign-in (including any MFA step) actually succeeds, with
   * this login's own fresh roles — the caller decides where to go next
   * (the modal returns to whatever page was already open; the full-page
   * sign-in redirects to wherever the visitor was headed before). */
  onSuccess: (roles: string[]) => void
  onForgotPassword: () => void
  onCreateAccount: () => void
}

/**
 * The actual sign-in form (credentials, organization SSO, TOTP/platform-MFA
 * steps, MFA enrollment) — every place a Vyoog customer can authenticate
 * renders this same component, whether inside the modal ({@link AuthModal},
 * opened from in-app "Login" links) or on a full page ({@code LoginPage},
 * used when a specific action like "Subscribe" needs to come back to
 * somewhere other than the default post-login destination). Extracted from
 * AuthModal (previously the only caller) so this non-trivial logic — MFA
 * challenges, SSO, enrollment — exists exactly once.
 */
export function AuthCredentialsForm({ initialSsoError, initialMfaStep, onSuccess, onForgotPassword, onCreateAccount }: AuthCredentialsFormProps) {
  const auth = useAuth()
  const { t } = useTranslation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)
  const [ssoStep, setSsoStep] = useState(false)
  const [orgCode, setOrgCode] = useState('')
  const [ssoSubmitting, setSsoSubmitting] = useState(false)
  const [ssoError, setSsoError] = useState<string | null>(null)
  const [mfaStep, setMfaStep] = useState(false)
  const [totp, setTotp] = useState('')
  const [platformMfaChallengeId, setPlatformMfaChallengeId] = useState<string | null>(
    initialMfaStep?.kind === 'verify' ? initialMfaStep.challengeId : null
  )
  const [platformMfaCode, setPlatformMfaCode] = useState('')
  const [enrollChallengeId, setEnrollChallengeId] = useState<string | null>(
    initialMfaStep?.kind === 'enroll' ? initialMfaStep.challengeId : null
  )

  const firstFieldRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    firstFieldRef.current?.focus()
    if (initialSsoError) setSsoStep(true)
    // Mount-only: this component is remounted fresh each time it's shown
    // (the modal returns null when closed; a page navigation remounts it).
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

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
        window.location.href = samlLoginApi.loginInitUrl(result.organizationId, result.protocol)
        return
      }
      setSsoError(t('auth.ssoNotAvailable'))
    } catch {
      setSsoError(t('auth.ssoNotAvailable'))
    } finally {
      setSsoSubmitting(false)
    }
  }

  const handleLogin = async (e: FormEvent) => {
    e.preventDefault()
    setFormError(null)
    setSubmitting(true)
    try {
      if (platformMfaChallengeId) {
        const roles = await auth.verifyMfaChallenge(platformMfaChallengeId, platformMfaCode)
        onSuccess(roles)
        return
      }
      const roles = await auth.login(email, password, mfaStep ? totp : undefined)
      onSuccess(roles)
    } catch (err) {
      if (err instanceof ApiError && err.detail?.platformMfaEnrollmentRequired
          && typeof err.detail.mfaEnrollmentChallengeId === 'string') {
        setEnrollChallengeId(err.detail.mfaEnrollmentChallengeId)
        setFormError(null)
      } else if (err instanceof ApiError && err.detail?.platformMfaRequired && typeof err.detail.mfaChallengeId === 'string') {
        setPlatformMfaChallengeId(err.detail.mfaChallengeId)
        setFormError(null)
      } else if (err instanceof ApiError && err.detail?.mfaRequired) {
        setMfaStep(true)
        setFormError(null)
      } else {
        setFormError(auth.error ?? t('auth.invalidCredentials'))
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <>
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
        <MfaSignInEnrollment challengeId={enrollChallengeId} onDone={onSuccess} onCancel={reset} />
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
                  onClick={onForgotPassword}
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
          <button type="button" onClick={onCreateAccount}>{t('auth.createOne')}</button>
        </div>
      )}
    </>
  )
}
