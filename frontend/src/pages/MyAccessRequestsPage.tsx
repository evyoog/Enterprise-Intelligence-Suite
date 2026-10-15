import { ClipboardCheck } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Chip, Paper, Typography } from '@mui/material'
import { accessRequestApi, type AccessRequestDto, type AccessRequestStatus } from '../api/accessRequestApi'
import { ApiError } from '../api/client'
import { PageHeader } from '../components/layout/PageHeader'
import { EmptyState } from '../components/ui/EmptyState'
import { ErrorState } from '../components/ui/ErrorState'

type ViewState =
  | { kind: 'loading' }
  | { kind: 'loaded'; items: AccessRequestDto[] }
  | { kind: 'error'; message: string }

const STATUS_COLOR: Record<AccessRequestStatus, 'success' | 'error' | 'warning' | 'default'> = {
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'error',
  CANCELLED: 'default',
}

/**
 * "/my/access-requests" — REQ-TEN-010 (C103): a member's own history of
 * requests for personal access to a product their organization subscribes
 * to. A PENDING request can be cancelled here; getting access back after
 * that always needs a new request (no reinstatement).
 */
export function MyAccessRequestsPage() {
  const { t, i18n } = useTranslation()
  const [state, setState] = useState<ViewState>({ kind: 'loading' })
  const [busyId, setBusyId] = useState<number | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)

  const fetchItems = () => {
    accessRequestApi.myRequests()
      .then((items) => setState({ kind: 'loaded', items }))
      .catch((e) => setState({ kind: 'error', message: e instanceof ApiError ? e.message : t('accessRequests.loadError') }))
  }

  useEffect(fetchItems, [t])

  const load = () => {
    setState({ kind: 'loading' })
    fetchItems()
  }

  const cancel = async (request: AccessRequestDto) => {
    setBusyId(request.id)
    setActionError(null)
    try {
      const updated = await accessRequestApi.cancel(request.id)
      setState((prev) => prev.kind === 'loaded' ? { kind: 'loaded', items: prev.items.map((r) => r.id === updated.id ? updated : r) } : prev)
    } catch (e) {
      setActionError(e instanceof ApiError ? e.message : t('accessRequests.cancelError'))
    } finally {
      setBusyId(null)
    }
  }

  return (
    <Box>
      <PageHeader icon={ClipboardCheck} area="workspace" title={t('accessRequests.myRequestsTitle')} subtitle={t('accessRequests.myRequestsSubtitle')} />

      {actionError && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setActionError(null)}>{actionError}</Alert>}

      {state.kind === 'loading' && <Typography sx={{ color: 'text.secondary' }}>{t('accessRequests.loading')}</Typography>}

      {state.kind === 'error' && <ErrorState title={t('accessRequests.loadError')} message={state.message} onRetry={load} />}

      {state.kind === 'loaded' && state.items.length === 0 && (
        <EmptyState title={t('accessRequests.myRequestsNone')} description={t('accessRequests.myRequestsNoneDescription')} />
      )}

      {state.kind === 'loaded' && state.items.length > 0 && (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
          {state.items.map((request) => (
            <Paper key={request.id} variant="outlined" sx={{ p: 2, display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', flexWrap: 'wrap', gap: 1 }}>
              <Box>
                <Typography sx={{ fontWeight: 700 }}>{request.productName}</Typography>
                {request.message && <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.5, whiteSpace: 'pre-wrap' }}>{request.message}</Typography>}
                <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                  {new Date(request.createdAt).toLocaleString(i18n.language)}
                </Typography>
                {request.status === 'REJECTED' && request.decisionNote && (
                  <Typography variant="body2" sx={{ color: 'error.main', mt: 0.5 }}>{t('accessRequests.note', { note: request.decisionNote })}</Typography>
                )}
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Chip size="small" color={STATUS_COLOR[request.status]} label={t(`accessRequests.statuses.${request.status}`)} />
                {request.status === 'PENDING' && (
                  <Button size="small" disabled={busyId === request.id} onClick={() => cancel(request)}>
                    {t('accessRequests.cancel')}
                  </Button>
                )}
              </Box>
            </Paper>
          ))}
        </Box>
      )}
    </Box>
  )
}
