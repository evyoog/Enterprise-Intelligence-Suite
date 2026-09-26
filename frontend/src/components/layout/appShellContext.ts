import { createContext, useContext } from 'react'

/** True inside the signed-in AppShell. Pages that are also public (catalog,
 * preferences) use it to leave out the public website header. */
export const AppShellContext = createContext(false)

export function useInAppShell() {
  return useContext(AppShellContext)
}
