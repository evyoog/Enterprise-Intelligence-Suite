import { useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from './AuthProvider'
import { forgetRecentSearches } from '../components/knowledge/knowledgeUtils'

/**
 * C79: whenever a session ends (Sign out here, sign-out in another tab or in
 * PMS, or expiry), open the website home page and forget what this browser
 * kept for that person, so none of their data stays on screen. Renders
 * nothing.
 */
export function SignedOutRedirect() {
  const { isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const wasSignedIn = useRef(isAuthenticated)

  useEffect(() => {
    if (wasSignedIn.current && !isAuthenticated) {
      forgetRecentSearches()
      navigate('/', { replace: true })
    }
    wasSignedIn.current = isAuthenticated
  }, [isAuthenticated, navigate])

  return null
}
