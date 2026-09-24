import { Box, Typography } from '@mui/material'

/**
 * "/admin/settings/common" — placeholder only, by explicit request. What
 * actually belongs here (general site-wide settings, shared subscription plan
 * definitions, or something else) hasn't been decided yet.
 */
export function CommonSettingsPage() {
  return (
    <Box sx={{ maxWidth: 560 }}>
      <Typography variant="h4" sx={{ fontWeight: 700, mb: 1 }}>
        Common
      </Typography>
      <Typography sx={{ color: 'text.secondary' }}>
        General settings will live here — not decided yet what goes in this section.
      </Typography>
    </Box>
  )
}
