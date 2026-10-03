import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, FormControlLabel, Skeleton, Snackbar, Switch, TextField, Typography } from '@mui/material'
import { BellRing } from 'lucide-react'
import { ApiError } from '../../api/client'
import { renewalsApi, type RenewalReminderPreferences } from '../../api/renewalsApi'
import { SettingsSection } from '../settings/SettingsSection'

const TIME = /^([01]\d|2[0-3]):[0-5]\d$/

/**
 * REQ-SUB-004.8 (C64): the user's renewal reminder settings on Preferences.
 * Empty days or time = the platform default. Turning reminders off stops the
 * emails only, never the renewal itself (.10).
 */
export function RenewalRemindersCard() {
  const { t } = useTranslation()
  const [prefs, setPrefs] = useState<RenewalReminderPreferences | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [enabled, setEnabled] = useState(true)
  const [days, setDays] = useState('')
  const [time, setTime] = useState('')
  const [busy, setBusy] = useState(false)
  const [saveError, setSaveError] = useState<string | null>(null)
  const [toast, setToast] = useState<string | null>(null)

  const apply = (p: RenewalReminderPreferences) => {
    setPrefs(p)
    setEnabled(p.enabled)
    setDays(p.daysBefore == null ? '' : String(p.daysBefore))
    setTime(p.sendTime ?? '')
  }
  const load = useCallback(() => renewalsApi.preferences()
    .then((p) => { apply(p); setLoadError(null) })
    .catch((e) => setLoadError(e instanceof ApiError ? e.message : t('preferences.renewalReminders.loadError'))), [t])
  useEffect(() => { void load() }, [load])

  if (loadError) {
    return (
      <Alert severity="error" sx={{ mt: 3 }} action={<Button onClick={() => void load()}>{t('preferences.renewalReminders.retry')}</Button>}>
        {loadError}
      </Alert>
    )
  }
  if (!prefs) return <Skeleton variant="rounded" height={220} sx={{ mt: 3 }} />

  const daysNumber = days === '' ? null : Number(days)
  const daysError = daysNumber !== null && (!Number.isInteger(daysNumber) || daysNumber < prefs.minDays || daysNumber > prefs.maxDays)
    ? t('preferences.renewalReminders.daysRange', { min: prefs.minDays, max: prefs.maxDays }) : null
  const timeError = time !== '' && !TIME.test(time) ? t('preferences.renewalReminders.timeFormat') : null
  const shownDays = daysNumber ?? prefs.platformDaysBefore
  const shownTime = time || prefs.platformSendTime

  const save = () => {
    if (daysError || timeError) return
    setBusy(true)
    setSaveError(null)
    renewalsApi.savePreferences({ enabled, daysBefore: daysNumber, sendTime: time || null })
      .then((p) => { apply(p); setToast(t('preferences.renewalReminders.saved')) })
      .catch((e) => setSaveError(e instanceof ApiError ? e.message : t('preferences.renewalReminders.saveError')))
      .finally(() => setBusy(false))
  }

  return (
    <Box sx={{ mt: 3 }}>
      <SettingsSection id="renewal-reminders" icon={BellRing} accent="amber" title={t('preferences.renewalReminders.title')}
        description={t('preferences.renewalReminders.description')}>
        <FormControlLabel control={<Switch checked={enabled} onChange={(e) => setEnabled(e.target.checked)} />}
          label={t('preferences.renewalReminders.enabled')} />
        {!enabled && <Alert severity="info" sx={{ mt: 1 }}>{t('preferences.renewalReminders.offNote')}</Alert>}
        <Box sx={{ display: 'grid', gap: 2, mt: 2, gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, minmax(0, 1fr))' } }}>
          <TextField label={t('preferences.renewalReminders.days')} value={days} disabled={!enabled}
            onChange={(e) => setDays(e.target.value.replace(/[^0-9]/g, ''))}
            error={Boolean(daysError)} helperText={daysError ?? t('preferences.renewalReminders.platformDays', { days: prefs.platformDaysBefore })}
            slotProps={{ htmlInput: { inputMode: 'numeric', min: prefs.minDays, max: prefs.maxDays } }} />
          <TextField label={t('preferences.renewalReminders.time')} type="time" value={time} disabled={!enabled}
            onChange={(e) => setTime(e.target.value)}
            error={Boolean(timeError)} helperText={timeError ?? t('preferences.renewalReminders.platformTime', { time: prefs.platformSendTime })}
            slotProps={{ inputLabel: { shrink: true } }} />
        </Box>
        <Typography variant="body2" sx={{ color: 'text.secondary', mt: 2 }}>
          {t('preferences.renewalReminders.timeZone', { zone: prefs.effectiveTimeZone })}
        </Typography>
        {enabled && (
          <Typography variant="body2" role="status" sx={{ mt: 1, fontWeight: 600 }}>
            {t('preferences.renewalReminders.summary', { days: shownDays, time: shownTime, zone: prefs.effectiveTimeZone })}
          </Typography>
        )}
        {saveError && <Alert severity="error" sx={{ mt: 2 }}>{saveError}</Alert>}
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 2 }}>
          <Button variant="contained" disabled={busy || Boolean(daysError) || Boolean(timeError)} onClick={save}>
            {t('preferences.renewalReminders.save')}
          </Button>
        </Box>
      </SettingsSection>
      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)} message={toast ?? ''} />
    </Box>
  )
}
