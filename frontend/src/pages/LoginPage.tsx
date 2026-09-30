import { useEffect } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { AuthCredentialsForm } from '../components/home/AuthCredentialsForm'
import { useAuth } from '../auth/AuthProvider'
import '../styles/landing.css'

/** "/login" — a full page, not the modal (`AuthModal`), for flows that need
 * to come back to somewhere specific after signing in: e.g. clicking
 * Subscribe while signed out sends the visitor here with
 * `?returnTo=/checkout/:productId`, and coming back from a real page
 * navigation (rather than a modal staying open over the product page) reads
 * better for what is, underneath, a full purchase flow. The in-app "Login"
 * links elsewhere keep using the modal — this page exists for this one
 * reason, not as a replacement for it. */
export function LoginPage() {
  const navigate = useNavigate()
  const auth = useAuth()
  const [searchParams] = useSearchParams()
  const returnTo = searchParams.get('returnTo')

  useEffect(() => {
    if (auth.isAuthenticated) {
      navigate(returnTo ?? '/', { replace: true })
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const afterLogin = (roles: string[]) => {
    navigate(returnTo ?? (roles.includes('ADMIN') ? '/admin' : '/organization/business-dashboard'), { replace: true })
  }

  const registerHref = returnTo ? `/register?returnTo=${encodeURIComponent(returnTo)}` : '/register'

  return (
    <div className="vyoog-landing">
      <div className="auth-page">
        <div style={{ width: 'min(430px, 100%)' }}>
          <Link to="/" className="auth-page-back">← Back to eVyoog</Link>
          <div className="modal-box auth-wrap" role="region" aria-labelledby="auth-modal-title">
            <AuthCredentialsForm
              onSuccess={afterLogin}
              onForgotPassword={() => navigate('/forgot-password')}
              onCreateAccount={() => navigate(registerHref)}
            />
          </div>
        </div>
      </div>
    </div>
  )
}
