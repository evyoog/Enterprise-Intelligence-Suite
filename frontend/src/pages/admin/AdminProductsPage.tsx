import { LayoutGrid as PHLayoutGrid } from 'lucide-react'
import { PageHeader } from '../../components/layout/PageHeader'
import { useState } from 'react'
import { Box, Button, Dialog, DialogContent, DialogTitle, IconButton, Paper, Typography } from '@mui/material'
import { Plus, X } from 'lucide-react'
import { ProductGrid } from '../../components/products/ProductGrid'
import { ProductForm } from '../../components/admin/ProductForm'
import { productsApi, type Product } from '../../api/productsApi'

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
 *
 * Full CRUD lives on this one page (C44): Read is the grid itself, Update
 * and Delete are ProductTile's own edit/trash icons (unchanged), and Create
 * is the "Add App" button below — previously the only way to create an app
 * was a separate page buried under Settings, which meant Create was the one
 * of the four CRUD actions you couldn't do from here. The same ProductForm
 * that page used is reused verbatim in a dialog instead of being duplicated.
 */
export function AdminProductsPage() {
  const [products, setProducts] = useState<Product[] | null>(null)
  const [createOpen, setCreateOpen] = useState(false)
  const [reloadToken, setReloadToken] = useState(0)

  const ssoConnectedCount = products?.filter((p) => p.ssoConnected).length ?? 0
  const standaloneCount = products ? products.length - ssoConnectedCount : 0

  const handleCreated = () => {
    setCreateOpen(false)
    setReloadToken((t) => t + 1)
  }

  return (
    <>
      <PageHeader icon={PHLayoutGrid} accent="blue" area="catalog" title="All Apps" subtitle="Every app across every platform."
        action={(
          <Button variant="contained" startIcon={<Plus size={16} />} onClick={() => setCreateOpen(true)}>
            Add App
          </Button>
        )} />

      <Box sx={{ display: 'flex', gap: 2, mb: 4, flexWrap: 'wrap' }}>
        <StatTile label="Total apps" value={products?.length ?? 0} color="text.primary" />
        <StatTile label="SSO connected" value={ssoConnectedCount} color="success.main" />
        <StatTile label="Standalone" value={standaloneCount} color="warning.main" />
      </Box>

      <ProductGrid admin onLoaded={setProducts} reloadToken={reloadToken} />

      <Dialog open={createOpen} onClose={() => setCreateOpen(false)} maxWidth="sm" fullWidth scroll="body">
        <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          Add a new app
          <IconButton size="small" aria-label="Close" onClick={() => setCreateOpen(false)}>
            <X size={18} />
          </IconButton>
        </DialogTitle>
        <DialogContent>
          <ProductForm
            onSubmit={productsApi.create}
            submitLabel="Add App"
            submittingLabel="Adding…"
            successMessage="App added."
            onSuccess={handleCreated}
          />
        </DialogContent>
      </Dialog>
    </>
  )
}
