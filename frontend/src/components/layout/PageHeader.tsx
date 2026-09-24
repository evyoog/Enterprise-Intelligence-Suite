import { Box, Typography } from '@mui/material'
import type { ReactNode } from 'react'

interface PageHeaderProps {
  title: string
  subtitle?: string
  action?: ReactNode
}

/**
 * Phase 23: a shared, softly-tinted header block — previously every internal
 * page (dashboard, admin lists) opened with a bare <Typography variant="h4">
 * directly on the page's flat background, with no visual separation from
 * the content below it. Reused across the newer admin/dashboard pages
 * rather than each repeating its own header markup with slightly different
 * spacing.
 */
export function PageHeader({ title, subtitle, action }: PageHeaderProps) {
  return (
    <Box
      sx={{
        mb: 3,
        p: 2.5,
        borderRadius: 3,
        display: 'flex',
        alignItems: 'flex-start',
        justifyContent: 'space-between',
        gap: 2,
        flexWrap: 'wrap',
        background: (theme) => theme.palette.mode === 'dark'
          ? 'linear-gradient(135deg, rgba(96,165,250,0.12), rgba(96,165,250,0.02))'
          : 'linear-gradient(135deg, rgba(37,99,235,0.08), rgba(37,99,235,0.01))',
        border: '1px solid',
        borderColor: 'divider',
      }}
    >
      <Box>
        <Typography variant="h4" sx={{ fontWeight: 700, mb: subtitle ? 0.5 : 0 }}>{title}</Typography>
        {subtitle && <Typography sx={{ color: 'text.secondary' }}>{subtitle}</Typography>}
      </Box>
      {action}
    </Box>
  )
}
