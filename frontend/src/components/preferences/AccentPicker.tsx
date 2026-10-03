import { Box, Typography } from '@mui/material'
import { Check } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { ACCENT_KEYS, ACCENT_PALETTES, type AccentKey } from '../../theming/accentPalette'

/**
 * C67/C68: the platform accent as a radio group of small circular swatches
 * with their names. Arrow keys move between colours; the selected swatch
 * gets a check and a thin outline ring.
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
    <Box role="radiogroup" aria-labelledby={labelledBy}
      sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(112px, 1fr))', gap: 1, mt: 2, maxWidth: 720 }}>
      {ACCENT_KEYS.map((key, index) => {
        const selected = key === value
        const swatch = ACCENT_PALETTES[key].swatch
        return (
          <Box
            key={key} component="button" type="button" id={`accent-${key}`} role="radio" aria-checked={selected}
            tabIndex={selected ? 0 : -1}
            onClick={() => onChange(key)}
            onKeyDown={(e: React.KeyboardEvent) => {
              if (e.key === 'ArrowRight' || e.key === 'ArrowDown') { e.preventDefault(); move(index, 1) }
              if (e.key === 'ArrowLeft' || e.key === 'ArrowUp') { e.preventDefault(); move(index, -1) }
            }}
            sx={{
              display: 'flex', alignItems: 'center', gap: 1.25, px: 1, py: 0.75, borderRadius: 2, cursor: 'pointer',
              font: 'inherit', color: 'text.primary', bgcolor: 'transparent', textAlign: 'left',
              border: '1px solid', borderColor: selected ? 'divider' : 'transparent',
              '&:hover': { bgcolor: 'action.hover' },
              '&:focus-visible': { outline: '2px solid', outlineColor: 'primary.main', outlineOffset: 1 },
            }}
          >
            <Box aria-hidden sx={{
              width: 22, height: 22, flexShrink: 0, borderRadius: '50%', bgcolor: swatch, color: '#fff',
              display: 'grid', placeItems: 'center',
              boxShadow: selected ? (theme) => `0 0 0 2px ${theme.palette.background.paper}, 0 0 0 3.5px ${swatch}` : 'none',
            }}>
              {selected && <Check size={13} strokeWidth={3} />}
            </Box>
            <Typography component="span" variant="body2" sx={{ fontWeight: selected ? 600 : 400 }}>{t(`preferences.accent.${key}`)}</Typography>
          </Box>
        )
      })}
    </Box>
  )
}
