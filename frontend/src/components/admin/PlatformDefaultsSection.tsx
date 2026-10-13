import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, MenuItem, Paper, TextField, Typography } from '@mui/material'
import { ApiError } from '../../api/client'
import { platformDefaultsApi, type PlatformDefaults } from '../../api/messageTemplatesApi'
import { platformAdministrationApi, type PlatformCurrency, type SupportedLanguage } from '../../api/platformAdministrationApi'
import { DATE_FORMATS } from '../../theming/formatOptions'

const NONE = ''

/**
 * REQ-GOV-002 (C92, TM-2 A): the defaults new users start with: language, region, currency, time zone, date format
 * and renewal reminder days. A user's own choice always wins; an empty field means "no platform default".
 */
export function PlatformDefaultsSection() {
  const { t } = useTranslation()
  const [defaults, setDefaults] = useState<PlatformDefaults | null>(null)
  const [languages, setLanguages] = useState<SupportedLanguage[]>([])
  const [currencies, setCurrencies] = useState<PlatformCurrency[]>([])
  const [form, setForm] = useState({ language: NONE, region: NONE, currency: NONE, timeZone: NONE, dateFormat: NONE, reminderDays: '' })
  const [error, setError] = useState<string | null>(null)
  const [saved, setSaved] = useState(false)
  const [busy, setBusy] = useState(false)

  const apply = (d: PlatformDefaults) => {
    setDefaults(d)
    setForm({
      language: d.language ?? NONE, region: d.region ?? NONE, currency: d.currency ?? NONE, timeZone: d.timeZone ?? NONE,
      dateFormat: d.dateFormat ?? NONE, reminderDays: d.reminderDays != null ? String(d.reminderDays) : '',
    })
  }

  useEffect(() => {
    platformDefaultsApi.get().then(apply).catch((e) => setError(e instanceof ApiError ? e.message : t('platformDefaults.loadError')))
    platformAdministrationApi.listSupportedLanguages().then(setLanguages).catch(() => setLanguages([]))
    platformAdministrationApi.listCurrencies().then(setCurrencies).catch(() => setCurrencies([]))
  }, [t])

  const set = (key: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) => { setSaved(false); setForm((f) => ({ ...f, [key]: e.target.value })) }

  const save = () => {
    setBusy(true)
    setError(null)
    platformDefaultsApi.save({
      language: form.language, region: form.region.trim(), currency: form.currency, timeZone: form.timeZone.trim(),
      dateFormat: form.dateFormat, reminderDays: form.reminderDays ? Number(form.reminderDays) : null,
    })
      .then((d) => { apply(d); setSaved(true) })
      .catch((e) => setError(e instanceof ApiError ? e.message : t('platformDefaults.saveError')))
      .finally(() => setBusy(false))
  }

  return (
    <Paper variant="outlined" sx={{ p: 2.5 }} component="section" aria-labelledby="platform-defaults-title">
      <Typography id="platform-defaults-title" variant="subtitle1" component="h5" sx={{ fontWeight: 700, mb: 0.5 }}>{t('platformDefaults.title')}</Typography>
      <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1.5 }}>{t('platformDefaults.description')}</Typography>
      {error && <Alert severity="error" sx={{ mb: 1.5 }}>{error}</Alert>}
      {saved && <Alert severity="success" sx={{ mb: 1.5 }}>{t('platformDefaults.saved')}</Alert>}
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }}>
        <TextField select size="small" label={t('platformDefaults.language')} value={form.language} onChange={set('language')} disabled={!defaults}>
          <MenuItem value={NONE}>{t('platformDefaults.none')}</MenuItem>
          {languages.map((l) => <MenuItem key={l.code} value={l.code}>{`${l.label} (${l.code})`}</MenuItem>)}
        </TextField>
        <TextField size="small" label={t('platformDefaults.region')} value={form.region} onChange={set('region')} disabled={!defaults}
          helperText={t('platformDefaults.regionHelp')} />
        <TextField select size="small" label={t('platformDefaults.currency')} value={form.currency} onChange={set('currency')} disabled={!defaults}>
          <MenuItem value={NONE}>{t('platformDefaults.none')}</MenuItem>
          {currencies.filter((c) => c.enabled || c.code === form.currency).map((c) => <MenuItem key={c.code} value={c.code}>{`${c.name} (${c.code})`}</MenuItem>)}
        </TextField>
        <TextField size="small" label={t('platformDefaults.timeZone')} value={form.timeZone} onChange={set('timeZone')} disabled={!defaults}
          helperText={t('platformDefaults.timeZoneHelp')} />
        <TextField select size="small" label={t('platformDefaults.dateFormat')} value={form.dateFormat} onChange={set('dateFormat')} disabled={!defaults}>
          <MenuItem value={NONE}>{t('platformDefaults.none')}</MenuItem>
          {DATE_FORMATS.map((f) => <MenuItem key={f} value={f}>{f === 'auto' ? t('preferences.regionDefault') : f}</MenuItem>)}
        </TextField>
        <TextField size="small" type="number" label={t('platformDefaults.reminderDays')} value={form.reminderDays} onChange={set('reminderDays')}
          disabled={!defaults} helperText={t('platformDefaults.reminderDaysHelp')} slotProps={{ htmlInput: { min: 1, max: 30 } }} />
      </Box>
      <Box sx={{ mt: 2 }}>
        <Button variant="contained" size="small" disabled={busy || !defaults} onClick={save}>{t('platformDefaults.save')}</Button>
      </Box>
    </Paper>
  )
}
