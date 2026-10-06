import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import { useAuth } from '../../auth/AuthProvider'
import { cartApi } from '../../api/cartApi'

/** C59 (REQ-MKT-003.7): the cart item count for the top-bar badge. The cart
 * page reports every change through {@link CartState.setCount}, so the badge
 * updates without a reload. */
interface CartState {
  count: number
  setCount: (count: number) => void
  refresh: () => void
}

const CartContext = createContext<CartState | null>(null)

const NO_CART: CartState = { count: 0, setCount: () => {}, refresh: () => {} }

export function CartProvider({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth()
  const [count, setCount] = useState(0)
  // C79: a cart answer that arrives after sign-out is dropped, so the last
  // person's item count never comes back.
  const signedInRef = useRef(isAuthenticated)
  useEffect(() => { signedInRef.current = isAuthenticated }, [isAuthenticated])

  const refresh = useCallback(() => {
    if (!isAuthenticated) return
    cartApi.get().then((cart) => { if (signedInRef.current) setCount(cart.itemCount) }).catch(() => {})
  }, [isAuthenticated])

  // eslint-disable-next-line react-hooks/set-state-in-effect -- the count follows sign-in and sign-out
  useEffect(() => { if (isAuthenticated) refresh(); else setCount(0) }, [isAuthenticated, refresh])

  const value = useMemo(() => ({ count, setCount, refresh }), [count, refresh])
  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}

/** Outside a provider (isolated component tests) the cart is simply empty. */
// eslint-disable-next-line react-refresh/only-export-components
export function useCart(): CartState {
  return useContext(CartContext) ?? NO_CART
}
