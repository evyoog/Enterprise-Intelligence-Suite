import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link as RouterLink } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, Link, MenuItem, Paper,
  Skeleton, Snackbar, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography,
} from '@mui/material'
import { AlertTriangle } from 'lucide-react'
import { ApiError } from '../../api/client'
import {
  adminServiceStatusApi, INCIDENT_PHASES, INCIDENT_SEVERITIES, type AdminIncident, type IncidentPayload, type IncidentPhase,
  type IncidentSeverity, type ProductStatus,
} from '../../api/serviceStatusApi'
import { adminSupportApi, type SupportTicket } from '../../api/supportApi'
import { ErrorState } from '../../components/ui/ErrorState'
import { EmptyState } from '../../components/ui/EmptyState'
import { FormSection } from '../../components/ui/FormSection'
import { useLocalePreference } from '../../theming/LocalePreferenceProvider'

const SEVERITY_COLOR: Record<IncidentSeverity, 'info' | 'warning' | 'error'> = { MINOR: 'info', MAJOR: 'warning', CRITICAL: 'error' }
const PHASE_COLOR: Record<IncidentPhase, 'default' | 'warning' | 'success'> = { LOGGED: 'default', INVESTIGATING: 'warning', RESOLVED: 'success', CLOSED: 'default' }

/** The phases an incident can be moved to from its current phase (the backend re-checks every move). A resolved incident can be reopened. */
const NEXT_PHASES: Record<IncidentPhase, IncidentPhase[]> = {
  LOGGED: ['LOGGED', 'INVESTIGATING', 'RESOLVED'],
  INVESTIGATING: ['INVESTIGATING', 'RESOLVED'],
  RESOLVED: ['RESOLVED', 'INVESTIGATING', 'CLOSED'],
  CLOSED: ['CLOSED'],
}

type PhaseFilter = 'OPEN' | 'ALL' | IncidentPhase

