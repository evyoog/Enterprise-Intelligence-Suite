import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Chip, Paper, Skeleton, Typography } from '@mui/material'
import { ShieldCheck } from 'lucide-react'
import { ApiError } from '../../api/client'
import { entitlementsApi, type Entitlement } from '../../api/registrationApi'
import { useLocalePreference } from '../../theming/LocalePreferenceProvider'
import { SettingsSection } from '../settings/SettingsSection'

/** One entitlement: the product, the plan, when the term ends, the features the plan includes and its limit. Read-only (C97 EN-1). */
export function EntitlementCard({ entitlement }: { entitlement: Entitlement }) {
  const { t } = useTranslation()
  const { formatDate } = useLocalePreference()
  return (
    <Paper variant="outlined" sx={{ p: 2, borderRadius: 2.5 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
        <Typography component="h3" sx={{ fontWeight: 700 }}>{entitlement.productName}</Typography>
        <Chip size="small" variant="outlined" label={t(`entitlements.owner.${entitlement.ownerType}`)} />
        {entitlement.planName && <Chip size="small" color="primary" variant="outlined" label={entitlement.planName} />}
      </Box>
      <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.5 }}>
        {entitlement.expiresAt ? t('entitlements.until', { date: formatDate(entitlement.expiresAt) }) : t('entitlements.noEnd')}
        {' · '}
        {entitlement.usageLimit != null ? t('entitlements.limit', { limit: entitlement.usageLimit.toLocaleString() }) : t('entitlements.noLimit')}
      </Typography>
      {entitlement.includedFeatures.length > 0 && (
        <Box sx={{ display: 'flex', gap: 0.75, flexWrap: 'wrap', mt: 1.25 }} role="list" aria-label={t('entitlements.features')}>
          {entitlement.includedFeatures.map((f) => <Chip key={f} role="listitem" size="small" label={f} />)}
        </Box>
      )}
    </Paper>
  )
}

/** C97 EN-1: what the signed-in customer's active subscriptions (and, for a member, the products they have access to) include. */
export function EntitlementsSection() {
  const { t } = useTranslation()
  const [items, setItems] = useState<Entitlement[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    entitlementsApi.mine().then(setItems)
      .catch((e) => setError(e instanceof ApiError ? e.message : t('entitlements.loadError')))
  }, [t])

  return (
    <Box sx={{ mt: 4 }}>
      <SettingsSection id="entitlements" icon={ShieldCheck} accent="emerald" title={t('entitlements.title')} description={t('entitlements.body')}>
        {error && <Alert severity="error">{error}</Alert>}
        {items === null && !error && <Skeleton variant="rounded" height={80} />}
        {items !== null && items.length === 0 && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('entitlements.none')}</Typography>}
        {items !== null && items.length > 0 && (
          <Box sx={{ display: 'grid', gap: 1.5 }}>{items.map((e) => <EntitlementCard key={e.subscriptionId} entitlement={e} />)}</Box>
        )}
      </SettingsSection>
    </Box>
  )
}
