import { useEffect, useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { ApiError, authApi, type SignInEnrollmentStart } from '../../api/authApi'
import { useAuth } from '../../auth/AuthProvider'

/**
 * C29 (2026-09-26, REQ-IAM-001): the organization requires two-factor
 * authentication and this member has no authenticator yet, so the sign-in is
 * held by the backend until they set one up here. Shows the QR code and
 * manual key, takes the first code, then shows the recovery codes once before
 * the session is used. Rendered inside AuthModal (landing.css styles).
 */
export function MfaSignInEnrollment({ challengeId, onDone, onCancel }: {
  challengeId: string
  onDone: (roles: string[]) => void
  onCancel: () => void
}) {
  const { t } = useTranslation()
  const auth = useAuth()
  const [setup, setSetup] = useState<SignInEnrollmentStart | null>(null)
  const [code, setCode] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [done, setDone] = useState<{ roles: string[]; recoveryCodes: string[] } | null>(null)

  useEffect(() => {
    let active = true
    authApi.startSignInEnrollment(challengeId)
      .then((s) => { if (active) setSetup(s) })
      .catch((e) => { if (active) setError(e instanceof ApiError ? e.message : t('auth.enrollStartError')) })
    return () => { active = false }
  }, [challengeId, t])

  const submit = async (e: FormEvent) => {
    e.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      setDone(await auth.completeSignInEnrollment(challengeId, code.trim()))
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t('auth.enrollVerifyError'))
    } finally {
      setSubmitting(false)
    }
  }

  if (done) {
    return (
      <div>
        <div className="auth-note" role="status">{t('auth.enrollDone')}</div>
        <p className="field-hint">{t('auth.recoveryCodesHint')}</p>
        <ul className="recovery-codes" aria-label={t('auth.recoveryCodesLabel')}>
          {done.recoveryCodes.map((c) => <li key={c}><code>{c}</code></li>)}
        </ul>
        <button className="auth-submit" type="button" onClick={() => onDone(done.roles)}>
          {t('auth.enrollContinue')}
        </button>
      </div>
    )
  }

  return (
    <form onSubmit={submit}>
      {setup && (
        <div className="field" style={{ textAlign: 'center' }}>
          <img
            src={`data:image/png;base64,${setup.qrCodePngBase64}`}
            alt={t('auth.enrollQrAlt')}
            width={180}
            height={180}
          />
          <p className="field-hint">{t('auth.enrollManualKey')} <code>{setup.secret}</code></p>
        </div>
      )}
      <div className="field">
        <label htmlFor="auth-enroll-code">{t('auth.verificationCode')}</label>
        <input
          id="auth-enroll-code"
          type="text"
          inputMode="numeric"
          autoComplete="one-time-code"
          placeholder="123456"
          value={code}
          onChange={(e) => setCode(e.target.value)}
          autoFocus
          required
        />
      </div>
      {error && <div className="auth-note auth-note-error" role="alert">{error}</div>}
      <button className="auth-submit" type="submit" disabled={submitting || !setup}>
        {submitting ? t('auth.signingIn') : t('auth.enrollSubmit')}
      </button>
      <div className="switch">
        <button type="button" onClick={onCancel}>{t('auth.backToSignIn')}</button>
      </div>
    </form>
  )
}