/** A value for an `<input type="datetime-local">`, in the browser's local time. */
function toLocalInput(iso?: string | null) {
  if (!iso) return ''
  const d = new Date(iso)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/**
 * Platform → Service status → Incidents tab — REQ-SUP-003, decision C101 (IC-1 A). A support incident is the same record as the
 * status-page incident: the public fields are what customers see on the status page; the internal fields (severity, phase, notes,
 * assignee, resolution summary, linked tickets, problem) are for administrators only. MANAGE_SERVICE_STATUS. The backend enforces
 * every rule; this page mirrors them for early feedback.
 */
export function AdminIncidentsPage() {
  const { t } = useTranslation()
  const { formatDateTime } = useLocalePreference()
  const [incidents, setIncidents] = useState<AdminIncident[] | null>(null)
  const [products, setProducts] = useState<ProductStatus[]>([])
  const [loadError, setLoadError] = useState<string | null>(null)
  const [filter, setFilter] = useState<PhaseFilter>('OPEN')
  const [editing, setEditing] = useState<AdminIncident | 'new' | null>(null)
  const [toast, setToast] = useState<string | null>(null)

  const load = useCallback(() => {
    adminServiceStatusApi.incidents()
      .then((rows) => { setIncidents(rows); setLoadError(null) })
      .catch((e) => setLoadError(e instanceof ApiError ? e.message : t('incidents.loadError')))
  }, [t])
  useEffect(() => { load() }, [load])
  useEffect(() => {
    adminServiceStatusApi.get().then((p) => setProducts(p.products)).catch(() => setProducts([]))
  }, [])

  const visible = useMemo(() => (incidents ?? []).filter((i) => (filter === 'ALL' ? true : filter === 'OPEN' ? i.phase !== 'CLOSED' : i.phase === filter)), [incidents, filter])

  return (
    <Box sx={{ pb: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 2, flexWrap: 'wrap', mb: 2 }}>
        <Box>
          <Typography variant="h6" component="h2" sx={{ fontWeight: 700 }}>{t('incidents.title')}</Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('incidents.subtitle')}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'center', flexWrap: 'wrap' }}>
          <TextField select size="small" label={t('incidents.filter.label')} value={filter} onChange={(e) => setFilter(e.target.value as PhaseFilter)} sx={{ minWidth: 180 }}>
            <MenuItem value="OPEN">{t('incidents.filter.open')}</MenuItem>
            <MenuItem value="ALL">{t('incidents.filter.all')}</MenuItem>
            {INCIDENT_PHASES.map((p) => <MenuItem key={p} value={p}>{t(`incidents.phase.${p}`)}</MenuItem>)}
          </TextField>
          <Button variant="contained" onClick={() => setEditing('new')}>{t('incidents.new')}</Button>
        </Box>
      </Box>

      {loadError && <ErrorState title={t('incidents.loadErrorTitle')} message={loadError} onRetry={load} />}
      {!loadError && incidents === null && <Skeleton variant="rounded" height={160} />}
      {incidents && visible.length === 0 && (
        <EmptyState icon={AlertTriangle} title={t('incidents.empty.title')}
          description={incidents.length === 0 ? t('incidents.empty.body') : t('incidents.empty.filtered')}
          action={<Button variant="contained" onClick={() => setEditing('new')}>{t('incidents.new')}</Button>} />
      )}
      {visible.length > 0 && (
        <Paper variant="outlined">
          <TableContainer>
            <Table size="small" aria-label={t('incidents.title')}>
              <TableHead>
                <TableRow>
                  <TableCell>{t('incidents.columns.title')}</TableCell>
                  <TableCell>{t('incidents.columns.product')}</TableCell>
                  <TableCell>{t('incidents.columns.severity')}</TableCell>
                  <TableCell>{t('incidents.columns.phase')}</TableCell>
                  <TableCell>{t('incidents.columns.started')}</TableCell>
                  <TableCell>{t('incidents.columns.ended')}</TableCell>
                  <TableCell>{t('incidents.columns.assignee')}</TableCell>
                  <TableCell align="right">{t('incidents.columns.tickets')}</TableCell>
                  <TableCell>{t('incidents.columns.problem')}</TableCell>
                  <TableCell align="right">{t('incidents.columns.actions')}</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {visible.map((i) => (
                  <TableRow key={i.id}>
                    <TableCell sx={{ fontWeight: 600 }}>{i.title}</TableCell>
                    <TableCell>{i.productName}</TableCell>
                    <TableCell><Chip size="small" color={SEVERITY_COLOR[i.severity]} label={t(`incidents.severity.${i.severity}`)} /></TableCell>
                    <TableCell><Chip size="small" variant={i.phase === 'CLOSED' ? 'outlined' : 'filled'} color={PHASE_COLOR[i.phase]} label={t(`incidents.phase.${i.phase}`)} /></TableCell>
                    <TableCell>{formatDateTime(i.startedAt)}</TableCell>
                    <TableCell>{i.endedAt ? formatDateTime(i.endedAt) : t('incidents.ongoing')}</TableCell>
                    <TableCell>{i.assignedToName ?? t('incidents.unassigned')}</TableCell>
                    <TableCell align="right">{i.tickets.length}</TableCell>
                    <TableCell>
                      {i.problemId != null
                        ? <Link component={RouterLink} to="/status?tab=problems" underline="hover">{i.problemTitle ?? t('incidents.problemFallback', { id: i.problemId })}</Link>
                        : <Typography component="span" variant="body2" sx={{ color: 'text.secondary' }}>{t('incidents.noProblem')}</Typography>}
                    </TableCell>
                    <TableCell align="right" sx={{ whiteSpace: 'nowrap' }}>
                      <Button size="small" onClick={() => setEditing(i)}
                        aria-label={t(i.phase === 'CLOSED' ? 'incidents.viewFor' : 'incidents.editFor', { title: i.title })}>
                        {t(i.phase === 'CLOSED' ? 'incidents.view' : 'incidents.edit')}
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      )}

      {editing && (
        <IncidentDialog incident={editing === 'new' ? null : editing} products={products} onClose={() => setEditing(null)} onChanged={load}
          onSaved={(saved, created) => {
            setEditing(null)
            setToast(t(created ? 'incidents.createdToast' : 'incidents.savedToast', { title: saved.title }))
            load()
          }} />
      )}
      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)}>
        {toast ? <Alert severity="success" variant="filled" onClose={() => setToast(null)}>{toast}</Alert> : undefined}
      </Snackbar>
    </Box>
  )
}

