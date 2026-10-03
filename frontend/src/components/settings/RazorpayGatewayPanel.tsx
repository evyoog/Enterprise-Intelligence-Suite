import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Chip, CircularProgress, IconButton, Paper, Skeleton, Tooltip, Typography, useTheme } from '@mui/material'
import { alpha } from '@mui/material/styles'
import { CheckCircle2, Copy, KeyRound, Link2, PlugZap, ShieldCheck, Webhook, XCircle } from 'lucide-react'
import { adminBillingApi, type GatewayStatus } from '../../api/billingApi'
import { ApiError } from '../../api/client'
import { accentColor } from '../../theming/accents'
import { SettingsSection, StatusTile } from './SettingsSection'

/** The variable names in config/secrets.env.example (BR-SEC-001). */
const SECRET_VARIABLES = ['RAZORPAY_KEY_ID', 'RAZORPAY_KEY_SECRET', 'RAZORPAY_WEBHOOK_SECRET']

/**
 * C60: the Razorpay credentials view. BR-SEC-001 — keys are kept only in the
 * common secrets file (`config/secrets.env`), never entered, stored or shown
 * here: this panel shows whether each one is set (the key ID masked), the
 * webhook to register, a connection test, and the steps to add or rotate
 * the keys in the file.
 */
export function RazorpayGatewayPanel({ onStatus }: { onStatus?: (status: GatewayStatus) => void }) {
  const { t } = useTranslation()
  const theme = useTheme()
  const [status, setStatus] = useState<GatewayStatus | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [testing, setTesting] = useState(false)
  const [testResult, setTestResult] = useState<{ ok: boolean; message: string } | null>(null)
  const [copied, setCopied] = useState<string | null>(null)

  const load = () => adminBillingApi.gatewayStatus()
    .then((s) => { setStatus(s); onStatus?.(s) })
    .catch((e) => setLoadError(e instanceof ApiError ? e.message : t('admin.billingSettings.loadError')))
  // eslint-disable-next-line react-hooks/exhaustive-deps -- load once on mount
  useEffect(() => { void load() }, [])

  const copy = (key: string, value: string) => {
    void navigator.clipboard?.writeText(value)
    setCopied(key)
  }

  const testConnection = () => {
    setTesting(true)
    setTestResult(null)
    adminBillingApi.testGateway()
      .then(() => setTestResult({ ok: true, message: t('admin.paymentGateway.connected') }))
      .catch((e) => setTestResult({ ok: false, message: e instanceof ApiError ? e.message : t('admin.paymentGateway.testFailed') }))
      .finally(() => setTesting(false))
  }

  if (loadError) {
    return <Alert severity="error" action={<Button onClick={() => { setLoadError(null); void load() }}>{t('admin.billingSettings.retry')}</Button>}>{loadError}</Alert>
  }
  if (!status) return <Skeleton variant="rounded" height={360} />

  const credential = (label: string, set: boolean, value?: string) => (
    <StatusTile icon={set ? CheckCircle2 : XCircle} accent={set ? 'emerald' : 'rose'} label={label}
      value={value ?? (set ? t('admin.paymentGateway.set') : t('admin.paymentGateway.notSet'))}
      status={set ? t('admin.billingSettings.gateway.present') : t('admin.billingSettings.gateway.missing')}
      statusColor={set ? 'success' : 'error'} />
  )
  const amber = accentColor('amber', theme.palette.mode)

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
      <SettingsSection id="gateway-status" icon={PlugZap} accent="amber" title={t('admin.billingSettings.gateway.statusTitle')}
        description={t('admin.billingSettings.gateway.statusBody')}
        action={(
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', justifyContent: 'flex-end' }}>
            <Chip label={status.configured ? t('admin.paymentGateway.configured') : t('admin.paymentGateway.notConfigured')}
              color={status.configured ? 'success' : 'default'} />
            <Chip variant="outlined" label={status.liveMode ? t('admin.paymentGateway.live') : t('admin.paymentGateway.test')}
              color={status.liveMode ? 'warning' : 'info'} />
          </Box>
        )}>
        <Box sx={{ display: 'grid', gap: 1.5, gridTemplateColumns: { xs: '1fr', md: 'repeat(3, minmax(0, 1fr))' } }}>
          {credential(t('admin.paymentGateway.keyId'), Boolean(status.maskedKeyId), status.maskedKeyId)}
          {credential(t('admin.paymentGateway.keySecret'), status.keySecretSet)}
          {credential(t('admin.paymentGateway.webhookSecret'), status.webhookSecretSet)}
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mt: 2.5, flexWrap: 'wrap' }}>
          <Button variant="contained" disabled={!status.configured || testing} onClick={testConnection}
            startIcon={testing ? <CircularProgress size={16} color="inherit" /> : <PlugZap size={16} aria-hidden />}>
            {t('admin.paymentGateway.testConnection')}
          </Button>
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>
            {t('admin.billingSettings.gateway.provider', { provider: status.provider })}
          </Typography>
        </Box>
        {testResult && <Alert severity={testResult.ok ? 'success' : 'error'} sx={{ mt: 2 }} role="status">{testResult.message}</Alert>}
      </SettingsSection>

      <SettingsSection id="gateway-webhook" icon={Webhook} accent="cyan" title={t('admin.billingSettings.gateway.webhookTitle')}
        description={t('admin.billingSettings.gateway.webhookBody')}>
        <Paper variant="outlined" sx={{ display: 'flex', alignItems: 'center', gap: 1, px: 1.5, py: 1, borderRadius: 2, bgcolor: 'action.hover' }}>
          <Link2 size={16} aria-hidden />
          <Typography variant="body2" sx={{ fontFamily: 'monospace', flex: 1, minWidth: 0, wordBreak: 'break-all' }}>{status.webhookUrl}</Typography>
          <Tooltip title={copied === 'webhook' ? t('checkout.bank.copied') : t('checkout.bank.copy')}>
            <IconButton size="small" aria-label={t('admin.billingSettings.gateway.copyWebhook')} onClick={() => copy('webhook', status.webhookUrl)}><Copy size={15} /></IconButton>
          </Tooltip>
        </Paper>
        <Typography variant="body2" sx={{ color: 'text.secondary', mt: 1.5 }}>
          {t('admin.paymentGateway.lastWebhook')}: {status.lastWebhookReceivedAt
            ? `${status.lastWebhookEventType} — ${new Date(status.lastWebhookReceivedAt).toLocaleString()}`
            : t('admin.paymentGateway.never')}
        </Typography>
      </SettingsSection>

      <SettingsSection id="gateway-setup" icon={KeyRound} accent="violet" title={t('admin.billingSettings.gateway.setupTitle')}
        description={t('admin.billingSettings.gateway.setupBody')}>
        <Box component="ol" sx={{ m: 0, pl: 0, listStyle: 'none', display: 'flex', flexDirection: 'column', gap: 1.5 }}>
          {[1, 2, 3, 4, 5].map((n) => (
            <Box component="li" key={n} sx={{ display: 'flex', gap: 1.5, alignItems: 'flex-start' }}>
              <Box aria-hidden sx={{ width: 26, height: 26, borderRadius: '50%', flexShrink: 0, display: 'grid', placeItems: 'center', fontSize: 13, fontWeight: 700, color: theme.palette.mode === 'dark' ? '#0b1220' : '#fff', bgcolor: amber }}>{n}</Box>
              <Box sx={{ flex: 1, minWidth: 0 }}>
                <Typography variant="body2" sx={{ fontWeight: 600 }}>{t(`admin.billingSettings.gateway.step${n}.title`)}</Typography>
                <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t(`admin.billingSettings.gateway.step${n}.body`)}</Typography>
                {n === 2 && (
                  <Box sx={{ mt: 1, display: 'flex', flexDirection: 'column', gap: 0.75 }}>
                    {SECRET_VARIABLES.map((name) => (
                      <Paper key={name} variant="outlined" sx={{ display: 'flex', alignItems: 'center', gap: 1, px: 1.25, py: 0.5, borderRadius: 1.5, bgcolor: alpha(amber, 0.06) }}>
                        <Typography variant="body2" sx={{ fontFamily: 'monospace', flex: 1 }}>{name}=</Typography>
                        <Tooltip title={copied === name ? t('checkout.bank.copied') : t('checkout.bank.copy')}>
                          <IconButton size="small" aria-label={t('admin.billingSettings.gateway.copyVariable', { name })} onClick={() => copy(name, `${name}=`)}><Copy size={14} /></IconButton>
                        </Tooltip>
                      </Paper>
                    ))}
                  </Box>
                )}
              </Box>
            </Box>
          ))}
        </Box>
        <Alert icon={<ShieldCheck size={18} />} severity="info" sx={{ mt: 2.5 }}>{t('admin.billingSettings.gateway.securityNote')}</Alert>
      </SettingsSection>
    </Box>
  )
}
