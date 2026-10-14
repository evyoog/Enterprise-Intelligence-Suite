import { useCallback, useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Checkbox, Chip, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, FormGroup,
  MenuItem, Paper, Skeleton, Snackbar, Step, StepLabel, Stepper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  TextField, Typography,
} from '@mui/material'
import { SearchCheck } from 'lucide-react'
import { ApiError } from '../../api/client'
import {
  PROBLEM_STATUSES, problemsApi, type Problem, type ProblemAction, type ProblemDetail, type ProblemStatus,
} from '../../api/problemsApi'
import { adminServiceStatusApi, type AdminIncident } from '../../api/serviceStatusApi'
import { ConfirmDialog } from '../../components/ui/ConfirmDialog'
import { EmptyState } from '../../components/ui/EmptyState'
import { ErrorState } from '../../components/ui/ErrorState'
import { FormSection } from '../../components/ui/FormSection'
import { useLocalePreference } from '../../theming/LocalePreferenceProvider'

const STATUS_COLOR: Record<ProblemStatus, 'default' | 'warning' | 'info' | 'success'> = {
  OPEN: 'warning', ROOT_CAUSE_IDENTIFIED: 'info', RESOLVED: 'success', CLOSED: 'default',
}
const PHASE_COLOR = { LOGGED: 'default', INVESTIGATING: 'warning', RESOLVED: 'success', CLOSED: 'default' } as const

type BlockReason = 'rootCause' | 'resolvedFirst' | 'openActions'

/**
 * Why a move to `target` is not possible right now, or null when it is (mirrors the backend rules, which re-check them): root cause
 * identified and resolved need a root cause; closed only from resolved and only when every corrective action is done.
 */
function blockedReason(current: ProblemStatus, target: ProblemStatus, hasRootCause: boolean, openActions: number): BlockReason | null {
  if ((target === 'ROOT_CAUSE_IDENTIFIED' || target === 'RESOLVED') && !hasRootCause) return 'rootCause'
  if (target === 'CLOSED') {
    if (current !== 'RESOLVED') return 'resolvedFirst'
    if (openActions > 0) return 'openActions'
  }
  return null
}

/**
 * Platform → Service status → Problems tab — REQ-SUP-003, decision C101 (IC-2 A). A problem is a separate record that links several
 * incidents to one root cause, with corrective actions. MANAGE_SERVICE_STATUS. The backend enforces every rule; this page mirrors them.
 */
