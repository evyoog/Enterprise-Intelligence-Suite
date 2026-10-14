import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, CircularProgress, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Grid, MenuItem,
  Paper, Skeleton, Snackbar, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography, useTheme,
} from '@mui/material'
import { AlertTriangle, CheckCircle2, Clock, KeyRound, Server, XCircle } from 'lucide-react'
import { ApiError } from '../../api/client'
import {
  adminServiceInstancesApi, INSTANCE_HEALTHS, INSTANCE_STATUSES,
  type CreatedReporterKey, type InstanceChangePayload, type InstanceHealth, type InstanceStatus, type InstanceSummary,
  type ReporterKey, type ServiceInstance,
} from '../../api/serviceInstancesApi'
import { productsApi, type Product } from '../../api/productsApi'
import { InstanceHealthChip, InstanceStatusChip } from '../../components/status/ServiceStatusChip'
import { ConfirmDialog } from '../../components/ui/ConfirmDialog'
import { EmptyState } from '../../components/ui/EmptyState'
import { ErrorState } from '../../components/ui/ErrorState'
import { SummaryCard } from '../../components/ui/SummaryCard'
import { useLocalePreference } from '../../theming/LocalePreferenceProvider'

/**
 * REQ-SRM-001 (decision C101), shown as a tab of the Platform > Service status page. MANAGE_SERVICE_STATUS:
 * every service instance with its health, a by-hand change (with a required, audited note), and the per-product
 * reporter keys products use to report health. The backend enforces every rule; this page only gives earlier feedback.
 */
