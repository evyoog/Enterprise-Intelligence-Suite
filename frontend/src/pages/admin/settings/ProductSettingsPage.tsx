import { Box, Typography } from '@mui/material'
import { productsApi } from '../../../api/productsApi'
import { ProductForm } from '../../../components/admin/ProductForm'

/** "/admin/settings/product" — the "add a product" form. */
export function ProductSettingsPage() {
  return (
    <Box sx={{ maxWidth: 560 }}>
      <Typography variant="h4" sx={{ fontWeight: 700, mb: 1 }}>
        App
      </Typography>
      <Typography sx={{ color: 'text.secondary', mb: 4 }}>
        Add a new app to the Thittam platform.
      </Typography>

      <ProductForm
        onSubmit={productsApi.create}
        submitLabel="Add App"
        submittingLabel="Adding…"
        successMessage="App added."
      />
    </Box>
  )
}