export function AdminProblemsPage() {
  const { t } = useTranslation()
  const { formatDate } = useLocalePreference()
  const [problems, setProblems] = useState<Problem[] | null>(null)
  const [incidents, setIncidents] = useState<AdminIncident[]>([])
  const [loadError, setLoadError] = useState<string | null>(null)
  const [creating, setCreating] = useState(false)
  const [openId, setOpenId] = useState<number | null>(null)
  const [toast, setToast] = useState<string | null>(null)

  const load = useCallback(() => {
    problemsApi.list().then((rows) => { setProblems(rows); setLoadError(null) })
      .catch((e) => setLoadError(e instanceof ApiError ? e.message : t('problems.loadError')))
    adminServiceStatusApi.incidents().then(setIncidents).catch(() => setIncidents([]))
  }, [t])
  useEffect(() => { load() }, [load])

  // An incident belongs to at most one problem.
  const free = useMemo(() => incidents.filter((i) => i.problemId == null), [incidents])

  return (
    <Box sx={{ pb: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 2, flexWrap: 'wrap', mb: 2 }}>
        <Box>
          <Typography variant="h6" component="h2" sx={{ fontWeight: 700 }}>{t('problems.title')}</Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('problems.subtitle')}</Typography>
        </Box>
        <Button variant="contained" onClick={() => setCreating(true)}>{t('problems.new')}</Button>
      </Box>

      {loadError && <ErrorState title={t('problems.loadErrorTitle')} message={loadError} onRetry={load} />}
      {!loadError && problems === null && <Skeleton variant="rounded" height={160} />}
      {problems?.length === 0 && (
        <EmptyState icon={SearchCheck} title={t('problems.empty.title')} description={t('problems.empty.body')}
          action={<Button variant="contained" onClick={() => setCreating(true)}>{t('problems.new')}</Button>} />
      )}
      {problems && problems.length > 0 && (
        <Paper variant="outlined">
          <TableContainer>
            <Table size="small" aria-label={t('problems.title')}>
              <TableHead>
                <TableRow>
                  <TableCell>{t('problems.columns.title')}</TableCell>
                  <TableCell>{t('problems.columns.status')}</TableCell>
                  <TableCell align="right">{t('problems.columns.incidents')}</TableCell>
                  <TableCell align="right">{t('problems.columns.actions')}</TableCell>
                  <TableCell>{t('problems.columns.created')}</TableCell>
                  <TableCell align="right">{t('problems.columns.open')}</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {problems.map((p) => (
                  <TableRow key={p.id}>
                    <TableCell sx={{ fontWeight: 600 }}>{p.title}</TableCell>
                    <TableCell><Chip size="small" variant={p.status === 'CLOSED' ? 'outlined' : 'filled'} color={STATUS_COLOR[p.status]} label={t(`problems.status.${p.status}`)} /></TableCell>
                    <TableCell align="right">{p.incidentCount}</TableCell>
                    <TableCell align="right">{t('problems.actionsCount', { open: p.openActionCount, total: p.actionCount })}</TableCell>
                    <TableCell>{formatDate(p.createdAt)}</TableCell>
                    <TableCell align="right">
                      <Button size="small" onClick={() => setOpenId(p.id)}
                        aria-label={t(p.status === 'CLOSED' ? 'problems.viewFor' : 'problems.openFor', { title: p.title })}>
                        {t(p.status === 'CLOSED' ? 'problems.view' : 'problems.manage')}
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      )}

      {creating && (
        <NewProblemDialog free={free} onClose={() => setCreating(false)}
          onCreated={(d) => { setCreating(false); setToast(t('problems.createdToast', { title: d.problem.title })); load() }} />
      )}
      {openId !== null && <ProblemDialog id={openId} free={free} onClose={() => setOpenId(null)} onChanged={load} />}
      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)}>
        {toast ? <Alert severity="success" variant="filled" onClose={() => setToast(null)}>{toast}</Alert> : undefined}
      </Snackbar>
    </Box>
  )
}

function NewProblemDialog({ free, onClose, onCreated }: { free: AdminIncident[]; onClose: () => void; onCreated: (d: ProblemDetail) => void }) {
  const { t } = useTranslation()
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [picked, setPicked] = useState<number[]>([])
  const [titleError, setTitleError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const submit = async () => {
    if (!title.trim()) { setTitleError(t('problems.errors.title')); return }
    setBusy(true)
    setError(null)
    try {
      onCreated(await problemsApi.create({ title: title.trim(), description: description.trim() || undefined, incidentIds: picked.length ? picked : undefined }))
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('problems.saveError'))
      setBusy(false)
    }
  }

  return (
    <Dialog open onClose={busy ? undefined : onClose} aria-labelledby="new-problem-title" maxWidth="sm" fullWidth>
      <DialogTitle id="new-problem-title">{t('problems.newTitle')}</DialogTitle>
      <DialogContent sx={{ display: 'grid', gap: 2, pt: '8px !important' }}>
        {error && <Alert severity="error">{error}</Alert>}
        <TextField label={t('problems.form.title')} required value={title} onChange={(e) => { setTitle(e.target.value); setTitleError(null) }}
          error={Boolean(titleError)} helperText={titleError} slotProps={{ htmlInput: { maxLength: 200 } }} />
        <TextField label={t('problems.form.description')} multiline minRows={2} value={description} onChange={(e) => setDescription(e.target.value)}
          slotProps={{ htmlInput: { maxLength: 4000 } }} />
        <Box component="fieldset" sx={{ border: 1, borderColor: 'divider', borderRadius: 1, m: 0, p: 1.5, minWidth: 0 }}>
          <Typography component="legend" variant="body2" sx={{ px: 0.5, fontWeight: 600 }}>{t('problems.form.incidents')}</Typography>
          {free.length === 0
            ? <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('problems.form.noFreeIncidents')}</Typography>
            : (
              <FormGroup sx={{ maxHeight: 220, overflowY: 'auto', flexWrap: 'nowrap' }}>
                {free.map((i) => (
                  <FormControlLabel key={i.id} label={`${i.title} · ${i.productName}`}
                    control={<Checkbox size="small" checked={picked.includes(i.id)}
                      onChange={(e) => setPicked((p) => (e.target.checked ? [...p, i.id] : p.filter((x) => x !== i.id)))} />} />
                ))}
              </FormGroup>
            )}
          <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t('problems.form.incidentsHint')}</Typography>
        </Box>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={busy}>{t('problems.cancel')}</Button>
        <Button variant="contained" onClick={submit} disabled={busy} startIcon={busy ? <CircularProgress size={14} color="inherit" /> : undefined}>
          {t('problems.create')}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

