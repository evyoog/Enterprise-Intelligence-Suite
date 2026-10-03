import type { ReactNode } from 'react'
import { Box, Paper, Typography } from '@mui/material'

/** C66: one titled group of a form — heading, short description, fields. */
export function FormSection({ id, title, description, children, action }: {
  id: string; title: string; description?: string; children: ReactNode; action?: ReactNode
}) {
  return (
    <Paper variant="outlined" component="section" aria-labelledby={`${id}-title`} sx={{ p: { xs: 2, sm: 3 }, borderRadius: 3, scrollMarginTop: 88 }} id={id}>
      <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 2, mb: 2.5 }}>
        <Box>
          <Typography id={`${id}-title`} component="h2" sx={{ fontWeight: 700, fontSize: 16 }}>{title}</Typography>
          {description && <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.25 }}>{description}</Typography>}
        </Box>
        {action}
      </Box>
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>{children}</Box>
    </Paper>
  )
}
