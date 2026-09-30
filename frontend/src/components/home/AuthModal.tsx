import { useEffect, useRef, type KeyboardEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'
import { X } from 'lucide-react'
import { AuthCredentialsForm } from './AuthCredentialsForm'
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
 * pages/register/) — this modal only ever handles login. The actual form
 * (credentials/SSO/MFA/enrollment) is {@link AuthCredentialsForm} — also
 * used by the full-page {@code LoginPage} (C48) for flows that need to
 * return somewhere specific after signing in (e.g. "Subscribe" while
 * signed out), rather than the modal's fixed post-login destination.
 *
 * Phase 22: a real focus trap (Tab/Shift+Tab wrap inside the dialog, Escape
 * closes it, focus moves to the first field on open and back to whatever
 * opened it on close) — previously the only accessibility touch here was one
 * aria-label on the close button.
 */
export function AuthModal({ mode, onClose, initialSsoError, initialMfaStep }: AuthModalProps) {
  const navigate = useNavigate()
  const { t } = useTranslation()

  const dialogRef = useRef<HTMLDivElement>(null)
  const previouslyFocused = useRef<HTMLElement | null>(null)

  useEffect(() => {
    if (mode === null) return
    previouslyFocused.current = document.activeElement as HTMLElement | null
    return () => {
      previouslyFocused.current?.focus?.()
    }
  }, [mode])

  if (mode === null) return null

  const afterLogin = (roles: string[]) => {
    onClose()
    // Platform admins land on the admin console; everyone else lands on
    // their dashboard (org admins get the business dashboard, regular
    // members get bounced to their personal one — see
    // BusinessDashboardPage's own 403 handling) rather than the public
    // storefront, which is for browsing, not what you see right after
    // signing in.
    navigate(roles.includes('ADMIN') ? '/admin' : '/organization/business-dashboard')
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

          <AuthCredentialsForm
            initialSsoError={initialSsoError}
            initialMfaStep={initialMfaStep}
            onSuccess={afterLogin}
            onForgotPassword={() => { onClose(); navigate('/forgot-password') }}
            onCreateAccount={() => { onClose(); navigate('/register') }}
          />
        </div>
      </div>
    </div>
  )
}