function IncidentDialog({ incident, products, onClose, onSaved, onChanged }: {
  incident: AdminIncident | null; products: ProductStatus[]; onClose: () => void
  onSaved: (saved: AdminIncident, created: boolean) => void; onChanged: () => void
}) {
  const { t } = useTranslation()
  const { formatDateTime } = useLocalePreference()
  // Kept in state so linking and unlinking tickets updates the dialog without closing it.
  const [current, setCurrent] = useState<AdminIncident | null>(incident)
  const closed = current?.phase === 'CLOSED'
  const startPhase: IncidentPhase = current?.phase ?? 'LOGGED'

  const [productId, setProductId] = useState<number | ''>(incident?.productId ?? '')
  const [title, setTitle] = useState(incident?.title ?? '')
  const [message, setMessage] = useState(incident?.message ?? '')
  const [startedAt, setStartedAt] = useState(incident ? toLocalInput(incident.startedAt) : toLocalInput(new Date().toISOString()))
  const [endedAt, setEndedAt] = useState(toLocalInput(incident?.endedAt))
  const [severity, setSeverity] = useState<IncidentSeverity>(incident?.severity ?? 'MINOR')
  const [phase, setPhase] = useState<IncidentPhase>(startPhase)
  const [notes, setNotes] = useState(incident?.internalNotes ?? '')
  const [assignee, setAssignee] = useState(incident?.assignedToCustomerId != null ? String(incident.assignedToCustomerId) : '')
  const [summary, setSummary] = useState(incident?.resolutionSummary ?? '')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [errors, setErrors] = useState<Record<string, string | null> | null>(null)
  const shown = (key: string) => errors?.[key] ?? null
  const edited = (key: string) => setErrors((e) => (e ? { ...e, [key]: null } : e))

  const phases = NEXT_PHASES[startPhase]
  const hasEnd = phase === 'RESOLVED' || phase === 'CLOSED'

  const validate = () => {
    const start = startedAt ? new Date(startedAt) : null
    const end = hasEnd && endedAt ? new Date(endedAt) : null
    return {
      product: productId === '' ? t('incidents.errors.product') : null,
      title: !title.trim() ? t('incidents.errors.title') : null,
      message: !message.trim() ? t('incidents.errors.message') : null,
      started: !start || Number.isNaN(start.getTime()) ? t('incidents.errors.started') : null,
      ended: start && end && end.getTime() < start.getTime() ? t('incidents.errors.endBeforeStart') : null,
      summary: phase === 'RESOLVED' && !summary.trim() ? t('incidents.errors.summary') : null,
      assignee: assignee.trim() && !/^[1-9]\d*$/.test(assignee.trim()) ? t('incidents.errors.assignee') : null,
    }
  }

  const submit = async () => {
    const found = validate()
    setErrors(found)
    if (Object.values(found).some(Boolean) || productId === '') return
    // An unchanged time is sent back exactly as the server gave it (the input drops the seconds).
    const iso = (value: string, original?: string | null) => (original && value === toLocalInput(original) ? original : new Date(value).toISOString())
    const payload: IncidentPayload = {
      productId, title: title.trim(), message: message.trim(),
      startedAt: iso(startedAt, current?.startedAt),
      endedAt: hasEnd && endedAt ? iso(endedAt, current?.endedAt) : null,
      severity, phase, internalNotes: notes, resolutionSummary: summary,
      assignedToCustomerId: assignee.trim() ? Number(assignee.trim()) : undefined,
    }
    setBusy(true)
    setError(null)
    try {
      const saved = current ? await adminServiceStatusApi.updateIncident(current.id, payload) : await adminServiceStatusApi.createIncident(payload)
      onSaved(saved, !current)
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('incidents.saveError'))
    } finally {
      setBusy(false)
    }
  }

  const dialogTitle = closed ? t('incidents.dialog.viewTitle') : current ? t('incidents.dialog.editTitle') : t('incidents.dialog.newTitle')

  return (
    <Dialog open onClose={busy ? undefined : onClose} aria-labelledby="incident-dialog-title" maxWidth="md" fullWidth>
      <DialogTitle id="incident-dialog-title">{dialogTitle}</DialogTitle>
      <DialogContent sx={{ display: 'grid', gap: 2, pt: '8px !important' }}>
        {closed && <Alert severity="info">{t('incidents.dialog.closedNote', { date: current?.closedAt ? formatDateTime(current.closedAt) : '' })}</Alert>}
        {error && <Alert severity="error">{error}</Alert>}

        <FormSection id="incident-public" title={t('incidents.dialog.public.title')} description={t('incidents.dialog.public.hint')}>
          <TextField select label={t('incidents.dialog.product')} required disabled={closed} value={productId}
            onChange={(e) => { setProductId(Number(e.target.value)); edited('product') }}
            error={Boolean(shown('product'))} helperText={shown('product') ?? (products.length === 0 ? t('incidents.dialog.noProducts') : undefined)}>
            {products.map((p) => <MenuItem key={p.productId} value={p.productId}>{p.productName}</MenuItem>)}
          </TextField>
          <TextField label={t('incidents.dialog.incidentTitle')} required disabled={closed} value={title}
            onChange={(e) => { setTitle(e.target.value); edited('title') }} error={Boolean(shown('title'))} helperText={shown('title')}
            slotProps={{ htmlInput: { maxLength: 200 } }} />
          <TextField label={t('incidents.dialog.message')} required multiline minRows={2} disabled={closed} value={message}
            onChange={(e) => { setMessage(e.target.value); edited('message') }} error={Boolean(shown('message'))} helperText={shown('message')}
            slotProps={{ htmlInput: { maxLength: 4000 } }} />
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2, alignItems: 'start' }}>
            <TextField type="datetime-local" label={t('incidents.dialog.started')} required disabled={closed} value={startedAt}
              onChange={(e) => { setStartedAt(e.target.value); edited('started'); edited('ended') }} error={Boolean(shown('started'))} helperText={shown('started')}
              slotProps={{ inputLabel: { shrink: true } }} />
            {hasEnd ? (
              <TextField type="datetime-local" label={t('incidents.dialog.ended')} disabled={closed} value={endedAt}
                onChange={(e) => { setEndedAt(e.target.value); edited('ended') }} error={Boolean(shown('ended'))}
                helperText={shown('ended') ?? t('incidents.dialog.endedHint')} slotProps={{ inputLabel: { shrink: true } }} />
            ) : (
              <Typography variant="body2" sx={{ color: 'text.secondary', alignSelf: 'center' }}>{t('incidents.dialog.noEnd')}</Typography>
            )}
          </Box>
        </FormSection>

        <FormSection id="incident-internal" title={t('incidents.dialog.internal.title')} description={t('incidents.dialog.internal.hint')}>
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2, alignItems: 'start' }}>
            <TextField select label={t('incidents.dialog.severity')} disabled={closed} value={severity} onChange={(e) => setSeverity(e.target.value as IncidentSeverity)}>
              {INCIDENT_SEVERITIES.map((s) => <MenuItem key={s} value={s}>{t(`incidents.severity.${s}`)}</MenuItem>)}
            </TextField>
            <TextField select label={t('incidents.dialog.phase')} disabled={closed} value={phase}
              onChange={(e) => { setPhase(e.target.value as IncidentPhase); edited('summary'); edited('ended') }}
              helperText={closed ? undefined : t('incidents.dialog.phaseHint')}>
              {phases.map((p) => (
                <MenuItem key={p} value={p}>
                  {t(`incidents.phase.${p}`)}{startPhase === 'RESOLVED' && p === 'INVESTIGATING' ? ` ${t('incidents.dialog.reopen')}` : ''}
                </MenuItem>
              ))}
            </TextField>
          </Box>
          <TextField label={t('incidents.dialog.summary')} required={phase === 'RESOLVED'} multiline minRows={2} disabled={closed} value={summary}
            onChange={(e) => { setSummary(e.target.value); edited('summary') }} error={Boolean(shown('summary'))}
            helperText={shown('summary') ?? t('incidents.dialog.summaryHint')} slotProps={{ htmlInput: { maxLength: 4000 } }} />
          <TextField label={t('incidents.dialog.notes')} multiline minRows={2} disabled={closed} value={notes}
            onChange={(e) => setNotes(e.target.value)} slotProps={{ htmlInput: { maxLength: 4000 } }} />
          <TextField label={t('incidents.dialog.assignee')} disabled={closed} value={assignee} inputMode="numeric"
            onChange={(e) => { setAssignee(e.target.value); edited('assignee') }} error={Boolean(shown('assignee'))}
            helperText={shown('assignee') ?? (current?.assignedToName ? t('incidents.dialog.assigneeCurrent', { name: current.assignedToName }) : t('incidents.dialog.assigneeHint'))} />
          {current?.problemId != null && (
            <Typography variant="body2">
              {t('incidents.dialog.problem')}{' '}
              <Link component={RouterLink} to="/status?tab=problems" underline="hover">{current.problemTitle ?? t('incidents.problemFallback', { id: current.problemId })}</Link>
            </Typography>
          )}
        </FormSection>

        {current && (
          <TicketsSection incident={current} closed={closed} onUpdated={(next) => { setCurrent(next); onChanged() }} />
        )}
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={busy}>{closed ? t('incidents.dialog.close') : t('incidents.dialog.cancel')}</Button>
        {!closed && (
          <Button variant="contained" onClick={submit} disabled={busy} startIcon={busy ? <CircularProgress size={14} color="inherit" /> : undefined}>
            {current ? t('incidents.dialog.save') : t('incidents.dialog.create')}
          </Button>
        )}
      </DialogActions>
    </Dialog>
  )
}

