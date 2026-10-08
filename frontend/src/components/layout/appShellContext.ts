import type { MyPermissions } from '../../api/myPermissionsApi'
import { createContext, useContext } from 'react'

/** True inside the signed-in AppShell. Pages that are also public (catalog,
 * preferences) use it to leave out the public website header. */
export const AppShellContext = createContext(false)

export function useInAppShell() {
  return useContext(AppShellContext)
}

/** The signed-in user's permissions as the sidebar sees them (C80), so pages
 * can show or hide sections the same way. `permissions` is null while loading
 * or if the lookup failed. */
export interface NavAccessValue {
  isAdmin: boolean
  permissions: MyPermissions | null
}

export const NavAccessContext = createContext<NavAccessValue>({ isAdmin: false, permissions: null })

export function useNavAccess() {
  return useContext(NavAccessContext)
}
