import { useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from './AuthProvider'

/** Sign out, then open the public website's home page (C79), wherever the
 * user signed out from. */
export function useSignOut() {
  const auth = useAuth()
  const navigate = useNavigate()
  return useCallback(() => {
    auth.logout()
    navigate('/', { replace: true })
  }, [auth, navigate])
}
