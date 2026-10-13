import { KeyRound, ShieldCheck } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Box, Tab, Tabs } from '@mui/material'
import { PageHeader } from '../components/layout/PageHeader'
import { useTabParam } from '../components/layout/useTabParam'
import { OrganizationMfaPolicyCard } from '../components/organization/OrganizationMfaPolicyCard'
import { OrganizationApiKeysSection } from '../components/organization/OrganizationApiKeysSection'
import { OrganizationSamlProvidersPage } from './OrganizationSamlProvidersPage'

const TABS = ['federation', 'mfa', 'apiKeys'] as const

/**
 * "/organization/identity-federation" — C80 (+ API keys, C92): the organization's sign-in
 * security in one place: identity federation (SAML, unchanged) and the
 * multi-factor policy (REQ-IAM-001, which used to sit on "/organization/settings").
 */
export function OrganizationSecurityPage() {
  const { t } = useTranslation()
  const [tab, setTab] = useTabParam(TABS, 'federation')
  return (
    <Box>
      <Tabs value={tab} onChange={(_, v) => setTab(v)} aria-label={t('orgSecurity.title')} sx={{ mb: 2.5, borderBottom: '1px solid', borderColor: 'divider' }}>
        <Tab value="federation" label={t('orgSecurity.tabs.federation')} />
        <Tab value="mfa" label={t('orgSecurity.tabs.mfa')} />
        <Tab value="apiKeys" label={t('orgSecurity.tabs.apiKeys')} />
      </Tabs>
      {tab === 'federation' ? (
        <OrganizationSamlProvidersPage />
      ) : tab === 'apiKeys' ? (
        <Box sx={{ pb: 4 }}>
          <PageHeader icon={KeyRound} area="organization" title={t('orgApiKeys.pageTitle')} subtitle={t('orgApiKeys.pageSubtitle')} />
          <OrganizationApiKeysSection />
        </Box>
      ) : (
        <Box sx={{ pb: 4 }}>
          <PageHeader icon={ShieldCheck} area="organization" title={t('orgSecurity.mfaTitle')} subtitle={t('orgSecurity.mfaSubtitle')} />
          <OrganizationMfaPolicyCard />
        </Box>
      )}
    </Box>
  )
}
