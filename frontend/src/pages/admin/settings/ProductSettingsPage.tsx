import { Package as PHPackage } from 'lucide-react'
import { PageHeader } from '../../../components/layout/PageHeader'
import { Box } from '@mui/material'
import { productsApi } from '../../../api/productsApi'
import { ProductForm } from '../../../components/admin/ProductForm'

/** "/admin/settings/product" — the "add a product" form. */
export function ProductSettingsPage() {
  return (
    <Box sx={{ maxWidth: 560 }}>
      <PageHeader icon={PHPackage} accent="blue" area="settings" title="App" subtitle="Add a new app to the Thittam platform." />

      <ProductForm
        onSubmit={productsApi.create}
        submitLabel="Add App"
        submittingLabel="Adding…"
        successMessage="App added."
      />
    </Box>
  )
}
