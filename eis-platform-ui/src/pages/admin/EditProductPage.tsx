import { useEffect, useState } from 'react'
import { Box, Button, CircularProgress, Typography } from '@mui/material'
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

  useEffect(() => {
    if (!id) return
    productsApi.get(Number(id))
      .then(setProduct)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not load this product.'))
  }, [id])

  return (
    <Box sx={{ maxWidth: 560 }}>
      <Button onClick={() => navigate(-1)} startIcon={<ArrowLeft size={16} />} sx={{ mb: 2 }}>
        Back
      </Button>

      <Typography variant="h4" sx={{ fontWeight: 700, mb: 1 }}>
        Edit app
      </Typography>
      <Typography sx={{ color: 'text.secondary', mb: 4 }}>
        Update this app's details, pricing, SSO status, or availability.
      </Typography>

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
