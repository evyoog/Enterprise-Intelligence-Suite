import { Box, LinearProgress, Typography } from '@mui/material'

export interface BarListItem {
  label: string
  value: number
}

/**
 * A horizontal "bar chart" built from the same LinearProgress primitive the
 * Seat usage section already uses for utilization — one real measure per
 * row, the value always printed as text (never hover-only), so there is
 * nothing here that depends on color alone or on a pointer to read.
 */
export function BarList({ items, valueFormatter = (v: number) => String(v) }: {
  items: BarListItem[]
  valueFormatter?: (value: number) => string
}) {
  const max = Math.max(1, ...items.map((i) => i.value))

  if (items.length === 0) {
    return <Typography variant="body2" sx={{ color: 'text.secondary' }}>No data yet.</Typography>
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
      {items.map((item) => (
        <Box key={item.label}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
            <Typography variant="body2" noWrap sx={{ maxWidth: '70%' }}>{item.label}</Typography>
            <Typography variant="body2" sx={{ fontWeight: 600 }}>{valueFormatter(item.value)}</Typography>
          </Box>
          <LinearProgress
            variant="determinate"
            value={(item.value / max) * 100}
            sx={{ height: 8, borderRadius: 999 }}
          />
        </Box>
      ))}
    </Box>
  )
}
