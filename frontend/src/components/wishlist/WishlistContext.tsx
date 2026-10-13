import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import { useAuth } from '../../auth/AuthProvider'
import { wishlistApi } from '../../api/wishlistApi'

/** C94 (REQ-MKT-004): the signed-in user's saved product ids, for the heart on the catalog cards. */
interface WishlistState {
  ids: ReadonlySet<number>
  has: (productId: number) => boolean
  /** Saves or removes a product; resolves to whether it is saved afterwards. Rejects when the call fails. */
  toggle: (productId: number) => Promise<boolean>
}

const NO_WISHLIST: WishlistState = { ids: new Set(), has: () => false, toggle: () => Promise.resolve(false) }
const WishlistContext = createContext<WishlistState | null>(null)

export function WishlistProvider({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth()
  const [ids, setIds] = useState<ReadonlySet<number>>(new Set())
  // A list that arrives after sign-out is dropped, so the last person's saved products never come back (as the cart does).
  const signedInRef = useRef(isAuthenticated)
  useEffect(() => { signedInRef.current = isAuthenticated }, [isAuthenticated])

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- the list follows sign-in and sign-out
    if (!isAuthenticated) { setIds(new Set()); return }
    wishlistApi.list().then((list) => { if (signedInRef.current) setIds(new Set(list)) }).catch(() => {})
  }, [isAuthenticated])

  const toggle = useCallback(async (productId: number) => {
    const saved = ids.has(productId)
    const list = await (saved ? wishlistApi.remove(productId) : wishlistApi.add(productId))
    if (signedInRef.current) setIds(new Set(list))
    return !saved
  }, [ids])

  const value = useMemo<WishlistState>(() => ({ ids, has: (id) => ids.has(id), toggle }), [ids, toggle])
  return <WishlistContext.Provider value={value}>{children}</WishlistContext.Provider>
}

/** Outside a provider (isolated component tests) nothing is saved. */
// eslint-disable-next-line react-refresh/only-export-components
export function useWishlist(): WishlistState {
  return useContext(WishlistContext) ?? NO_WISHLIST
}
