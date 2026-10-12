import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Button, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, Switch, TextField, Typography } from '@mui/material'
import { ApiError } from '../../api/client'
import { adminEventsApi } from '../../api/eventsApi'

interface Props {
  open: boolean
  onClose: () => void
}

/**
 * Event platform settings (REQ-INT-002, C91): how many days delivered events are kept (30 by default, EV-3) and who is
 * told when an event fails for good, in the app and by email (EV-4). Replay only works while events are kept.
 */
export function EventSettingsDialog({ open, onClose }: Props) {
  const { t } = useTranslation()
  const [retention, setRetention] = useState('30')
  const [recipients, setRecipients] = useState('')
  const [inApp, setInApp] = useState(true)
  const [email, setEmail] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [saved, setSaved] = useState(false)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    if (!open) return
    adminEventsApi.settings().then((s) => {
      setRetention(String(s.retentionDays))
      setRecipients(s.alertRecipients.join('\n'))
      setInApp(s.alertInApp)
      setEmail(s.alertEmail)
    }).catch((e) => setError(e instanceof ApiError ? e.message : t('admin.events.settings.loadError')))
  }, [open, t])

  const days = Number(retention)
  const valid = Number.isInteger(days) && days >= 1 && days <= 3650
  const save = () => {
    setBusy(true)
    setError(null)
    setSaved(false)
    adminEventsApi.updateSettings({
      retentionDays: days,
      alertRecipients: recipients.split(/[\s,;]+/).filter(Boolean),
      alertInApp: inApp,
      alertEmail: email,
    }).then((s) => { setRecipients(s.alertRecipients.join('\n')); setSaved(true) })
      .catch((e) => setError(e instanceof ApiError ? e.message : t('admin.events.settings.saveError')))
      .finally(() => setBusy(false))
  }

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm" aria-labelledby="event-settings-title">
      <DialogTitle id="event-settings-title">{t('admin.events.settings.title')}</DialogTitle>
      <DialogContent>
        <TextField type="number" fullWidth size="small" label={t('admin.events.settings.retention')} value={retention}
          onChange={(e) => { setRetention(e.target.value); setSaved(false) }} sx={{ mt: 1, mb: 2 }}
          error={!valid} helperText={valid ? t('admin.events.settings.retentionHelp') : t('admin.events.settings.retentionRange')}
          slotProps={{ htmlInput: { min: 1, max: 3650 } }} />
        <Typography component="h3" variant="subtitle2" sx={{ mb: 0.5 }}>{t('admin.events.settings.alerts')}</Typography>
        <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1.5 }}>{t('admin.events.settings.alertsHelp')}</Typography>
        <TextField multiline minRows={3} fullWidth size="small" label={t('admin.events.settings.recipients')} value={recipients}
          onChange={(e) => { setRecipients(e.target.value); setSaved(false) }} helperText={t('admin.events.settings.recipientsHelp')} />
        <FormControlLabel sx={{ display: 'block', mt: 1 }} label={t('admin.events.settings.inApp')}
          control={<Switch checked={inApp} onChange={(e) => { setInApp(e.target.checked); setSaved(false) }} />} />
        <FormControlLabel sx={{ display: 'block' }} label={t('admin.events.settings.email')}
          control={<Switch checked={email} onChange={(e) => { setEmail(e.target.checked); setSaved(false) }} />} />
        {error && <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>}
        {saved && <Alert severity="success" sx={{ mt: 2 }}>{t('admin.events.settings.saved')}</Alert>}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>{t('admin.events.close')}</Button>
        <Button variant="contained" disabled={!valid || busy} onClick={save}>{t('admin.events.settings.save')}</Button>
      </DialogActions>
    </Dialog>
  )
}
