import { Box, ButtonBase, Tooltip, Typography } from '@mui/material'
import { useDrawn } from './motion'

export interface BarDatum { key: string | number; label: string; value: number; hint?: string }

/**
 * C69: horizontal bar chart. Bars grow from zero once (when motion is
 * allowed); every value is also printed as text, and each row is a button
 * with an accessible name, so nothing depends on colour or hovering.
 */
export function AnimatedBars({ data, onSelect, motion, ariaLabel, actionLabel }: {
  data: BarDatum[]; onSelect?: (d: BarDatum) => void; motion: boolean; ariaLabel: string
  /** Accessible name prefix for a clickable row, e.g. "Open details". */
  actionLabel?: string
}) {
  const drawn = useDrawn(motion)
  const max = Math.max(1, ...data.map((d) => d.value))
  return (
    <Box component="ul" aria-label={ariaLabel} sx={{ listStyle: 'none', m: 0, p: 0, display: 'flex', flexDirection: 'column', gap: 0.5 }}>
      {data.map((d, i) => {
        const pct = (d.value / max) * 100
        const row = (
          <Box sx={{ display: 'grid', gridTemplateColumns: 'minmax(96px, 34%) minmax(0, 1fr) auto', alignItems: 'center', gap: 1.5, width: '100%', py: 0.75, px: 1 }}>
            <Typography variant="body2" noWrap title={d.label}>{d.label}</Typography>
            <Box sx={{ height: 10, borderRadius: 999, bgcolor: 'action.hover', overflow: 'hidden' }}>
              <Box sx={{
                height: '100%', borderRadius: 999, bgcolor: 'primary.main', opacity: 0.85,
                width: drawn ? `${Math.max(pct, d.value > 0 ? 2 : 0)}%` : 0,
                transition: motion ? `width 600ms cubic-bezier(.2,.8,.2,1) ${i * 60}ms` : 'none',
              }} />
            </Box>
            <Typography variant="body2" sx={{ fontWeight: 600, minWidth: 28, textAlign: 'right', fontVariantNumeric: 'tabular-nums' }}>{d.value}</Typography>
          </Box>
        )
        return (
          <Box component="li" key={d.key}>
            <Tooltip title={d.hint ?? `${d.label}: ${d.value}`} placement="top" arrow>
              {onSelect ? (
                <ButtonBase onClick={() => onSelect(d)} aria-label={`${actionLabel ? `${actionLabel}: ` : ''}${d.label}, ${d.hint ?? d.value}`}
                  sx={{ display: 'block', width: '100%', textAlign: 'left', borderRadius: 1.5, transition: 'background-color 150ms', '&:hover': { bgcolor: 'action.hover' } }}>
                  {row}
                </ButtonBase>
              ) : <Box>{row}</Box>}
            </Tooltip>
          </Box>
        )
      })}
    </Box>
  )
}
