import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Badge, Box, Button, Skeleton, Snackbar, Tab, Tabs } from '@mui/material'
import { Percent } from 'lucide-react'
import { taxApi, type TaxReference, type TaxStatus } from '../../api/taxApi'
import { PageHeader } from '../../components/layout/PageHeader'
import { useTabParam } from '../../components/layout/useTabParam'
import { BlockedInvoicesPanel } from '../../components/tax/BlockedInvoicesPanel'
import { TaxClassesPanel } from '../../components/tax/TaxClassesPanel'
import { TaxEngineCard } from '../../components/tax/TaxEngineCard'
import { TaxExemptionsPanel } from '../../components/tax/TaxExemptionsPanel'
import { TaxProviderPanel } from '../../components/tax/TaxProviderPanel'
import { TaxRegistrationsPanel } from '../../components/tax/TaxRegistrationsPanel'
import { TaxRulesPanel } from '../../components/tax/TaxRulesPanel'
import { errorText } from '../../components/tax/taxFormat'

const TABS = ['rules', 'classification', 'exemptions', 'registrations', 'provider', 'blocked'] as const
type TabKey = typeof TABS[number]

/**
 * "/admin/billing/tax" — MANAGE_BILLING. Platform Admin → Billing → Tax Configuration (REQ-BIL-002, C96): the External Tax Engine
 * switch with the active mode and its status, and everything behind the two engines: rules, classes, exemptions, registrations,
 * the external provider and the Draft invoices waiting for tax.
 */
export function AdminTaxConfigurationPage() {
  const { t } = useTranslation()
  const [tab, setTab] = useTabParam<TabKey>(TABS, 'rules')
  const [status, setStatus] = useState<TaxStatus | null>(null)
  const [reference, setReference] = useState<TaxReference | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [toast, setToast] = useState<string | null>(null)

  const loadStatus = useCallback(() => taxApi.status().then(setStatus).catch((e) => setError(errorText(e, t('admin.tax.loadError')))), [t])
  const load = useCallback(() => {
    return Promise.all([taxApi.status(), taxApi.reference()])
      .then(([s, r]) => { setStatus(s); setReference(r); setError(null) })
      .catch((e) => setError(errorText(e, t('admin.tax.loadError'))))
  }, [t])
  useEffect(() => { void load() }, [load])

  return (
    <>
      <PageHeader icon={Percent} accent="violet" eyebrow={t('admin.tax.eyebrow')} title={t('admin.tax.title')} subtitle={t('admin.tax.subtitle')} />
      {error && <Alert severity="error" action={<Button onClick={() => void load()}>{t('admin.tax.retry')}</Button>} sx={{ mb: 3 }}>{error}</Alert>}
      {!error && (!status || !reference) && <Skeleton variant="rounded" height={260} />}
      {status && reference && (
        <>
          <TaxEngineCard status={status} onChanged={setStatus} onToast={setToast} />
          <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
            <Tabs value={tab} onChange={(_, v: TabKey) => setTab(v)} variant="scrollable" allowScrollButtonsMobile aria-label={t('admin.tax.tabs.label')}>
              {TABS.map((key) => (
                <Tab key={key} value={key} id={`tax-tab-${key}`} aria-controls={`tax-panel-${key}`}
                  label={key === 'blocked' && status.invoicesWaiting > 0
                    ? <Badge color="error" badgeContent={status.invoicesWaiting} sx={{ '& .MuiBadge-badge': { right: -14 } }}>{t(`admin.tax.tabs.${key}`)}</Badge>
                    : t(`admin.tax.tabs.${key}`)} />
              ))}
            </Tabs>
          </Box>
          <Box role="tabpanel" id={`tax-panel-${tab}`} aria-labelledby={`tax-tab-${tab}`}>
            {tab === 'rules' && <TaxRulesPanel status={status} reference={reference} onStatusChange={() => void loadStatus()} onToast={setToast} />}
            {tab === 'classification' && <TaxClassesPanel onToast={setToast} />}
            {tab === 'exemptions' && <TaxExemptionsPanel reference={reference} onToast={setToast} />}
            {tab === 'registrations' && <TaxRegistrationsPanel reference={reference} onToast={setToast} />}
            {tab === 'provider' && <TaxProviderPanel key={`${status.external.providerCode}|${status.external.companyCode}|${status.external.environment}|${status.external.timeoutSeconds}|${status.external.maxAttempts}`} status={status} onStatus={setStatus} onToast={setToast} />}
            {tab === 'blocked' && <BlockedInvoicesPanel onChanged={() => void loadStatus()} onToast={setToast} />}
          </Box>
        </>
      )}
      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast(null)} message={toast ?? ''} />
    </>
  )
}
