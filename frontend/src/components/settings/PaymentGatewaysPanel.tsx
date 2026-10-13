import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Chip, CircularProgress, Paper, Skeleton, Typography } from '@mui/material'
import { PlugZap } from 'lucide-react'
import { adminBillingApi, type GatewayStatus, type PaymentGatewayInfo } from '../../api/billingApi'
import { ApiError } from '../../api/client'
import { RazorpayGatewayPanel } from './RazorpayGatewayPanel'
import { SettingsSection } from './SettingsSection'

/**
 * C97 BI-8: the payment screens are gateway-neutral. This panel lists every gateway the platform has (each one is a backend
 * `PaymentGateway`), shows which is active at checkout, and tests a gateway's connection. A gateway's credentials stay in the backend
 * environment (BR-SEC-001) and are never shown. The detailed credentials/webhook view of Razorpay, the first gateway, is below the list.
 */
export function PaymentGatewaysPanel({ onStatus }: { onStatus?: (status: GatewayStatus) => void }) {
  const { t } = useTranslation()
  const [gateways, setGateways] = useState<PaymentGatewayInfo[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState<string | null>(null)
  const [result, setResult] = useState<{ code: string; ok: boolean; message: string } | null>(null)

  useEffect(() => {
    adminBillingApi.gateways().then(setGateways)
      .catch((e) => setError(e instanceof ApiError ? e.message : t('admin.billingSettings.loadError')))
  }, [t])

  const makeActive = async (code: string) => {
    setBusy(code); setError(null)
    try { setGateways(await adminBillingApi.setActiveGateway(code)) }
    catch (e) { setError(e instanceof ApiError ? e.message : t('checkout.genericError')) }
    finally { setBusy(null) }
  }
  const test = async (g: PaymentGatewayInfo) => {
    setBusy(g.code); setResult(null)
    try {
      await adminBillingApi.testGatewayByCode(g.code)
      setResult({ code: g.code, ok: true, message: t('admin.paymentGateway.connected') })
    } catch (e) {
      setResult({ code: g.code, ok: false, message: e instanceof ApiError ? e.message : t('admin.paymentGateway.testFailed') })
    } finally { setBusy(null) }
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
      <SettingsSection id="gateway-list" icon={PlugZap} accent="amber" title={t('admin.gateways.title')} description={t('admin.gateways.body')}>
        {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}
        {gateways === null && !error ? <Skeleton variant="rounded" height={96} /> : (
          <Box sx={{ display: 'grid', gap: 1.5 }}>
            {(gateways ?? []).map((g) => (
              <Paper key={g.code} variant="outlined" sx={{ p: 2, borderRadius: 2.5, display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap' }}>
                <Box sx={{ flex: 1, minWidth: 220 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                    <Typography component="h3" sx={{ fontWeight: 700 }}>{g.name}</Typography>
                    {g.active && <Chip size="small" color="primary" label={t('admin.gateways.active')} />}
                    <Chip size="small" variant="outlined" color={g.configured ? 'success' : 'default'}
                      label={g.configured ? t('admin.paymentGateway.configured') : t('admin.paymentGateway.notConfigured')} />
                  </Box>
                  <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.5 }}>
                    {t('admin.gateways.methods')}: {g.methods.map((m) => t(`checkout.methods.${m}`, { defaultValue: m })).join(', ')}
                  </Typography>
                </Box>
                <Button variant="outlined" disabled={!g.configured || busy === g.code} onClick={() => void test(g)}
                  startIcon={busy === g.code ? <CircularProgress size={14} color="inherit" /> : undefined}
                  aria-label={t('admin.gateways.testNamed', { name: g.name })}>{t('admin.paymentGateway.testConnection')}</Button>
                <Button variant="contained" disabled={g.active || !g.configured || busy === g.code} onClick={() => void makeActive(g.code)}
                  aria-label={t('admin.gateways.makeActiveNamed', { name: g.name })}>{t('admin.gateways.makeActive')}</Button>
                {result?.code === g.code && <Alert severity={result.ok ? 'success' : 'error'} role="status" sx={{ width: '100%' }}>{result.message}</Alert>}
              </Paper>
            ))}
          </Box>
        )}
        <Alert severity="info" sx={{ mt: 2 }}>{t('admin.gateways.addNote')}</Alert>
      </SettingsSection>
      <RazorpayGatewayPanel onStatus={onStatus} />
    </Box>
  )
}
