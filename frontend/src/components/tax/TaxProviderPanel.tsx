import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Chip, CircularProgress, MenuItem, Paper, TextField, Typography } from '@mui/material'
import { CheckCircle2, XCircle } from 'lucide-react'
import { taxApi, type TaxStatus, type ValidationReport } from '../../api/taxApi'
import { ValidationChecks } from './TaxEngineCard'
import { errorText, formatDateTime } from './taxFormat'

/**
 * REQ-BIL-002.30 to .34: the external provider settings (non-secret), which credentials the server has (set / not set, never the
 * values) and Test connection with capability validation. The address and credentials live in the backend environment (BR-30).
 */
export function TaxProviderPanel({ status, onStatus, onToast }: { status: TaxStatus; onStatus: (s: TaxStatus) => void; onToast: (message: string) => void }) {
  const { t, i18n } = useTranslation()
  const ext = status.external
  const [providerCode, setProviderCode] = useState(ext.providerCode ?? '')
  const [companyCode, setCompanyCode] = useState(ext.companyCode ?? '')
  const [environment, setEnvironment] = useState<string>(ext.environment)
  const [timeoutSeconds, setTimeoutSeconds] = useState(String(ext.timeoutSeconds))
  const [maxAttempts, setMaxAttempts] = useState(String(ext.maxAttempts))
  const [saving, setSaving] = useState(false)
  const [testing, setTesting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [report, setReport] = useState<ValidationReport | null>(null)


  const timeoutOk = /^\d+$/.test(timeoutSeconds) && Number(timeoutSeconds) >= 2 && Number(timeoutSeconds) <= 60
  const attemptsOk = /^\d+$/.test(maxAttempts) && Number(maxAttempts) >= 1 && Number(maxAttempts) <= 5

  const save = async () => {
    setSaving(true)
    setError(null)
    try {
      onStatus(await taxApi.saveProvider({ providerCode, companyCode: companyCode.trim() || undefined, environment, timeoutSeconds: Number(timeoutSeconds), maxAttempts: Number(maxAttempts) }))
      onToast(t('admin.tax.saved'))
    } catch (e) {
      setError(errorText(e, t('admin.tax.provider.error')))
    } finally {
      setSaving(false)
    }
  }

  const test = async () => {
    setTesting(true)
    setError(null)
    try {
      setReport(await taxApi.validateProvider())
      onStatus(await taxApi.status())
    } catch (e) {
      setError(errorText(e, t('admin.tax.provider.error')))
    } finally {
      setTesting(false)
    }
  }

  const validationColor = ext.validationStatus === 'PASSED' ? 'success' : ext.validationStatus === 'FAILED' ? 'error' : 'default'
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
      <Paper variant="outlined" component="section" aria-labelledby="tax-provider-title" sx={{ p: { xs: 2, sm: 3 }, borderRadius: 3 }}>
        <Typography id="tax-provider-title" component="h2" variant="subtitle1" sx={{ fontWeight: 700 }}>{t('admin.tax.provider.title')}</Typography>
        <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2 }}>{t('admin.tax.provider.description')}</Typography>
        <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, minmax(0, 1fr))' } }}>
          <TextField select label={t('admin.tax.provider.select')} value={providerCode} onChange={(e) => setProviderCode(e.target.value)}>
            <MenuItem value="" disabled>{t('admin.tax.provider.none')}</MenuItem>
            {ext.providers.map((p) => <MenuItem key={p.code} value={p.code}>{p.name}</MenuItem>)}
          </TextField>
          <TextField label={t('admin.tax.provider.company')} value={companyCode} onChange={(e) => setCompanyCode(e.target.value)} />
          <TextField select label={t('admin.tax.provider.environment')} value={environment} onChange={(e) => setEnvironment(e.target.value)}>
            <MenuItem value="SANDBOX">{t('admin.tax.provider.sandbox')}</MenuItem>
            <MenuItem value="PRODUCTION">{t('admin.tax.provider.production')}</MenuItem>
          </TextField>
          <TextField label={t('admin.tax.provider.timeout')} value={timeoutSeconds} onChange={(e) => setTimeoutSeconds(e.target.value)} error={!timeoutOk} slotProps={{ htmlInput: { inputMode: 'numeric' } }} />
          <TextField label={t('admin.tax.provider.attempts')} value={maxAttempts} onChange={(e) => setMaxAttempts(e.target.value)} error={!attemptsOk} helperText={t('admin.tax.provider.attemptsHelp')} slotProps={{ htmlInput: { inputMode: 'numeric' } }} />
        </Box>
        {status.externalEngineEnabled && <Alert severity="info" sx={{ mt: 2 }}>{t('admin.tax.provider.rulesNote')}</Alert>}
        {error && <Alert severity="error" role="alert" sx={{ mt: 2 }}>{error}</Alert>}
        <Button variant="contained" sx={{ mt: 2 }} onClick={() => void save()} disabled={saving || !providerCode || !timeoutOk || !attemptsOk}
          startIcon={saving ? <CircularProgress size={14} color="inherit" /> : undefined}>{t('admin.tax.provider.save')}</Button>
      </Paper>

      <Paper variant="outlined" component="section" aria-labelledby="tax-credentials-title" sx={{ p: { xs: 2, sm: 3 }, borderRadius: 3 }}>
        <Typography id="tax-credentials-title" component="h2" variant="subtitle1" sx={{ fontWeight: 700 }}>{t('admin.tax.provider.credentialsTitle')}</Typography>
        <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2 }}>{t('admin.tax.provider.credentialsBody')}</Typography>
        <Box sx={{ display: 'flex', gap: 1.5, flexWrap: 'wrap', alignItems: 'center' }}>
          {Object.entries(ext.credentials).map(([name, isSet]) => (
            <Chip key={name} variant="outlined" color={isSet ? 'success' : 'default'}
              icon={isSet ? <CheckCircle2 size={14} aria-hidden /> : <XCircle size={14} aria-hidden />}
              label={`${name}: ${t(isSet ? 'admin.tax.provider.set' : 'admin.tax.provider.notSet')}`} />
          ))}
          {ext.endpointHost && <Typography variant="body2">{t('admin.tax.provider.host')}: <b>{ext.endpointHost}</b></Typography>}
        </Box>
      </Paper>

      <Paper variant="outlined" component="section" aria-labelledby="tax-validation-title" sx={{ p: { xs: 2, sm: 3 }, borderRadius: 3 }}>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap', mb: 1 }}>
          <Typography id="tax-validation-title" component="h2" variant="subtitle1" sx={{ fontWeight: 700, flex: 1 }}>{t('admin.tax.provider.validationTitle')}</Typography>
          <Chip size="small" color={validationColor} label={t(`admin.tax.provider.status.${ext.validationStatus}`)} />
          <Button variant="outlined" onClick={() => void test()} disabled={testing || !ext.providerCode} startIcon={testing ? <CircularProgress size={14} /> : undefined}>
            {t(testing ? 'admin.tax.provider.testing' : 'admin.tax.provider.test')}
          </Button>
        </Box>
        {ext.validatedAt
          ? <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('admin.tax.provider.validated', { when: formatDateTime(ext.validatedAt, i18n.language) })}{ext.validationSummary ? ` — ${ext.validationSummary}` : ''}</Typography>
          : <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('admin.tax.provider.validationNone')}</Typography>}
        {report && <Box sx={{ mt: 1 }}><ValidationChecks report={report} /></Box>}
        {(ext.countries.length > 0 || ext.currencies.length > 0) && (
          <Box sx={{ mt: 2, display: 'grid', gap: 1.5 }}>
            {([['countries', ext.countries], ['currencies', ext.currencies], ['types', ext.transactionTypes]] as const).map(([key, list]) => (
              <Box key={key}>
                <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t(`admin.tax.provider.${key}`)}</Typography>
                <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>{list.map((v) => <Chip key={v} size="small" variant="outlined" label={v} />)}</Box>
              </Box>
            ))}
          </Box>
        )}
      </Paper>
    </Box>
  )
}
