import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { AuthModal, type AuthMode } from '../components/home/AuthModal'

interface AuthModalState {
  openLogin: () => void
  openRegister: () => void
  close: () => void
}

const AuthModalContext = createContext<AuthModalState | null>(null)

/**
 * Login modal state, shared app-wide — the Home hero's CTAs, the Navbar's
 * "Login" button, and the pricing section's CTAs all need to open the exact
 * same modal instance rather than each page rendering its own copy.
 * Registration is real, routed pages now (a multi-step organization wizard
 * doesn't fit a modal) — openRegister navigates there instead of opening
 * anything, kept on this same context only so every existing call site
 * doesn't need to know the difference.
 */
export function AuthModalProvider({ children }: { children: ReactNode }) {
  const [mode, setMode] = useState<AuthMode>(null)
  const [ssoError, setSsoError] = useState<string | null>(null)
  const navigate = useNavigate()

  const openLogin = useCallback(() => setMode('login'), [])
  const openRegister = useCallback(() => navigate('/register'), [navigate])
  const close = useCallback(() => setMode(null), [])

  // Phase 5 (2026.3.3): a failed SAML sign-in redirects the browser back to
  // "/?ssoError=..." (see SamlLoginController) — there's no running SPA
  // state at that point in the flow to catch a JSON error, only this: open
  // the login modal straight into its SSO step showing the real reason, and
  // strip the param so a reload/share of the URL doesn't repeat it.
  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    const error = params.get('ssoError')
    if (!error) return
    setSsoError(error)
    setMode('login')
    params.delete('ssoError')
    const query = params.toString()
    window.history.replaceState({}, '', window.location.pathname + (query ? `?${query}` : '') + window.location.hash)
  }, [])

  const value = useMemo(() => ({ openLogin, openRegister, close }), [openLogin, openRegister, close])

  return (
    <AuthModalContext.Provider value={value}>
      {children}
      <AuthModal mode={mode} onClose={close} initialSsoError={ssoError} />
    </AuthModalContext.Provider>
  )
}

export function useAuthModal(): AuthModalState {
  const ctx = useContext(AuthModalContext)
  if (!ctx) throw new Error('useAuthModal() called outside <AuthModalProvider>')
  return ctx
}
