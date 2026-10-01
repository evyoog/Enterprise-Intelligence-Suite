import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Paper, Skeleton, Snackbar, TextField, Typography } from '@mui/material'
import { adminBillingApi, type OfflineBankDetails } from '../../api/billingApi'
import { ApiError } from '../../api/client'
import { PageHeader } from '../../components/layout/PageHeader'

interface Form { accountName: string; bankName: string; accountNumber: string; ifsc: string; swiftBic: string }

const EMPTY: Form = { accountName: '', bankName: '', accountNumber: '', ifsc: '', swiftBic: '' }

function toForm(d: OfflineBankDetails): Form {
  return {
    accountName: d.accountName ?? '', bankName: d.bankName ?? '', accountNumber: d.accountNumber ?? '',
    ifsc: d.ifsc ?? '', swiftBic: d.swiftBic ?? '',
  }
}

/** "/admin/billing/settings" — MANAGE_BILLING. C55, REQ-BIL-001.21
 * (admin-billing-settings.md). Bank details are printed on every offline
 * invoice, so they are not secrets and live in the database, not in
 * config/secrets.env (BR-SEC-001). */
export function AdminBillingSettingsPage() {
  const { t } = useTranslation()
  const [saved, setSaved] = useState<OfflineBankDetails | null>(null)
  const [form, setForm] = useState<Form>(EMPTY)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [touched, setTouched] = useState(false)
  const [busy, setBusy] = useState(false)
  const [toast, setToast] = useState(false)

  const load = () => adminBillingApi.offlineBankDetails()
    .then((d) => { setSaved(d); setForm(toForm(d)) })
    .catch((e) => setLoadError(e instanceof ApiError ? e.message : t('admin.billingSettings.loadError')))
  const retry = () => { setLoadError(null); void load() }
  // eslint-disable-next-line react-hooks/exhaustive-deps -- load once on mount
  useEffect(() => { void load() }, [])

  const dirty = saved !== null && JSON.stringify(form) !== JSON.stringify(toForm(saved))

  // "Unsaved changes" warning when the tab is closed or reloaded with edits.
  useEffect(() => {
    if (!dirty) return
    const warn = (e: BeforeUnloadEvent) => { e.preventDefault() }
    window.addEventListener('beforeunload', warn)
    return () => window.removeEventListener('beforeunload', warn)
  }, [dirty])

  const errors: Record<keyof Form, string | null> = {
    accountName: !form.accountName.trim() ? t('admin.billingSettings.required') : form.accountName.length > 200 ? t('admin.billingSettings.tooLong', { max: 200 }) : null,
    bankName: !form.bankName.trim() ? t('admin.billingSettings.required') : form.bankName.length > 200 ? t('admin.billingSettings.tooLong', { max: 200 }) : null,
    accountNumber: !form.accountNumber.trim() ? t('admin.billingSettings.required') : form.accountNumber.length > 34 ? t('admin.billingSettings.tooLong', { max: 34 }) : null,
    ifsc: form.ifsc && !/^[A-Za-z0-9]{11}$/.test(form.ifsc) ? t('admin.billingSettings.ifscLength') : null,
    swiftBic: form.swiftBic && !/^[A-Za-z0-9]{8}([A-Za-z0-9]{3})?$/.test(form.swiftBic) ? t('admin.billingSettings.swiftLength') : null,
  }
  const valid = Object.values(errors).every((e) => !e)

  const save = () => {
    setTouched(true)
    if (!valid) return
    setBusy(true)
    setError(null)
    adminBillingApi.saveOfflineBankDetails({
      accountName: form.accountName.trim(), bankName: form.bankName.trim(), accountNumber: form.accountNumber.trim(),
      ifsc: form.ifsc.trim() || undefined, swiftBic: form.swiftBic.trim() || undefined,
    })
      .then((d) => { setSaved(d); setForm(toForm(d)); setTouched(false); setToast(true) })
      .catch((e) => setError(e instanceof ApiError ? e.message : t('checkout.genericError')))
      .finally(() => setBusy(false))
  }

  const field = (key: keyof Form, labelKey: string, required: boolean, maxLength: number) => (
    <TextField
      label={t(`admin.billingSettings.${labelKey}`)} value={form[key]} required={required}
      onChange={(e) => setForm((f) => ({ ...f, [key]: e.target.value }))}
      error={touched && Boolean(errors[key])} helperText={touched ? errors[key] : undefined}
      slotProps={{ htmlInput: { maxLength } }} fullWidth
    />
  )

  return (
    <>
      <PageHeader title={t('admin.billingSettings.title')} subtitle={t('admin.billingSettings.subtitle')} />
      {loadError && (
        <Alert severity="error" action={<Button onClick={retry}>{t('admin.billingSettings.retry')}</Button>}>{loadError}</Alert>
      )}
      {!loadError && saved === null && <Skeleton variant="rounded" height={320} sx={{ maxWidth: 720 }} />}
      {saved !== null && (
        <Paper variant="outlined" component="form" noValidate onSubmit={(e: React.FormEvent) => { e.preventDefault(); save() }}
          sx={{ p: 3, borderRadius: 3, maxWidth: 720 }}>
          <Typography component="h2" variant="h6" sx={{ fontWeight: 700, mb: 2 }}>{t('admin.billingSettings.bankTitle')}</Typography>
          {!saved.accountNumber && <Alert severity="info" sx={{ mb: 2 }}>{t('admin.billingSettings.empty')}</Alert>}
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' } }}>
            <Box sx={{ gridColumn: { sm: '1 / -1' } }}>{field('accountName', 'accountName', true, 200)}</Box>
            {field('bankName', 'bankName', true, 200)}
            {field('accountNumber', 'accountNumber', true, 34)}
            {field('ifsc', 'ifsc', false, 11)}
            {field('swiftBic', 'swift', false, 11)}
          </Box>
          {dirty && <Alert severity="warning" sx={{ mt: 2 }}>{t('admin.billingSettings.unsaved')}</Alert>}
          <Box sx={{ display: 'flex', gap: 1, mt: 3, alignItems: 'center', flexWrap: 'wrap' }}>
            <Button type="submit" variant="contained" disabled={!dirty || busy}>{t('admin.billingSettings.save')}</Button>
            <Button disabled={!dirty || busy} onClick={() => { setForm(toForm(saved)); setTouched(false) }}>{t('admin.billingSettings.cancel')}</Button>
            {saved.updatedAt && (
              <Typography variant="body2" sx={{ color: 'text.secondary', ml: 'auto' }}>
                {t('admin.billingSettings.lastUpdated', { date: new Date(saved.updatedAt).toLocaleString() })}
              </Typography>
            )}
          </Box>
        </Paper>
      )}
      <Snackbar open={toast} autoHideDuration={4000} onClose={() => setToast(false)}>
        <Alert severity="success" variant="filled" onClose={() => setToast(false)}>{t('admin.billingSettings.saved')}</Alert>
      </Snackbar>
    </>
  )
}
