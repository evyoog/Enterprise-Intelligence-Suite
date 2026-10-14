import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, IconButton, Paper, Skeleton, Switch, Tab, Tabs,
  Tooltip, Typography,
} from '@mui/material'
import { CheckCircle2, Circle, Copy } from 'lucide-react'
import { ApiError } from '../../api/client'
import { paymentGatewayApi, type GatewayDetail, type GatewayField } from '../../api/paymentGatewayApi'
import { GatewayLogo } from './GatewayLogo'

type TabKey = 'overview' | 'webhook' | 'activity'

/** C98: everything about one gateway that does not belong in the list: capabilities, payment methods, evidence that each capability has worked,
 * the webhook set-up, recent webhook events and the administrators' change history. Read-only apart from the payment-method switches. */
export function GatewayDetailsDialog({ code, planned, onClose, onChanged }: { code: string; planned: boolean; onClose: () => void; onChanged: () => void }) {
  const { t } = useTranslation()
  const [detail, setDetail] = useState<GatewayDetail | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [tab, setTab] = useState<TabKey>('overview')
  const [copied, setCopied] = useState(false)

  const load = useCallback(() => paymentGatewayApi.detail(code).then(setDetail)
    .catch((e) => setError(e instanceof ApiError ? e.message : t('admin.paymentGateway.loadError'))), [code, t])
  useEffect(() => { void load() }, [load])

  const g = detail?.gateway
  const methodLabel = (m: string) => t(`admin.paymentGateway.methods.${m}`, { defaultValue: m })

  const toggleMethod = async (method: string, on: boolean) => {
    if (!g) return
    const next = on ? [...g.enabledMethods, method] : g.enabledMethods.filter((m) => m !== method)
    setError(null)
    try {
      setDetail(await paymentGatewayApi.setMethods(code, next))
      onChanged()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('admin.paymentGateway.saveFailed'))
    }
  }

  const copy = () => {
    if (!detail) return
    void navigator.clipboard?.writeText(detail.webhook.url)
    setCopied(true)
  }

  const fieldLine = (f: GatewayField) => (
    <Box component="li" key={f.key} sx={{ display: 'flex', gap: 1, alignItems: 'center', py: 0.25 }}>
      <Typography variant="body2" sx={{ flex: 1 }}>{f.label}{f.required ? ' *' : ''}</Typography>
      {planned ? <Typography variant="caption" sx={{ color: 'text.secondary' }}>{f.secret ? t('admin.paymentGateway.secretField') : t('admin.paymentGateway.publicField')}</Typography>
        : <Chip size="small" variant="outlined" color={f.configured ? 'success' : 'default'}
          label={f.configured ? t('admin.paymentGateway.fieldSet', { source: t(`admin.paymentGateway.source.${f.source}`) }) : t('admin.paymentGateway.fieldNotSet')} />}
    </Box>
  )

  const evidence = detail ? [
    ['authenticated', detail.evidence.authenticated], ['webhookReceived', detail.evidence.webhookReceived],
    ['paymentCaptured', detail.evidence.paymentCaptured], ['refundMade', detail.evidence.refundMade],
  ] as const : []

  return (
    <Dialog open onClose={onClose} fullWidth maxWidth="md" aria-labelledby="gw-details-title">
      <DialogTitle id="gw-details-title" sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
        {g && <GatewayLogo logo={g.logo} name={g.name} />}
        {g?.name ?? code}
      </DialogTitle>
      <DialogContent>
        {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}
        {!detail && !error && <Skeleton variant="rounded" height={260} />}
        {detail && g && (
          <>
            {planned && <Alert severity="info" sx={{ mb: 2 }}>{t('admin.paymentGateway.plannedNote', { name: g.name })}</Alert>}
            <Tabs value={tab} onChange={(_, v) => setTab(v)} aria-label={t('admin.paymentGateway.detailTabs')} sx={{ mb: 2, borderBottom: 1, borderColor: 'divider' }}>
              <Tab value="overview" label={t('admin.paymentGateway.tabs.overview')} />
              {!planned && <Tab value="webhook" label={t('admin.paymentGateway.tabs.webhook')} />}
              {!planned && <Tab value="activity" label={t('admin.paymentGateway.tabs.activity')} />}
            </Tabs>

            {tab === 'overview' && (
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
                <Typography variant="body2">{g.description}</Typography>
                <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' } }}>
                  <Box>
                    <Typography variant="overline" sx={{ color: 'text.secondary' }}>{t('admin.paymentGateway.regions')}</Typography>
                    <Typography variant="body2">{g.countries.join(', ')} · {g.currencies.join(', ')}</Typography>
                  </Box>
                  <Box>
                    <Typography variant="overline" sx={{ color: 'text.secondary' }}>{t('admin.paymentGateway.capabilities')}</Typography>
                    <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                      {detail.environments.map((e) => <Chip key={e} size="small" variant="outlined" label={t(`admin.paymentGateway.env.${e}`, { defaultValue: e })} />)}
                      {detail.webhookSupport && <Chip size="small" variant="outlined" label={t('admin.paymentGateway.cap.webhooks')} />}
                      {detail.refundSupport && <Chip size="small" variant="outlined" label={t('admin.paymentGateway.cap.refunds')} />}
                      {detail.recurringSupport && <Chip size="small" variant="outlined" label={t('admin.paymentGateway.cap.recurring')} />}
                    </Box>
                  </Box>
                </Box>
                <Box>
                  <Typography variant="overline" sx={{ color: 'text.secondary' }}>{t('admin.paymentGateway.paymentMethods')}</Typography>
                  <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                    {g.supportedMethods.map((m) => planned
                      ? <Chip key={m} size="small" label={methodLabel(m)} />
                      : (
                        <FormControlLabel key={m} label={methodLabel(m)}
                          control={<Switch size="small" checked={g.enabledMethods.includes(m)}
                            disabled={g.enabledMethods.length === 1 && g.enabledMethods.includes(m)}
                            onChange={(e) => void toggleMethod(m, e.target.checked)} />} />
                      ))}
                  </Box>
                  {!planned && <Typography variant="caption" sx={{ color: 'text.secondary' }}>{t('admin.paymentGateway.methodsHelp')}</Typography>}
                </Box>
                <Box>
                  <Typography variant="overline" sx={{ color: 'text.secondary' }}>{t('admin.paymentGateway.fields')}</Typography>
                  <Box component="ul" sx={{ listStyle: 'none', p: 0, m: 0 }}>{detail.fields.map(fieldLine)}</Box>
                </Box>
                {!planned && (
                  <Box>
                    <Typography variant="overline" sx={{ color: 'text.secondary' }}>{t('admin.paymentGateway.evidence')}</Typography>
                    <Typography variant="caption" sx={{ display: 'block', color: 'text.secondary', mb: 0.5 }}>{t('admin.paymentGateway.evidenceHelp')}</Typography>
                    <Box component="ul" sx={{ listStyle: 'none', p: 0, m: 0, display: 'grid', gap: 0.5, gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' } }}>
                      {evidence.map(([key, ok]) => (
                        <Box component="li" key={key} sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          {ok ? <CheckCircle2 size={16} color="var(--mui-palette-success-main, #2e7d32)" aria-hidden /> : <Circle size={16} aria-hidden />}
                          <Typography variant="body2">{t(`admin.paymentGateway.evidenceItems.${key}`)}: <b>{ok ? t('admin.paymentGateway.seen') : t('admin.paymentGateway.notSeen')}</b></Typography>
                        </Box>
                      ))}
                    </Box>
                  </Box>
                )}
              </Box>
            )}

            {tab === 'webhook' && (
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                {!detail.webhook.reachableFromInternet && <Alert severity="warning">{t('admin.paymentGateway.localUrl')}</Alert>}
                <Box>
                  <Typography variant="overline" sx={{ color: 'text.secondary' }}>{t('admin.paymentGateway.endpoint')}</Typography>
                  <Paper variant="outlined" sx={{ display: 'flex', alignItems: 'center', gap: 1, px: 1.5, py: 0.75, borderRadius: 2, bgcolor: 'action.hover' }}>
                    <Typography variant="body2" sx={{ fontFamily: 'monospace', flex: 1, minWidth: 0, wordBreak: 'break-all' }}>{detail.webhook.url}</Typography>
                    <Tooltip title={copied ? t('admin.paymentGateway.copied') : t('admin.paymentGateway.copy')}>
                      <IconButton size="small" onClick={copy} aria-label={t('admin.paymentGateway.copyEndpoint')}><Copy size={15} /></IconButton>
                    </Tooltip>
                  </Paper>
                </Box>
                <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                  <Chip size="small" color={detail.webhook.secretConfigured ? 'success' : 'default'}
                    label={detail.webhook.secretConfigured ? t('admin.paymentGateway.hookSecretSet') : t('admin.paymentGateway.hookSecretMissing')} />
                  <Chip size="small" color={detail.webhook.signatureStatus === 'VERIFIED' ? 'success' : 'default'}
                    label={t(`admin.paymentGateway.signature.${detail.webhook.signatureStatus}`)} />
                  {detail.webhook.failureCount > 0 && <Chip size="small" color="error" label={t('admin.paymentGateway.hookFailures', { count: detail.webhook.failureCount })} />}
                </Box>
                <Typography variant="body2">
                  {t('admin.paymentGateway.lastEvent')}: {detail.webhook.lastEventAt
                    ? `${detail.webhook.lastEventType} · ${new Date(detail.webhook.lastEventAt).toLocaleString()}` : t('admin.paymentGateway.never')}
                </Typography>
                {detail.webhook.lastFailureAt && (
                  <Alert severity="warning">{t('admin.paymentGateway.lastFailure', { date: new Date(detail.webhook.lastFailureAt).toLocaleString() })}: {detail.webhook.lastFailureReason}</Alert>
                )}
                {detail.webhook.events.length > 0 && (
                  <Typography variant="body2">{t('admin.paymentGateway.subscribeTo')}: {detail.webhook.events.map((e) => <Chip key={e} size="small" variant="outlined" label={e} sx={{ mr: 0.5 }} />)}</Typography>
                )}
                {detail.setupSteps.length > 0 && (
                  <Box>
                    <Typography variant="overline" sx={{ color: 'text.secondary' }}>{t('admin.paymentGateway.setupSteps')}</Typography>
                    <Box component="ol" sx={{ m: 0, pl: 3 }}>{detail.setupSteps.map((step) => <Typography key={step} component="li" variant="body2" sx={{ py: 0.25 }}>{step}</Typography>)}</Box>
                  </Box>
                )}
              </Box>
            )}

            {tab === 'activity' && (
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
                <Box>
                  <Typography variant="overline" sx={{ color: 'text.secondary' }}>{t('admin.paymentGateway.recentEvents')}</Typography>
                  {detail.recentEvents.length === 0
                    ? <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('admin.paymentGateway.noEvents')}</Typography>
                    : detail.recentEvents.slice(0, 8).map((e, i) => (
                      <Typography key={`${e.receivedAt}-${i}`} variant="body2">{new Date(e.receivedAt).toLocaleString()} · {e.eventType}{e.paymentId ? ` · #${e.paymentId}` : ''}</Typography>
                    ))}
                </Box>
                <Box>
                  <Typography variant="overline" sx={{ color: 'text.secondary' }}>{t('admin.paymentGateway.history')}</Typography>
                  {detail.history.length === 0
                    ? <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('admin.paymentGateway.noHistory')}</Typography>
                    : detail.history.map((h, i) => (
                      <Typography key={`${h.at}-${i}`} variant="body2">{new Date(h.at).toLocaleString()} · {h.detail ?? h.action}{h.actor ? ` · ${h.actor}` : ''}</Typography>
                    ))}
                  <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mt: 0.5 }}>
                    {t('admin.paymentGateway.configVersion', { version: detail.configVersion })}
                    {detail.credentialsUpdatedAt ? ` · ${t('admin.paymentGateway.credentialsUpdated', { date: new Date(detail.credentialsUpdatedAt).toLocaleString() })}` : ''}
                  </Typography>
                </Box>
              </Box>
            )}
          </>
        )}
      </DialogContent>
      <DialogActions><Button onClick={onClose}>{t('admin.paymentGateway.close')}</Button></DialogActions>
    </Dialog>
  )
}
