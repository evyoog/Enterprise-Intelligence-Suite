import { Box } from '@mui/material'
import { useDrawn } from './motion'

/** C69: a thin progress meter that fills once from zero (motion permitting). */
export function MeterBar({ value, motion, tone = 'primary', label }: { value: number; motion: boolean; tone?: 'primary' | 'warning' | 'error'; label: string }) {
  const drawn = useDrawn(motion)
  const pct = Math.max(0, Math.min(100, value))
  return (
    <Box role="progressbar" aria-label={label} aria-valuenow={Math.round(pct)} aria-valuemin={0} aria-valuemax={100}
      sx={{ height: 8, borderRadius: 999, bgcolor: 'action.hover', overflow: 'hidden' }}>
      <Box sx={{ height: '100%', borderRadius: 999, bgcolor: `${tone}.main`, width: drawn ? `${pct}%` : 0, transition: motion ? 'width 700ms cubic-bezier(.2,.8,.2,1)' : 'none' }} />
    </Box>
  )
}
