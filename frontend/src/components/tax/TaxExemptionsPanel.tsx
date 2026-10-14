import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, Paper, Skeleton, Table, TableBody, TableCell, TableHead, TableRow,
  TextField, Typography,
} from '@mui/material'
import { Plus } from 'lucide-react'
import { taxApi, type ExemptionStatus, type TaxExemption, type TaxReference } from '../../api/taxApi'
import { EmptyState } from '../ui/EmptyState'
import { StatusBadge, type StatusTone } from '../ui/StatusBadge'
import { errorText, formatDate } from './taxFormat'

const TONE: Record<ExemptionStatus, StatusTone> = { PENDING_APPROVAL: 'warning', APPROVED: 'success', REVOKED: 'neutral' }

/** REQ-BIL-002.18: exemption certificates for a customer or an organization. */
export function TaxExemptionsPanel({ reference, onToast }: { reference: TaxReference; onToast: (message: string) => void }) {
  const { t, i18n } = useTranslation()
  const [rows, setRows] = useState<TaxExemption[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [creating, setCreating] = useState(false)
  const [busyId, setBusyId] = useState<number | null>(null)

  const load = useCallback(() => {
    return taxApi.exemptions().then((r) => { setRows(r); setError(null) }).catch((e) => setError(errorText(e, t('admin.tax.loadError'))))
  }, [t])
  useEffect(() => { void load() }, [load])

  const act = async (id: number, action: 'approve' | 'revoke') => {
    setBusyId(id)
    setActionError(null)
    try {
      await (action === 'approve' ? taxApi.approveExemption(id) : taxApi.revokeExemption(id))
      onToast(t('admin.tax.saved'))
      void load()
    } catch (e) {
      setActionError(errorText(e, t('admin.tax.rules.actionError')))
    } finally {
      setBusyId(null)
    }
  }

  return (
    <Paper variant="outlined" sx={{ p: { xs: 2, sm: 3 }, borderRadius: 3 }}>
      <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-start', flexWrap: 'wrap', mb: 2 }}>
        <Box sx={{ flex: 1, minWidth: 260 }}>
          <Typography component="h2" variant="subtitle1" sx={{ fontWeight: 700 }}>{t('admin.tax.exemptions.title')}</Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('admin.tax.exemptions.description')}</Typography>
        </Box>
        <Button variant="contained" startIcon={<Plus size={16} />} onClick={() => setCreating(true)}>{t('admin.tax.exemptions.new')}</Button>
      </Box>
      {actionError && <Alert severity="error" role="alert" sx={{ mb: 2 }}>{actionError}</Alert>}
      {error && <Alert severity="error" action={<Button onClick={() => void load()}>{t('admin.tax.retry')}</Button>}>{error}</Alert>}
      {!error && rows === null && <Skeleton variant="rounded" height={120} />}
      {rows && rows.length === 0 && <EmptyState title={t('admin.tax.exemptions.title')} description={t('admin.tax.exemptions.empty')} />}
      {rows && rows.length > 0 && (
        <Table size="small" aria-label={t('admin.tax.exemptions.title')}>
          <TableHead>
            <TableRow>{(['owner', 'country', 'reason', 'dates', 'status', 'actions'] as const).map((c) => <TableCell key={c}>{t(`admin.tax.exemptions.columns.${c}`)}</TableCell>)}</TableRow>
          </TableHead>
          <TableBody>
            {rows.map((e) => (
              <TableRow key={e.id}>
                <TableCell>{e.ownerLabel}<Typography variant="caption" sx={{ display: 'block', color: 'text.secondary' }}>{t(`admin.tax.exemptions.${e.ownerType === 'CUSTOMER' ? 'customer' : 'organization'}`)} #{e.ownerId}</Typography></TableCell>
                <TableCell>{e.countryName}</TableCell>
                <TableCell>{e.reason}{e.certificateNumber ? <Typography variant="caption" sx={{ display: 'block', color: 'text.secondary' }}>{e.certificateNumber}</Typography> : null}</TableCell>
                <TableCell>{formatDate(e.effectiveFrom, i18n.language)} – {e.effectiveTo ? formatDate(e.effectiveTo, i18n.language) : '…'}</TableCell>
                <TableCell><StatusBadge tone={TONE[e.status]} label={t(`admin.tax.exemptions.status.${e.status}`)} /></TableCell>
                <TableCell>
                  {e.status === 'PENDING_APPROVAL' && <Button size="small" variant="contained" onClick={() => void act(e.id, 'approve')} disabled={busyId === e.id}>{t('admin.tax.exemptions.approve')}</Button>}
                  {e.status !== 'REVOKED' && <Button size="small" color="error" onClick={() => void act(e.id, 'revoke')} disabled={busyId === e.id}>{t('admin.tax.exemptions.revoke')}</Button>}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
      {creating && <ExemptionDialog reference={reference} onClose={() => setCreating(false)} onSaved={() => { setCreating(false); onToast(t('admin.tax.saved')); void load() }} />}
    </Paper>
  )
}

function ExemptionDialog({ reference, onClose, onSaved }: { reference: TaxReference; onClose: () => void; onSaved: () => void }) {
  const { t } = useTranslation()
  const [ownerType, setOwnerType] = useState<'CUSTOMER' | 'ORGANIZATION'>('ORGANIZATION')
  const [ownerId, setOwnerId] = useState('')
  const [country, setCountry] = useState(reference.supplierCountryCode ?? 'IN')
  const [reason, setReason] = useState('')
  const [certificate, setCertificate] = useState('')
  const [from, setFrom] = useState(new Date().toISOString().slice(0, 10))
  const [to, setTo] = useState('')
  const [submitted, setSubmitted] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const errors = {
    ownerId: !/^\d+$/.test(ownerId.trim()) ? t('admin.tax.exemptions.errors.ownerId') : undefined,
    country: !country ? t('admin.tax.exemptions.errors.country') : undefined,
    reason: !reason.trim() ? t('admin.tax.exemptions.errors.reason') : undefined,
    from: !from ? t('admin.tax.exemptions.errors.from') : undefined,
  }

  const save = async () => {
    setSubmitted(true)
    if (Object.values(errors).some(Boolean)) return
    setBusy(true)
    setError(null)
    try {
      await taxApi.createExemption({ ownerType, ownerId: Number(ownerId), countryCode: country, reason: reason.trim(), certificateNumber: certificate.trim() || undefined, effectiveFrom: from, effectiveTo: to || undefined })
      onSaved()
    } catch (e) {
      setError(errorText(e, t('admin.tax.rules.actionError')))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Dialog open onClose={busy ? undefined : onClose} aria-labelledby="tax-exemption-title" maxWidth="xs" fullWidth>
      <DialogTitle id="tax-exemption-title">{t('admin.tax.exemptions.new')}</DialogTitle>
      <DialogContent sx={{ display: 'grid', gap: 2, pt: '8px !important' }}>
        <TextField select label={t('admin.tax.exemptions.ownerType')} value={ownerType} onChange={(e) => setOwnerType(e.target.value as 'CUSTOMER' | 'ORGANIZATION')}>
          <MenuItem value="ORGANIZATION">{t('admin.tax.exemptions.organization')}</MenuItem>
          <MenuItem value="CUSTOMER">{t('admin.tax.exemptions.customer')}</MenuItem>
        </TextField>
        <TextField label={t('admin.tax.exemptions.ownerId')} value={ownerId} onChange={(e) => setOwnerId(e.target.value)} required slotProps={{ htmlInput: { inputMode: 'numeric' } }}
          error={submitted && Boolean(errors.ownerId)} helperText={submitted ? errors.ownerId : undefined} />
        <TextField select label={t('admin.tax.exemptions.countryField')} value={country} onChange={(e) => setCountry(e.target.value)} required>
          {reference.countries.map((c) => <MenuItem key={c.code} value={c.code}>{c.name}</MenuItem>)}
        </TextField>
        <TextField label={t('admin.tax.exemptions.reason')} value={reason} onChange={(e) => setReason(e.target.value)} required error={submitted && Boolean(errors.reason)} helperText={submitted ? errors.reason : undefined} />
        <TextField label={t('admin.tax.exemptions.certificate')} value={certificate} onChange={(e) => setCertificate(e.target.value)} />
        <TextField type="date" label={t('admin.tax.exemptions.from')} value={from} onChange={(e) => setFrom(e.target.value)} required slotProps={{ inputLabel: { shrink: true } }} />
        <TextField type="date" label={t('admin.tax.exemptions.to')} value={to} onChange={(e) => setTo(e.target.value)} slotProps={{ inputLabel: { shrink: true } }} />
        {error && <Alert severity="error" role="alert">{error}</Alert>}
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={busy}>{t('forms.cancel')}</Button>
        <Button variant="contained" onClick={() => void save()} disabled={busy}>{t('admin.tax.exemptions.save')}</Button>
      </DialogActions>
    </Dialog>
  )
}
