import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, Paper, Skeleton,
  Snackbar, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography,
} from '@mui/material'
import { Ticket } from 'lucide-react'
import { ApiError } from '../../api/client'
import { couponApi, type Coupon, type CouponDiscountType, type CouponStatus, type SaveCouponInput } from '../../api/couponApi'
import { PageHeader } from '../../components/layout/PageHeader'
import { ConfirmDialog } from '../../components/ui/ConfirmDialog'
import { EmptyState } from '../../components/ui/EmptyState'
import { formatMoney } from '../../utils/money'

const CURRENCIES = ['INR', 'USD', 'EUR', 'GBP']
const CODE_PATTERN = /^[A-Z0-9_-]{3,40}$/
const STATUS_COLOR: Record<CouponStatus, 'success' | 'default' | 'warning'> = { ACTIVE: 'success', DISABLED: 'default', EXPIRED: 'warning' }

/** "2026-12-31T18:30" for a datetime-local input, in the browser's time zone. */
function toLocalInput(iso: string) {
  const d = new Date(iso)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const browserTimeZone = () => Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC'

/**
 * "/admin/billing/coupons" — MANAGE_BILLING. C100 (PB-1 note): the platform administrator creates, edits, disables (before the expiry) and deletes
 * (never redeemed) coupon codes that customers enter in the cart. The backend re-checks every rule; this page only gives earlier feedback.
 */
export function AdminCouponsPage() {
  const { t, i18n } = useTranslation()
  const [coupons, setCoupons] = useState<Coupon[] | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [toast, setToast] = useState<string | null>(null)
  const [editing, setEditing] = useState<Coupon | 'new' | null>(null)
  const [deleting, setDeleting] = useState<Coupon | null>(null)
  const [busyId, setBusyId] = useState<number | null>(null)

  const load = useCallback(() => {
    couponApi.list().then((rows) => { setCoupons(rows); setLoadError(null) })
      .catch((e) => setLoadError(e instanceof ApiError ? e.message : t('admin.coupons.loadError')))
  }, [t])
  useEffect(() => { load() }, [load])

  const discountText = (c: Coupon) => c.discountType === 'PERCENT'
    ? t('admin.coupons.percentOff', { percent: Number(c.percentOff) })
    : t('admin.coupons.amountOff', { amount: formatMoney(c.amountOff ?? 0, c.currency ?? 'USD', i18n.language) })

  const toggle = async (c: Coupon) => {
    setBusyId(c.id)
    setActionError(null)
    try {
      const updated = await couponApi.setEnabled(c.id, !c.enabled)
      setCoupons((rows) => rows?.map((r) => (r.id === updated.id ? updated : r)) ?? rows)
      setToast(t(updated.enabled ? 'admin.coupons.enabledToast' : 'admin.coupons.disabledToast', { code: c.code }))
    } catch (e) {
      setActionError(e instanceof ApiError ? e.message : t('admin.coupons.actionError'))
    } finally {
      setBusyId(null)
    }
  }

  const remove = async () => {
    if (!deleting) return
    const target = deleting
    setBusyId(target.id)
    setActionError(null)
    try {
      await couponApi.remove(target.id)
      setCoupons((rows) => rows?.filter((r) => r.id !== target.id) ?? rows)
      setToast(t('admin.coupons.deletedToast', { code: target.code }))
    } catch (e) {
      // 409: the coupon was redeemed, so it can only be disabled.
      setActionError(e instanceof ApiError ? e.message : t('admin.coupons.actionError'))
    } finally {
      setBusyId(null)
      setDeleting(null)
    }
  }

  return (
    <>
      <PageHeader icon={Ticket} accent="amber" eyebrow={t('admin.billingSettings.eyebrow')}
        title={t('admin.coupons.title')} subtitle={t('admin.coupons.subtitle')}
        action={<Button variant="contained" onClick={() => setEditing('new')}>{t('admin.coupons.add')}</Button>} />

      {loadError && <Alert severity="error" sx={{ mb: 2 }} action={<Button color="inherit" onClick={load}>{t('admin.coupons.retry')}</Button>}>{loadError}</Alert>}
      {actionError && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setActionError(null)}>{actionError}</Alert>}

      {!loadError && coupons === null && <Skeleton variant="rounded" height={200} />}
      {coupons?.length === 0 && (
        <EmptyState icon={Ticket} title={t('admin.coupons.empty.title')} description={t('admin.coupons.empty.body')}
          action={<Button variant="contained" onClick={() => setEditing('new')}>{t('admin.coupons.add')}</Button>} />
      )}
      {coupons && coupons.length > 0 && (
        <Paper variant="outlined">
          <TableContainer>
            <Table size="small" aria-label={t('admin.coupons.title')}>
              <TableHead>
                <TableRow>
                  <TableCell>{t('admin.coupons.columns.code')}</TableCell>
                  <TableCell>{t('admin.coupons.columns.discount')}</TableCell>
                  <TableCell>{t('admin.coupons.columns.expires')}</TableCell>
                  <TableCell>{t('admin.coupons.columns.status')}</TableCell>
                  <TableCell align="right">{t('admin.coupons.columns.redeemed')}</TableCell>
                  <TableCell align="right">{t('admin.coupons.columns.actions')}</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {coupons.map((c) => (
                  <TableRow key={c.id}>
                    <TableCell>
                      <Typography sx={{ fontWeight: 700, fontFamily: 'monospace' }}>{c.code}</Typography>
                      {c.description && <Typography variant="caption" sx={{ color: 'text.secondary' }}>{c.description}</Typography>}
                    </TableCell>
                    <TableCell>{discountText(c)}</TableCell>
                    <TableCell>{new Date(c.expiresAt).toLocaleString(i18n.language)}</TableCell>
                    <TableCell><Chip size="small" color={STATUS_COLOR[c.status]} label={t(`admin.coupons.status.${c.status}`)} /></TableCell>
                    <TableCell align="right">{c.redeemedCount}</TableCell>
                    <TableCell align="right" sx={{ whiteSpace: 'nowrap' }}>
                      <Button size="small" onClick={() => setEditing(c)} aria-label={t('admin.coupons.editCode', { code: c.code })}>{t('admin.coupons.edit')}</Button>
                      {c.status !== 'EXPIRED' && (
                        <Button size="small" disabled={busyId === c.id} onClick={() => toggle(c)}
                          aria-label={t(c.enabled ? 'admin.coupons.disableCode' : 'admin.coupons.enableCode', { code: c.code })}>
                          {t(c.enabled ? 'admin.coupons.disable' : 'admin.coupons.enable')}
                        </Button>
                      )}
                      <Button size="small" color="error" disabled={busyId === c.id} onClick={() => setDeleting(c)}
                        aria-label={t('admin.coupons.deleteCode', { code: c.code })}>
                        {t('admin.coupons.delete')}
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
        <CouponDialog coupon={editing === 'new' ? null : editing} onClose={() => setEditing(null)}
          onSaved={(saved, created) => {
            setEditing(null)
            setToast(t(created ? 'admin.coupons.createdToast' : 'admin.coupons.savedToast', { code: saved.code }))
            load()
          }} />
      )}
      <ConfirmDialog open={Boolean(deleting)} title={t('admin.coupons.deleteTitle', { code: deleting?.code })} body={t('admin.coupons.deleteBody')}
        confirmLabel={t('admin.coupons.delete')} busy={busyId !== null && deleting?.id === busyId} onConfirm={remove} onClose={() => setDeleting(null)} />
      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)}>
        {toast ? <Alert severity="success" variant="filled" onClose={() => setToast(null)}>{toast}</Alert> : undefined}
      </Snackbar>
    </>
  )
}

function CouponDialog({ coupon, onClose, onSaved }: { coupon: Coupon | null; onClose: () => void; onSaved: (saved: Coupon, created: boolean) => void }) {
  const { t } = useTranslation()
  const [code, setCode] = useState(coupon?.code ?? '')
  const [description, setDescription] = useState(coupon?.description ?? '')
  const [type, setType] = useState<CouponDiscountType>(coupon?.discountType ?? 'PERCENT')
  const [percent, setPercent] = useState(coupon?.percentOff != null ? String(Number(coupon.percentOff)) : '')
  const [amount, setAmount] = useState(coupon?.amountOff != null ? (coupon.amountOff / 100).toFixed(2) : '')
  const [currency, setCurrency] = useState(coupon?.currency ?? 'INR')
  const [expires, setExpires] = useState(coupon ? toLocalInput(coupon.expiresAt) : '')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const percentValue = Number(percent)
  const amountMinor = Math.round(parseFloat(amount || '0') * 100)
  const expiresDate = expires ? new Date(expires) : null

  type Field = 'code' | 'description' | 'percent' | 'amount' | 'expires'
  const validate = (): Record<Field, string | null> => ({
    code: !CODE_PATTERN.test(code) ? t('admin.coupons.form.codeInvalid') : null,
    description: description.length > 200 ? t('admin.coupons.form.descriptionTooLong') : null,
    percent: type === 'PERCENT' && !(percent !== '' && percentValue >= 1 && percentValue <= 99) ? t('admin.coupons.form.percentInvalid') : null,
    amount: type === 'FIXED' && !(amountMinor > 0) ? t('admin.coupons.form.amountInvalid') : null,
    expires: !expiresDate || Number.isNaN(expiresDate.getTime()) ? t('admin.coupons.form.expiryRequired')
      : expiresDate.getTime() <= Date.now() ? t('admin.coupons.form.expiryPast') : null,
  })
  // Shown after the first Save; a field's message clears as soon as it is edited.
  const [errors, setErrors] = useState<Record<Field, string | null> | null>(null)
  const shown = (key: Field) => errors?.[key] ?? null
  const edited = (key: Field) => setErrors((e) => (e ? { ...e, [key]: null } : e))

  const submit = async () => {
    const found = validate()
    setErrors(found)
    if (Object.values(found).some(Boolean) || !expiresDate) return
    const body: SaveCouponInput = {
      code, description: description.trim(), discountType: type,
      percentOff: type === 'PERCENT' ? percentValue : null,
      amountOff: type === 'FIXED' ? amountMinor : null,
      currency: type === 'FIXED' ? currency : null,
      expiresAt: expiresDate.toISOString(),
    }
    setBusy(true)
    setError(null)
    try {
      const saved = coupon ? await couponApi.update(coupon.id, body) : await couponApi.create(body)
      onSaved(saved, !coupon)
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('admin.coupons.actionError'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Dialog open onClose={busy ? undefined : onClose} aria-labelledby="coupon-dialog-title" maxWidth="xs" fullWidth>
      <DialogTitle id="coupon-dialog-title">{t(coupon ? 'admin.coupons.editTitle' : 'admin.coupons.addTitle')}</DialogTitle>
      <DialogContent sx={{ display: 'grid', gap: 2, pt: '8px !important' }}>
        {error && <Alert severity="error">{error}</Alert>}
        <TextField label={t('admin.coupons.form.code')} value={code} required disabled={Boolean(coupon)}
          onChange={(e) => { setCode(e.target.value.toUpperCase()); edited('code') }} error={Boolean(shown('code'))}
          helperText={shown('code') ?? t('admin.coupons.form.codeHelp')}
          slotProps={{ htmlInput: { maxLength: 40, autoCapitalize: 'characters', autoComplete: 'off' } }} />
        <TextField label={t('admin.coupons.form.description')} value={description} onChange={(e) => { setDescription(e.target.value); edited('description') }}
          error={Boolean(shown('description'))} helperText={shown('description')} slotProps={{ htmlInput: { maxLength: 200 } }} />
        <TextField select label={t('admin.coupons.form.type')} value={type} onChange={(e) => setType(e.target.value as CouponDiscountType)}>
          <MenuItem value="PERCENT">{t('admin.coupons.form.typePercent')}</MenuItem>
          <MenuItem value="FIXED">{t('admin.coupons.form.typeFixed')}</MenuItem>
        </TextField>
        {type === 'PERCENT' ? (
          <TextField label={t('admin.coupons.form.percent')} type="number" value={percent} required onChange={(e) => { setPercent(e.target.value); edited('percent') }}
            error={Boolean(shown('percent'))} helperText={shown('percent') ?? t('admin.coupons.form.percentHelp')}
            slotProps={{ htmlInput: { min: 1, max: 99, step: 1 } }} />
        ) : (
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '2fr 1fr' }, gap: 2, alignItems: 'start' }}>
            <TextField label={t('admin.coupons.form.amount')} type="number" value={amount} required onChange={(e) => { setAmount(e.target.value); edited('amount') }}
              error={Boolean(shown('amount'))} helperText={shown('amount') ?? t('admin.coupons.form.amountHelp')}
              slotProps={{ htmlInput: { min: 0, step: '0.01' } }} />
            <TextField select label={t('admin.coupons.form.currency')} value={currency} onChange={(e) => setCurrency(e.target.value)}>
              {CURRENCIES.map((c) => <MenuItem key={c} value={c}>{c}</MenuItem>)}
            </TextField>
          </Box>
        )}
        <TextField label={t('admin.coupons.form.expires')} type="datetime-local" value={expires} required onChange={(e) => { setExpires(e.target.value); edited('expires') }}
          error={Boolean(shown('expires'))} helperText={shown('expires') ?? t('admin.coupons.form.expiresHelp', { zone: browserTimeZone() })}
          slotProps={{ inputLabel: { shrink: true } }} />
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={busy}>{t('common.cancel')}</Button>
        <Button variant="contained" onClick={submit} disabled={busy} startIcon={busy ? <CircularProgress size={14} color="inherit" /> : undefined}>
          {t('admin.coupons.form.save')}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
