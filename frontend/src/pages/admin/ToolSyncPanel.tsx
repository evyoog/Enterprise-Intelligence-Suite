import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, Pagination, Paper, Skeleton, Snackbar,
  Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography,
} from '@mui/material'
import { GitCompare, Pause, Play, RefreshCw, RotateCcw } from 'lucide-react'
import { ApiError } from '../../api/client'
import {
  toolSyncApi, type DeliveryStatus, type ReconcileResult, type SyncConnector, type SyncTenant, type ToolDeliveryPage,
} from '../../api/toolSyncApi'
import { useLocalePreference } from '../../theming/LocalePreferenceProvider'

const DELIVERY_COLOR: Record<DeliveryStatus, 'info' | 'success' | 'error'> = { PENDING: 'info', DELIVERED: 'success', FAILED: 'error' }
const TENANT_COLOR: Record<SyncTenant['status'], 'info' | 'success' | 'error'> = { PENDING: 'info', READY: 'success', FAILED: 'error' }

/**
 * The "Tool sync" tab of "/admin/integrations/events" — REQ-INT-003, `MANAGE_INTEGRATIONS`. For every connected tool: where each
 * organization's tenant stands and how many messages wait or failed; below, every message to the tools with its attempts and error.
 * Retry (a failed message), Replay (send the object again), Pause/Resume a tool, Start/Resync and Reconcile an organization. The
 * backend audits each action.
 */
