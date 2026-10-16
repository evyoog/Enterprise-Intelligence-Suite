import { useEffect, useState } from 'react'
import { myPermissionsApi } from '../api/myPermissionsApi'
import { useAuth } from './AuthProvider'

/** One lookup per signed-in user, shared by every component that asks (cart, wishlist, bell, preferences, route guard). */
const lookups = new Map<string, Promise<boolean>>()

function hasWorkspace(username: string): Promise<boolean> {
  let lookup = lookups.get(username)
  if (!lookup) {
    // Same rule as the navigation's { customer: true }: a platform admin has a customer workspace only as a member of an organization.
    // If the lookup fails, behave as before (assume a workspace) rather than hide something a customer needs.
    lookup = myPermissionsApi.get().then((p) => p.organization.length > 0).catch(() => {
      lookups.delete(username)
      return true
    })
    lookups.set(username, lookup)
  }
  return lookup
}

/** Forgets the cached answers, so the next sign-in asks again. */
export function resetCustomerWorkspaceCache() {
  lookups.clear()
}

/**
 * Whether the signed-in account has a customer workspace: a row in `customer` that the customer-only endpoints (/me/cart, /me/wishlist,
 * /me/notifications, /me/preferences, ...) can answer for. The platform admin (apps@vyoog.com) exists only in Keycloak and has none, so those
 * endpoints answer 404 "No Vyoog account is linked to this login". `null` while it is being worked out (or when signed out); `true` for every
 * non-admin account at once.
 */
export function useCustomerWorkspace(): boolean | null {
  const { isAuthenticated, isAdmin, user } = useAuth()
  const username = user?.username ?? null
  const [answer, setAnswer] = useState<{ username: string; workspace: boolean } | null>(null)

  useEffect(() => {
    if (!isAuthenticated || !isAdmin || !username) return
    let active = true
    hasWorkspace(username).then((workspace) => { if (active) setAnswer({ username, workspace }) })
    return () => { active = false }
  }, [isAuthenticated, isAdmin, username])

  if (!isAuthenticated) return null
  if (!isAdmin) return true
  return answer?.username === username ? answer.workspace : null
}
