import { useEffect, useState } from 'react'
import { Avatar, Box, Button, Paper, Typography } from '@mui/material'
import { ArrowLeft, Pencil, Trash2 } from 'lucide-react'
import { useNavigate, useParams, Link as RouterLink } from 'react-router-dom'
import { ApiError, resolveAssetUrl } from '../../api/client'
import { platformsApi, type Platform } from '../../api/platformsApi'
import type { Product } from '../../api/productsApi'
import { ProductGrid } from '../../components/products/ProductGrid'

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
 * "/admin/platforms/:id" — one platform's own scoped dashboard: its name,
 * logo, description, stat tiles, and only the apps assigned to it. Reuses
 * ProductGrid's `platformId` filter rather than a dedicated backend query —
 * the admin product list is already fetched whole everywhere else.
 */
export function PlatformDashboardPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const platformId = Number(id)
  const [platform, setPlatform] = useState<Platform | null>(null)
  const [products, setProducts] = useState<Product[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    platformsApi.get(platformId)
      .then(setPlatform)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not load this platform.'))
  }, [id, platformId])

  const ssoConnectedCount = products?.filter((p) => p.ssoConnected).length ?? 0
  const standaloneCount = products ? products.length - ssoConnectedCount : 0

  if (error) {
    return <Typography color="error" role="alert">{error}</Typography>
  }

  if (!platform) {
    return null
  }

  const handleDelete = () => {
    const count = products?.length ?? 0
    const warning = count > 0
      ? `Delete "${platform.name}"? Its ${count} assigned app${count === 1 ? '' : 's'} will stay, just no longer grouped under it.`
      : `Delete "${platform.name}"? This cannot be undone.`
    if (!window.confirm(warning)) return
    platformsApi.delete(platform.id)
      .then(() => navigate('/admin/platforms'))
      .catch((e) => window.alert(e instanceof ApiError ? e.message : 'Could not delete this platform.'))
  }

  return (
    <>
      <Button component={RouterLink} to="/admin/platforms" startIcon={<ArrowLeft size={16} />} sx={{ mb: 2 }}>
        Back to Platforms
      </Button>

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          {platform.imageUrl && <Avatar src={resolveAssetUrl(platform.imageUrl)} sx={{ width: 48, height: 48 }} />}
          <Box>
            <Typography variant="h4" sx={{ fontWeight: 700 }}>
              {platform.name}
            </Typography>
            {platform.description && (
              <Typography sx={{ color: 'text.secondary' }}>{platform.description}</Typography>
            )}
          </Box>
        </Box>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button
            component={RouterLink}
            to={`/admin/platforms/${platform.id}/edit`}
            variant="outlined"
            startIcon={<Pencil size={14} />}
          >
            Edit Platform
          </Button>
          <Button variant="outlined" color="error" startIcon={<Trash2 size={14} />} onClick={handleDelete}>
            Delete
          </Button>
        </Box>
      </Box>

      <Box sx={{ display: 'flex', gap: 2, mb: 4, flexWrap: 'wrap' }}>
        <StatTile label="Total apps" value={products?.length ?? 0} color="text.primary" />
        <StatTile label="SSO connected" value={ssoConnectedCount} color="success.main" />
        <StatTile label="Standalone" value={standaloneCount} color="warning.main" />
      </Box>

      <ProductGrid admin platformId={platformId} onLoaded={setProducts} />
    </>
  )
}
