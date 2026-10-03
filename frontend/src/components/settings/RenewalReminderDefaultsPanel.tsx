import { useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Chip, Paper, Skeleton, TextField, Typography } from '@mui/material'
import { BellRing, CalendarClock } from 'lucide-react'
import { adminRenewalsApi, type RenewalReminderDefaults } from '../../api/renewalsApi'
import { TimeZonePicker } from '../preferences/TimeZonePicker'
import { FieldGrid, SettingsSection } from './SettingsSection'
import { SaveBar } from './SaveBar'
import { useSettingsForm } from './useSettingsForm'

type Form = { daysBefore: string; sendTime: string; timeZone: string }
const TIME = /^([01]\d|2[0-3]):[0-5]\d$/

const toForm = (v: RenewalReminderDefaults): Form => ({ daysBefore: String(v.daysBefore), sendTime: v.sendTime, timeZone: v.timeZone })

/**
 * REQ-SUB-004.4/.5 (C64): Billing settings → Renewal reminders. The platform
 * defaults every user starts from; users can override the days and time and
 * turn reminders off.
 */
export function RenewalReminderDefaultsPanel({ onSaved }: { onSaved: () => void }) {
  const { t } = useTranslation()
  const validate = useCallback((f: Form) => {
    const days = Number(f.daysBefore)
    return {
      daysBefore: !Number.isInteger(days) || days < 1 || days > 30 ? t('admin.billingSettings.reminders.daysRange') : undefined,
      sendTime: !TIME.test(f.sendTime) ? t('admin.billingSettings.reminders.timeFormat') : undefined,
      timeZone: !f.timeZone ? t('admin.billingSettings.required') : undefined,
    }
  }, [t])
  const s = useSettingsForm<RenewalReminderDefaults, Form>({
    load: adminRenewalsApi.defaults,
    save: (f) => adminRenewalsApi.saveDefaults({ daysBefore: Number(f.daysBefore), sendTime: f.sendTime, timeZone: f.timeZone }),
    toForm, validate,
    loadErrorText: t('admin.billingSettings.loadError'), saveErrorText: t('admin.billingSettings.reminders.saveError'),
  })

  if (!s.form) {
    return s.loadError
      ? <Alert severity="error" action={<Button onClick={s.retry}>{t('admin.billingSettings.retry')}</Button>}>{s.loadError}</Alert>
      : <Skeleton variant="rounded" height={320} />
  }
  const f = s.form
  const days = Math.min(30, Math.max(1, Number(f.daysBefore) || 1))
  const save = () => { void s.submit().then((ok) => { if (ok) onSaved() }) }

  return (
    <>
      <Box sx={{ display: 'grid', gap: 3, gridTemplateColumns: { xs: '1fr', lg: 'minmax(0, 2fr) minmax(0, 1fr)' }, alignItems: 'start' }}>
        <SettingsSection id="reminder-defaults" icon={BellRing} accent="amber" title={t('admin.billingSettings.reminders.title')}
          description={t('admin.billingSettings.reminders.description')}>
          <FieldGrid columns={3}>
            <TextField label={t('admin.billingSettings.reminders.days')} value={f.daysBefore} required
              onChange={(e) => s.set('daysBefore', e.target.value.replace(/[^0-9]/g, ''))}
              error={Boolean(s.shownError('daysBefore'))} helperText={s.shownError('daysBefore') ?? t('admin.billingSettings.reminders.daysHelp')}
              slotProps={{ htmlInput: { inputMode: 'numeric' } }} />
            <TextField label={t('admin.billingSettings.reminders.time')} type="time" value={f.sendTime} required
              onChange={(e) => s.set('sendTime', e.target.value)}
              error={Boolean(s.shownError('sendTime'))} helperText={s.shownError('sendTime') ?? t('admin.billingSettings.reminders.timeHelp')}
              slotProps={{ inputLabel: { shrink: true } }} />
            <TimeZonePicker fullWidth required label={t('admin.billingSettings.reminders.zone')} value={f.timeZone}
              onChange={(zone) => s.set('timeZone', zone)}
              error={Boolean(s.shownError('timeZone'))} helperText={s.shownError('timeZone') ?? t('admin.billingSettings.reminders.zoneHelp')} />
          </FieldGrid>
          <Alert severity="info" sx={{ mt: 2 }}>{t('admin.billingSettings.reminders.userNote')}</Alert>
        </SettingsSection>

        <Paper variant="outlined" component="section" aria-labelledby="reminder-preview-title" sx={{ p: 2.5, borderRadius: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1.5 }}>
            <CalendarClock size={18} aria-hidden />
            <Typography id="reminder-preview-title" component="h2" variant="subtitle1" sx={{ fontWeight: 700 }}>
              {t('admin.billingSettings.reminders.previewTitle')}
            </Typography>
          </Box>
          <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1.5 }}>
            {t('admin.billingSettings.reminders.previewBody', { time: f.sendTime, zone: f.timeZone })}
          </Typography>
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.75 }}>
            {Array.from({ length: days }, (_, i) => days - i).map((d) => (
              <Chip key={d} size="small" color={d === 1 ? 'warning' : 'default'} label={t('admin.billingSettings.reminders.daysLeft', { count: d })} />
            ))}
            <Chip size="small" color="success" label={t('admin.billingSettings.reminders.renewalDay')} />
          </Box>
        </Paper>
      </Box>
      <SaveBar dirty={s.dirty} busy={s.busy} updatedAt={s.saved?.updatedAt} error={s.error} onSave={save} onCancel={s.reset} />
    </>
  )
}
