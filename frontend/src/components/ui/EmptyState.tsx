import type { ComponentType, ReactNode } from 'react'
import { Box, Paper, Typography } from '@mui/material'
import { Inbox } from 'lucide-react'

/** C66: a useful empty state — icon, title, hint and an optional action. */
export function EmptyState({ title, description, action, icon: Icon = Inbox }: {
  title: string; description?: string; action?: ReactNode; icon?: ComponentType<{ size?: number | string }>
}) {
  return (
    <Paper variant="outlined" sx={{ p: { xs: 3, sm: 5 }, textAlign: 'center', borderRadius: 3, borderStyle: 'dashed' }}>
      <Box aria-hidden sx={{ width: 44, height: 44, mx: 'auto', mb: 1.5, borderRadius: 2, display: 'grid', placeItems: 'center', bgcolor: 'action.hover', color: 'text.secondary' }}>
        <Icon size={22} />
      </Box>
      <Typography sx={{ fontWeight: 600 }}>{title}</Typography>
      {description && <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.5 }}>{description}</Typography>}
      {action && <Box sx={{ mt: 2 }}>{action}</Box>}
    </Paper>
  )
}
