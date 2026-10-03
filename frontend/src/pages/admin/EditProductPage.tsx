import { Package as PHPackage } from 'lucide-react'
import { PageHeader } from '../../components/layout/PageHeader'
import { useEffect, useState } from 'react'
import { Alert, Box, Button, Chip, CircularProgress, Typography } from '@mui/material'
import { ArrowLeft } from 'lucide-react'
import { useNavigate, useParams } from 'react-router-dom'
import { ApiError } from '../../api/client'
import { productsApi, type Product } from '../../api/productsApi'
import { ProductForm } from '../../components/admin/ProductForm'

/**
 * "/admin/products/:id/edit" — reached from the Edit button on an admin
 * product card, which can live on either the flat "/admin/apps" list or a
 * platform's own scoped dashboard. Goes back via history rather than a fixed
 * route so it lands wherever the admin actually came from.
 */
export function EditProductPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [product, setProduct] = useState<Product | null>(null)
  const [error, setError] = useState<string | null>(null)
  // 02.01.01.04/.05 Publish/Retire product (sprint 2026.4.1).
  const [isChangingStatus, setIsChangingStatus] = useState(false)
  const [statusError, setStatusError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    productsApi.get(Number(id))
      .then(setProduct)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not load this product.'))
  }, [id])

  const changeStatus = (action: 'publish' | 'retire') => {
    if (!product) return
    setIsChangingStatus(true)
    setStatusError(null)
    productsApi[action](product.id)
      .then(setProduct)
      .catch((e) => setStatusError(e instanceof ApiError ? e.message : 'Could not update this product’s status.'))
      .finally(() => setIsChangingStatus(false))
  }

  return (
    <Box sx={{ maxWidth: 560 }}>
      <Button onClick={() => navigate(-1)} startIcon={<ArrowLeft size={16} />} sx={{ mb: 2 }}>
        Back
      </Button>

      <PageHeader icon={PHPackage} accent="blue" area="catalog" title="Edit app"
        subtitle="Update this app's details, pricing, SSO status, or availability."
        action={product ? <Chip color="primary" variant="outlined" label={`Version ${product.version}`} /> : undefined} />

      {product && (
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 3 }}>
          <Chip
            size="small"
            color={product.status === 'ACTIVE' ? 'success' : product.status === 'RETIRED' ? 'default' : 'warning'}
            label={product.status}
          />
          {product.status !== 'RETIRED' && (
            <Button size="small" disabled={isChangingStatus} onClick={() => changeStatus('retire')}>
              Retire
            </Button>
          )}
          {product.status !== 'ACTIVE' && (
            <Button size="small" disabled={isChangingStatus} onClick={() => changeStatus('publish')}>
              Publish
            </Button>
          )}
        </Box>
      )}
      {statusError && <Alert severity="error" sx={{ mb: 2 }}>{statusError}</Alert>}

      {error && <Typography color="error" role="alert">{error}</Typography>}

      {!error && !product && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
          <CircularProgress size={28} />
        </Box>
      )}

      {product && (
        <ProductForm
          initialProduct={product}
          onSubmit={(payload) => productsApi.update(product.id, payload)}
          submitLabel="Save Changes"
          submittingLabel="Saving…"
          successMessage="App updated."
        />
      )}
    </Box>
  )
}
