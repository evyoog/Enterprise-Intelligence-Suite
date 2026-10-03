import { useMemo } from 'react'
import { Autocomplete, TextField } from '@mui/material'
import { useTranslation } from 'react-i18next'
import { SUPPORTED_TIMEZONES } from '../../theming/LocalePreferenceProvider'
import { CONTROL_COLUMN } from './PreferenceLayout'

/** Every IANA zone the browser knows, plus the short list (whose names some
 * browsers report under older aliases, e.g. Asia/Calcutta for Asia/Kolkata). */
function allZones(): string[] {
  let zones: string[]
  try {
    zones = (Intl as unknown as { supportedValuesOf?: (key: string) => string[] }).supportedValuesOf?.('timeZone') ?? []
  } catch {
    zones = []
  }
  return [...new Set([...SUPPORTED_TIMEZONES.map((z) => z.code), ...zones, 'UTC'])].sort()
}

/** "GMT+5:30" for a zone, now. */
function offsetOf(zone: string): string {
  try {
    return new Intl.DateTimeFormat('en-US', { timeZone: zone, timeZoneName: 'shortOffset' })
      .formatToParts(new Date()).find((p) => p.type === 'timeZoneName')?.value ?? ''
  } catch {
    return ''
  }
}

/**
 * C69: a searchable picker over all IANA time zones (type a city or region),
 * each shown with its current UTC offset. Used for the account time zone on
 * Preferences — the same value the renewal reminder emails use — and for
 * the platform default reminder zone in Billing settings.
 */
export function TimeZonePicker({ value, onChange, labelId, label, helperText, error, required, fullWidth }: {
  value: string; onChange: (zone: string) => void
  /** Preference rows: the id of the row label that names the field. */
  labelId?: string
  /** Forms: a visible field label instead (with optional helper text). */
  label?: string; helperText?: string; error?: boolean; required?: boolean; fullWidth?: boolean
}) {
  const { t } = useTranslation()
  const options = useMemo(() => {
    const zones = allZones()
    return zones.includes(value) ? zones : [value, ...zones]
  }, [value])
  const offsets = useMemo(() => new Map(options.map((z) => [z, offsetOf(z)])), [options])
  const zoneLabel = (zone: string) => {
    const offset = offsets.get(zone)
    return offset ? `${zone.replace(/_/g, ' ')} (${offset})` : zone.replace(/_/g, ' ')
  }

  return (
    <Autocomplete
      disableClearable value={value} options={options}
      onChange={(_, zone) => zone && onChange(zone)}
      getOptionLabel={zoneLabel}
      // Match on the IANA name too, so "Kolkata", "new york" and "GMT+9" all work.
      filterOptions={(opts, { inputValue }) => {
        const q = inputValue.trim().toLowerCase().replace(/\s+/g, '_')
        const text = inputValue.trim().toLowerCase()
        return q ? opts.filter((z) => z.toLowerCase().includes(q) || zoneLabel(z).toLowerCase().includes(text)) : opts
      }}
      noOptionsText={t('preferences.timeZoneNone')}
      sx={fullWidth ? undefined : { width: CONTROL_COLUMN, maxWidth: '100%' }} fullWidth={fullWidth} size={label ? 'medium' : 'small'}
      slotProps={{ popper: { sx: { minWidth: 300 } } }}
      renderInput={(params) => (
        <TextField {...params} label={label} helperText={helperText} error={error} required={required}
          placeholder={t('preferences.timeZoneSearch')}
          slotProps={{ ...params.slotProps, htmlInput: { ...params.slotProps.htmlInput, ...(labelId ? { 'aria-labelledby': labelId } : {}) } }} />
      )}
    />
  )
}
