import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../../auth/AuthProvider'

/**
 * Phase 7 (2026.3.3): route guard for pages that only make sense once
 * signed in (dashboard, security, identity federation). Same posture as
 * {@link ../routing/RequireAdmin}: a UX convenience that sends the wrong
 * audience somewhere sensible, NOT the security boundary — every endpoint
 * these pages call is already gated server-side by SecurityConfig's own
 * {@code /me/**}/{@code /organization/me/**} authenticated() rules, so a
 * visitor who bypassed this component entirely still couldn't see or change
 * anything real.
 *
 * Before this existed, an unauthenticated visitor who opened one of these
 * URLs directly (a stale bookmark, a shared link, a reload after their
 * session expired) saw that page's own generic "Could not load…" error
 * state — technically safe, but a confusing dead end instead of a clear path
 * back to signing in. Deliberately NOT applied to every internal page —
 * {@code /account/preferences} stays reachable signed out on purpose (theme/
 * language are useful before an account even exists; see PreferenceSync's
 * own doc for how it degrades to local-only there).
 */
export function RequireAuth({ children }: { children: ReactNode }) {
  const auth = useAuth()

  if (!auth.isAuthenticated) {
    return <Navigate to="/" replace />
  }

  return children
}