export function AdminServiceInstancesPage() {
  const { t } = useTranslation()
  const { formatDateTime } = useLocalePreference()
  const theme = useTheme()
  const [products, setProducts] = useState<Product[]>([])
  const [productId, setProductId] = useState('')
  const [status, setStatus] = useState<InstanceStatus | ''>('')
  const [health, setHealth] = useState<InstanceHealth | ''>('')
  const [instances, setInstances] = useState<ServiceInstance[] | null>(null)
  const [summary, setSummary] = useState<InstanceSummary | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [keys, setKeys] = useState<ReporterKey[] | null>(null)
  const [keysError, setKeysError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [toast, setToast] = useState<string | null>(null)
  const [changing, setChanging] = useState<ServiceInstance | null>(null)
  const [creatingKey, setCreatingKey] = useState(false)
  const [createdKey, setCreatedKey] = useState<CreatedReporterKey | null>(null)
  const [revoking, setRevoking] = useState<ReporterKey | null>(null)
  const [revokeBusy, setRevokeBusy] = useState(false)

  useEffect(() => { productsApi.listAdmin().then(setProducts).catch(() => setProducts([])) }, [])

  const loadInstances = useCallback(() => {
    const filters = {
      productId: productId ? Number(productId) : undefined,
      status: status || undefined,
      health: health || undefined,
    }
    return Promise.all([adminServiceInstancesApi.list(filters), adminServiceInstancesApi.summary(filters.productId)])
      .then(([rows, sum]) => { setInstances(rows); setSummary(sum); setLoadError(null) })
      .catch((e) => setLoadError(e instanceof ApiError ? e.message : t('serviceInstances.loadError')))
  }, [productId, status, health, t])
  useEffect(() => { void loadInstances() }, [loadInstances])

  const loadKeys = useCallback(() => {
    adminServiceInstancesApi.reporterKeys().then((rows) => { setKeys(rows); setKeysError(null) })
      .catch((e) => setKeysError(e instanceof ApiError ? e.message : t('serviceInstances.keys.loadError')))
  }, [t])
  useEffect(() => { loadKeys() }, [loadKeys])

  const revoke = async () => {
    if (!revoking) return
    const target = revoking
    setRevokeBusy(true)
    setActionError(null)
    try {
      await adminServiceInstancesApi.revokeReporterKey(target.id)
      setToast(t('serviceInstances.keys.revokedToast', { product: target.productName }))
      loadKeys()
    } catch (e) {
      setActionError(e instanceof ApiError ? e.message : t('serviceInstances.actionError'))
    } finally {
      setRevokeBusy(false)
      setRevoking(null)
    }
  }

  const when = (iso?: string | null) => (iso ? formatDateTime(iso) : t('serviceInstances.never'))
  const filtered = Boolean(productId || status || health)
  const cards = [
    { icon: Server, label: t('serviceInstances.summary.total'), value: summary?.total, color: theme.palette.primary.main },
    { icon: CheckCircle2, label: t('serviceInstances.summary.healthy'), value: summary?.healthy, color: theme.palette.success.main },
    { icon: AlertTriangle, label: t('serviceInstances.summary.degraded'), value: summary?.degraded, color: theme.palette.warning.main },
    { icon: XCircle, label: t('serviceInstances.summary.down'), value: summary?.down, color: theme.palette.error.main },
    { icon: Clock, label: t('serviceInstances.summary.noRecentReport'), value: summary?.noRecentReport, color: theme.palette.text.secondary },
  ]

  return (
    <Box component="section" aria-labelledby="service-instances-heading">
      <Typography id="service-instances-heading" variant="h6" component="h2" sx={{ fontWeight: 700 }}>{t('serviceInstances.title')}</Typography>
      <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2, maxWidth: 720 }}>{t('serviceInstances.subtitle')}</Typography>

      {loadError && <Box sx={{ mb: 2 }}><ErrorState title={t('serviceInstances.loadError')} message={loadError} onRetry={() => { void loadInstances() }} /></Box>}
      {actionError && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setActionError(null)}>{actionError}</Alert>}

      <Grid container spacing={2} sx={{ mb: 2 }}>
        {cards.map((c) => (
          <Grid key={c.label} size={{ xs: 12, sm: 6, lg: 'grow' }}>
            <SummaryCard icon={c.icon} label={c.label} color={c.color} value={c.value === undefined ? <Skeleton width={32} /> : c.value} />
          </Grid>
        ))}
      </Grid>

      <Paper variant="outlined" sx={{ p: 1.5, mb: 2, borderRadius: 3, display: 'flex', gap: 1.5, flexWrap: 'wrap' }}
        role="group" aria-label={t('serviceInstances.filters.label')}>
        <TextField select size="small" label={t('serviceInstances.filters.product')} value={productId} onChange={(e) => setProductId(e.target.value)}
          sx={{ minWidth: 200 }} slotProps={{ inputLabel: { shrink: true }, select: { displayEmpty: true } }}>
          <MenuItem value="">{t('serviceInstances.filters.allProducts')}</MenuItem>
          {products.map((p) => <MenuItem key={p.id} value={String(p.id)}>{p.name}</MenuItem>)}
        </TextField>
        <TextField select size="small" label={t('serviceInstances.filters.status')} value={status} onChange={(e) => setStatus(e.target.value as InstanceStatus | '')}
          sx={{ minWidth: 180 }} slotProps={{ inputLabel: { shrink: true }, select: { displayEmpty: true } }}>
          <MenuItem value="">{t('serviceInstances.filters.allStatuses')}</MenuItem>
          {INSTANCE_STATUSES.map((s) => <MenuItem key={s} value={s}>{t(`serviceInstances.status.${s}`)}</MenuItem>)}
        </TextField>
        <TextField select size="small" label={t('serviceInstances.filters.health')} value={health} onChange={(e) => setHealth(e.target.value as InstanceHealth | '')}
          sx={{ minWidth: 180 }} slotProps={{ inputLabel: { shrink: true }, select: { displayEmpty: true } }}>
          <MenuItem value="">{t('serviceInstances.filters.allHealth')}</MenuItem>
          {INSTANCE_HEALTHS.map((h) => <MenuItem key={h} value={h}>{t(`serviceInstances.health.${h}`)}</MenuItem>)}
        </TextField>
      </Paper>

      {!loadError && instances === null && <Skeleton variant="rounded" height={160} />}
      {instances?.length === 0 && (
        <EmptyState icon={Server} title={t('serviceInstances.empty.title')} description={t(filtered ? 'serviceInstances.empty.filtered' : 'serviceInstances.empty.body')} />
      )}
      {instances && instances.length > 0 && (
        <Paper variant="outlined">
          <TableContainer>
            <Table size="small" aria-label={t('serviceInstances.title')}>
              <TableHead>
                <TableRow>
                  <TableCell>{t('serviceInstances.columns.product')}</TableCell>
                  <TableCell>{t('serviceInstances.columns.owner')}</TableCell>
                  <TableCell>{t('serviceInstances.columns.status')}</TableCell>
                  <TableCell>{t('serviceInstances.columns.health')}</TableCell>
                  <TableCell>{t('serviceInstances.columns.lastReport')}</TableCell>
                  <TableCell>{t('serviceInstances.columns.changedBy')}</TableCell>
                  <TableCell>{t('serviceInstances.columns.tenant')}</TableCell>
                  <TableCell align="right">{t('serviceInstances.columns.actions')}</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {instances.map((i) => (
                  <TableRow key={i.id}>
                    <TableCell sx={{ fontWeight: 600 }}>{i.productName}</TableCell>
                    <TableCell>
                      {i.ownerName ?? '—'}
                      <Typography variant="caption" component="div" sx={{ color: 'text.secondary' }}>{t(`serviceInstances.ownerType.${i.ownerType}`)}</Typography>
                    </TableCell>
                    <TableCell><InstanceStatusChip status={i.status} /></TableCell>
                    <TableCell>
                      <InstanceHealthChip health={i.health} stale={i.stale} />
                      {i.healthMessage && <Typography variant="caption" component="div" sx={{ color: 'text.secondary' }}>{i.healthMessage}</Typography>}
                    </TableCell>
                    <TableCell sx={{ whiteSpace: 'nowrap' }}>{when(i.lastReportedAt)}</TableCell>
                    <TableCell>
                      {t(`serviceInstances.changedBy.${i.changedBy}`)}
                      {i.changeNote && <Typography variant="caption" component="div" sx={{ color: 'text.secondary' }}>{i.changeNote}</Typography>}
                    </TableCell>
                    <TableCell sx={{ fontFamily: 'monospace' }}>{i.tenantReference ?? '—'}</TableCell>
                    <TableCell align="right" sx={{ whiteSpace: 'nowrap' }}>
                      <Button size="small" onClick={() => setChanging(i)}
                        aria-label={t('serviceInstances.change.actionFor', { product: i.productName, owner: i.ownerName ?? t(`serviceInstances.ownerType.${i.ownerType}`) })}>
                        {t('serviceInstances.change.action')}
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      )}

      <Box component="section" aria-labelledby="reporter-keys-heading" sx={{ mt: 4 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 2, flexWrap: 'wrap', mb: 1.5 }}>
          <Box sx={{ minWidth: 0 }}>
            <Typography id="reporter-keys-heading" variant="h6" component="h2" sx={{ fontWeight: 700 }}>{t('serviceInstances.keys.title')}</Typography>
            <Typography variant="body2" sx={{ color: 'text.secondary', maxWidth: 720 }}>{t('serviceInstances.keys.subtitle')}</Typography>
            <Typography variant="body2" sx={{ color: 'text.secondary', maxWidth: 720, mt: 0.5 }}>
              {t('serviceInstances.keys.howToPrefix')} <code>/product-reports/health</code> {t('serviceInstances.keys.howToMiddle')} <code>X-Reporter-Key</code>.
            </Typography>
          </Box>
          <Button variant="contained" startIcon={<KeyRound size={16} />} onClick={() => setCreatingKey(true)}>{t('serviceInstances.keys.create')}</Button>
        </Box>

        {keysError && <Box sx={{ mb: 2 }}><ErrorState title={t('serviceInstances.keys.loadError')} message={keysError} onRetry={loadKeys} /></Box>}
        {!keysError && keys === null && <Skeleton variant="rounded" height={100} />}
        {keys?.length === 0 && <EmptyState icon={KeyRound} title={t('serviceInstances.keys.empty.title')} description={t('serviceInstances.keys.empty.body')} />}
        {keys && keys.length > 0 && (
          <Paper variant="outlined">
            <TableContainer>
              <Table size="small" aria-label={t('serviceInstances.keys.title')}>
                <TableHead>
                  <TableRow>
                    <TableCell>{t('serviceInstances.keys.columns.product')}</TableCell>
                    <TableCell>{t('serviceInstances.keys.columns.label')}</TableCell>
                    <TableCell>{t('serviceInstances.keys.columns.prefix')}</TableCell>
                    <TableCell>{t('serviceInstances.keys.columns.created')}</TableCell>
                    <TableCell>{t('serviceInstances.keys.columns.lastUsed')}</TableCell>
                    <TableCell>{t('serviceInstances.keys.columns.revoked')}</TableCell>
                    <TableCell align="right">{t('serviceInstances.columns.actions')}</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {keys.map((k) => (
                    <TableRow key={k.id}>
                      <TableCell sx={{ fontWeight: 600 }}>{k.productName}</TableCell>
                      <TableCell>{k.label ?? '—'}</TableCell>
                      <TableCell sx={{ fontFamily: 'monospace' }}>{k.keyPrefix}…</TableCell>
                      <TableCell sx={{ whiteSpace: 'nowrap' }}>{formatDateTime(k.createdAt)}</TableCell>
                      <TableCell sx={{ whiteSpace: 'nowrap' }}>{when(k.lastUsedAt)}</TableCell>
                      <TableCell sx={{ whiteSpace: 'nowrap' }}>
                        {k.revokedAt ? <Chip size="small" variant="outlined" label={formatDateTime(k.revokedAt)} /> : '—'}
                      </TableCell>
                      <TableCell align="right">
                        {!k.revokedAt && (
                          <Button size="small" color="error" onClick={() => setRevoking(k)}
                            aria-label={t('serviceInstances.keys.revokeFor', { product: k.productName, prefix: k.keyPrefix })}>
                            {t('serviceInstances.keys.revoke')}
                          </Button>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        )}
      </Box>

      {changing && (
        <ChangeDialog instance={changing} onClose={() => setChanging(null)}
          onSaved={() => { setChanging(null); setToast(t('serviceInstances.change.saved')); void loadInstances() }} />
      )}
      {creatingKey && (
        <CreateKeyDialog products={products} onClose={() => setCreatingKey(false)}
          onCreated={(created) => { setCreatingKey(false); setCreatedKey(created); loadKeys() }} />
      )}
      {createdKey && <SecretDialog created={createdKey} onClose={() => setCreatedKey(null)} />}
      <ConfirmDialog open={Boolean(revoking)} title={t('serviceInstances.keys.revokeTitle', { product: revoking?.productName })} body={t('serviceInstances.keys.revokeBody')}
        confirmLabel={t('serviceInstances.keys.revoke')} busy={revokeBusy} onConfirm={revoke} onClose={() => setRevoking(null)} />
      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)}>
        {toast ? <Alert severity="success" variant="filled" onClose={() => setToast(null)}>{toast}</Alert> : undefined}
      </Snackbar>
    </Box>
  )
}

function ChangeDialog({ instance, onClose, onSaved }: { instance: ServiceInstance; onClose: () => void; onSaved: () => void }) {
  const { t } = useTranslation()
  const [status, setStatus] = useState<InstanceStatus>(instance.status)
  const [health, setHealth] = useState<InstanceHealth>(instance.health)
  const [note, setNote] = useState('')
  const [submitted, setSubmitted] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const unchanged = status === instance.status && health === instance.health
  const noteMissing = note.trim() === ''

  const submit = async () => {
    setSubmitted(true)
    if (unchanged || noteMissing) return
    const body: InstanceChangePayload = {
      status: status !== instance.status ? status : undefined,
      health: health !== instance.health ? health : undefined,
      note: note.trim(),
    }
    setBusy(true)
    setError(null)
    try {
      await adminServiceInstancesApi.change(instance.id, body)
      onSaved()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('serviceInstances.actionError'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Dialog open onClose={busy ? undefined : onClose} aria-labelledby="instance-change-title" maxWidth="xs" fullWidth>
      <DialogTitle id="instance-change-title">{t('serviceInstances.change.title', { product: instance.productName })}</DialogTitle>
      <DialogContent sx={{ display: 'grid', gap: 2, pt: '8px !important' }}>
        <DialogContentText>{t('serviceInstances.change.intro')}</DialogContentText>
        {error && <Alert severity="error">{error}</Alert>}
        {submitted && unchanged && <Alert severity="warning">{t('serviceInstances.change.nothingChanged')}</Alert>}
        <TextField select label={t('serviceInstances.change.status')} value={status} onChange={(e) => setStatus(e.target.value as InstanceStatus)}>
          {INSTANCE_STATUSES.map((s) => <MenuItem key={s} value={s}>{t(`serviceInstances.status.${s}`)}</MenuItem>)}
        </TextField>
        <TextField select label={t('serviceInstances.change.health')} value={health} onChange={(e) => setHealth(e.target.value as InstanceHealth)}>
          {INSTANCE_HEALTHS.map((h) => <MenuItem key={h} value={h}>{t(`serviceInstances.health.${h}`)}</MenuItem>)}
        </TextField>
        <TextField label={t('serviceInstances.change.note')} value={note} required multiline minRows={2} onChange={(e) => setNote(e.target.value)}
          error={submitted && noteMissing} helperText={submitted && noteMissing ? t('serviceInstances.change.noteRequired') : t('serviceInstances.change.noteHelp')}
          slotProps={{ htmlInput: { maxLength: 500 } }} />
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={busy}>{t('forms.cancel')}</Button>
        <Button variant="contained" onClick={submit} disabled={busy} startIcon={busy ? <CircularProgress size={14} color="inherit" /> : undefined}>
          {t('serviceInstances.change.save')}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

function CreateKeyDialog({ products, onClose, onCreated }: { products: Product[]; onClose: () => void; onCreated: (created: CreatedReporterKey) => void }) {
  const { t } = useTranslation()
  const [productId, setProductId] = useState('')
  const [label, setLabel] = useState('')
  const [submitted, setSubmitted] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const submit = async () => {
    setSubmitted(true)
    if (!productId) return
    setBusy(true)
    setError(null)
    try {
      onCreated(await adminServiceInstancesApi.createReporterKey(Number(productId), label))
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('serviceInstances.actionError'))
      setBusy(false)
    }
  }

  return (
    <Dialog open onClose={busy ? undefined : onClose} aria-labelledby="create-key-title" maxWidth="xs" fullWidth>
      <DialogTitle id="create-key-title">{t('serviceInstances.keys.createTitle')}</DialogTitle>
      <DialogContent sx={{ display: 'grid', gap: 2, pt: '8px !important' }}>
        {error && <Alert severity="error">{error}</Alert>}
        <TextField select label={t('serviceInstances.keys.product')} value={productId} required onChange={(e) => setProductId(e.target.value)}
          error={submitted && !productId} helperText={submitted && !productId ? t('serviceInstances.keys.productRequired') : undefined}>
          {products.map((p) => <MenuItem key={p.id} value={String(p.id)}>{p.name}</MenuItem>)}
        </TextField>
        <TextField label={t('serviceInstances.keys.label')} value={label} onChange={(e) => setLabel(e.target.value)}
          helperText={t('serviceInstances.keys.labelHelp')} slotProps={{ htmlInput: { maxLength: 100, autoComplete: 'off' } }} />
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={busy}>{t('forms.cancel')}</Button>
        <Button variant="contained" onClick={submit} disabled={busy} startIcon={busy ? <CircularProgress size={14} color="inherit" /> : undefined}>
          {t('serviceInstances.keys.createSubmit')}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

/** The secret is shown once; it can only be closed with the button, so a stray click does not lose it. */
function SecretDialog({ created, onClose }: { created: CreatedReporterKey; onClose: () => void }) {
  const { t } = useTranslation()
  const [copied, setCopied] = useState(false)
  const [copyFailed, setCopyFailed] = useState(false)

  const copy = async () => {
    try {
      await navigator.clipboard.writeText(created.secret)
      setCopied(true)
      setCopyFailed(false)
    } catch {
      setCopyFailed(true)
    }
  }

  return (
    <Dialog open aria-labelledby="key-secret-title" maxWidth="sm" fullWidth onClose={(_e, reason) => { if (reason !== 'backdropClick') onClose() }}>
      <DialogTitle id="key-secret-title">{t('serviceInstances.secret.title', { product: created.key.productName })}</DialogTitle>
      <DialogContent sx={{ display: 'grid', gap: 2, pt: '8px !important' }}>
        <Alert severity="warning">{t('serviceInstances.secret.warning')}</Alert>
        <TextField label={t('serviceInstances.secret.label')} value={created.secret} fullWidth
          slotProps={{ htmlInput: { readOnly: true, style: { fontFamily: 'monospace' }, onFocus: (e) => e.currentTarget.select() } }} />
        <Box>
          <Button variant="outlined" onClick={copy}>{t(copied ? 'serviceInstances.secret.copied' : 'serviceInstances.secret.copy')}</Button>
        </Box>
        <Box role="status" aria-live="polite">
          {copied && <Typography variant="body2" sx={{ color: 'success.main' }}>{t('serviceInstances.secret.copiedNote')}</Typography>}
          {copyFailed && <Typography variant="body2" sx={{ color: 'error.main' }}>{t('serviceInstances.secret.copyFailed')}</Typography>}
        </Box>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button variant="contained" onClick={onClose}>{t('serviceInstances.secret.done')}</Button>
      </DialogActions>
    </Dialog>
  )
}
