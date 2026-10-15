import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Paper, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, TextField, Typography,
} from '@mui/material'
import { accessRequestApi, type AccessRequestDto, type AccessRequestStatus } from '../../api/accessRequestApi'
import { ApiError } from '../../api/client'
import { EmptyState } from '../ui/EmptyState'
import { ErrorState } from '../ui/ErrorState'
import { StatusBadge, type StatusTone } from '../ui/StatusBadge'

type ViewState =
  | { kind: 'loading' }
  | { kind: 'loaded'; items: AccessRequestDto[] }
  | { kind: 'error'; message: string }

const STATUS_TONE: Record<AccessRequestStatus, StatusTone> = {
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'error',
  CANCELLED: 'neutral',
}

/**
 * The Access requests tab (REQ-TEN-010 / C103): every member's request, in
 * the whole organization, for personal access to a product it already
 * subscribes to. Approve grants access at once (the backend checks the
 * seat limit); reject needs a note. Needs "Manage product access" or being
 * an org admin — the backend, not this tab, decides and returns 403.
 */
export function AccessRequestsTab() {
  const { t, i18n } = useTranslation()
  const [state, setState] = useState<ViewState>({ kind: 'loading' })
  const [busyId, setBusyId] = useState<number | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [rejecting, setRejecting] = useState<AccessRequestDto | null>(null)
  const [rejectNote, setRejectNote] = useState('')
  const [rejectError, setRejectError] = useState<string | null>(null)

  const fetchItems = () => {
    accessRequestApi.listForOrganization()
      .then((items) => setState({ kind: 'loaded', items }))
      .catch((e) => setState({ kind: 'error', message: e instanceof ApiError ? e.message : t('accessRequests.loadError') }))
  }

  useEffect(fetchItems, [t])

  const load = () => {
    setState({ kind: 'loading' })
    fetchItems()
  }

  const replace = (updated: AccessRequestDto) => {
    setState((prev) => prev.kind === 'loaded' ? { kind: 'loaded', items: prev.items.map((r) => r.id === updated.id ? updated : r) } : prev)
  }

  const approve = async (request: AccessRequestDto) => {
    setBusyId(request.id)
    setActionError(null)
    try {
      replace(await accessRequestApi.approve(request.id))
    } catch (e) {
      setActionError(e instanceof ApiError ? e.message : t('accessRequests.actionError'))
    } finally {
      setBusyId(null)
    }
  }

  const openReject = (request: AccessRequestDto) => {
    setRejecting(request)
    setRejectNote('')
    setRejectError(null)
  }

  const confirmReject = async () => {
    if (!rejecting) return
    if (!rejectNote.trim()) {
      setRejectError(t('accessRequests.noteRequired'))
      return
    }
    setBusyId(rejecting.id)
    setRejectError(null)
    try {
      const updated = await accessRequestApi.reject(rejecting.id, rejectNote.trim())
      replace(updated)
      setRejecting(null)
    } catch (e) {
      setRejectError(e instanceof ApiError ? e.message : t('accessRequests.actionError'))
    } finally {
      setBusyId(null)
    }
  }

  if (state.kind === 'loading') {
    return <Typography sx={{ color: 'text.secondary', py: 2 }}>{t('accessRequests.loading')}</Typography>
  }

  if (state.kind === 'error') {
    return <ErrorState title={t('accessRequests.loadError')} message={state.message} onRetry={load} />
  }

  const { items } = state

  if (items.length === 0) {
    return <EmptyState title={t('accessRequests.none')} description={t('accessRequests.noneDescription')} />
  }

  return (
    <Box>
      {actionError && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setActionError(null)}>{actionError}</Alert>}
      <Paper variant="outlined">
        <TableContainer>
          <Table size="small" aria-label={t('access.tabs.requests')}>
            <TableHead>
              <TableRow>
                <TableCell>{t('accessRequests.member')}</TableCell>
                <TableCell>{t('accessRequests.product')}</TableCell>
                <TableCell>{t('accessRequests.statusLabel')}</TableCell>
                <TableCell>{t('accessRequests.message')}</TableCell>
                <TableCell>{t('accessRequests.requested')}</TableCell>
                <TableCell>{t('accessRequests.decision')}</TableCell>
                <TableCell>{t('accessRequests.actions')}</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {items.map((r) => (
                <TableRow key={r.id}>
                  <TableCell>{r.memberName}</TableCell>
                  <TableCell>{r.productName}</TableCell>
                  <TableCell><StatusBadge tone={STATUS_TONE[r.status]} label={t(`accessRequests.statuses.${r.status}`)} /></TableCell>
                  <TableCell>{r.message || '—'}</TableCell>
                  <TableCell>{new Date(r.createdAt).toLocaleString(i18n.language)}</TableCell>
                  <TableCell>
                    {r.decidedByName ? (
                      <Box>
                        <Typography variant="body2">{t('accessRequests.decidedBy', { name: r.decidedByName })}</Typography>
                        {r.decidedAt && <Typography variant="caption" sx={{ color: 'text.secondary' }}>{new Date(r.decidedAt).toLocaleString(i18n.language)}</Typography>}
                        {r.decisionNote && <Typography variant="caption" sx={{ display: 'block', color: 'text.secondary' }}>{t('accessRequests.note', { note: r.decisionNote })}</Typography>}
                      </Box>
                    ) : '—'}
                  </TableCell>
                  <TableCell>
                    {r.status === 'PENDING' && (
                      <Box sx={{ display: 'flex', gap: 1 }}>
                        <Button size="small" variant="contained" disabled={busyId === r.id} onClick={() => approve(r)}>
                          {t('accessRequests.approve')}
                        </Button>
                        <Button size="small" color="error" variant="outlined" disabled={busyId === r.id} onClick={() => openReject(r)}>
                          {t('accessRequests.reject')}
                        </Button>
                      </Box>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>

      <Dialog open={rejecting !== null} onClose={() => !busyId && setRejecting(null)} aria-labelledby="reject-access-request-title" maxWidth="xs" fullWidth>
        <DialogTitle id="reject-access-request-title">{t('accessRequests.rejectTitle')}</DialogTitle>
        <DialogContent>
          {rejectError && <Alert severity="error" sx={{ mb: 2 }}>{rejectError}</Alert>}
          <TextField
            autoFocus
            fullWidth
            required
            multiline
            minRows={2}
            label={t('accessRequests.reason')}
            value={rejectNote}
            onChange={(e) => setRejectNote(e.target.value)}
          />
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setRejecting(null)} disabled={busyId !== null}>{t('forms.cancel')}</Button>
          <Button variant="contained" color="error" onClick={confirmReject} disabled={busyId !== null}>
            {t('accessRequests.reject')}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
