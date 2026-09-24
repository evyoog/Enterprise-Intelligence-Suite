import { Box, Typography } from '@mui/material'
import { platformsApi } from '../../../api/platformsApi'
import { PlatformForm } from '../../../components/admin/PlatformForm'

/** "/admin/settings/platform" — the "add a high-level platform" form. */
export function PlatformSettingsPage() {
  return (
    <Box sx={{ maxWidth: 560 }}>
      <Typography variant="h4" sx={{ fontWeight: 700, mb: 1 }}>
        Platform
      </Typography>
      <Typography sx={{ color: 'text.secondary', mb: 4 }}>
        Add a new high-level platform (e.g. Thittam) to group apps under.
      </Typography>

      <PlatformForm
        onSubmit={platformsApi.create}
        submitLabel="Add Platform"
        submittingLabel="Adding…"
        successMessage="Platform added."
      />
    </Box>
  )
}
