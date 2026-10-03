import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Skeleton, Snackbar, Switch, TextField, Typography } from '@mui/material'
import { ApiError } from '../../api/client'
import { renewalsApi, type RenewalReminderPreferences } from '../../api/renewalsApi'
import { PreferenceRow, PreferenceSection } from './PreferenceLayout'
import { TimeZonePicker } from './TimeZonePicker'
import { useLocalePreference } from '../../theming/LocalePreferenceProvider'

const TIME = /^([01]\d|2[0-3]):[0-5]\d$/

/**
 * REQ-SUB-004.8 (C64): the user's renewal reminder settings on Preferences,
 * laid out as preference rows (C67/C68). Empty days or time = the platform default.
 * Turning reminders off stops the emails only, never the renewal itself
 * (.10). Saved with its own button, as before.
 */
export function RenewalRemindersCard() {
  const { t } = useTranslation()
  // The reminder clock is the account time zone (RenewalReminderService
  // reads CustomerPreference.timeZone, else the platform default). The picker
  // shows the zone the server will use; changing it is the same change as
  // under Language & Formats, which PreferenceSync saves to the account.
  const { setTimeZone } = useLocalePreference()
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

  const section = (children: React.ReactNode) => (
    <PreferenceSection id="renewal-reminders" title={t('preferences.renewalReminders.title')} description={t('preferences.renewalReminders.description')}>
      {children}
    </PreferenceSection>
  )

  if (loadError) {
    return section(
      <Alert severity="error" sx={{ mt: 1 }} action={<Button onClick={() => void load()}>{t('preferences.renewalReminders.retry')}</Button>}>
        {loadError}
      </Alert>,
    )
  }
  if (!prefs) return section(<Skeleton variant="rounded" height={200} sx={{ mt: 1 }} />)

  const daysNumber = days === '' ? null : Number(days)
  const daysError = daysNumber !== null && (!Number.isInteger(daysNumber) || daysNumber < prefs.minDays || daysNumber > prefs.maxDays)
    ? t('preferences.renewalReminders.daysRange', { min: prefs.minDays, max: prefs.maxDays }) : null
  const timeError = time !== '' && !TIME.test(time) ? t('preferences.renewalReminders.timeFormat') : null
  const shownDays = daysNumber ?? prefs.platformDaysBefore
  const shownTime = time || prefs.platformSendTime

  const changeZone = (zone: string) => {
    setTimeZone(zone)
    setPrefs({ ...prefs, effectiveTimeZone: zone })
  }

  const save = () => {
    if (daysError || timeError) return
    setBusy(true)
    setSaveError(null)
    renewalsApi.savePreferences({ enabled, daysBefore: daysNumber, sendTime: time || null })
      .then((p) => { apply(p); setToast(t('preferences.renewalReminders.saved')) })
      .catch((e) => setSaveError(e instanceof ApiError ? e.message : t('preferences.renewalReminders.saveError')))
      .finally(() => setBusy(false))
  }

  return section(
    <>
      <PreferenceRow label={t('preferences.renewalReminders.enabled')} labelId="renewal-enabled-label"
        hint={enabled ? t('preferences.renewalReminders.enabledHint') : t('preferences.renewalReminders.offNote')}
        control={<Switch checked={enabled} onChange={(e) => setEnabled(e.target.checked)} slotProps={{ input: { 'aria-labelledby': 'renewal-enabled-label' } }} />} />
      <PreferenceRow label={t('preferences.renewalReminders.daysLabel')} labelFor="renewal-days"
        hint={daysError
          ? <Typography component="span" variant="body2" color="error">{daysError}</Typography>
          : t('preferences.renewalReminders.platformDays', { days: prefs.platformDaysBefore })}
        control={(
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25 }}>
            <TextField id="renewal-days" size="small" value={days} disabled={!enabled} placeholder={String(prefs.platformDaysBefore)}
              onChange={(e) => setDays(e.target.value.replace(/[^0-9]/g, ''))} error={Boolean(daysError)} sx={{ width: 96 }}
              slotProps={{ htmlInput: { inputMode: 'numeric', min: prefs.minDays, max: prefs.maxDays, 'aria-label': t('preferences.renewalReminders.days') } }} />
            <Typography variant="body2" aria-hidden sx={{ color: 'text.secondary', whiteSpace: 'nowrap' }}>{t('preferences.renewalReminders.daysSuffix')}</Typography>
          </Box>
        )} />
      <PreferenceRow label={t('preferences.renewalReminders.time')} labelFor="renewal-time"
        hint={timeError
          ? <Typography component="span" variant="body2" color="error">{timeError}</Typography>
          : t('preferences.renewalReminders.platformTime', { time: prefs.platformSendTime })}
        control={(
          <TextField id="renewal-time" size="small" type="time" value={time} disabled={!enabled} error={Boolean(timeError)}
            onChange={(e) => setTime(e.target.value)} sx={{ width: 160 }} />
        )} />
      <PreferenceRow label={t('preferences.timeZone')} hint={t('preferences.renewalReminders.timeZoneHint')} labelId="renewal-timezone-label"
        control={<TimeZonePicker value={prefs.effectiveTimeZone} onChange={changeZone} labelId="renewal-timezone-label" />} />

      <Box sx={(theme) => ({
        mt: 1, px: 2, py: 1.5, borderRadius: 2, border: '1px solid', borderColor: 'divider',
        bgcolor: theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.03)' : '#F8FAFC',
      })}>
        <Typography variant="body2" role="status" sx={{ color: enabled ? 'text.primary' : 'text.secondary' }}>
          {enabled
            ? t('preferences.renewalReminders.summary', { days: shownDays, time: shownTime, zone: prefs.effectiveTimeZone })
            : t('preferences.renewalReminders.summaryOff')}
        </Typography>
      </Box>
      {saveError && <Alert severity="error" sx={{ mt: 2 }}>{saveError}</Alert>}
      <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 3 }}>
        <Button variant="contained" disabled={busy || Boolean(daysError) || Boolean(timeError)} onClick={save}>
          {t('preferences.renewalReminders.save')}
        </Button>
      </Box>
      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)} message={toast ?? ''} />
    </>,
  )
}
