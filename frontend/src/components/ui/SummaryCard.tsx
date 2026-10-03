import type { ComponentType, ReactNode } from 'react'
import { Box, Paper, Typography } from '@mui/material'
import { showcasePalette } from '../../utils/showcaseColor'

/** C66: a compact metric tile — icon, label, number, supporting text. */
export function SummaryCard({ icon: Icon, label, value, hint, color }: {
  icon: ComponentType<{ size?: number | string }>; label: string; value: ReactNode; hint?: string; color?: string
}) {
  const palette = showcasePalette(color)
  return (
    <Paper variant="outlined" sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1.75, borderRadius: 3, height: '100%' }}>
      <Box aria-hidden sx={{ width: 40, height: 40, borderRadius: 2, flexShrink: 0, display: 'grid', placeItems: 'center', bgcolor: palette.soft, color: palette.text }}>
        <Icon size={20} />
      </Box>
      <Box sx={{ minWidth: 0 }}>
        <Typography variant="body2" sx={{ color: 'text.secondary', fontWeight: 500 }}>{label}</Typography>
        <Typography sx={{ fontSize: 22, fontWeight: 700, lineHeight: 1.25 }}>{value}</Typography>
        {hint && <Typography variant="caption" sx={{ color: 'text.secondary' }}>{hint}</Typography>}
      </Box>
    </Paper>
  )
}
