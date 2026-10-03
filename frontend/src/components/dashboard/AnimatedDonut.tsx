import { Box, ButtonBase, Typography, useTheme } from '@mui/material'
import type { Theme } from '@mui/material/styles'
import { useDrawn } from './motion'

export interface DonutDatum { key: string; label: string; value: number; tone: 'primary' | 'success' | 'warning' | 'error' | 'neutral' }

function color(theme: Theme, tone: DonutDatum['tone']) {
  if (tone === 'neutral') return theme.palette.mode === 'dark' ? '#475569' : '#CBD5E1'
  return theme.palette[tone].main
}

/**
 * C69: SVG donut that draws its segments once (stroke-dashoffset), with a
 * legend that repeats every value as text. Legend rows can toggle a filter.
 */
export function AnimatedDonut({ data, centerValue, centerLabel, motion, onSelect, selectedKey, size = 132 }: {
  data: DonutDatum[]; centerValue: string; centerLabel: string; motion: boolean
  onSelect?: (d: DonutDatum) => void; selectedKey?: string | null; size?: number
}) {
  const theme = useTheme()
  const drawn = useDrawn(motion)
  const total = data.reduce((s, d) => s + d.value, 0)
  const r = 42
  const c = 2 * Math.PI * r
  let offset = 0
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
      <Box sx={{ position: 'relative', width: size, height: size, flexShrink: 0 }} aria-hidden>
        <svg viewBox="0 0 100 100" width={size} height={size} style={{ transform: 'rotate(-90deg)' }}>
          <circle cx="50" cy="50" r={r} fill="none" stroke={theme.palette.action.hover} strokeWidth="12" />
          {total > 0 && data.map((d) => {
            const len = (d.value / total) * c
            const seg = (
              <circle key={d.key} cx="50" cy="50" r={r} fill="none" stroke={color(theme, d.tone)} strokeWidth="12"
                strokeDasharray={`${drawn ? len : 0} ${c}`} strokeDashoffset={-offset}
                opacity={selectedKey && selectedKey !== d.key ? 0.35 : 1}
                style={{ transition: motion ? 'stroke-dasharray 700ms cubic-bezier(.2,.8,.2,1), opacity 150ms' : 'none' }} />
            )
            offset += len
            return seg
          })}
        </svg>
        <Box sx={{ position: 'absolute', inset: 0, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
          <Typography sx={{ fontWeight: 700, fontSize: 22, lineHeight: 1.1 }}>{centerValue}</Typography>
          <Typography variant="caption" sx={{ color: 'text.secondary' }}>{centerLabel}</Typography>
        </Box>
      </Box>
      <Box component="ul" sx={{ listStyle: 'none', m: 0, p: 0, width: '100%', display: 'flex', flexDirection: 'column', gap: 0.25 }}>
        {data.map((d) => {
          const pct = total ? Math.round((d.value / total) * 100) : 0
          const content = (
            <>
              <Box aria-hidden sx={{ width: 9, height: 9, borderRadius: '50%', bgcolor: color(theme, d.tone), flexShrink: 0 }} />
              <Typography variant="body2" sx={{ flex: 1, fontWeight: selectedKey === d.key ? 700 : 400 }}>{d.label}</Typography>
              <Typography variant="body2" sx={{ color: 'text.secondary', fontVariantNumeric: 'tabular-nums' }}>{d.value} ({pct}%)</Typography>
            </>
          )
          return (
            <Box component="li" key={d.key}>
              {onSelect ? (
                <ButtonBase onClick={() => onSelect(d)} aria-pressed={selectedKey === d.key}
                  sx={{ display: 'flex', alignItems: 'center', gap: 1, width: '100%', px: 0.75, py: 0.5, borderRadius: 1.5, textAlign: 'left', '&:hover': { bgcolor: 'action.hover' } }}>
                  {content}
                </ButtonBase>
              ) : <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, px: 0.75, py: 0.5 }}>{content}</Box>}
            </Box>
          )
        })}
      </Box>
    </Box>
  )
}
