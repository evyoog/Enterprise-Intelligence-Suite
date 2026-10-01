import { useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../auth/AuthProvider'

/** C59 (REQ-MKT-003.1): every Buy / Subscribe on a plan goes to the cart,
 * which adds the item (or keeps the free-plan behaviour for a product with
 * no paid plan). A signed-out visitor signs in first and comes back to the
 * same add (REQ-MKT-003 Open question 5: no anonymous cart). */
export function cartAddPath(productId: number, planId?: number) {
  return `/cart?add=${productId}${planId != null ? `&plan=${planId}` : ''}`
}

export function useBuy() {
  const navigate = useNavigate()
  const { isAuthenticated } = useAuth()
  return useCallback((productId: number, planId?: number) => {
    const path = cartAddPath(productId, planId)
    navigate(isAuthenticated ? path : `/login?returnTo=${encodeURIComponent(path)}`)
  }, [navigate, isAuthenticated])
}
