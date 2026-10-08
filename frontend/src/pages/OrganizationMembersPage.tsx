import { UsersRound } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Box, Tab, Tabs } from '@mui/material'
import { PageHeader } from '../components/layout/PageHeader'
import { useTabParam } from '../components/layout/useTabParam'
import { OrganizationGroupsCard } from '../components/organization/OrganizationGroupsCard'
import { OrganizationMembersCard } from '../components/organization/OrganizationMembersCard'
import { OrganizationStructurePanel } from '../components/organization/OrganizationStructurePanel'

const TABS = ['members', 'groups', 'structure'] as const

/**
 * "/organization/members" — C80: members and their roles (REQ-IAM-002) and
 * groups (REQ-TEN-003), the two cards that used to be the top of
 * "/organization/settings". Each card loads its own data and hides itself if
 * the backend refuses. Role assignment for a member happens here.
 */
export function OrganizationMembersPage() {
  const { t } = useTranslation()
  const [tab, setTab] = useTabParam(TABS, 'members')
  return (
    <Box sx={{ pb: 4 }}>
      <PageHeader icon={UsersRound} area="organization" title={t('orgMembers.title')} subtitle={t('orgMembers.subtitle')} />
      <Tabs value={tab} onChange={(_, v) => setTab(v)} aria-label={t('orgMembers.title')} sx={{ mb: 2.5, borderBottom: '1px solid', borderColor: 'divider' }}>
        <Tab value="members" label={t('orgMembers.tabs.members')} />
        <Tab value="groups" label={t('orgMembers.tabs.groups')} />
        <Tab value="structure" label={t('orgMembers.tabs.structure')} />
      </Tabs>
      {tab === 'members' && <OrganizationMembersCard />}
      {tab === 'groups' && <OrganizationGroupsCard />}
      {tab === 'structure' && <OrganizationStructurePanel />}
    </Box>
  )
}
