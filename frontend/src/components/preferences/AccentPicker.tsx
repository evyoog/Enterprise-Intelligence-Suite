import { Box, Tooltip, Typography } from '@mui/material'
import { Check } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { ACCENT_KEYS, ACCENT_PALETTES, type AccentKey } from '../../theming/accentPalette'

/**
 * C67: the platform accent as a radio group of colour swatches — arrow keys
 * move between colours, the selected one has a check and a ring.
 */
export function AccentPicker({ value, onChange, labelledBy }: {
  value: AccentKey; onChange: (value: AccentKey) => void; labelledBy: string
}) {
  const { t } = useTranslation()
  const move = (from: number, step: number) => {
    const next = ACCENT_KEYS[(from + step + ACCENT_KEYS.length) % ACCENT_KEYS.length]
    onChange(next)
    document.getElementById(`accent-${next}`)?.focus()
  }
  return (
    <Box role="radiogroup" aria-labelledby={labelledBy} sx={{ display: 'flex', flexWrap: 'wrap', gap: 1.5, mt: 1.5 }}>
      {ACCENT_KEYS.map((key, index) => {
        const selected = key === value
        const name = t(`preferences.accent.${key}`)
        return (
          <Tooltip key={key} title={name}>
            <Box
              component="button" type="button" id={`accent-${key}`} role="radio" aria-checked={selected} aria-label={name}
              tabIndex={selected ? 0 : -1}
              onClick={() => onChange(key)}
              onKeyDown={(e: React.KeyboardEvent) => {
                if (e.key === 'ArrowRight' || e.key === 'ArrowDown') { e.preventDefault(); move(index, 1) }
                if (e.key === 'ArrowLeft' || e.key === 'ArrowUp') { e.preventDefault(); move(index, -1) }
              }}
              sx={{
                display: 'inline-flex', alignItems: 'center', gap: 1, px: 1.25, py: 0.75, borderRadius: 2, cursor: 'pointer',
                font: 'inherit', fontSize: 14, color: 'text.primary', bgcolor: 'background.paper',
                border: '1px solid', borderColor: selected ? 'text.primary' : 'divider',
                boxShadow: selected ? (theme) => `0 0 0 1px ${theme.palette.text.primary}` : 'none',
                '&:hover': { borderColor: selected ? 'text.primary' : 'text.secondary' },
                '&:focus-visible': { outline: '2px solid', outlineColor: 'primary.main', outlineOffset: 2 },
              }}
            >
              <Box aria-hidden sx={{ width: 18, height: 18, borderRadius: '50%', bgcolor: ACCENT_PALETTES[key].swatch, display: 'grid', placeItems: 'center', color: '#fff' }}>
                {selected && <Check size={12} strokeWidth={3} />}
              </Box>
              <Typography component="span" variant="body2" sx={{ fontWeight: selected ? 600 : 400 }}>{name}</Typography>
            </Box>
          </Tooltip>
        )
      })}
    </Box>
  )
}
