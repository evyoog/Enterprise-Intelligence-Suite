import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Grid, Skeleton } from '@mui/material'
import { ApiError } from '../../api/client'
import { productsApi, type Product } from '../../api/productsApi'
import { ConfirmDialog } from '../ui/ConfirmDialog'
import { EmptyState } from '../ui/EmptyState'
import { ErrorState } from '../ui/ErrorState'
import { ProductTile } from './ProductTile'

interface ProductGridProps {
  /** Admin view: shows every product (including INACTIVE) via the ADMIN-only
   * /products/admin endpoint. Default (false) is the public storefront view —
   * ACTIVE products only, via the public /products endpoint. */
  admin?: boolean
  /** Called once the list has loaded — lets a parent page (e.g. the admin
   * dashboard) compute summary stats without re-fetching itself. */
  onLoaded?: (products: Product[]) => void
  /** Scopes the grid to apps assigned to this platform — used by
   * PlatformDashboardPage. Filtered client-side after the normal admin/public
   * fetch, same as everywhere else this dataset is small enough to just
   * filter in the browser. */
  platformId?: number
  /** Bump this (e.g. after creating a new app elsewhere on the same page) to
   * force a refetch — the grid has no other way to know new data exists. */
  reloadToken?: number
}

/**
 * Fetches and renders the admin product list on mount — the flat "/admin/apps"
 * view and each platform's own scoped dashboard. The public storefront
 * ("/products") has its own richer layout (search + category sections) in
 * ProductsPage, built directly on ProductTile instead of this component.
 */
export function ProductGrid({ admin = false, onLoaded, platformId, reloadToken }: ProductGridProps) {
  const [products, setProducts] = useState<Product[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [pendingDelete, setPendingDelete] = useState<Product | null>(null)
  const [deleting, setDeleting] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)
  const { t } = useTranslation()

  useEffect(() => {
    const request = admin ? productsApi.listAdmin() : productsApi.list()
    request
      .then((result) => {
        const scoped = platformId === undefined
          ? result
          : result.filter((p) => p.platforms.some((pl) => pl.id === platformId))
        setProducts(scoped)
        onLoaded?.(scoped)
      })
      .catch((e) => setError(e instanceof ApiError ? e.message : t('admin.apps.loadError')))
    // onLoaded is a callback prop; refetching when its identity changes would loop.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [admin, platformId, reloadToken, t])

  if (error) {
    return <ErrorState title={t('admin.apps.loadErrorTitle')} message={error} />
  }

  if (products === null) {
    return <Skeleton variant="rounded" height={240} />
  }

  if (products.length === 0) {
    return <EmptyState title={t('admin.apps.emptyTitle')} description={t('admin.apps.emptyBody')} />
  }

  const handleDelete = () => {
    if (!pendingDelete) return
    setDeleting(true)
    setDeleteError(null)
    productsApi.delete(pendingDelete.id)
      .then(() => setProducts((current) => current?.filter((p) => p.id !== pendingDelete.id) ?? current))
      .catch((e) => setDeleteError(e instanceof ApiError ? e.message : t('admin.apps.deleteError')))
      .finally(() => { setDeleting(false); setPendingDelete(null) })
  }

  return (
    <>
      {deleteError && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setDeleteError(null)}>{deleteError}</Alert>}
      <Grid container spacing={2.5}>
        {products.map((product, index) => (
          <Grid key={product.id} size={{ xs: 12, sm: 6, md: 4, lg: 3 }}>
            <ProductTile
              product={product}
              admin={admin}
              showPlatformChips={platformId === undefined}
              animationDelay={index * 45}
              onDelete={admin ? () => setPendingDelete(product) : undefined}
            />
          </Grid>
        ))}
      </Grid>
      <ConfirmDialog open={pendingDelete !== null} busy={deleting}
        title={t('admin.apps.deleteTitle', { name: pendingDelete?.name ?? '' })} body={t('admin.apps.deleteBody')}
        confirmLabel={t('catalog.app.delete')} onConfirm={handleDelete} onClose={() => setPendingDelete(null)} />
    </>
  )
}
