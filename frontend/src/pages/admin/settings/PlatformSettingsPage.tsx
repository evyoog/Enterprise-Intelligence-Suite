import { Layers as PHLayers } from 'lucide-react'
import { PageHeader } from '../../../components/layout/PageHeader'
import { Box } from '@mui/material'
import { platformsApi } from '../../../api/platformsApi'
import { PlatformForm } from '../../../components/admin/PlatformForm'

/** "/admin/settings/platform" — the "add a high-level platform" form. */
export function PlatformSettingsPage() {
  return (
    <Box sx={{ maxWidth: 560 }}>
      <PageHeader icon={PHLayers} accent="indigo" area="settings" title="Platform" subtitle="Add a new high-level platform (e.g. Thittam) to group apps under." />

      <PlatformForm
        onSubmit={platformsApi.create}
        submitLabel="Add Platform"
        submittingLabel="Adding…"
        successMessage="Platform added."
      />
    </Box>
  )
}
