import { Box, FormControlLabel, Radio, RadioGroup, TextField, Tooltip, Typography } from '@mui/material'
import { Check } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { COLOR_PRESETS, isHexColor } from '../../utils/showcaseColor'

/**
 * C66: choose a showcase colour without typing hex — "use the default" (or
 * "use the platform colour" for an app) versus a custom colour picked from
 * presets, the browser colour picker or a hex field. `value` null means the
 * default / inherited colour.
 */
export function ColorPicker({ id, value, onChange, defaultLabel, defaultColor }: {
  id: string; value: string | null; onChange: (value: string | null) => void
  /** Label of the "no custom colour" option, e.g. "Use default EIS colour". */
  defaultLabel: string
  /** The colour used when no custom colour is chosen (shown as a swatch). */
  defaultColor: string
}) {
  const { t } = useTranslation()
  const custom = value !== null
  const hexError = custom && !isHexColor(value)
  return (
    <Box>
      <RadioGroup row value={custom ? 'custom' : 'default'} aria-label={t('ui.color.mode')}
        onChange={(e) => onChange(e.target.value === 'custom' ? (value ?? defaultColor) : null)}>
        <FormControlLabel value="default" control={<Radio />} label={(
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Box aria-hidden sx={{ width: 14, height: 14, borderRadius: '50%', bgcolor: defaultColor }} />{defaultLabel}
          </Box>
        )} />
        <FormControlLabel value="custom" control={<Radio />} label={t('ui.color.custom')} />
      </RadioGroup>
      {custom && (
        <Box sx={{ mt: 1.5, display: 'flex', flexDirection: 'column', gap: 1.5 }}>
          <Box role="group" aria-label={t('ui.color.presets')} sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
            {COLOR_PRESETS.map((preset) => {
              const selected = value?.toUpperCase() === preset.hex
              const name = t(`ui.color.preset.${preset.key}`)
              return (
                <Tooltip key={preset.key} title={`${name} ${preset.hex}`}>
                  <Box component="button" type="button" aria-label={name} aria-pressed={selected} onClick={() => onChange(preset.hex)}
                    sx={{
                      width: 32, height: 32, borderRadius: '50%', bgcolor: preset.hex, border: '2px solid', cursor: 'pointer', p: 0,
                      borderColor: selected ? 'text.primary' : 'transparent', display: 'grid', placeItems: 'center', color: '#fff',
                      outlineOffset: 2,
                    }}>
                    {selected && <Check size={16} aria-hidden />}
                  </Box>
                </Tooltip>
              )
            })}
          </Box>
          <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'flex-start' }}>
            <Box component="input" type="color" id={`${id}-picker`} aria-label={t('ui.color.picker')}
              value={isHexColor(value) ? value : defaultColor} onChange={(e: React.ChangeEvent<HTMLInputElement>) => onChange(e.target.value.toUpperCase())}
              sx={{ width: 44, height: 40, p: 0.25, border: '1px solid', borderColor: 'divider', borderRadius: 2, bgcolor: 'background.paper', cursor: 'pointer' }} />
            <TextField size="small" id={`${id}-hex`} label={t('ui.color.hex')} value={value ?? ''} sx={{ width: 160 }}
              onChange={(e) => onChange(e.target.value.trim())} error={hexError}
              helperText={hexError ? t('ui.color.hexError') : undefined} slotProps={{ htmlInput: { maxLength: 7 } }} />
          </Box>
          <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t('ui.color.hint')}</Typography>
        </Box>
      )}
    </Box>
  )
}
