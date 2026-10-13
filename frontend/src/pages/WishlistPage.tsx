import { useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Box, Button, Grid, Skeleton, Snackbar } from '@mui/material'
import { Heart } from 'lucide-react'
import { Link as RouterLink } from 'react-router-dom'
import { ApiError } from '../api/client'
import { productsApi, type Product } from '../api/productsApi'
import { reviewsApi } from '../api/reviewsApi'
import { useBuy } from '../components/cart/useBuy'
import { canAddToCart, useAddToCart } from '../components/cart/useAddToCart'
import { CatalogProductCard } from '../components/catalog/CatalogProductCard'
import { PageHeader } from '../components/layout/PageHeader'
import { EmptyState } from '../components/ui/EmptyState'
import { ErrorState } from '../components/ui/ErrorState'
import { useWishlist } from '../components/wishlist/WishlistContext'
import type { Rating } from '../utils/catalogFilters'

/**
 * "/my/wishlist" — C94, REQ-MKT-004: the products the signed-in customer saved with the heart on the catalog cards,
 * newest first, with the same card and the same Subscribe and Add to cart actions. A saved product that is no longer
 * in the catalog (not active) is not shown.
 */
export function WishlistPage() {
  const { t } = useTranslation()
  const wishlist = useWishlist()
  const buy = useBuy()
  const { addToCart, busyId, notice } = useAddToCart()
  const [products, setProducts] = useState<Product[] | null>(null)
  const [ratings, setRatings] = useState<ReadonlyMap<number, Rating>>(new Map())
  const [error, setError] = useState<string | null>(null)
  const [toast, setToast] = useState<string | null>(null)
  const [reload, setReload] = useState(0)

  useEffect(() => {
    productsApi.list().then((list) => { setProducts(list); setError(null) })
      .catch((e) => setError(e instanceof ApiError ? e.message : t('catalog.loadError')))
    reviewsApi.listRatings()
      .then((list) => setRatings(new Map(list.map((r) => [r.productId, { averageRating: r.averageRating, reviewCount: r.reviewCount }]))))
      .catch(() => setRatings(new Map()))
  }, [reload, t])

  // Newest saved first: the context keeps the server's order.
  const saved = useMemo(() => {
    if (!products) return null
    const byId = new Map(products.map((p) => [p.id, p]))
    return [...wishlist.ids].map((id) => byId.get(id)).filter((p): p is Product => Boolean(p))
  }, [products, wishlist.ids])

  const toggle = (id: number) => wishlist.toggle(id).catch((e) => setToast(e instanceof ApiError ? e.message : t('wishlist.error')))

  return (
    <>
      <PageHeader icon={Heart} title={t('wishlist.title')} subtitle={t('wishlist.subtitle')} />
      {error && <ErrorState title={t('catalog.loadErrorTitle')} message={error} onRetry={() => setReload((n) => n + 1)} />}
      {!error && saved === null && (
        <Grid container spacing={2.5}>
          {[0, 1, 2].map((i) => <Grid key={i} size={{ xs: 12, sm: 6, lg: 4 }}><Skeleton variant="rounded" height={320} /></Grid>)}
        </Grid>
      )}
      {saved && saved.length === 0 && (
        <EmptyState title={t('wishlist.emptyTitle')} description={t('wishlist.emptyBody')}
          action={<Button component={RouterLink} to="/products" variant="contained">{t('wishlist.browse')}</Button>} />
      )}
      {saved && saved.length > 0 && (
        <Grid container spacing={2.5}>
          {saved.map((product) => (
            <Grid key={product.id} size={{ xs: 12, sm: 6, lg: 4 }}>
              <CatalogProductCard
                product={product}
                rating={ratings.get(product.id)}
                saved
                onToggleSaved={() => toggle(product.id)}
                onSubscribe={() => buy(product.id)}
                onAddToCart={() => addToCart(product)}
                addToCartUnavailable={!canAddToCart(product)}
                addingToCart={busyId === product.id}
              />
            </Grid>
          ))}
        </Grid>
      )}
      <Box>{notice}</Box>
      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)} message={toast ?? ''} />
    </>
  )
}