export function ToolSyncPanel() {
  const { t } = useTranslation()
  const { formatDateTime } = useLocalePreference()
  const [connectors, setConnectors] = useState<SyncConnector[] | null>(null)
  const [deliveries, setDeliveries] = useState<ToolDeliveryPage | null>(null)
  const [status, setStatus] = useState<DeliveryStatus | ''>('')
  const [page, setPage] = useState(0)
  const [error, setError] = useState<string | null>(null)
  const [toast, setToast] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const [reconcile, setReconcile] = useState<ReconcileResult | null>(null)

  const load = useCallback(() => Promise.all([toolSyncApi.overview(), toolSyncApi.deliveries({ status, page })])
    .then(([o, d]) => { setConnectors(o); setDeliveries(d); setError(null) })
    .catch((e) => setError(e instanceof ApiError ? e.message : t('admin.toolSync.loadError'))), [status, page, t])

  useEffect(() => { void load() }, [load])

  const act = (run: () => Promise<{ message: string }>) => {
    setBusy(true)
    run()
      .then((r) => { setToast(r.message); void load() })
      .catch((e) => setToast(e instanceof ApiError ? e.message : t('admin.toolSync.actionError')))
      .finally(() => setBusy(false))
  }

  const doReconcile = (organizationId: number, connectorId: number) => {
    setBusy(true)
    toolSyncApi.reconcile(organizationId, connectorId, true)
      .then((r) => { setReconcile(r); void load() })
      .catch((e) => setToast(e instanceof ApiError ? e.message : t('admin.toolSync.actionError')))
      .finally(() => setBusy(false))
  }

  const pageCount = deliveries ? Math.max(1, Math.ceil(deliveries.totalElements / deliveries.size)) : 1

  return (
    <>
      {error && <Alert severity="error" action={<Button onClick={() => void load()}>{t('admin.toolSync.retryLoad')}</Button>}>{error}</Alert>}
      {!error && !connectors && <Skeleton variant="rounded" height={320} />}

      {connectors && connectors.length === 0 && <Alert severity="info">{t('admin.toolSync.noTools')}</Alert>}

      {connectors?.map((c) => (
        <Paper key={c.id} variant="outlined" sx={{ p: 2, mb: 3, borderRadius: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap', mb: 1 }}>
            <Typography component="h2" variant="h6" sx={{ fontFamily: 'monospace' }}>{c.productCode}</Typography>
            <Chip size="small" color={c.status === 'ACTIVE' ? 'success' : 'warning'} label={t(`admin.toolSync.connector.${c.status}`)} />
            <Typography variant="body2" sx={{ color: 'text.secondary', flex: 1, wordBreak: 'break-all' }}>{c.baseMcpUrl}</Typography>
            {c.status === 'ACTIVE'
              ? <Button size="small" disabled={busy} startIcon={<Pause size={14} />} onClick={() => act(() => toolSyncApi.pause(c.id))}
                  aria-label={t('admin.toolSync.pauseTool', { tool: c.productCode })}>{t('admin.toolSync.pause')}</Button>
              : <Button size="small" disabled={busy} startIcon={<Play size={14} />} onClick={() => act(() => toolSyncApi.resume(c.id))}
                  aria-label={t('admin.toolSync.resumeTool', { tool: c.productCode })}>{t('admin.toolSync.resume')}</Button>}
          </Box>
          <Box sx={{ overflowX: 'auto' }}>
            <Table size="small">
              <TableHead>
                <TableRow sx={{ bgcolor: 'action.hover' }}>
                  <TableCell>{t('admin.toolSync.col.organization')}</TableCell>
                  <TableCell>{t('admin.toolSync.col.tenant')}</TableCell>
                  <TableCell>{t('admin.toolSync.col.schema')}</TableCell>
                  <TableCell>{t('admin.toolSync.col.lastDelivered')}</TableCell>
                  <TableCell align="right">{t('admin.toolSync.col.waiting')}</TableCell>
                  <TableCell align="right">{t('admin.toolSync.col.failed')}</TableCell>
                  <TableCell>{t('admin.toolSync.col.lastError')}</TableCell>
                  <TableCell align="right">{t('admin.toolSync.col.actions')}</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {c.tenants.length === 0 && (
                  <TableRow><TableCell colSpan={8} sx={{ color: 'text.secondary', py: 3, textAlign: 'center' }}>{t('admin.toolSync.noTenants')}</TableCell></TableRow>
                )}
                {c.tenants.map((tn) => (
                  <TableRow key={tn.organizationId} hover>
                    <TableCell>{tn.organizationName ?? `#${tn.organizationId}`}</TableCell>
                    <TableCell><Chip size="small" color={TENANT_COLOR[tn.status]} label={t(`admin.toolSync.tenant.${tn.status}`)} /></TableCell>
                    <TableCell>{tn.schemaVersion ?? '—'}</TableCell>
                    <TableCell>{tn.lastDeliveredAt ? formatDateTime(tn.lastDeliveredAt) : '—'}</TableCell>
                    <TableCell align="right">{tn.pending}</TableCell>
                    <TableCell align="right">{tn.failed}</TableCell>
                    <TableCell sx={{ maxWidth: 260, wordBreak: 'break-word' }}>{tn.lastError ?? '—'}</TableCell>
                    <TableCell align="right" sx={{ whiteSpace: 'nowrap' }}>
                      <Button size="small" disabled={busy} startIcon={<RefreshCw size={14} />} onClick={() => act(() => toolSyncApi.start(tn.organizationId, c.id))}
                        aria-label={t('admin.toolSync.resyncOrg', { org: tn.organizationName ?? tn.organizationId, tool: c.productCode })}>
                        {tn.status === 'READY' ? t('admin.toolSync.resync') : t('admin.toolSync.start')}
                      </Button>
                      {tn.status === 'READY' && (
                        <Button size="small" disabled={busy} startIcon={<GitCompare size={14} />} onClick={() => doReconcile(tn.organizationId, c.id)}
                          aria-label={t('admin.toolSync.reconcileOrg', { org: tn.organizationName ?? tn.organizationId, tool: c.productCode })}>
                          {t('admin.toolSync.reconcile')}
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Box>
        </Paper>
      ))}

      <Paper variant="outlined" sx={{ p: 2, mb: 2, borderRadius: 3, display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap' }}>
        <Typography component="h2" variant="h6" sx={{ flex: 1 }}>{t('admin.toolSync.deliveries')}</Typography>
        <TextField select size="small" label={t('admin.events.filter.status')} value={status} sx={{ minWidth: 160 }}
          onChange={(e) => { setStatus(e.target.value as DeliveryStatus | ''); setPage(0) }}>
          <MenuItem value="">{t('admin.events.filter.allStatuses')}</MenuItem>
          {(['PENDING', 'DELIVERED', 'FAILED'] as DeliveryStatus[]).map((s) => <MenuItem key={s} value={s}>{t(`admin.events.status.${s}`)}</MenuItem>)}
        </TextField>
      </Paper>

      {deliveries && (
        <Paper variant="outlined" sx={{ borderRadius: 3, overflowX: 'auto' }}>
          <Table size="small">
            <caption style={{ captionSide: 'top', textAlign: 'left', padding: '12px 16px' }}>
              {t('admin.toolSync.caption', { count: deliveries.totalElements })}
            </caption>
            <TableHead>
              <TableRow sx={{ bgcolor: 'action.hover' }}>
                <TableCell>{t('admin.toolSync.col.created')}</TableCell>
                <TableCell>{t('admin.toolSync.col.tool')}</TableCell>
                <TableCell>{t('admin.toolSync.col.organization')}</TableCell>
                <TableCell>{t('admin.toolSync.col.object')}</TableCell>
                <TableCell>{t('admin.events.col.status')}</TableCell>
                <TableCell align="right">{t('admin.events.col.attempts')}</TableCell>
                <TableCell>{t('admin.events.col.nextAttempt')}</TableCell>
                <TableCell>{t('admin.toolSync.col.lastError')}</TableCell>
                <TableCell align="right">{t('admin.events.col.actions')}</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {deliveries.items.length === 0 && (
                <TableRow><TableCell colSpan={9} sx={{ color: 'text.secondary', py: 4, textAlign: 'center' }}>{t('admin.toolSync.emptyDeliveries')}</TableCell></TableRow>
              )}
              {deliveries.items.map((d) => (
                <TableRow key={d.id} hover>
                  <TableCell>{formatDateTime(d.createdAt)}</TableCell>
                  <TableCell sx={{ fontFamily: 'monospace' }}>{d.productCode ?? d.connectorId}</TableCell>
                  <TableCell>{`#${d.organizationId}`}</TableCell>
                  <TableCell>{`${d.aggregateType} ${d.aggregateId}`}</TableCell>
                  <TableCell><Chip size="small" color={DELIVERY_COLOR[d.status]} label={t(`admin.events.status.${d.status}`)} /></TableCell>
                  <TableCell align="right">{d.attempts}</TableCell>
                  <TableCell>{d.nextAttemptAt ? formatDateTime(d.nextAttemptAt) : '—'}</TableCell>
                  <TableCell sx={{ maxWidth: 280, wordBreak: 'break-word' }}>{d.lastError ?? '—'}</TableCell>
                  <TableCell align="right" sx={{ whiteSpace: 'nowrap' }}>
                    {d.status === 'FAILED' && (
                      <Button size="small" color="warning" disabled={busy} startIcon={<RotateCcw size={14} />} onClick={() => act(() => toolSyncApi.retry(d.id))}
                        aria-label={t('admin.toolSync.retryDelivery', { id: d.id })}>{t('admin.events.retry')}</Button>
                    )}
                    <Button size="small" disabled={busy} onClick={() => act(() => toolSyncApi.replay(d.id))}
                      aria-label={t('admin.toolSync.replayDelivery', { id: d.id })}>{t('admin.toolSync.replay')}</Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      )}

      {deliveries && deliveries.totalElements > deliveries.size && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
          <Pagination count={pageCount} page={page + 1} onChange={(_, p) => setPage(p - 1)} />
        </Box>
      )}

      <Dialog open={Boolean(reconcile)} onClose={() => setReconcile(null)} maxWidth="sm" fullWidth aria-labelledby="reconcile-title">
        {reconcile && (
          <>
            <DialogTitle id="reconcile-title">{t('admin.toolSync.reconcileTitle', { tool: reconcile.productCode })}</DialogTitle>
            <DialogContent dividers>
              {!reconcile.reachable
                ? <Alert severity="error">{reconcile.problem}</Alert>
                : (
                  <>
                    <Alert severity={reconcile.inSync ? 'success' : 'warning'} sx={{ mb: 2 }}>
                      {reconcile.inSync ? t('admin.toolSync.inSync') : t('admin.toolSync.driftResent', { count: reconcile.resent })}
                    </Alert>
                    <Table size="small">
                      <TableHead>
                        <TableRow>
                          <TableCell>{t('admin.toolSync.col.object')}</TableCell>
                          <TableCell align="right">{t('admin.toolSync.col.platform')}</TableCell>
                          <TableCell align="right">{t('admin.toolSync.col.tool')}</TableCell>
                          <TableCell align="right">{t('admin.toolSync.col.resent')}</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {reconcile.types.map((ty) => (
                          <TableRow key={ty.aggregateType}>
                            <TableCell>{ty.aggregateType}</TableCell>
                            <TableCell align="right">{ty.platformCount}</TableCell>
                            <TableCell align="right">{ty.toolCount ?? '—'}</TableCell>
                            <TableCell align="right">{ty.inSync ? '—' : ty.resent}</TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </>
                )}
            </DialogContent>
            <DialogActions><Button onClick={() => setReconcile(null)}>{t('admin.events.close')}</Button></DialogActions>
          </>
        )}
      </Dialog>

      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)} message={toast ?? ''} />
    </>
  )
}
