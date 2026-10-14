import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, MenuItem, Paper, TextField, Typography } from '@mui/material'
import { ShieldCheck } from 'lucide-react'
import { ApiError } from '../../api/client'
import { entitlementsApi, type Entitlement } from '../../api/registrationApi'
import { PageHeader } from '../../components/layout/PageHeader'
import { EntitlementCard } from '../../components/subscriptions/EntitlementsSection'

type Kind = 'customer' | 'organization'

/** "/admin/entitlements" — VIEW_ENTITLEMENTS (C97 EN-3): a read-only support lookup of what a customer or an organization is entitled to now.
 * Every lookup is recorded in the audit log. Nothing here grants or revokes anything (EN-1). */
export function AdminEntitlementsPage() {
  const { t } = useTranslation()
  const [kind, setKind] = useState<Kind>('customer')
  const [id, setId] = useState('')
  const [items, setItems] = useState<Entitlement[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const valid = /^\d{1,12}$/.test(id)

  const lookUp = async () => {
    setBusy(true); setError(null); setItems(null)
    try {
      setItems(kind === 'customer' ? await entitlementsApi.adminCustomer(Number(id)) : await entitlementsApi.adminOrganization(Number(id)))
    } catch (e) {
      setError(e instanceof ApiError && e.status === 404 ? t('entitlements.admin.notFound') : e instanceof ApiError ? e.message : t('entitlements.loadError'))
    } finally { setBusy(false) }
  }

  return (
    <>
      <PageHeader icon={ShieldCheck} accent="emerald" title={t('entitlements.admin.title')} subtitle={t('entitlements.admin.subtitle')} />
      <Paper variant="outlined" component="form" onSubmit={(e) => { e.preventDefault(); if (valid) void lookUp() }}
        sx={{ p: 2.5, borderRadius: 3, display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'flex-start' }}>
        <TextField select size="small" label={t('entitlements.admin.kind')} value={kind} onChange={(e) => setKind(e.target.value as Kind)} sx={{ minWidth: 180 }}>
          <MenuItem value="customer">{t('entitlements.admin.customer')}</MenuItem>
          <MenuItem value="organization">{t('entitlements.admin.organization')}</MenuItem>
        </TextField>
        <TextField size="small" label={t('entitlements.admin.id')} value={id} onChange={(e) => setId(e.target.value.trim())}
          error={id !== '' && !valid} helperText={id !== '' && !valid ? t('entitlements.admin.idInvalid') : t('entitlements.admin.idHelp')}
          slotProps={{ htmlInput: { inputMode: 'numeric', maxLength: 12 } }} />
        <Button type="submit" variant="contained" disabled={!valid || busy}>{t('entitlements.admin.lookUp')}</Button>
      </Paper>
      <Box sx={{ mt: 3, display: 'grid', gap: 1.5 }} aria-live="polite">
        {error && <Alert severity="error">{error}</Alert>}
        {items !== null && items.length === 0 && <Typography sx={{ color: 'text.secondary' }}>{t('entitlements.admin.none')}</Typography>}
        {items?.map((e) => <EntitlementCard key={e.subscriptionId} entitlement={e} />)}
      </Box>
    </>
  )
}
