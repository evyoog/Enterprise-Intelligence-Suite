import { Box, ButtonBase, LinearProgress, Typography } from '@mui/material'

export interface BarListItem {
  label: string
  value: number
}

/**
 * A horizontal "bar chart" built from the same LinearProgress primitive the
 * Seat usage section already uses for utilization — one real measure per
 * row, the value always printed as text (never hover-only), so there is
 * nothing here that depends on color alone or on a pointer to read.
 *
 * Optionally click-to-filter (C54), same pattern as the product-page rating
 * distribution (C49): passing `onItemClick` turns each row into a toggle —
 * clicking the already-selected row clears the selection.
 */
export function BarList({ items, valueFormatter = (v: number) => String(v), onItemClick, selectedLabel }: {
  items: BarListItem[]
  valueFormatter?: (value: number) => string
  onItemClick?: (item: BarListItem) => void
  selectedLabel?: string | null
}) {
  const max = Math.max(1, ...items.map((i) => i.value))

  if (items.length === 0) {
    return <Typography variant="body2" sx={{ color: 'text.secondary' }}>No data yet.</Typography>
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
      {items.map((item) => {
        const row = (
          <Box sx={{ width: '100%' }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
              <Typography variant="body2" noWrap sx={{ maxWidth: '70%', fontWeight: selectedLabel === item.label ? 700 : 400 }}>
                {item.label}
              </Typography>
              <Typography variant="body2" sx={{ fontWeight: 600 }}>{valueFormatter(item.value)}</Typography>
            </Box>
            <LinearProgress
              variant="determinate"
              value={(item.value / max) * 100}
              sx={{ height: 8, borderRadius: 999, opacity: selectedLabel && selectedLabel !== item.label ? 0.4 : 1 }}
            />
          </Box>
        )
        if (!onItemClick) {
          return <Box key={item.label}>{row}</Box>
        }
        return (
          <ButtonBase
            key={item.label}
            onClick={() => onItemClick(item)}
            aria-pressed={selectedLabel === item.label}
            sx={{ display: 'block', width: '100%', textAlign: 'left', borderRadius: 1, '&:hover': { bgcolor: 'action.hover' } }}
          >
            {row}
          </ButtonBase>
        )
      })}
    </Box>
  )
}
