import { useCallback, useState, type ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink, useNavigate } from 'react-router-dom'
import { Alert, Button, Snackbar } from '@mui/material'
import { useAuth } from '../../auth/AuthProvider'
import { ApiError } from '../../api/client'
import { cartApi } from '../../api/cartApi'
import type { Product, ProductPlan } from '../../api/productsApi'
import { cartAddPath } from './useBuy'
import { useCart } from './CartContext'

/** C93 (REQ-MKT-003.10, CT-1 A): the plan a card's Add to cart puts in the cart: the cheapest paid plan. */
export function cheapestPaidPlan(product: Pick<Product, 'plans'>): ProductPlan | undefined {
  return product.plans.filter((p) => p.price > 0).sort((a, b) => a.price - b.price)[0]
}

/** Only a product with a paid plan can be added from a card; free plans keep their Subscribe action (CT-4 A). */
export const canAddToCart = (product: Pick<Product, 'plans'>) => cheapestPaidPlan(product) !== undefined

type Feedback = { message: string; severity: 'success' | 'error'; showCartLink: boolean }

/**
 * C93 (REQ-MKT-003.10): Add to cart on a product card. The item is added where the customer is (CT-2 A), so
 * several products can be added in a row; the cart badge follows. A signed-out visitor signs in first and the
 * product is added when they come back, as with Buy (CT-4 A, REQ-MKT-003 Open question 5).
 * Render the returned {@code notice} once on the page.
 */
export function useAddToCart(): { addToCart: (product: Product) => void; busyId: number | null; notice: ReactNode } {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { isAuthenticated } = useAuth()
  const { setCount } = useCart()
  const [busyId, setBusyId] = useState<number | null>(null)
  const [feedback, setFeedback] = useState<Feedback | null>(null)

  const addToCart = useCallback((product: Product) => {
    const plan = cheapestPaidPlan(product)
    if (!plan) return
    if (!isAuthenticated) {
      navigate(`/login?returnTo=${encodeURIComponent(cartAddPath(product.id, plan.id))}`)
      return
    }
    setBusyId(product.id)
    cartApi.add(product.id, plan.id)
      .then((cart) => {
        setCount(cart.itemCount)
        setFeedback({ message: t('cart.added', { product: product.name, plan: plan.name }), severity: 'success', showCartLink: true })
      })
      .catch((e) => setFeedback({ message: e instanceof ApiError ? e.message : t('cart.error'), severity: 'error', showCartLink: false }))
      .finally(() => setBusyId(null))
  }, [isAuthenticated, navigate, setCount, t])

  const notice = (
    <Snackbar open={feedback !== null} autoHideDuration={6000} onClose={() => setFeedback(null)}
      anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}>
      <Alert severity={feedback?.severity ?? 'success'} variant="filled" onClose={() => setFeedback(null)}
        action={feedback?.showCartLink
          ? <Button color="inherit" size="small" component={RouterLink} to="/cart">{t('cart.viewCart')}</Button>
          : undefined}>
        {feedback?.message}
      </Alert>
    </Snackbar>
  )
  return { addToCart, busyId, notice }
}