function ProblemDialog({ id, free, onClose, onChanged }: { id: number; free: AdminIncident[]; onClose: () => void; onChanged: () => void }) {
  const { t } = useTranslation()
  const { formatDate, formatDateTime } = useLocalePreference()
  const [detail, setDetail] = useState<ProblemDetail | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const [notice, setNotice] = useState<string | null>(null)

  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [rootCause, setRootCause] = useState('')
  const [target, setTarget] = useState<ProblemStatus | ''>('')
  const [incidentChoice, setIncidentChoice] = useState('')
  const [newAction, setNewAction] = useState({ description: '', owner: '', dueOn: '' })
  const [editingAction, setEditingAction] = useState<{ id: number; description: string; owner: string; dueOn: string } | null>(null)
  const [deletingAction, setDeletingAction] = useState<ProblemAction | null>(null)

  const fill = useCallback((d: ProblemDetail) => {
    setDetail(d)
    setTitle(d.problem.title)
    setDescription(d.problem.description ?? '')
    setRootCause(d.problem.rootCause ?? '')
    setTarget('')
  }, [])

  useEffect(() => {
    problemsApi.get(id).then(fill).catch((e) => setLoadError(e instanceof ApiError ? e.message : t('problems.loadError')))
  }, [id, fill, t])

  /** Runs one call, shows the backend's own message if it refuses, and refreshes the list behind the dialog. */
  const run = async (call: () => Promise<ProblemDetail>, message?: string, resetForm = true) => {
    setBusy(true)
    setError(null)
    setNotice(null)
    try {
      const next = await call()
      if (resetForm) fill(next)
      else setDetail(next)
      if (message) setNotice(message)
      onChanged()
      return true
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('problems.saveError'))
      return false
    } finally {
      setBusy(false)
    }
  }

  const problem = detail?.problem
  const closed = problem?.status === 'CLOSED'
  const hasRootCause = rootCause.trim() !== ''
  const openActions = detail ? detail.actions.filter((a) => !a.done).length : 0

  // Every status except the current one; each is either offered or blocked with a reason.
  const moves = problem ? PROBLEM_STATUSES.filter((s) => s !== problem.status).map((s) => ({ status: s, reason: blockedReason(problem.status, s, hasRootCause, openActions) })) : []
  const blocked = moves.filter((m) => m.reason)

  const reasonText = (m: { status: ProblemStatus; reason: BlockReason | null }) =>
    t(`problems.blocked.${m.reason}`, { status: t(`problems.status.${m.status}`), count: openActions })

  const saveDetails = () => run(() => problemsApi.update(id, { title: title.trim(), description, rootCause }), t('problems.savedToast'))
  const changeStatus = () => target && run(() => problemsApi.update(id, { status: target, rootCause }), t('problems.statusChangedToast', { status: t(`problems.status.${target}`) }))
  const addAction = async () => {
    if (!newAction.description.trim()) { setError(t('problems.errors.action')); return }
    const ok = await run(() => problemsApi.addAction(id, {
      description: newAction.description.trim(), owner: newAction.owner.trim() || undefined, dueOn: newAction.dueOn || undefined,
    }), t('problems.actions.addedToast'), false)
    if (ok) setNewAction({ description: '', owner: '', dueOn: '' })
  }
  const saveAction = async () => {
    if (!editingAction) return
    if (!editingAction.description.trim()) { setError(t('problems.errors.action')); return }
    const ok = await run(() => problemsApi.updateAction(id, editingAction.id, {
      description: editingAction.description.trim(), owner: editingAction.owner, dueOn: editingAction.dueOn || undefined,
    }), t('problems.actions.savedToast'), false)
    if (ok) setEditingAction(null)
  }

  return (
    <Dialog open onClose={busy ? undefined : onClose} aria-labelledby="problem-dialog-title" maxWidth="md" fullWidth>
      <DialogTitle id="problem-dialog-title">{problem ? t('problems.detail.title', { title: problem.title }) : t('problems.detail.loading')}</DialogTitle>
      <DialogContent sx={{ display: 'grid', gap: 2, pt: '8px !important' }}>
        {loadError && <Alert severity="error">{loadError}</Alert>}
        {!detail && !loadError && <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}><CircularProgress size={28} aria-label={t('problems.detail.loading')} /></Box>}
        {error && <Alert severity="error" onClose={() => setError(null)}>{error}</Alert>}
        {notice && <Alert severity="success" onClose={() => setNotice(null)}>{notice}</Alert>}

        {detail && problem && (
          <>
            {closed && <Alert severity="info">{t('problems.detail.closedNote', { date: problem.closedAt ? formatDateTime(problem.closedAt) : '' })}</Alert>}

            <FormSection id="problem-details" title={t('problems.detail.detailsTitle')} description={t('problems.detail.detailsHint')}>
              <TextField label={t('problems.form.title')} required disabled={closed} value={title} onChange={(e) => setTitle(e.target.value)} slotProps={{ htmlInput: { maxLength: 200 } }} />
              <TextField label={t('problems.form.description')} multiline minRows={2} disabled={closed} value={description} onChange={(e) => setDescription(e.target.value)}
                slotProps={{ htmlInput: { maxLength: 4000 } }} />
              <TextField label={t('problems.form.rootCause')} multiline minRows={2} disabled={closed} value={rootCause} onChange={(e) => setRootCause(e.target.value)}
                helperText={t('problems.form.rootCauseHint')} slotProps={{ htmlInput: { maxLength: 4000 } }} />
              {!closed && (
                <Box><Button variant="contained" disabled={busy || !title.trim()} onClick={saveDetails}>{t('problems.detail.saveDetails')}</Button></Box>
              )}
            </FormSection>

            <FormSection id="problem-status" title={t('problems.detail.statusTitle')} description={t('problems.detail.statusHint')}>
              <Stepper activeStep={PROBLEM_STATUSES.indexOf(problem.status)} alternativeLabel nonLinear aria-label={t('problems.detail.statusTitle')}>
                {PROBLEM_STATUSES.map((s) => <Step key={s} completed={PROBLEM_STATUSES.indexOf(s) <= PROBLEM_STATUSES.indexOf(problem.status)}><StepLabel>{t(`problems.status.${s}`)}</StepLabel></Step>)}
              </Stepper>
              {!closed && (
                <>
                  <Box sx={{ display: 'flex', gap: 1, alignItems: 'flex-start', flexWrap: 'wrap' }}>
                    <TextField select size="small" label={t('problems.detail.moveTo')} value={target} sx={{ minWidth: 240 }} onChange={(e) => setTarget(e.target.value as ProblemStatus)}>
                      {moves.map((m) => <MenuItem key={m.status} value={m.status} disabled={Boolean(m.reason)}>{t(`problems.status.${m.status}`)}</MenuItem>)}
                    </TextField>
                    <Button variant="outlined" disabled={busy || !target} onClick={changeStatus}>{t('problems.detail.changeStatus')}</Button>
                  </Box>
                  {blocked.length > 0 && (
                    <Alert severity="info" role="note" aria-label={t('problems.detail.blockedTitle')}>
                      <Box component="ul" sx={{ m: 0, pl: 2 }}>{blocked.map((m) => <li key={m.status}>{reasonText(m)}</li>)}</Box>
                    </Alert>
                  )}
                </>
              )}
            </FormSection>

            <FormSection id="problem-incidents" title={t('problems.detail.incidentsTitle')} description={t('problems.detail.incidentsHint')}>
              {detail.incidents.length === 0
                ? <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('problems.detail.noIncidents')}</Typography>
                : (
                  <Box component="ul" aria-label={t('problems.detail.incidentsTitle')} sx={{ listStyle: 'none', p: 0, m: 0, display: 'flex', flexDirection: 'column', gap: 1 }}>
                    {detail.incidents.map((i) => (
                      <Box component="li" key={i.id} sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                        <Typography variant="body2" sx={{ fontWeight: 600 }}>{i.title}</Typography>
                        <Typography variant="body2" sx={{ color: 'text.secondary' }}>{i.productName} · {formatDate(i.startedAt)}</Typography>
                        <Chip size="small" color={PHASE_COLOR[i.phase]} variant="outlined" label={t(`incidents.phase.${i.phase}`)} />
                        <Chip size="small" variant="outlined" label={t(`incidents.severity.${i.severity}`)} />
                        {!closed && (
                          <Button size="small" color="error" disabled={busy} sx={{ ml: 'auto' }} aria-label={t('problems.detail.removeIncidentFor', { title: i.title })}
                            onClick={() => run(() => problemsApi.unlinkIncident(id, i.id), t('problems.detail.incidentRemovedToast'), false)}>
                            {t('problems.detail.removeIncident')}
                          </Button>
                        )}
                      </Box>
                    ))}
                  </Box>
                )}
              {!closed && (
                <Box sx={{ display: 'flex', gap: 1, alignItems: 'flex-start' }}>
                  <TextField select size="small" label={t('problems.detail.addIncident')} value={incidentChoice} sx={{ flex: 1 }} onChange={(e) => setIncidentChoice(e.target.value)}
                    disabled={free.length === 0} helperText={free.length === 0 ? t('problems.detail.noFreeIncidents') : undefined}>
                    {free.map((i) => <MenuItem key={i.id} value={String(i.id)}>{i.title} · {i.productName}</MenuItem>)}
                  </TextField>
                  <Button variant="outlined" disabled={busy || !incidentChoice}
                    onClick={async () => { if (await run(() => problemsApi.linkIncident(id, Number(incidentChoice)), t('problems.detail.incidentLinkedToast'), false)) setIncidentChoice('') }}>
                    {t('problems.detail.linkIncident')}
                  </Button>
                </Box>
              )}
            </FormSection>

            <FormSection id="problem-actions" title={t('problems.actions.title')}
              description={t('problems.actions.hint', { open: openActions, total: detail.actions.length })}>
              {detail.actions.length === 0 && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('problems.actions.none')}</Typography>}
              <Box component="ul" aria-label={t('problems.actions.title')} sx={{ listStyle: 'none', p: 0, m: 0, display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                {detail.actions.map((a) => (
                  <Box component="li" key={a.id}>
                    {editingAction?.id === a.id ? (
                      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '2fr 1fr 1fr auto' }, gap: 1, alignItems: 'start' }}>
                        <TextField size="small" label={t('problems.actions.description')} value={editingAction.description}
                          onChange={(e) => setEditingAction({ ...editingAction, description: e.target.value })} slotProps={{ htmlInput: { maxLength: 500 } }} />
                        <TextField size="small" label={t('problems.actions.owner')} value={editingAction.owner}
                          onChange={(e) => setEditingAction({ ...editingAction, owner: e.target.value })} slotProps={{ htmlInput: { maxLength: 100 } }} />
                        <TextField size="small" type="date" label={t('problems.actions.dueOn')} value={editingAction.dueOn}
                          onChange={(e) => setEditingAction({ ...editingAction, dueOn: e.target.value })} slotProps={{ inputLabel: { shrink: true } }} />
                        <Box sx={{ display: 'flex', gap: 0.5 }}>
                          <Button size="small" variant="contained" disabled={busy} onClick={saveAction}>{t('problems.actions.save')}</Button>
                          <Button size="small" disabled={busy} onClick={() => setEditingAction(null)}>{t('problems.cancel')}</Button>
                        </Box>
                      </Box>
                    ) : (
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                        <Checkbox size="small" checked={a.done} disabled={busy || closed} sx={{ p: 0.5 }}
                          slotProps={{ input: { 'aria-label': t('problems.actions.doneFor', { description: a.description }) } }}
                          onChange={(e) => run(() => problemsApi.updateAction(id, a.id, { done: e.target.checked }), undefined, false)} />
                        <Box sx={{ flex: 1, minWidth: 180 }}>
                          <Typography variant="body2" sx={{ fontWeight: 600, textDecoration: a.done ? 'line-through' : 'none' }}>{a.description}</Typography>
                          <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                            {[a.owner ? t('problems.actions.ownerIs', { owner: a.owner }) : t('problems.actions.noOwner'),
                              a.dueOn ? t('problems.actions.dueIs', { date: formatDate(`${a.dueOn}T00:00:00`) }) : t('problems.actions.noDue'),
                              a.done && a.doneAt ? t('problems.actions.doneOn', { date: formatDate(a.doneAt) }) : null].filter(Boolean).join(' · ')}
                          </Typography>
                        </Box>
                        {!closed && (
                          <>
                            <Button size="small" disabled={busy} aria-label={t('problems.actions.editFor', { description: a.description })}
                              onClick={() => setEditingAction({ id: a.id, description: a.description, owner: a.owner ?? '', dueOn: a.dueOn ?? '' })}>
                              {t('problems.actions.edit')}
                            </Button>
                            <Button size="small" color="error" disabled={busy} aria-label={t('problems.actions.deleteFor', { description: a.description })}
                              onClick={() => setDeletingAction(a)}>
                              {t('problems.actions.delete')}
                            </Button>
                          </>
                        )}
                      </Box>
                    )}
                  </Box>
                ))}
              </Box>
              {!closed && (
                <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '2fr 1fr 1fr auto' }, gap: 1, alignItems: 'start' }}>
                  <TextField size="small" label={t('problems.actions.newDescription')} value={newAction.description}
                    onChange={(e) => setNewAction({ ...newAction, description: e.target.value })} slotProps={{ htmlInput: { maxLength: 500 } }} />
                  <TextField size="small" label={t('problems.actions.newOwner')} value={newAction.owner}
                    onChange={(e) => setNewAction({ ...newAction, owner: e.target.value })} slotProps={{ htmlInput: { maxLength: 100 } }} />
                  <TextField size="small" type="date" label={t('problems.actions.newDueOn')} value={newAction.dueOn}
                    onChange={(e) => setNewAction({ ...newAction, dueOn: e.target.value })} slotProps={{ inputLabel: { shrink: true } }} />
                  <Button variant="outlined" disabled={busy} onClick={addAction}>{t('problems.actions.add')}</Button>
                </Box>
              )}
            </FormSection>
          </>
        )}
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={busy}>{t('problems.detail.close')}</Button>
      </DialogActions>
      <ConfirmDialog open={Boolean(deletingAction)} title={t('problems.actions.deleteTitle')} body={deletingAction?.description}
        confirmLabel={t('problems.actions.delete')} busy={busy}
        onClose={() => setDeletingAction(null)}
        onConfirm={async () => {
          const target = deletingAction
          if (!target) return
          await run(() => problemsApi.deleteAction(id, target.id), t('problems.actions.deletedToast'), false)
          setDeletingAction(null)
        }} />
    </Dialog>
  )
}
