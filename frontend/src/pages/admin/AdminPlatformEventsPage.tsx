import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, Pagination, Paper, Skeleton,
  Snackbar, Tab, Table, TableBody, TableCell, TableHead, TableRow, Tabs, TextField, Typography,
} from '@mui/material'
import { AlertTriangle, CheckCircle2, Clock, Radio, RotateCcw } from 'lucide-react'
import { ApiError } from '../../api/client'
import { adminEventsApi, type EventStatus, type PlatformEventDetail, type PlatformEventPage } from '../../api/eventsApi'
import { PageHeader } from '../../components/layout/PageHeader'
import { StatusTile } from '../../components/settings/SettingsSection'
import { useLocalePreference } from '../../theming/LocalePreferenceProvider'
import { ToolSyncPanel } from './ToolSyncPanel'

const STATUS_COLOR: Record<EventStatus, 'info' | 'success' | 'error'> = { PENDING: 'info', DELIVERED: 'success', FAILED: 'error' }

function prettyPayload(payload: string) {
  try {
    return JSON.stringify(JSON.parse(payload), null, 2)
  } catch {
    return payload
  }
}

/**
 * "/admin/integrations/events" — REQ-INT-002.6 (C62), `MANAGE_INTEGRATIONS`:
 * the platform's outbox events with filters, a detail dialog and Retry for
 * FAILED events; and (REQ-INT-003) the "Tool sync" tab, the monitor of what
 * is sent to the connected tools.
 */
