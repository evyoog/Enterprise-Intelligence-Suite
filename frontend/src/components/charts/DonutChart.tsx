import { Box, Typography, useTheme } from '@mui/material'
import type { Theme } from '@mui/material/styles'

export interface DonutSegment {
  label: string
  value: number
  /** A semantic theme token, never a one-off hex — see billing-ui-standards.md's status-chip mapping. */
  tone: 'success' | 'warning' | 'error' | 'info' | 'neutral'
}

function toneColor(theme: Theme, tone: DonutSegment['tone']): string {
  switch (tone) {
    case 'success': return theme.palette.success.main
    case 'warning': return theme.palette.warning.main
    case 'error': return theme.palette.error.main
    case 'info': return theme.palette.info.main
    default: return theme.palette.text.disabled
  }
}

/**
 * A CSS conic-gradient donut, no charting library. Color never stands alone:
 * the legend beneath always repeats every segment as text, which doubles as
 * the "table view" a screen-reader user (or anyone else) needs — the ring
 * itself is `aria-hidden`, and the one real accessible description lives on
 * the legend's own `aria-label`.
 */
export function DonutChart({ segments, centerLabel, centerValue, size = 160 }: {
  segments: DonutSegment[]
  centerLabel?: string
  centerValue?: string
  size?: number
}) {
  const theme = useTheme()
  const total = segments.reduce((sum, s) => sum + s.value, 0)

  let cursor = 0
  const stops: string[] = []
  for (const segment of segments) {
    const start = total > 0 ? (cursor / total) * 360 : 0
    cursor += segment.value
    const end = total > 0 ? (cursor / total) * 360 : 0
    stops.push(`${toneColor(theme, segment.tone)} ${start}deg ${end}deg`)
  }
  const gradient = total > 0 ? `conic-gradient(${stops.join(', ')})` : `conic-gradient(${theme.palette.action.disabledBackground} 0deg 360deg)`

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
      <Box
        aria-hidden
        sx={{
          position: 'relative', width: size, height: size, borderRadius: '50%',
          background: gradient,
        }}
      >
        <Box sx={{
          position: 'absolute', inset: size * 0.18, borderRadius: '50%',
          bgcolor: 'background.paper', display: 'flex', flexDirection: 'column',
          alignItems: 'center', justifyContent: 'center', textAlign: 'center', px: 1,
        }}>
          {centerValue && <Typography sx={{ fontWeight: 700, fontSize: size * 0.11, lineHeight: 1.1 }}>{centerValue}</Typography>}
          {centerLabel && <Typography variant="caption" sx={{ color: 'text.secondary' }}>{centerLabel}</Typography>}
        </Box>
      </Box>
      <Box
        component="ul"
        aria-label={segments.map((s) => `${s.label}: ${s.value}`).join(', ')}
        sx={{ listStyle: 'none', m: 0, p: 0, display: 'flex', flexDirection: 'column', gap: 0.75, width: '100%' }}
      >
        {segments.map((segment) => {
          const pct = total > 0 ? Math.round((segment.value / total) * 100) : 0
          return (
            <Box component="li" key={segment.label} sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Box sx={{ width: 10, height: 10, borderRadius: '50%', bgcolor: toneColor(theme, segment.tone), flexShrink: 0 }} />
              <Typography variant="body2" sx={{ flex: 1 }}>{segment.label}</Typography>
              <Typography variant="body2" sx={{ color: 'text.secondary' }}>{segment.value} ({pct}%)</Typography>
            </Box>
          )
        })}
      </Box>
    </Box>
  )
}
