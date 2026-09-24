import { useState } from 'react'
import { Box, Paper, Typography } from '@mui/material'
import { ProductGrid } from '../../components/products/ProductGrid'
import type { Product } from '../../api/productsApi'

interface StatTileProps {
  label: string
  value: number
  color: string
}

function StatTile({ label, value, color }: StatTileProps) {
  return (
    <Paper variant="outlined" sx={{ p: 2.5, flex: 1, minWidth: 160 }}>
      <Typography variant="body2" sx={{ color: 'text.secondary' }}>
        {label}
      </Typography>
      <Typography variant="h4" sx={{ fontWeight: 700, mt: 0.5, color }}>
        {value}
      </Typography>
    </Paper>
  )
}

/**
 * "/admin/apps" — every app across every platform (via the ADMIN-only
 * endpoint, so INACTIVE apps show too), plus a quick read on how many are
 * actually wired into the Vyoog SSO bridge. The admin landing page itself is
 * PlatformsListPage ("/admin"); platform-scoped views live under
 * "/admin/platforms/:id" (PlatformDashboardPage).
 */
export function AdminProductsPage() {
  const [products, setProducts] = useState<Product[] | null>(null)

  const ssoConnectedCount = products?.filter((p) => p.ssoConnected).length ?? 0
  const standaloneCount = products ? products.length - ssoConnectedCount : 0

  return (
    <>
      <Typography variant="h4" sx={{ fontWeight: 700, mb: 0.5 }}>
        All Apps
      </Typography>
      <Typography sx={{ color: 'text.secondary', mb: 3 }}>
        Every app across every platform.
      </Typography>

      <Box sx={{ display: 'flex', gap: 2, mb: 4, flexWrap: 'wrap' }}>
        <StatTile label="Total apps" value={products?.length ?? 0} color="text.primary" />
        <StatTile label="SSO connected" value={ssoConnectedCount} color="success.main" />
        <StatTile label="Standalone" value={standaloneCount} color="warning.main" />
      </Box>

      <ProductGrid admin onLoaded={setProducts} />
    </>
  )
}
