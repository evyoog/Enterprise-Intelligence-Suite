import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Chip, CircularProgress, Paper, Typography } from '@mui/material'
import { Copy } from 'lucide-react'
import { adminBillingApi, type GatewayStatus } from '../../api/billingApi'
import { ApiError } from '../../api/client'
import { PageHeader } from '../../components/layout/PageHeader'

function Row({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'space-between', py: 1, borderBottom: '1px solid', borderColor: 'divider' }}>
      <Typography variant="body2" sx={{ color: 'text.secondary' }}>{label}</Typography>
      <Typography variant="body2" sx={{ fontWeight: 600 }}>{value}</Typography>
    </Box>
  )
}

/** "/admin/billing/payment-gateway" — MANAGE_BILLING, read-only
 * (REQ-BIL-001.13/.14, BR-SEC-001). Never shows or edits a secret. */
export function AdminPaymentGatewayPage() {
  const { t } = useTranslation()
  const [status, setStatus] = useState<GatewayStatus | null>(null)
  const [testResult, setTestResult] = useState<string | null>(null)
  const [testError, setTestError] = useState<string | null>(null)
  const [testing, setTesting] = useState(false)

  useEffect(() => { adminBillingApi.gatewayStatus().then(setStatus).catch(() => {}) }, [])

  if (!status) return <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}><CircularProgress size={28} /></Box>

  const testConnection = () => {
    setTesting(true)
    setTestError(null)
    setTestResult(null)
    adminBillingApi.testGateway()
      .then(() => setTestResult(t('admin.paymentGateway.connected')))
      .catch((e) => setTestError(e instanceof ApiError ? e.message : t('admin.paymentGateway.testFailed')))
      .finally(() => setTesting(false))
  }

  return (
    <Box sx={{ maxWidth: 640 }}>
      <PageHeader title={t('admin.paymentGateway.title')} subtitle={t('admin.paymentGateway.subtitle')} />
      {!status.configured && (
        <Alert severity="info" sx={{ mb: 2 }}>{t('admin.paymentGateway.notConfiguredGuidance')}</Alert>
      )}
      {testResult && <Alert severity="success" sx={{ mb: 2 }}>{testResult}</Alert>}
      {testError && <Alert severity="error" sx={{ mb: 2 }}>{testError}</Alert>}
      <Paper variant="outlined" sx={{ p: 2.5 }}>
        <Row label={t('admin.paymentGateway.provider')} value={status.provider} />
        <Row label={t('admin.paymentGateway.status')} value={
          <Chip size="small" label={status.configured ? t('admin.paymentGateway.configured') : t('admin.paymentGateway.notConfigured')}
            color={status.configured ? 'success' : 'default'} />
        } />
        <Row label={t('admin.paymentGateway.mode')} value={status.liveMode ? t('admin.paymentGateway.live') : t('admin.paymentGateway.test')} />
        <Row label={t('admin.paymentGateway.keyId')} value={status.maskedKeyId ?? t('admin.paymentGateway.notSet')} />
        <Row label={t('admin.paymentGateway.keySecret')} value={status.keySecretSet ? t('admin.paymentGateway.set') : t('admin.paymentGateway.notSet')} />
        <Row label={t('admin.paymentGateway.webhookSecret')} value={status.webhookSecretSet ? t('admin.paymentGateway.set') : t('admin.paymentGateway.notSet')} />
        <Row label={t('admin.paymentGateway.webhookUrl')} value={
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>{status.webhookUrl}</Typography>
            <Button size="small" onClick={() => navigator.clipboard?.writeText(status.webhookUrl)} startIcon={<Copy size={14} />}>
              {t('admin.paymentGateway.copy')}
            </Button>
          </Box>
        } />
        <Row label={t('admin.paymentGateway.lastWebhook')} value={
          status.lastWebhookReceivedAt
            ? `${status.lastWebhookEventType} — ${new Date(status.lastWebhookReceivedAt).toLocaleString()}`
            : t('admin.paymentGateway.never')
        } />
      </Paper>
      <Button variant="contained" sx={{ mt: 2 }} disabled={!status.configured || testing} onClick={testConnection}>
        {t('admin.paymentGateway.testConnection')}
      </Button>
    </Box>
  )
}
