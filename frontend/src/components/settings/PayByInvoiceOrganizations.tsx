import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, CircularProgress, InputAdornment, Paper, Switch, TextField, Typography } from '@mui/material'
import { Search } from 'lucide-react'
import { adminBillingApi, type PayByInvoiceOrganization } from '../../api/billingApi'
import { ApiError } from '../../api/client'

/**
 * C97 BI-4: pay by invoice is only for organizations the platform administrator enables. Shows the enabled organizations; a name search
 * finds others to enable. Each switch saves at once (and is audited on the server).
 */
export function PayByInvoiceOrganizations() {
  const { t } = useTranslation()
  const [enabled, setEnabled] = useState<PayByInvoiceOrganization[] | null>(null)
  const [query, setQuery] = useState('')
  const [found, setFound] = useState<PayByInvoiceOrganization[]>([])
  const [searching, setSearching] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const loadEnabled = useCallback(() => adminBillingApi.payByInvoiceOrganizations()
    .then(setEnabled).catch((e) => setError(e instanceof ApiError ? e.message : t('admin.billingSettings.loadError'))), [t])
  useEffect(() => { void loadEnabled() }, [loadEnabled])

  useEffect(() => {
    const term = query.trim()
    if (term.length < 2) return undefined
    const timer = setTimeout(() => {
      setSearching(true)
      adminBillingApi.payByInvoiceOrganizations(term)
        .then(setFound)
        .catch((e) => setError(e instanceof ApiError ? e.message : t('admin.billingSettings.loadError')))
        .finally(() => setSearching(false))
    }, 300)
    return () => clearTimeout(timer)
  }, [query, t])

  const toggle = async (org: PayByInvoiceOrganization, on: boolean) => {
    setError(null)
    try {
      const saved = await adminBillingApi.setPayByInvoice(org.id, on)
      setFound((list) => list.map((o) => (o.id === saved.id ? saved : o)))
      await loadEnabled()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('checkout.genericError'))
    }
  }

  const row = (org: PayByInvoiceOrganization) => (
    <Paper key={org.id} variant="outlined" sx={{ display: 'flex', alignItems: 'center', gap: 1.5, px: 1.5, py: 1, borderRadius: 2 }}>
      <Box sx={{ flex: 1, minWidth: 0 }}>
        <Typography sx={{ fontWeight: 600 }} noWrap>{org.name}</Typography>
        <Typography variant="caption" sx={{ color: 'text.secondary' }}>{org.code}</Typography>
      </Box>
      <Switch checked={org.enabled} onChange={(e) => void toggle(org, e.target.checked)}
        slotProps={{ input: { 'aria-label': t('admin.billingSettings.payByInvoiceOrgs.switchLabel', { name: org.name }) } }} />
    </Paper>
  )
  const shownFound = query.trim().length >= 2 ? found : []

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
      <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('admin.billingSettings.payByInvoiceOrgs.body')}</Typography>
      {error && <Alert severity="error" onClose={() => setError(null)}>{error}</Alert>}
      <Typography variant="subtitle2" component="h3">{t('admin.billingSettings.payByInvoiceOrgs.enabledTitle')}</Typography>
      {enabled === null ? <CircularProgress size={20} aria-label={t('admin.billingSettings.loading')} />
        : enabled.length === 0 ? <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('admin.billingSettings.payByInvoiceOrgs.none')}</Typography>
        : <Box sx={{ display: 'grid', gap: 1 }}>{enabled.map(row)}</Box>}
      <TextField label={t('admin.billingSettings.payByInvoiceOrgs.search')} value={query} onChange={(e) => setQuery(e.target.value)}
        helperText={t('admin.billingSettings.payByInvoiceOrgs.searchHelp')} size="small"
        slotProps={{ input: { startAdornment: <InputAdornment position="start"><Search size={16} aria-hidden /></InputAdornment> } }} />
      {searching && <CircularProgress size={18} aria-label={t('admin.billingSettings.loading')} />}
      {!searching && query.trim().length >= 2 && shownFound.length === 0 && (
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('admin.billingSettings.payByInvoiceOrgs.noMatch')}</Typography>
      )}
      {shownFound.length > 0 && <Box sx={{ display: 'grid', gap: 1 }}>{shownFound.map(row)}</Box>}
    </Box>
  )
}
