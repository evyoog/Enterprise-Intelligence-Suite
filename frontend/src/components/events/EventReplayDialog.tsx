import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, List, ListItem, ListItemText, MenuItem, TextField, Typography } from '@mui/material'
import { ApiError } from '../../api/client'
import { adminEventsApi, type ReplayResult } from '../../api/eventsApi'

interface Props {
  open: boolean
  eventTypes: string[]
  onClose: () => void
}

const today = () => new Date().toISOString().slice(0, 10)

/**
 * Replay (REQ-INT-002.9, C91, EV-1 B): re-send the delivered events of a time range to ONE receiver, for example after a
 * product was down. A preview counts the events first; only delivered events are sent and the receiver must be safe to
 * run twice. One replay is limited in size by the server.
 */
export function EventReplayDialog({ open, eventTypes, onClose }: Props) {
  const { t } = useTranslation()
  const [receivers, setReceivers] = useState<string[]>([])
  const [receiver, setReceiver] = useState('')
  const [from, setFrom] = useState(today())
  const [to, setTo] = useState(today())
  const [type, setType] = useState('')
  const [result, setResult] = useState<ReplayResult | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    if (open) adminEventsApi.receivers().then(setReceivers).catch(() => setReceivers([]))
  }, [open])

  const body = (preview: boolean) => ({ receiver, from, to, eventType: type || undefined, preview })
  const run = (preview: boolean) => {
    setBusy(true)
    setError(null)
    adminEventsApi.replay(body(preview)).then(setResult)
      .catch((e) => setError(e instanceof ApiError ? e.message : t('admin.events.replay.error')))
      .finally(() => setBusy(false))
  }
  const changed = () => { setResult(null); setError(null) }
  const valid = receiver !== '' && from !== '' && to !== '' && to >= from
  const done = result !== null && !result.preview

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm" aria-labelledby="event-replay-title">
      <DialogTitle id="event-replay-title">{t('admin.events.replay.title')}</DialogTitle>
      <DialogContent>
        <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2 }}>{t('admin.events.replay.intro')}</Typography>
        <TextField select fullWidth size="small" label={t('admin.events.replay.receiver')} value={receiver}
          onChange={(e) => { setReceiver(e.target.value); changed() }} sx={{ mb: 2 }}
          helperText={receivers.length === 0 ? t('admin.events.replay.noReceivers') : undefined}>
          {receivers.map((r) => <MenuItem key={r} value={r}>{r}</MenuItem>)}
        </TextField>
        <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mb: 2 }}>
          <TextField type="date" size="small" label={t('admin.events.filter.from')} value={from} sx={{ flex: 1, minWidth: 150 }}
            onChange={(e) => { setFrom(e.target.value); changed() }} slotProps={{ inputLabel: { shrink: true } }} />
          <TextField type="date" size="small" label={t('admin.events.filter.to')} value={to} sx={{ flex: 1, minWidth: 150 }}
            onChange={(e) => { setTo(e.target.value); changed() }} slotProps={{ inputLabel: { shrink: true } }}
            error={to !== '' && from !== '' && to < from} helperText={to !== '' && from !== '' && to < from ? t('admin.events.replay.rangeError') : undefined} />
        </Box>
        <TextField select fullWidth size="small" label={t('admin.events.filter.type')} value={type}
          onChange={(e) => { setType(e.target.value); changed() }}>
          <MenuItem value="">{t('admin.events.filter.allTypes')}</MenuItem>
          {eventTypes.map((x) => <MenuItem key={x} value={x}>{x}</MenuItem>)}
        </TextField>

        {error && <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>}
        {result && (
          <Box sx={{ mt: 2 }}>
            {result.preview && (
              <Alert severity={result.matched > result.limit ? 'warning' : 'info'}>
                {t('admin.events.replay.previewSummary', { count: result.matched, receiver: result.receiver })}
                {result.matched > result.limit ? ` ${t('admin.events.replay.tooMany', { limit: result.limit })}` : ''}
              </Alert>
            )}
            {done && (
              <Alert severity={result.failed > 0 ? 'warning' : 'success'}>
                {t('admin.events.replay.doneSummary', { replayed: result.replayed, failed: result.failed, receiver: result.receiver })}
              </Alert>
            )}
            {result.failures.length > 0 && (
              <>
                <Typography component="h3" variant="subtitle2" sx={{ mt: 1.5 }}>{t('admin.events.replay.failures')}</Typography>
                <List dense>
                  {result.failures.map((f) => (
                    <ListItem key={f.eventId} disableGutters><ListItemText primary={`${f.eventType} — ${f.eventId}`} secondary={f.error} /></ListItem>
                  ))}
                </List>
              </>
            )}
          </Box>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>{done ? t('admin.events.close') : t('admin.events.replay.cancel')}</Button>
        <Button disabled={!valid || busy} onClick={() => run(true)}>{t('admin.events.replay.preview')}</Button>
        <Button variant="contained" color="warning" disabled={!valid || busy || !result?.preview || result.matched === 0 || result.matched > result.limit || done}
          onClick={() => run(false)}>
          {t('admin.events.replay.send')}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
