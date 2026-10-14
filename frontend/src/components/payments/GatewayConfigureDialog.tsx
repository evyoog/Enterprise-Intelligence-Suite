import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, Skeleton, TextField, Typography,
} from '@mui/material'
import { ApiError } from '../../api/client'
import { paymentGatewayApi, type GatewayDetail } from '../../api/paymentGatewayApi'

/**
 * C98 (REQ-BIL-003.4): the configuration form is built from the provider's own field list, so every provider gets the right fields and the
 * screen knows nothing about any one of them. Secret fields are write-only: a saved one shows only "set" and a blank keeps it. Nothing the
 * server returns contains a secret.
 */
export function GatewayConfigureDialog({ code, name, onClose, onSaved }: {
  code: string; name: string; onClose: () => void; onSaved: (message: string) => void
}) {
  const { t } = useTranslation()
  const [detail, setDetail] = useState<GatewayDetail | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [environment, setEnvironment] = useState('TEST')
  const [values, setValues] = useState<Record<string, string>>({})
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    paymentGatewayApi.detail(code)
      .then((d) => { setDetail(d); setEnvironment(d.gateway.environment ?? d.environments[0] ?? 'TEST') })
      .catch((e) => setLoadError(e instanceof ApiError ? e.message : t('admin.paymentGateway.loadError')))
  }, [code, t])

  const entered = Object.fromEntries(Object.entries(values).filter(([, v]) => v.trim() !== ''))
  const canStore = detail?.encryptionAvailable ?? false
  const hasEnteredValues = Object.keys(entered).length > 0
  const changed = hasEnteredValues || (detail != null && environment !== (detail.gateway.environment ?? detail.environments[0]))

  const save = async () => {
    setBusy(true)
    setError(null)
    try {
      await paymentGatewayApi.saveConfiguration(code, { environment, fields: entered })
      onSaved(t('admin.paymentGateway.saved', { name }))
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('admin.paymentGateway.saveFailed'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Dialog open onClose={busy ? undefined : onClose} fullWidth maxWidth="sm" aria-labelledby="gw-config-title">
      <DialogTitle id="gw-config-title">{t('admin.paymentGateway.configureTitle', { name })}</DialogTitle>
      <DialogContent>
        {loadError && <Alert severity="error">{loadError}</Alert>}
        {!detail && !loadError && <Skeleton variant="rounded" height={220} />}
        {detail && (
          <Box component="form" noValidate onSubmit={(e) => { e.preventDefault(); if (changed && canStore && !busy) void save() }}
            sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            {!canStore && <Alert severity="warning">{t('admin.paymentGateway.noEncryptionKey')}</Alert>}
            {detail.credentialsUnreadable && <Alert severity="error">{t('admin.paymentGateway.unreadable')}</Alert>}
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('admin.paymentGateway.configureHelp')}</Typography>
            {detail.gateway.activated && <Alert severity="info">{t('admin.paymentGateway.rotationNote')}</Alert>}
            <TextField select size="small" label={t('admin.paymentGateway.environment')} value={environment} onChange={(e) => setEnvironment(e.target.value)}>
              {detail.environments.map((env) => <MenuItem key={env} value={env}>{t(`admin.paymentGateway.env.${env}`, { defaultValue: env })}</MenuItem>)}
            </TextField>
            {detail.fields.map((f) => (
              <TextField key={f.key} size="small" required={f.required && !f.configured}
                label={f.label} value={values[f.key] ?? ''} onChange={(e) => setValues((v) => ({ ...v, [f.key]: e.target.value }))}
                type={f.secret ? 'password' : 'text'} autoComplete="off" slotProps={{ htmlInput: { maxLength: 500, spellCheck: false } }}
                placeholder={f.configured ? (f.secret ? t('admin.paymentGateway.keepSecret') : f.maskedValue ?? '') : ''}
                helperText={[
                  f.configured ? t('admin.paymentGateway.fieldSet', { source: t(`admin.paymentGateway.source.${f.source}`) }) : t('admin.paymentGateway.fieldNotSet'),
                  f.maskedValue && !f.secret ? t('admin.paymentGateway.current', { value: f.maskedValue }) : null,
                  f.help,
                ].filter(Boolean).join(' · ')} />
            ))}
            {error && <Alert severity="error" role="alert">{error}</Alert>}
          </Box>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={busy}>{t('admin.paymentGateway.cancel')}</Button>
        <Button variant="contained" disabled={!detail || !changed || !canStore || busy} onClick={() => void save()}
          startIcon={busy ? <CircularProgress size={16} color="inherit" /> : undefined}>
          {t('admin.paymentGateway.save')}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