/** Linked support tickets (the customers who reported the same trouble). Pick from the tickets list, or type an id when that list is not available to this administrator. */
function TicketsSection({ incident, closed, onUpdated }: { incident: AdminIncident; closed: boolean; onUpdated: (next: AdminIncident) => void }) {
  const { t } = useTranslation()
  // null while loading; [] with `manual` when the list is not permitted (403).
  const [tickets, setTickets] = useState<SupportTicket[] | null>(null)
  const [manual, setManual] = useState(false)
  const [choice, setChoice] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (closed) return
    adminSupportApi.listAll().then(setTickets).catch(() => { setTickets([]); setManual(true) })
  }, [closed])

  const linkedIds = new Set(incident.tickets.map((x) => x.id))
  const options = (tickets ?? []).filter((x) => !linkedIds.has(x.id))

  const run = async (call: () => Promise<AdminIncident>) => {
    setBusy(true)
    setError(null)
    try {
      onUpdated(await call())
      setChoice('')
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('incidents.saveError'))
    } finally {
      setBusy(false)
    }
  }

  const manualInvalid = manual && choice.trim() !== '' && !/^[1-9]\d*$/.test(choice.trim())

  return (
    <FormSection id="incident-tickets" title={t('incidents.tickets.title')} description={t('incidents.tickets.hint')}>
      {error && <Alert severity="error">{error}</Alert>}
      {incident.tickets.length === 0
        ? <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('incidents.tickets.none')}</Typography>
        : (
          <Box component="ul" aria-label={t('incidents.tickets.title')} sx={{ listStyle: 'none', p: 0, m: 0, display: 'flex', flexDirection: 'column', gap: 1 }}>
            {incident.tickets.map((x) => (
              <Box component="li" key={x.id} sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                <Typography variant="body2" sx={{ fontWeight: 600 }}>#{x.id} {x.subject}</Typography>
                <Chip size="small" variant="outlined" label={x.status} />
                <Chip size="small" variant="outlined" label={x.priority} />
                {!closed && (
                  <Button size="small" color="error" disabled={busy} sx={{ ml: 'auto' }}
                    aria-label={t('incidents.tickets.unlinkFor', { id: x.id })}
                    onClick={() => run(() => adminServiceStatusApi.unlinkTicket(incident.id, x.id))}>
                    {t('incidents.tickets.unlink')}
                  </Button>
                )}
              </Box>
            ))}
          </Box>
        )}
      {!closed && (
        <Box sx={{ display: 'flex', gap: 1, alignItems: 'flex-start' }}>
          {manual ? (
            <TextField size="small" label={t('incidents.tickets.ticketId')} value={choice} inputMode="numeric" sx={{ flex: 1 }}
              onChange={(e) => setChoice(e.target.value)} error={manualInvalid}
              helperText={manualInvalid ? t('incidents.tickets.ticketIdInvalid') : t('incidents.tickets.ticketIdHint')} />
          ) : (
            <TextField select size="small" label={t('incidents.tickets.pick')} value={choice} sx={{ flex: 1 }} onChange={(e) => setChoice(e.target.value)}
              disabled={tickets === null || options.length === 0}
              helperText={tickets !== null && options.length === 0 ? t('incidents.tickets.noneToLink') : undefined}>
              {options.map((x) => <MenuItem key={x.id} value={String(x.id)}>#{x.id} {x.subject}</MenuItem>)}
            </TextField>
          )}
          <Box sx={{ pt: '1px', flexShrink: 0 }}>
            <Button variant="outlined" disabled={busy || !choice.trim() || manualInvalid}
              onClick={() => run(() => adminServiceStatusApi.linkTicket(incident.id, Number(choice.trim())))}>
              {t('incidents.tickets.link')}
            </Button>
          </Box>
        </Box>
      )}
    </FormSection>
  )
}