export function AdminPlatformEventsPage() {
  const { t } = useTranslation()
  const { formatDateTime } = useLocalePreference()
  const [type, setType] = useState('')
  const [status, setStatus] = useState<EventStatus | ''>('')
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')
  const [page, setPage] = useState(0)
  const [types, setTypes] = useState<string[]>([])
  const [result, setResult] = useState<PlatformEventPage | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [detail, setDetail] = useState<PlatformEventDetail | null>(null)
  const [toast, setToast] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const [tab, setTab] = useState<'events' | 'toolSync'>('events')

  const load = useCallback(() => adminEventsApi.list({ type, status, from, to, page })
    .then((r) => { setResult(r); setError(null) })
    .catch((e) => setError(e instanceof ApiError ? e.message : t('admin.events.loadError'))), [type, status, from, to, page, t])

  useEffect(() => { void load() }, [load])
  useEffect(() => { adminEventsApi.types().then(setTypes).catch(() => setTypes([])) }, [])

  const open = (id: number) => {
    adminEventsApi.get(id).then(setDetail).catch((e) => setToast(e instanceof ApiError ? e.message : t('admin.events.loadError')))
  }

  const retry = (id: number) => {
    setBusy(true)
    adminEventsApi.retry(id)
      .then((d) => { setToast(t('admin.events.retried')); if (detail) setDetail(d); void load() })
      .catch((e) => setToast(e instanceof ApiError ? e.message : t('admin.events.retryError')))
      .finally(() => setBusy(false))
  }

  const clear = () => { setType(''); setStatus(''); setFrom(''); setTo(''); setPage(0) }
  const count = (s: EventStatus) => result?.items.filter((i) => i.status === s).length ?? 0
  const pageCount = result ? Math.max(1, Math.ceil(result.totalElements / result.size)) : 1
  const statusLabel = (s: EventStatus) => t(`admin.events.status.${s}`)
  const tile = (s: EventStatus, icon: typeof Clock, accent: 'blue' | 'emerald' | 'rose') => (
    <StatusTile icon={icon} accent={accent} label={statusLabel(s)} value={String(count(s))}
      status={t('admin.events.onThisPage')} selected={status === s}
      onClick={() => { setStatus(status === s ? '' : s); setPage(0) }} />
  )

  return (
    <>
      <PageHeader icon={Radio} accent="cyan" area="integrations" title={t('admin.events.title')} subtitle={t('admin.events.subtitle')} />

      <Tabs value={tab} onChange={(_, v) => setTab(v)} aria-label={t('admin.toolSync.tabsLabel')} sx={{ mb: 3, borderBottom: 1, borderColor: 'divider' }}>
        <Tab value="events" label={t('admin.toolSync.tabEvents')} id="events-tab" aria-controls="events-panel" />
        <Tab value="toolSync" label={t('admin.toolSync.tabToolSync')} id="toolsync-tab" aria-controls="toolsync-panel" />
      </Tabs>

      {tab === 'toolSync' && <div role="tabpanel" id="toolsync-panel" aria-labelledby="toolsync-tab"><ToolSyncPanel /></div>}
      {tab === 'events' && <div role="tabpanel" id="events-panel" aria-labelledby="events-tab">
      <Box sx={{ display: 'grid', gap: 2, mb: 3, gridTemplateColumns: { xs: '1fr', sm: 'repeat(3, minmax(0, 1fr))' } }}>
        {tile('PENDING', Clock, 'blue')}
        {tile('DELIVERED', CheckCircle2, 'emerald')}
        {tile('FAILED', AlertTriangle, 'rose')}
      </Box>

      <Paper variant="outlined" sx={{ p: 2, mb: 2, borderRadius: 3, display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
        <TextField select size="small" label={t('admin.events.filter.type')} value={type} sx={{ minWidth: 220 }}
          onChange={(e) => { setType(e.target.value); setPage(0) }}>
          <MenuItem value="">{t('admin.events.filter.allTypes')}</MenuItem>
          {types.map((x) => <MenuItem key={x} value={x}>{x}</MenuItem>)}
        </TextField>
        <TextField select size="small" label={t('admin.events.filter.status')} value={status} sx={{ minWidth: 160 }}
          onChange={(e) => { setStatus(e.target.value as EventStatus | ''); setPage(0) }}>
          <MenuItem value="">{t('admin.events.filter.allStatuses')}</MenuItem>
          {(['PENDING', 'DELIVERED', 'FAILED'] as EventStatus[]).map((s) => <MenuItem key={s} value={s}>{statusLabel(s)}</MenuItem>)}
        </TextField>
        <TextField type="date" size="small" label={t('admin.events.filter.from')} value={from}
          onChange={(e) => { setFrom(e.target.value); setPage(0) }} slotProps={{ inputLabel: { shrink: true } }} />
        <TextField type="date" size="small" label={t('admin.events.filter.to')} value={to}
          onChange={(e) => { setTo(e.target.value); setPage(0) }} slotProps={{ inputLabel: { shrink: true } }} />
        <Button onClick={clear}>{t('admin.events.filter.clear')}</Button>
      </Paper>

      {error && (
        <Alert severity="error" action={<Button onClick={() => void load()}>{t('admin.events.retryLoad')}</Button>}>{error}</Alert>
      )}
      {!error && !result && <Skeleton variant="rounded" height={320} />}

      {result && (
        <Paper variant="outlined" sx={{ borderRadius: 3, overflowX: 'auto' }}>
          <Table size="small">
            <caption style={{ captionSide: 'top', textAlign: 'left', padding: '12px 16px' }}>
              {t('admin.events.caption', { count: result.totalElements })}
            </caption>
            <TableHead>
              <TableRow sx={{ bgcolor: 'action.hover' }}>
                <TableCell>{t('admin.events.col.occurred')}</TableCell>
                <TableCell>{t('admin.events.col.type')}</TableCell>
                <TableCell>{t('admin.events.col.aggregate')}</TableCell>
                <TableCell>{t('admin.events.col.status')}</TableCell>
                <TableCell align="right">{t('admin.events.col.attempts')}</TableCell>
                <TableCell>{t('admin.events.col.nextAttempt')}</TableCell>
                <TableCell align="right">{t('admin.events.col.actions')}</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {result.items.length === 0 && (
                <TableRow><TableCell colSpan={7} sx={{ color: 'text.secondary', py: 4, textAlign: 'center' }}>{t('admin.events.empty')}</TableCell></TableRow>
              )}
              {result.items.map((e) => (
                <TableRow key={e.id} hover>
                  <TableCell>{formatDateTime(e.occurredAt)}</TableCell>
                  <TableCell sx={{ fontFamily: 'monospace' }}>{e.eventType}</TableCell>
                  <TableCell>{`${e.aggregateType} #${e.aggregateId}`}</TableCell>
                  <TableCell><Chip size="small" color={STATUS_COLOR[e.status]} label={statusLabel(e.status)} /></TableCell>
                  <TableCell align="right">{e.attempts}</TableCell>
                  <TableCell>{e.nextAttemptAt ? formatDateTime(e.nextAttemptAt) : '—'}</TableCell>
                  <TableCell align="right" sx={{ whiteSpace: 'nowrap' }}>
                    <Button size="small" onClick={() => open(e.id)} aria-label={t('admin.events.viewEvent', { type: e.eventType, id: e.id })}>
                      {t('admin.events.view')}
                    </Button>
                    {e.status === 'FAILED' && (
                      <Button size="small" color="warning" disabled={busy} startIcon={<RotateCcw size={14} />} onClick={() => retry(e.id)}
                        aria-label={t('admin.events.retryEvent', { type: e.eventType, id: e.id })}>
                        {t('admin.events.retry')}
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      )}

      {result && result.totalElements > result.size && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
          <Pagination count={pageCount} page={page + 1} onChange={(_, p) => setPage(p - 1)} />
        </Box>
      )}

      <Dialog open={Boolean(detail)} onClose={() => setDetail(null)} maxWidth="md" fullWidth aria-labelledby="event-detail-title">
        {detail && (
          <>
            <DialogTitle id="event-detail-title">{detail.eventType}</DialogTitle>
            <DialogContent dividers>
              <Box component="dl" sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '180px 1fr' }, gap: 1, m: 0, mb: 2,
                '& dt': { color: 'text.secondary' }, '& dd': { m: 0, wordBreak: 'break-all' } }}>
                <dt>{t('admin.events.field.eventId')}</dt><dd>{detail.eventId}</dd>
                <dt>{t('admin.events.col.aggregate')}</dt><dd>{`${detail.aggregateType} #${detail.aggregateId}`}</dd>
                <dt>{t('admin.events.col.occurred')}</dt><dd>{formatDateTime(detail.occurredAt)}</dd>
                <dt>{t('admin.events.col.status')}</dt><dd><Chip size="small" color={STATUS_COLOR[detail.status]} label={statusLabel(detail.status)} /></dd>
                <dt>{t('admin.events.col.attempts')}</dt><dd>{detail.attempts}</dd>
                <dt>{t('admin.events.field.delivered')}</dt><dd>{detail.deliveredAt ? formatDateTime(detail.deliveredAt) : '—'}</dd>
                <dt>{t('admin.events.field.lastError')}</dt><dd>{detail.lastError ?? '—'}</dd>
              </Box>
              <Typography component="h3" variant="subtitle2" sx={{ mb: 1 }}>{t('admin.events.field.payload')}</Typography>
              <Box component="pre" sx={{ m: 0, mb: 2, p: 1.5, borderRadius: 2, bgcolor: 'action.hover', fontSize: 13, overflowX: 'auto' }}>
                {prettyPayload(detail.payload)}
              </Box>
              <Typography component="h3" variant="subtitle2" sx={{ mb: 1 }}>{t('admin.events.field.receipts')}</Typography>
              {detail.receipts.length === 0
                ? <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('admin.events.noReceipts')}</Typography>
                : (
                  <Box component="ul" sx={{ m: 0, pl: 2.5 }}>
                    {detail.receipts.map((r) => <li key={r.handler}>{`${r.handler} — ${formatDateTime(r.processedAt)}`}</li>)}
                  </Box>
                )}
            </DialogContent>
            <DialogActions>
              {detail.status === 'FAILED' && (
                <Button color="warning" disabled={busy} startIcon={<RotateCcw size={16} />} onClick={() => retry(detail.id)}>{t('admin.events.retry')}</Button>
              )}
              <Button onClick={() => setDetail(null)}>{t('admin.events.close')}</Button>
            </DialogActions>
          </>
        )}
      </Dialog>

      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)} message={toast ?? ''} />
      </div>}
    </>
  )
}
