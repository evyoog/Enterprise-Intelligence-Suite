import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, CircularProgress, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Paper, Skeleton, Snackbar, Typography, useTheme,
} from '@mui/material'
import { Plus } from 'lucide-react'
import { ApiError } from '../../api/client'
import { paymentGatewayApi, type Gateway, type GatewaySummary } from '../../api/paymentGatewayApi'
import { GatewayConfigureDialog } from './GatewayConfigureDialog'
import { GatewayDetailsDialog } from './GatewayDetailsDialog'
import { GatewayFormDialog } from './GatewayFormDialog'
import { GatewayLogo } from './GatewayLogo'
import { configurationColor, connectionColor } from './gatewayStatus'

/**
 * C98 / REQ-BIL-003: the provider-independent Payment Gateways list. Everything shown comes from the backend (the provider registry and the
 * saved configuration); nothing is hard-coded for a provider. C99: an administrator adds any gateway with the Add form and its credentials are
 * stored encrypted. A gateway with a working integration has the full set of actions; one added here has no integration, is labelled so, and can
 * only be configured, edited or removed, so nothing is ever shown as operational that is not.
 */
export function PaymentGatewayManagement() {
  const { t } = useTranslation()
  const theme = useTheme()
  const [gateways, setGateways] = useState<Gateway[] | null>(null)
  const [summary, setSummary] = useState<GatewaySummary | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState<string | null>(null)
  const [notice, setNotice] = useState<{ code: string; severity: 'success' | 'error' | 'warning'; text: string } | null>(null)
  const [configuring, setConfiguring] = useState<Gateway | null>(null)
  const [viewing, setViewing] = useState<Gateway | null>(null)
  const [adding, setAdding] = useState(false)
  const [editing, setEditing] = useState<Gateway | null>(null)
  const [removing, setRemoving] = useState<Gateway | null>(null)
  const [toast, setToast] = useState<string | null>(null)

  const load = useCallback(() => Promise.all([paymentGatewayApi.list(), paymentGatewayApi.summary()])
    .then(([list, sum]) => { setGateways(list); setSummary(sum); setError(null) })
    .catch((e) => setError(e instanceof ApiError ? e.message : t('admin.paymentGateway.loadError'))), [t])
  useEffect(() => { void load() }, [load])

  const act = async (g: Gateway, action: () => Promise<unknown>, success: string) => {
    setBusy(g.code)
    setNotice(null)
    try {
      await action()
      setNotice({ code: g.code, severity: 'success', text: success })
    } catch (e) {
      setNotice({ code: g.code, severity: 'error', text: e instanceof ApiError ? e.message : t('admin.paymentGateway.saveFailed') })
    } finally {
      setBusy(null)
      void load()
    }
  }

  const test = async (g: Gateway) => {
    setBusy(g.code)
    setNotice(null)
    try {
      const r = await paymentGatewayApi.test(g.code)
      setNotice({ code: g.code, severity: r.successful ? 'success' : 'error', text: `${t(`admin.paymentGateway.connection.${r.status}`)}${r.message ? ` — ${r.message}` : ''}` })
    } catch (e) {
      setNotice({ code: g.code, severity: 'error', text: e instanceof ApiError ? e.message : t('admin.paymentGateway.testFailed') })
    } finally {
      setBusy(null)
      void load()
    }
  }

  if (error && !gateways) {
    return <Alert severity="error" action={<Button onClick={() => void load()}>{t('admin.paymentGateway.retry')}</Button>}>{error}</Alert>
  }
  if (!gateways || !summary) return <Skeleton variant="rounded" height={320} />

  const stats: [string, number][] = [
    [t('admin.paymentGateway.stats.registered'), summary.registered],
    [t('admin.paymentGateway.stats.configured'), summary.configured],
    [t('admin.paymentGateway.stats.active'), summary.active],
    [t('admin.paymentGateway.stats.needsConfiguration'), summary.needsConfiguration],
  ]
  const methodLabel = (m: string) => t(`admin.paymentGateway.methods.${m}`, { defaultValue: m })

  const row = (g: Gateway) => {
    const working = busy === g.code
    const rowNotice = notice?.code === g.code ? notice : null
    return (
      <Paper key={g.code} component="li" variant="outlined" sx={{ listStyle: 'none', p: 2, borderRadius: 2.5 }} aria-label={g.name}>
        <Box sx={{ display: 'grid', gap: 1.5, alignItems: 'center', gridTemplateColumns: { xs: '1fr', md: 'minmax(180px, 1.2fr) minmax(160px, 1.3fr) minmax(200px, 1.6fr)' } }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, minWidth: 0 }}>
            <Box sx={{ width: 40, display: 'grid', placeItems: 'center', flexShrink: 0 }}>
              <GatewayLogo logo={g.logo} name={g.name} dark={theme.palette.mode === 'dark'} />
            </Box>
            <Box sx={{ minWidth: 0 }}>
              <Typography component="h3" sx={{ fontWeight: 700, lineHeight: 1.25 }}>{g.name}</Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                {g.environment ? t(`admin.paymentGateway.env.${g.environment}`) : t('admin.paymentGateway.envNotSet')}
              </Typography>
            </Box>
          </Box>
          <Box>
            <Typography variant="body2">{g.enabledMethods.map(methodLabel).join(', ')}</Typography>
            <Typography variant="caption" sx={{ color: 'text.secondary' }}>{g.currencies.join(', ')}</Typography>
          </Box>
          <Box sx={{ display: 'flex', gap: 0.75, flexWrap: 'wrap' }}>
            <Chip size="small" color={configurationColor(g.configurationStatus)} label={t(`admin.paymentGateway.configuration.${g.configurationStatus}`)} />
            {g.implementation === 'CONFIGURATION_ONLY' ? (
              <Chip size="small" variant="outlined" label={t('admin.paymentGateway.noIntegrationChip')} title={g.connectionMessage} />
            ) : g.configurationStatus === 'CONFIGURED' && (
              <Chip size="small" variant="outlined" color={connectionColor(g.connectionStatus)}
                label={t(`admin.paymentGateway.connection.${g.connectionStatus}`)} title={g.connectionMessage} />
            )}
            <Chip size="small" variant={g.activated ? 'filled' : 'outlined'} color={g.activated ? 'primary' : 'default'}
              label={g.activated ? t('admin.paymentGateway.active') : t('admin.paymentGateway.inactive')} />
            {g.defaultGateway && <Chip size="small" color="secondary" label={t('admin.paymentGateway.default')} />}
          </Box>
        </Box>
        {g.implementation === 'CONFIGURATION_ONLY' && (
          <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mt: 0.75 }}>{t('admin.paymentGateway.configurationOnlyLine')}</Typography>
        )}
        {g.testedAt && g.configurationStatus === 'CONFIGURED' && (
          <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mt: 0.75 }}>
            {t('admin.paymentGateway.lastTest', { date: new Date(g.testedAt).toLocaleString() })}{g.connectionMessage ? ` — ${g.connectionMessage}` : ''}
          </Typography>
        )}
        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mt: 1.5, alignItems: 'center' }}>
          {g.actions.configure && <Button size="small" variant="outlined" disabled={working} onClick={() => setConfiguring(g)} aria-label={t('admin.paymentGateway.configureNamed', { name: g.name })}>{t('admin.paymentGateway.configure')}</Button>}
          {g.actions.test && <Button size="small" variant="outlined" disabled={working} onClick={() => void test(g)} aria-label={t('admin.paymentGateway.testNamed', { name: g.name })}>{t('admin.paymentGateway.testConnection')}</Button>}
          {g.actions.activate && <Button size="small" variant="contained" disabled={working} aria-label={t('admin.paymentGateway.activateNamed', { name: g.name })}
            onClick={() => void act(g, () => paymentGatewayApi.activate(g.code), t('admin.paymentGateway.activated', { name: g.name }))}>{t('admin.paymentGateway.activate')}</Button>}
          {g.actions.setDefault && <Button size="small" variant="outlined" disabled={working} aria-label={t('admin.paymentGateway.makeDefaultNamed', { name: g.name })}
            onClick={() => void act(g, () => paymentGatewayApi.setDefault(g.code), t('admin.paymentGateway.defaultSet', { name: g.name }))}>{t('admin.paymentGateway.makeDefault')}</Button>}
          {g.actions.deactivate && <Button size="small" color="warning" disabled={working} aria-label={t('admin.paymentGateway.deactivateNamed', { name: g.name })}
            onClick={() => void act(g, () => paymentGatewayApi.deactivate(g.code), t('admin.paymentGateway.deactivated', { name: g.name }))}>{t('admin.paymentGateway.deactivate')}</Button>}
          <Button size="small" onClick={() => setViewing(g)} aria-label={t('admin.paymentGateway.viewDetailsNamed', { name: g.name })}>{t('admin.paymentGateway.viewDetails')}</Button>
          {g.actions.edit && <Button size="small" disabled={working} onClick={() => setEditing(g)} aria-label={t('admin.paymentGateway.editNamed', { name: g.name })}>{t('admin.paymentGateway.edit')}</Button>}
          {g.actions.remove && <Button size="small" color="error" disabled={working} onClick={() => setRemoving(g)} aria-label={t('admin.paymentGateway.removeNamed', { name: g.name })}>{t('admin.paymentGateway.remove')}</Button>}
          {working && <CircularProgress size={16} aria-label={t('admin.paymentGateway.working')} />}
        </Box>
        {rowNotice && <Alert severity={rowNotice.severity} role="status" sx={{ mt: 1.5 }} onClose={() => setNotice(null)}>{rowNotice.text}</Alert>}
      </Paper>
    )
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
      <Box component="dl" sx={{ m: 0, display: 'grid', gap: 1.5, gridTemplateColumns: { xs: 'repeat(2, 1fr)', md: 'repeat(4, 1fr)' } }}>
        {stats.map(([label, value]) => (
          <Paper key={label} variant="outlined" sx={{ px: 2, py: 1.25, borderRadius: 2.5 }}>
            <Typography component="dt" variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>{label}</Typography>
            <Typography component="dd" variant="h5" sx={{ m: 0, fontWeight: 700 }}>{value}</Typography>
          </Paper>
        ))}
      </Box>

      <Box component="section" aria-labelledby="gw-list-title">
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 2, flexWrap: 'wrap', mb: 1 }}>
          <Typography id="gw-list-title" component="h2" variant="subtitle1" sx={{ fontWeight: 700 }}>{t('admin.paymentGateway.configuredTitle')}</Typography>
          <Button variant="contained" startIcon={<Plus size={16} />} onClick={() => setAdding(true)}>{t('admin.paymentGateway.form.addButton')}</Button>
        </Box>
        <Box component="ul" sx={{ m: 0, p: 0, display: 'grid', gap: 1.5 }}>{gateways.map(row)}</Box>
        <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mt: 1 }}>{t('admin.paymentGateway.routingNote')}</Typography>
      </Box>

      {configuring && (
        <GatewayConfigureDialog code={configuring.code} name={configuring.name} onClose={() => setConfiguring(null)}
          onSaved={(message) => { setConfiguring(null); setToast(message); void load() }} />
      )}
      {viewing && <GatewayDetailsDialog code={viewing.code} configurationOnly={viewing.implementation === 'CONFIGURATION_ONLY'} onClose={() => setViewing(null)} onChanged={() => void load()} />}
      {adding && <GatewayFormDialog onClose={() => setAdding(false)} onSaved={(message) => { setAdding(false); setToast(message); void load() }} />}
      {editing && <GatewayFormDialog code={editing.code} onClose={() => setEditing(null)} onSaved={(message) => { setEditing(null); setToast(message); void load() }} />}
      {removing && (
        <Dialog open onClose={() => setRemoving(null)} aria-labelledby="gw-remove-title">
          <DialogTitle id="gw-remove-title">{t('admin.paymentGateway.removeTitle', { name: removing.name })}</DialogTitle>
          <DialogContent><DialogContentText>{t('admin.paymentGateway.removeBody')}</DialogContentText></DialogContent>
          <DialogActions>
            <Button onClick={() => setRemoving(null)}>{t('admin.paymentGateway.cancel')}</Button>
            <Button color="error" variant="contained" onClick={() => {
              const g = removing
              setRemoving(null)
              void act(g, () => paymentGatewayApi.remove(g.code), t('admin.paymentGateway.removed', { name: g.name }))
            }}>{t('admin.paymentGateway.remove')}</Button>
          </DialogActions>
        </Dialog>
      )}
      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)} message={toast ?? ''} />
    </Box>
  )
}
