import { ShieldCheck } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Box, Tab, Tabs } from '@mui/material'
import { AccessActivityTab } from '../components/access/AccessActivityTab'
import { AccessMembersTab } from '../components/access/AccessMembersTab'
import { RoleDefaultsTab } from '../components/access/RoleDefaultsTab'
import { PageHeader } from '../components/layout/PageHeader'
import { useTabParam } from '../components/layout/useTabParam'

const TABS = ['members', 'roles', 'activity'] as const

/**
 * "/organization/access" — REQ-TEN-005 (C90): what each member can do and which products they can open. Members
 * (individual overrides, bulk change), Role defaults and Activity. Needs the feature permission "Manage access";
 * the backend decides every call.
 */
export function AccessManagementPage() {
  const { t } = useTranslation()
  const [tab, setTab] = useTabParam(TABS, 'members')
  return (
    <Box sx={{ pb: 4 }}>
      <PageHeader icon={ShieldCheck} area="organization" title={t('access.title')} subtitle={t('access.subtitle')} />
      <Tabs value={tab} onChange={(_, v) => setTab(v)} aria-label={t('access.title')} sx={{ mb: 2.5, borderBottom: '1px solid', borderColor: 'divider' }}>
        {TABS.map((k) => <Tab key={k} value={k} label={t(`access.tabs.${k}`)} />)}
      </Tabs>
      {tab === 'members' && <AccessMembersTab />}
      {tab === 'roles' && <RoleDefaultsTab />}
      {tab === 'activity' && <AccessActivityTab />}
    </Box>
  )
}
