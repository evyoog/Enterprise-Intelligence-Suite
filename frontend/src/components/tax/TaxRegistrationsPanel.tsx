import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Checkbox, Chip, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, MenuItem, Paper, Skeleton, Table, TableBody,
  TableCell, TableHead, TableRow, TextField, Typography,
} from '@mui/material'
import { Plus } from 'lucide-react'
import { taxApi, type TaxReference, type TaxRegistration, type TaxType } from '../../api/taxApi'
import { ConfirmDialog } from '../ui/ConfirmDialog'
import { EmptyState } from '../ui/EmptyState'
import { errorText, formatDate } from './taxFormat'

/** REQ-BIL-002.37: the supplier's tax registrations, sent to an external provider. */
export function TaxRegistrationsPanel({ reference, onToast }: { reference: TaxReference; onToast: (message: string) => void }) {
  const { t, i18n } = useTranslation()
  const [rows, setRows] = useState<TaxRegistration[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [editing, setEditing] = useState<TaxRegistration | 'new' | null>(null)
  const [deleting, setDeleting] = useState<TaxRegistration | null>(null)

  const load = useCallback(() => {
    return taxApi.registrations().then((r) => { setRows(r); setError(null) }).catch((e) => setError(errorText(e, t('admin.tax.loadError'))))
  }, [t])
  useEffect(() => { void load() }, [load])

  const remove = async () => {
    if (!deleting?.id) return
    try {
      await taxApi.deleteRegistration(deleting.id)
      onToast(t('admin.tax.saved'))
      void load()
    } catch (e) {
      setError(errorText(e, t('admin.tax.rules.actionError')))
    } finally {
      setDeleting(null)
    }
  }

  return (
    <Paper variant="outlined" sx={{ p: { xs: 2, sm: 3 }, borderRadius: 3 }}>
      <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-start', flexWrap: 'wrap', mb: 2 }}>
        <Box sx={{ flex: 1, minWidth: 260 }}>
          <Typography component="h2" variant="subtitle1" sx={{ fontWeight: 700 }}>{t('admin.tax.registrations.title')}</Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('admin.tax.registrations.description')}</Typography>
        </Box>
        <Button variant="contained" startIcon={<Plus size={16} />} onClick={() => setEditing('new')}>{t('admin.tax.registrations.new')}</Button>
      </Box>
      {error && <Alert severity="error" action={<Button onClick={() => void load()}>{t('admin.tax.retry')}</Button>}>{error}</Alert>}
      {!error && rows === null && <Skeleton variant="rounded" height={100} />}
      {rows && rows.length === 0 && <EmptyState title={t('admin.tax.registrations.title')} description={t('admin.tax.registrations.empty')} />}
      {rows && rows.length > 0 && (
        <Table size="small" aria-label={t('admin.tax.registrations.title')}>
          <TableHead>
            <TableRow>
              <TableCell>{t('admin.tax.registrations.country')}</TableCell><TableCell>{t('admin.tax.registrations.taxType')}</TableCell>
              <TableCell>{t('admin.tax.registrations.number')}</TableCell><TableCell>{t('admin.tax.rules.columns.dates')}</TableCell><TableCell />
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((r, i) => (
              <TableRow key={r.id ?? `profile-${i}`}>
                <TableCell>{r.stateName ? `${r.stateName}, ` : ''}{r.countryName}</TableCell>
                <TableCell>{t(`admin.tax.taxType.${r.taxType}`)}</TableCell>
                <TableCell sx={{ fontFamily: 'monospace' }}>{r.registrationNumber}{r.fromBusinessProfile && <Chip size="small" sx={{ ml: 1 }} label={t('admin.tax.registrations.fromProfile')} />}</TableCell>
                <TableCell>{r.effectiveFrom ? formatDate(r.effectiveFrom, i18n.language) : '…'} – {r.effectiveTo ? formatDate(r.effectiveTo, i18n.language) : '…'}{r.enabled ? '' : ` (${t('forms.inactive')})`}</TableCell>
                <TableCell align="right">
                  {!r.fromBusinessProfile && <Button size="small" onClick={() => setEditing(r)}>{t('admin.tax.classes.edit')}</Button>}
                  {!r.fromBusinessProfile && <Button size="small" color="error" onClick={() => setDeleting(r)}>{t('admin.tax.registrations.delete')}</Button>}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
      {editing !== null && <RegistrationDialog registration={editing === 'new' ? null : editing} reference={reference} onClose={() => setEditing(null)}
        onSaved={() => { setEditing(null); onToast(t('admin.tax.saved')); void load() }} />}
      <ConfirmDialog open={deleting !== null} title={t('admin.tax.registrations.deleteTitle')} body={t('admin.tax.registrations.deleteBody')}
        confirmLabel={t('admin.tax.registrations.deleteConfirm')} onConfirm={() => void remove()} onClose={() => setDeleting(null)} />
    </Paper>
  )
}

function RegistrationDialog({ registration, reference, onClose, onSaved }: {
  registration: TaxRegistration | null; reference: TaxReference; onClose: () => void; onSaved: () => void
}) {
  const { t } = useTranslation()
  const [country, setCountry] = useState(registration?.countryCode ?? reference.supplierCountryCode ?? 'IN')
  const [state, setState] = useState(registration?.stateCode ?? '')
  const [taxType, setTaxType] = useState<TaxType>(registration?.taxType ?? 'GST')
  const [number, setNumber] = useState(registration?.registrationNumber ?? '')
  const [from, setFrom] = useState(registration?.effectiveFrom ?? '')
  const [to, setTo] = useState(registration?.effectiveTo ?? '')
  const [enabled, setEnabled] = useState(registration?.enabled ?? true)
  const [submitted, setSubmitted] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const states = reference.states[country] ?? []

  const save = async () => {
    setSubmitted(true)
    if (!country || !number.trim()) return
    setBusy(true)
    setError(null)
    const body = { countryCode: country, stateCode: state || undefined, taxType, registrationNumber: number.trim(), effectiveFrom: from || undefined, effectiveTo: to || undefined, enabled }
    try {
      if (registration?.id) await taxApi.updateRegistration(registration.id, body); else await taxApi.createRegistration(body)
      onSaved()
    } catch (e) {
      setError(errorText(e, t('admin.tax.rules.actionError')))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Dialog open onClose={busy ? undefined : onClose} aria-labelledby="tax-registration-title" maxWidth="xs" fullWidth>
      <DialogTitle id="tax-registration-title">{t('admin.tax.registrations.new')}</DialogTitle>
      <DialogContent sx={{ display: 'grid', gap: 2, pt: '8px !important' }}>
        <TextField select label={t('admin.tax.registrations.country')} value={country} onChange={(e) => { setCountry(e.target.value); setState('') }} required>
          {reference.countries.map((c) => <MenuItem key={c.code} value={c.code}>{c.name}</MenuItem>)}
        </TextField>
        {states.length > 0 ? (
          <TextField select label={t('admin.tax.registrations.state')} value={state} onChange={(e) => setState(e.target.value)}>
            <MenuItem value="">—</MenuItem>
            {states.map((s) => <MenuItem key={s.code} value={s.code}>{s.name}</MenuItem>)}
          </TextField>
        ) : (
          <TextField label={t('admin.tax.registrations.state')} value={state} onChange={(e) => setState(e.target.value)} />
        )}
        <TextField select label={t('admin.tax.registrations.taxType')} value={taxType} onChange={(e) => setTaxType(e.target.value as TaxType)}>
          {reference.taxTypes.map((v) => <MenuItem key={v} value={v}>{t(`admin.tax.taxType.${v}`)}</MenuItem>)}
        </TextField>
        <TextField label={t('admin.tax.registrations.number')} value={number} onChange={(e) => setNumber(e.target.value)} required
          error={submitted && !number.trim()} helperText={submitted && !number.trim() ? t('admin.tax.registrations.errors.number') : undefined} />
        <TextField type="date" label={t('admin.tax.registrations.from')} value={from} onChange={(e) => setFrom(e.target.value)} slotProps={{ inputLabel: { shrink: true } }} />
        <TextField type="date" label={t('admin.tax.registrations.to')} value={to} onChange={(e) => setTo(e.target.value)} slotProps={{ inputLabel: { shrink: true } }} />
        <FormControlLabel control={<Checkbox checked={enabled} onChange={(e) => setEnabled(e.target.checked)} />} label={t('admin.tax.registrations.enabled')} />
        {error && <Alert severity="error" role="alert">{error}</Alert>}
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={busy}>{t('forms.cancel')}</Button>
        <Button variant="contained" onClick={() => void save()} disabled={busy}>{t('admin.tax.registrations.save')}</Button>
      </DialogActions>
    </Dialog>
  )
}
