import { Box, Typography } from '@mui/material'

export type StatusTone = 'success' | 'warning' | 'error' | 'info' | 'neutral'

const TONE: Record<StatusTone, { dot: string; text: string }> = {
  success: { dot: '#10B981', text: 'success.main' },
  warning: { dot: '#F59E0B', text: 'warning.main' },
  error: { dot: '#EF4444', text: 'error.main' },
  info: { dot: '#3B82F6', text: 'info.main' },
  neutral: { dot: '#94A3B8', text: 'text.secondary' },
}

/** C66: a status shown as a dot plus a text label (never colour alone). */
export function StatusBadge({ label, tone = 'neutral' }: { label: string; tone?: StatusTone }) {
  return (
    <Box component="span" sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.75 }}>
      <Box component="span" aria-hidden sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: TONE[tone].dot, flexShrink: 0 }} />
      <Typography component="span" variant="body2" sx={{ fontWeight: 600, color: TONE[tone].text }}>{label}</Typography>
    </Box>
  )
}
