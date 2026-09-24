import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../../auth/AuthProvider'

/**
 * Route guard for /admin/*. This is a UX convenience (send the wrong audience
 * somewhere sensible), NOT the security boundary — that's SecurityConfig's
 * hasRole("ADMIN") check on the backend. A logged-out user or non-admin who
 * bypasses this component entirely still can't do anything the backend
 * wouldn't already reject.
 */
export function RequireAdmin({ children }: { children: ReactNode }) {
  const auth = useAuth()

  if (!auth.isAuthenticated) {
    return <Navigate to="/" replace />
  }

  if (!auth.isAdmin) {
    return <Navigate to="/products" replace />
  }

  return children
}
