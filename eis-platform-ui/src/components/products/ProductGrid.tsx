import { useEffect, useState } from 'react'
import { Box, CircularProgress, Grid, Typography } from '@mui/material'
import { ApiError } from '../../api/client'
import { productsApi, type Product } from '../../api/productsApi'
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
}

/**
 * Fetches and renders the admin product list on mount — the flat "/admin/apps"
 * view and each platform's own scoped dashboard. The public storefront
 * ("/products") has its own richer layout (search + category sections) in
 * ProductsPage, built directly on ProductTile instead of this component.
 */
export function ProductGrid({ admin = false, onLoaded, platformId }: ProductGridProps) {
  const [products, setProducts] = useState<Product[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const request = admin ? productsApi.listAdmin() : productsApi.list()
    request
      .then((result) => {
        const scoped = platformId === undefined
          ? result
          : result.filter((p) => p.platforms.some((pl) => pl.id === platformId))
        setProducts(scoped)
        // eslint-disable-next-line react-hooks/exhaustive-deps
        onLoaded?.(scoped)
      })
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not load products.'))
  }, [admin, platformId])

  if (error) {
    return <Typography color="error" role="alert">{error}</Typography>
  }

  if (products === null) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
        <CircularProgress size={28} />
      </Box>
    )
  }

  if (products.length === 0) {
    return (
      <Typography sx={{ color: 'text.secondary' }}>
        No products yet — check back soon.
      </Typography>
    )
  }

  const handleDelete = (product: Product) => {
    if (!window.confirm(`Delete "${product.name}"? This cannot be undone.`)) return
    productsApi.delete(product.id)
      .then(() => setProducts((current) => current?.filter((p) => p.id !== product.id) ?? current))
      .catch((e) => window.alert(e instanceof ApiError ? e.message : 'Could not delete this product.'))
  }

  return (
    <Grid container spacing={2.5}>
      {products.map((product, index) => (
        <Grid key={product.id} size={{ xs: 12, sm: 6, md: 4, lg: 3 }}>
          <ProductTile
            product={product}
            admin={admin}
            showPlatformChips={platformId === undefined}
            animationDelay={index * 45}
            onDelete={admin ? () => handleDelete(product) : undefined}
          />
        </Grid>
      ))}
    </Grid>
  )
}
